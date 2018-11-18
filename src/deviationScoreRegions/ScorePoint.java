package deviationScoreRegions;

import java.awt.Point;

public class ScorePoint extends Point
{
    public double score;

    public ScorePoint(int x, int y, double score)
    {
        this.x = x;
        this.y = y;
        this.score = score;
    }
}