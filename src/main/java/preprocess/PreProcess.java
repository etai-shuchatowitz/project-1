package preprocess;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import preprocess.utils.CreateNGrams;
import preprocess.utils.StanfordNLPClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class PreProcess {

    private static final Logger LOG = LogManager.getLogger(StanfordNLPClient.class);

    public Map<String, String> preprocessDocument(String extension, String pathName) throws IOException {
        Set<String> stopWords = fillStopWords();
        Map<String, String> docNameToStringWithoutStopwords = getDocAsStringWithoutStopwords(pathName, extension, stopWords);
        return lemmasizeDocs(docNameToStringWithoutStopwords);
    }

    public Set<String> getAllPhrasesInDocuments(Map<String, String> documents) throws IOException {
        Set<String> nGrams = findNGrams(documents);
        return calculateAllPhrases(nGrams, documents);
    }



    private static Set<String> calculateAllPhrases(Set<String> nGrams, Map<String, String> docsToText) {

        Set<String> allPhrases = new HashSet<>();
        for (Map.Entry<String, String> entry : docsToText.entrySet()) {
            Set<String> phrases = new HashSet<>(Arrays.asList(entry.getValue().split(" ")));
            allPhrases.addAll(phrases);
        }
        allPhrases.addAll(nGrams);

        System.out.println(allPhrases.size());

        return allPhrases;
    }

    private static Map<String, String> lemmasizeDocs(Map<String, String> docToText) {

        Map<String, String> lemmasizedDocs = new HashMap<>();
        for(Map.Entry<String, String> entry : docToText.entrySet()) {
            String lemmasizedText = StanfordNLPClient.annotateDocument(entry.getValue());
            lemmasizedDocs.put(entry.getKey(), lemmasizedText);
        }

        return lemmasizedDocs;
    }

    private static Set<String> fillStopWords() {

        Set<String> stopWords = null;
        // https://github.com/Yoast/YoastSEO.js/blob/develop/src/config/stopwords.js
        try {
            stopWords = new HashSet<>(Files.readAllLines(Paths.get("src/main/resources/stop_words.txt")));
        } catch (IOException e) {
            LOG.error("Error parsing data");
        }

        return stopWords;
    }

    private static Set<String> findNGrams(Map<String, String> docToText) throws IOException {

        Set<String> nGrams = new HashSet<>();

        Map<String, Integer> nGramFrequency = new HashMap<>();
        for (Map.Entry<String, String> entry : docToText.entrySet() ) {
            String text = entry.getValue();
            Map<String, Integer> docNGramFrequnecy = CreateNGrams.createNGrams(nGramFrequency, text);
            docNGramFrequnecy.forEach(nGramFrequency::putIfAbsent);
        }

        for (Map.Entry<String, Integer> entry : nGramFrequency.entrySet()) {
            if (entry.getValue() > 2) {
                nGrams.add(entry.getKey());
            }
        }

        return nGrams;

    }

    /**
     * Iterate through all of the files in the path and return a map from the path to
     * a string of words in the text with stop words removed
     * @throws IOException
     */
    public static Map<String, String> getDocAsStringWithoutStopwords(String pathName, String extension, Set<String> stopWords) throws IOException {

        Map<String, String> docNameToString = new HashMap<>();

        String[] extensions = {extension};
        Iterator<File> iterator = FileUtils.iterateFiles(new File(pathName), extensions, true); //
        while(iterator.hasNext()) { // for each file in the folder
            StringBuffer stringBuffer = new StringBuffer();
            Path path = Paths.get(iterator.next().getAbsolutePath());
            try(Stream<String> stream = Files.lines(path)) {
                stream.forEach( file ->  {
                    String stringWithoutPunctuation = file.replaceAll("\\p{Punct}", "").toLowerCase(); // remove punctuation per line
                    List<String> splitWords = new ArrayList<>(Arrays.asList(stringWithoutPunctuation.split(" ")));
                    splitWords.removeAll(stopWords); // remove the stop words
                    for (String word : splitWords) {
                        String parsedWord = word.trim();
                        if(parsedWord.length() > 0) {
                            stringBuffer.append(parsedWord + " ");
                        }
                    }
                });
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
            docNameToString.put(path.toString(), stringBuffer.toString()); // store each doc in its own string for later preprocessing.
        }
        return docNameToString;
    }
}
