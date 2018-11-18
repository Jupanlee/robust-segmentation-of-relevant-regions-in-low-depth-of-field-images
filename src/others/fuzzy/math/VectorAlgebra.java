package others.fuzzy.math;

import java.util.List;

public class VectorAlgebra
{
    public static float scalarProduct(float[] a, float[] b)
    {
        float c = 0.0F;
        for (int i = 0; i < a.length; i++) {
            c += a[i] * b[i];
        }
        return c;
    }

    public static float distance(float[] a, float[] b) {
        float[] dest = subtract(a, b);
        return (float)Math.sqrt(scalarProduct(dest, dest));
    }

    public static float[] times(float[] a, float alpha)
    {
        float[] c = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            a[i] *= alpha;
        }
        return c;
    }

    public static float[] add(float[] a, float[] b)
    {
        float[] c = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            a[i] += b[i];
        }
        return c;
    }

    public static float[] add(float[] a, float b)
    {
        float[] c = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            a[i] += b;
        }
        return c;
    }

    public static float[] subtract(float[] a, float[] b)
    {
        float[] c = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            a[i] -= b[i];
        }
        return c;
    }

    public static float[] matrixMult(float[] vector, float[][] matrix)
    {
        float[] result = new float[vector.length];

        for (int y = 0; y < matrix.length; y++) {
            float row = 0.0F;
            for (int x = 0; x < matrix[0].length; x++) {
                row += matrix[y][x] * vector[x];
            }
            result[y] = row;
        }

        return result;
    }

    public static float[] normalize(float[] a)
    {
        float norm = 1.0F / (float)Math.sqrt(scalarProduct(a, a));
        return times(a, norm);
    }

    public static float[] average(List<float[]> dataPoints) {
        assert (dataPoints.size() > 0);
        float[] c = new float[((float[])dataPoints.get(0)).length];
        for (float[] f : dataPoints) {
            c = add(c, f);
        }
        c = times(c, 1.0F / dataPoints.size());

        return c;
    }
}