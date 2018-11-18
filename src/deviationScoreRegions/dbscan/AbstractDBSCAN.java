package deviationScoreRegions.dbscan;

import basics.javaAddons.DEBUG;
import basics.javaAddons.MQueue;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDBSCAN
{
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

    public abstract List<Point> range(Point paramPoint, int paramInt);

    protected void setVisited(Point p)
    {
        this.visited[p.x][p.y] = 1;
    }

    protected boolean isCore(Point p)
    {
        return range(p, this.epsilon).size() >= this.minPts;
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
        for (Point p : points) {
            if (p.x > maxX) maxX = p.x;
            if (p.y > maxY) maxY = p.y;
        }

        this.dimension = new Dimension(maxX, maxY);

        this.dimension.height += 1;
        this.dimension.width += 1;

        if ((this.dimension.width <= 0) || (this.dimension.height <= 0)) return;

        int max = Math.max(this.dimension.height, this.dimension.width);
        this.seedList = new MQueue((int)Math.pow(max, 2.5D));

        this.visited = new boolean[this.dimension.width][this.dimension.height];
        this.clustered = new boolean[this.dimension.width][this.dimension.height];
        this.inSeedList = new boolean[this.dimension.width][this.dimension.height];
        this.clusterField = new int[this.dimension.width][this.dimension.height];

        for (Point point : points) if (this.visited[point.x][point.y] == 0) {
            processingPoint(point);
            setVisited(point);
            this.currentClusterFather = null;

            if (isCore(point))
            {
                List currentCluster = startNewCluster(point);

                this.seedList.clear();
                this.seedList.addAll(range(point, eps));

                while (!this.seedList.isEmpty()) {
                    Point seedPoint = (Point)this.seedList.removeFirst();
                    processingPoint(seedPoint);

                    if (this.visited[seedPoint.x][seedPoint.y] == 0) {
                        setVisited(seedPoint);
                        addUnvisitedCoreNeighbourhood(seedPoint, this.seedList);
                    }

                    if (this.clustered[seedPoint.x][seedPoint.y] == 0) {
                        addPointToCluster(currentCluster, seedPoint);
                    }

                }

                finishedCurrentCluster();
            }
            else {
                this.noise.add(point);
                this.clusterField[point.x][point.y] = -1;
                setVisited(point);
            }
        }

        DEBUG.println(" DONE");
    }

    public void finishedCurrentCluster() {
    }

    public List<List<Point>> get(List<Point> points, int eps, int MinPts) {
        this.clusters = new ArrayList();
        get(points, eps, MinPts, this.clusters);
        return this.clusters;
    }

    protected List<Point> startNewCluster(Point point)
    {
        List currentCluster = new ArrayList();
        this.clusters.add(currentCluster);
        addPointToCluster(currentCluster, point);
        this.currentClusterFather = point;
        return currentCluster;
    }

    private void addUnvisitedCoreNeighbourhood(Point corePoint, List<Point> destination) {
        processingPoint(corePoint);

        List neighbours = range(corePoint, this.epsilon);
        if (neighbours.size() >= this.minPts)
            addUnvisitedPointsToSeedList(neighbours);
    }

    protected List<Point> getCurrentProcessedCluster()
    {
        return (List)this.clusters.get(this.clusters.size() - 1);
    }

    protected void addUnvisitedPointsToSeedList(List<Point> source) {
        for (Point p : source)
            if ((this.visited[p.x][p.y] == 0) && (this.inSeedList[p.x][p.y] == 0)) {
                this.seedList.add(p);
                this.inSeedList[p.x][p.y] = 1;
            }
    }

    protected void addPointToCluster(List<Point> C, Point p)
    {
        C.add(p);
        this.clustered[p.x][p.y] = 1;
        this.clusterField[p.x][p.y] = (this.clusters.size() - 1);
    }
}