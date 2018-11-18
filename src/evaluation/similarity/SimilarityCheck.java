package evaluation.similarity;

import basics.Tools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.DeviationScoreRegions_ParameterReduced;
import evaluation.similarity.similarity.distances.MinkowskiFormDistance;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SimilarityCheck
{
    static String maskDir = "/tmp/weiler_tmp/masks/";
    static Double[][] similarity;
    static Double[][] similarityOOI;
    static List<String> fileNames;
    static String imagesFolder;
    static List<String> folders;

    public static void main(String[] args)
            throws IOException
    {
        DEBUG.setVerbose(false);

        getSimilarity("/tmp/weiler_tmp/similarity");
    }

    public static int[] getColorHistogram(ImageProcessor imageProcessor)
    {
        return getColorHistogram(imageProcessor, 8);
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

    private static void getSimilarity(String folder) throws IOException {
        fileNames = Tools.getAllFilesFromDirectoryWithSubfolders(folder, new String[] { ".jpg", ".png" });
        imagesFolder = folder;
        printStrings(fileNames);
        folders = Tools.getAllFolders(folder);

        similarity = new Double[fileNames.size()][fileNames.size()];
        similarityOOI = new Double[fileNames.size()][fileNames.size()];
        int count = fileNames.size() * fileNames.size() / 2;

        for (int i = 0; i < fileNames.size(); i++) {
            String fileNameA = (String)fileNames.get(i);
            for (int j = i + 1; j < fileNames.size(); j++) {
                System.out.println(count-- + " togo.");
                String fileNameB = (String)fileNames.get(j);
                ImageProcessor a = Tools.loadImageProcessor(fileNameA);
                ImageProcessor b = Tools.loadImageProcessor(fileNameB);

                similarity[i][j] = Double.valueOf(getSimilarityScore(a, b));
                System.out.println(fileNameA + " vs. " + fileNameB + " sim = " + similarity[i][j]);

                a.setMask(loadMask(fileNameA));
                b.setMask(loadMask(fileNameB));

                similarityOOI[i][j] = Double.valueOf(getSimilarityScore(a, b));
            }
        }

        System.out.println("Similarity of complete images");
        print(similarity);
        System.out.println("\nSimilarity of OOIs");
        print(similarityOOI);

        printStrings(folders);
        System.out.println("\nMean Similarity of classes");
        print(getClassDistances(similarity));
        System.out.println("\nMean Similarity of classes of OOIs");
        print(getClassDistances(similarityOOI));
    }

    private static <Nr extends Number> Number[][] getClassDistances(Nr[][] matrix) {
        Number[][] classDistances = new Double[folders.size()][folders.size()];
        for (int i = 0; i < folders.size(); i++) {
            for (int j = i; j < folders.size(); j++) {
                classDistances[i][j] = Double.valueOf(getClassDistance((String)folders.get(i), (String)folders.get(j), matrix));
            }
        }

        return classDistances;
    }

    private static <Nr extends Number> double getClassDistance(String classAFolder, String classBFolder, Nr[][] matrix) {
        List distances = new LinkedList();

        for (int i = 0; i < matrix.length; i++) {
            for (int j = i + 1; j < matrix[i].length; j++)
            {
                String dirA = Tools.getDir((String)fileNames.get(i));
                String dirB = Tools.getDir((String)fileNames.get(j));

                if ((dirA.equals(classAFolder)) && (dirB.equals(classBFolder))) {
                    distances.add(Double.valueOf(matrix[i][j].doubleValue()));
                }
            }
        }

        return Tools.getMean(distances);
    }

    private static <Nr extends Number> void print(Nr[][] matrix)
    {
        List folders = Tools.getAllFolders(imagesFolder);

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                int value = matrix[i][j] != null ? matrix[i][j].intValue() : 0;

                System.out.print(value + ", ");
            }
            System.out.println();
        }
    }

    private static void printStrings(Collection<String> strings) {
        for (String s : strings)
        {
            System.out.print(s + ", ");
        }
        System.out.println();
    }

    private static double getSimilarityScore(ImageProcessor a, ImageProcessor b) {
        double score = new MinkowskiFormDistance(2.0D).getDistance(getColorHistogram(a), getColorHistogram(b));
        if (DEBUG.getVerbose()) {
            System.out.print("a = ");
            Tools.printIntArray(getColorHistogram(a), -1, "000");
            System.out.print("b = ");
            Tools.printIntArray(getColorHistogram(b), -1, "000");
            Tools.save(drawSimInImage(" dist = " + Tools.formatNumber(score), a, b));
        }
        return score;
    }

    private static ImageProcessor drawSimInImage(String score, ImageProcessor a, ImageProcessor b) {
        ImageProcessor result = new ColorProcessor(a.getWidth() + b.getWidth(), Math.max(a.getHeight(), b.getHeight()));
        result.copyBits(Tools.cropToMask(a, Color.blue), 0, 0, 0);
        result.copyBits(Tools.cropToMask(b, Color.blue), a.getWidth(), 0, 0);
        result = Tools.write(score, result);
        return result;
    }

    public static void generateMasks(String folder, int size) throws IOException {
        List fileNames = Tools.getAllFilesFromDirectoryWithSubfolders(folder, new String[] { ".jpg", ".png" });
        int count = fileNames.size();
        for (String fileName : fileNames) {
            System.out.println(count-- + " " + fileName);
            ImageProcessor original = Tools.loadImageProcessor(fileName, size);
            new DeviationScoreRegions_ParameterReduced().run(original);
            ImageProcessor mask = original.getMask();
            Tools.save(mask, "/tmp/weiler_tmp/dsrMasks/" + Tools.getNameWithoutExtension(fileName) + ".png");
        }
    }

    private static ImageProcessor loadMask(String fileName) throws IOException {
        ImageProcessor original = Tools.loadImageProcessor(fileName);
        ImageProcessor mask = Tools.loadImageProcessor(maskDir + Tools.getNameWithoutExtension(fileName) + ".png");
        original.setMask(mask.resize(original.getWidth(), original.getHeight()));
        mask = mask.convertToByte(false);
        return mask;
    }
}