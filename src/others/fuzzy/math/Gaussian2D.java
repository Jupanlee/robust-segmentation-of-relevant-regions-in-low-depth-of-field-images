package others.fuzzy.math;

public class Gaussian2D
        implements Function2D
{
    private static final double A = 0.1591549430918954D;
    private final double sigma;

    public Gaussian2D(double sigma)
    {
        this.sigma = sigma;
    }

    public float compute(float dx, float dy)
    {
        double exp = -(dx * dx + dy * dy) / (2.0D * this.sigma * this.sigma);
        double t1 = 0.1591549430918954D * Math.exp(exp);
        return (float)t1;
    }

    public float computeDerivX(float dx, float dy)
    {
        double exp = -(dx * dx + dy * dy) / (2.0D * this.sigma * this.sigma);
        double derivedExp = -dx / (this.sigma * this.sigma);
        double t1 = 0.1591549430918954D * Math.exp(exp) * derivedExp;

        return (float)t1;
    }

    public float computeDerivY(float dx, float dy)
    {
        double exp = -(dx * dx + dy * dy) / (2.0D * this.sigma * this.sigma);
        double derivedExp = -dy / (this.sigma * this.sigma);
        double t1 = 0.1591549430918954D * Math.exp(exp) * derivedExp;

        return (float)t1;
    }
}