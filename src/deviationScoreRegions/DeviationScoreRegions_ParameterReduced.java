//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions;

import basics.ProgressListener;
import basics.Tools;
import basics.convexHull.ConvexHullTools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.GUI.DebugInfoShower;
import deviationScoreRegions.dbscan.DbscanTools;
import deviationScoreRegions.grow.ColorRegion;
import deviationScoreRegions.grow.ColorRegionTools;
import deviationScoreRegions.modular.scoreImage.DifferenceScoreImage;
import deviationScoreRegions.modular.scoreImage.ScoreImage;
import deviationScoreRegions.modular.scoreImage.DifferenceScoreImage.DifferenceScoreImageParameter;
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

public class DeviationScoreRegions_ParameterReduced implements Batchable {
    int iterations = 1;
    double sigmaPreBlur = 0.9D;
    double sigmaBlur = 0.9D;
    int tetha_score = 50;
    double scoreClusteringThreshold = 255.0D;
    double tetha_epsilon = 0.025D;
    double mainClusterSize = 0.5D;
    int maskApproximationSize = 400;
    double tetha_c = 0.01D;
    double tetha_rec = 0.15D;
    double maxScoreRegionSize = 0.33D;
    double tetha_dist = 25.0D;
    double tetha_rel = 0.66D;
    ImageProcessor scoreImageProcessor;
    ScoreImage scoreImage;
    boolean convexHullLinking = true;
    boolean saveMaskRelevancyPix = false;
    ImageProcessor mask;
    DebugInfoShower debugInfoShower = null;
    private ProgressListener progressListener = null;
    private boolean checkMBO = true;

    public static void main(String[] args) throws IOException {
        DEBUG.setVerbose(false);
        Batch.run(new DeviationScoreRegions_ParameterReduced(), 512, "../../images/schwierig");
    }

    public String toString() {
        return "DSR\t" + this.sigmaBlur + "\t" + this.tetha_score + "\t" + this.tetha_epsilon + "\t" + this.tetha_c + "\t" + this.tetha_rec + "\t" + this.tetha_dist + "\t" + this.tetha_rel + "\t" + this.tetha_rel;
    }

    public void printTitle() {
        System.out.println("DSR-Version\tSigma-Blur\tTetha-Score\tTetha-Epsilon\tTetha-C\tTetha-Rec\tTetha-Dist\tTetha_mbo\tTetha_rel");
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public DeviationScoreRegions_ParameterReduced() {
    }

    public DeviationScoreRegions_ParameterReduced(ProgressListener progressListener) {
        this.setProgressListener(progressListener);
    }

    public ImageProcessor run(ImageProcessor original) {
        if (DEBUG.getVerbose()) {
            Tools.save(original);
        }

        ImageProcessor resized;
        if (Math.max(original.getWidth(), original.getHeight()) > this.maskApproximationSize) {
            resized = Tools.resize(original, this.maskApproximationSize);
        } else {
            resized = original.duplicate();
        }

        if (this.progressListener != null) {
            this.progressListener.progressUpdate(0.0D, "Scoring");
        }

        DifferenceScoreImageParameter sp = new DifferenceScoreImageParameter();
        sp.setSigmaBlur(this.sigmaBlur);
        sp.setSigmaPreBlur(this.sigmaPreBlur);
        sp.setIterations(this.iterations);
        sp.setMaxSize(this.maskApproximationSize);
        this.scoreImage = new DifferenceScoreImage();
        this.scoreImageProcessor = this.scoreImage.generateScore(original);
        int breite = (int)Math.sqrt((double)this.scoreImageProcessor.getPixelCount());
        if (DEBUG.getVerbose()) {
            Tools.save(this.scoreImageProcessor);
        }

        if (this.progressListener != null) {
            this.progressListener.progressUpdate(0.15D, "Score Clustering");
        }

        List<Point> scorePoints = Tools.imageProcessorToPoints(this.scoreImageProcessor, this.tetha_score);
        ScoreDbscan scoreDbscan = new ScoreDbscan();
        scoreDbscan.setScoreImage(this.scoreImageProcessor);
        scoreDbscan.setScoreThreshold(this.scoreClusteringThreshold);
        double scorePointDensity = 0.0D;

        Point p;
        for(Iterator i$ = scorePoints.iterator(); i$.hasNext(); scorePointDensity += Math.min((double)this.scoreImageProcessor.getPixelValue(p.x, p.y) / this.scoreClusteringThreshold, 1.0D)) {
            p = (Point)i$.next();
        }

        scorePointDensity /= (double)this.scoreImageProcessor.getPixelCount();
        int minPts = (int)Math.round(Math.pow(this.tetha_epsilon + 1.0D, 2.0D) * scorePointDensity);
        this.mask = new ByteProcessor(this.scoreImageProcessor.getWidth(), this.scoreImageProcessor.getHeight());
        this.mask.setColor(Color.white);
        int epsilon = (int)Math.round(this.tetha_epsilon * (double)breite);
        List<List<Point>> clusters = scoreDbscan.get(scorePoints, epsilon, minPts);
        if (DEBUG.getVerbose()) {
            System.out.println("epsilon = " + this.tetha_epsilon + " scorePointDensity = " + scorePointDensity + " minPts = " + minPts);
            String str = "";
            Tools.showImage("debug", Tools.write(str, Tools.drawClusters(clusters, new ColorProcessor(this.mask.getWidth(), this.mask.getHeight()), false, true)), "DBSCAN Cluster", true);
        }

        DbscanTools.sortBySiye(clusters);
        if (this.progressListener != null) {
            this.progressListener.progressUpdate(0.5D, "Mask Approximation");
        }

        Iterator i$ = clusters.iterator();

        List colorRegions;
        while(i$.hasNext()) {
            colorRegions = (List)i$.next();
            if ((double)colorRegions.size() / (double)((List)clusters.get(0)).size() < this.mainClusterSize) {
                break;
            }

            Iterator it1 = colorRegions.iterator();

            while(it1.hasNext()) {
                Point p1 = (Point)it1.next();
                List<Point> rangePoints = scoreDbscan.range(p1, epsilon);
                if (rangePoints.size() >= minPts) {
                    if (this.convexHullLinking) {
                        this.mask.fillPolygon(ConvexHullTools.get(rangePoints));
                    } else {
                        this.mask.fillOval(p1.x - epsilon / 2, p1.y - epsilon / 2, epsilon, epsilon);
                    }
                }
            }
        }

        if (DEBUG.getVerbose()) {
            Tools.showImage("debug", this.mask, "Approximation Mask", true);
        }

        this.mask.erode();
        this.mask = Morphological.close(this.mask, (int)(this.tetha_c * (double)breite));
        this.mask = Morphological.dilateByReconstruction(this.mask, (int)(this.tetha_rec * (double)breite));
        this.scoreImageProcessor = Tools.cropToMask(this.scoreImageProcessor, this.mask);
        if (DEBUG.getVerbose()) {
            Tools.showImage("debug", Tools.cropToMask(resized, this.mask), "cropped");
        }

        if (DEBUG.getVerbose()) {
            Tools.save(Tools.cropToMask(original, this.mask.resize(original.getWidth(), original.getHeight())));
        }

        if (this.progressListener != null) {
            this.progressListener.progressUpdate(0.75D, "Region Scoring");
        }

        int maxSize = (int)Math.round((double)Tools.imageProcessorToPoints(this.mask, 1).size() * this.maxScoreRegionSize);
        if (DEBUG.getVerbose()) {
            System.out.println("maxSize == " + maxSize);
        }

        colorRegions = ColorRegionTools.getRegionsFromImageProcessor(resized, this.tetha_dist, this.mask);
        if (DEBUG.getVerbose()) {
            Tools.save(ColorRegionTools.draw(colorRegions, this.mask.getWidth(), this.mask.getHeight()));
        }

        ColorRegion[][] colorRegionMap = new ColorRegion[this.mask.getWidth()][this.mask.getHeight()];
        Iterator it2 = colorRegions.iterator();

        while(it2.hasNext()) {
            ColorRegion colorRegion = (ColorRegion)it2.next();

            Point p2;
            for(Iterator it3 = colorRegion.getPixels().iterator(); it3.hasNext(); colorRegionMap[p.x][p.y] = colorRegion) {
                p2= (Point)it3.next();
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
            double scoreBoundaryOverlap = ColorRegionTools.overlap(outline, this.scoreImageProcessor);
            double maskRelevancy = maskBoundaryOverlap * scoreBoundaryOverlap;
            boolean clearRegion;
            if (this.checkMBO) {
                clearRegion = maskBoundaryOverlap <= this.tetha_rel && maskRelevancy <= this.tetha_rel && colorRegion.size() <= maxSize;
            } else {
                clearRegion = maskRelevancy <= this.tetha_rel && colorRegion.size() <= maxSize;
            }

            if (clearRegion) {
                Iterator it3 = colorRegion.getOutline().iterator();

                while(it3.hasNext()) {
                    Point outlinePoint = (Point)it3.next();
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
        if (DEBUG.getVerbose()) {
            Tools.save(Tools.cropToMask(original));
        }

        return DEBUG.getVerbose() ? Tools.cropToMask(original) : original;
    }
}
