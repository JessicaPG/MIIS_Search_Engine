// Copyright (C) 2015  Julián Urbano <urbano.julian@gmail.com>
// Distributed under the terms of the MIT License.

package ti;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


/**
 * A processor to extract terms from HTML documents.
 */
public class HtmlProcessor implements DocumentProcessor {

	List<String> stopWors = new ArrayList<>();

	/**
	 * Creates a new HTML processor.
	 *
	 * @param pathToStopWords the path to the file with stopwords, or {@code null} if stopwords are not filtered.
	 * @throws IOException if an error occurs while reading stopwords.
	 */
	public HtmlProcessor(File pathToStopWords) throws IOException {

		//read file into stream, try-with-resources
		try (Stream<String> stream = Files.lines(Paths.get(pathToStopWords.getPath()))) {
			stopWors = stream.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Tuple<String, String> parse(String html) {

		Document doc = Jsoup.parse(html);
		String title = "";
		String body = "";

		if (doc.title() != null) {
			title = doc.title();
		}

		if (doc.body() != null) {
			body = doc.body().text();
		}

		return new Tuple(title, body);
	}

	/**
	 * Process the given text (tokenize, normalize, filter stopwords and stemize) and return the list of terms to index.
	 *
	 * @param text the text to process.
	 * @return the list of index terms.
	 */
	public ArrayList<String> processText(String text)
	{
		ArrayList<String> terms = new ArrayList<>();
		ArrayList<String> tokens = tokenize(text);

		for (String term :tokens) {
			term = normalize(term);
			if (!isStopWord(term)) {
				terms.add(stem(term));
			}
		}
		return terms;
	}

	/**
	 * Tokenize the given text.
	 *
	 * @param text the text to tokenize.
	 * @return the list of tokens.
	 */
	protected ArrayList<String> tokenize(String text)
	{
		ArrayList<String> tokens = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(text);

		while (st.hasMoreTokens()) {
			tokens.add(st.nextToken());
		}
		return tokens;
	}

	/**
	 * Normalize the given term.
	 *
	 * @param text the term to normalize.
	 * @return the normalized term.
	 */
	protected String normalize(String text){
		return text.toLowerCase();
	}

	/**
	 * Checks whether the given term is a stopword.
	 *
	 * @param term the term to check.
	 * @return {@code true} if the term is a stopword and {@code false} otherwise.
	 */
	protected boolean isStopWord(String term)
	{
		//boolean isTopWord = false;
		return this.stopWors.contains(term)? true :false;

	}

	/**
	 * Stem the given term.
	 *
	 * @param term the term to stem.
	 * @return the stem of the term.
	 */
	protected String stem(String term)
	{
		String stem = null;
		Stemmer lemma = new Stemmer();
		lemma.add(term.toCharArray(),term.length());
		lemma.stem();
		stem = lemma.toString();

		return stem;
	}
}
