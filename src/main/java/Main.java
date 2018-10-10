import cluster.KMeans;
import matrix.MatrixUtils;
import preprocess.PreProcess;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws IOException {
        String extension = "txt";
        String pathName = "src/main/resources/dataset_3/data";
        PreProcess preProcess = new PreProcess();

        Map<String, String> documents = preProcess.preprocessDocument(extension, pathName);
        Set<String> phrases = preProcess.getAllPhrasesInDocuments(documents);

        int[][] documentMatrix = MatrixUtils.calculateDocumentMatrix(documents, phrases);
        double[][] tfidf = MatrixUtils.convertToTfIdf(documentMatrix, documentMatrix.length, documentMatrix[0].length);

        MatrixUtils.generateTopicsPerFolder(tfidf);

        int k = 3;

        KMeans kMeans = new KMeans(tfidf, k, 1000, "cosin");
        kMeans.kmeans();

        double[][] clusters = kMeans.getCentroids();
        int[] labels = kMeans.getLabel();
    }
}
