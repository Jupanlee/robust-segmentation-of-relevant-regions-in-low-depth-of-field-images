package deviationScoreRegions;

import basics.Tools;
import basics.javaAddons.DEBUG;
import evaluation.Batch.Batchable;
import evaluation.MThread;
import evaluation.SegmentationCases;
import evaluation.fuzzy.FuzzySegmentationBatch;
import ij.process.ImageProcessor;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import others.fuzzy.filters.fuzzy.Parameters;
import others.morphological.MorphologicalOOIExtraction;
import others.sirithana.VideoSegmentation;

public class Evaluate extends MThread
{
    private Batch.Batchable batchable;
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

    public Evaluate(String description, Batch.Batchable batchable, String directory, int size, String logFileName)
    {
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

    public static void testDSR_Modular(String folder)
    {
        int size = 600;
        String description = "testDSR_Modular";
        new Evaluate(description, new FuzzySegmentationBatch(), folder, size, "./results_" + description + ".txt").start();
    }

    public static void testFuzzyParameters(String folder)
    {
        String description = "testFuyyzParameters";
        int size = 300;

        int count = 0;
        for (int spatial : new int[] { 14, 15, 16, 17, 18 })
            for (float threshold : new float[] { 0.2F, 0.1666667F, 0.1428572F, 0.125F, 0.1111111F })
                for (int range : new int[] { 16, 17, 18, 19, 20 })
                    for (int minsize : new int[] { 60, 70, 75, 80, 85, 90 }) {
                        count++;
                        FuzzySegmentationBatch.parameters = new Parameters(threshold, spatial, range, minsize);
                        new Evaluate(description, new FuzzySegmentationBatch(), folder, size, "./results_" + description + count + ".txt").start();
                    }
    }

    public void run()
    {
        Tools.deleteFile(this.logFileName);

        List fileNames = Tools.getAllFilesFromDirectoryWithSubfolders(this.directory, ".jpg");
        this.results = new int[fileNames.size()][5];

        for (int i = 0; i < fileNames.size(); i++) {
            String fileName = (String)fileNames.get(i);
            try
            {
                resetTime();
                ImageProcessor imageProcessor = Tools.loadImageProcessor(fileName, this.size);
                this.batchable.run(imageProcessor);
                ImageProcessor mask = imageProcessor.getMask();
                String maskFileName = this.directory + "/../masks/" + Tools.getNameOnly(fileName) + ".png";

                ImageProcessor reference = Tools.loadImageProcessor(maskFileName);
                if ((reference != null) && (mask != null)) {
                    SegmentationCases sc = new SegmentationCases(mask, reference);

                    this.results[i][0] = sc.getTruePositives();
                    this.results[i][1] = sc.getTrueNegatives();
                    this.results[i][2] = sc.getFalsePositives();
                    this.results[i][3] = sc.getFalseNegatives();
                    this.results[i][4] = (int)getCPUTime();

                    String text = this.batchable + "\t" + Tools.getFileNameWithoutDirectory(fileName) + "\t" + this.size + "\t" + sc + "\t" + getCPUTime() + "\n";
                    Tools.appendToFile(this.logFileName, text);
                }

            }
            catch (IOException ex)
            {
                Logger.getLogger(Evaluate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String text = this.description + "\t" + this.batchable + "\t" + this.size + "\t" + Tools.getMean(spatialDistortionValues()) + "\t" + Collections.min(spatialDistortionValues()) + "\t" + Collections.max(spatialDistortionValues()) + "\t" + Tools.getStandardDeviation(spatialDistortionValues()) + "\t" + Tools.getMedian(spatialDistortionValues()) + "\t" + getMeanRuntime();

        System.out.println(text);
        try {
            writeToLog(text);
        } catch (IOException ex) {
            Logger.getLogger(Evaluate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int getMeanRuntime() {
        int sum = 0;
        for (int i = 0; i < this.results.length; i++) {
            sum += this.results[i][4];
        }
        return sum / this.results.length;
    }

    private double getSD(int tp, int fp, int fn)
    {
        return Math.min(1.0D, (fp + fn) / (tp + fn));
    }

    private Collection<Double> spatialDistortionValues()
    {
        Collection sdValues = new LinkedList();

        for (int i = 0; i < this.results.length; i++) {
            sdValues.add(Double.valueOf(getSD(this.results[i][0], this.results[i][2], this.results[i][3])));
        }
        return sdValues;
    }

    public static void testMaskApproximationSize(String folder) {
        int size = 800;
        String description = "DSR_MaskApproxSize_";
        for (int parameter = 100; parameter <= 800; parameter += 100) {
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.maskApproximationSize = parameter;
            new Evaluate(description, dsr, folder, size, "./results_" + description + parameter + ".txt").start();
        }
    }

    public static void testVSOthers(String folder, int size)
    {
        String description = "DSR_VsOthers_";

        for (Batch.Batchable batchable : new Batch.Batchable[] { new DeviationScoreRegions_ParameterReduced(), new FuzzySegmentationBatch(), new MorphologicalOOIExtraction(), new VideoSegmentation() })
            new Evaluate(description, batchable, folder, size, "./results_" + description + batchable.getClass().getName() + ".txt").start();
    }

    public static void testConvexHullLinking(String folder)
    {
        int size = 800;
        String description = "DSR_ConvexHullLinking_";

        for (boolean parameter : new boolean[] { true, false }) {
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.convexHullLinking = parameter;
            new Evaluate(description, dsr, folder, size, "./results_" + description + parameter + ".txt").start();
        }
    }

    public static void testTethaScore(String folder) {
        int size = 800;
        String description = "DSR_TethaScore_";
        for (int parameter = 0; parameter <= 150; parameter += 10) {
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.tetha_score = parameter;
            new Evaluate(description, dsr, folder, size, "./results_" + description + Tools.formatNumber(parameter) + ".txt").start();
        }
    }

    public static void testTethaRel(String folder) {
        int size = 800;
        String description = "DSR_TethaRel_";

        for (double parameter = 0.625D; parameter <= 1.0D; parameter += 0.025D) {
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.tetha_rel = parameter;
            new Evaluate(description, dsr, folder, size, "./results_" + description + Tools.formatNumber(parameter) + ".txt").start();
        }
    }

    public static void testTethaDist(String folder)
    {
        int size = 800;
        String description = "DSR_TethaDist_";

        for (int parameter = 0; parameter <= 100; parameter += 10) {
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.tetha_dist = parameter;
            new Evaluate(description, dsr, folder, size, "./results_" + description + Tools.formatNumber(parameter) + ".txt").start();
        }
    }

    public static void testTethaRec(String folder) {
        int size = 800;
        String description = "DSR_TethaRec_";

        for (double parameter = 0.0D; parameter <= 1.0D; parameter += 0.1D) {
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.tetha_rec = parameter;
            new Evaluate(description, dsr, folder, size, "./results_" + description + Tools.formatNumber(parameter) + ".txt").start();
        }
    }

    public static void testTethaEpsilon(String folder)
    {
        int size = 800;
        String description = "DSR_TethaEpsilon_";
        for (double parameterValue = 0.01D; parameterValue <= 0.06D; parameterValue += 0.005D) {
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.tetha_epsilon = parameterValue;
            new Evaluate(description, dsr, folder, size, "./results_" + description + parameterValue + ".txt").start();
        }
    }

    public static void testSigmaBlur(String folder) {
        int size = 800;
        String description = "DSR_sigmaBlur_";
        for (double parameter = 0.775D; parameter <= 1.25D; parameter += 0.025D) {
            DeviationScoreRegions_ParameterReduced dsr = new DeviationScoreRegions_ParameterReduced();
            dsr.sigmaBlur = parameter;
            new Evaluate(description, dsr, folder, size, "./results_" + description + Tools.formatNumber(parameter, "0.000") + ".txt").start();
        }
    }

    public static void testSizesOnFlickrCC(String folder)
    {
        DEBUG.setVerbose(false);
        for (int size = 100; size <= 1100; size += 100)
            new Evaluate("size test", new DeviationScoreRegions_ParameterReduced(), folder, size, "./results_DSR_size" + size + ".txt").start();
    }

    // ERROR //
    public static void testBigSizes()
    {
        // Byte code:
        //   0: sipush 1000
        //   3: istore_0
        //   4: iload_0
        //   5: sipush 1600
        //   8: if_icmpgt +58 -> 66
        //   11: new 18	deviationScoreRegions/Evaluate
        //   14: dup
        //   15: ldc 137
        //   17: new 91	deviationScoreRegions/DeviationScoreRegions_ParameterReduced
        //   20: dup
        //   21: invokespecial 92	deviationScoreRegions/DeviationScoreRegions_ParameterReduced:<init>	()V
        //   24: ldc 138
        //   26: iload_0
        //   27: new 21	java/lang/StringBuilder
        //   30: dup
        //   31: invokespecial 22	java/lang/StringBuilder:<init>	()V
        //   34: ldc 136
        //   36: invokevirtual 24	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   39: iload_0
        //   40: invokevirtual 38	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //   43: ldc 25
        //   45: invokevirtual 24	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   48: invokevirtual 26	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   51: invokespecial 27	deviationScoreRegions/Evaluate:<init>	(Ljava/lang/String;Levaluation/Batch$Batchable;Ljava/lang/String;ILjava/lang/String;)V
        //   54: invokevirtual 28	deviationScoreRegions/Evaluate:start	()V
        //   57: wide
        //   63: goto -59 -> 4
        //   66: return
    }
}