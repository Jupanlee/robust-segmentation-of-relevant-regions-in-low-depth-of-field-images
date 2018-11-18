package deviationScoreRegions.grow;

import basics.Tools;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ColorRegionTools
{
    public static ImageProcessor mask(Collection<ColorRegion> colorRegions, int width, int height)
    {
        ImageProcessor result = new ByteProcessor(width, height);
        for (ColorRegion colorRegion : colorRegions) {
            for (Point p : colorRegion.getPixels()) {
                result.putPixel(p.x, p.y, 255);
            }
        }

        return result;
    }

    public static ImageProcessor draw(ColorRegion colorRegion, int width, int height) {
        ImageProcessor result = new ColorProcessor(width, height);
        draw(result, colorRegion);
        return result;
    }

    public static void draw(ImageProcessor imageProcessor, ColorRegion colorRegion, Color c) {
        for (Point p : colorRegion.getPixels())
            imageProcessor.putPixel(p.x, p.y, c.getRGB());
    }

    public static void draw(ImageProcessor imageProcessor, ColorRegion colorRegion)
    {
        Color c = Tools.randomColor(15);
        draw(imageProcessor, colorRegion, c);
    }

    public static ImageProcessor draw(ColorRegion colorRegion, ImageProcessor original, boolean makeBW, Color c) {
        ImageProcessor result = original.duplicate();
        if (makeBW) {
            result.copyBits(result.convertToByte(true), 0, 0, 0);
            result.threshold(1);
            result.multiply(0.33D);
        }

        draw(result, colorRegion, c);

        return result;
    }

    public static ImageProcessor draw(Collection<ColorRegion> colorRegions, int width, int height) {
        ImageProcessor result = new ColorProcessor(width, height);
        for (ColorRegion colorRegion : colorRegions) {
            draw(result, colorRegion);
        }

        return result;
    }

    public static double getRelativeScore(ColorRegion colorRegion, ImageProcessor scoreImage, int neighbourRadius) {
        if (colorRegion.size() == 0) {
            return 0.0D;
        }

        double sum = 0.0D;
        Set neighbours = getNeighbours(colorRegion.getPixels(), neighbourRadius, scoreImage.getWidth(), scoreImage.getHeight());
        for (Point p : neighbours) {
            sum += scoreImage.getPixelValue(p.x, p.y);
        }

        return neighbours.isEmpty() ? 0.0D : sum / neighbours.size();
    }

    public static List<Double> getRelativeScores(List<ColorRegion> colorRegions, ImageProcessor scoreImage, int neighbourRadius) {
        List relativeScores = new ArrayList();
        for (ColorRegion colorRegion : colorRegions) {
            relativeScores.add(Double.valueOf(getRelativeScore(colorRegion, scoreImage, neighbourRadius)));
        }
        return relativeScores;
    }

    public static double getMeanRelativeScore(List<ColorRegion> colorRegions, ImageProcessor scoreImage, int neighbourRadius) {
        return Tools.getMean(getRelativeScores(colorRegions, scoreImage, neighbourRadius));
    }

    private static Set<Point> getNeighbours(List<Point> points, int radius, int maxWidth, int maxHeight) {
        Set neighbours = new HashSet();
        for (Point p : points) {
            neighbours.addAll(getNeighbours(p, radius, maxWidth, maxHeight));
        }

        return neighbours;
    }

    public static Set<Point> getOutline(ColorRegion colorRegion, int radius, int maxWidth, int maxHeight) {
        Set outline = new HashSet();

        for (Point p : colorRegion.getPixels()) {
            for (Point candidate : getNeighbours(p, radius, maxWidth, maxHeight)) {
                if (!colorRegion.getPixels().contains(candidate)) {
                    outline.add(candidate);
                }
            }
        }

        return outline;
    }

    private static Set<Point> getNeighbours(Point p, int radius, int maxWidth, int maxHeight) {
        Set neighbours = new HashSet();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                Point neighbour = new Point(p.x + dx, p.y + dy);
                if ((neighbour.x > 0) && (neighbour.x < maxWidth) && (neighbour.y > 0) && (neighbour.y < maxHeight)) {
                    neighbours.add(neighbour);
                }
            }
        }
        return neighbours;
    }

    public static List<ColorRegion> getRegionsFromImageProcessor(ImageProcessor imageProcessor, double deltaEToBeSimilar) {
        return getRegionsFromImageProcessor(imageProcessor, deltaEToBeSimilar, null);
    }

    public static List<ColorRegion> pointListsToColorRegions(List<List<Point>> clusters) {
        List colorRegions = new LinkedList();

        for (List cluster : clusters) {
            colorRegions.add(new ColorRegion(cluster));
        }

        return colorRegions;
    }

    public static List<ColorRegion> getRegionsFromImageProcessor(ImageProcessor imageProcessor, double deltaEToBeSimilar, ImageProcessor mask) {
        return getRegionsFromImageProcessor(imageProcessor, deltaEToBeSimilar, mask, -1);
    }

    public static List<ColorRegion> getRegionsFromImageProcessor(ImageProcessor imageProcessor, double deltaEToBeSimilar, ImageProcessor mask, int ignoreValue) {
        List regions = new LinkedList();

        Grow grow = new Grow(imageProcessor, deltaEToBeSimilar);
        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++) {
                if (((mask != null) && (mask.getPixel(x, y) == 0)) ||
                        (imageProcessor.getPixel(x, y) == ignoreValue) ||
                        (grow.doneWith(x, y))) continue;
                ColorRegion region = grow.expand(new Point(x, y), mask);
                regions.add(region);
            }

        }

        return regions;
    }

    public static double overlap(ColorRegion region, ImageProcessor mask) {
        if (region.size() == 0) {
            return 0.0D;
        }

        int overlaps = 0;
        for (Point p : region.getPixels()) {
            if (mask.getPixel(p.x, p.y) != 0) {
                overlaps++;
            }
        }

        return overlaps / region.size();
    }
}