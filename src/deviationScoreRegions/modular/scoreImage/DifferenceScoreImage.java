//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions.modular.scoreImage;

import basics.MImage;
import basics.Tools;
import deviationScoreRegions.modular.edgeFilters.OldScoreLabDifference;
import deviationScoreRegions.scoring.ScoreDeviation;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class DifferenceScoreImage extends ScoreImage {
    private final double sigmaBlur;
    private final double sigmaPreBlur;
    private final int iterations;
    private final int radius;
    private final int maxSize;
    public static boolean deviation = false;

    public int getPixelCount() {
        return this.scoreImageProcessor.getPixelCount();
    }

    public int getWidth() {
        return this.scoreImageProcessor.getWidth();
    }

    public int getHeight() {
        return this.scoreImageProcessor.getHeight();
    }

    public DifferenceScoreImage() {
        this(new DifferenceScoreImage.DifferenceScoreImageParameter());
    }

    public DifferenceScoreImage(DifferenceScoreImage.DifferenceScoreImageParameter scoreImageParameter) {
        this.sigmaBlur = scoreImageParameter.getSigmaBlur();
        this.iterations = scoreImageParameter.getIterations();
        this.radius = scoreImageParameter.getRadius();
        this.maxSize = scoreImageParameter.getMaxSize();
        this.sigmaPreBlur = scoreImageParameter.getSigmaPreBlur();
    }

    private ImageProcessor getEdges(ImageProcessor ip) {
        ImageProcessor edges = new ByteProcessor(ip.getWidth(), ip.getHeight());
        MImage mImage = new MImage(ip);

        for(int x = 0; x < ip.getWidth(); ++x) {
            for(int y = 0; y < ip.getHeight(); ++y) {
                double deltaE = Tools.euklidDistance(mImage.getLab(x, y), mImage.getNeigbourMeanLab(x, y, this.radius, true));
                int value = (int)Math.pow(deltaE, 2.0D);
                edges.putPixel(x, y, value);
            }
        }

        return edges;
    }

    public static void main(String[] args) throws IOException {
        int maxSize = 400;
        List<String> fileNames = Tools.getFilesFromDirectory("../../images/schwierig", ".jpg");
        Collections.shuffle(fileNames, new Random(0L));
        Iterator i$ = fileNames.iterator();

        while(i$.hasNext()) {
            String fileName = (String)i$.next();
            ImageProcessor imageProcessor = Tools.loadImageProcessor(fileName);
            if (Tools.getLongestSide(imageProcessor) > maxSize) {
                Tools.resize(imageProcessor, maxSize);
            }
        }

    }

    public ImageProcessor generateScore(ImageProcessor original) {
        if (deviation) {
            this.scoreImageProcessor = (new ScoreDeviation(new OldScoreLabDifference(), 0.95D, 10, 2.0D, 1.0D)).run(original);
        } else {
            ImageProcessor preBlurred = Tools.blur(original, this.sigmaPreBlur, this.iterations);
            ImageProcessor difference = Tools.difference(this.getEdges(preBlurred), this.getEdges(Tools.blur(preBlurred, this.sigmaBlur)));
            this.scoreImageProcessor = new ShortProcessor(original.getWidth(), original.getHeight());
            this.scoreImageProcessor.copyBits(Tools.power(difference, 2.0D), 0, 0, 0);
        }

        if (Tools.getLongestSide(this.scoreImageProcessor) > this.maxSize) {
            this.scoreImageProcessor = Tools.resize(this.scoreImageProcessor, this.maxSize);
        }

        return this.scoreImageProcessor;
    }

    public static class DifferenceScoreImageParameter {
        private double sigmaBlur = 0.9D;
        private int iterations = 1;
        private int radius = 1;
        private int maxSize = 400;
        private double sigmaPreBlur;

        public DifferenceScoreImageParameter() {
            this.sigmaPreBlur = this.sigmaBlur;
        }

        public double getSigmaPreBlur() {
            return this.sigmaPreBlur;
        }

        public void setSigmaPreBlur(double sigmaPreBlur) {
            this.sigmaPreBlur = sigmaPreBlur;
        }

        public int getMaxSize() {
            return this.maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public int getRadius() {
            return this.radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public int getIterations() {
            return this.iterations;
        }

        public void setIterations(int iterations) {
            this.iterations = iterations;
        }

        public double getSigmaBlur() {
            return this.sigmaBlur;
        }

        public void setSigmaBlur(double sigmaBlur) {
            this.sigmaBlur = sigmaBlur;
        }
    }
}
