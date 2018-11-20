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

public class DeviationScoreRegions_2_differentSizes implements Batchable {
    double preBlur = 1.25D;
    int preBlurIterations = 3;
    double blur;
    int scoreImageThreshold;
    double scoreClusteringThreshold;
    static double labDifferencePower = 2.25D;
    double finalPower;
    double epsilonPercentage;
    double minPtsScorePointDensityMultiplier;
    double mainClusterSize;
    double convexHullLinkingEpsilonPercentage;
    int maskApproximationSize;
    int nrOfErodes;
    int approxMapCloseSize;
    int approxMapReconstructSize;
    int addedBorderSize;
    double maxScoreRegionSize;
    double deltaEToBeSimilar;
    double approxMapReconstructSizePercentage;
    double minMBO;
    double relevancyIterationDec;
    double minMaskRelevancy;
    boolean convexHullLinking;
    boolean saveMaskRelevancyPix;
    ImageProcessor mask;
    DebugInfoShower debugInfoShower;
    private ProgressListener progressListener;

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public DeviationScoreRegions_2_differentSizes() {
        this.blur = this.preBlur;
        this.scoreImageThreshold = 33;
        this.scoreClusteringThreshold = 127.5D;
        this.finalPower = 4.0D;
        this.epsilonPercentage = 0.025D;
        this.minPtsScorePointDensityMultiplier = 1.0D;
        this.mainClusterSize = 0.5D;
        this.convexHullLinkingEpsilonPercentage = this.epsilonPercentage;
        this.maskApproximationSize = 350;
        this.nrOfErodes = 0;
        this.approxMapCloseSize = (int)((double)this.maskApproximationSize * 0.01D);
        this.approxMapReconstructSize = (int)((double)this.maskApproximationSize * 0.25D);
        this.addedBorderSize = 0;
        this.maxScoreRegionSize = 0.33D;
        this.deltaEToBeSimilar = 25.0D;
        this.approxMapReconstructSizePercentage = 0.33D;
        this.minMBO = 0.85D;
        this.relevancyIterationDec = 0.33D;
        this.minMaskRelevancy = 0.75D;
        this.convexHullLinking = false;
        this.saveMaskRelevancyPix = false;
        this.debugInfoShower = null;
        this.progressListener = null;
    }

    public DeviationScoreRegions_2_differentSizes(ProgressListener progressListener) {
        this.blur = this.preBlur;
        this.scoreImageThreshold = 33;
        this.scoreClusteringThreshold = 127.5D;
        this.finalPower = 4.0D;
        this.epsilonPercentage = 0.025D;
        this.minPtsScorePointDensityMultiplier = 1.0D;
        this.mainClusterSize = 0.5D;
        this.convexHullLinkingEpsilonPercentage = this.epsilonPercentage;
        this.maskApproximationSize = 350;
        this.nrOfErodes = 0;
        this.approxMapCloseSize = (int)((double)this.maskApproximationSize * 0.01D);
        this.approxMapReconstructSize = (int)((double)this.maskApproximationSize * 0.25D);
        this.addedBorderSize = 0;
        this.maxScoreRegionSize = 0.33D;
        this.deltaEToBeSimilar = 25.0D;
        this.approxMapReconstructSizePercentage = 0.33D;
        this.minMBO = 0.85D;
        this.relevancyIterationDec = 0.33D;
        this.minMaskRelevancy = 0.75D;
        this.convexHullLinking = false;
        this.saveMaskRelevancyPix = false;
        this.debugInfoShower = null;
        this.progressListener = null;
        this.setProgressListener(progressListener);
    }

    public static void main(String[] args) throws IOException {
        int size = 0;
        Batch.run(new DeviationScoreRegions_2_differentSizes(), size, "../../../data/batch/images/similarity");
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

        ImageProcessor resized = Tools.resize(original, this.maskApproximationSize);
        if (this.progressListener != null) {
            this.progressListener.progressUpdate(0.0D, "Scoring");
        }

        ImageProcessor preBlurred = Tools.blur(original, this.preBlur, this.preBlurIterations);
        ImageProcessor difference = Tools.difference(getEdges(preBlurred), getEdges(Tools.blur(preBlurred, this.blur)));
        ImageProcessor scoreImage = Tools.power(difference, labDifferencePower);
        if (Math.max(scoreImage.getWidth(), scoreImage.getHeight()) > this.maskApproximationSize) {
            scoreImage = Tools.resize(scoreImage, this.maskApproximationSize, 2);
        }

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
        double breite = Math.sqrt((double)scoreImage.getPixelCount());
        int epsilon = (int)Math.round(breite * this.epsilonPercentage);
        double scorePointDensity = 0.0D;

        Point p;
        for(Iterator i$ = scorePoints.iterator(); i$.hasNext(); scorePointDensity += Math.min((double)scoreImage.getPixelValue(p.x, p.y) / this.scoreClusteringThreshold, 1.0D)) {
            p = (Point)i$.next();
        }

        scorePointDensity /= (double)scoreImage.getPixelCount();
        int minPts = (int)Math.round(Math.pow((double)(epsilon + 1), 2.0D) * scorePointDensity * this.minPtsScorePointDensityMultiplier);
        this.mask = new ByteProcessor(scoreImage.getWidth(), scoreImage.getHeight());
        this.mask.setColor(Color.white);
        List<List<Point>> clusters = scoreDbscan.get(scorePoints, epsilon, minPts);
        if (DEBUG.getVerbose()) {
            System.out.println("epsilon = " + epsilon + " scorePointDensity = " + scorePointDensity + " minPts = " + minPts);
            String str = "";
            Tools.showImage("debug", Tools.write(str, Tools.drawClusters(clusters, new ColorProcessor(this.mask.getWidth(), this.mask.getHeight()), false, true)), "DBSCAN Cluster", true);
        }

        DbscanTools.sortBySiye(clusters);
        if (this.progressListener != null) {
            this.progressListener.progressUpdate(0.5D, "Mask Approximation");
        }

        Iterator it = clusters.iterator();
        List colorRegions;
        while(it.hasNext()) {
            colorRegions = (List)it.next();
            if ((double)colorRegions.size() / (double)((List)clusters.get(0)).size() < this.mainClusterSize) {
                break;
            }

            int eps = (int)Math.round(breite * this.convexHullLinkingEpsilonPercentage);
            Iterator it1 = colorRegions.iterator();

            while(it1.hasNext()) {
                Point p1 = (Point)it1.next();
                List<Point> rangePoints = scoreDbscan.range(p1, eps);
                if (rangePoints.size() >= minPts) {
                    if (this.convexHullLinking) {
                        this.mask.fillPolygon(ConvexHullTools.get(rangePoints));
                    } else {
                        this.mask.fillOval(p1.x, p1.y, eps, eps);
                    }
                }
            }
        }

        if (DEBUG.getVerbose()) {
            Tools.showImage("debug", this.mask, "Approximation Mask", true);
        }

        this.mask = Tools.addBorder(this.mask, this.addedBorderSize);
        this.mask.erode();

        int maxSize;
        for(maxSize = 0; maxSize < this.nrOfErodes; ++maxSize) {
            this.mask.erode();
        }

        this.mask = Morphological.close(this.mask, this.approxMapCloseSize);
        this.mask = Morphological.dilateByReconstruction(this.mask, this.approxMapReconstructSize);
        this.mask = Tools.removeBorder(this.mask, this.addedBorderSize);
        if (DEBUG.getVerbose()) {
            Tools.save(this.mask);
        }

        scoreImage = Tools.cropToMask(scoreImage, this.mask);
        if (DEBUG.getVerbose()) {
            Tools.showImage("debug", Tools.cropToMask(resized, this.mask), "cropped");
        }

        if (DEBUG.getVerbose()) {
            Tools.save(Tools.cropToMask(original, this.mask.resize(original.getWidth(), original.getHeight())));
        }

        if (this.progressListener != null) {
            this.progressListener.progressUpdate(0.75D, "Region Scoring");
        }

        maxSize = (int)Math.round((double)Tools.imageProcessorToPoints(this.mask, 1).size() * this.maxScoreRegionSize);
        if (DEBUG.getVerbose()) {
            System.out.println("maxSize == " + maxSize);
        }

        colorRegions = ColorRegionTools.getRegionsFromImageProcessor(resized, this.deltaEToBeSimilar, this.mask);
        if (DEBUG.getVerbose()) {
            Tools.save(ColorRegionTools.draw(colorRegions, this.mask.getWidth(), this.mask.getHeight()));
        }

        ColorRegion[][] colorRegionMap = new ColorRegion[this.mask.getWidth()][this.mask.getHeight()];
        Iterator it2 = colorRegions.iterator();

        while(it2.hasNext()) {
            ColorRegion colorRegion = (ColorRegion)it2.next();

            Point p2;
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
            ColorRegion outline = new ColorRegion(ColorRegionTools.getOutline(colorRegion, 1, this.mask.getWidth(), this.mask.getHeight()));
            double maskBoundaryOverlap = ColorRegionTools.overlap(outline, this.mask);
            double scoreBoundaryOverlap = ColorRegionTools.overlap(outline, scoreImage);
            double maskRelevancy = maskBoundaryOverlap * scoreBoundaryOverlap;
            boolean clearRegion = maskBoundaryOverlap <= this.minMBO - (double)iteration * this.relevancyIterationDec && maskRelevancy <= this.minMaskRelevancy - (double)iteration * this.relevancyIterationDec && colorRegion.size() <= maxSize;
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
                Tools.showImage("debug", Tools.write(s, ColorRegionTools.draw(colorRegion, ColorRegionTools.draw(colorRegions, this.mask.getWidth(), this.mask.getHeight()), true, c)), "iteration " + iteration, this.saveMaskRelevancyPix);
            }

            if (seedRegions.isEmpty()) {
                seedRegions.addAll(nextSeedRegions);
                nextSeedRegions.clear();
                ++iteration;
            }
        }

        if (DEBUG.getVerbose() && this.saveMaskRelevancyPix) {
            Tools.showImage("debug", ColorRegionTools.draw(colorRegions, this.mask.getWidth(), this.mask.getHeight()), "color regions");
        }

        if (this.progressListener != null) {
            this.progressListener.progressUpdate(1.0D, "");
        }

        this.mask = ColorRegionTools.mask(colorRegions, this.mask.getWidth(), this.mask.getHeight());
        if (DEBUG.getVerbose() && this.saveMaskRelevancyPix) {
            Tools.save(this.mask);
        }

        this.mask = this.mask.resize(original.getWidth(), original.getHeight());
        original.setMask(this.mask);
        return Tools.cropToMask(original);
    }

    public String toString() {
        return "DSR_2";
    }
}
