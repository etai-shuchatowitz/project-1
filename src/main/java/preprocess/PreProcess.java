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

    private static LinkedHashMap<String, String> lemmasizeDocs(Map<String, String> docToText) {

        LinkedHashMap<String, String> lemmasizedDocs = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : docToText.entrySet()) {
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

    private static List<String> findNGrams(Map<String, String> docToText) throws IOException {

        List<String> nGrams = new ArrayList<>();

        Map<String, Integer> nGramFrequency = new HashMap<>();

        for (Map.Entry<String, String> entry : docToText.entrySet()) {
            String text = entry.getValue();
            Map<String, Integer> docNGramFrequnecy = CreateNGrams.createNGrams(nGramFrequency, text);
            docNGramFrequnecy.forEach(nGramFrequency::putIfAbsent);
        }

        for (Map.Entry<String, Integer> nGramEntry : nGramFrequency.entrySet()) {
            if (nGramEntry.getValue() > 15 && nGramEntry.getKey().length() > 3) {
                //System.out.println(entry.getKey());
                nGrams.add(nGramEntry.getKey());
            }
        }

        return nGrams;

    }

    /**
     * Iterate through all of the files in the path and return a map from the path to
     * a string of words in the text with stop words removed
     *
     * @throws IOException
     */
    public static LinkedHashMap<String, String> getDocAsStringWithoutStopwords(String pathName, String extension, Set<String> stopWords) throws IOException {

        LinkedHashMap<String, String> docNameToString = new LinkedHashMap<>();

        String[] extensions = {extension};
        Iterator<File> iterator = FileUtils.iterateFiles(new File(pathName), extensions, true); //
        while (iterator.hasNext()) { // for each file in the folder
            StringBuffer stringBuffer = new StringBuffer();
            Path path = Paths.get(iterator.next().getAbsolutePath());
            try (Stream<String> stream = Files.lines(path)) {
                stream.forEach(file -> {
                    String stringWithoutPunctuation = file.replaceAll("\\p{Punct}", "").toLowerCase(); // remove punctuation per line
                    List<String> splitWords = new ArrayList<>(Arrays.asList(stringWithoutPunctuation.split(" ")));
                    splitWords.removeAll(stopWords); // remove the stop words
                    for (String word : splitWords) {
                        String parsedWord = word.trim();
                        if (parsedWord.length() > 0) {
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

    public LinkedHashMap<String, String> preprocessDocument(String extension, String pathName) throws IOException {
        Set<String> stopWords = fillStopWords();
        LinkedHashMap<String, String> docNameToStringWithoutStopwords = getDocAsStringWithoutStopwords(pathName, extension, stopWords);
        return lemmasizeDocs(docNameToStringWithoutStopwords);
    }

    public List<String> getAllPhrasesInDocuments(Map<String, String> documents) throws IOException {
        List<String> nGrams = findNGrams(documents);
        System.out.println(nGrams.size());
        for (String nGram : nGrams) {
            System.out.println(nGram);
        }
        return nGrams;
    }
}
