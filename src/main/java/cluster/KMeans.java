package cluster;

import java.util.*;

public class KMeans {

    private double[][] data;
    private int k;

    private int iterations;

    private int numDocuments;
    private int numWords;

    private double[][] centroids;
    private int[] label;

    private String similarity;

    private double maxDistance;

    private int[] centroidLabels;

    private Map<Integer, Integer> documentNumberToLabelNumber;
    private Map<Integer, List<Integer>> folderToListOfIs;

    public KMeans(double[][] data, int k, int iterations, String similarity, Map<Integer, Integer> documentNumberToLabelNumber, Map<Integer, List<Integer>> folderToListOfIs) {
        this.data = data;
        this.k = k;
        this.iterations = iterations;
        this.numDocuments = data.length; // rows
        this.numWords = data[0].length; // columns
        this.label = new int[numDocuments];
        this.similarity = similarity;
        this.maxDistance = Double.POSITIVE_INFINITY;
        this.centroidLabels = new int[k];
        this.documentNumberToLabelNumber = documentNumberToLabelNumber;
        this.folderToListOfIs = folderToListOfIs;
    }

    public void kmeans() {

        // randomly fill centroids
        randomlyFillCentroids();
        double threshhold = 0.001;

        double[][] iterCentroids = centroids;

        int iteration = 0;

        do {

            // set centroids to previous results
            centroids = iterCentroids;

            // assignLabels each point to the closest centroid
            assignLabels();

            // updateCentroids
            iterCentroids = updateCentroids();

            iteration++;

        } while (!stopCondition(threshhold, iterCentroids, iteration));
    }

    private boolean stopCondition(double threshhold, double[][] roundCentroids, int iteration) {
        if(iteration >= iterations) {
            return true;
        }
        double max = 0;
        for (int i = 0; i < k; i++) {
            double tempDistance = distance(centroids[i], roundCentroids[i]);
            if (tempDistance > max) {
                max = tempDistance;
            }
        }

        if (Math.abs(max - maxDistance) < threshhold) {
            return true;
        } else {
            maxDistance = max;
            return false;
        }


    }

    private double[][] updateCentroids() {

        double[][] tempCentroids = new double[k][numWords]; // 3 x 64
        int[] tally = new int[k];

        // initialize to zero
        for (int i = 0; i < k; i++) {
            tally[i] = 0;
            for (int j = 0; j < numWords; j++) {
                tempCentroids[i][j] = 0.0;
            }
        }

        // do the sums
        for (int i = 0; i < numDocuments; i++) {
            int value = label[i]; // get the document's label (i.e. 0, 1, 2)
            for (int j = 0; j < numWords; j++) {
                tempCentroids[value][j] += data[i][j];
            }
            tally[value]++;
        }

        // get the average
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < numWords; j++) {
                tempCentroids[i][j] /= (double) tally[i]; // could have division by zero
            }
        }


        return tempCentroids;
    }

    private void randomlyFillCentroids() {
        centroids = new double[k][numWords];

        for (int i = 0; i < k; i++) {

            List<Integer> listOfDocumentNumbers = folderToListOfIs.get(i);
            int random = listOfDocumentNumbers.get(new Random().nextInt(listOfDocumentNumbers.size()));

            for (int j = 0; j < numWords; j++) {
                centroids[i][j] = data[random][j];
            }

            centroidLabels[i] = documentNumberToLabelNumber.get(random);
        }
    }

    private void assignLabels() {

        for (int i = 0; i < numDocuments; i++) {

            double minDistance = Double.POSITIVE_INFINITY;
            int minIndex = 0;

            for (int j = 0; j < k; j++) {

                double distance = distance(data[i], centroids[j]);

                if (distance < minDistance) {
                    minDistance = distance;
                    minIndex = j;
                }
            }
            label[i] = centroidLabels[minIndex];
        }
    }

    private double distance(double[] x, double[] y) {
        if (similarity.equalsIgnoreCase("euclidean")) {
            return euclideanDistance(x, y);
        } else {
            return cosinDistance(x, y);
        }
    }

    private double cosinDistance(double[] x, double[] y) {

        double dot = 0;
        double magA = 0;
        double magB = 0;

        for (int i = 0; i < x.length; i++) {
            dot += x[i] * y[i];
            magA += x[i] * x[i];
            magB += y[i] * y[i];
        }

        magA = Math.sqrt(magA);
        magB = Math.sqrt(magB);

        double cosin_similarity = dot / (magA * magB);

        return cosin_similarity;
    }

    private double euclideanDistance(double[] x, double[] y) {
        double sum = 0;

        for (int i = 0; i < x.length; i++) {
            double value = x[i] - y[i];
            sum += value * value;
        }

        return Math.sqrt(sum);
    }

    public double[][] getCentroids() {
        return centroids;
    }

    public int[] getLabel() {
        return label;
    }
}
