//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions;

import basics.Tools;
import basics.convexHull.ConvexHullTools;
import basics.javaAddons.DEBUG;
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
import java.util.Iterator;
import java.util.List;
import others.morphological.Morphological;

public class DeviationScoreRegionsBasic extends Dbscan implements Batchable {
    private double gaussBlurRadius = 0.0D;
    private double powerAfterBlur = 1.0D;
    ImageProcessor neighbourScoreImage;
    private int finalMapReconstructSize = 100;
    private int finalMapCloseSize = 3;
    private ImageProcessor mask;

    public DeviationScoreRegionsBasic() {
    }

    public static void main(String[] args) {
        Batch.run(new DeviationScoreRegionsBasic(), 450, "data/batch/images/fuzzy");
    }

    private static double getMean(ImageProcessor imageProcessor) {
        double sum = 0.0D;

        for(int x = 0; x < imageProcessor.getWidth(); ++x) {
            for(int y = 0; y < imageProcessor.getHeight(); ++y) {
                sum += (double)imageProcessor.getPixelValue(x, y);
            }
        }

        return sum / (double)imageProcessor.getPixelCount();
    }

    public static double getMeanPointsFromRange(List<Point> points, int epsilon) {
        int sum = 0;
        Query query = new Query(points);

        Point p;
        for(Iterator i$ = points.iterator(); i$.hasNext(); sum += query.range(p, (double)epsilon).size()) {
            p = (Point)i$.next();
        }

        return (double)sum / (double)points.size();
    }

    public ImageProcessor run(ImageProcessor original) {
        Tools.save(original);
        int width = original.getWidth();
        int height = original.getHeight();
        ImageProcessor scoreImage = (new OldScoreLabDifference()).run(original);
        scoreImage = Tools.threshold(scoreImage, 33.0D);
        this.neighbourScoreImage = Tools.blur(scoreImage, this.gaussBlurRadius);
        this.neighbourScoreImage = Tools.power(this.neighbourScoreImage, this.powerAfterBlur);
        double minScore = getMean(this.neighbourScoreImage) * 0.666D;
        if (DEBUG.getVerbose()) {
            System.out.println("minScore = " + minScore);
            Tools.save(scoreImage);
        }

        List<Point> scorePoints = Tools.imageProcessorToPoints(scoreImage, 1);
        double breite = Math.sqrt((double)original.getPixelCount());
        this.epsilon = (int)(breite * 0.025D);
        double scorePointDensity = (double)scorePoints.size() / (double)original.getPixelCount();
        this.minPts = (int)(Math.pow((double)(this.epsilon + 1), 2.0D) * scorePointDensity * 1.0D);
        this.mask = new ByteProcessor(width, height);
        this.mask.setColor(Color.white);
        this.get(scorePoints, this.epsilon, this.minPts);
        if (DEBUG.getVerbose()) {
            System.out.println("epsilon = " + this.epsilon + " scorePointDensity = " + scorePointDensity + " minPts = " + this.minPts);
            Tools.save(Tools.drawClusters(this.clusters, new ColorProcessor(original.getWidth(), original.getHeight()), false, true));
        }

        DbscanTools.sortBySiye(this.clusters);
        Iterator i$ = this.clusters.iterator();

        List colorRegions;
        while(i$.hasNext()) {
            List<Point> cluster = (List)i$.next();
            if ((double)cluster.size() / (double)((List)this.clusters.get(0)).size() < 0.5D) {
                break;
            }

            Iterator it = cluster.iterator();

            while(it.hasNext()) {
                Point p = (Point)it.next();
                colorRegions = this.range(p, this.epsilon);
                if (colorRegions.size() >= this.minPts) {
                    this.mask.fillPolygon(ConvexHullTools.get(colorRegions));
                }
            }
        }

        this.mask = Morphological.close(this.mask, this.finalMapCloseSize);
        this.mask = Morphological.dilateByReconstruction(this.mask, this.finalMapReconstructSize);
        if (DEBUG.getVerbose()) {
            Tools.save(this.mask);
        }

        scoreImage = Tools.cropToMask(scoreImage, this.mask);
        int maxSize = (int)Math.round((double)Tools.imageProcessorToPoints(this.mask, 1).size() * 0.1D);
        if (DEBUG.getVerbose()) {
            System.out.println("maxSize == " + maxSize);
        }

        boolean changesmade = true;
        double deltaEToBeSimilar = 30.0D;
        colorRegions = ColorRegionTools.getRegionsFromImageProcessor(original, deltaEToBeSimilar, this.mask);
        if (DEBUG.getVerbose()) {
            Tools.save(ColorRegionTools.draw(colorRegions, width, height));
        }

        int iteration = -1;

        while(changesmade) {
            changesmade = false;
            ++iteration;
            int DEBUGCOUNT = colorRegions.size();
            Iterator it = colorRegions.iterator();

            while(it.hasNext()) {
                ColorRegion colorRegion = (ColorRegion)it.next();
                if (colorRegion.size() > 0) {
                    ColorRegion outline = new ColorRegion(ColorRegionTools.getOutline(colorRegion, 1, width, height));
                    double maskBoundaryOverlap = ColorRegionTools.overlap(outline, this.mask);
                    double scoreBoundaryOverlap = ColorRegionTools.overlap(outline, scoreImage);
                    double maskRelevancy = 100.0D * maskBoundaryOverlap * scoreBoundaryOverlap;
                    if (DEBUG.getVerbose()) {
                        if (colorRegion.size() > 15 && maskBoundaryOverlap < 0.85D) {
                            (new StringBuilder()).append("MBO=").append(Tools.formatNumber(maskBoundaryOverlap)).append(" SBO=").append(Tools.formatNumber(scoreBoundaryOverlap)).append(" REL=").append(Tools.formatNumber(maskRelevancy)).toString();
                        }

                        --DEBUGCOUNT;
                    }

                    if (maskBoundaryOverlap <= 0.75D - (double)iteration * 0.333D && maskRelevancy < 75.0D && colorRegion.size() <= maxSize) {
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
