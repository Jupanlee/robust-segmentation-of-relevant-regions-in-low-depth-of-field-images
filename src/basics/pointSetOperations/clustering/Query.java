//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics.pointSetOperations.clustering;

import basics.Tools;
import basics.javaAddons.MQueue;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Point;
import java.util.Iterator;
import java.util.List;

public class Query<SomePoint extends Point> {
    private List<SomePoint> points;
    private boolean[][] pointField;
    private MQueue<Point> rangeResult;

    public Query(List<SomePoint> points) {
        this.points = points;
        int maxX = 0;
        int maxY = 0;
        Iterator i$ = points.iterator();

        Point p;
        while(i$.hasNext()) {
            p = (Point)i$.next();
            if (p.x > maxX) {
                maxX = p.x;
            }

            if (p.y > maxY) {
                maxY = p.y;
            }
        }

        this.rangeResult = new MQueue(points.size());
        this.pointField = new boolean[maxX + 1][maxY + 1];

        for(i$ = points.iterator(); i$.hasNext(); this.pointField[p.x][p.y] = true) {
            p = (Point)i$.next();
        }

    }

    public Point removeFirst() {
        Point p = (Point)this.points.remove(0);
        this.pointField[p.x][p.y] = false;
        return p;
    }

    public MQueue<Point> range(SomePoint queryPoint, double l2distance, boolean includeQueryPoint) {
        this.rangeResult.clear();
        int maxSize = (int)(l2distance + 1.0D);

        for(int x = queryPoint.x - maxSize; x <= queryPoint.x + maxSize; ++x) {
            for(int y = queryPoint.y - maxSize; y <= queryPoint.y + maxSize; ++y) {
                if (this.pointOnField(x, y)) {
                    Point p = new Point(x, y);
                    if (Tools.getL2Dist(p, queryPoint) <= l2distance && (includeQueryPoint || p.x != queryPoint.x || p.y != queryPoint.y)) {
                        this.rangeResult.add(p);
                    }
                }
            }
        }

        return this.rangeResult;
    }

    public MQueue<Point> range(SomePoint queryPoint, double l2distance) {
        return this.range(queryPoint, l2distance, false);
    }

    public Point randomPoint() {
        return (Point)this.points.get((int)(Math.random() * (double)this.points.size()));
    }

    public MQueue<Point> nearestNeighbour(SomePoint point) {
        int max = Math.max(this.pointField.length, this.pointField[0].length);

        for(int epsilon = 1; epsilon < max; ++epsilon) {
            MQueue<Point> neighbours = this.range(point, (double)epsilon);
            if (neighbours.size() > 0) {
                return neighbours;
            }
        }

        return null;
    }

    private boolean pointOnField(int x, int y) {
        return x >= 0 && x < this.pointField.length && y >= 0 && y < this.pointField[0].length ? this.pointField[x][y] : false;
    }

    public static void main(String[] args) {
        ImageProcessor i = new ColorProcessor(400, 400);
        Query q = new Query(Tools.createPoints(400, 400));
        List<Point> points = q.range(new Point(200, 200), 150.0D);
        Tools.drawPoints(points, i, Color.BLUE);
        points = q.range(new Point(200, 200), 50.0D);
        Tools.drawPoints(points, i, Color.RED);
        Tools.save(i);
    }
}
