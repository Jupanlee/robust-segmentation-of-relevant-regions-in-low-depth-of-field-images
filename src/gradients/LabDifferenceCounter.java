package gradients;

import basics.ColorConversions;
import basics.Tools;
import evaluation.Batch;
import evaluation.Batch.Batchable;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.io.IOException;
import java.util.Vector;

public class LabDifferenceCounter extends PixelOperation
        implements Batch.Batchable
{
    public double getPixelColor(ImageProcessor imageProcessor, int x, int y)
    {
        Vector neighbours = Tools.getNeighbourRGBValues(imageProcessor, x, y, false);
        double[] mean = ColorConversions.mean(ColorConversions.getLab(neighbours));
        double[] centerColor = ColorConversions.getLab(imageProcessor.getPixel(x, y));

        double deltaE = Tools.euklidDistance(mean, centerColor);
        return Math.pow(deltaE, 1.5D);
    }

    public ImageProcessor run(ImageProcessor imageProcessor)
    {
        ImageProcessor result = new ByteProcessor(imageProcessor.getWidth(), imageProcessor.getHeight());
        return PixelOperation.renderImage(imageProcessor, result, this);
    }

    public static void main(String[] args) throws IOException {
        Batch.run(new LabDifferenceCounter(), 500);
    }
}