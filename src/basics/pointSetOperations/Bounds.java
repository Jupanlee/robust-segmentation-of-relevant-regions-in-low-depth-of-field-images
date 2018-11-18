//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics.pointSetOperations;

import basics.Tools;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Bounds {
    public Bounds() {
    }

    public static Polygon getX(List<Point> points) {
        List<Point> xSorted = getSortedList(points, new Bounds.CompareX());
        return createPolygonFromPoints(xSorted);
    }

    public static Polygon getY(List<Point> points) {
        List<Point> ySorted = getSortedList(points, new Bounds.CompareY());
        return createPolygonFromPoints(ySorted);
    }

    public static Polygon getNN(List<Point> points) {
        List<Point> ySorted = getSortedList(points, new Bounds.CompareNearestNeighbour(new Point(0, 0)));
        return createPolygonFromPoints(ySorted);
    }

    public static Polygon getIntersection(Polygon a, Polygon b) {
        Polygon intersection = new Polygon();
        Polygon smaller = a.npoints < b.npoints ? a : b;
        Polygon bigger = a.npoints > b.npoints ? a : b;
        Point lastAddedPoint = null;

        for(int i = 0; i < smaller.npoints; ++i) {
            Point pointFromSmaller = new Point(smaller.xpoints[i], smaller.ypoints[i]);
            double dist = lastAddedPoint == null ? 0.0D : Tools.getL2Dist((Point)lastAddedPoint, pointFromSmaller);
            if (bigger.intersects((double)pointFromSmaller.x, (double)pointFromSmaller.y, 1.0D, 1.0D) && dist < 10.0D) {
                System.out.println(dist);
                intersection.addPoint(pointFromSmaller.x, pointFromSmaller.y);
            }
        }

        return intersection;
    }

    private static Polygon createPolygonFromPoints(List<Point> points) {
        Polygon connectedPoints = new Polygon();
        Iterator i$ = points.iterator();

        while(i$.hasNext()) {
            Point point = (Point)i$.next();
            connectedPoints.addPoint(point.x, point.y);
        }

        return connectedPoints;
    }

    private static List<Point> getSortedList(List list, Comparator comparator) {
        List sortedList = new ArrayList(list);
        Collections.sort(sortedList, comparator);
        return sortedList;
    }

    private static class CompareNearestNeighbour implements Comparator<Point> {
        Point neighbour;

        public CompareNearestNeighbour(Point neighbour) {
            this.neighbour = neighbour;
        }

        public int compare(Point o1, Point o2) {
            return (int)Math.round(Tools.getL2Dist(o1, this.neighbour) - Tools.getL2Dist(o2, this.neighbour));
        }
    }

    private static class CompareY implements Comparator<Point> {
        private CompareY() {
        }

        public int compare(Point o1, Point o2) {
            return o1.y - o2.y;
        }
    }

    private static class CompareX implements Comparator<Point> {
        private CompareX() {
        }

        public int compare(Point o1, Point o2) {
            return o1.x - o2.x;
        }
    }
}
