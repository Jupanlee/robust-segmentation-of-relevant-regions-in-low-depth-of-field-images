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

public class DeviationScoreRegions_Tuned2
        implements Batch.Batchable
{
    double preBlur = 1.25D;
    int preBlurIterations = 3;
    double blur = this.preBlur;
    int scoreImageThreshold = 33;
    double scoreClusteringThreshold = 127.5D;
    static double labDifferencePower = 2.0D;
    double finalPower = 4.0D;

    double epsilonPercentage = 0.025D;
    double minPtsScorePointDensityMultiplier = 1.0D;
    double mainClusterSize = 0.5D;

    double convexHullLinkingEpsilonPercentage = 0.05D;

    int morphImageSize = 400;
    int approxMapDilateSize = (int)(this.morphImageSize * 0.0D);
    int approxMapCloseSize = (int)(this.morphImageSize * 0.0025D);
    int approxMapReconstructSize = (int)(this.morphImageSize * 0.5D);
    int addedBorderSize = 0;

    double maxScoreRegionSize = 0.33D;
    double deltaEToBeSimilar = 25.0D;
    double approxMapReconstructSizePercentage = 0.33D;
    double relevancyStart = 0.85D;
    double relevancyIterationDec = 0.33D;
    double minMaskRelevancy = 0.75D;

    boolean saveMaskRelevancyPix = false;
    ImageProcessor mask;
    DebugInfoShower debugInfoShower = null;

    private ProgressListener progressListener = null;

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public DeviationScoreRegions_Tuned2() {
    }

    public DeviationScoreRegions_Tuned2(ProgressListener progressListener) {
        setProgressListener(progressListener);
    }

    public static void noiseTest(int size) throws IOException {
        for (String fileName : Tools.getFilesFromDirectory("../data/batch/images/tmp", ".jpg")) {
            ImageProcessor imageProcessor = Tools.resize(Tools.loadImageProcessor(fileName), size);
            for (double noiseSize = 0.0D; noiseSize < 10.0D; noiseSize += 1.0D) {
                ImageProcessor noised = Tools.addNoise(imageProcessor, noiseSize);
                new DeviationScoreRegions_Tuned2().run(noised);
                Tools.save(Tools.cropToMask(noised));
            }
        }
    }

    public static void main(String[] args)
            throws IOException
    {
        Batch.run(new DeviationScoreRegions_Tuned2(), 0, "../../../data/batch/images/flickr");
    }

    public static ImageProcessor getEdges(ImageProcessor ip) {
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
        int width = original.getWidth();
        int height = original.getHeight();

        if (this.progressListener != null) this.progressListener.progressUpdate(0.0D, "Scoring");

        ImageProcessor preBlurred = Tools.blur(original, this.preBlur, this.preBlurIterations);
        ImageProcessor difference = Tools.difference(getEdges(preBlurred), getEdges(Tools.blur(preBlurred, this.blur)));
        ImageProcessor scoreImage = Tools.power(difference, labDifferencePower);
        scoreImage.threshold(this.scoreImageThreshold);

        if (DEBUG.getVerbose()) {
            Tools.save(scoreImage);
        }

        if (this.progressListener != null) this.progressListener.progressUpdate(0.15D, "Score Clustering");

        List scorePoints = Tools.imageProcessorToPoints(scoreImage, this.scoreImageThreshold);
        ScoreDbscan scoreDbscan = new ScoreDbscan();
        scoreDbscan.setScoreImage(scoreImage);
        scoreDbscan.setScoreThreshold(this.scoreClusteringThreshold);
        double breite = Math.sqrt(original.getPixelCount());
        int epsilon = (int)Math.round(breite * this.epsilonPercentage);

        double scorePointDensity = 0.0D;
        for (Point p : scorePoints) {
            scorePointDensity += Math.min(scoreImage.getPixelValue(p.x, p.y) / this.scoreClusteringThreshold, 1.0D);
        }
        scorePointDensity /= scoreImage.getPixelCount();
        System.out.println("scorePointDensity == " + scorePointDensity);

        int minPts = (int)Math.round(Math.pow(epsilon + 1, 2.0D) * scorePointDensity * this.minPtsScorePointDensityMultiplier);
        this.mask = new ByteProcessor(width, height);
        this.mask.setColor(Color.white);
        List clusters = scoreDbscan.get(scorePoints, epsilon, minPts);
        if (DEBUG.getVerbose()) {
            System.out.println("epsilon = " + epsilon + " scorePointDensity = " + scorePointDensity + " minPts = " + minPts);
            String str = "";
            Tools.showImage("debug", Tools.write(str, Tools.drawClusters(clusters, new ColorProcessor(original.getWidth(), original.getHeight()), false, true)), "DBSCAN Cluster", true);
        }
        DbscanTools.sortBySiye(clusters);

        if (this.progressListener != null) this.progressListener.progressUpdate(0.5D, "Mask Approximation");

        for (List cluster : clusters)
        {
            if (cluster.size() / ((List)clusters.get(0)).size() < this.mainClusterSize)
            {
                break;
            }

            for (Point p : cluster) {
                List rangePoints = scoreDbscan.range(p, (int)Math.round(breite * this.convexHullLinkingEpsilonPercentage));
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

        if (this.progressListener != null) this.progressListener.progressUpdate(0.75D, "Region Scoring");

        int maxSize = (int)Math.round(Tools.imageProcessorToPoints(this.mask, 1).size() * this.maxScoreRegionSize);
        if (DEBUG.getVerbose()) {
            System.out.println("maxSize == " + maxSize);
        }

        List colorRegions = ColorRegionTools.getRegionsFromImageProcessor(original, this.deltaEToBeSimilar, this.mask);
        if (DEBUG.getVerbose()) {
            Tools.save(ColorRegionTools.draw(colorRegions, width, height));
        }

        ColorRegion[][] colorRegionMap = new ColorRegion[width][height];
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

            ColorRegion outline = new ColorRegion(ColorRegionTools.getOutline(colorRegion, 1, width, height));

            double maskBoundaryOverlap = ColorRegionTools.overlap(outline, this.mask);
            double scoreBoundaryOverlap = ColorRegionTools.overlap(outline, scoreImage);
            double maskRelevancy = maskBoundaryOverlap * scoreBoundaryOverlap;

            boolean clearRegion = (maskBoundaryOverlap <= this.relevancyStart - iteration * this.relevancyIterationDec) && (maskRelevancy <= this.minMaskRelevancy - iteration * this.relevancyIterationDec) && (colorRegion.size() <= maxSize);
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
                Tools.showImage("debug", Tools.write(s, ColorRegionTools.draw(colorRegion, ColorRegionTools.draw(colorRegions, width, height), true, c)), "iteration " + iteration, this.saveMaskRelevancyPix);
            }

            if (seedRegions.isEmpty()) {
                seedRegions.addAll(nextSeedRegions);
                nextSeedRegions.clear();
                iteration++;
            }
        }
        if ((DEBUG.getVerbose()) && (this.saveMaskRelevancyPix)) {
            Tools.showImage("debug", ColorRegionTools.draw(colorRegions, width, height), "color regions");
        }

        if (this.progressListener != null) this.progressListener.progressUpdate(1.0D, "");

        this.mask = ColorRegionTools.mask(colorRegions, width, height);
        if ((DEBUG.getVerbose()) && (this.saveMaskRelevancyPix))
        {
            Tools.save(this.mask);
        }

        original.setMask(this.mask);
        return Tools.cropToMask(original);
    }
}