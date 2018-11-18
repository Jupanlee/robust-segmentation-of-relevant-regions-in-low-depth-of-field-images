package others.fuzzy.math;

public class KernelEpanechnikov
        implements Function
{
    public float compute(float x)
    {
        if (x < 1.0F)
            return 1.0F - x;
        return 0.0F;
    }
}