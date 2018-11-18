//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions.dbscan;

import basics.javaAddons.DEBUG;
import basics.javaAddons.MQueue;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractDBSCAN {
    protected boolean[][] visited;
    protected boolean[][] clustered;
    private boolean[][] inSeedList;
    protected int[][] clusterField;
    protected List<List<Point>> clusters;
    protected List<Point> noise = new ArrayList();
    protected Point currentClusterFather = null;
    protected Dimension dimension;
    protected MQueue<Point> seedList;
    protected int epsilon;
    protected int minPts;
    protected List<Point> points;

    public AbstractDBSCAN() {
    }

    public abstract List<Point> range(Point var1, int var2);

    protected void setVisited(Point p) {
        this.visited[p.x][p.y] = true;
    }

    protected boolean isCore(Point p) {
        return this.range(p, this.epsilon).size() >= this.minPts;
    }

    protected void processingPoint(Point p) {
    }

    public List<Point> getNoise() {
        return this.noise;
    }

    public void get(List<Point> points, int eps, int MinPts, List<List<Point>> result) {
        this.epsilon = eps;
        this.minPts = MinPts;
        this.points = points;
        DEBUG.print("Processing " + points.size() + " pixels (eps = " + eps + ", minPts = " + MinPts + ") ");
        int maxX = -2147483648;
        int maxY = -2147483648;
        Iterator i$ = points.iterator();

        while(i$.hasNext()) {
            Point p = (Point)i$.next();
            if (p.x > maxX) {
                maxX = p.x;
            }

            if (p.y > maxY) {
                maxY = p.y;
            }
        }

        this.dimension = new Dimension(maxX, maxY);
        ++this.dimension.height;
        ++this.dimension.width;
        if (this.dimension.width > 0 && this.dimension.height > 0) {
            int max = Math.max(this.dimension.height, this.dimension.width);
            this.seedList = new MQueue((int)Math.pow((double)max, 2.5D));
            this.visited = new boolean[this.dimension.width][this.dimension.height];
            this.clustered = new boolean[this.dimension.width][this.dimension.height];
            this.inSeedList = new boolean[this.dimension.width][this.dimension.height];
            this.clusterField = new int[this.dimension.width][this.dimension.height];
            Iterator it = points.iterator();

            while(true) {
                while(true) {
                    Point point;
                    do {
                        if (!it.hasNext()) {
                            DEBUG.println(" DONE");
                            return;
                        }

                        point = (Point)it.next();
                    } while(this.visited[point.x][point.y]);

                    this.processingPoint(point);
                    this.setVisited(point);
                    this.currentClusterFather = null;
                    if (this.isCore(point)) {
                        List<Point> currentCluster = this.startNewCluster(point);
                        this.seedList.clear();
                        this.seedList.addAll(this.range(point, eps));

                        while(!this.seedList.isEmpty()) {
                            Point seedPoint = (Point)this.seedList.removeFirst();
                            this.processingPoint(seedPoint);
                            if (!this.visited[seedPoint.x][seedPoint.y]) {
                                this.setVisited(seedPoint);
                                this.addUnvisitedCoreNeighbourhood(seedPoint, this.seedList);
                            }

                            if (!this.clustered[seedPoint.x][seedPoint.y]) {
                                this.addPointToCluster(currentCluster, seedPoint);
                            }
                        }

                        this.finishedCurrentCluster();
                    } else {
                        this.noise.add(point);
                        this.clusterField[point.x][point.y] = -1;
                        this.setVisited(point);
                    }
                }
            }
        }
    }

    public void finishedCurrentCluster() {
    }

    public List<List<Point>> get(List<Point> points, int eps, int MinPts) {
        this.clusters = new ArrayList();
        this.get(points, eps, MinPts, this.clusters);
        return this.clusters;
    }

    protected List<Point> startNewCluster(Point point) {
        List<Point> currentCluster = new ArrayList();
        this.clusters.add(currentCluster);
        this.addPointToCluster(currentCluster, point);
        this.currentClusterFather = point;
        return currentCluster;
    }

    private void addUnvisitedCoreNeighbourhood(Point corePoint, List<Point> destination) {
        this.processingPoint(corePoint);
        List<Point> neighbours = this.range(corePoint, this.epsilon);
        if (neighbours.size() >= this.minPts) {
            this.addUnvisitedPointsToSeedList(neighbours);
        }

    }

    protected List<Point> getCurrentProcessedCluster() {
        return (List)this.clusters.get(this.clusters.size() - 1);
    }

    protected void addUnvisitedPointsToSeedList(List<Point> source) {
        Iterator i$ = source.iterator();

        while(i$.hasNext()) {
            Point p = (Point)i$.next();
            if (!this.visited[p.x][p.y] && !this.inSeedList[p.x][p.y]) {
                this.seedList.add(p);
                this.inSeedList[p.x][p.y] = true;
            }
        }

    }

    protected void addPointToCluster(List<Point> C, Point p) {
        C.add(p);
        this.clustered[p.x][p.y] = true;
        this.clusterField[p.x][p.y] = this.clusters.size() - 1;
    }
}
