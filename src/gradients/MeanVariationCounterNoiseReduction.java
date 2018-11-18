package gradients;

import basics.Tools;
import evaluation.Batch;
import evaluation.Batch.Batchable;
import ij.process.ImageProcessor;
import java.io.IOException;

public class MeanVariationCounterNoiseReduction
        implements Batch.Batchable
{
    private double power;

    public MeanVariationCounterNoiseReduction(double power)
    {
        this.power = power;
    }

    public ImageProcessor run(ImageProcessor imageProcessor) {
        ImageProcessor denoised = Tools.median(imageProcessor, 1);
        ImageProcessor medianImage = Tools.median(imageProcessor, 2);
        ImageProcessor differenceImage = Tools.difference(denoised, medianImage).convertToByte(true);

        return Tools.power(differenceImage, this.power);
    }

    public static void main(String[] args) throws IOException
    {
        Batch.run(new MeanVariationCounterNoiseReduction(2.0D), 800);
    }
}