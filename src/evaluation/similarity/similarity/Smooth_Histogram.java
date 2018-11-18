package evaluation.similarity.similarity;

import basics.Tools;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.Duplicater;
import ij.process.ImageProcessor;
import ij.process.StackStatistics;
import java.io.IOException;
import java.io.PrintStream;

public class Smooth_Histogram
{
    private boolean batch;
    private static final String pluginName = "Histo Peaks";
    private double mean;
    private double stdDev;
    private double hMin;
    private double hMax;
    private double binWidth;
    private static final int MIN_USER_DEFINED = 1;
    private static final int MAX_USER_DEFINED = 2;
    private static final int AUTO = 4;
    private static final int MIN_MAX = 8;
    private int min_max_mode = 4;
    private boolean bin_width_1 = false;
    private boolean negative_pixels = false;
    private double stdevs = 2.0D;

    public int[] histogram(ImagePlus imp, int nBins) {
        if (!this.batch) {
            Duplicater dup = new Duplicater();
            imp = dup.duplicateStack(imp, "Copy");
        }

        WindowManager.setTempCurrentImage(imp);

        if ((imp.getType() == 3) || (imp.getType() == 4))
            IJ.error("Histo Peaks", "Cannot process colour images");
        else if ((imp.getType() == 1) || (imp.getType() == 0)) {
            IJ.run("32-bit");
        }

        int measurements = 278;
        if (!this.negative_pixels) {
            imp.getProcessor().setThreshold(1.0D, 1.7976931348623157E+308D, 2);
        }

        Analyzer.setMeasurements(measurements);
        StackStatistics stats = new StackStatistics(imp);
        this.mean = stats.mean;
        this.stdDev = stats.stdDev;
        if ((this.min_max_mode & 0x4) != 0) {
            if ((this.min_max_mode & 0x1) == 0) this.hMin = (this.mean - this.stdevs * this.stdDev);
            if ((this.min_max_mode & 0x2) == 0) this.hMax = (this.mean + this.stdevs * this.stdDev);
        }
        else if ((this.min_max_mode & 0x8) != 0) {
            if ((this.min_max_mode & 0x1) == 0) this.hMin = stats.min;
            if ((this.min_max_mode & 0x2) == 0) this.hMax = stats.max;
        }
        if (!this.negative_pixels) {
            this.hMin = Math.max(0.0D, this.hMin);
        }

        ImageStack stack = imp.getStack();
        double scalingRatio = (this.hMax - this.hMin) / (this.hMax - this.hMin + 1.0D);
        for (int i = 1; i <= imp.getImageStackSize(); i++) {
            float[] pixels = (float[])(float[])stack.getProcessor(i).getPixels();
            for (int k = 0; k < pixels.length; k++) {
                pixels[k] = (float)((pixels[k] + Math.random() - this.hMin) * scalingRatio + this.hMin);
            }
        }

        this.hMax += 1.0D;

        if (this.bin_width_1) {
            nBins = (int)Math.round(this.hMax - this.hMin);
            this.binWidth = 1.0D;
        } else {
            this.binWidth = ((this.hMax - this.hMin) / nBins);
        }

        imp.getProcessor().setThreshold(4.9E-324D, 1.7976931348623157E+308D, 2);
        measurements = 22;
        Analyzer.setMeasurements(measurements);

        stats = new StackStatistics(imp, nBins, this.hMin, this.hMax);

        return stats.histogram;
    }

    double bin2intensity(int bin) {
        return this.hMin + bin * this.binWidth;
    }

    public static void main(String[] args) throws IOException {
        ImageProcessor imageA = Tools.loadImageProcessor("data/testpix/a002.jpg").convertToByte(true);

        int[] histogramIJ = imageA.getHistogram();
        int[] histogram = new Smooth_Histogram().histogram(new ImagePlus("test", imageA), 256);

        for (int i = 0; i < histogram.length; i++)
            System.out.println(histogramIJ[i] + " -> " + histogram[i]);
    }
}