package others.fuzzy.math;

public class Gaussian
        implements Function
{
    private final float sigma;

    public Gaussian(float sigma)
    {
        this.sigma = sigma;
    }

    public float compute(float x)
    {
        double exp = -(Math.pow(x, 2.0D) / this.sigma);
        double t1 = Math.exp(exp);
        return (float)t1;
    }
}