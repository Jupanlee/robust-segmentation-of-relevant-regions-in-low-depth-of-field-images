package evaluation;

import basics.ColorConversions;
import basics.Stopwatch;
import basics.Tools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.DeviationScoreRegions_Tuned;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.io.PrintStream;
import java.util.Vector;

public class Batch
{
    private static String stdImagesDirectory = "../../images/base_640";
    private static String stdSuffix = "";
    private static String stdReferenceMasks = "../../images/masks";
    private static String stdGeneratedMasks = "../../masks_generated";
    private static int stdSize = 500;
    private static String logFileName = "log.txt";
    private static boolean includeSubfolders = true;
    private static boolean invertMask = false;
    private static boolean blueMaskBackground = true;
    private static boolean printSteps = true;
    private static String saveDir = System.getProperty("user.home") + "/tmp/doftmp/";
    private static String saveSubdirectory = "";
    private static boolean grayscale = false;

    public static void setGrayscale(boolean grayscale) {
        grayscale = grayscale;
    }

    public static void run(Batchable batchable, int[] sizes, String imagesDir)
    {
        printSteps = false;
        DEBUG.setVerbose(false);
        for (int size : sizes) {
            System.out.println("Size = " + size);
            saveSubdirectory = "size " + size + "/";
            run(batchable, size, imagesDir);
        }
    }

    public static void run(Batchable batchable, int size, String imagesDir) {
        run(batchable, imagesDir, stdSuffix, stdReferenceMasks, stdGeneratedMasks + "/" + batchable.getClass().getName(), size);
    }

    public static void run(Batchable batchable, int size) {
        run(batchable, stdImagesDirectory, stdSuffix, stdReferenceMasks, stdGeneratedMasks + "/" + batchable.getClass().getName(), size);
    }

    public static void run(Batchable batchable) {
        run(batchable, stdImagesDirectory, stdSuffix, stdReferenceMasks, stdGeneratedMasks + "/" + batchable.getClass().getName(), stdSize);
    }

    public static void run(Batchable batchable, String imagesDir, String suffix, String referenceMaskDir, String generatedMasksDirectory, int width)
    {
        try
        {
            Stopwatch stopwatch = new Stopwatch();

            Vector evaluations = new Vector();
            evaluations.add(new Evaluation(new EvaluateSpatialDistortion()));

            for (String fileName : Tools.getFilesFromDirectory(imagesDir, suffix, includeSubfolders))
            {
                if ((fileName.endsWith(".jpg")) || (fileName.endsWith(".png")))
                {
                    String name = Tools.getFileNameWithoutDirectory(fileName);
                    DEBUG.println("Processing " + fileName);
                    imageProcessor = Tools.loadImageProcessor(fileName);
                    if (grayscale) imageProcessor = imageProcessor.convertToByte(true);

                    if ((Math.max(imageProcessor.getWidth(), imageProcessor.getHeight()) > width) &&
                            (width != 0)) imageProcessor = Tools.resize(imageProcessor, width);

                    stopwatch.start();
                    ImageProcessor result = batchable.run(imageProcessor);
                    stopwatch.stop();
                    DEBUG.println("Execution time in ms: " + stopwatch.getLastTime());

                    if (result != null) {
                        if (blueMaskBackground) {
                            Tools.save(Tools.maskBackground(imageProcessor, Color.blue));
                        }
                        else {
                            Tools.save(result, saveDir + saveSubdirectory + name);
                        }

                    }

                    if (imageProcessor.getMask() != null) {
                        Tools.saveToFile(imageProcessor.getMask(), generatedMasksDirectory + "/" + Tools.getFileNameWithoutDirectory(fileName));

                        reference = Tools.loadImageProcessor(referenceMaskDir + "/" + Tools.getNameWithoutExtension(fileName) + ".png");
                        if ((invertMask) && (reference != null)) reference.invert();
                        if (printSteps) Evaluation.printTitle();

                        for (Evaluation evaluation : evaluations) {
                            evaluation.run(imageProcessor.getMask(), reference);
                            if (printSteps) System.out.println(evaluation);
                        }
                    }
                }
            }
            ImageProcessor imageProcessor;
            ImageProcessor reference;
            System.out.println("==================== SUMMARY ======================");
            System.out.println(stopwatch);
            for (Evaluation evaluation : evaluations)
            {
                evaluation.printStatisticSummary();
                Tools.appendToFile(logFileName, evaluation.getStringArray());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ColorConversions.useLookupTable();

        DEBUG.setVerbose(false);
        Tools.deleteFile(logFileName);
        String imagesPath = "../data/batch/images/tmp";
        run(new DeviationScoreRegions_Tuned(), 350, imagesPath);
    }

    public static abstract interface Batchable
    {
        public abstract ImageProcessor run(ImageProcessor paramImageProcessor);
    }
}