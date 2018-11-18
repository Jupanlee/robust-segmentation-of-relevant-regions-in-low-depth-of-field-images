package evaluation.similarity.similarity.distances;

public class MinkowskiFormDistance
        implements DistanceMetric
{
    private double power = 2.0D;

    public MinkowskiFormDistance(double power) {
        this.power = power;
    }

    public double getDistance(int[] histogramA, int[] histogramB)
    {
        double distance = 0.0D;

        for (int i = 0; i < histogramA.length; i++) {
            distance += Math.pow(Math.abs(histogramA[i] - histogramB[i]), this.power);
        }

        distance = Math.pow(distance, 1.0D / this.power);

        return distance;
    }

    public String toString()
    {
        return "MinkowskiForm";
    }
}