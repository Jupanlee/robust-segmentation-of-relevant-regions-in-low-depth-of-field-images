//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions;

import basics.Tools;
import basics.javaAddons.DEBUG;
import evaluation.MThread;
import evaluation.SegmentationCases;
import evaluation.Batch.Batchable;
import evaluation.fuzzy.FuzzySegmentationBatch;
import ij.process.ImageProcessor;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import others.fuzzy.filters.fuzzy.Parameters;
import others.morphological.MorphologicalOOIExtraction;
import others.sirithana.VideoSegmentation;

public class Evaluate extends MThread {
    private Batchable batchable;
    private String directory;
    private int size;
    String logFileName;
    static final int SIZE = 600;
    final int nrOfDifferentResultTypes = 5;
    final int TP = 0;
    final int TN = 1;
    final int FP = 2;
    final int FN = 3;
    final int RUNTIME = 4;
    int[][] results;
    String description;

    public Evaluate(String description, Batchable batchable, String directory, int size, String logFileName) {
        this.batchable = batchable;
        this.directory = directory;
        this.size = size;
        this.logFileName = logFileName;
        this.description = description;
    }

    public static void main(String[] args) throws IOException {
        DEBUG.setVerbose(false);
        MThread.setNumberOfThreads(12);
        String folder = "/tmp/weiler_tmp/flickr_cc";
        testDSR_Modular(folder);
    }

    public static void testDSR_Modular(String folder) {
        int size = 600;
        String description = "testDSR_Modular";
        (new Evaluate(description, new FuzzySegmentationBatch(), folder, size, "./results_" + description + ".txt")).start();
    }

    public static void testFuzzyParameters(String folder) {
        String description = "testFuyyzParameters";
        int size = 300;
        int count = 0;
        int[] arr1 = new int[]{14, 15, 16, 17, 18};
        int len1 = arr1.length;

        for(int i = 0; i < len1; ++i) {
            int spatial = arr1[i];
            float[] arr2 = new float[]{0.2F, 0.16666667F, 0.14285715F, 0.125F, 0.11111111F};
            int len2 = arr2.length;

            for(int j = 0; j < len2; ++j) {
                float threshold = arr2[j];
                int[] arr3 = new int[]{16, 17, 18, 19, 20};
                int len3 = arr3.length;

                for(int k = 0; k < len3; ++k) {
                    int range = arr3[k];
                    int[] arr4 = new int[]{60, 70, 75, 80, 85, 90};
                    int len4 = arr4.length;

                    for(int t = 0; t < len4; ++t) {
                        int minsize = arr4[t];
                        ++count;
                        FuzzySegmentationBatch.parameters = new Parameters(threshold, spatial, range, minsize);
                        (new Evaluate(description, new FuzzySegmentationBatch(), folder, size, "./results_" + description + count + ".txt")).start();
                    }
                }
            }
        }

    }

    public void run() {
        Tools.deleteFile(this.logFileName);
        List<String> fileNames = Tools.getAllFilesFromDirectoryWithSubfolders(this.directory, ".jpg");
        this.results = new int[fileNames.size()][5];

        for(int i = 0; i < fileNames.size(); ++i) {
            String fileName = (String)fileNames.get(i);

            try {
                this.resetTime();
                ImageProcessor imageProcessor = Tools.loadImageProcessor(fileName, this.size);
                this.batchable.run(imageProcessor);
                ImageProcessor mask = imageProcessor.getMask();
                String maskFileName = this.directory + "/../masks/" + Tools.getNameOnly(fileName) + ".png";
                ImageProcessor reference = Tools.loadImageProcessor(maskFileName);
                if (reference != null && mask != null) {
                    SegmentationCases sc = new SegmentationCases(mask, reference);
                    this.results[i][0] = sc.getTruePositives();
                    this.results[i][1] = sc.getTrueNegatives();
                    this.results[i][2] = sc.getFalsePositives();
                    this.results[i][3] = sc.getFalseNegatives();
                    this.results[i][4] = (int)this.getCPUTime();
                    String text = this.batchable + "\t" + Tools.getFileNameWithoutDirectory(fileName) + "\t" + this.size + "\t" + sc + "\t" + this.getCPUTime() + "\n";
                    Tools.appendToFile(this.logFileName, text);
                }
            } catch (IOException var11) {
                Logger.getLogger(Evaluate.class.getName()).log(Level.SEVERE, (String)null, var11);
            }
        }

        String text = this.description + "\t" + this.batchable + "\t" + this.size + "\t" + Tools.getMean(this.spatialDistortionValues()) + "\t" + Collections.min(this.spatialDistortionValues()) + "\t" + Collections.max(this.spatialDistortionValues()) + "\t" + Tools.getStandardDeviation(this.spatialDistortionValues()) + "\t" + Tools.getMedian(this.spatialDistortionValues()) + "\t" + this.getMeanRuntime();
        System.out.println(text);

        try {
            this.writeToLog(text);
        } catch (IOException var10) {
            Logger.getLogger(Evaluate.class.getName()).log(Level.SEVERE, (String)null, var10);
        }

    }

    private int getMeanRuntime() {
        int sum = 0;

        for(int i = 0; i < this.results.length; ++i) {
            sum += this.results[i][4];
        }

        return sum / this.results.length;
    }

    private double getSD(int tp, int fp, int fn) {
        return Math.min(1.0D, (double)(fp + fn) / (double)(tp + fn));
    }

    private Collection<Double> spatialDistortionValues() {
        Collection<Double> sdValues = new LinkedList();

        for(int i = 0; i < this.results.length; ++i) {
            sdValues.add(this.getSD(this.results[i][0], this.results[i][2], this.results[i][3]));
        }

        return sdValues;
    }

    public static void testMaskApproximationSize(String folder) {
        int size = 800;
        String description = "DSR_MaskApproxSize_";

        for(int parameter = 100; parameter <= 800; parameter += 100) {
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.maskApproximationSize = parameter;
            (new Evaluate(description, dsr, folder, size, "./results_" + description + parameter + ".txt")).start();
        }

    }

    public static void testVSOthers(String folder, int size) {
        String description = "DSR_VsOthers_";
        Batchable[] arr$ = new Batchable[]{new DeviationScoreRegions_ParameterReduced(), new FuzzySegmentationBatch(), new MorphologicalOOIExtraction(), new VideoSegmentation()};
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Batchable batchable = arr$[i$];
            (new Evaluate(description, batchable, folder, size, "./results_" + description + batchable.getClass().getName() + ".txt")).start();
        }

    }

    public static void testConvexHullLinking(String folder) {
        int size = 800;
        String description = "DSR_ConvexHullLinking_";
        boolean[] arr$ = new boolean[]{true, false};
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            boolean parameter = arr$[i$];
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.convexHullLinking = parameter;
            (new Evaluate(description, dsr, folder, size, "./results_" + description + parameter + ".txt")).start();
        }

    }

    public static void testTethaScore(String folder) {
        int size = 800;
        String description = "DSR_TethaScore_";

        for(int parameter = 0; parameter <= 150; parameter += 10) {
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.tetha_score = parameter;
            (new Evaluate(description, dsr, folder, size, "./results_" + description + Tools.formatNumber((double)parameter) + ".txt")).start();
        }

    }

    public static void testTethaRel(String folder) {
        int size = 800;
        String description = "DSR_TethaRel_";

        for(double parameter = 0.625D; parameter <= 1.0D; parameter += 0.025D) {
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.tetha_rel = parameter;
            (new Evaluate(description, dsr, folder, size, "./results_" + description + Tools.formatNumber(parameter) + ".txt")).start();
        }

    }

    public static void testTethaDist(String folder) {
        int size = 800;
        String description = "DSR_TethaDist_";

        for(int parameter = 0; parameter <= 100; parameter += 10) {
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.tetha_dist = (double)parameter;
            (new Evaluate(description, dsr, folder, size, "./results_" + description + Tools.formatNumber((double)parameter) + ".txt")).start();
        }

    }

    public static void testTethaRec(String folder) {
        int size = 800;
        String description = "DSR_TethaRec_";

        for(double parameter = 0.0D; parameter <= 1.0D; parameter += 0.1D) {
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.tetha_rec = parameter;
            (new Evaluate(description, dsr, folder, size, "./results_" + description + Tools.formatNumber(parameter) + ".txt")).start();
        }

    }

    public static void testTethaEpsilon(String folder) {
        int size = 800;
        String description = "DSR_TethaEpsilon_";

        for(double parameterValue = 0.01D; parameterValue <= 0.06D; parameterValue += 0.005D) {
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.tetha_epsilon = parameterValue;
            (new Evaluate(description, dsr, folder, size, "./results_" + description + parameterValue + ".txt")).start();
        }

    }

    public static void testSigmaBlur(String folder) {
        int size = 800;
        String description = "DSR_sigmaBlur_";

        for(double parameter = 0.775D; parameter <= 1.25D; parameter += 0.025D) {
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.sigmaBlur = parameter;
            (new Evaluate(description, dsr, folder, size, "./results_" + description + Tools.formatNumber(parameter, "0.000") + ".txt")).start();
        }

    }

    public static void testSizesOnFlickrCC(String folder) {
        DEBUG.setVerbose(false);

        for(int size = 100; size <= 1100; size += 100) {
            (new Evaluate("size test", new DeviationScoreRegions_ParameterReduced(), folder, size, "./results_DSR_size" + size + ".txt")).start();
        }

    }

    public static void testBigSizes() {
        for(int size = 1000; size <= 1600; size += 150) {
            (new Evaluate("testBigSizes", new DeviationScoreRegions_ParameterReduced(), "/tmp/weiler_tmp/big", size, "./results_DSR_size" + size + ".txt")).start();
        }

    }
}
