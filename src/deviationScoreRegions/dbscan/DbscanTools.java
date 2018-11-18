//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions.dbscan;

import basics.Tools;
import java.awt.Point;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class DbscanTools {
    public DbscanTools() {
    }

    public static void createClusterFromConnectedNoise(Dbscan dbscan, int noiseEpsilon, int noiseMinPts) {
        List<List<Point>> noiseClusters = (new Dbscan()).get(dbscan.noise, noiseEpsilon, noiseMinPts);
        System.out.println("found " + noiseClusters.size() + " noiseClusters");
        Iterator i$ = noiseClusters.iterator();

        while(i$.hasNext()) {
            List<Point> noiseCluster = (List)i$.next();
            dbscan.clusters.add(noiseCluster);
        }

    }

    public static void mergeNoise(Dbscan dbscan, int maxSearchRadius) {
        int[][] clusterFieldNew = Tools.duplicateArray(dbscan.clusterField);
        int width = dbscan.dimension.width;
        int height = dbscan.dimension.height;

        for(int x = 0; x < width; ++x) {
            label64:
            for(int y = 0; y < height; ++y) {
                if (dbscan.clusterField[x][y] == -1) {
                    int size = 0;
                    boolean neighborClusterFound = false;

                    while(true) {
                        while(true) {
                            if (neighborClusterFound || size >= maxSearchRadius) {
                                continue label64;
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
                                    int clusterID = dbscan.clusterField[p.x][p.y];
                                    if (clusterID != -1) {
                                        List<Point> cluster = (List)dbscan.clusters.get(clusterID);
                                        cluster.add(noisePoint);
                                        dbscan.noise.remove(noisePoint);
                                        neighborClusterFound = true;
                                        clusterFieldNew[x][y] = clusterID;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        dbscan.clusterField = clusterFieldNew;
    }

    public static void sortBySiye(List<List<Point>> clusters) {
        Collections.sort(clusters, new DbscanTools.ClusterSizeComparator());
    }

    private static class ClusterSizeComparator implements Comparator<List<Point>> {
        private ClusterSizeComparator() {
        }

        public int compare(List<Point> cluster1, List<Point> cluster2) {
            return cluster2.size() - cluster1.size();
        }
    }
}
