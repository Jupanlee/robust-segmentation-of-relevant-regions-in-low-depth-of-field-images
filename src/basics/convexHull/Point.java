package basics.convexHull;

public class Point
{
    public double x;
    public double y;

    public Point(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public Point(Point p) {
        this(p.x, p.y);
    }

    public Point relTo(Point p) {
        return new Point(this.x - p.x, this.y - p.y);
    }

    public void makeRelTo(Point p) {
        this.x -= p.x;
        this.y -= p.y;
    }

    public Point moved(double x0, double y0) {
        return new Point(this.x + x0, this.y + y0);
    }

    public Point reversed() {
        return new Point(-this.x, -this.y);
    }

    public boolean isLower(Point p) {
        return (this.y < p.y) || ((this.y == p.y) && (this.x < p.x));
    }

    public double mdist()
    {
        return Math.abs(this.x) + Math.abs(this.y);
    }

    public double mdist(Point p) {
        return relTo(p).mdist();
    }

    public boolean isFurther(Point p) {
        return mdist() > p.mdist();
    }

    public boolean isBetween(Point p0, Point p1) {
        return p0.mdist(p1) >= mdist(p0) + mdist(p1);
    }

    public double cross(Point p) {
        return this.x * p.y - p.x * this.y;
    }

    public boolean isLess(Point p) {
        double f = cross(p);
        return (f > 0.0D) || ((f == 0.0D) && (isFurther(p)));
    }

    public double area2(Point p0, Point p1) {
        return p0.relTo(this).cross(p1.relTo(this));
    }

    public double area2(Line g) {
        return area2(g.p0, g.p1);
    }

    public boolean isRightOf(Line g) {
        return area2(g) < 0.0D;
    }

    public boolean isConvex(Point p0, Point p1) {
        double f = area2(p0, p1);
        return (f < 0.0D) || ((f == 0.0D) && (!isBetween(p0, p1)));
    }
}