package basics.convexHull;

public class JarvisMarch
        implements IConvexHull
{
    private Point[] p;
    private int n;
    private int h;

    public int computeHull(Point[] p)
    {
        this.p = p;
        this.n = p.length;
        this.h = 0;
        jarvisMarch();
        return this.h;
    }

    private void jarvisMarch() {
        int i = indexOfLowestPoint();
        do {
            exchange(this.h, i);
            i = indexOfRightmostPointFrom(this.p[this.h]);
            this.h += 1;
        }while (i > 0);
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

    private int indexOfRightmostPointFrom(Point q) {
        int i = 0;
        for (int j = 1; j < this.n; j++) {
            if (this.p[j].relTo(q).isLess(this.p[i].relTo(q))) {
                i = j;
            }
        }
        return i;
    }

    private void exchange(int i, int j) {
        Point t = this.p[i];
        this.p[i] = this.p[j];
        this.p[j] = t;
    }
}