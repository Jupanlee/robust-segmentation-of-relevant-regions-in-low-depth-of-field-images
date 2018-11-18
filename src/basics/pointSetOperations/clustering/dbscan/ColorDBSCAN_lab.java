//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics.pointSetOperations.clustering.dbscan;

import basics.MColor;
import basics.Tools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.dbscan.Dbscan;
import evaluation.Batch.Batchable;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ColorDBSCAN_lab extends Dbscan implements Batchable {
    protected MColor[][] labColorField;
    protected ImageProcessor imageProcessor;
    protected double deltaEToBeSimilar = 5.0D;
    protected double deltaEToBeSimilarToMean = 50.0D;
    protected MColor meanClusterColor;
    protected boolean mergeNoise = true;

    public double getDeltaEToBeSimilar() {
        return this.deltaEToBeSimilar;
    }

    public void setDeltaEToBeSimilar(double deltaEToBeSimilar) {
        this.deltaEToBeSimilar = deltaEToBeSimilar;
    }

    public void setDeltaEToBeSimilarToMean(double deltaEToBeSimilarToMean) {
        this.deltaEToBeSimilarToMean = deltaEToBeSimilarToMean;
    }

    private void initColorField() {
        this.labColorField = new MColor[this.imageProcessor.getWidth()][this.imageProcessor.getHeight()];

        for(int x = 0; x < this.imageProcessor.getWidth(); ++x) {
            for(int y = 0; y < this.imageProcessor.getHeight(); ++y) {
                this.labColorField[x][y] = new MColor(this.imageProcessor.getPixel(x, y));
            }
        }

    }

    public ColorDBSCAN_lab() {
    }

    public ColorDBSCAN_lab(int minPts, int epsilon) {
        this.minPts = minPts;
        this.epsilon = epsilon;
    }

    public List<List<Point>> get(ImageProcessor imageProcessor, int eps, int MinPts) {
        this.imageProcessor = imageProcessor;
        this.points = Tools.createPoints(imageProcessor.getWidth(), imageProcessor.getHeight());
        this.initColorField();
        this.clusters = super.get(this.points, eps, MinPts);
        if (this.mergeNoise) {
            this.mergeNoise();
        }

        return this.clusters;
    }

    private void mergeNoise() {
        int width = this.imageProcessor.getWidth();
        int height = this.imageProcessor.getHeight();

        for(int x = 0; x < width; ++x) {
            label65:
            for(int y = 0; y < height; ++y) {
                if (this.clusterField[x][y] == -1) {
                    if ((x + 1) * (y + 1) % 1000 == 0) {
                        DEBUG.println((x + 1) * (y + 1) + " / " + this.imageProcessor.getPixelCount());
                    }

                    int size = 0;
                    boolean neighborClusterFound = false;

                    while(true) {
                        while(true) {
                            if (neighborClusterFound) {
                                continue label65;
                            }

                            Point noisePoint = new Point(x, y);
                            ++size;
                            Point p = new Point(x, y);
                            p.x -= size;
                            p.y -= size;
                            int umfang = size * 8;

                            for(int i = 0; i < umfang; ++i) {
                                int kante = size * 2;
                                if (i < kante) {
                                    ++p.x;
                                } else if (i < kante * 2) {
                                    ++p.y;
                                } else if (i < kante * 3) {
                                    --p.x;
                                } else {
                                    --p.y;
                                }

                                if (p.x > 0 && p.y > 0 && p.x < width && p.y < height) {
                                    int clusterID = this.clusterField[p.x][p.y];
                                    if (clusterID != -1) {
                                        List<Point> cluster = (List)this.clusters.get(clusterID);
                                        cluster.add(noisePoint);
                                        neighborClusterFound = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public List<Point> range(Point p, int epsilon) {
        List<Point> neighbours = super.range(p, epsilon);
        List<Point> similarColorNeighbours = new ArrayList();
        Iterator i$ = neighbours.iterator();

        while(i$.hasNext()) {
            Point neighbour = (Point)i$.next();
            if (this.similar(neighbour, p)) {
                similarColorNeighbours.add(neighbour);
            }
        }

        return similarColorNeighbours;
    }

    protected boolean similar(Point neighbour, Point corePoint) {
        double distanceToCorePoint = this.labColorField[neighbour.x][neighbour.y].getLabDeltaE(this.labColorField[corePoint.x][corePoint.y]);
        double distanceToMean = this.meanClusterColor == null ? 0.0D : this.labColorField[neighbour.x][neighbour.y].getLabDeltaE(this.meanClusterColor);
        return distanceToCorePoint <= this.deltaEToBeSimilar && distanceToMean <= this.deltaEToBeSimilarToMean;
    }

    protected List<Point> startNewCluster(Point point) {
        this.meanClusterColor = this.labColorField[point.x][point.y];
        return super.startNewCluster(point);
    }

    public void finishedCurrentCluster() {
        this.meanClusterColor = null;
        super.finishedCurrentCluster();
    }

    protected void addPointToCluster(List<Point> cluster, Point p) {
        this.meanClusterColor.add(this.labColorField[p.x][p.y]);
        super.addPointToCluster(cluster, p);
    }

    public ImageProcessor run(ImageProcessor original) {
        this.clusters = this.get(original, this.epsilon, this.minPts);
        Tools.save(Tools.drawClusters(this.clusters, original, true, false));
        Tools.save(Tools.drawClusters(this.clusters, original, false, false));
        return original;
    }

    public static void main(String[] args) throws IOException {
        Iterator i$ = Tools.getFilesFromDirectory("../../images/flickr_cc", ".jpg").iterator();

        while(i$.hasNext()) {
            String fileName = (String)i$.next();
            ImageProcessor imageProcessor = Tools.loadImageProcessor(fileName, 800);
            Tools.save(imageProcessor);
            double deltaE = 5.0D;
            int epsilon = 1;
            int minPts = (int)Math.round(Math.pow((double)(epsilon + 1), 2.0D) * 0.75D);
            ColorDBSCAN_lab colorDbscan = new ColorDBSCAN_lab(minPts, epsilon);
            colorDbscan.setDeltaEToBeSimilar(deltaE);
            colorDbscan.deltaEToBeSimilarToMean = 250.0D;
            colorDbscan.mergeNoise = false;
            colorDbscan.run(imageProcessor);
        }

    }
}
