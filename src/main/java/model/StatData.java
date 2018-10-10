package model;

public class StatData {



    private double precision;
    private double recall;
    private double fMeasure;

    public StatData(double precision, double recall, double fMeasure) {
        this.fMeasure = fMeasure;
        this.precision = precision;
        this.recall = recall;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getfMeasure() {
        return fMeasure;
    }
    @Override
    public String toString() {
        return "StatData{" +
                "precision=" + precision +
                ", recall=" + recall +
                ", fMeasure=" + fMeasure +
                '}';
    }
}
