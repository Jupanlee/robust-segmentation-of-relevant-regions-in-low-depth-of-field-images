//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics.filter;

import java.awt.Color;

public class ColorSpaceConverter {
    public double[] D50 = new double[]{96.4212D, 100.0D, 82.5188D};
    public double[] D55 = new double[]{95.6797D, 100.0D, 92.1481D};
    public double[] D65 = new double[]{95.0429D, 100.0D, 108.89D};
    public double[] D75 = new double[]{94.9722D, 100.0D, 122.6394D};
    public double[] whitePoint;
    public double[] chromaD50;
    public double[] chromaD55;
    public double[] chromaD65;
    public double[] chromaD75;
    public double[] chromaWhitePoint;
    public double[][] M;
    public double[][] Mi;

    public ColorSpaceConverter() {
        this.whitePoint = this.D65;
        this.chromaD50 = new double[]{0.3457D, 0.3585D, 100.0D};
        this.chromaD55 = new double[]{0.3324D, 0.3474D, 100.0D};
        this.chromaD65 = new double[]{0.3127D, 0.329D, 100.0D};
        this.chromaD75 = new double[]{0.299D, 0.3149D, 100.0D};
        this.chromaWhitePoint = this.chromaD65;
        this.M = new double[][]{{0.4124D, 0.3576D, 0.1805D}, {0.2126D, 0.7152D, 0.0722D}, {0.0193D, 0.1192D, 0.9505D}};
        this.Mi = new double[][]{{3.2406D, -1.5372D, -0.4986D}, {-0.9689D, 1.8758D, 0.0415D}, {0.0557D, -0.204D, 1.057D}};
        this.whitePoint = this.D65;
        this.chromaWhitePoint = this.chromaD65;
    }

    public ColorSpaceConverter(String white) {
        this.whitePoint = this.D65;
        this.chromaD50 = new double[]{0.3457D, 0.3585D, 100.0D};
        this.chromaD55 = new double[]{0.3324D, 0.3474D, 100.0D};
        this.chromaD65 = new double[]{0.3127D, 0.329D, 100.0D};
        this.chromaD75 = new double[]{0.299D, 0.3149D, 100.0D};
        this.chromaWhitePoint = this.chromaD65;
        this.M = new double[][]{{0.4124D, 0.3576D, 0.1805D}, {0.2126D, 0.7152D, 0.0722D}, {0.0193D, 0.1192D, 0.9505D}};
        this.Mi = new double[][]{{3.2406D, -1.5372D, -0.4986D}, {-0.9689D, 1.8758D, 0.0415D}, {0.0557D, -0.204D, 1.057D}};
        this.whitePoint = this.D65;
        this.chromaWhitePoint = this.chromaD65;
        if (white.equalsIgnoreCase("d50")) {
            this.whitePoint = this.D50;
            this.chromaWhitePoint = this.chromaD50;
        } else if (white.equalsIgnoreCase("d55")) {
            this.whitePoint = this.D55;
            this.chromaWhitePoint = this.chromaD55;
        } else if (white.equalsIgnoreCase("d65")) {
            this.whitePoint = this.D65;
            this.chromaWhitePoint = this.chromaD65;
        } else if (white.equalsIgnoreCase("d75")) {
            this.whitePoint = this.D75;
            this.chromaWhitePoint = this.chromaD75;
        }

    }

    public int[] HSBtoRGB(double H, double S, double B) {
        int[] result = new int[3];
        int rgb = Color.HSBtoRGB((float)H, (float)S, (float)B);
        result[0] = rgb >> 16 & 255;
        result[1] = rgb >> 8 & 255;
        result[2] = rgb >> 0 & 255;
        return result;
    }

    public int[] HSBtoRGB(double[] HSB) {
        return this.HSBtoRGB(HSB[0], HSB[1], HSB[2]);
    }

    public int[] LABtoRGB(double L, double a, double b) {
        return this.XYZtoRGB(this.LABtoXYZ(L, a, b));
    }

    public int[] LABtoRGB(double[] Lab) {
        return this.XYZtoRGB(this.LABtoXYZ(Lab));
    }

    public double[] LABtoXYZ(double L, double a, double b) {
        double[] result = new double[3];
        double y = (L + 16.0D) / 116.0D;
        double y3 = Math.pow(y, 3.0D);
        double x = a / 500.0D + y;
        double x3 = Math.pow(x, 3.0D);
        double z = y - b / 200.0D;
        double z3 = Math.pow(z, 3.0D);
        if (y3 > 0.008856D) {
            y = y3;
        } else {
            y = (y - 0.13793103448275862D) / 7.787D;
        }

        if (x3 > 0.008856D) {
            x = x3;
        } else {
            x = (x - 0.13793103448275862D) / 7.787D;
        }

        if (z3 > 0.008856D) {
            z = z3;
        } else {
            z = (z - 0.13793103448275862D) / 7.787D;
        }

        result[0] = x * this.whitePoint[0];
        result[1] = y * this.whitePoint[1];
        result[2] = z * this.whitePoint[2];
        return result;
    }

    public double[] LABtoXYZ(double[] Lab) {
        return this.LABtoXYZ(Lab[0], Lab[1], Lab[2]);
    }

    public double[] RGBtoHSB(int R, int G, int B) {
        double[] result = new double[3];
        float[] hsb = new float[3];
        Color.RGBtoHSB(R, G, B, hsb);
        result[0] = (double)hsb[0];
        result[1] = (double)hsb[1];
        result[2] = (double)hsb[2];
        return result;
    }

    public double[] RGBtoHSB(int[] RGB) {
        return this.RGBtoHSB(RGB[0], RGB[1], RGB[2]);
    }

    public double[] RGBtoLAB(int R, int G, int B) {
        return this.XYZtoLAB(this.RGBtoXYZ(R, G, B));
    }

    public double[] RGBtoLAB(int[] RGB) {
        return this.XYZtoLAB(this.RGBtoXYZ(RGB));
    }

    public double[] RGBtoXYZ(int R, int G, int B) {
        double[] result = new double[3];
        double r = (double)R / 255.0D;
        double g = (double)G / 255.0D;
        double b = (double)B / 255.0D;
        if (r <= 0.04045D) {
            r /= 12.92D;
        } else {
            r = Math.pow((r + 0.055D) / 1.055D, 2.4D);
        }

        if (g <= 0.04045D) {
            g /= 12.92D;
        } else {
            g = Math.pow((g + 0.055D) / 1.055D, 2.4D);
        }

        if (b <= 0.04045D) {
            b /= 12.92D;
        } else {
            b = Math.pow((b + 0.055D) / 1.055D, 2.4D);
        }

        r *= 100.0D;
        g *= 100.0D;
        b *= 100.0D;
        result[0] = r * this.M[0][0] + g * this.M[0][1] + b * this.M[0][2];
        result[1] = r * this.M[1][0] + g * this.M[1][1] + b * this.M[1][2];
        result[2] = r * this.M[2][0] + g * this.M[2][1] + b * this.M[2][2];
        return result;
    }

    public double[] RGBtoXYZ(int[] RGB) {
        return this.RGBtoXYZ(RGB[0], RGB[1], RGB[2]);
    }

    public double[] xyYtoXYZ(double x, double y, double Y) {
        double[] result = new double[3];
        if (y == 0.0D) {
            result[0] = 0.0D;
            result[1] = 0.0D;
            result[2] = 0.0D;
        } else {
            result[0] = x * Y / y;
            result[1] = Y;
            result[2] = (1.0D - x - y) * Y / y;
        }

        return result;
    }

    public double[] xyYtoXYZ(double[] xyY) {
        return this.xyYtoXYZ(xyY[0], xyY[1], xyY[2]);
    }

    public double[] XYZtoLAB(double X, double Y, double Z) {
        double x = X / this.whitePoint[0];
        double y = Y / this.whitePoint[1];
        double z = Z / this.whitePoint[2];
        if (x > 0.008856D) {
            x = Math.pow(x, 0.3333333333333333D);
        } else {
            x = 7.787D * x + 0.13793103448275862D;
        }

        if (y > 0.008856D) {
            y = Math.pow(y, 0.3333333333333333D);
        } else {
            y = 7.787D * y + 0.13793103448275862D;
        }

        if (z > 0.008856D) {
            z = Math.pow(z, 0.3333333333333333D);
        } else {
            z = 7.787D * z + 0.13793103448275862D;
        }

        double[] result = new double[]{116.0D * y - 16.0D, 500.0D * (x - y), 200.0D * (y - z)};
        return result;
    }

    public double[] XYZtoLAB(double[] XYZ) {
        return this.XYZtoLAB(XYZ[0], XYZ[1], XYZ[2]);
    }

    public int[] XYZtoRGB(double X, double Y, double Z) {
        int[] result = new int[3];
        double x = X / 100.0D;
        double y = Y / 100.0D;
        double z = Z / 100.0D;
        double r = x * this.Mi[0][0] + y * this.Mi[0][1] + z * this.Mi[0][2];
        double g = x * this.Mi[1][0] + y * this.Mi[1][1] + z * this.Mi[1][2];
        double b = x * this.Mi[2][0] + y * this.Mi[2][1] + z * this.Mi[2][2];
        if (r > 0.0031308D) {
            r = 1.055D * Math.pow(r, 0.4166666666666667D) - 0.055D;
        } else {
            r *= 12.92D;
        }

        if (g > 0.0031308D) {
            g = 1.055D * Math.pow(g, 0.4166666666666667D) - 0.055D;
        } else {
            g *= 12.92D;
        }

        if (b > 0.0031308D) {
            b = 1.055D * Math.pow(b, 0.4166666666666667D) - 0.055D;
        } else {
            b *= 12.92D;
        }

        r = r < 0.0D ? 0.0D : r;
        g = g < 0.0D ? 0.0D : g;
        b = b < 0.0D ? 0.0D : b;
        result[0] = (int)Math.round(r * 255.0D);
        result[1] = (int)Math.round(g * 255.0D);
        result[2] = (int)Math.round(b * 255.0D);
        return result;
    }

    public int[] XYZtoRGB(double[] XYZ) {
        return this.XYZtoRGB(XYZ[0], XYZ[1], XYZ[2]);
    }

    public double[] XYZtoxyY(double X, double Y, double Z) {
        double[] result = new double[3];
        if (X + Y + Z == 0.0D) {
            result[0] = this.chromaWhitePoint[0];
            result[1] = this.chromaWhitePoint[1];
            result[2] = this.chromaWhitePoint[2];
        } else {
            result[0] = X / (X + Y + Z);
            result[1] = Y / (X + Y + Z);
            result[2] = Y;
        }

        return result;
    }

    public double[] XYZtoxyY(double[] XYZ) {
        return this.XYZtoxyY(XYZ[0], XYZ[1], XYZ[2]);
    }
}
