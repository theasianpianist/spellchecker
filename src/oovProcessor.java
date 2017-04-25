/**
 * Created by LFQLE on 4/16/2017.
 */
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.optimaize.langdetect.*;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.tika.language.LanguageIdentifier;
import org.omg.CORBA.SystemException;


public class oovProcessor {


	public static String getLang(String text) throws Exception {
		LanguageIdentifier li = new LanguageIdentifier(text);
		String l = li.getLanguage();
		return l;
	}

	public static String stripPunc(String text) {
		String newString = "";
		for (int i = 0; i < text.length(); i++) {
			Character c = text.charAt(i);
			if (Character.isLetter(c) || c.toString().equals(" ")) {
				newString += c.toString();
			}
		}
		return newString;
	}

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
		int suggestionsNumber = 1;

		final File file = new File("Data");
		for(final File child : file.listFiles()) {
			try (
					InputStream fis = new FileInputStream(child.getPath());
					InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
					BufferedReader br = new BufferedReader(isr);
					PrintWriter writer = new PrintWriter("newTweets.txt", "UTF-8")
			) {
				while ((line = br.readLine()) != null) { //isolate the tweet body from each line, then run each word through the spell checker
					String[] parts = line.split(";");
					if (parts.length > 1) {
						String noPunc = stripPunc(parts[2]);
						if (!noPunc.equals("text") && getLang(noPunc).equals("en")) {
							String[] words = noPunc.split(" ");
							ArrayList<String> correctedList = new ArrayList<>();
							String fileName = child.getPath().split("_")[1];
							String newTweet = fileName.substring(0, fileName.length() - 4) + "\t";
							for (String word : words) {
								if (!spellChecker.exist(word.toLowerCase()) && word.length() > 2) {
									String[] suggestion = spellChecker.suggestSimilar(word.toLowerCase(), suggestionsNumber);
									if (suggestion != null && suggestion.length > 0) { //if the spell checker gives a suggestion, add the top suggestion to the corrected sentence, else add the original word
										correctedList.add(suggestion[0]);
									} else {
										correctedList.add(word);
									}
								} else {
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

	}

}
