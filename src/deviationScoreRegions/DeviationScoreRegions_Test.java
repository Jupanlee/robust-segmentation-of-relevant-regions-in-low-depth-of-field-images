//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions;

import basics.MImage;
import basics.Tools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.dbscan.DbscanTools;
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

public class DeviationScoreRegions_Test implements Batchable {
    double preBlur = 1.0D;
    int preBlurIterations = 5;
    double blur = 0.9D;
    int scoreImageThreshold = 33;
    double scoreClusteringThreshold = 63.75D;
    double labDifferencePower = 2.5D;
    double finalPower = 4.0D;
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
    double relevancyIterationDec = 0.2D;
    double minMaskRelevancy = 0.75D;
    boolean saveMaskRelevancyPix = false;
    ImageProcessor mask;

    public DeviationScoreRegions_Test() {
    }

    public static void main(String[] args) {
        DEBUG.setVerbose(true);
        Batch.run(new DeviationScoreRegions_Test(), 0, "../data/batch/images/base_640");
    }

    private ImageProcessor getEdges(ImageProcessor ip) {
        ImageProcessor edges = new ByteProcessor(ip.getWidth(), ip.getHeight());
        MImage mImage = new MImage(ip);

        for(int x = 0; x < ip.getWidth(); ++x) {
            for(int y = 0; y < ip.getHeight(); ++y) {
                double deltaE = Tools.euklidDistance(mImage.getLab(x, y), mImage.getNeigbourMeanLab(x, y, 1));
                int value = (int)Math.pow(deltaE, this.labDifferencePower);
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
        ImageProcessor preBlurred = Tools.blur(original, this.preBlur, this.preBlurIterations);
        ImageProcessor difference = Tools.difference(this.getEdges(preBlurred), this.getEdges(Tools.blur(preBlurred, this.blur)));
        ImageProcessor scoreImage = Tools.power(difference, this.labDifferencePower);
        scoreImage.threshold(this.scoreImageThreshold);
        if (DEBUG.getVerbose()) {
            Tools.save(scoreImage);
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
        List<List<Point>> clusters = scoreDbscan.get(scorePoints, epsilon, minPts);
        if (DEBUG.getVerbose()) {
            System.out.println("epsilon = " + epsilon + " scorePointDensity = " + scorePointDensity + " minPts = " + minPts);
            String str = "";
            Tools.showImage("debug", Tools.write(str, Tools.drawClusters(clusters, new ColorProcessor(original.getWidth(), original.getHeight()), false, true)), "DBSCAN Cluster", true);
        }

        DbscanTools.sortBySiye(clusters);
        Iterator i$ = clusters.iterator();

        while(i$.hasNext()) {
            List<Point> cluster = (List)i$.next();
            if ((double)cluster.size() / (double)((List)clusters.get(0)).size() >= this.mainClusterSize) {
                Tools.drawPoints(cluster, this.mask, Color.white);
            }
        }

        int morphImageSize = 300;
        this.mask = Tools.resize(this.mask, morphImageSize);
        this.mask = Morphological.dilate(this.mask, 3);
        this.mask = Morphological.close(this.mask, 30);
        this.mask = Morphological.dilateByReconstruction(this.mask, Math.round(60.0F));
        int longestSide = Math.max(width, height);
        this.mask = Tools.resize(this.mask, longestSide);
        original.setMask(this.mask);
        return Tools.cropToMask(original);
    }
}
