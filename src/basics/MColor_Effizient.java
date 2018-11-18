package basics;

import java.util.Arrays;

public class MColor_Effizient
{
    int[] rgb = null;
    double[] hsv = null;
    double[] lab = null;

    private static double[][][][] lookupHSV = (double[][][][])null;
    private static double[][][][] lookupLAB = (double[][][][])null;

    public static void useLookupTable() {
        lookupHSV = new double[256][256][256][3];
        lookupLAB = new double[256][256][256][3];

        for (int r = 0; r < 256; r++)
            for (int g = 0; g < 256; g++)
                for (int b = 0; b < 256; b++) {
                    lookupHSV[r][g][b] = Tools.getHSV(r, g, b);
                    lookupLAB[r][g][b] = ColorConversions.getLab(r, g, b);
                }
    }

    public MColor_Effizient()
    {
    }

    public MColor_Effizient(int rgb)
    {
        setRgb(Tools.rgbToArray(rgb));
    }

    public MColor_Effizient(int[] rgb) {
        setRgb(rgb);
    }

    public MColor_Effizient(double[] hsv) {
        setHsv(hsv);
    }

    private void setHsv(double[] hsv) {
        this.hsv = Arrays.copyOf(hsv, 3);
        this.rgb = Tools.getRGB(hsv);
        this.lab = ColorConversions.getLab(this.rgb);
    }

    private void setRgb(int[] rgb) {
        this.rgb = Arrays.copyOf(rgb, 3);
    }

    private void updateHSV() {
        if (lookupHSV != null)
        {
            int r = this.rgb[0];
            int g = this.rgb[1];
            int b = this.rgb[2];

            this.hsv = lookupHSV[r][g][b];
        } else {
            this.hsv = Tools.getHSV(this.rgb);
        }
    }

    private void updateLAB() {
        if (lookupLAB != null)
        {
            int r = this.rgb[0];
            int g = this.rgb[1];
            int b = this.rgb[2];

            this.lab = lookupLAB[r][g][b]; } else {
            this.lab = ColorConversions.getLab(this.rgb);
        }
    }

    public double[] getHsv() {
        if (this.hsv == null) updateHSV();
        return this.hsv;
    }

    public double[] getLab() {
        if (this.lab == null) updateLAB();
        return this.lab;
    }

    public int[] getRgb() {
        return this.rgb;
    }

    public int getRgbInt() {
        return Tools.rgbToInt(this.rgb);
    }

    public double getLabDeltaE(MColor_Effizient other) {
        return Tools.euklidDistance(getLab(), other.getLab());
    }

    public void shift(double deltaH, double deltaS, double deltaV, boolean modulo) {
        setHsv(shiftHSV(this.hsv, deltaH, deltaS, deltaV, modulo));
    }

    public MColor add(MColor_Effizient other) {
        double[] newHsv = new double[3];
        newHsv[0] = (this.hsv[0] + other.hsv[0]);
        newHsv[1] = (this.hsv[1] + other.hsv[1]);
        newHsv[2] = (this.hsv[2] + other.hsv[2]);
        return new MColor(newHsv);
    }

    public MColor divide(double divisor) {
        double[] newHsv = new double[3];
        newHsv[0] = (this.hsv[0] / divisor);
        newHsv[1] = (this.hsv[1] / divisor);
        newHsv[2] = (this.hsv[2] / divisor);

        return new MColor(newHsv);
    }

    public String toString()
    {
        return "hsv(" + this.hsv[0] + "," + this.hsv[1] + "," + this.hsv[2] + " rgb(" + this.rgb[0] + "," + this.rgb[1] + "," + this.rgb[2] + ")";
    }

    public static double[] shiftHSV(double[] hsv, double deltaH, double deltaS, double deltaV, boolean modulo)
    {
        double[] hsvShift = Arrays.copyOf(hsv, 3);

        hsvShift[0] += deltaH;
        hsvShift[1] += deltaS;
        hsvShift[2] += deltaV;

        if (modulo)
        {
            hsvShift[0] %= 1.0D;
            hsvShift[1] %= 1.0D;
            hsvShift[2] %= 1.0D;
        }
        else {
            if (hsvShift[0] > 1.0D) hsvShift[0] = 1.0D;
            if (hsvShift[1] > 1.0D) hsvShift[1] = 1.0D;
            if (hsvShift[2] > 1.0D) hsvShift[2] = 1.0D;

            if (hsvShift[0] < 0.0D) hsvShift[0] = 0.0D;
            if (hsvShift[1] < 0.0D) hsvShift[1] = 0.0D;
            if (hsvShift[2] < 0.0D) hsvShift[2] = 0.0D;
        }

        return hsvShift;
    }
}