package deviationScoreRegions.grow;

import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ColorRegion
{
    private List<Point> points = new LinkedList();
    private Set<Point> outline = new HashSet();

    public void addOutlinePixel(Point p) {
        this.outline.add(p);
    }

    public void add(Point p) {
        this.points.add(p);
    }

    public List<Point> getPixels() {
        return this.points;
    }

    public int size() {
        return this.points.size();
    }

    public void clear() {
        this.points.clear();
    }

    public Set<Point> getOutline() {
        return this.outline;
    }

    public ColorRegion()
    {
    }

    public ColorRegion(Collection<? extends Point> c) {
        this.points.addAll(c);
    }
}