package matrix;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MatrixUtils {

    private static Map<String, Integer> wordsPerDoc = new HashMap<>();
    private static Map<String, Integer> numberOfDocsContainingWord = new HashMap<>();
    private static Map<Integer, String> iToDocumentName = new HashMap<>();
    private static Map<Integer, String> jToPhrase = new HashMap<>();

    private static Map<String, List<Integer>> folderToListOfIs = new HashMap<>();

    public static Integer[][] calculateDocumentMatrix(Map<String, String> textPerDoc, Set<String> allPhrases) {

        List<String> phraseList = new ArrayList<>(allPhrases); // need to convert to list to preserve order

        Integer[][] documentMatrix = new Integer[textPerDoc.size()][allPhrases.size()]; // initialize matrix of docs by phrases

        int i = 0;

        for (Map.Entry<String, String> document : textPerDoc.entrySet()) {

            String documentName = document.getKey();

            iToDocumentName.put(i, documentName);

            fillFolderToListOfIs(documentName, i);
            int j = 0;

            for (String phrase : phraseList) {

                jToPhrase.put(j, phrase);

                String text = document.getValue();
                String wordWithSpaces;

                // Complicated edge case handling
                if(text.lastIndexOf(phrase) == text.length() - phrase.length()) {
                    wordWithSpaces = " " + phrase; // need to surround with spaces to account for substrings
                } else if(text.lastIndexOf(phrase) == 0) {
                    wordWithSpaces = phrase + " "; // need to surround with spaces to account for substrings
                } else {
                   wordWithSpaces = " " + phrase + " "; // need to surround with spaces to account for substrings
                }

                int count = StringUtils.countMatches(text, wordWithSpaces);

                if(count > 0) {
                    int documentTally;
                    if (numberOfDocsContainingWord.get(phrase) != null) {
                        documentTally = numberOfDocsContainingWord.get(phrase);
                        documentTally++;
                    } else {
                        documentTally = 1;
                    }
                    numberOfDocsContainingWord.put(phrase, documentTally);
                }

                int numWords = text.split(" ").length;
                wordsPerDoc.put(document.getKey(), numWords);

                documentMatrix[i][j] = count;
                j++;
            }

            i++;
        }

        return documentMatrix;
    }

    private static void fillFolderToListOfIs(String documentName, int i) {
        List<Integer> integers;
        if(documentName.contains("C1")) {
            integers = folderToListOfIs.getOrDefault("C1", new ArrayList<>());
            integers.add(i);
            folderToListOfIs.put("C1", integers);
        } else if (documentName.contains("C4")) {
            integers = folderToListOfIs.getOrDefault("C4", new ArrayList<>());
            integers.add(i);
            folderToListOfIs.put("C4", integers);
        } else {
            integers = folderToListOfIs.getOrDefault("C7", new ArrayList<>());
            integers.add(i);
            folderToListOfIs.put("C7", integers);
        }
    }

    public static Double[][] convertToTfIdf(Integer[][] matrix, int x, int y) {
        Double[][] tfidfMatrix = new Double[x][y];

        for (Map.Entry<String, List<Integer>> folder : folderToListOfIs.entrySet()) {
            System.out.println(folder);
        }

        for(int i = 0; i < x; i++) {

            for (int j = 0; j < y; j++) {

                int value = matrix[i][j];

                String docName = iToDocumentName.get(i);
                int numWordsInDoc = wordsPerDoc.get(docName);
                double tf = (double) value / (double) numWordsInDoc;

                String phrase = jToPhrase.get(j);

                double idf = Math.log( (double) x / (double) numberOfDocsContainingWord.get(phrase));
                double tfidf = tf * idf;
                tfidfMatrix[i][j] = tfidf;
            }
        }

        return tfidfMatrix;
    }

    public static void generateTopicsPerFolder(Double[][] tfidf) throws IOException {

        for (Map.Entry<String, List<Integer>> folder : folderToListOfIs.entrySet()) {
            Double[][] folderTfIdfMatrix = new Double[folder.getValue().size()][tfidf[0].length];

            // Create document matrix
            for(int i = 0; i < folder.getValue().size(); i++) {
                folderTfIdfMatrix[i] = tfidf[folder.getValue().get(i)];
            }

            // Sum up cols and put them into an array
            List<Double> sums = new ArrayList<>();
            Map<Double, String> sumToPhrase = new HashMap<>();
            for (int j = 0; j < folderTfIdfMatrix[0].length; j++) {
                double sumPerColumn = 0;
                for (int i = 0; i < folderTfIdfMatrix.length; i++) {
                    sumPerColumn += folderTfIdfMatrix[i][j];
                }
                sums.add(sumPerColumn);
                sumToPhrase.put(sumPerColumn, jToPhrase.get(j));
            }


            //sort the collection in reverse order
            sums.sort(Collections.reverseOrder());

            // make a list of keywords
            Set<String> keywords = new HashSet<>();
            for (Double sum : sums) {
                if(sum > 0.1) {
                   keywords.add(sumToPhrase.get(sum));
                } else {
                    break;
                }
            }

            //write the keywords to a file
            Path file = Paths.get(folder.getKey() + ".txt");
            Files.write(file, keywords, Charset.forName("UTF-8"));
        }

    }
}
