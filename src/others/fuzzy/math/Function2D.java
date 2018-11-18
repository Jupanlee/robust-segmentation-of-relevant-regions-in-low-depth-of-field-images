package others.fuzzy.math;

public abstract interface Function2D
{
    public abstract float compute(float paramFloat1, float paramFloat2);

    public abstract float computeDerivX(float paramFloat1, float paramFloat2);

    public abstract float computeDerivY(float paramFloat1, float paramFloat2);
}