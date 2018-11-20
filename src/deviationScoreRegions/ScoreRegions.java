//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions;

import basics.Tools;
import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ScoreRegions {
    double[][] scores;
    double[][] neighbourSocres;
    List<ScoreRegion> regionsList;
    ScoreRegion[][] regionField;
    int width;
    int height;
    int todoCount;
    int neighbourDistance;

    public <PointList extends List<Point>> ScoreRegions(double[][] scores, List<PointList> clusters, int neighbourDistance) {
        this.scores = scores;
        this.width = scores.length;
        this.height = scores[0].length;
        this.regionsList = new LinkedList();
        this.regionField = new ScoreRegion[this.width][this.height];
        Tools.fill(this.regionField, (Object)null);
        this.todoCount = clusters.size();
        this.neighbourDistance = neighbourDistance;
        Iterator i$ = clusters.iterator();

        while(i$.hasNext()) {
            Collection<Point> cluster = (List)i$.next();
            ScoreRegion region = new ScoreRegion(this);
            this.regionsList.add(region);
            Iterator it = cluster.iterator();

            while(it.hasNext()) {
                Point p = (Point)it.next();
                region.addPoint(p);
            }
        }

    }

    public void updateRegionScores() {
        Iterator i$ = this.regionsList.iterator();

        while(i$.hasNext()) {
            ScoreRegion scoreRegion = (ScoreRegion)i$.next();
            scoreRegion.updateScore();
        }

    }

    public double getScore(Point p) {
        return this.scores[p.x][p.y];
    }

    public List<ScoreRegion> getRegions() {
        return this.regionsList;
    }

    public boolean hasTodo() {
        return this.todoCount > 0;
    }

    public void clearTodo() {
        ScoreRegion r;
        for(Iterator i$ = this.regionsList.iterator(); i$.hasNext(); r.doneWith = false) {
            r = (ScoreRegion)i$.next();
        }

        this.todoCount = this.regionsList.size();
    }

    public ScoreRegion getOutlineOverlapRegion(ScoreRegion region, double minOverlapPercentage, int minChildSize, int maxChildSize) {
        Map<ScoreRegion, Integer> overlaps = new HashMap();
        Iterator i$ = region.outline().iterator();

        ScoreRegion outlineOverlapRegion;
        int minOverlap;
        while(i$.hasNext()) {
            Point outlinePoint = (Point)i$.next();
            outlineOverlapRegion = this.regionField[outlinePoint.x][outlinePoint.y];
            if (outlineOverlapRegion != null) {
                minOverlap = overlaps.containsKey(outlineOverlapRegion) ? (Integer)overlaps.get(outlineOverlapRegion) + 1 : 1;
                overlaps.put(outlineOverlapRegion, minOverlap);
            }
        }

        double maxFoundOverlapCount = -1.0D;
        outlineOverlapRegion = null;
        Iterator it = overlaps.keySet().iterator();

        while(it.hasNext()) {
            ScoreRegion overlapRegion = (ScoreRegion)it.next();
            int overlapCount = overlaps.get(overlapRegion);
            if ((double)overlapCount > maxFoundOverlapCount && !overlapRegion.doneWith && overlapRegion.size() >= minChildSize && overlapRegion.size() <= maxChildSize) {
                maxFoundOverlapCount = (double)overlapCount;
                outlineOverlapRegion = overlapRegion;
            }
        }

        if (outlineOverlapRegion == null) {
            return null;
        } else {
            assert !outlineOverlapRegion.doneWith;

            minOverlap = (int)((double)region.outline().size() * minOverlapPercentage);
            System.out.println("region with size " + region.size() + "has maxFoundOverlapCount = " + maxFoundOverlapCount + " minOverlap = " + minOverlap);
            if (maxFoundOverlapCount >= (double)minOverlap) {
                return outlineOverlapRegion;
            } else {
                return null;
            }
        }
    }

    public ScoreRegion getTopScoreRegion() {
        ScoreRegion topScoreRegion = null;
        double max = -1.0D;
        Iterator i$ = this.regionsList.iterator();

        while(i$.hasNext()) {
            ScoreRegion r = (ScoreRegion)i$.next();
            if (r != null && r.getRelativeScore() > max && !r.doneWith) {
                topScoreRegion = r;
                max = r.getRelativeScore();
            }
        }

        return topScoreRegion;
    }
}
