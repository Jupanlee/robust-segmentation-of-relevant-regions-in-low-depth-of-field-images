package evaluation.similarity.similarity;

import basics.Tools;
import basics.javaAddons.DEBUG;
import evaluation.similarity.similarity.distances.L1Dist;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import others.fuzzy.filters.similarity.ImageDistance;

public class SimilarityCheckTMP
{
    public static void main(String[] args)
            throws IOException
    {
        DEBUG.setVerbose(false);
        test(null, "../../../data/batch/images/tmp");
    }

    public static int[] getColorHistogram(ImageProcessor imageProcessor) {
        return getColorHistogram(imageProcessor, 3);
    }

    public static int[] getColorHistogram(ImageProcessor original, int binsPerColorChannel) {
        int maxColorValue = 256;
        int pixelcount = 0;

        double colorCountPerBin = maxColorValue / binsPerColorChannel;
        double[] colorHistogram = new double[binsPerColorChannel * 3];

        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                if ((original.getMask() == null) || (original.getMask().getPixel(x, y) > 0)) {
                    Color c = new Color(original.getPixel(x, y));
                    pixelcount++;

                    int r = (int)(c.getRed() / colorCountPerBin);
                    int g = (int)(c.getGreen() / colorCountPerBin);
                    int b = (int)(c.getBlue() / colorCountPerBin);

                    colorHistogram[(binsPerColorChannel * 0 + r)] += 1.0D;
                    colorHistogram[(binsPerColorChannel * 1 + g)] += 1.0D;
                    colorHistogram[(binsPerColorChannel * 2 + b)] += 1.0D;
                }
            }
        }

        int[] result = new int[colorHistogram.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (int)(colorHistogram[i] / pixelcount * 1000.0D);
        }

        return result;
    }

    private static boolean isImage(String fileName) {
        return (fileName.endsWith(".jpg")) || (fileName.endsWith(".png"));
    }

    private static void test(ImageDistance dist, String folder) throws IOException {
        List processedFiles = new LinkedList();
        for (Iterator i$ = Tools.getAllFilesFromDirectoryWithSubfolders(folder, "").iterator(); i$.hasNext(); ) {
            String fileNameA = (String)i$.next();
            if (isImage(fileNameA))
            {
                processedFiles.add(fileNameA);
                for (String fileNameB : Tools.getAllFilesFromDirectoryWithSubfolders(folder, ""))
                    if ((isImage(fileNameB)) &&
                            (!processedFiles.contains(fileNameB))) {
                        System.out.println(fileNameA + " vs. " + fileNameB);

                        ImageProcessor a = Tools.loadImageProcessor(fileNameA);
                        ImageProcessor b = Tools.loadImageProcessor(fileNameB);

                        System.out.println(dist + "(a, b) == " + getSimilarityScore(dist, a, b));

                        a.setMask(loadMask(fileNameA));
                        b.setMask(loadMask(fileNameB));

                        System.out.println(dist + "(ooi(a), ooi(b)) == " + getSimilarityScore(dist, a, b));
                    }
            }
        }
        String fileNameA;
    }

    private static double getSimilarityScore(ImageDistance dist, ImageProcessor a, ImageProcessor b)
    {
        double score = new L1Dist().getDistance(getColorHistogram(a), getColorHistogram(b));

        Tools.save(drawSimInImage(dist + " dist = " + Tools.formatNumber(score, "#,###,###.##"), a, b));
        return score;
    }

    private static ImageProcessor drawSimInImage(String score, ImageProcessor a, ImageProcessor b) {
        ImageProcessor result = new ColorProcessor(a.getWidth() + b.getWidth(), Math.max(a.getHeight(), b.getHeight()));
        result.copyBits(Tools.cropToMask(a, Color.blue), 0, 0, 0);
        result.copyBits(Tools.cropToMask(b, Color.blue), a.getWidth(), 0, 0);
        result = Tools.write(score, result);
        return result;
    }

    private static boolean[][] getROIFromMask(ImageProcessor mask) {
        boolean[][] roi = new boolean[mask.getWidth()][mask.getHeight()];
        for (int x = 0; x < roi.length; x++) {
            for (int y = 0; y < roi[x].length; y++) {
                roi[x][y] = (mask.getPixel(x, y) >= 1 ? true : false);
            }
        }
        return roi;
    }

    private static ImageProcessor loadMask(String fileName) throws IOException {
        ImageProcessor mask = Tools.loadImageProcessor("../../../data/batch/reference masks/" + Tools.getNameWithoutExtension(fileName) + ".png");
        mask = mask.convertToByte(false);
        mask.invert();
        return mask;
    }
}