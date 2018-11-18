//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions.modular.scoreImage;

import basics.Tools;
import deviationScoreRegions.grow.ColorRegion;
import deviationScoreRegions.grow.ColorRegionTools;
import deviationScoreRegions.modular.Fill;
import ij.process.ImageProcessor;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScoreImageFeatures {
    private final ScoreImage scoreImage;
    private final double relClosingSize = 0.025D;
    private final double relOpeningSize = 0.0D;
    private final int morphThreshold = 64;
    private final ImageProcessor filledMorphImage;
    private final ImageProcessor morphImage;
    private final List<ColorRegion> regions;

    public ScoreImageFeatures(ScoreImage scoreImage) {
        this.scoreImage = scoreImage;
        this.morphImage = this.calcMorphImage();
        this.filledMorphImage = (new Fill()).run(this.morphImage);
        this.regions = ColorRegionTools.getRegionsFromImageProcessor(this.filledMorphImage, 20.0D, (ImageProcessor)null, 0);
    }

    private ImageProcessor calcMorphImage() {
        int scoreImagePixels = this.scoreImage.getImageProcessor().getPixelCount();
        double breite = Math.sqrt((double)scoreImagePixels);
        int closingSize = (int)(breite * 0.025D);
        int openingSize = (int)(breite * 0.0D);
        ImageProcessor result = Tools.close(this.scoreImage.getImageProcessor(), closingSize, true);
        result = Tools.open(result, openingSize);
        result.threshold(64);
        return result;
    }

    public double closedCovering() {
        int whitePixelFromClosedScoreImage = Tools.getPixelCount(this.filledMorphImage, 1.0D, 1.7976931348623157E308D);
        double result = (double)whitePixelFromClosedScoreImage / (double)this.scoreImage.getImageProcessor().getPixelCount();
        Tools.save(Tools.write("closedCovering = " + Tools.formatNumber(result), this.filledMorphImage));
        return result;
    }

    private int getSum(List<ColorRegion> colorRegions, int start, int endExcluded) {
        int sum = 0;

        for(int i = start; i < endExcluded; ++i) {
            sum += ((ColorRegion)colorRegions.get(i)).size();
        }

        return sum;
    }

    public int getMorphedRegionNr() {
        return this.regions.size();
    }

    public double getCearness(int largestRegions) {
        Collections.sort(this.regions, new ScoreImageFeatures.ColorRegionComparator());
        int largestRegionsPixel = this.getSum(this.regions, 0, largestRegions);
        int smallerRegionsPixel = this.getSum(this.regions, largestRegions, Math.min(largestRegions * 2, this.regions.size()));
        Tools.save(Tools.write("clearness = " + Tools.formatNumber((double)smallerRegionsPixel / (double)largestRegionsPixel), this.filledMorphImage));
        return (double)smallerRegionsPixel / (double)largestRegionsPixel;
    }

    public double getInstability() {
        int size = (int)(Math.sqrt((double)this.scoreImage.getImageProcessor().getPixelCount()) * 0.01D);
        ImageProcessor closed = Tools.close(this.scoreImage.getImageProcessor(), size);
        ImageProcessor filledClosed = (new Fill()).run(closed);
        ImageProcessor difference = Tools.difference(closed, filledClosed);
        double changedPixel = (double)Tools.getPixelCount(difference, 1.0D, 1.7976931348623157E308D);
        double whiteMaskPixel = (double)Tools.getPixelCount(this.morphImage, 1.0D, 1.7976931348623157E308D);
        double instability = changedPixel / (whiteMaskPixel + 1.0D);
        Tools.save(Tools.write("getInstability = " + Tools.formatNumber(instability), difference));
        return instability;
    }

    private class ColorRegionComparator implements Comparator<ColorRegion> {
        private ColorRegionComparator() {
        }

        public int compare(ColorRegion o1, ColorRegion o2) {
            return o2.size() - o1.size();
        }
    }
}
