package basics;

import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NearestNeighbour
{
    int width;
    int height;
    boolean[][] pointField;

    public NearestNeighbour(Collection<Point> points, int width, int height)
    {
        this.width = width;
        this.height = height;
        this.pointField = new boolean[width][height];
        for (Point p : points)
            this.pointField[p.x][p.y] = 1;
    }

    public Point getNearestNeighbour(Point p)
    {
        Set neighbours = getNeighbours(p, 1, Math.max(this.width, this.height));
        if ((neighbours == null) || (neighbours.size() == 0)) {
            return null;
        }
        return (Point)neighbours.iterator().next();
    }

    public Set<Point> getNeighbours(Point p, int neighbourCount, int maxSearchRadius)
    {
        Set neighbours = new HashSet();

        int size = 0;
        while (size < maxSearchRadius)
        {
            size++;
            Point point = new Point(p);
            point.x -= size;
            point.y -= size;

            int umfang = size * 8;
            for (int i = 0; i < umfang; i++) {
                int kante = size * 2;
                if (i < kante) point.x += 1;
                else if (i < kante * 2) point.y += 1;
                else if (i < kante * 3) point.x -= 1; else {
                    point.y -= 1;
                }
                if ((point.x < 0) || (point.y < 0) || (point.x >= this.width) || (point.y >= this.height))
                    continue;
                if (this.pointField[point.x][point.y] != 0) {
                    neighbours.add(new Point(point));
                    if (neighbours.size() == neighbourCount) return neighbours;
                }
            }

        }

        return neighbours;
    }
}