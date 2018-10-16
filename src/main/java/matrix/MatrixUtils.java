package matrix;

import model.StatData;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MatrixUtils {

    private static Map<String, Integer> wordsPerDoc = new HashMap<>();
    private static Map<String, Integer> numberOfDocsContainingWord = new HashMap<>();
    private static Map<Integer, String> intToDocumentName = new HashMap<>();
    private static Map<Integer, String> intToPhrase = new HashMap<>();

    private static Map<Integer, List<Integer>> folderToListOfDocInts = new HashMap<>(); // stores the folder number -> list of integers representing each file (ex: {0 -> [1, 3, 8, 24, 17, 5, 9, 4]}

    public static double[][] calculateDocumentMatrix(LinkedHashMap<String, String> textPerDoc, Set<String> allPhrases) throws IOException {

        List<String> phraseList = new ArrayList<>(allPhrases); // need to convert to list to preserve order

        double[][] documentMatrix = new double[textPerDoc.size()][allPhrases.size()]; // initialize matrix of docs by phrases

        int i = 0;

        for (Map.Entry<String, String> document : textPerDoc.entrySet()) {

            String documentName = document.getKey();

            intToDocumentName.put(i, documentName);

            fillFolderToListOfDocInts(documentName, i);
            int j = 0;

            for (String phrase : phraseList) {

                intToPhrase.put(j, phrase);

                String text = document.getValue();
                String wordWithSpaces;

                // Complicated edge case handling
                if (text.lastIndexOf(phrase) == text.length() - phrase.length()) {
                    wordWithSpaces = " " + phrase; // need to surround with spaces to account for substrings
                } else if (text.lastIndexOf(phrase) == 0) {
                    wordWithSpaces = phrase + " "; // need to surround with spaces to account for substrings
                } else {
                    wordWithSpaces = " " + phrase + " "; // need to surround with spaces to account for substrings
                }

                double count = StringUtils.countMatches(text, wordWithSpaces);

                if (count > 0) {
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

        write2DMatrixToCSV(documentMatrix, "documentmatrix");
        return documentMatrix;
    }

    private static void fillFolderToListOfDocInts(String documentName, int i) {
        List<Integer> integers;
        if (documentName.contains("C1")) {
            integers = folderToListOfDocInts.getOrDefault(0, new ArrayList<>());
            integers.add(i);
            folderToListOfDocInts.put(0, integers);
        } else if (documentName.contains("C4")) {
            integers = folderToListOfDocInts.getOrDefault(1, new ArrayList<>());
            integers.add(i);
            folderToListOfDocInts.put(1, integers);
        } else {
            integers = folderToListOfDocInts.getOrDefault(2, new ArrayList<>());
            integers.add(i);
            folderToListOfDocInts.put(2, integers);
        }
    }

    public static double[][] convertToTfIdf(double[][] matrix, int x, int y) {
        double[][] tfidfMatrix = new double[x][y];

        for (Map.Entry<Integer, List<Integer>> folder : folderToListOfDocInts.entrySet()) {
            System.out.println(folder);
        }

        for (int i = 0; i < x; i++) {

            for (int j = 0; j < y; j++) {

                double value = matrix[i][j];

                String docName = intToDocumentName.get(i);
                int numWordsInDoc = wordsPerDoc.get(docName);
                double tf = value / (double) numWordsInDoc;

                String phrase = intToPhrase.get(j);

                double idf = Math.log((double) x / (double) numberOfDocsContainingWord.get(phrase));
                double tfidf = tf * idf;
                tfidfMatrix[i][j] = tfidf;
            }
        }

        return tfidfMatrix;
    }

    public static void generateTopicsPerFolder(double[][] tfidf) throws IOException {

        for (Map.Entry<Integer, List<Integer>> folder : folderToListOfDocInts.entrySet()) {
            double[][] folderTfIdfMatrix = new double[folder.getValue().size()][tfidf[0].length];

            // Create document matrix
            for (int i = 0; i < folder.getValue().size(); i++) {
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
                sumToPhrase.put(sumPerColumn, intToPhrase.get(j));
            }

            //sort the collection in reverse order
            sums.sort(Collections.reverseOrder());

            // make a list of keywords
            Set<String> keywords = new HashSet<>();
            for (double sum : sums) {
                if (sum > 0.1) {
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

    public static int[][] generateConfusionMatrix(int[] labels) {
        int[][] confusionMatrix = new int[folderToListOfDocInts.keySet().size()][folderToListOfDocInts.keySet().size()];
        for (Map.Entry<Integer, List<Integer>> folder : folderToListOfDocInts.entrySet()) {
            int actualLabel = folder.getKey();
            for (int i = 0; i < folder.getValue().size(); i++) {
                int predictedLabel = labels[i];
                confusionMatrix[predictedLabel][actualLabel]++;
            }
        }
        return confusionMatrix;
    }

    public static StatData[] getPrecisionAndRecall(int[][] confusionMatrix) {

        StatData[] statDatas = new StatData[confusionMatrix.length];

        // get precision
        for (int i = 0; i < confusionMatrix.length; i++) {

            int num = 0;
            int pdenom = 0;
            int rdenom = 0;
            double recall = 0;
            double precision = 0;
            double fMeasure = 0;
            StatData s;

            for (int j = 0; j < confusionMatrix[0].length; j++) {
                if (i == j) {
                    num = confusionMatrix[i][j];
                }
                pdenom += confusionMatrix[i][j];
                rdenom += confusionMatrix[j][i];
            }

            if (rdenom != 0) {
                recall = (double) num / (double) rdenom;
            }

            if (pdenom != 0) {
                precision = (double) num / (double) pdenom;
            }

            if (recall != 0 || precision != 0) {
                fMeasure = 2 * ((recall * precision) / (recall + precision));
            }

            StatData val = new StatData(precision, recall, fMeasure);

            statDatas[i] = val;
        }
        return statDatas;
    }

    public static Map<Integer, String> getIntToDocumentName() {
        return intToDocumentName;
    }

    public static Map<Integer, String> getIntToPhrase() {
        return intToPhrase;
    }

    public static Map<Integer, List<Integer>> getFolderToListOfDocInts() {
        return folderToListOfDocInts;
    }

    public static void write2DMatrixToCSV(double[][] matrix, String fileName) throws IOException {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < matrix.length; i++)//for each row
        {
            for(int j = 0; j < matrix[0].length; j++)//for each column
            {
                builder.append(matrix[i][j]+"");//append to the output string
                if(j < matrix[0].length - 1)//if this is not the last row element
                    builder.append(",");//then add comma (if you don't like commas you can use spaces)
            }
            builder.append("\n");//append new line at the end of the row
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName + ".csv"));
        writer.write(builder.toString());//save the string representation of the board
        writer.close();
    }
}
