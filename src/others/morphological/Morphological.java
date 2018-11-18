package others.morphological;

import basics.Tools;
import basics.math.MMatrix;
import ij.process.ImageProcessor;

public class Morphological
{
    private static boolean DEBUG = false;

    public static ImageProcessor dilate(ImageProcessor imageProcessor, int size) {
        if (size == 0) return imageProcessor.duplicate();

        float[][] structuringElement = new MMatrix(1.0F, size, size).getTable();
        return dilate(imageProcessor, structuringElement);
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

    public static ImageProcessor erode(ImageProcessor imageProcessor, int size) {
        if (size <= 1) return imageProcessor.duplicate();
        float[][] structuringElement = new MMatrix(1.0F, size, size).getTable();
        return erode(imageProcessor, structuringElement);
    }

    public static ImageProcessor erode(ImageProcessor imageProcessor, float[][] structuringElement) {
        return Tools.invert(dilate(Tools.invert(imageProcessor), structuringElement));
    }

    public static ImageProcessor close(ImageProcessor imageProcessor, int size) {
        if (size <= 1) return imageProcessor.duplicate();
        float[][] structuringElement = new MMatrix(1.0F, size, size).getTable();
        return erode(dilate(imageProcessor, structuringElement), structuringElement);
    }

    public static ImageProcessor open(ImageProcessor imageProcessor, int size) {
        float[][] structuringElement = new MMatrix(1.0F, size, size).getTable();
        return dilate(erode(imageProcessor, structuringElement), structuringElement);
    }

    public static ImageProcessor geodesicDilate(ImageProcessor mask, ImageProcessor marker, int size, int maxIterations) {
        ImageProcessor result = marker.duplicate();
        int iterationCount = 0;

        ImageProcessor resultBefore = result.createProcessor(result.getWidth(), result.getHeight());
        boolean maxIterationsNotReached = true;
        while ((!Tools.equalHistograms(result, resultBefore)) && (maxIterationsNotReached)) {
            iterationCount++;

            maxIterationsNotReached = (maxIterations == -1) || (iterationCount < maxIterations);

            resultBefore.copyBits(result, 0, 0, 0);

            for (int i = 0; i < size; i++) {
                result.erode();
                result.copyBits(mask, 0, 0, 12);
            }

            if (DEBUG) {
                Tools.showGrayscaleImageInNewWindow(result, iterationCount + ". geodesicDilate iteration");
            }
        }

        return result;
    }

    public static ImageProcessor geodesicErode(ImageProcessor mask, ImageProcessor marker, int size, int maxIterations) {
        return Tools.invert(geodesicDilate(Tools.invert(mask), Tools.invert(marker), size, maxIterations));
    }

    public static ImageProcessor dilateByReconstruction(ImageProcessor imageprocesor, int size) {
        if (size <= 1) return imageprocesor.duplicate();
        return geodesicErode(imageprocesor, dilate(imageprocesor, size), size, -1);
    }

    public static ImageProcessor morphologicalClosingOpeningByReconstruction(ImageProcessor mask, float[][] dilateSE, float[][] erodeSE) {
        ImageProcessor result = mask.duplicate();

        int iterationen = dilateSE.length;

        result = geodesicErode(result, dilate(result, dilateSE), iterationen, -1);

        result = geodesicDilate(result, erode(result, erodeSE), iterationen, -1);

        return result;
    }

    public static ImageProcessor morphologicalClosingOpeningByReconstruction(ImageProcessor mask, ImageProcessor marker, int dilateSize, int erodeSize, int size) {
        return morphologicalClosingOpeningByReconstruction(mask, marker, dilateSize, erodeSize, size, -1);
    }

    public static ImageProcessor morphologicalClosingOpeningByReconstruction(ImageProcessor mask, ImageProcessor marker, int dilateSize, int erodeSize, int size, int maxIterations) {
        ImageProcessor result = mask.duplicate();

        result = geodesicErode(result, marker, erodeSize, maxIterations);

        result = geodesicDilate(result, marker, dilateSize, maxIterations);

        return result;
    }

    public static ImageProcessor morphologicalClosingOpeningByReconstruction(ImageProcessor mask, int dilateSize, int erodeSize) {
        float[][] dilateSE = new MMatrix(1.0F, dilateSize, dilateSize).getTable();
        float[][] erodeSE = new MMatrix(1.0F, erodeSize, erodeSize).getTable();

        return morphologicalClosingOpeningByReconstruction(mask, dilateSE, erodeSE);
    }

    public static void main(String[] args) throws Exception
    {
        ImageProcessor mask = Tools.loadImageProcessor("data/test/mask.png");
        ImageProcessor marker = Tools.loadImageProcessor("data/test/marker.png");

        Tools.save(geodesicErode(mask, marker, 1, 1));
        Tools.save(dilate(marker, 1));
    }
}