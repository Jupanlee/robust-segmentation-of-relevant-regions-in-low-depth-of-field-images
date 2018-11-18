package deviationScoreRegions.tmp;

import basics.ColorConversions;
import basics.Tools;
import gradients.PixelOperation;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.util.Vector;

public class OldMeanLabNeighborDistance extends PixelOperation
        implements PlugInFilter
{
    public double getPixelColor(ImageProcessor imageProcessor, int x, int y)
    {
        Vector neighbours = Tools.getNeighbourRGBValues(imageProcessor, x, y, false);
        double[] mean = ColorConversions.mean(ColorConversions.getLab(neighbours));
        double[] centerColor = ColorConversions.getLab(imageProcessor.getPixel(x, y));

        double deltaE = Tools.euklidDistance(mean, centerColor);
        return Math.pow(deltaE, 2.0D);
    }

    public void run(ImageProcessor imageProcessor)
    {
        imageProcessor.copyBits(run(imageProcessor, 0.95D, 10), 0, 0, 0);
    }

    public ImageProcessor run(ImageProcessor imageProcessor, double blurRadius, int iterations) {
        ImageProcessor result = new ByteProcessor(imageProcessor.getWidth(), imageProcessor.getHeight());
        return PixelOperation.renderImage(Tools.blur(imageProcessor, blurRadius, iterations), result, this);
    }

    public int setup(String string, ImagePlus ip)
    {
        return 31;
    }
}