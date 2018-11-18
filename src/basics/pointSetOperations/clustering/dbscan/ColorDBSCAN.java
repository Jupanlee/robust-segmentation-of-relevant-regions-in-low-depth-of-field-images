//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics.pointSetOperations.clustering.dbscan;

import basics.Tools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.dbscan.Dbscan;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ColorDBSCAN extends Dbscan {
    float[][][] labColorField;
    private ImageProcessor imageProcessor;
    double l_radius;
    double a_radius;
    double b_radius;

    public ColorDBSCAN(ImageProcessor imageProcessor, double l_radius, double a_radius, double b_radius) {
        this.imageProcessor = imageProcessor.duplicate();
        this.l_radius = l_radius;
        this.a_radius = a_radius;
        this.b_radius = b_radius;
    }

    private void initColorField() {
        this.labColorField = new float[this.imageProcessor.getWidth()][this.imageProcessor.getHeight()][3];

        for(int x = 0; x < this.imageProcessor.getWidth(); ++x) {
            for(int y = 0; y < this.imageProcessor.getHeight(); ++y) {
                this.labColorField[x][y] = Tools.getHSV(this.imageProcessor.getPixel(x, y));
            }
        }

    }

    public List<List<Point>> get(int eps, int MinPts) {
        List<Point> points = Tools.createPoints(this.imageProcessor.getWidth(), this.imageProcessor.getHeight());
        this.initColorField();
        this.clusters = super.get(points, eps, MinPts);
        this.mergeNoise();
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
            if (this.similarColor(neighbour, p)) {
                similarColorNeighbours.add(neighbour);
            }
        }

        return similarColorNeighbours;
    }

    private boolean similarColor(Point a, Point b) {
        float[] hsvA = this.labColorField[a.x][a.y];
        float[] hsvB = this.labColorField[b.x][b.y];
        double H_diff = Math.pow((double)(hsvA[0] - hsvB[0]), 2.0D);
        double S_diff = Math.pow((double)(hsvA[1] - hsvB[1]), 2.0D);
        double V_diff = Math.pow((double)(hsvA[2] - hsvB[2]), 2.0D);
        double v = H_diff / this.l_radius + S_diff / this.a_radius + V_diff / this.b_radius;
        return v <= 1.0D;
    }

    public static void main(String[] args) throws IOException {
        Iterator i$ = Tools.getFilesFromDirectory("../data/batch/images/base small", ".jpg").iterator();

        while(i$.hasNext()) {
            String fileName = (String)i$.next();
            ImageProcessor original = Tools.resize(Tools.loadImageProcessor(fileName), 500);
            ImageProcessor ip = original.duplicate().convertToRGB();
            ImageProcessor gray = ip.duplicate().convertToByte(true);
            Tools.fillROI(gray);
            Tools.showImage("window1", original, "processing...", true);
            int epsilon = 10;
            int minPts = epsilon / 2;
            double H_radius = 0.002D;
            double S_radius = 0.002D;
            double V_radius = 0.002D;
            ColorDBSCAN dbscan = new ColorDBSCAN(ip, H_radius, S_radius, V_radius);
            List<List<Point>> clusters = dbscan.get(epsilon, minPts);
            dbscan.mergeNoise();
            Tools.showImage("window3", Tools.drawClusters(clusters, original, true, false), "final", true);
        }

    }
}
