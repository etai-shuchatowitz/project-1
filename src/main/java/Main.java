import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import preprocess.CreateNGrams;
import preprocess.StanfordNLPClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Main {

    private static final Logger LOG = LogManager.getLogger(StanfordNLPClient.class);

    private static Set<String> stopWords = new HashSet<>();
    private static Set<String> nGrams = new HashSet<>();

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
                nGrams.add(entry.getKey());
            }
        }
    }


    public static void iterateThroughText() throws IOException {

        Map<String, Integer> nGramFrequnecy = new HashMap<>();

        String[] extensions = {"txt"};
        Iterator<File> iterator = FileUtils.iterateFiles(new File("src/main/resources/dataset_3/data"), extensions, true);
        while(iterator.hasNext()) {
            StringBuffer stringBuffer = new StringBuffer();
            try(Stream<String> stream = Files.lines(Paths.get(iterator.next().getAbsolutePath()))) {
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

            nGramFrequnecy = CreateNGrams.createNGrams(nGramFrequnecy, stringBuffer.toString());

        }
        fillNGrams(nGramFrequnecy);
    }
}
