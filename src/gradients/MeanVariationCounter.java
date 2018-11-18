package gradients;

import basics.Tools;
import basics.javaAddons.DEBUG;
import evaluation.Batch;
import evaluation.Batch.Batchable;
import ij.process.ImageProcessor;
import java.io.IOException;
import java.util.Arrays;

public class MeanVariationCounter
        implements Batch.Batchable
{
    public ImageProcessor run(ImageProcessor imageProcessor)
    {
        ImageProcessor denoised = imageProcessor.duplicate();

        ImageProcessor medianConvolvedImage = imageProcessor.duplicate();

        int size = 3;
        float[] medianKernel = new float[size * size];
        Arrays.fill(medianKernel, 1.0F);
        if (DEBUG.getVerbose()) {
            Tools.showImage("1", imageProcessor, "processing original...");
        }
        medianConvolvedImage.convolve(medianKernel, size, size);
        if (DEBUG.getVerbose());
        ImageProcessor differenceImage = medianConvolvedImage.duplicate();
        differenceImage.copyBits(denoised, 0, 0, 8);
        differenceImage = differenceImage.convertToByte(true);
        if (DEBUG.getVerbose());
        return differenceImage;
    }

    public static void main(String[] args) throws IOException {
        Batch.run(new MeanVariationCounter(), 0, "data/batch/images/micro tests");
        Batch.run(new MeanVariationCounter(), 800);
    }
}