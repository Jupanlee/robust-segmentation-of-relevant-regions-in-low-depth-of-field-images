package evaluation.similarity.similarity.distances;

public abstract interface DistanceMetric
{
    public abstract double getDistance(int[] paramArrayOfInt1, int[] paramArrayOfInt2);
}