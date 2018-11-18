package evaluation;

import basics.Tools;
import ij.process.ImageProcessor;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Vector;

public class Evaluation
{
    private ImageTest imageTest;
    private Vector<Double> scores = new Vector();
    ImageProcessor imageToEvaluate;

    public Evaluation()
    {
    }

    public Evaluation(ImageTest imageTest)
    {
        this.imageTest = imageTest;
    }

    public void run(String fileNameToEvaluate, String referenceFileName) throws Exception {
        this.imageTest.setup(fileNameToEvaluate, referenceFileName);

        this.scores.add(Double.valueOf(this.imageTest.getScore()));
    }

    public void run(ImageProcessor imageToEvaluate, ImageProcessor referenceMask) throws Exception
    {
        if ((imageToEvaluate == null) || (referenceMask == null)) {
            System.out.println("Nothing to evaluate.");
            return;
        }

        ImageProcessor maskToEvaluate = imageToEvaluate.getMask() == null ? imageToEvaluate : imageToEvaluate.getMask();

        this.imageTest.setup(maskToEvaluate, referenceMask);

        this.scores.add(Double.valueOf(this.imageTest.getScore()));
    }

    public double getSum() {
        double sum = 0.0D;
        for (Double score : this.scores) {
            sum += score.doubleValue();
        }
        return sum;
    }

    public double getMin() {
        if (this.scores.size() <= 1) return 0.0D;
        return ((Double)Collections.min(this.scores)).doubleValue();
    }

    public double getMax() {
        if (this.scores.size() <= 1) return 0.0D;

        return ((Double)Collections.max(this.scores)).doubleValue();
    }

    public double getMean() {
        if (this.scores.size() <= 1) return 0.0D;

        int scoresize = this.scores.size();
        double sum = getSum();
        return sum / scoresize;
    }

    public double getStandardDeviation() {
        if (this.scores.size() <= 1) return 0.0D;

        double mean = getMean();

        double sum = 0.0D;
        for (Double score : this.scores) {
            sum += Math.pow(score.doubleValue() - mean, 2.0D);
        }

        return Math.sqrt(sum / (this.scores.size() - 1));
    }

    public double getLastScore() {
        return ((Double)this.scores.get(this.scores.size() - 1)).doubleValue();
    }

    public static void printTitle() {
        System.out.println("last\tmean\tmin\tmax\tsd");
    }

    public String[] getStringArray() {
        String[] str = new String[4];

        str[0] = Tools.formatNumber(getMean());
        str[1] = Tools.formatNumber(getMin());
        str[2] = Tools.formatNumber(getMax());
        str[3] = Tools.formatNumber(getStandardDeviation());

        return str;
    }

    public String toString()
    {
        String str = "";
        if (this.scores.size() > 0) {
            str = str + Tools.formatNumber(getLastScore()) + " \t";
            str = str + Tools.formatNumber(getMean()) + " \t";
            str = str + Tools.formatNumber(getMin()) + " \t";
            str = str + Tools.formatNumber(getMax()) + " \t";
            str = str + Tools.formatNumber(getStandardDeviation());
            str = str + " (" + this.imageTest.toString() + ")";
        }
        return str;
    }

    public void printStatisticSummary()
    {
        System.out.println("\n\n" + this.imageTest.getClass().getName());
        for (Double score : this.scores)
            System.out.println(score);
    }

    public void startStopWatch()
    {
    }

    public static boolean test() {
        Evaluation evaluation = new Evaluation();
        evaluation.scores.add(new Double(1.0D));
        evaluation.scores.add(new Double(-1.0D));
        return evaluation.getStandardDeviation() == 1.414213562373095D;
    }
}