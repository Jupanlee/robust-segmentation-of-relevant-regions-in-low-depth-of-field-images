package basics;

import basics.filter.ColorSpaceConverter;
import basics.javaAddons.DEBUG;
import java.awt.Color;
import java.util.List;

public class ColorConversions
{
    private static double[][][][] lookupTable = (double[][][][])null;

    public static void useLookupTable() {
        DEBUG.initProgress("Build Lookup Table", 256);
        lookupTable = new double[256][256][256][3];
        for (int r = 0; r < 256; r++) {
            DEBUG.printProgressBar("Build Lookup Table");
            for (int g = 0; g < 256; g++)
                for (int b = 0; b < 256; b++)
                    lookupTable[r][g][b] = calcLab(r, g, b);
        }
    }

    public static double[] mean(double[][] labs)
    {
        double[] mean = new double[3];

        mean[0] = Tools.getMean(labs[0]);
        mean[1] = Tools.getMean(labs[1]);
        mean[2] = Tools.getMean(labs[2]);

        return mean;
    }

    public static double[][] getLab(List<Integer> rgbs)
    {
        double[][] labs = new double[3][rgbs.size()];
        for (int i = 0; i < rgbs.size(); i++) {
            double[] lab = getLab(((Integer)rgbs.get(i)).intValue());
            labs[0][i] = lab[0];
            labs[1][i] = lab[1];
            labs[2][i] = lab[2];
        }
        return labs;
    }

    public static double[] getLab(int[] rgb) {
        return getLab(rgb[0], rgb[1], rgb[2]);
    }

    public static double[] getLab(int rgb) {
        Color c = new Color(rgb);
        return getLab(c.getRed(), c.getGreen(), c.getBlue());
    }

    public static double[] getLab(int R, int G, int B) {
        if (lookupTable != null) return lookupTable[R][G][B];
        return calcLab(R, G, B);
    }

    public static double[] calcLab(int R, int G, int B)
    {
        return new ColorSpaceConverter().RGBtoLAB(new int[] { R, G, B });
    }
}