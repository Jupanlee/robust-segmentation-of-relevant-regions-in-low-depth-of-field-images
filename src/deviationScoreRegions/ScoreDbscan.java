package deviationScoreRegions;

import basics.pointSetOperations.clustering.Query;
import basics.pointSetOperations.clustering.dbscan.AbstractDBSCANdebuginfos;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.util.List;

public class ScoreDbscan extends AbstractDBSCANdebuginfos
{
    private Query query;
    private ImageProcessor scoreImage = null;
    private double threshold = 1.0D;

    public void setScoreImage(ImageProcessor scoreImage) {
        this.scoreImage = scoreImage;
    }

    public void setScoreThreshold(double threshold) {
        this.threshold = threshold;
    }

    public List<Point> range(Point p, int eps) {
        return this.query.range(p, eps);
    }

    public List<List<Point>> get(List<Point> points, int eps, int MinPts)
    {
        this.query = new Query(points);
        return super.get(points, eps, MinPts);
    }

    protected boolean isCore(Point p)
    {
        List rangePoints = range(p, this.epsilon);
        double sum = 0.0D;
        for (Point rangePoint : rangePoints) {
            double score = this.scoreImage != null ? this.scoreImage.getPixelValue(rangePoint.x, rangePoint.y) : this.threshold;
            sum += Math.min(score / this.threshold, 1.0D);
        }

        return sum >= this.minPts;
    }
}