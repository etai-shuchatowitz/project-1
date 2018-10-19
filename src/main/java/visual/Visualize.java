package visual;

import org.math.plot.Plot2DPanel;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dimensionalityreduction.PCA;
import org.nd4j.linalg.factory.Nd4j;

import javax.swing.*;


public class Visualize {

    public static void visualize(double[][] array) {

        INDArray matrix = Nd4j.create(array);
        INDArray factors = PCA.pca(matrix, 2, false);

        double[][] pcaMatrix = factors.toDoubleMatrix();

        Plot2DPanel plot = new Plot2DPanel();

        plot.addScatterPlot("my plot", pcaMatrix);

        // put the PlotPanel in a JFrame, as a JPanel
        JFrame frame = new JFrame("a plot panel");
        frame.setContentPane(plot);
        frame.setVisible(true);

    }

}
