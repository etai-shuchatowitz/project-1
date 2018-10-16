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
    private static Map<Integer, String> documentNumberToDocumentName = new HashMap<>();
    private static Map<Integer, String> wordNumberToPhrase = new HashMap<>();

    private static Map<Integer, List<Integer>> folderToListOfIs = new HashMap<>();

    public static double[][] calculateDocumentMatrix(Map<String, String> textPerDoc, List<String> allPhrases) {

        double[][] documentMatrix = new double[textPerDoc.size()][allPhrases.size()]; // initialize matrix of docs by phrases

        int documentNumber = 0;

        for (Map.Entry<String, String> document : textPerDoc.entrySet()) {
            String text = document.getValue();

            String documentName = document.getKey();

            documentNumberToDocumentName.put(documentNumber, documentName);

            fillFolderToListOfIs(documentName, documentNumber);

            int wordNumber = 0;

            for (String phrase : allPhrases) {

                wordNumberToPhrase.put(wordNumber, phrase);
                String wordWithSpaces;

                // Complicated edge case handling
                if(text.lastIndexOf(phrase) == text.length() - phrase.length()) {
                    wordWithSpaces = " " + phrase; // need to surround with spaces to account for substrings
                } else if(text.lastIndexOf(phrase) == 0) {
                    wordWithSpaces = phrase + " "; // need to surround with spaces to account for substrings
                } else {
                   wordWithSpaces = " " + phrase + " "; // need to surround with spaces to account for substrings
                }

                double count = StringUtils.countMatches(text, wordWithSpaces);

                if(count > 0) {
                    numberOfDocsContainingWord.merge(phrase, 1, Integer::sum);
                }

                int numWords = text.split(" ").length;
                wordsPerDoc.put(document.getKey(), numWords);

                documentMatrix[documentNumber][wordNumber] = count;
                wordNumber++;
            }

            documentNumber++;
        }

        return documentMatrix;
    }

    private static void fillFolderToListOfIs(String documentName, int i) {
        List<Integer> integers;
        if(documentName.contains("C1")) {
            integers = folderToListOfIs.getOrDefault(0, new ArrayList<>());
            integers.add(i);
            folderToListOfIs.put(0, integers);
        } else if (documentName.contains("C4")) {
            integers = folderToListOfIs.getOrDefault(1, new ArrayList<>());
            integers.add(i);
            folderToListOfIs.put(1, integers);
        } else {
            integers = folderToListOfIs.getOrDefault(2, new ArrayList<>());
            integers.add(i);
            folderToListOfIs.put(2, integers);
        }
    }

    public static double[][] convertToTfIdf(double[][] documentMatrix, int x, int y) {
        double[][] tfidfMatrix = new double[x][y];

        for (Map.Entry<Integer, List<Integer>> folder : folderToListOfIs.entrySet()) {
            System.out.println(folder);
        }

        for(int document = 0; document < x; document++) {

            for (int word = 0; word < y; word++) {

                double numberOfAppearences = documentMatrix[document][word];

                String docName = documentNumberToDocumentName.get(document);
                int numWordsInDoc = wordsPerDoc.get(docName);
                double tf = numberOfAppearences / (double) numWordsInDoc;

                String phrase = wordNumberToPhrase.get(word);

                double idf = Math.log( (double) x / (double) numberOfDocsContainingWord.get(phrase));
                double tfidf = tf * idf;
                if(numberOfDocsContainingWord.get(phrase) <= 8) {
                    tfidfMatrix[document][word] = tfidf;
                }
            }
        }

        return tfidfMatrix;
    }

    public static void generateTopicsPerFolder(double[][] tfidf) throws IOException {

        for (Map.Entry<Integer, List<Integer>> folder : folderToListOfIs.entrySet()) {
            double[][] folderTfIdfMatrix = new double[folder.getValue().size()][tfidf[0].length];

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
                sumToPhrase.put(sumPerColumn, wordNumberToPhrase.get(j));
            }

            //sort the collection in reverse order
            sums.sort(Collections.reverseOrder());

            // make a list of keywords
            Set<String> keywords = new HashSet<>();
            for (double sum : sums) {
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

    public static int[][] generateConfusionMatrix(int[] labels) {
        int[][] confusionMatrix = new int[folderToListOfIs.keySet().size()][folderToListOfIs.keySet().size()];
        for (Map.Entry<Integer, List<Integer>> folder : folderToListOfIs.entrySet()) {
            int actualLabel = folder.getKey();
            for(int i = 0; i < folder.getValue().size(); i++) {
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
                fMeasure = 2 * ( (recall * precision) / (recall + precision) );
            }

            StatData val = new StatData(precision, recall, fMeasure);

            statDatas[i] = val;
        }
        return statDatas;
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

    public static Map<Integer, List<Integer>> getFolderToListOfIs() {
        return folderToListOfIs;
    }
}
