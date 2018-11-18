package deviationScoreRegions;

import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Point;
import java.util.Set;

public class ScoreRegionTools
{
    public static void draw(ScoreRegion scoreRegion, ImageProcessor imageProcessor, Color c)
    {
        for (Point point : scoreRegion.points)
            imageProcessor.putPixel(point.x, point.y, c.getRGB());
    }

    public static double getBinaryOverlap(ScoreRegion region, boolean[][] binaryField, int width)
    {
        if (region.size() == 0) return 0.0D;
        int overlapCount = 0;

        Set outline = region.outline(width);
        for (Point outlinePoint : outline) {
            if (binaryField[outlinePoint.x][outlinePoint.y] != 0) overlapCount++;
        }

        return overlapCount / outline.size();
    }

    public static void addToMask(ScoreRegion region, boolean[][] mask) {
        for (Point p : region.getPoints())
            mask[p.x][p.y] = 1;
    }

    public static ScoreRegion getMaxMaskRelevancyRegion(ScoreRegions scoreRegions, boolean[][] binaryField, int width)
    {
        ScoreRegion bestRegion = null;

        double maxMaskRelevancy = 4.9E-324D;
        for (ScoreRegion region : scoreRegions.getRegions()) if (!region.doneWith) {
            double regionScore = region.getRelativeScore() * 100.0D;
            double overlapScore = 0.0001D + getBinaryOverlap(region, binaryField, width) * 100.0D;
            double maskRelevancy = regionScore * Math.pow(overlapScore, 1.0D);
            if (maskRelevancy > maxMaskRelevancy) {
                bestRegion = region;
                maxMaskRelevancy = maskRelevancy;
            }
        }
        return bestRegion;
    }

    public static String debugInfos(ScoreRegion r, boolean[][] binaryField, int width) {
        int regionScore = (int)(r.getRelativeScore() * 100.0D);
        int overlapScore = (int)(0.0001D + getBinaryOverlap(r, binaryField, width) * 100.0D);
        int maskRelevancy = (int)(regionScore * Math.pow(overlapScore, 1.0D));
        return "MR " + maskRelevancy + " RS " + regionScore + " OLS " + overlapScore;
    }
}