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
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import others.morphological.Morphological;

public class DeviationScoreRegions_2_differentSizes
        implements Batch.Batchable
{
    double preBlur = 1.25D;
    int preBlurIterations = 3;
    double blur = this.preBlur;
    int scoreImageThreshold = 33;
    double scoreClusteringThreshold = 127.5D;
    static double labDifferencePower = 2.25D;
    double finalPower = 4.0D;

    double epsilonPercentage = 0.025D;
    double minPtsScorePointDensityMultiplier = 1.0D;
    double mainClusterSize = 0.5D;

    double convexHullLinkingEpsilonPercentage = this.epsilonPercentage;

    int maskApproximationSize = 350;
    int nrOfErodes = 0;
    int approxMapCloseSize = (int)(this.maskApproximationSize * 0.01D);
    int approxMapReconstructSize = (int)(this.maskApproximationSize * 0.25D);
    int addedBorderSize = 0;

    double maxScoreRegionSize = 0.33D;
    double deltaEToBeSimilar = 25.0D;
    double approxMapReconstructSizePercentage = 0.33D;
    double minMBO = 0.85D;
    double relevancyIterationDec = 0.33D;
    double minMaskRelevancy = 0.75D;
    boolean convexHullLinking = false;

    boolean saveMaskRelevancyPix = false;
    ImageProcessor mask;
    DebugInfoShower debugInfoShower = null;
    private ProgressListener progressListener = null;

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public DeviationScoreRegions_2_differentSizes() {
    }

    public DeviationScoreRegions_2_differentSizes(ProgressListener progressListener) {
        setProgressListener(progressListener);
    }

    public static void main(String[] args)
            throws IOException
    {
        int size = 0;

        Batch.run(new DeviationScoreRegions_2_differentSizes(), size, "../../../data/batch/images/similarity");
    }

    public static ImageProcessor getEdges(ImageProcessor ip)
    {
        ImageProcessor edges = new ByteProcessor(ip.getWidth(), ip.getHeight());
        MImage mImage = new MImage(ip);

        for (int x = 0; x < ip.getWidth(); x++) {
            for (int y = 0; y < ip.getHeight(); y++) {
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
        List scorePoints = Tools.imageProcessorToPoints(scoreImage, this.scoreImageThreshold);
        ScoreDbscan scoreDbscan = new ScoreDbscan();
        scoreDbscan.setScoreImage(scoreImage);
        scoreDbscan.setScoreThreshold(this.scoreClusteringThreshold);
        double breite = Math.sqrt(scoreImage.getPixelCount());
        int epsilon = (int)Math.round(breite * this.epsilonPercentage);

        double scorePointDensity = 0.0D;
        for (Point p : scorePoints) {
            scorePointDensity += Math.min(scoreImage.getPixelValue(p.x, p.y) / this.scoreClusteringThreshold, 1.0D);
        }
        scorePointDensity /= scoreImage.getPixelCount();

        int minPts = (int)Math.round(Math.pow(epsilon + 1, 2.0D) * scorePointDensity * this.minPtsScorePointDensityMultiplier);
        this.mask = new ByteProcessor(scoreImage.getWidth(), scoreImage.getHeight());
        this.mask.setColor(Color.white);
        List clusters = scoreDbscan.get(scorePoints, epsilon, minPts);
        if (DEBUG.getVerbose()) {
            System.out.println("epsilon = " + epsilon + " scorePointDensity = " + scorePointDensity + " minPts = " + minPts);
            String str = "";
            Tools.showImage("debug", Tools.write(str, Tools.drawClusters(clusters, new ColorProcessor(this.mask.getWidth(), this.mask.getHeight()), false, true)), "DBSCAN Cluster", true);
        }
        DbscanTools.sortBySiye(clusters);

        if (this.progressListener != null) {
            this.progressListener.progressUpdate(0.5D, "Mask Approximation");
        }

        for (List cluster : clusters)
        {
            if (cluster.size() / ((List)clusters.get(0)).size() < this.mainClusterSize)
            {
                break;
            }

            eps = (int)Math.round(breite * this.convexHullLinkingEpsilonPercentage);
            for (Point p : cluster) {
                List rangePoints = scoreDbscan.range(p, eps);
                if (rangePoints.size() >= minPts)
                    if (this.convexHullLinking)
                        this.mask.fillPolygon(ConvexHullTools.get(rangePoints));
                    else
                        this.mask.fillOval(p.x, p.y, eps, eps);
            }
        }
        int eps;
        if (DEBUG.getVerbose()) {
            Tools.showImage("debug", this.mask, "Approximation Mask", true);
        }

        this.mask = Tools.addBorder(this.mask, this.addedBorderSize);
        this.mask.erode();
        for (int i = 0; i < this.nrOfErodes; i++) {
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

        int maxSize = (int)Math.round(Tools.imageProcessorToPoints(this.mask, 1).size() * this.maxScoreRegionSize);
        if (DEBUG.getVerbose()) {
            System.out.println("maxSize == " + maxSize);
        }

        List colorRegions = ColorRegionTools.getRegionsFromImageProcessor(resized, this.deltaEToBeSimilar, this.mask);
        if (DEBUG.getVerbose()) {
            Tools.save(ColorRegionTools.draw(colorRegions, this.mask.getWidth(), this.mask.getHeight()));
        }

        ColorRegion[][] colorRegionMap = new ColorRegion[this.mask.getWidth()][this.mask.getHeight()];
        for (Iterator i$ = colorRegions.iterator(); i$.hasNext(); ) { colorRegion = (ColorRegion)i$.next();
            for (Point p : colorRegion.getPixels())
                colorRegionMap[p.x][p.y] = colorRegion;
        }
        ColorRegion colorRegion;
        int iteration = 0;
        Set seedRegions = new HashSet(colorRegions);
        Set nextSeedRegions = new HashSet();
        Iterator iterator = seedRegions.iterator();
        while (iterator.hasNext())
        {
            ColorRegion colorRegion = (ColorRegion)iterator.next();
            iterator.remove();

            ColorRegion outline = new ColorRegion(ColorRegionTools.getOutline(colorRegion, 1, this.mask.getWidth(), this.mask.getHeight()));

            double maskBoundaryOverlap = ColorRegionTools.overlap(outline, this.mask);
            double scoreBoundaryOverlap = ColorRegionTools.overlap(outline, scoreImage);
            double maskRelevancy = maskBoundaryOverlap * scoreBoundaryOverlap;

            boolean clearRegion = (maskBoundaryOverlap <= this.minMBO - iteration * this.relevancyIterationDec) && (maskRelevancy <= this.minMaskRelevancy - iteration * this.relevancyIterationDec) && (colorRegion.size() <= maxSize);
            if (clearRegion)
            {
                for (Point outlinePoint : colorRegion.getOutline()) {
                    nextSeedRegions.add(colorRegionMap[outlinePoint.x][outlinePoint.y]);
                }

                colorRegions.remove(colorRegion);
            }

            if ((DEBUG.getVerbose()) &&
                    (colorRegion.size() > 20) && (maskBoundaryOverlap < 0.85D)) {
                String s = "MBO=" + Tools.formatNumber(maskBoundaryOverlap) + " SBO=" + Tools.formatNumber(scoreBoundaryOverlap) + " REL=" + Tools.formatNumber(maskRelevancy);
                Color c = clearRegion ? Color.red : Color.green;
                Tools.showImage("debug", Tools.write(s, ColorRegionTools.draw(colorRegion, ColorRegionTools.draw(colorRegions, this.mask.getWidth(), this.mask.getHeight()), true, c)), "iteration " + iteration, this.saveMaskRelevancyPix);
            }

            if (seedRegions.isEmpty()) {
                seedRegions.addAll(nextSeedRegions);
                nextSeedRegions.clear();
                iteration++;
            }
        }
        if ((DEBUG.getVerbose()) && (this.saveMaskRelevancyPix)) {
            Tools.showImage("debug", ColorRegionTools.draw(colorRegions, this.mask.getWidth(), this.mask.getHeight()), "color regions");
        }

        if (this.progressListener != null) {
            this.progressListener.progressUpdate(1.0D, "");
        }

        this.mask = ColorRegionTools.mask(colorRegions, this.mask.getWidth(), this.mask.getHeight());
        if ((DEBUG.getVerbose()) && (this.saveMaskRelevancyPix))
        {
            Tools.save(this.mask);
        }
        this.mask = this.mask.resize(original.getWidth(), original.getHeight());
        original.setMask(this.mask);
        return Tools.cropToMask(original);
    }

    public String toString()
    {
        return "DSR_2";
    }
}