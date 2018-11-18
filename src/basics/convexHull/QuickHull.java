package basics.convexHull;

public class QuickHull
        implements IConvexHull
{
    private Point[] p;
    private int n;
    private int h;
    private static final double eps = 0.001D;

    public int computeHull(Point[] p_)
    {
        this.p = p_;
        this.n = this.p.length;
        this.h = 0;
        quickHull();
        return this.h;
    }

    private void quickHull() {
        exchange(0, indexOfLowestPoint());
        this.h += 1;
        Line g = new Line(this.p[0], this.p[0].moved(-0.001D, 0.0D));
        computeHullPoints(g, 1, this.n - 1);
    }

    private void computeHullPoints(Line g, int lo, int hi) {
        if (lo > hi) {
            return;
        }
        int k = indexOfFurthestPoint(g, lo, hi);
        Line g0 = new Line(g.p0, this.p[k]);
        Line g1 = new Line(this.p[k], g.p1);
        exchange(k, hi);

        int i = partition(g0, lo, hi - 1);

        computeHullPoints(g0, lo, i - 1);

        exchange(hi, i);
        exchange(i, this.h);
        this.h += 1;

        int j = partition(g1, i + 1, hi);

        computeHullPoints(g1, i + 1, j - 1);
    }

    private int indexOfLowestPoint() {
        int min = 0;
        for (int i = 1; i < this.n; i++) {
            if ((this.p[i].y < this.p[min].y) || ((this.p[i].y == this.p[min].y) && (this.p[i].x < this.p[min].x))) {
                min = i;
            }
        }
        return min;
    }

    private void exchange(int i, int j) {
        Point t = this.p[i];
        this.p[i] = this.p[j];
        this.p[j] = t;
    }

    private int indexOfFurthestPoint(Line g, int lo, int hi) {
        int f = lo;
        double mx = 0.0D;
        for (int i = lo; i <= hi; i++) {
            double d = -this.p[i].area2(g);
            if ((d > mx) || ((d == mx) && (this.p[i].x > this.p[f].x))) {
                mx = d;
                f = i;
            }
        }
        return f;
    }

    private int partition(Line g, int lo, int hi) {
        int i = lo; int j = hi;
        while (i <= j) {
            while ((i <= j) && (this.p[i].isRightOf(g))) {
                i++;
            }
            while ((i <= j) && (!this.p[j].isRightOf(g))) {
                j--;
            }
            if (i <= j) {
                exchange(i++, j--);
            }
        }
        return i;
    }
}