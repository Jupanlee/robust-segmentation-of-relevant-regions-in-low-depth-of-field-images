package basics;

import java.util.Arrays;

public class MColor
{
    private int[] rgb = { 0, 0, 0 };
    private double[] hsv = { 0.0D, 0.0D, 0.0D };
    private double[] lab;

    public MColor()
    {
    }

    public MColor(MColor mColor)
    {
        this(mColor.getRgb());
    }

    public MColor(int rgb) {
        setRgb(Tools.rgbToArray(rgb));
    }

    public MColor(int[] rgb) {
        setRgb(rgb);
    }

    public MColor(double[] hsv) {
        setHsv(hsv);
    }

    public MColor add(MColor other) {
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

    private void setHsv(double[] hsv) {
        this.hsv = Arrays.copyOf(hsv, 3);
        this.rgb = Tools.getRGB(hsv);
        this.lab = ColorConversions.getLab(this.rgb);
    }

    private void setRgb(int[] rgb) {
        this.rgb = Arrays.copyOf(rgb, 3);
        this.hsv = Tools.getHSV(rgb);
        this.lab = ColorConversions.getLab(rgb);
    }

    public double[] getHsv() {
        return this.hsv;
    }

    public int[] getRgb() {
        return this.rgb;
    }

    public int getRgbInt() {
        return Tools.rgbToInt(this.rgb);
    }

    public double[] getLab() {
        return this.lab;
    }

    public double getLabDeltaE(MColor other) {
        return Tools.euklidDistance(getLab(), other.getLab());
    }

    public void shift(double deltaH, double deltaS, double deltaV, boolean modulo) {
        setHsv(shiftHSV(this.hsv, deltaH, deltaS, deltaV, modulo));
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