package newTry;

import basics.Tools;
import deviationScoreRegions.modular.edgeFilters.MeanLabNeighborDistance;
import evaluation.Batch.Batchable;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Rectangle;
import java.io.IOException;
import others.morphological.Morphological;

public class GridDivide
        implements Batch.Batchable
{
    static double minValue = 30.0D;
    static double incValue = 512.0D;
    static int size = 512;
    static double p = -2.0D;
    static ImageProcessor scoreImage;

    public static void main(String[] args)
            throws IOException
    {
        for (String fileName : Tools.getFilesFromDirectory("../../images/base", ".jpg")) {
            ImageProcessor i = Tools.loadImageProcessor(fileName, 600);
            new GridDivide().run(i);
        }
    }

    public ImageProcessor run(ImageProcessor i)
    {
        scoreImage = new ByteProcessor(size, size);

        ImageProcessor edges = i.duplicate();
        new MeanLabNeighborDistance().run(edges);
        Tools.save(edges);
        int closeSize = (int)Math.sqrt(i.getPixelCount() * 0.005D);
        int recSize = (int)Math.sqrt(i.getPixelCount() * 0.02D);
        ImageProcessor closed = Morphological.close(edges, closeSize);
        Tools.save(closed);
        Tools.save(Morphological.dilateByReconstruction(closed, recSize));

        process(i.resize(size, size), 0, 0, size, size);
        scoreImage = scoreImage.resize(i.getWidth(), i.getHeight());
        Tools.save(scoreImage);
        return i;
    }

    public static double getMax(ImageProcessor ip) {
        double max = ip.getPixelValue(0, 0);

        for (int x = 0; x < ip.getWidth(); x++) {
            for (int y = 0; y < ip.getHeight(); y++) {
                double v = ip.getPixelValue(x, y);
                if (v <= max) continue; max = v;
            }
        }

        return max;
    }

    private static void process(ImageProcessor i, int x, int y, int width, int height) {
        ImageProcessor crop = crop(i, x, y, width, height);
        ImageProcessor edges = null;

        if (getMax(edges) > minValue)
        {
            incPixels(scoreImage, x, y, width, height, Math.pow(width, p) * incValue);

            int deltaX = width / 2;
            int deltaY = height / 2;
            process(i, x, y, deltaX, deltaY);
            process(i, x + deltaX, y, deltaX, deltaY);
            process(i, x, y + deltaY, deltaX, deltaY);
            process(i, x + deltaX, y + deltaY, deltaX, deltaY);
        }
    }

    private static void incPixels(ImageProcessor i, int fromX, int fromY, int width, int height, double value) {
        for (int x = fromX; x < fromX + width; x++)
            for (int y = fromY; y < fromY + height; y++)
                i.putPixelValue(x, y, i.getPixelValue(x, y) + value);
    }

    private static ImageProcessor crop(ImageProcessor i, int x, int y, int width, int height)
    {
        i.setRoi(new Rectangle(x, y, width, height));
        return i.crop();
    }

    private static ImageProcessor[] split(ImageProcessor i)
    {
        if (i.getPixelCount() == 1) return new ImageProcessor[] { i };

        int x = i.getWidth() / 2;
        int y = i.getHeight() / 2;

        i.setRoi(new Rectangle(0, 0, x, y));
        ImageProcessor topLeft = i.crop();
        i.setRoi(new Rectangle(x, y, i.getWidth(), i.getHeight()));
        ImageProcessor bottomRight = i.crop();
        i.setRoi(new Rectangle(x, 0, i.getWidth(), y));
        ImageProcessor topRight = i.crop();
        i.setRoi(new Rectangle(0, y, x, i.getHeight()));
        ImageProcessor bottomLeft = i.crop();

        return new ImageProcessor[] { topLeft, topRight, bottomLeft, bottomRight };
    }
}