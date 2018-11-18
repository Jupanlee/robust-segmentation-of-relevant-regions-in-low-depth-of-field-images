package basics.pointSetOperations.clustering.dbscan;

import basics.ProSet;
import basics.Tools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.dbscan.AbstractDBSCAN;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import java.util.Random;

public abstract class AbstractDBSCANdebuginfos extends AbstractDBSCAN
{
    private int count;
    private int maxCount;
    public static boolean debuginfos = false;
    public ImageProcessor debugImageProcessor = null;
    private ProSet<Point> visited = new ProSet();
    private Point processingPoint = null;

    protected void showDebugInfos()
    {
        if (debuginfos) {
            this.debugImageProcessor = Tools.drawClusters(this.clusters, new ColorProcessor(this.dimension.width, this.dimension.height), false, false, new Random(123L));

            if (this.currentClusterFather != null) {
                this.debugImageProcessor.putPixel(this.currentClusterFather.x, this.currentClusterFather.y, Color.RED.getRGB());
            }
            if (this.processingPoint != null) {
                this.debugImageProcessor.putPixel(this.processingPoint.x, this.processingPoint.y, Color.magenta.getRGB());
            }

            Tools.save(this.debugImageProcessor);
        }
    }

    protected void setVisited(Point p)
    {
        super.setVisited(p);
        this.visited.add(p);

        this.count -= 1;
        if ((this.maxCount / 10 > 0) && (this.count % (this.maxCount / 10) == 0)) DEBUG.print(".");

        showDebugInfos();
    }

    protected void processingPoint(Point p)
    {
        super.processingPoint(p);
        this.processingPoint = p;
        showDebugInfos();
    }

    protected void addUnvisitedPointsToSeedList(List<Point> source)
    {
        super.addUnvisitedPointsToSeedList(source);
        showDebugInfos();
    }

    public List<List<Point>> get(List<Point> points, int eps, int MinPts)
    {
        this.count = points.size();
        this.maxCount = points.size();
        super.get(points, eps, MinPts);
        if (DEBUG.getVerbose()) {
            DEBUG.println("found " + this.clusters.size() + " clusters.");
            this.debugImageProcessor = new ColorProcessor(this.dimension.width, this.dimension.height);
            this.debugImageProcessor = Tools.drawClusters(this.clusters, this.debugImageProcessor, false, false);
        }

        return this.clusters;
    }
}