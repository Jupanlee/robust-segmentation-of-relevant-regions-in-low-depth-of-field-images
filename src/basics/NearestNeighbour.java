//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics;

import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NearestNeighbour {
    int width;
    int height;
    boolean[][] pointField;

    public NearestNeighbour(Collection<Point> points, int width, int height) {
        this.width = width;
        this.height = height;
        this.pointField = new boolean[width][height];

        Point p;
        for(Iterator i$ = points.iterator(); i$.hasNext(); this.pointField[p.x][p.y] = true) {
            p = (Point)i$.next();
        }

    }

    public Point getNearestNeighbour(Point p) {
        Set<Point> neighbours = this.getNeighbours(p, 1, Math.max(this.width, this.height));
        return neighbours != null && neighbours.size() != 0 ? (Point)neighbours.iterator().next() : null;
    }

    public Set<Point> getNeighbours(Point p, int neighbourCount, int maxSearchRadius) {
        Set<Point> neighbours = new HashSet();
        int size = 0;

        while(size < maxSearchRadius) {
            ++size;
            Point point = new Point(p);
            point.x -= size;
            point.y -= size;
            int umfang = size * 8;

            for(int i = 0; i < umfang; ++i) {
                int kante = size * 2;
                if (i < kante) {
                    ++point.x;
                } else if (i < kante * 2) {
                    ++point.y;
                } else if (i < kante * 3) {
                    --point.x;
                } else {
                    --point.y;
                }

                if (point.x >= 0 && point.y >= 0 && point.x < this.width && point.y < this.height && this.pointField[point.x][point.y]) {
                    neighbours.add(new Point(point));
                    if (neighbours.size() == neighbourCount) {
                        return neighbours;
                    }
                }
            }
        }

        return neighbours;
    }
}
