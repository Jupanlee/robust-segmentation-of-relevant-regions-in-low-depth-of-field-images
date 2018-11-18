package evaluation.fuzzy;

import basics.Tools;
import basics.javaAddons.DEBUG;
import evaluation.Batch;
import evaluation.Batch.Batchable;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;
import others.fuzzy.filters.color.colors.ColorFactory;
import others.fuzzy.filters.color.colors.ColorFactoryLUV;
import others.fuzzy.filters.color.colors.ColorFactoryLab;
import others.fuzzy.filters.color.colors.ColorFactoryRGB;
import others.fuzzy.filters.fuzzy.FuzzySegmentationFilter;
import others.fuzzy.filters.fuzzy.Parameters;

public class FuzzySegmentationBatch
        implements Batch.Batchable
{
    public static Parameters parameters = new Parameters();

    public static void main(String[] args)
            throws IOException
    {
        DEBUG.setVerbose(false);

        Batch.run(new FuzzySegmentationBatch(), 300, "../../images/tmp");
    }

    public static Parameters getTunedParameter()
    {
        Parameters parameters = new Parameters(0.2F, 16, 17, 70);
        Parameters.FACTORY = new ColorFactoryLUV();
        return parameters;
    }

    public static void testParameter(String fileName, int size) throws IOException
    {
        ImageProcessor i = Tools.loadImageProcessor(fileName, size);

        double THRESHOLD = 0.142857149243355D;
        int SIGMA_SPATIAL = 16;
        int SIGMA_RANGE = 18;
        int MINSIZE = 80;

        for (ColorFactory cf : new ColorFactory[] { new ColorFactoryRGB(), new ColorFactoryLab(), new ColorFactoryLUV() }) {
            Parameters.FACTORY = cf;
            for (double delta = -0.3D; delta < 2.0D; delta += 0.1D) {
                ImageProcessor original = i.duplicate();
                ImageProcessor mask = original.duplicate();
                double threshold = THRESHOLD + THRESHOLD * delta;
                int sigma_spatial = (int)(SIGMA_SPATIAL + SIGMA_SPATIAL * delta);
                int sigma_range = (int)(SIGMA_RANGE + SIGMA_RANGE * delta);
                int minsize = (int)(MINSIZE + MINSIZE * delta);
                Parameters parameters = new Parameters((float)threshold, sigma_spatial, sigma_range, minsize);
                System.out.println(parameters);

                FuzzySegmentationFilter filter = new FuzzySegmentationFilter(parameters);
                filter.setup("", new ImagePlus("img", mask));
                filter.run(mask);
                mask.invert();
                original.setMask(mask);
                original.copyBits(Tools.cropToMask(original), 0, 0, 0);

                String resultFileName = System.getProperty("user.home") + "/tmp/doftmp/" + Tools.getNameOnly(fileName) + " " + parameters.toString() + ".jpg";
                Tools.save(Tools.maskBackground(original, Color.blue), resultFileName);
            }
        }
    }

    public ImageProcessor run(ImageProcessor original) {
        ImageProcessor mask = original.duplicate();

        if (DEBUG.getVerbose()) {
            Tools.save(mask);
        }

        FuzzySegmentationFilter filter = new FuzzySegmentationFilter(parameters);
        filter.setup("", new ImagePlus("img", mask));
        filter.run(mask);
        mask.invert();
        original.setMask(mask);
        original.copyBits(Tools.cropToMask(original), 0, 0, 0);
        return original;
    }

    public String toString()
    {
        return "Fuzzy\t" + parameters.toString();
    }
}