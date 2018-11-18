//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions;

import basics.MImage;
import basics.ProgressListener;
import basics.Tools;
import basics.convexHull.ConvexHullTools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.GUI.DebugInfoShower;
import deviationScoreRegions.dbscan.DbscanTools;
import deviationScoreRegions.grow.ColorRegion;
import deviationScoreRegions.grow.ColorRegionTools;
import evaluation.Batch;
import evaluation.Batch.Batchable;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import others.morphological.Morphological;

public class DeviationScoreRegions_Tuned2 implements Batchable {
    double preBlur = 1.25D;
    int preBlurIterations = 3;
    double blur;
    int scoreImageThreshold;
    double scoreClusteringThreshold;
    static double labDifferencePower = 2.0D;
    double finalPower;
    double epsilonPercentage;
    double minPtsScorePointDensityMultiplier;
    double mainClusterSize;
    double convexHullLinkingEpsilonPercentage;
    int morphImageSize;
    int approxMapDilateSize;
    int approxMapCloseSize;
    int approxMapReconstructSize;
    int addedBorderSize;
    double maxScoreRegionSize;
    double deltaEToBeSimilar;
    double approxMapReconstructSizePercentage;
    double relevancyStart;
    double relevancyIterationDec;
    double minMaskRelevancy;
    boolean saveMaskRelevancyPix;
    ImageProcessor mask;
    DebugInfoShower debugInfoShower;
    private ProgressListener progressListener;

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public DeviationScoreRegions_Tuned2() {
        this.blur = this.preBlur;
        this.scoreImageThreshold = 33;
        this.scoreClusteringThreshold = 127.5D;
        this.finalPower = 4.0D;
        this.epsilonPercentage = 0.025D;
        this.minPtsScorePointDensityMultiplier = 1.0D;
        this.mainClusterSize = 0.5D;
        this.convexHullLinkingEpsilonPercentage = 0.05D;
        this.morphImageSize = 400;
        this.approxMapDilateSize = (int)((double)this.morphImageSize * 0.0D);
        this.approxMapCloseSize = (int)((double)this.morphImageSize * 0.0025D);
        this.approxMapReconstructSize = (int)((double)this.morphImageSize * 0.5D);
        this.addedBorderSize = 0;
        this.maxScoreRegionSize = 0.33D;
        this.deltaEToBeSimilar = 25.0D;
        this.approxMapReconstructSizePercentage = 0.33D;
        this.relevancyStart = 0.85D;
        this.relevancyIterationDec = 0.33D;
        this.minMaskRelevancy = 0.75D;
        this.saveMaskRelevancyPix = false;
        this.debugInfoShower = null;
        this.progressListener = null;
    }

    public DeviationScoreRegions_Tuned2(ProgressListener progressListener) {
        this.blur = this.preBlur;
        this.scoreImageThreshold = 33;
        this.scoreClusteringThreshold = 127.5D;
        this.finalPower = 4.0D;
        this.epsilonPercentage = 0.025D;
        this.minPtsScorePointDensityMultiplier = 1.0D;
        this.mainClusterSize = 0.5D;
        this.convexHullLinkingEpsilonPercentage = 0.05D;
        this.morphImageSize = 400;
        this.approxMapDilateSize = (int)((double)this.morphImageSize * 0.0D);
        this.approxMapCloseSize = (int)((double)this.morphImageSize * 0.0025D);
        this.approxMapReconstructSize = (int)((double)this.morphImageSize * 0.5D);
        this.addedBorderSize = 0;
        this.maxScoreRegionSize = 0.33D;
        this.deltaEToBeSimilar = 25.0D;
        this.approxMapReconstructSizePercentage = 0.33D;
        this.relevancyStart = 0.85D;
        this.relevancyIterationDec = 0.33D;
        this.minMaskRelevancy = 0.75D;
        this.saveMaskRelevancyPix = false;
        this.debugInfoShower = null;
        this.progressListener = null;
        this.setProgressListener(progressListener);
    }

    public static void noiseTest(int size) throws IOException {
        Iterator i$ = Tools.getFilesFromDirectory("../data/batch/images/tmp", ".jpg").iterator();

        while(i$.hasNext()) {
            String fileName = (String)i$.next();
            ImageProcessor imageProcessor = Tools.resize(Tools.loadImageProcessor(fileName), size);

            for(double noiseSize = 0.0D; noiseSize < 10.0D; ++noiseSize) {
                ImageProcessor noised = Tools.addNoise(imageProcessor, noiseSize);
                (new DeviationScoreRegions_Tuned2()).run(noised);
                Tools.save(Tools.cropToMask(noised));
            }
        }

    }

    public static void main(String[] args) throws IOException {
        Batch.run(new DeviationScoreRegions_Tuned2(), 0, "../../../data/batch/images/flickr");
    }

    public static ImageProcessor getEdges(ImageProcessor ip) {
        ImageProcessor edges = new ByteProcessor(ip.getWidth(), ip.getHeight());
        MImage mImage = new MImage(ip);

        for(int x = 0; x < ip.getWidth(); ++x) {
            for(int y = 0; y < ip.getHeight(); ++y) {
                double deltaE = Tools.euklidDistance(mImage.getLab(x, y), mImage.getNeigbourMeanLab(x, y, 1));
                int value = (int)Math.pow(deltaE, labDifferencePower);
                edges.putPixel(x, y, value);
            }
        }

        return edges;
    }

    public ImageProcessor run(ImageProcessor original) {
        if (DEBUG.getVerbose()) {
            Tools.save(original);
        }

        int width = original.getWidth();
        int height = original.getHeight();
        if (this.progressListener != null) {
            this.progressListener.progressUpdate(0.0D, "Scoring");
        }

        ImageProcessor preBlurred = Tools.blur(original, this.preBlur, this.preBlurIterations);
        ImageProcessor difference = Tools.difference(getEdges(preBlurred), getEdges(Tools.blur(preBlurred, this.blur)));
        ImageProcessor scoreImage = Tools.power(difference, labDifferencePower);
        scoreImage.threshold(this.scoreImageThreshold);
        if (DEBUG.getVerbose()) {
            Tools.save(scoreImage);
        }

        if (this.progressListener != null) {
            this.progressListener.progressUpdate(0.15D, "Score Clustering");
        }

        List<Point> scorePoints = Tools.imageProcessorToPoints(scoreImage, this.scoreImageThreshold);
        ScoreDbscan scoreDbscan = new ScoreDbscan();
        scoreDbscan.setScoreImage(scoreImage);
        scoreDbscan.setScoreThreshold(this.scoreClusteringThreshold);
        double breite = Math.sqrt((double)original.getPixelCount());
        int epsilon = (int)Math.round(breite * this.epsilonPercentage);
        double scorePointDensity = 0.0D;

        Point p;
        for(Iterator i$ = scorePoints.iterator(); i$.hasNext(); scorePointDensity += Math.min((double)scoreImage.getPixelValue(p.x, p.y) / this.scoreClusteringThreshold, 1.0D)) {
            p = (Point)i$.next();
        }

        scorePointDensity /= (double)scoreImage.getPixelCount();
        System.out.println("scorePointDensity == " + scorePointDensity);
        int minPts = (int)Math.round(Math.pow((double)(epsilon + 1), 2.0D) * scorePointDensity * this.minPtsScorePointDensityMultiplier);
        this.mask = new ByteProcessor(width, height);
        this.mask.setColor(Color.white);
        List<List<Point>> clusters = scoreDbscan.get(scorePoints, epsilon, minPts);
        if (DEBUG.getVerbose()) {
            System.out.println("epsilon = " + epsilon + " scorePointDensity = " + scorePointDensity + " minPts = " + minPts);
            String str = "";
            Tools.showImage("debug", Tools.write(str, Tools.drawClusters(clusters, new ColorProcessor(original.getWidth(), original.getHeight()), false, true)), "DBSCAN Cluster", true);
        }

        DbscanTools.sortBySiye(clusters);
        if (this.progressListener != null) {
            this.progressListener.progressUpdate(0.5D, "Mask Approximation");
        }

        Iterator i$ = clusters.iterator();

        while(i$.hasNext()) {
            List<Point> cluster = (List)i$.next();
            if ((double)cluster.size() / (double)((List)clusters.get(0)).size() < this.mainClusterSize) {
                break;
            }

            Iterator i$ = cluster.iterator();

            while(i$.hasNext()) {
                Point p = (Point)i$.next();
                List<Point> rangePoints = scoreDbscan.range(p, (int)Math.round(breite * this.convexHullLinkingEpsilonPercentage));
                if (rangePoints.size() >= minPts) {
                    this.mask.fillPolygon(ConvexHullTools.get(rangePoints));
                }
            }
        }

        if (DEBUG.getVerbose()) {
            Tools.showImage("debug", this.mask, "Approximation Mask", true);
        }

        this.mask = Tools.resize(this.mask, this.morphImageSize);
        this.mask = Tools.addBorder(this.mask, this.addedBorderSize);
        this.mask = Morphological.dilate(this.mask, this.approxMapDilateSize);
        this.mask = Morphological.close(this.mask, this.approxMapCloseSize);
        this.mask = Morphological.dilateByReconstruction(this.mask, this.approxMapReconstructSize);
        int longestSide = Math.max(width, height);
        this.mask = Tools.removeBorder(this.mask, this.addedBorderSize);
        this.mask = Tools.resize(this.mask, longestSide);
        if (DEBUG.getVerbose()) {
            Tools.save(this.mask);
        }

        scoreImage = Tools.cropToMask(scoreImage, this.mask);
        if (DEBUG.getVerbose()) {
            Tools.showImage("debug", Tools.cropToMask(original, this.mask), "cropped");
        }

        if (this.progressListener != null) {
            this.progressListener.progressUpdate(0.75D, "Region Scoring");
        }

        int maxSize = (int)Math.round((double)Tools.imageProcessorToPoints(this.mask, 1).size() * this.maxScoreRegionSize);
        if (DEBUG.getVerbose()) {
            System.out.println("maxSize == " + maxSize);
        }

        List<ColorRegion> colorRegions = ColorRegionTools.getRegionsFromImageProcessor(original, this.deltaEToBeSimilar, this.mask);
        if (DEBUG.getVerbose()) {
            Tools.save(ColorRegionTools.draw(colorRegions, width, height));
        }

        ColorRegion[][] colorRegionMap = new ColorRegion[width][height];
        Iterator i$ = colorRegions.iterator();

        while(i$.hasNext()) {
            ColorRegion colorRegion = (ColorRegion)i$.next();

            Point p;
            for(Iterator i$ = colorRegion.getPixels().iterator(); i$.hasNext(); colorRegionMap[p.x][p.y] = colorRegion) {
                p = (Point)i$.next();
            }
        }

        int iteration = 0;
        Set<ColorRegion> seedRegions = new HashSet(colorRegions);
        Set<ColorRegion> nextSeedRegions = new HashSet();
        Iterator iterator = seedRegions.iterator();

        while(iterator.hasNext()) {
            ColorRegion colorRegion = (ColorRegion)iterator.next();
            iterator.remove();
            ColorRegion outline = new ColorRegion(ColorRegionTools.getOutline(colorRegion, 1, width, height));
            double maskBoundaryOverlap = ColorRegionTools.overlap(outline, this.mask);
            double scoreBoundaryOverlap = ColorRegionTools.overlap(outline, scoreImage);
            double maskRelevancy = maskBoundaryOverlap * scoreBoundaryOverlap;
            boolean clearRegion = maskBoundaryOverlap <= this.relevancyStart - (double)iteration * this.relevancyIterationDec && maskRelevancy <= this.minMaskRelevancy - (double)iteration * this.relevancyIterationDec && colorRegion.size() <= maxSize;
            if (clearRegion) {
                Iterator i$ = colorRegion.getOutline().iterator();

                while(i$.hasNext()) {
                    Point outlinePoint = (Point)i$.next();
                    nextSeedRegions.add(colorRegionMap[outlinePoint.x][outlinePoint.y]);
                }

                colorRegions.remove(colorRegion);
            }

            if (DEBUG.getVerbose() && colorRegion.size() > 20 && maskBoundaryOverlap < 0.85D) {
                String s = "MBO=" + Tools.formatNumber(maskBoundaryOverlap) + " SBO=" + Tools.formatNumber(scoreBoundaryOverlap) + " REL=" + Tools.formatNumber(maskRelevancy);
                Color c = clearRegion ? Color.red : Color.green;
                Tools.showImage("debug", Tools.write(s, ColorRegionTools.draw(colorRegion, ColorRegionTools.draw(colorRegions, width, height), true, c)), "iteration " + iteration, this.saveMaskRelevancyPix);
            }

            if (seedRegions.isEmpty()) {
                seedRegions.addAll(nextSeedRegions);
                nextSeedRegions.clear();
                ++iteration;
            }
        }

        if (DEBUG.getVerbose() && this.saveMaskRelevancyPix) {
            Tools.showImage("debug", ColorRegionTools.draw(colorRegions, width, height), "color regions");
        }

        if (this.progressListener != null) {
            this.progressListener.progressUpdate(1.0D, "");
        }

        this.mask = ColorRegionTools.mask(colorRegions, width, height);
        if (DEBUG.getVerbose() && this.saveMaskRelevancyPix) {
            Tools.save(this.mask);
        }

        original.setMask(this.mask);
        return Tools.cropToMask(original);
    }
}
