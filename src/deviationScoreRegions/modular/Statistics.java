package deviationScoreRegions.modular;

import basics.Tools;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.io.PrintStream;

public class Statistics
{
    public static ImageProcessor sum(ImageProcessor[] imageProcessors)
    {
        ImageProcessor sum = new FloatProcessor(imageProcessors[0].getWidth(), imageProcessors[0].getHeight());
        for (ImageProcessor img : imageProcessors) {
            sum.copyBits(img, 0, 0, 3);
        }
        return sum;
    }

    public static ImageProcessor avg(ImageProcessor[] imageProcessors) {
        ImageProcessor avg = sum(imageProcessors);
        avg.multiply(1.0F / imageProcessors.length);
        return avg;
    }

    public static ImageProcessor standardDeviation(ImageProcessor[] imageProcessors) {
        ImageProcessor avg = avg(imageProcessors);

        ImageProcessor sum = new FloatProcessor(avg.getWidth(), avg.getHeight());
        for (ImageProcessor i : imageProcessors) {
            ImageProcessor diff = Tools.difference(i, avg);

            diff.sqr();

            sum.copyBits(diff, 0, 0, 3);
        }

        sum.multiply(1.0F / (imageProcessors.length - 1));
        sum.sqrt();
        return sum.convertToByte(true);
    }

    public static void main(String[] args) {
        ImageProcessor a1 = new ByteProcessor(1, 1);
        a1.putPixelValue(0, 0, 3.0D);

        ImageProcessor a2 = new ByteProcessor(1, 1);
        a2.putPixelValue(0, 0, 4.0D);

        ImageProcessor a3 = new ByteProcessor(1, 1);
        a3.putPixelValue(0, 0, 5.0D);

        ImageProcessor a4 = new ByteProcessor(1, 1);
        a4.putPixelValue(0, 0, 6.0D);

        ImageProcessor a5 = new ByteProcessor(1, 1);
        a5.putPixelValue(0, 0, 7.0D);

        ImageProcessor[] imageProcessors = { a1, a2, a3, a4, a5 };
        ImageProcessor stdDev = standardDeviation(imageProcessors);
        System.out.println("stdev == " + stdDev.getPixelValue(0, 0));
    }
}