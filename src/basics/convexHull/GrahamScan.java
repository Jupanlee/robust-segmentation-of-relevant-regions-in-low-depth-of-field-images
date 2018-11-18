package basics.convexHull;

public class GrahamScan
        implements IConvexHull
{
    private Point[] p;
    private int n;
    private int h;

    public int computeHull(Point[] p)
    {
        this.p = p;
        this.n = p.length;
        if (this.n < 3) {
            return this.n;
        }
        this.h = 0;
        grahamScan();
        return this.h;
    }

    private void grahamScan() {
        exchange(0, indexOfLowestPoint());
        Point pl = new Point(this.p[0]);
        makeRelTo(pl);
        sort();
        makeRelTo(pl.reversed());
        int i = 3; int k = 3;
        while (k < this.n) {
            exchange(i, k);
            while (!isConvex(i - 1)) {
                exchange(i - 1, i--);
            }
            k++;
            i++;
        }
        this.h = i;
    }

    private void exchange(int i, int j) {
        Point t = this.p[i];
        this.p[i] = this.p[j];
        this.p[j] = t;
    }

    private void makeRelTo(Point p0)
    {
        Point p1 = new Point(p0);
        for (int i = 0; i < this.n; i++)
            this.p[i].makeRelTo(p1);
    }

    private int indexOfLowestPoint()
    {
        int min = 0;
        for (int i = 1; i < this.n; i++) {
            if ((this.p[i].y < this.p[min].y) || ((this.p[i].y == this.p[min].y) && (this.p[i].x < this.p[min].x))) {
                min = i;
            }
        }
        return min;
    }

    private boolean isConvex(int i) {
        return this.p[i].isConvex(this.p[(i - 1)], this.p[(i + 1)]);
    }

    private void sort() {
        quicksort(1, this.n - 1);
    }

    private void quicksort(int lo, int hi) {
        int i = lo; int j = hi;
        Point q = this.p[((lo + hi) / 2)];
        while (i <= j) {
            while (this.p[i].isLess(q)) {
                i++;
            }
            while (q.isLess(this.p[j])) {
                j--;
            }
            if (i <= j) {
                exchange(i++, j--);
            }
        }
        if (lo < j) {
            quicksort(lo, j);
        }
        if (i < hi)
            quicksort(i, hi);
    }
}