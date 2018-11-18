//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics.pointSetOperations;

import basics.Tools;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Growing {
    public Growing() {
    }

    public static List<List<Point>> grow8Neighbourhood(int width, int height, Growing.Growable growable) {
        List<List<Point>> regions = new ArrayList();
        boolean[][] visited = Tools.newBoolArray2d(width, height, false);

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                if (!visited[x][y]) {
                    List<Point> newRegion = new ArrayList();
                    List<Point> seeds = new ArrayList();
                    seeds.add(new Point(x, y));
                    growable.newRegionAction(new Point(x, y));
                    Point lastGrowingPoint = new Point(x, y);

                    while(!seeds.isEmpty()) {
                        Point growToPoint = (Point)seeds.remove(0);
                        if (!visited[growToPoint.x][growToPoint.y]) {
                            visited[growToPoint.x][growToPoint.y] = true;
                            if (growable.canGrow(lastGrowingPoint, growToPoint)) {
                                seeds.add(growToPoint);
                                newRegion.add(growToPoint);
                                lastGrowingPoint = growToPoint;
                                growable.newGrowedPointAction(growToPoint);
                                seeds.addAll(growable.grow(growToPoint));
                            }
                        }
                    }

                    regions.add(newRegion);
                }
            }
        }

        return regions;
    }

    public interface Growable {
        boolean canGrow(Point var1, Point var2);

        void newRegionAction(Point var1);

        void newGrowedPointAction(Point var1);

        List<Point> grow(Point var1);
    }
}
