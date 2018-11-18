package others.morphological;

import basics.Tools;
import basics.math.MMatrix;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.io.File;
import javax.imageio.ImageIO;

public class MorphologicalFilters
{
    public static ImageProcessor invert(ImageProcessor imageProcessor)
    {
        ImageProcessor result = imageProcessor.duplicate();
        result.invert();
        return result;
    }

    public static boolean equalHistograms(ImageProcessor a, ImageProcessor b)
    {
        if ((a == null) || (b == null)) {
            return false;
        }

        int[] histogramA = a.getHistogram();
        int[] histogramB = b.getHistogram();

        for (int i = 0; i < histogramA.length; i++) {
            if (histogramA[i] != histogramB[i]) {
                return false;
            }
        }
        return true;
    }

    public static ImageProcessor dilate(ImageProcessor imageProcessor, float[][] structuringElement) {
        int height = structuringElement[0].length - 1;
        int width = structuringElement.length - 1;

        ImageProcessor np = imageProcessor.createProcessor(imageProcessor.getWidth(), imageProcessor.getHeight());

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (structuringElement[x][y] == 1.0F) {
                    np.copyBits(imageProcessor, y - height / 2, x - width / 2, 13);
                }
            }
        }

        return np;
    }

    public static ImageProcessor erode(ImageProcessor imageProcessor, float[][] structuringElement) {
        return invert(dilate(invert(imageProcessor), structuringElement));
    }

    public static ImageProcessor geodesicDilate(ImageProcessor mask, ImageProcessor marker, int size) {
        ImageProcessor result = marker.duplicate();
        int iterationCount = 0;

        ImageProcessor resultBefore = result.createProcessor(result.getWidth(), result.getHeight());
        while (!equalHistograms(result, resultBefore)) {
            iterationCount++;

            resultBefore.copyBits(result, 0, 0, 0);

            for (int i = 0; i < size; i++) {
                result.erode();
                result.copyBits(mask, 0, 0, 12);
            }
        }

        return result;
    }

    public static ImageProcessor geodesicErode(ImageProcessor mask, ImageProcessor marker, int size) {
        return invert(geodesicDilate(invert(mask), invert(marker), size));
    }

    public static ImageProcessor morphologicalClosingOpeningByReconstruction(ImageProcessor mask, float[][] dilateSE, float[][] erodeSE) {
        ImageProcessor result = mask.duplicate();

        int iterationen = dilateSE.length;

        result = geodesicErode(result, dilate(result, dilateSE), iterationen);

        result = geodesicDilate(result, erode(result, erodeSE), iterationen);

        return result;
    }

    public static ImageProcessor morphologicalClosingOpeningByReconstruction(ImageProcessor mask, ImageProcessor marker, int dilateSize, int erodeSize, int size) {
        ImageProcessor result = mask.duplicate();

        result = geodesicErode(result, marker, erodeSize);

        result = geodesicDilate(result, marker, dilateSize);

        return result;
    }

    public static void main(String[] args) throws Exception {
        ImageProcessor imageProcessor = Tools.resize(new ColorProcessor(ImageIO.read(new File("data/sample.jpg"))), 400);
        Tools.save(imageProcessor);
        imageProcessor.findEdges();
        Tools.save(imageProcessor);

        float[][] se = new MMatrix(1.0F, 20, 20).getTable();

        ImageProcessor marker = dilate(imageProcessor, se);
        Tools.save(marker);

        imageProcessor = geodesicErode(imageProcessor, marker, 20);
        Tools.save(imageProcessor);
    }
}