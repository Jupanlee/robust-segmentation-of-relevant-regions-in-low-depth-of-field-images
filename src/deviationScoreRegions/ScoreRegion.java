package deviationScoreRegions;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ScoreRegion
{
    private double score;
    List<Point> points = new LinkedList();
    boolean doneWith = false;
    ScoreRegions scoreRegions;

    public List<Point> getPoints()
    {
        return this.points;
    }

    public double getRelativeScore() {
        if (this.points.size() == 0) return 0.0D;
        return this.score / (this.points.size() * 255);
    }

    public ScoreRegion(ScoreRegions scoreRegions) {
        this.scoreRegions = scoreRegions;
    }

    public int size() {
        return this.points.size();
    }

    public void addPoint(Point p) {
        this.points.add(p);
        this.scoreRegions.regionField[p.x][p.y] = this;

        for (Point neighbour : getNeighbours(p, this.scoreRegions.neighbourDistance))
            addScoreForPoint(neighbour);
    }

    private void addScoreForPoint(Point p)
    {
        this.score += this.scoreRegions.getScore(p);
    }

    public void updateScore() {
        this.score = 0.0D;
        Point p;
        for (Iterator i$ = this.points.iterator(); i$.hasNext(); addScoreForPoint(p)) p = (Point)i$.next();
    }

    public void doneWith(boolean value)
    {
        if (this.doneWith != value) {
            this.doneWith = value;
            this.scoreRegions.todoCount -= 1;
        }
    }

    public Set<Point> outline() {
        return outline(1);
    }

    private Set<Point> getNeighbours(Point p, int width) {
        Set neighbours = new HashSet();
        for (int dx = -width; dx <= width; dx++) {
            for (int dy = -width; dy <= width; dy++) {
                Point neighbour = new Point(p.x + dx, p.y + dy);
                if ((neighbour.x > 0) && (neighbour.x < this.scoreRegions.width) && (neighbour.y > 0) && (neighbour.y < this.scoreRegions.height)) {
                    neighbours.add(neighbour);
                }
            }
        }
        return neighbours;
    }

    public Set<Point> outline(int width) {
        Set outline = new HashSet();

        for (Point p : this.points) {
            for (Point candidate : getNeighbours(p, width)) {
                ScoreRegion r = this.scoreRegions.regionField[candidate.x][candidate.y];
                if ((r != null) && (r != this)) outline.add(candidate);
            }

        }

        return outline;
    }

    public void clear() {
        this.points.clear();
        this.score = 0.0D;
    }

    private void deleteRegion(ScoreRegion r) {
        this.scoreRegions.regionsList.remove(r);
        this.scoreRegions.todoCount -= 1;
    }

    public void merge(ScoreRegion other)
    {
        for (Point p : other.getPoints()) {
            addPoint(p);
        }
        deleteRegion(other);
        other.clear();
    }

    public void eraseAllUsedScorePointsFromField(int neighbourRadius) {
        for (Point p : this.points)
            for (int dx = -neighbourRadius; dx <= neighbourRadius; dx++)
                for (int dy = -neighbourRadius; dy <= neighbourRadius; dy++) {
                    Point neighbour = new Point(p.x + dx, p.y + dy);
                    if ((neighbour.x > 0) && (neighbour.x < this.scoreRegions.width) && (neighbour.y > 0) && (neighbour.y < this.scoreRegions.height))
                        this.scoreRegions.scores[neighbour.x][neighbour.y] = 0.0D;
                }
    }
}