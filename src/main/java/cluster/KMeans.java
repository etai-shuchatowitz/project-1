package cluster;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class KMeans {

    private double[][] data;
    private int k;

    private int iterations;

    private int rows; // 24
    private int columns; // 2376

    private double[][] centroids;
    private int[] label;

    private String similarity;

    public KMeans(double[][] data, int k, int iterations, String similarity) {
        this.data = data;
        this.k = k;
        this.iterations = iterations;
        this.rows = data.length;
        this.columns = data[0].length;
        this.similarity = similarity;
    }

    public void kmeans() {

        // randomly fill centroids
        randomlyFillCentroids();
        double threshhold = 0.001;
        int iteration = 0;

        double[][] iterCentroids = centroids;

        do {

            // set centroids to previous results
            centroids = iterCentroids;

            // assignLabels each point to the closest centroid
            assignLabels();

            // updateCentroids
            iterCentroids = updateCentroids();

            iteration++;

        } while (!stopCondition(iteration, threshhold, iterCentroids));
    }

    private boolean stopCondition(int iteration, double threshhold, double[][] roundCentroids) {
        if (iteration >= iterations) {
            return true;
        }
        double max = 0;
        for (int i = 0; i < k; i++) {
            double tempDistance = distance(centroids[i], roundCentroids[i]);
            if (tempDistance > max) {
                max = tempDistance;
            }
        }
        return max < threshhold;

    }

    private void assignLabels() {
        label = new int[rows];

        for (int i = 0; i < rows; i++) {

            double minDistance = Double.POSITIVE_INFINITY;
            int minJ = 0;

            for (int j = 0; j < k; j++) {
                double distance = distance(data[i], centroids[j]);
                if (distance < minDistance) {
                    minDistance = distance;
                    minJ = j;
                }
            }
            label[i] = minJ;
        }

    }

    private double[][] updateCentroids() {

        double[][] tempCentroids = new double[k][columns];
        int[] tally = new int[k];

        // initialize to zero
        for (int i = 0; i < k; i++) {
            tally[i] = 0;
            for (int j = 0; j < columns; j++) {
                tempCentroids[i][j] = 0.0;
            }
        }

        // do the sums
        for (int i = 0; i < rows; i++) {
            int value = label[i];
            for (int j = 0; j < columns; j++) {
                tempCentroids[value][j] += data[i][j];
            }
            tally[value]++;
        }

        // get the average
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < columns; j++) {
                tempCentroids[i][j] /= tally[i];
            }
        }

        return tempCentroids;
    }

    private void randomlyFillCentroids() {
        centroids = new double[k][columns];

        Set<Integer> randomIntegers = new HashSet<>();

        for (int i = 0; i < k; i++) {

            int random;
            do {
                random = new Random().nextInt(rows);
            } while (randomIntegers.contains(random));

            randomIntegers.add(random);

            for (int j = 0; j < columns; j++) {
                centroids[i][j] = data[random][j];
            }
        }
    }


    private double distance(double[] x, double[] y) {
        if (similarity.equalsIgnoreCase("euclidean")) {
            return euclideanDistance(x, y);
        }
        else {
            return cosinDistance(x, y);
        }
    }

    private double cosinDistance(double[] x, double[] y) {
        double dot = 0;
        double magA = 0;
        double magB = 0;

        for (int i=0; i < x.length; i++) {
            dot += x[i] * y[i];
            magA += x[i] * x[i];
            magB += y[i] * y[i];
        }

        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
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
