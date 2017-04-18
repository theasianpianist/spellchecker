/**
 * Created by LFQLE on 4/16/2017.
 */
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import static org.apache.lucene.util.Version.*;

public class oovProcessor {

	public static void main(String[] args) throws Exception {
		SpellChecker spellChecker = null;
		String dictPath = "D:\\Documents\\Code\\Tweet Analysis\\dict.txt";
		String line;

		Directory directory = new RAMDirectory();
		spellChecker = new SpellChecker(directory);
		InputStream input = new FileInputStream(dictPath);
		PlainTextDictionary dict = new PlainTextDictionary(input);

		StandardAnalyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);

		spellChecker.indexDictionary(dict, config, true);
		int suggestionsNumber = 5;

		try (
				InputStream fis = new FileInputStream("tweets.csv");
				InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
				BufferedReader br = new BufferedReader(isr);
				PrintWriter writer = new PrintWriter("newTweets.txt", "UTF-8")
		) {
			while ((line = br.readLine()) != null) { //isolate the tweet body from each line, then run each word through the spell checker
				String[] parts = line.split(";");
				if (parts.length > 1) {
					String[] words = parts[2].split(" ");
					ArrayList<String> correctedList = new ArrayList<>();
					String newTweet = "";
 					for (String word : words) {
						if (!spellChecker.exist(word) && word.length() > 2) {
							String[] suggestion = spellChecker.suggestSimilar(word.toLowerCase(), suggestionsNumber);
							if (suggestion != null && suggestion.length > 0) { //if the spell checker gives a suggestion, add the top suggestion to the corrected sentence, else add the original word
								correctedList.add(suggestion[0]);
							} else {
								correctedList.add(word);
							}
						}
						else {
							correctedList.add(word.toLowerCase());
						}
					}
					for (String word : correctedList) {
						newTweet += word + " ";
					}
					writer.println(newTweet);
				}
			}
		}

	}

}
