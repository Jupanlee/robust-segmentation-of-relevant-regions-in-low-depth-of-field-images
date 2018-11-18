package evaluation.similarity.similarity;

import basics.Tools;
import ij.process.ImageProcessor;
import java.io.IOException;

public class HistogramSimilarity
{
    public static void main(String[] args)
            throws IOException
    {
        ImageProcessor imageA = Tools.loadImageProcessor("data/testpix/a002.jpg").convertToByte(true);
        ImageProcessor imageB = Tools.loadImageProcessor("data/testpix/a002_small.jpg").convertToByte(true);

        ImageProcessor n003 = Tools.loadImageProcessor("data/testpix/n003.png").convertToByte(true);
        ImageProcessor n003_rotate = Tools.loadImageProcessor("data/testpix/n003_rotate.png").convertToByte(true);

        int[] histogramA = n003.getHistogram();
        int[] histogramB = n003_rotate.getHistogram();
    }
}