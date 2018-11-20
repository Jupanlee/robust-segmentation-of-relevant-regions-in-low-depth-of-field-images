package evaluation;

import basics.Tools;
import ij.process.ImageProcessor;
import java.io.PrintStream;

public abstract class ImageTest
{
    protected Segments segEvaluate;
    protected Segments segReference;
    private double score = -1.0D;

    abstract double calculateScore();

    public double getScore() { if (this.score == -1.0D) this.score = calculateScore();
        return this.score; }

    public double getScore(ImageProcessor evaluateImageProcessor, ImageProcessor referenceImageProcessor) throws Exception
    {
        setup(evaluateImageProcessor, referenceImageProcessor);
        return getScore();
    }

    abstract String getTestDescription();

    protected int getCount(Method m)
    {
        int count = 0;

        for (int x = 0; x < this.segEvaluate.getWidth(); x++) {
            for (int y = 0; y < this.segEvaluate.getHeight(); y++)
            {
                Segment sC = this.segEvaluate.getSegment(x, y);
                Segment sH = this.segReference.getSegment(x, y);

                switch (m.ordinal()) {
                case 1:
                    if ((sC == Segment.background) || (sH == Segment.background)) continue;
                    count++; break;
                case 2:
                    if ((sC != Segment.background) || (sH != Segment.background)) continue;
                    count++; break;
                case 3:
                    if ((sC == Segment.background) || (sH != Segment.background)) continue;
                    count++; break;
                case 4:
                    if ((sC != Segment.background) || (sH == Segment.background)) continue;
                    count++;
            }
            }
        }

        return count;
    }

    public void setup(ImageProcessor evaluateImageProcessor, ImageProcessor referenceImageProcessor) throws Exception
    {
        this.score = -1.0D;

        int smallestWidth = Math.min(evaluateImageProcessor.getWidth(), referenceImageProcessor.getWidth());
        int smallestHeight = Math.min(evaluateImageProcessor.getHeight(), referenceImageProcessor.getHeight());

        evaluateImageProcessor = evaluateImageProcessor.resize(smallestWidth, smallestHeight);
        referenceImageProcessor = referenceImageProcessor.resize(smallestWidth, smallestHeight);

        this.segEvaluate = new Segments(evaluateImageProcessor);
        this.segReference = new Segments(referenceImageProcessor);

        if ((this.segEvaluate.getHeight() != this.segReference.getHeight()) || (this.segEvaluate.getWidth() != this.segReference.getWidth())) {
            throw new Exception("segmente nicht gleich gross!");
        }

        ImageProcessor errors = Tools.difference(evaluateImageProcessor, referenceImageProcessor);
        errors = Tools.write(getTestDescription() + getScore(), errors);
        Tools.save(errors);
    }

    public void setup(String fileNameMaskToEvaluate, String fileNameMaskReference) throws Exception
    {
        ImageProcessor evaluateImageProcessor = Tools.loadImageProcessor(fileNameMaskToEvaluate);
        ImageProcessor referenceImageProcessor = Tools.loadImageProcessor(fileNameMaskReference);

        setup(evaluateImageProcessor, referenceImageProcessor);
    }

    public int getTruePositives() {
        return getCount(Method.truePositive);
    }

    public int getTrueNegatives() {
        return getCount(Method.trueNegative);
    }

    public int getFalseNegatives() {
        return getCount(Method.falseNegative);
    }

    public int getFalsePositives() {
        return getCount(Method.falsePositive);
    }

    public String toString()
    {
        return getTestDescription() + " = " + getScore();
    }

    public String getStatisticData() {
        String statisticData = "";
        statisticData = statisticData + "false Positives = " + getFalsePositives() + "\n";
        statisticData = statisticData + "false Negatives = " + getFalseNegatives() + "\n";
        statisticData = statisticData + "true Positives = " + getTruePositives() + "\n";
        statisticData = statisticData + "true Negatives = " + getTrueNegatives();
        statisticData = statisticData + "=> Score = " + getScore();

        return statisticData;
    }

    public static void main(String[] args)
            throws Exception
    {
        ImageTest[] imageTests = { new EvaluateSpatialDistortion(), new EvaluateRelevancy(), new EvaluateSensitivity(), new EvaluateSpecificity() };

        for (ImageTest imageTest : imageTests) {
            imageTest.setup("data/tmp/computerMask.png", "data/tmp/humanMask.png");
            System.out.println(imageTest);
        }
        System.out.println("\n" + imageTests[0].getStatisticData());
    }

    protected static enum Method
    {
        truePositive, trueNegative, falsePositive, falseNegative;
    }
}