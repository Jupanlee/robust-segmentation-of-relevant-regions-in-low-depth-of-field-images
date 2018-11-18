package deviationScoreRegions.modular;

import basics.ProgressListener;
import basics.SystemOutProgressListener;
import basics.Tools;
import basics.convexHull.ConvexHullTools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.modular.scoreImage.ScoreImage;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Point;
import java.util.List;
import others.morphological.Morphological;

public class ApproximationMask
{
    private final boolean convexHullLinking = true;
    private final double mainClusterSize = 1.0D;
    private final double tetha_c = 0.0D;
    private final double tetha_rec = 0.25D;
    private final ProgressListener progressListener;
    private ScoreClustering scoreClustering;
    private ImageProcessor imageProcessor;

    public ImageProcessor getImageProcessor()
    {
        return this.imageProcessor;
    }

    public int getWidth() {
        return this.imageProcessor.getWidth();
    }

    public int getHeight() {
        return this.imageProcessor.getHeight();
    }

    public ScoreClustering getScoreClustering() {
        return this.scoreClustering;
    }

    public ApproximationMask(ScoreClustering scoreClustering) {
        this(scoreClustering, new SystemOutProgressListener());
    }

    public ApproximationMask(ScoreClustering scoreClustering, ProgressListener progressListener) {
        this.scoreClustering = scoreClustering;
        this.progressListener = progressListener;
        int width = scoreClustering.getScoreImage().getImageProcessor().getWidth();
        int height = scoreClustering.getScoreImage().getImageProcessor().getHeight();
        int breite = (int)Math.sqrt(width * height);

        int epsilon = scoreClustering.getEpsilon();
        int minPts = scoreClustering.getMinPts();

        this.imageProcessor = new ByteProcessor(width, height);
        this.imageProcessor.setColor(Color.white);

        List clusters = scoreClustering.getClusters();
        for (List cluster : clusters)
        {
            if (cluster.size() / ((List)clusters.get(0)).size() < 1.0D)
            {
                break;
            }
            for (Point p : cluster) {
                List rangePoints = scoreClustering.range(p, epsilon);
                if (rangePoints.size() >= minPts)
                {
                    this.imageProcessor.fillPolygon(ConvexHullTools.get(rangePoints));

                    if (Tools.chance(0.001D)) {
                        progressListener.updateImage(this.imageProcessor);
                    }
                }
            }
        }
        if (DEBUG.getVerbose());
        progressListener.updateImage(this.imageProcessor);

        this.imageProcessor.erode();

        this.imageProcessor = Morphological.close(this.imageProcessor, (int)(0.0D * breite));
        this.imageProcessor = Morphological.dilateByReconstruction(this.imageProcessor, (int)(0.25D * breite));
    }

    public int getMaskPixelCount()
    {
        return this.imageProcessor.getPixelCount() - Tools.getPixelCount(this.imageProcessor, 0);
    }
}