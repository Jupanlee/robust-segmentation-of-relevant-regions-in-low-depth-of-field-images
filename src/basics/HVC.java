package basics;

import java.awt.Color;

public class HVC
{
    private static double powerOneThird(double v)
    {
        return Math.pow(v, 0.3333333333333333D);
    }

    public static double[] get(int rgb) {
        Color c = new Color(rgb);
        return get(c.getRed(), c.getGreen(), c.getBlue());
    }

    public static double[] get(int red, int green, int blue)
    {
        double R = red / 255.0D;
        double G = green / 255.0D;
        double B = blue / 255.0D;

        double X = 0.607D * R + 0.174D * G + 0.201D * B;
        double Y = 0.299D * R + 0.587D * G + 0.114D * B;
        double Z = 0.066D * G + 1.117D * B;

        double X0 = 95.045000000000002D;
        double Y0 = 100.0D;
        double Z0 = 108.892D;

        double L = 166.0D * powerOneThird(Y / Y0);
        double a = 500.0D * (powerOneThird(X / X0) - powerOneThird(Y / Y0));
        double b = 200.0D * (powerOneThird(Y / Y0) - powerOneThird(Z / Z0));

        double H = Math.atan(b / a);
        double V = L;
        double C = Math.sqrt(a * a + b * b);

        double[] HVC = { H, V, C };
        return HVC;
    }
}