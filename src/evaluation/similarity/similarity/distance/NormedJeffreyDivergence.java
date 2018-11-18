package evaluation.similarity.similarity.distances;

public class NormedJeffreyDivergence
        implements DistanceMetric
{
    private static int startIndex = 1;

    public double getDistance(int[] histogramA, int[] histogramB)
    {
        double distance = 0.0D;
        int pixelcount = 0;

        for (int i = 0; i < histogramA.length; i++)
        {
            int hi = histogramA[i];
            int ki = histogramB[i];

            double mi = (hi + ki) / 2.0D;

            if ((hi > 0) && (ki > 0)) {
                pixelcount++;
                distance += hi * Math.log(hi / mi) + ki * Math.log(ki / mi);
            }
        }

        return distance / pixelcount;
    }
}