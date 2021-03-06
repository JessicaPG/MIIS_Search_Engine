// Copyright (C) 2015  Julián Urbano <urbano.julian@gmail.com>
// Distributed under the terms of the MIT License.

package ti;
import java.util.*;

/**
 * Implements retrieval in a vector space with the cosine similarity function and a TFxIDF weight formulation.
 */
public class Cosine implements RetrievalModel
{
	public Cosine() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<Tuple<Integer, Double>> runQuery(String queryText, Index index, DocumentProcessor docProcessor)
	{
		ArrayList<String> terms = docProcessor.processText(queryText);
		ArrayList<Tuple<Integer, Double>> vectorQuery = computeVector(terms,index);

		return computeScores(vectorQuery,index); //return results (docID, similarity)
	}

	/**
	 * Returns the list of documents in the specified index sorted by similarity with the specified query vector.
	 *
	 * @param queryVector the vector with query term weights.
	 * @param index       the index to search in.
	 * @return a list of {@link Tuple}s where the first item is the {@code docID} and the second one the similarity score.
	 */
	protected ArrayList<Tuple<Integer, Double>> computeScores(ArrayList<Tuple<Integer, Double>> queryVector, Index index)
	{
		ArrayList<Tuple<Integer, Double>> results;
		HashMap<Integer, Tuple<Integer, Double>> checkDocs = new HashMap<>();
		double queryWeight = 0, sumNum = 0, termWeight= 0, termNorm = 0, queryNorm = 0;
		queryNorm = calculateNormQuery(queryVector);

		//Iterate over terms in query
		for (Tuple<Integer, Double> term: queryVector) {
			int termID = term.item1;
			queryWeight = term.item2;
			ArrayList<Tuple<Integer, Double>> ListDoc = index.invertedIndex.get(termID);

			//Iterate over docs associated with terms and calculate de similarity score
			for (Tuple<Integer, Double> docs : ListDoc) {
				termWeight = docs.item2;
				sumNum = termWeight * queryWeight;
				termNorm  = index.documents.get(docs.item1).item2;

				//Check if the doc has already been computated
				if (checkDocs.get(docs.item1) != null){
					//update acc similarity of doc already visited
					checkDocs.get(docs.item1).item2 += sumNum/(termNorm * queryNorm);

				}else{ //New doc in results
					checkDocs.put(docs.item1, new Tuple(docs.item1,sumNum/(termNorm * queryNorm)));
				}

			}

		}

		//Transform hash into an arraylist
		results = new ArrayList(checkDocs.values());

		// Sort documents by similarity and return the ranking
		Collections.sort(results, (o1,o2) -> o2.item2.compareTo(o1.item2));/*new Comparator<Tuple<Integer, Double>>()

		{
			@Override
			public int compare(Tuple<Integer, Double> o1, Tuple<Integer, Double> o2)
			{
				return o2.item2.compareTo(o1.item2);
			}
		});*/
		return results;
	}

	protected double calculateNormQuery (ArrayList<Tuple<Integer, Double>> queryVector){
		double queryWeight = 0, queryNorms = 0;
		for (Tuple<Integer, Double> term: queryVector) {
			queryWeight = term.item2;
			queryNorms += Math.pow(queryWeight,2.0);
		}
		return Math.sqrt(queryNorms);
	}

	/**
	 * Compute the vector of weights for the specified list of terms.
	 *
	 * @param terms the list of terms.
	 * @param index the index
	 * @return a list of {@code Tuple}s with the {@code termID} as first item and the weight as second one.
	 */
	protected ArrayList<Tuple<Integer, Double>> computeVector(ArrayList<String> terms, Index index)
	{
		ArrayList<Tuple<Integer, Double>> vector = new ArrayList<>();
		HashSet<String> setTerms = new HashSet<>(terms);
		double weight = 0;

		for (String term : setTerms) {
			//frequency
			int freq = Collections.frequency(terms, term);
			double tftd = 1.0 + (Math.log(freq)/Math.log(2)); //Check only Math.log(freq)
			weight = tftd * index.vocabulary.get(term).item2;
			vector.add(new Tuple(index.vocabulary.get(term).item1, weight));
		}
		return vector;
	}
}
