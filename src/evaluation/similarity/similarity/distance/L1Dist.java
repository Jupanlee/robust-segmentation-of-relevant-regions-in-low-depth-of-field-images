package evaluation.similarity.similarity.distances;

public class L1Dist
        implements DistanceMetric
{
    public double getDistance(int[] histogramA, int[] histogramB)
    {
        double sum = 0.0D;
        for (int i = 0; i < histogramB.length; i++) {
            sum += Math.abs(histogramA[i] - histogramB[i]);
        }
        return sum;
    }
}