import cluster.KMeans;
import matrix.MatrixUtils;
import model.StatData;
import preprocess.PreProcess;
import visual.Visualize;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {
        String extension = "txt";
        String pathName = "src/main/resources/dataset_3/data";
        PreProcess preProcess = new PreProcess();
        Map<String, String> documents = preProcess.preprocessDocument(extension, pathName);
        List<String> phrases = preProcess.getAllPhrasesInDocuments(documents);

        double[][] documentMatrix = MatrixUtils.calculateDocumentMatrix(documents, phrases);
        double[][] tfidf = MatrixUtils.convertToTfIdf(documentMatrix, documentMatrix.length, documentMatrix[0].length);

        MatrixUtils.write2DMatrixToCSV(tfidf, "tfidf");
        MatrixUtils.write2DMatrixToCSV(documentMatrix, "documentMatrix");

        MatrixUtils.generateTopicsPerFolder(tfidf);

        Map<Integer, List<Integer>> folderNumberToInts = MatrixUtils.getFolderToListOfIs();
        Map<Integer, Integer> documentNumberToLabelNumber = MatrixUtils.getDocumentNumberToLabelNumber();

        int k = 3;

        int highestMin = Integer.MIN_VALUE;
        int[] bestLabels = new int[tfidf.length];

        for (int iter = 0; iter < 20; iter++) {
            KMeans kMeans = new KMeans(tfidf, k, 10, "cosin", documentNumberToLabelNumber);
            kMeans.kmeans();

            double[][] clusters = kMeans.getCentroids();

            int[] labels = kMeans.getLabel();

            Map<Integer, Integer> numberPerLabel = new HashMap<>();


            for (int i = 0; i < labels.length; i++) {
                numberPerLabel.merge(labels[i], 1, Integer::sum);
            }

            int localMin = Integer.MAX_VALUE;

            for(Map.Entry<Integer, Integer> number : numberPerLabel.entrySet()) {
                if(number.getValue() < localMin) {
                    localMin = number.getValue();
                }
            }

            System.out.println("LocalMin is: " + localMin);

            if(localMin > highestMin && numberPerLabel.size() == k) {
                highestMin = localMin;
                bestLabels = labels;
            }

             System.out.println("#" + iter + ": " + numberPerLabel);
        }

        System.out.println();
        for (int i = 0; i < bestLabels.length; i++) {
            System.out.print(bestLabels[i] + " ");
        }

        System.out.println();

        // Visualize.visualize(tfidf);

        int[][] confusionMatrix = MatrixUtils.generateConfusionMatrix(bestLabels);

        System.out.println("Printing confusion matrix");
        System.out.println("------------------------------------");
        for (int i = 0; i < confusionMatrix.length; i++) {
            for (int j = 0; j < confusionMatrix[0].length; j++) {
                System.out.print(confusionMatrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("------------------------------------");

        StatData[] statDatas = MatrixUtils.getPrecisionAndRecall(confusionMatrix);
        for (StatData statData : statDatas) {
            System.out.println("vals are: " + statData);
        }

    }

    public static double[][] readCSVToArray() throws IOException {
        String fName = "iris.csv";
        String thisLine;
        int count=0;
        FileInputStream fis = new FileInputStream(fName);
        DataInputStream myInput = new DataInputStream(fis);
        int i=0;
        double[][] array = new double[150][4];

        while ((thisLine = myInput.readLine()) != null) {
            String strar[] = thisLine.split(",");
            int j = 0;
            for(String s : strar) {
                double num = Double.parseDouble(s);
                array[i][j] = num;
                j++;
            }
            i++;
        }

        return array;
    }
}
