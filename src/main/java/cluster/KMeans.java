package cluster;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class KMeans {

    private double[][] data;
    private int k;

    private int iterations;

    private int m;
    private int n;

    private double[][] centroids;
    private int[] label;

    private String similarity;

    public KMeans(double[][] data, int k, int iterations, String similarity) {
        this.data = data;
        this.k = k;
        this.iterations = iterations;
        this.m = data.length;
        this.n = data[0].length;
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


//        int tally0 = 0;
//        int tally1 = 0;
//        int tally2 = 0;
//        for (int i = 0; i < m; i++) {
//            switch (label[i]) {
//                case(0):
//                    tally0++;
//                    break;
//                case(1):
//                    tally1++;
//                    break;
//                case(2):
//                    tally2++;
//                    break;
//            }
//
//            System.out.println(label[i]);
//        }
//
//        System.out.println("there are " + tally0 + " 0s, " +tally1 + " 1s  and " + tally2 + " 2s");
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

    private double[][] updateCentroids() {

        double[][] tempCentroids = new double[k][n];
        int[] tally = new int[k];

        // initialize to zero
        for (int i = 0; i < k; i++) {
            tally[i] = 0;
            for (int j = 0; j < n; j++) {
                tempCentroids[i][j] = 0.0;
            }
        }

        // do the sums
        for (int i = 0; i < m; i++) {
            int value = label[i];
            for (int j = 0; j < n; j++) {
                tempCentroids[value][j] += data[i][j];
            }
            tally[value]++;
        }

        // get the average
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < n; j++) {
                tempCentroids[i][j] /= tally[i];
            }
        }

        return tempCentroids;
    }

    private void randomlyFillCentroids() {
        centroids = new double[k][n];

        Set<Integer> randomIntegers = new HashSet<>();

        for (int i = 0; i < k; i++) {

            int random;
            do {
                random = new Random().nextInt(m);
            } while (randomIntegers.contains(random));

            randomIntegers.add(random);

            for (int j = 0; j < n; j++) {
                centroids[i][j] = data[random][j];
            }
        }
    }

    private void assignLabels() {
        label = new int[m];

        for (int i = 0; i < m; i++) {

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
