package basics;

import java.awt.Point;

public class NormalDistribution
{
    public static double get(double mue, double sigma, double x)
    {
        double pi = 3.141592653589793D;
        double sigmaWurzel2Pi = sigma * Math.sqrt(2.0D * pi);
        double xMinusMueDurchSigmaQuadrat = Math.pow((x - mue) / sigma, 2.0D);
        return 1.0D / sigmaWurzel2Pi * Math.exp(-0.5D * xMinusMueDurchSigmaQuadrat);
    }

    public static double[][] gaussGlocke(double[][] field, double mue, double sigma, double threshold) {
        int radius = 0;
        while (get(mue, sigma, radius) > threshold) radius++;

        int kante = radius * 2 + 1;
        double[][] weights = new double[kante][kante];
        Point center = new Point(radius, radius);

        for (int x = 0; x < kante; x++) {
            for (int y = 0; y < kante; y++) {
                double distToCenter = Tools.getL2Dist(new Point(x, y), center);
                weights[x][y] = get(mue, sigma, distToCenter);
            }
        }

        double[][] result = new double[field.length][field[0].length];

        for (int x = 0; x < field.length; x++) {
            for (int y = 0; y < field[0].length; y++)
            {
                double[][] neighbourValues = Tools.getNeighbourhood(new Point(x, y), radius, field);
                double[][] weightedNeighbourValues = Tools.multiplyDoubleFields(neighbourValues, weights);
                result[x][y] = Tools.getSum(weightedNeighbourValues, true);
            }
        }

        return result;
    }

    public static double[][] gaussGlocke(int[][] field, double mue, double sigma, double threshold) {
        int radius = 0;
        while (get(mue, sigma, radius) > threshold) radius++;

        int kante = radius * 2 + 1;
        double[][] weights = new double[kante][kante];
        Point center = new Point(radius, radius);

        for (int x = 0; x < kante; x++) {
            for (int y = 0; y < kante; y++) {
                double distToCenter = Tools.getL2Dist(new Point(x, y), center);
                weights[x][y] = get(mue, sigma, distToCenter);
            }
        }

        double[][] result = new double[field.length][field[0].length];

        for (int x = 0; x < field.length; x++) {
            for (int y = 0; y < field[0].length; y++)
            {
                double[][] neighbourValues = Tools.getNeighbourhood(new Point(x, y), radius, field);
                double[][] weightedNeighbourValues = Tools.multiplyDoubleFields(neighbourValues, weights);
                result[x][y] = Tools.getSum(weightedNeighbourValues, true);
            }
        }

        return result;
    }
}