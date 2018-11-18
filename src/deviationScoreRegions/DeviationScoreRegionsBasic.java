package deviationScoreRegions;

import basics.Tools;
import basics.convexHull.ConvexHullTools;
import basics.javaAddons.DEBUG;
import basics.javaAddons.MQueue;
import basics.pointSetOperations.clustering.Query;
import deviationScoreRegions.dbscan.Dbscan;
import deviationScoreRegions.dbscan.DbscanTools;
import deviationScoreRegions.grow.ColorRegion;
import deviationScoreRegions.grow.ColorRegionTools;
import deviationScoreRegions.modular.edgeFilters.OldScoreLabDifference;
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

public class DeviationScoreRegionsBasic extends Dbscan
        implements Batch.Batchable
{
    private double gaussBlurRadius = 0.0D;
    private double powerAfterBlur = 1.0D;
    ImageProcessor neighbourScoreImage;
    private int finalMapReconstructSize = 100;
    private int finalMapCloseSize = 3;
    private ImageProcessor mask;

    public static void main(String[] args)
    {
        Batch.run(new DeviationScoreRegionsBasic(), 450, "data/batch/images/fuzzy");
    }

    private static double getMean(ImageProcessor imageProcessor) {
        double sum = 0.0D;
        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++) {
                sum += imageProcessor.getPixelValue(x, y);
            }
        }
        return sum / imageProcessor.getPixelCount();
    }

    public static double getMeanPointsFromRange(List<Point> points, int epsilon)
    {
        int sum = 0;
        Query query = new Query(points);
        for (Point p : points) {
            sum += query.range(p, epsilon).size();
        }

        return sum / points.size();
    }

    public ImageProcessor run(ImageProcessor original) {
        Tools.save(original);
        int width = original.getWidth();
        int height = original.getHeight();

        ImageProcessor scoreImage = new OldScoreLabDifference().run(original);

        scoreImage = Tools.threshold(scoreImage, 33.0D);

        this.neighbourScoreImage = Tools.blur(scoreImage, this.gaussBlurRadius);
        this.neighbourScoreImage = Tools.power(this.neighbourScoreImage, this.powerAfterBlur);
        double minScore = getMean(this.neighbourScoreImage) * 0.666D;
        if (DEBUG.getVerbose()) {
            System.out.println("minScore = " + minScore);
            Tools.save(scoreImage);
        }

        List scorePoints = Tools.imageProcessorToPoints(scoreImage, 1);
        double breite = Math.sqrt(original.getPixelCount());
        this.epsilon = (int)(breite * 0.025D);
        double scorePointDensity = scorePoints.size() / original.getPixelCount();
        this.minPts = (int)(Math.pow(this.epsilon + 1, 2.0D) * scorePointDensity * 1.0D);
        this.mask = new ByteProcessor(width, height);
        this.mask.setColor(Color.white);
        get(scorePoints, this.epsilon, this.minPts);
        if (DEBUG.getVerbose()) {
            System.out.println("epsilon = " + this.epsilon + " scorePointDensity = " + scorePointDensity + " minPts = " + this.minPts);
            Tools.save(Tools.drawClusters(this.clusters, new ColorProcessor(original.getWidth(), original.getHeight()), false, true));
        }
        DbscanTools.sortBySiye(this.clusters);

        for (List cluster : this.clusters)
        {
            if (cluster.size() / ((List)this.clusters.get(0)).size() < 0.5D)
                break;
            for (Point p : cluster) {
                List rangePoints = range(p, this.epsilon);
                if (rangePoints.size() >= this.minPts) {
                    this.mask.fillPolygon(ConvexHullTools.get(rangePoints));
                }
            }

        }

        this.mask = Morphological.close(this.mask, this.finalMapCloseSize);
        this.mask = Morphological.dilateByReconstruction(this.mask, this.finalMapReconstructSize);
        if (DEBUG.getVerbose()) {
            Tools.save(this.mask);
        }

        scoreImage = Tools.cropToMask(scoreImage, this.mask);

        int maxSize = (int)Math.round(Tools.imageProcessorToPoints(this.mask, 1).size() * 0.1D);
        if (DEBUG.getVerbose()) {
            System.out.println("maxSize == " + maxSize);
        }

        boolean changesmade = true;
        double deltaEToBeSimilar = 30.0D;
        List colorRegions = ColorRegionTools.getRegionsFromImageProcessor(original, deltaEToBeSimilar, this.mask);
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
                    double maskRelevancy = 100.0D * maskBoundaryOverlap * scoreBoundaryOverlap;
                    if (DEBUG.getVerbose())
                    {
                        String s;
                        if ((colorRegion.size() > 15) && (maskBoundaryOverlap < 0.85D)) {
                            s = "MBO=" + Tools.formatNumber(maskBoundaryOverlap) + " SBO=" + Tools.formatNumber(scoreBoundaryOverlap) + " REL=" + Tools.formatNumber(maskRelevancy);
                        }

                        DEBUGCOUNT--;
                    }

                    if ((maskBoundaryOverlap <= 0.75D - iteration * 0.333D) && (maskRelevancy < 75.0D) && (colorRegion.size() <= maxSize)) {
                        changesmade = true;
                        colorRegion.clear();
                    }
                }
            }
            if (DEBUG.getVerbose()) {
                Tools.save(ColorRegionTools.draw(colorRegions, width, height));
            }

            this.mask = ColorRegionTools.mask(colorRegions, width, height);
            if (DEBUG.getVerbose()) {
                Tools.save(this.mask);
            }

        }

        Tools.save(this.mask);

        original.setMask(this.mask);
        return Tools.cropToMask(original);
    }
}