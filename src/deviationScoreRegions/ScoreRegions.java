package deviationScoreRegions;

import basics.Tools;
import java.awt.Point;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScoreRegions
{
    double[][] scores;
    double[][] neighbourSocres;
    List<ScoreRegion> regionsList;
    ScoreRegion[][] regionField;
    int width;
    int height;
    int todoCount;
    int neighbourDistance;

    public <PointList extends List<Point>> ScoreRegions(double[][] scores, List<PointList> clusters, int neighbourDistance)
    {
        this.scores = scores;
        this.width = scores.length;
        this.height = scores[0].length;
        this.regionsList = new LinkedList();
        this.regionField = new ScoreRegion[this.width][this.height];
        Tools.fill(this.regionField, null);

        this.todoCount = clusters.size();
        this.neighbourDistance = neighbourDistance;

        for (Collection cluster : clusters) {
            region = new ScoreRegion(this);
            this.regionsList.add(region);

            for (Point p : cluster)
                region.addPoint(p);
        }
        ScoreRegion region;
    }

    public void updateRegionScores() {
        for (ScoreRegion scoreRegion : this.regionsList)
            scoreRegion.updateScore();
    }

    public double getScore(Point p)
    {
        return this.scores[p.x][p.y];
    }

    public List<ScoreRegion> getRegions() {
        return this.regionsList;
    }

    public boolean hasTodo() {
        return this.todoCount > 0;
    }

    public void clearTodo()
    {
        for (ScoreRegion r : this.regionsList) {
            r.doneWith = false;
        }
        this.todoCount = this.regionsList.size();
    }

    public ScoreRegion getOutlineOverlapRegion(ScoreRegion region, double minOverlapPercentage, int minChildSize, int maxChildSize) {
        Map overlaps = new HashMap();

        for (Point outlinePoint : region.outline()) {
            ScoreRegion regionfromOutlinePoint = this.regionField[outlinePoint.x][outlinePoint.y];
            if (regionfromOutlinePoint != null) {
                int increasedValue = overlaps.containsKey(regionfromOutlinePoint) ? ((Integer)overlaps.get(regionfromOutlinePoint)).intValue() + 1 : 1;
                overlaps.put(regionfromOutlinePoint, Integer.valueOf(increasedValue));
            }

        }

        double maxFoundOverlapCount = -1.0D;
        ScoreRegion outlineOverlapRegion = null;
        for (ScoreRegion overlapRegion : overlaps.keySet()) {
            int overlapCount = ((Integer)overlaps.get(overlapRegion)).intValue();
            if ((overlapCount > maxFoundOverlapCount) && (!overlapRegion.doneWith) && (overlapRegion.size() >= minChildSize) && (overlapRegion.size() <= maxChildSize)) {
                maxFoundOverlapCount = overlapCount;
                outlineOverlapRegion = overlapRegion;
            }
        }

        if (outlineOverlapRegion == null) return null;
        assert (!outlineOverlapRegion.doneWith);
        int minOverlap = (int)(region.outline().size() * minOverlapPercentage);
        System.out.println("region with size " + region.size() + "has maxFoundOverlapCount = " + maxFoundOverlapCount + " minOverlap = " + minOverlap);
        if (maxFoundOverlapCount >= minOverlap) return outlineOverlapRegion;
        return null;
    }

    public ScoreRegion getTopScoreRegion()
    {
        ScoreRegion topScoreRegion = null;

        double max = -1.0D;
        for (ScoreRegion r : this.regionsList) if ((r != null) &&
                (r.getRelativeScore() > max) && (!r.doneWith)) {
            topScoreRegion = r;
            max = r.getRelativeScore();
        }


        return topScoreRegion;
    }
}