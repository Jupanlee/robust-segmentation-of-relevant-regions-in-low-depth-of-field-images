package deviationScoreRegions.modular.edgeFilters;

import basics.Tools;
import ij.process.ImageProcessor;
import java.io.IOException;

public class Sobel
        implements EdgeFilter
{
    private static final float[] sobel = { -0.125F, 0.0F, 0.125F, -0.25F, 0.0F, 0.25F, -0.125F, 0.0F, 0.125F };

    public void run(ImageProcessor i)
    {
        i.convolve(sobel, 3, 3);
        i.sqr();
    }

    public static void main(String[] args) throws IOException {
        for (String fileName : Tools.getFilesFromDirectory("../../images/base", ".jpg")) {
            ImageProcessor original = Tools.resize(Tools.loadImageProcessor(fileName), 500);
            new Sobel().run(original);
            Tools.save(original);
        }
    }
}