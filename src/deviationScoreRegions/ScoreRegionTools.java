//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions;

import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Point;
import java.util.Iterator;
import java.util.Set;

public class ScoreRegionTools {
    public ScoreRegionTools() {
    }

    public static void draw(ScoreRegion scoreRegion, ImageProcessor imageProcessor, Color c) {
        Iterator i$ = scoreRegion.points.iterator();

        while(i$.hasNext()) {
            Point point = (Point)i$.next();
            imageProcessor.putPixel(point.x, point.y, c.getRGB());
        }

    }

    public static double getBinaryOverlap(ScoreRegion region, boolean[][] binaryField, int width) {
        if (region.size() == 0) {
            return 0.0D;
        } else {
            int overlapCount = 0;
            Set<Point> outline = region.outline(width);
            Iterator i$ = outline.iterator();

            while(i$.hasNext()) {
                Point outlinePoint = (Point)i$.next();
                if (binaryField[outlinePoint.x][outlinePoint.y]) {
                    ++overlapCount;
                }
            }

            return (double)overlapCount / (double)outline.size();
        }
    }

    public static void addToMask(ScoreRegion region, boolean[][] mask) {
        Point p;
        for(Iterator i$ = region.getPoints().iterator(); i$.hasNext(); mask[p.x][p.y] = true) {
            p = (Point)i$.next();
        }

    }

    public static ScoreRegion getMaxMaskRelevancyRegion(ScoreRegions scoreRegions, boolean[][] binaryField, int width) {
        ScoreRegion bestRegion = null;
        double maxMaskRelevancy = 4.9E-324D;
        Iterator i$ = scoreRegions.getRegions().iterator();

        while(i$.hasNext()) {
            ScoreRegion region = (ScoreRegion)i$.next();
            if (!region.doneWith) {
                double regionScore = region.getRelativeScore() * 100.0D;
                double overlapScore = 1.0E-4D + getBinaryOverlap(region, binaryField, width) * 100.0D;
                double maskRelevancy = regionScore * Math.pow(overlapScore, 1.0D);
                if (maskRelevancy > maxMaskRelevancy) {
                    bestRegion = region;
                    maxMaskRelevancy = maskRelevancy;
                }
            }
        }

        return bestRegion;
    }

    public static String debugInfos(ScoreRegion r, boolean[][] binaryField, int width) {
        int regionScore = (int)(r.getRelativeScore() * 100.0D);
        int overlapScore = (int)(1.0E-4D + getBinaryOverlap(r, binaryField, width) * 100.0D);
        int maskRelevancy = (int)((double)regionScore * Math.pow((double)overlapScore, 1.0D));
        return "MR " + maskRelevancy + " RS " + regionScore + " OLS " + overlapScore;
    }
}
