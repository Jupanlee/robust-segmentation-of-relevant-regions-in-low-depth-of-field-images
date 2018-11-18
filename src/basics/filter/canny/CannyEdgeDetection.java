package basics.filter.canny;

import ij.IJ;
import ij.plugin.filter.RankFilters;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.util.ArrayList;

public class CannyEdgeDetection
{
    static final int BYTE = 0;
    static final int SHORT = 1;
    static final int FLOAT = 2;
    static final int OTHER = 3;
    private static boolean typed = false;
    private static int type;

    public static ImageProcessor run(ImageProcessor imageProcessor)
    {
        double radius = 3.0D;
        float alpha = 0.5F;
        float upper = 100.0F;
        float lower = 50.0F;
        return run(imageProcessor, radius, alpha, upper, lower);
    }

    public static ImageProcessor run(ImageProcessor ip, double radius, float alpha, float upper, float lower)
    {
        int type = getType(ip);
        if (type == 3) {
            IJ.error("area detection", "No action taken. Greyscale or pseudocolored images required!");
            return ip;
        }
        ImageProcessor ipcopy = getDeriche(ip, alpha, radius);
        ipcopy = trin(ipcopy, upper, lower);
        ipcopy = hyst(ipcopy);
        return ipcopy;
    }

    public static ImageProcessor getDeriche(ImageProcessor ip, float alpha)
    {
        return getDeriche(ip, alpha, 0.0D);
    }

    public static ImageProcessor getDeriche(ImageProcessor ip, float alpha, double radius)
    {
        int type = getType(ip);
        if (type == 3) {
            IJ.error("Deriche", "No action taken. Greyscale or pseudocolored image required!");
            return ip;
        }
        ArrayList arrays = null;
        ImageProcessor ip2 = ip.duplicate();
        RankFilters filter = new RankFilters();
        if (radius > 0.0D) {
            filter.rank(ip2, radius, 4);
        }

        arrays = dericheCalc(ip2, alpha);
        double[] norm = (double[])arrays.get(0);
        double[] angle = (double[])arrays.get(1);
        FloatProcessor normfp = new FloatProcessor(ip2.getWidth(), ip2.getHeight(), norm);
        normfp.resetMinAndMax();
        FloatProcessor anglefp = new FloatProcessor(ip2.getWidth(), ip2.getHeight(), angle);
        anglefp.resetMinAndMax();
        ip2 = nonMaximalSuppression(normfp, anglefp);
        return ip2;
    }

    public static FloatProcessor getDericheAngle(ImageProcessor ip, float alpha)
    {
        return getDericheAngle(ip, alpha, 0.0D);
    }

    public static FloatProcessor getDericheAngle(ImageProcessor ip, float alpha, double radius)
    {
        int type = getType(ip);
        if (type == 3) {
            IJ.error("Deriche", "No action taken. Greyscale or pseudocolored image required");
            return (FloatProcessor)ip;
        }
        ImageProcessor ip2 = ip.duplicate();
        RankFilters filter = new RankFilters();
        if (radius > 0.0D) {
            filter.rank(ip2, radius, 4);
        }

        double[] angle = (double[])dericheCalc(ip2, alpha).get(1);
        FloatProcessor anglefp = new FloatProcessor(ip2.getWidth(), ip2.getHeight(), angle);
        anglefp.resetMinAndMax();
        return anglefp;
    }

    public static FloatProcessor getDericheNorm(ImageProcessor ip, float alpha)
    {
        return getDericheNorm(ip, alpha, 0.0D);
    }

    public static FloatProcessor getDericheNorm(ImageProcessor ip, float alpha, double radius)
    {
        int type = getType(ip);
        if (type == 3) {
            IJ.error("Deriche", "greyscale or pseudocolored images required");
            return (FloatProcessor)ip;
        }
        ImageProcessor ip2 = ip.duplicate();
        RankFilters filter = new RankFilters();
        if (radius > 0.0D) {
            filter.rank(ip2, radius, 4);
        }

        double[] norm = (double[])dericheCalc(ip2, alpha).get(0);
        FloatProcessor normfp = new FloatProcessor(ip2.getWidth(), ip2.getHeight(), norm);
        normfp.resetMinAndMax();
        return normfp;
    }

    public static ImageProcessor trin(ImageProcessor ima, float T1, float T2)
    {
        int la = ima.getWidth();
        int ha = ima.getHeight();
        ByteProcessor res = new ByteProcessor(la, ha);

        for (int x = 0; x < la; x++) {
            for (int y = 0; y < ha; y++) {
                float pix = ima.getPixelValue(x, y);
                if (pix >= T1)
                    res.putPixel(x, y, 255);
                else if (pix >= T2) {
                    res.putPixel(x, y, 128);
                }
            }
        }
        return res;
    }

    public static ImageProcessor hyst(ImageProcessor ima)
    {
        int la = ima.getWidth();
        int ha = ima.getHeight();
        ImageProcessor res = ima.duplicate();
        boolean change = true;

        while (change) {
            change = false;
            for (int x = 1; x < la - 1; x++) {
                for (int y = 1; y < ha - 1; y++) {
                    if (res.getPixelValue(x, y) == 255.0F) {
                        if (res.getPixelValue(x + 1, y) == 128.0F) {
                            change = true;
                            res.putPixelValue(x + 1, y, 255.0D);
                        }
                        if (res.getPixelValue(x - 1, y) == 128.0F) {
                            change = true;
                            res.putPixelValue(x - 1, y, 255.0D);
                        }
                        if (res.getPixelValue(x, y + 1) == 128.0F) {
                            change = true;
                            res.putPixelValue(x, y + 1, 255.0D);
                        }
                        if (res.getPixelValue(x, y - 1) == 128.0F) {
                            change = true;
                            res.putPixelValue(x, y - 1, 255.0D);
                        }
                        if (res.getPixelValue(x + 1, y + 1) == 128.0F) {
                            change = true;
                            res.putPixelValue(x + 1, y + 1, 255.0D);
                        }
                        if (res.getPixelValue(x - 1, y - 1) == 128.0F) {
                            change = true;
                            res.putPixelValue(x - 1, y - 1, 255.0D);
                        }
                        if (res.getPixelValue(x - 1, y + 1) == 128.0F) {
                            change = true;
                            res.putPixelValue(x - 1, y + 1, 255.0D);
                        }
                        if (res.getPixelValue(x + 1, y - 1) == 128.0F) {
                            change = true;
                            res.putPixelValue(x + 1, y - 1, 255.0D);
                        }
                    }
                }
            }
            if (change) {
                for (int x = la - 2; x > 0; x--) {
                    for (int y = ha - 2; y > 0; y--) {
                        if (res.getPixelValue(x, y) == 255.0F) {
                            if (res.getPixelValue(x + 1, y) == 128.0F) {
                                change = true;
                                res.putPixelValue(x + 1, y, 255.0D);
                            }
                            if (res.getPixelValue(x - 1, y) == 128.0F) {
                                change = true;
                                res.putPixelValue(x - 1, y, 255.0D);
                            }
                            if (res.getPixelValue(x, y + 1) == 128.0F) {
                                change = true;
                                res.putPixelValue(x, y + 1, 255.0D);
                            }
                            if (res.getPixelValue(x, y - 1) == 128.0F) {
                                change = true;
                                res.putPixelValue(x, y - 1, 255.0D);
                            }
                            if (res.getPixelValue(x + 1, y + 1) == 128.0F) {
                                change = true;
                                res.putPixelValue(x + 1, y + 1, 255.0D);
                            }
                            if (res.getPixelValue(x - 1, y - 1) == 128.0F) {
                                change = true;
                                res.putPixelValue(x - 1, y - 1, 255.0D);
                            }
                            if (res.getPixelValue(x - 1, y + 1) == 128.0F) {
                                change = true;
                                res.putPixelValue(x - 1, y + 1, 255.0D);
                            }
                            if (res.getPixelValue(x + 1, y - 1) == 128.0F) {
                                change = true;
                                res.putPixelValue(x + 1, y - 1, 255.0D);
                            }
                        }
                    }
                }
            }
        }

        for (int x = 0; x < la; x++) {
            for (int y = 0; y < ha; y++) {
                if (res.getPixelValue(x, y) == 128.0F) {
                    res.putPixelValue(x, y, 0.0D);
                }
            }
        }
        return res;
    }

    public static ArrayList<double[]> dericheCalc(ImageProcessor ip, float alphaD)
    {
        double[] norm_deriche = null;
        double[] angle_deriche = null;

        float[] nf_grx = null;
        float[] nf_gry = null;
        int[] a1 = null;
        float[] a2 = null;
        float[] a3 = null;
        float[] a4 = null;

        int icolonnes = 0;

        int lignes = ip.getHeight();
        int colonnes = ip.getWidth();
        int nmem = lignes * colonnes;

        int lig_1 = lignes - 1;
        int lig_2 = lignes - 2;
        int lig_3 = lignes - 3;
        int col_1 = colonnes - 1;
        int col_2 = colonnes - 2;
        int col_3 = colonnes - 3;

        norm_deriche = new double[nmem];
        angle_deriche = new double[nmem];
        ArrayList result = new ArrayList();

        nf_grx = new float[nmem];
        nf_gry = new float[nmem];

        a1 = new int[nmem];
        a2 = new float[nmem];
        a3 = new float[nmem];
        a4 = new float[nmem];

        float ad1 = (float)(-Math.exp(-alphaD));
        float ad2 = 0.0F;
        float an1 = 1.0F;
        float an2 = 0.0F;
        float an3 = (float)Math.exp(-alphaD);
        float an4 = 0.0F;
        float an11 = 1.0F;

        for (int i = 0; i < lignes; i++) {
            for (int j = 0; j < colonnes; j++) {
                a1[(i * colonnes + j)] = ip.getPixel(j, i);
            }

        }

        for (int i = 0; i < lignes; i++) {
            icolonnes = i * colonnes;
            int icol_1 = icolonnes - 1;
            int icol_2 = icolonnes - 2;
            a2[icolonnes] = (an1 * a1[icolonnes]);
            a2[(icolonnes + 1)] = (an1 * a1[(icolonnes + 1)] + an2 * a1[icolonnes] - ad1 * a2[icolonnes]);

            for (int j = 2; j < colonnes; j++) {
                a2[(icolonnes + j)] = (an1 * a1[(icolonnes + j)] + an2 * a1[(icol_1 + j)] - ad1 * a2[(icol_1 + j)] - ad2 * a2[(icol_2 + j)]);
            }

        }

        for (int i = 0; i < lignes; i++) {
            icolonnes = i * colonnes;
            int icol_1 = icolonnes + 1;
            int icol_2 = icolonnes + 2;
            a3[(icolonnes + col_1)] = 0.0F;
            a3[(icolonnes + col_2)] = (an3 * a1[(icolonnes + col_1)]);
            for (int j = col_3; j >= 0; j--) {
                a3[(icolonnes + j)] = (an3 * a1[(icol_1 + j)] + an4 * a1[(icol_2 + j)] - ad1 * a3[(icol_1 + j)] - ad2 * a3[(icol_2 + j)]);
            }

        }

        int icol_1 = lignes * colonnes;

        for (int i = 0; i < icol_1; i++) {
            a2[i] += a3[i];
        }

        for (int j = 0; j < colonnes; j++) {
            a3[j] = 0.0F;
            a3[(colonnes + j)] = (an11 * a2[j] - ad1 * a3[j]);
            for (int i = 2; i < lignes; i++) {
                a3[(i * colonnes + j)] = (an11 * a2[((i - 1) * colonnes + j)] - ad1 * a3[((i - 1) * colonnes + j)] - ad2 * a3[((i - 2) * colonnes + j)]);
            }

        }

        for (int j = 0; j < colonnes; j++) {
            a4[(lig_1 * colonnes + j)] = 0.0F;
            a4[(lig_2 * colonnes + j)] = (-an11 * a2[(lig_1 * colonnes + j)] - ad1 * a4[(lig_1 * colonnes + j)]);

            for (int i = lig_3; i >= 0; i--) {
                a4[(i * colonnes + j)] = (-an11 * a2[((i + 1) * colonnes + j)] - ad1 * a4[((i + 1) * colonnes + j)] - ad2 * a4[((i + 2) * colonnes + j)]);
            }

        }

        icol_1 = colonnes * lignes;
        for (int i = 0; i < icol_1; i++) {
            a3[i] += a4[i];
        }

        for (int i = 0; i < lignes; i++) {
            for (int j = 0; j < colonnes; j++) {
                nf_gry[(i * colonnes + j)] = a3[(i * colonnes + j)];
            }

        }

        for (int i = 0; i < lignes; i++) {
            for (int j = 0; j < colonnes; j++) {
                a1[(i * colonnes + j)] = ip.getPixel(j, i);
            }
        }

        for (int i = 0; i < lignes; i++) {
            icolonnes = i * colonnes;
            icol_1 = icolonnes - 1;
            int icol_2 = icolonnes - 2;
            a2[icolonnes] = 0.0F;
            a2[(icolonnes + 1)] = (an11 * a1[icolonnes]);
            for (int j = 2; j < colonnes; j++) {
                a2[(icolonnes + j)] = (an11 * a1[(icol_1 + j)] - ad1 * a2[(icol_1 + j)] - ad2 * a2[(icol_2 + j)]);
            }

        }

        for (int i = 0; i < lignes; i++) {
            icolonnes = i * colonnes;
            icol_1 = icolonnes + 1;
            int icol_2 = icolonnes + 2;
            a3[(icolonnes + col_1)] = 0.0F;
            a3[(icolonnes + col_2)] = (-an11 * a1[(icolonnes + col_1)]);
            for (int j = col_3; j >= 0; j--) {
                a3[(icolonnes + j)] = (-an11 * a1[(icol_1 + j)] - ad1 * a3[(icol_1 + j)] - ad2 * a3[(icol_2 + j)]);
            }
        }

        icol_1 = lignes * colonnes;
        for (int i = 0; i < icol_1; i++) {
            a2[i] += a3[i];
        }

        for (int j = 0; j < colonnes; j++) {
            a3[j] = (an1 * a2[j]);
            a3[(colonnes + j)] = (an1 * a2[(colonnes + j)] + an2 * a2[j] - ad1 * a3[j]);

            for (int i = 2; i < lignes; i++) {
                a3[(i * colonnes + j)] = (an1 * a2[(i * colonnes + j)] + an2 * a2[((i - 1) * colonnes + j)] - ad1 * a3[((i - 1) * colonnes + j)] - ad2 * a3[((i - 2) * colonnes + j)]);
            }

        }

        for (int j = 0; j < colonnes; j++) {
            a4[(lig_1 * colonnes + j)] = 0.0F;
            a4[(lig_2 * colonnes + j)] = (an3 * a2[(lig_1 * colonnes + j)] - ad1 * a4[(lig_1 * colonnes + j)]);
            for (int i = lig_3; i >= 0; i--) {
                a4[(i * colonnes + j)] = (an3 * a2[((i + 1) * colonnes + j)] + an4 * a2[((i + 2) * colonnes + j)] - ad1 * a4[((i + 1) * colonnes + j)] - ad2 * a4[((i + 2) * colonnes + j)]);
            }

        }

        icol_1 = colonnes * lignes;
        for (int i = 0; i < icol_1; i++) {
            a3[i] += a4[i];
        }

        for (int i = 0; i < lignes; i++) {
            for (int j = 0; j < colonnes; j++) {
                nf_grx[(i * colonnes + j)] = a3[(i * colonnes + j)];
            }

        }

        for (int i = 0; i < lignes; i++) {
            for (int j = 0; j < colonnes; j++) {
                a2[(i * colonnes + j)] = nf_gry[(i * colonnes + j)];
            }
        }
        icol_1 = colonnes * lignes;
        for (int i = 0; i < icol_1; i++) {
            norm_deriche[i] = modul(nf_grx[i], nf_gry[i]);
            angle_deriche[i] = angle(nf_grx[i], nf_gry[i]);
        }
        result.add(norm_deriche);
        result.add(angle_deriche);
        return result;
    }

    public static ImageProcessor nonMaximalSuppression(ImageProcessor grad, ImageProcessor ang)
    {
        FloatProcessor res = new FloatProcessor(grad.getWidth(), grad.getHeight());

        int la = grad.getWidth();
        int ha = grad.getHeight();

        float pix1 = 0.0F;
        float pix2 = 0.0F;

        for (int x = 1; x < la - 1; x++) {
            for (int y = 1; y < ha - 1; y++) {
                float ag = ang.getPixelValue(x, y);
                if ((ag > -22.5D) && (ag <= 22.5D)) {
                    pix1 = grad.getPixelValue(x + 1, y);
                    pix2 = grad.getPixelValue(x - 1, y);
                } else if ((ag > 22.5D) && (ag <= 67.5D)) {
                    pix1 = grad.getPixelValue(x + 1, y - 1);
                    pix2 = grad.getPixelValue(x - 1, y + 1);
                } else if (((ag > 67.5D) && (ag <= 90.0F)) || ((ag < -67.5D) && (ag >= -90.0F))) {
                    pix1 = grad.getPixelValue(x, y - 1);
                    pix2 = grad.getPixelValue(x, y + 1);
                } else if ((ag < -22.5D) && (ag >= -67.5D)) {
                    pix1 = grad.getPixelValue(x + 1, y + 1);
                    pix2 = grad.getPixelValue(x - 1, y - 1);
                }
                float pix = grad.getPixelValue(x, y);
                if ((pix >= pix1) && (pix >= pix2)) {
                    res.putPixelValue(x, y, pix);
                }
            }
        }
        return res;
    }

    public static double modul(float dx, float dy)
    {
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double angle(float dx, float dy)
    {
        return -Math.toDegrees(Math.atan(dy / dx));
    }

    static int getType(ImageProcessor ip)
    {
        if (!typed) {
            typed = true;
            if ((ip instanceof ByteProcessor & !ip.isColorLut()))
                type = 0;
            else if ((ip instanceof ShortProcessor))
                type = 1;
            else if ((ip instanceof FloatProcessor))
                type = 2;
            else {
                type = 3;
            }
        }
        return type;
    }
}