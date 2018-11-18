package deviationScoreRegions.scoring;

import basics.Tools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.modular.edgeFilters.OldScoreLabDifference;
import evaluation.Batch.Batchable;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ScoreDeviation
        implements Batch.Batchable
{
    private double GAUSSBLURRADIUS = 0.85D;
    private int REFACTORITERATIONS = 10;
    private double edgeImagePower = 1.0D;
    private double finalPower = 2.5D;
    private Batch.Batchable edgeFilter;

    public ScoreDeviation(Batch.Batchable score)
    {
        this.edgeFilter = score;
    }

    public ScoreDeviation(Batch.Batchable edgeFilter, double gaussBlurRadius, int refactorations, double finalPower, double edgeImagePower) {
        this.edgeFilter = edgeFilter;
        this.GAUSSBLURRADIUS = gaussBlurRadius;
        this.REFACTORITERATIONS = refactorations;
        this.finalPower = finalPower;
        this.edgeImagePower = edgeImagePower;
    }

    public ImageProcessor run(ImageProcessor imageProcessor) {
        ImageProcessor debugFirstBlurrImage = null;

        List edgeImages = new ArrayList();

        ImageProcessor refactured = imageProcessor.duplicate();

        for (int i = 0; i <= this.REFACTORITERATIONS; i++) {
            ImageProcessor edgeImage = this.edgeFilter.run(refactured);
            edgeImage = Tools.power(edgeImage, this.edgeImagePower);
            edgeImages.add(edgeImage);
            if (DEBUG.getVerbose()) {
                Tools.showImage("debug", edgeImage, "iteration == " + i, false);
            }

            if (i == 0) {
                debugFirstBlurrImage = edgeImage.duplicate();
            }

            refactured = Tools.blur(refactured, this.GAUSSBLURRADIUS);
        }

        double[][] scores = new double[imageProcessor.getWidth()][imageProcessor.getHeight()];
        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++)
            {
                Vector scoresAtCurrentXY = new Vector();
                for (ImageProcessor edgeImage : edgeImages) {
                    scoresAtCurrentXY.add(new Double(edgeImage.getPixel(x, y)));
                }

                scores[x][y] = Tools.getStandardDeviation(scoresAtCurrentXY);
            }
        }

        Tools.save(Tools.write("std. dev. of old scoreDeviation", Tools.createImageProcessorFromArray(scores)));

        ImageProcessor standardDeviationImage = new ByteProcessor(((ImageProcessor)edgeImages.get(0)).getWidth(), ((ImageProcessor)edgeImages.get(0)).getHeight());
        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++)
            {
                Vector neighbourScores = new Vector();
                for (Point neighbour : Tools.get8Neighbourhood(new Point(x, y), imageProcessor.getWidth(), imageProcessor.getHeight())) {
                    neighbourScores.add(Double.valueOf(scores[neighbour.x][neighbour.y]));
                }
                standardDeviationImage.putPixelValue(x, y, Math.pow(Tools.getMeanDouble(neighbourScores), this.finalPower));
            }

        }

        return standardDeviationImage;
    }

    public static void main(String[] args) throws IOException {
        for (String fileName : Tools.getFilesFromDirectory("data/batch/images/base", ".jpg")) {
            ImageProcessor original = Tools.resize(Tools.loadImageProcessor(fileName), 500);

            double blur = 0.8D;
            int blurIterations = 10;
            double labDifferencePower = 1.0D;
            double finalPower = 2.0D;

            Tools.save(new OldScoreLabDifference().run(original));
            Tools.save(new ScoreBlurDifference().run(original));

            Tools.save(new ScoreDeviation(new OldScoreLabDifference(), 1.0D, 10, 2.0D, 1.0D).run(original));
            Tools.save(new ScoreDeviation(new ScoreBlurDifference(), blur, blurIterations, finalPower, labDifferencePower).run(original));
        }
    }
}