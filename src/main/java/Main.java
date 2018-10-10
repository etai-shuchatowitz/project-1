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

        Integer[][] documentMatrix = MatrixUtils.calculateDocumentMatrix(documents, phrases);
        Double[][] tfidf = MatrixUtils.convertToTfIdf(documentMatrix, documentMatrix.length, documentMatrix[0].length);

        MatrixUtils.generateTopicsPerFolder(tfidf);
    }
}
