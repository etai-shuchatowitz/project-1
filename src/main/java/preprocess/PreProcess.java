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

    private static Set<String> stopWords = new HashSet<>();
    private static Set<String> nGrams = new HashSet<>();
    private static Map<String, String> docNameToString = new HashMap<>();
    private static Set<String> allWordsInDocs = new HashSet<>();

    public static void main(String[] args) throws IOException {
        fillStopWords();
        iterateThroughText();
    }

    public static void fillStopWords() {
        // https://github.com/Yoast/YoastSEO.js/blob/develop/src/config/stopwords.js
        try {
            stopWords  = new HashSet<>(Files.readAllLines(Paths.get("src/main/resources/stop_words.txt")));
        } catch (IOException e) {
            LOG.error("Error parsing data");
        }
    }

    private static void fillNGrams(Map<String, Integer> nGramFrequency) {
        for (Map.Entry<String, Integer> entry : nGramFrequency.entrySet()) {
            if (entry.getValue() > 2) {
                //System.out.println(entry.getKey());
                nGrams.add(entry.getKey());
            }
        }
    }

    private static void buildDocToWordFrequencyMatrix() {



    }

    public static void iterateThroughText() throws IOException {

        Map<String, Integer> nGramFrequnecy = new HashMap<>();

        String[] extensions = {"txt"};
        Iterator<File> iterator = FileUtils.iterateFiles(new File("src/main/resources/dataset_3/data"), extensions, true);
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
                            allWordsInDocs.add(parsedWord);
                            stringBuffer.append(parsedWord + " ");
                        }
                    }

                });
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }

            Map<String, Integer> docNGramFrequnecy = CreateNGrams.createNGrams(nGramFrequnecy, stringBuffer.toString());
            docNGramFrequnecy.forEach(nGramFrequnecy::putIfAbsent);

            docNameToString.put(path.getFileName().toString(), stringBuffer.toString()); // store each doc in its own string for later preprocessing.
        }

        fillNGrams(nGramFrequnecy);

        System.out.println(allWordsInDocs);
    }
}
