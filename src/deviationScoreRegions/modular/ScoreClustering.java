package deviationScoreRegions.modular;

import basics.Tools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.ScoreDbscan;
import deviationScoreRegions.dbscan.DbscanTools;
import deviationScoreRegions.modular.scoreImage.ScoreImage;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ScoreClustering extends ScoreDbscan
{
    private final double scoreClusteringThreshold = 255.0D;
    private final double tetha_epsilon = 0.025D;
    private final int tetha_score = 30;
    private ScoreImage scoreImage;
    private List<Point> scorePoints;
    private List<Point> coreScorePoints;
    private double density;

    public List<Point> getScorePoints()
    {
        return this.scorePoints;
    }

    public List<Point> getCoreScorePoints() {
        return this.coreScorePoints;
    }

    public List<List<Point>> getClusters() {
        return this.clusters;
    }

    public ScoreImage getScoreImage() {
        return this.scoreImage;
    }

    public int getEpsilon() {
        return this.epsilon;
    }

    public int getMinPts() {
        return this.minPts;
    }

    private double calcScorePointDensity(ScoreImage scoreImage)
    {
        double sum = 0.0D;
        for (Point p : this.scorePoints) {
            sum += Math.min(scoreImage.getImageProcessor().getPixelValue(p.x, p.y) / 255.0D, 1.0D);
        }
        return sum / scoreImage.getImageProcessor().getPixelCount();
    }

    protected boolean isCore(Point p)
    {
        boolean isCore = super.isCore(p);
        if (isCore) {
            this.coreScorePoints.add(p);
        }
        return isCore;
    }

    public ScoreClustering(ScoreImage scoreImage)
    {
        setScoreImage(scoreImage.getImageProcessor());
        setScoreThreshold(255.0D);

        this.scoreImage = scoreImage;
        this.scorePoints = Tools.imageProcessorToPoints(scoreImage.getImageProcessor(), 30);
        this.density = calcScorePointDensity(scoreImage);
        this.minPts = ((int)Math.round(Math.pow(1.025D, 2.0D) * this.density) + 1);
        this.epsilon = (int)Math.round(0.025D * Math.sqrt(scoreImage.getImageProcessor().getPixelCount()));
        this.coreScorePoints = new ArrayList();
        this.clusters = get(this.scorePoints, this.epsilon, this.minPts);
        if (DEBUG.getVerbose()) {
            System.out.println("epsilon = 0.025 scorePointDensity = " + this.density + " minPts = " + this.minPts);
            String str = "";
            Tools.save(Tools.write(str, Tools.drawClusters(this.clusters, new ColorProcessor(scoreImage.getImageProcessor().getWidth(), scoreImage.getImageProcessor().getHeight()), false, true)));
        }

        DbscanTools.sortBySiye(this.clusters);
    }
}