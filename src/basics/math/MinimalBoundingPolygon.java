//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics.math;

import basics.Tools;
import basics.javaAddons.MQueue;
import basics.pointSetOperations.clustering.Query;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

public class MinimalBoundingPolygon {
    public MinimalBoundingPolygon() {
    }

    public static Polygon get(List<Point> points) {
        boolean[][] field = Tools.pointsToFieldArray(points);
        int width = field.length;
        int height = field[0].length;
        MQueue<Point> contourPoints = new MQueue(width * height);

        int i;
        for(i = 0; i < width; ++i) {
            checkField(field, new Point(i, 0), new Point(0, 1), contourPoints);
        }

        for(i = 0; i < width; ++i) {
            checkField(field, new Point(i, height - 1), new Point(0, -1), contourPoints);
        }

        for(i = 0; i < height; ++i) {
            checkField(field, new Point(0, i), new Point(1, 0), contourPoints);
        }

        for(i = 0; i < height; ++i) {
            checkField(field, new Point(width - 1, i), new Point(-1, 0), contourPoints);
        }

        Polygon polygon = new Polygon();
        Query query = new Query(contourPoints);
        List<Point> seeds = new ArrayList();
        seeds.add(query.randomPoint());

        while(!seeds.isEmpty()) {
            Point seed = (Point)seeds.remove(0);
            List<Point> neighbours = query.nearestNeighbour(seed);
            if (neighbours != null && neighbours.size() > 0) {
                Point neighbour = (Point)neighbours.get(0);
                polygon.addPoint(neighbour.x, neighbour.y);
                seeds.add(neighbour);
            }
        }

        return polygon;
    }

    public static List<Point> getPoints(List<Point> points) {
        boolean[][] field = Tools.pointsToFieldArray(points);
        int width = field.length;
        int height = field[0].length;
        MQueue<Point> contourPoints = new MQueue(width * height);

        int i;
        for(i = 0; i < width; ++i) {
            checkField(field, new Point(i, 0), new Point(0, 1), contourPoints);
        }

        for(i = 0; i < width; ++i) {
            checkField(field, new Point(i, height - 1), new Point(0, -1), contourPoints);
        }

        for(i = 0; i < height; ++i) {
            checkField(field, new Point(0, i), new Point(1, 0), contourPoints);
        }

        for(i = 0; i < height; ++i) {
            checkField(field, new Point(width - 1, i), new Point(-1, 0), contourPoints);
        }

        List<Point> result = new ArrayList();
        Query query = new Query(contourPoints);
        List<Point> seeds = new ArrayList();
        seeds.add(query.randomPoint());

        while(!seeds.isEmpty()) {
            Point seed = (Point)seeds.remove(0);
            List<Point> neighbours = query.nearestNeighbour(seed);
            if (neighbours != null && neighbours.size() > 0) {
                Point neighbour = (Point)neighbours.get(0);
                result.add(neighbour);
                seeds.add(neighbour);
            }
        }

        return result;
    }

    private static void checkField(boolean[][] field, Point start, Point directionVector, MQueue<Point> p) {
        Point current = new Point(start);

        boolean insidefield;
        for(insidefield = true; insidefield && !field[current.x][current.y]; insidefield = current.x >= 0 && current.y >= 0 && current.x < field.length && current.y < field[0].length) {
            current.translate(directionVector.x, directionVector.y);
        }

        if (insidefield) {
            p.add(current);
        }

    }
}
