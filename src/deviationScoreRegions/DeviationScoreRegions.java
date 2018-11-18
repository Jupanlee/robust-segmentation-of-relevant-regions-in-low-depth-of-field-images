package deviationScoreRegions;

import basics.Tools;
import basics.convexHull.ConvexHullTools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.dbscan.Dbscan;
import deviationScoreRegions.dbscan.DbscanTools;
import deviationScoreRegions.grow.ColorRegion;
import deviationScoreRegions.grow.ColorRegionTools;
import deviationScoreRegions.modular.edgeFilters.OldScoreLabDifference;
import deviationScoreRegions.scoring.ScoreDeviation;
import evaluation.Batch;
import evaluation.Batch.Batchable;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Point;
import java.io.PrintStream;
import java.util.List;
import others.morphological.Morphological;

public class DeviationScoreRegions extends Dbscan
        implements Batch.Batchable
{
    int imageSize = 300;

    double blur = 1.0D;
    int blurIterations = 10;
    int scoreImageThreshold = 33;
    double labDifferencePower = 1.0D;
    double finalPower = 4.0D;

    int dbscanScoreImageThreshold = 1;
    double epsilonPercentage = 0.025D;
    double minPtsScorePointDensityMultiplier = 1.0D;
    double mainClusterSize = 0.5D;

    double closingEpsilonPercentage = 0.05D;
    int approxMapDilateSize = 0;
    int approxMapCloseSize = 15;

    double maxScoreRegionSize = 0.2D;
    double deltaEToBeSimilar = 25.0D;
    double approxMapReconstructSizePercentage = 0.33D;

    double relevancyStart = 0.85D;
    double relevancyIterationDec = 0.333D;
    double minMaskRelevancy = 0.75D;

    boolean saveMaskRelevancyPix = false;
    ImageProcessor mask;

    public static void main(String[] args)
    {
        DEBUG.setVerbose(true);
        Batch.run(new DeviationScoreRegions(), 450, "../../images/schwierig");
    }

    public ImageProcessor run(ImageProcessor original)
    {
        Tools.save(original);
        int width = original.getWidth();
        int height = original.getHeight();

        ImageProcessor scoreImage = new ScoreDeviation(new OldScoreLabDifference(), this.blur, this.blurIterations, this.finalPower, this.labDifferencePower).run(original);
        scoreImage = Tools.threshold(scoreImage, this.scoreImageThreshold);
        if (DEBUG.getVerbose()) {
            Tools.save(scoreImage);
        }

        List scorePoints = Tools.imageProcessorToPoints(scoreImage, this.dbscanScoreImageThreshold);
        double breite = Math.sqrt(original.getPixelCount());
        this.epsilon = (int)Math.round(breite * this.epsilonPercentage);
        double scorePointDensity = scorePoints.size() / original.getPixelCount();
        this.minPts = (int)Math.round(Math.pow(this.epsilon + 1, 2.0D) * scorePointDensity * this.minPtsScorePointDensityMultiplier);
        this.mask = new ByteProcessor(width, height);
        this.mask.setColor(Color.white);
        get(scorePoints, this.epsilon, this.minPts);
        if (DEBUG.getVerbose()) {
            System.out.println("epsilon = " + this.epsilon + " scorePointDensity = " + scorePointDensity + " minPts = " + this.minPts);
            String str = "";
            Tools.showImage("debug", Tools.write(str, Tools.drawClusters(this.clusters, new ColorProcessor(original.getWidth(), original.getHeight()), false, true)), "DBSCAN Cluster");
        }
        DbscanTools.sortBySiye(this.clusters);

        for (List cluster : this.clusters)
        {
            if (cluster.size() / ((List)this.clusters.get(0)).size() < this.mainClusterSize) break;
            for (Point p : cluster) {
                List rangePoints = range(p, (int)Math.round(breite * this.closingEpsilonPercentage));
                if (rangePoints.size() >= this.minPts) {
                    this.mask.fillPolygon(ConvexHullTools.get(rangePoints));
                }
            }
        }
        if (DEBUG.getVerbose()) {
            Tools.showImage("debug", this.mask, "Approximation Mask", true);
        }

        this.mask = Morphological.dilate(this.mask, this.approxMapDilateSize);
        this.mask = Morphological.close(this.mask, this.approxMapCloseSize);
        int finalMapReconstructSize = (int)Math.round(this.approxMapReconstructSizePercentage * breite);
        this.mask = Morphological.dilateByReconstruction(this.mask, finalMapReconstructSize);
        if (DEBUG.getVerbose()) {
            Tools.showImage("debug", this.mask, "Approximation Mask - morphological");
        }
        scoreImage = Tools.cropToMask(scoreImage, this.mask);
        if (DEBUG.getVerbose()) {
            Tools.showImage("debug", Tools.cropToMask(original, this.mask), "cropped", true);
        }

        int maxSize = (int)Math.round(Tools.imageProcessorToPoints(this.mask, 1).size() * this.maxScoreRegionSize);
        if (DEBUG.getVerbose()) {
            System.out.println("maxSize == " + maxSize);
        }

        boolean changesmade = true;
        List colorRegions = ColorRegionTools.getRegionsFromImageProcessor(original, this.deltaEToBeSimilar, this.mask);
        if (DEBUG.getVerbose()) {
            Tools.save(ColorRegionTools.draw(colorRegions, width, height));
        }
        int iteration = -1;
        while (changesmade) {
            changesmade = false;
            iteration++;

            int DEBUGCOUNT = colorRegions.size();
            for (ColorRegion colorRegion : colorRegions) {
                if (colorRegion.size() > 0)
                {
                    ColorRegion outline = new ColorRegion(ColorRegionTools.getOutline(colorRegion, 1, width, height));

                    double maskBoundaryOverlap = ColorRegionTools.overlap(outline, this.mask);
                    double scoreBoundaryOverlap = ColorRegionTools.overlap(outline, scoreImage);
                    double maskRelevancy = maskBoundaryOverlap * scoreBoundaryOverlap;

                    boolean clearRegion = (maskBoundaryOverlap <= this.relevancyStart - iteration * this.relevancyIterationDec) && (maskRelevancy <= this.minMaskRelevancy - iteration * this.relevancyIterationDec) && (colorRegion.size() <= maxSize);

                    if (DEBUG.getVerbose()) {
                        if ((colorRegion.size() > 20) && (maskBoundaryOverlap < 0.85D)) {
                            String s = "MBO=" + Tools.formatNumber(maskBoundaryOverlap) + " SBO=" + Tools.formatNumber(scoreBoundaryOverlap) + " REL=" + Tools.formatNumber(maskRelevancy);
                            Color c = clearRegion ? Color.red : Color.green;
                            Tools.showImage("debug", Tools.write(s, ColorRegionTools.draw(colorRegion, ColorRegionTools.draw(colorRegions, width, height), true, c)), "iteration " + iteration, this.saveMaskRelevancyPix);
                        }

                        DEBUGCOUNT--;
                    }

                    if (clearRegion) {
                        changesmade = true;
                        colorRegion.clear();
                    }
                }
            }
            if ((DEBUG.getVerbose()) && (this.saveMaskRelevancyPix)) {
                Tools.showImage("debug", ColorRegionTools.draw(colorRegions, width, height), "color regions");
            }

            this.mask = ColorRegionTools.mask(colorRegions, width, height);
            if ((DEBUG.getVerbose()) && (this.saveMaskRelevancyPix)) {
                Tools.save(this.mask);
            }

        }

        original.setMask(this.mask);
        return Tools.cropToMask(original);
    }
}