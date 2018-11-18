//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics.filter;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;
import java.awt.AWTEvent;
import java.awt.Checkbox;
import java.awt.Rectangle;
import java.awt.TextField;
import java.util.Vector;

public class Fast_Filters implements ExtendedPlugInFilter, DialogListener {
    private static final String[] TYPES = new String[]{"mean", "border-limited mean", "median", "minimum", "maximum", "eliminate maxima", "eliminate minima", "background from minima", "background from maxima", "background from median"};
    private static final int MEAN = 0;
    private static final int BORDER_LIMITED_MEAN = 1;
    private static final int MEDIAN = 2;
    private static final int MIN = 3;
    private static final int MAX = 4;
    private static final int[][] taskLists = new int[][]{{0}, {1}, {2}, {3}, {4}, {3, 4}, {4, 3}, {3, 4, 1}, {4, 3, 1}, {2, 1}};
    private static final String[] PREPROCESSES = new String[]{"none", "smooth", "median"};
    private static int type = 0;
    private static int xRadius = 5;
    private static int yRadius = 5;
    private static boolean linkXY = true;
    private static int preProcess = 0;
    private static boolean subtract = false;
    private static double[] offset = new double[]{128.0D, 32768.0D, 0.0D, 128.0D, 128.0D};
    private int flags = 16834655;
    private int impType;
    private int nPasses = 1;
    private int pass;

    public Fast_Filters() {
    }

    public int setup(String arg, ImagePlus imp) {
        return IJ.versionLessThan("1.38x") ? 4096 : this.flags;
    }

    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
        this.impType = imp.getType();
        int digits = this.impType == 2 ? 2 : 0;
        GenericDialog gd = new GenericDialog(command + "...");
        gd.addChoice("Filter Type", TYPES, TYPES[type]);
        gd.addNumericField("x Radius", (double)xRadius, 0);
        gd.addNumericField("y Radius", (double)yRadius, 0);
        gd.addCheckbox("Link x & y", linkXY);
        gd.addChoice("Preprocessing", PREPROCESSES, PREPROCESSES[preProcess]);
        gd.addCheckbox("Subtract Filtered", subtract);
        gd.addNumericField("Offset (subtract only)", offset[this.impType], digits);
        gd.addPreviewCheckbox(pfr);
        gd.addDialogListener(this);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return 4096;
        } else {
            IJ.register(this.getClass());
            return IJ.setupDialog(imp, this.flags);
        }
    }

    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
        Vector numFields = gd.getNumericFields();
        TextField xNumField = (TextField)numFields.get(0);
        TextField yNumField = (TextField)numFields.get(1);
        Checkbox linkCheckbox = (Checkbox)gd.getCheckboxes().get(0);
        linkXY = gd.getNextBoolean();
        if (linkXY && !xNumField.getText().equals(yNumField.getText())) {
            if (e.getSource() != xNumField && e.getSource() != linkCheckbox) {
                if (e.getSource() == yNumField) {
                    xNumField.setText(yNumField.getText());
                }
            } else {
                yNumField.setText(xNumField.getText());
            }
        }

        type = gd.getNextChoiceIndex();
        xRadius = (int)gd.getNextNumber();
        yRadius = (int)gd.getNextNumber();
        preProcess = gd.getNextChoiceIndex();
        subtract = gd.getNextBoolean();
        offset[this.impType] = gd.getNextNumber();
        return !gd.invalidNumber() && xRadius >= 0 && yRadius >= 0 && xRadius < 1000000 && yRadius < 1000000;
    }

    public void run(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        Rectangle roiRect = ip.getRoi();
        int[] taskList = taskLists[type];
        int nTasks = taskList.length;
        int extraX = xRadius * (nTasks - 1);
        int extraY = yRadius * nTasks;
        if (preProcess > 0) {
            extraX += xRadius;
            if (xRadius > 0 && yRadius > 0) {
                this.filterFloat(ip, preProcess, 1, true, extraX, extraY + 1);
                this.filterFloat(ip, preProcess, 1, false, extraX, extraY);
                ++extraY;
            } else if (xRadius > 0) {
                this.filterFloat(ip, preProcess, 2, true, extraX, extraY);
            } else if (yRadius > 0) {
                this.filterFloat(ip, preProcess, 2, false, extraX, extraY);
            }
        }

        for(int iTask = 0; iTask < nTasks; ++iTask) {
            if (xRadius > 0) {
                this.filterFloat(ip, taskList[iTask], xRadius, true, xRadius * (nTasks - iTask - 1), yRadius * (nTasks - iTask));
            }

            if (yRadius > 0) {
                this.filterFloat(ip, taskList[iTask], yRadius, false, xRadius * (nTasks - iTask - 1), yRadius * (nTasks - iTask - 1));
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }
        }

        if (subtract) {
            float[] pixels = (float[])((float[])ip.getPixels());
            float[] snapPixels = (float[])((float[])ip.getSnapshotPixels());
            float fOffset = (float)offset[this.impType];

            for(int y = roiRect.y; y < roiRect.y + roiRect.height; ++y) {
                int x = roiRect.x;

                for(int p = x + y * width; x < roiRect.x + roiRect.width; ++p) {
                    pixels[p] = snapPixels[p] - pixels[p] + fOffset;
                    ++x;
                }
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }
        }

        if (roiRect.height != height || roiRect.width != width) {
            resetOutOfRoi(ip, extraX, extraY);
        }

        this.showProgress(1.0D);
    }

    private void filterFloat(ImageProcessor ip, int type, int radius, boolean xDirection, int extraX, int extraY) {
        float sign = type == 3 ? -1.0F : 1.0F;
        int width = ip.getWidth();
        int height = ip.getHeight();
        Rectangle roiRect = (Rectangle)ip.getRoi().clone();
        roiRect.grow(extraX, extraY);
        Rectangle rect = roiRect.intersection(new Rectangle(width, height));
        float[] pixels = (float[])((float[])ip.getPixels());
        int length = xDirection ? width : height;
        float[] cache = new float[length];
        float[] vHi = type == 2 ? new float[(2 * xRadius + 1) * (2 * yRadius + 1)] : null;
        float[] vLo = type == 2 ? new float[(2 * xRadius + 1) * (2 * yRadius + 1)] : null;
        int pointInc = xDirection ? 1 : width;
        int lineInc = xDirection ? width : 1;
        int lineFrom = xDirection ? rect.y : rect.x;
        if (lineFrom < 0) {
            lineFrom = 0;
        }

        int lineTo = xDirection ? rect.y + rect.height : rect.x + rect.width;
        if (lineTo > (xDirection ? height : width)) {
            lineTo = xDirection ? height : width;
        }

        int writeFrom = xDirection ? rect.x : rect.y;
        int writeTo = xDirection ? rect.x + rect.width : rect.y + rect.height;
        int readFrom = writeFrom - radius < 0 ? 0 : writeFrom - radius;
        int readTo = writeTo + radius > length ? length : writeTo + radius;
        int pixel0 = lineFrom * lineInc + writeFrom * pointInc;

        for(int line = lineFrom; line < lineTo; pixel0 += lineInc) {
            if (line % 30 == 0) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                this.showProgress((double)(line - lineFrom) / (double)(lineTo - lineFrom));
            }

            int p = line * lineInc + readFrom * pointInc;

            for(int i = readFrom; i < readTo; p += pointInc) {
                cache[i] = pixels[p] * sign;
                ++i;
            }

            switch(type) {
                case 0:
                    lineMean(radius, cache, pixels, writeFrom, writeTo, pixel0, pointInc);
                    break;
                case 1:
                    lineBorderLimitedMean(radius, cache, pixels, writeFrom, writeTo, pixel0, pointInc);
                    break;
                case 2:
                    lineMedian(radius, cache, pixels, writeFrom, writeTo, pixel0, pointInc, vLo, vHi);
                    break;
                case 3:
                case 4:
                    lineMax(radius, sign, cache, pixels, writeFrom, writeTo, pixel0, pointInc);
            }

            ++line;
        }

        ++this.pass;
    }

    private static void lineMean(int radius, float[] cache, float[] pixels, int writeFrom, int writeTo, int pixel0, int pointInc) {
        double sum = 0.0D;
        double factor = 1.0D / (double)(1 + 2 * radius);
        int length = cache.length;
        float first = cache[0];
        float last = cache[length - 1];
        int sumFrom = writeFrom - radius;
        int sumTo = writeFrom + radius;
        if (sumFrom < 0) {
            sum = (double)((float)(-sumFrom) * first);
            sumFrom = 0;
        }

        if (sumTo > length) {
            sum += (double)((float)(sumTo - length) * last);
            sumTo = length;
        }

        int i;
        for(i = sumFrom; i < sumTo; ++i) {
            sum += (double)cache[i];
        }

        i = writeFrom;
        int iMinus = writeFrom - radius;
        int iPlus = writeFrom + radius;

        for(int p = pixel0; i < writeTo; p += pointInc) {
            sum += iPlus < length ? (double)cache[iPlus] : (double)last;
            pixels[p] = (float)(sum * factor);
            sum -= iMinus >= 0 ? (double)cache[iMinus] : (double)first;
            ++i;
            ++iMinus;
            ++iPlus;
        }

    }

    private static void lineBorderLimitedMean(int radius, float[] cache, float[] pixels, int writeFrom, int writeTo, int pixel0, int pointInc) {
        double sum = 0.0D;
        int length = cache.length;
        int sumFrom = writeFrom - radius > 0 ? writeFrom - radius : 0;
        int sumTo = writeFrom + radius < length ? writeFrom + radius : length;
        int kSize = sumTo - sumFrom;

        int i;
        for(i = sumFrom; i < sumTo; ++i) {
            sum += (double)cache[i];
        }

        i = writeFrom;
        int iMinus = writeFrom - radius;
        int iPlus = writeFrom + radius;

        for(int p = pixel0; i < writeTo; p += pointInc) {
            if (iPlus < length) {
                sum += (double)cache[iPlus];
                ++kSize;
            }

            pixels[p] = (float)sum / (float)kSize;
            if (iMinus >= 0) {
                sum -= (double)cache[iMinus];
                --kSize;
            }

            ++i;
            ++iMinus;
            ++iPlus;
        }

    }

    private static void lineMedian(int radius, float[] cache, float[] pixels, int writeFrom, int writeTo, int pixel0, int pointInc, float[] vHi, float[] vLo) {
        int length = cache.length;
        float median = cache[writeFrom];
        int i = writeFrom;
        int iMinus = writeFrom - radius;
        int iPlus = writeFrom + radius;

        for(int p = pixel0; i < writeTo; p += pointInc) {
            int nHi = 0;
            int nLo = 0;
            int iStart = iMinus >= 0 ? iMinus : 0;
            int iStop = iPlus < length ? iPlus : length - 1;
            int nPoints = iStop - iStart + 1;

            int half;
            for(half = iStart; half <= iStop; ++half) {
                float v = cache[half];
                if (v > median) {
                    vHi[nHi++] = v;
                } else if (v < median) {
                    vLo[nLo++] = v;
                }
            }

            if (nPoints % 2 == 0) {
                float v = cache[i];
                if (v > median) {
                    vHi[nHi++] = v;
                } else if (v < median) {
                    vLo[nLo++] = v;
                }
            }

            half = nPoints / 2;
            if (nHi > half) {
                median = RankFilters.findNthLowestNumber(vHi, nHi, nHi - half - 1);
            } else if (nLo > half) {
                median = RankFilters.findNthLowestNumber(vLo, nLo, half);
            }

            pixels[p] = median;
            ++i;
            ++iMinus;
            ++iPlus;
        }

    }

    private static void lineMax(int radius, float sign, float[] cache, float[] pixels, int writeFrom, int writeTo, int pixel0, int pointInc) {
        int length = cache.length;
        int pUp = pixel0;
        int pDn = pixel0 + (writeTo - writeFrom - 1) * pointInc;
        float maxUp = -3.4028235E38F;
        float maxDn = -3.4028235E38F;
        int iInUp = writeFrom + radius;
        int iOutUp = writeFrom - radius - 1;
        int iInDn = writeTo - radius - 1;
        int iOutDn = writeTo + radius;

        while(pUp <= pDn) {
            boolean first;
            int maxFrom;
            int maxTo;
            int i;
            for(first = true; pUp <= pDn; ++iOutUp) {
                if (iInUp < length && maxUp < cache[iInUp]) {
                    maxUp = cache[iInUp];
                }

                if (first || iOutUp >= 0 && cache[iOutUp] == maxUp && (iInUp >= length || cache[iOutUp] > cache[iInUp])) {
                    if (!first) {
                        break;
                    }

                    maxFrom = iOutUp >= -1 ? iOutUp + 1 : 0;
                    maxTo = iInUp < length ? iInUp : length - 1;
                    maxUp = cache[maxFrom];

                    for(i = maxFrom + 1; i <= maxTo; ++i) {
                        if (maxUp < cache[i]) {
                            maxUp = cache[i];
                        }
                    }
                }

                first = false;
                pixels[pUp] = maxUp * sign;
                pUp += pointInc;
                ++iInUp;
            }

            for(first = true; pUp <= pDn; --iOutDn) {
                if (iInDn >= 0 && maxDn < cache[iInDn]) {
                    maxDn = cache[iInDn];
                }

                if (first || iOutDn < length && cache[iOutDn] == maxDn && (iInDn < 0 || cache[iOutDn] > cache[iInDn])) {
                    if (!first) {
                        break;
                    }

                    maxFrom = iOutDn <= length ? iOutDn - 1 : length - 1;
                    maxTo = iInDn > 0 ? iInDn : 0;
                    maxDn = cache[maxFrom];

                    for(i = maxFrom - 1; i >= maxTo; --i) {
                        if (maxDn < cache[i]) {
                            maxDn = cache[i];
                        }
                    }
                }

                first = false;
                pixels[pDn] = maxDn * sign;
                pDn -= pointInc;
                --iInDn;
            }
        }

    }

    private static void resetOutOfRoi(ImageProcessor ip, int extraX, int extraY) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        Rectangle roiRect = ip.getRoi();
        Rectangle temp = (Rectangle)roiRect.clone();
        temp.grow(extraX, extraY);
        Rectangle outer = temp.intersection(new Rectangle(width, height));
        float[] pixels = (float[])((float[])ip.getPixels());
        float[] snapPixels = (float[])((float[])ip.getSnapshotPixels());
        int leftWidth = outer.y;

        int rightWidth;
        for(rightWidth = leftWidth * width + outer.x; leftWidth < roiRect.y; rightWidth += width) {
            System.arraycopy(snapPixels, rightWidth, pixels, rightWidth, outer.width);
            ++leftWidth;
        }

        leftWidth = roiRect.x - outer.x;
        rightWidth = outer.x + outer.width - (roiRect.x + roiRect.width);

        int y;
        int p;
        for(y = roiRect.y; y < roiRect.y + roiRect.height; ++y) {
            if (leftWidth > 0) {
                p = outer.x + y * width;
                System.arraycopy(snapPixels, p, pixels, p, leftWidth);
            }

            if (rightWidth > 0) {
                p = roiRect.x + roiRect.width + y * width;
                System.arraycopy(snapPixels, p, pixels, p, rightWidth);
            }
        }

        y = roiRect.y + roiRect.height;

        for(p = y * width + outer.x; y < outer.y + outer.height; p += width) {
            System.arraycopy(snapPixels, p, pixels, p, outer.width);
            ++y;
        }

    }

    public void setNPasses(int nPasses) {
        if (xRadius > 0 && yRadius > 0) {
            nPasses *= 2;
        }

        this.nPasses = nPasses * taskLists[type].length;
        this.pass = 0;
    }

    private void showProgress(double percent) {
        if (this.nPasses != 0) {
            percent = (double)this.pass / (double)this.nPasses + percent / (double)this.nPasses;
            IJ.showProgress(percent);
        }
    }
}
