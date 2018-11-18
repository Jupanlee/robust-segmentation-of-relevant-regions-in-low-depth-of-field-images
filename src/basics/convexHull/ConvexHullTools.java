package basics.convexHull;

import basics.Tools;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import java.awt.Polygon;
import java.util.List;

public class ConvexHullTools
{
    public static Polygon get(List<java.awt.Point> points)
    {
        Roi roi = new PolygonRoi(Tools.newPolygon(points, false), 6);
        return roi.getConvexHull();
    }

    public static Polygon oldGet(IConvexHull convexHull, List<java.awt.Point> points)
    {
        Point[] newPoints = new Point[points.size()];
        for (int i = 0; i < points.size(); i++) {
            java.awt.Point p = (java.awt.Point)points.get(i);
            newPoints[i] = new Point(p.x, p.y);
        }
        convexHull.computeHull(newPoints);

        Polygon polygon = new Polygon();
        for (Point p : newPoints) {
            polygon.addPoint((int)p.x, (int)p.y);
        }

        return polygon;
    }
}