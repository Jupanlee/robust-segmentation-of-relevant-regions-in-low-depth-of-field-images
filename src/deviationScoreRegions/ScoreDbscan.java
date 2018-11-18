//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions;

import basics.pointSetOperations.clustering.Query;
import basics.pointSetOperations.clustering.dbscan.AbstractDBSCANdebuginfos;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.util.Iterator;
import java.util.List;

public class ScoreDbscan extends AbstractDBSCANdebuginfos {
    private Query query;
    private ImageProcessor scoreImage = null;
    private double threshold = 1.0D;

    public ScoreDbscan() {
    }

    public void setScoreImage(ImageProcessor scoreImage) {
        this.scoreImage = scoreImage;
    }

    public void setScoreThreshold(double threshold) {
        this.threshold = threshold;
    }

    public List<Point> range(Point p, int eps) {
        return this.query.range(p, (double)eps);
    }

    public List<List<Point>> get(List<Point> points, int eps, int MinPts) {
        this.query = new Query(points);
        return super.get(points, eps, MinPts);
    }

    protected boolean isCore(Point p) {
        List<Point> rangePoints = this.range(p, this.epsilon);
        double sum = 0.0D;

        double score;
        for(Iterator i$ = rangePoints.iterator(); i$.hasNext(); sum += Math.min(score / this.threshold, 1.0D)) {
            Point rangePoint = (Point)i$.next();
            score = this.scoreImage != null ? (double)this.scoreImage.getPixelValue(rangePoint.x, rangePoint.y) : this.threshold;
        }

        return sum >= (double)this.minPts;
    }
}
