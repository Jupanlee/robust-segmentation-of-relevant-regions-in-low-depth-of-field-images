package evaluation;

import basics.Tools;
import basics.filter.canny.CannyEdgeDetection;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.DeviationScoreRegions_2_differentSizes;
import deviationScoreRegions.modular.DSR_Modular;
import evaluation.fuzzy.FuzzySegmentationBatch;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import others.morphological.MorphologicalOOIExtraction;
import others.sirithana.VideoSegmentation;

public class Versus
{
    public static void main(String[] args)
            throws Exception
    {
        DEBUG.setVerbose(false);

        versus();
    }

    public static void cannyParameters() throws IOException {
        int counter = 0;
        int size = 800;
        for (String fileName : Tools.getFilesFromDirectory("../../images/edgeExtractionComparison", ".jpg")) {
            counter++;
            ImageProcessor original = Tools.loadImageProcessor(fileName, size).convertToByte(true);

            double radius = 3.0D;
            float alpha = 0.5F;
            float upper = 100.0F;
            float lower = 50.0F;

            String str = System.getProperty("user.home") + "/tmp/doftmp/" + Tools.formatNumber(counter, "000");
            for (float delta = -0.1F; delta <= 10.0F; delta = (float)(delta + 0.25D)) {
                ImageProcessor canny = CannyEdgeDetection.run(original, radius * delta, alpha, upper * delta, lower * delta);
                Tools.save(canny, str + " delta = " + Tools.formatNumber(delta) + ".png");
            }
        }
    }

    public static void versus()
            throws IOException, Exception
    {
        int counter = 0;
        List files = Tools.getFilesFromDirectory("../../images/tmp", ".jpg");

        for (String fileName : files) {
            counter++;
            DEBUG.setVerbose(false);
            String str = System.getProperty("user.home") + "/tmp/doftmp/" + Tools.formatNumber(counter, "000");
            ImageProcessor original = Tools.loadImageProcessor(fileName);
            System.out.println("Processing " + fileName);

            int sizeDSR = 640;
            int sizeMorph = 640;
            int sizeFuzzy = 640;
            int sizeVideo = sizeDSR;

            Tools.saveToFile(Tools.resize(original, sizeDSR), str + "_A_" + Tools.getNameOnly(fileName) + "_" + sizeDSR + ".png");
            saveToFile(new DSR_Modular(), original, sizeDSR, str + "_B_DSR" + sizeDSR + ".png");
            saveToFile(new MorphologicalOOIExtraction(), original, sizeMorph, str + "_C_Morph" + sizeMorph + ".png");
            saveToFile(new VideoSegmentation(), original, sizeVideo, str + "_E_Video" + sizeVideo + ".png");
            FuzzySegmentationBatch.parameters = FuzzySegmentationBatch.getTunedParameter();
            saveToFile(new FuzzySegmentationBatch(), original, sizeFuzzy, str + "_D_Fuzzy" + sizeFuzzy + ".png");
        }
    }

    public static void findSimilarResults(int size) throws Exception {
        Batch.Batchable[] algorithms = { new DeviationScoreRegions_2_differentSizes(), new MorphologicalOOIExtraction(), new FuzzySegmentationBatch(), new VideoSegmentation() };
        for (String fileName : Tools.getFilesFromDirectory("../../images/base_640", ".jpg")) {
            Map spatialdistortion = new HashMap();
            for (Batch.Batchable algorithm : algorithms) {
                spatialdistortion.put(algorithm, Double.valueOf(getSpatialDistorition(algorithm, fileName, size)));
            }

            double max = 0.0D;
            for (Iterator i$ = spatialdistortion.values().iterator(); i$.hasNext(); ) { double value = ((Double)i$.next()).doubleValue();
                max = Math.max(max, value);
            }

            if (max <= 30.0D)
                Tools.save(Tools.loadImageProcessor(fileName), "../../../data/similarResults/" + Tools.getNameWithoutExtension(fileName) + " SD " + max + ".jpg");
        }
    }

    private static double getSpatialDistorition(Batch.Batchable b, String fileName, int size)
            throws Exception
    {
        ImageProcessor segmented = b.run(Tools.loadImageProcessor(fileName, size));
        Tools.save(segmented);
        ImageProcessor reference = Tools.loadImageProcessor("../../../data/batch/reference masks/" + Tools.getNameWithoutExtension(fileName) + ".png");
        return reference == null ? -2147483648.0D : new EvaluateSpatialDistortion().getScore(segmented, reference);
    }

    private static void saveToFile(Batch.Batchable batchable, ImageProcessor ip, int size, String name) {
        ImageProcessor resized = Tools.resize(ip, size);
        if (batchable != null) {
            batchable.run(resized);
        }

        Tools.saveToFile(Tools.maskBackground(resized, Color.blue), name);
    }
}