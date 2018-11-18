//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ColorRegionTools {
    public ColorRegionTools() {
    }

    public static ImageProcessor mask(Collection<ColorRegion> colorRegions, int width, int height) {
        ImageProcessor result = new ByteProcessor(width, height);
        Iterator i$ = colorRegions.iterator();

        while(i$.hasNext()) {
            ColorRegion colorRegion = (ColorRegion)i$.next();
            Iterator it = colorRegion.getPixels().iterator();

            while(it.hasNext()) {
                Point p = (Point)it.next();
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
        Iterator i$ = colorRegion.getPixels().iterator();

        while(i$.hasNext()) {
            Point p = (Point)i$.next();
            imageProcessor.putPixel(p.x, p.y, c.getRGB());
        }

    }

    public static void draw(ImageProcessor imageProcessor, ColorRegion colorRegion) {
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
        Iterator i$ = colorRegions.iterator();

        while(i$.hasNext()) {
            ColorRegion colorRegion = (ColorRegion)i$.next();
            draw(result, colorRegion);
        }

        return result;
    }

    public static double getRelativeScore(ColorRegion colorRegion, ImageProcessor scoreImage, int neighbourRadius) {
        if (colorRegion.size() == 0) {
            return 0.0D;
        } else {
            double sum = 0.0D;
            Set<Point> neighbours = getNeighbours(colorRegion.getPixels(), neighbourRadius, scoreImage.getWidth(), scoreImage.getHeight());

            Point p;
            for(Iterator i$ = neighbours.iterator(); i$.hasNext(); sum += (double)scoreImage.getPixelValue(p.x, p.y)) {
                p = (Point)i$.next();
            }

            return neighbours.isEmpty() ? 0.0D : sum / (double)neighbours.size();
        }
    }

    public static List<Double> getRelativeScores(List<ColorRegion> colorRegions, ImageProcessor scoreImage, int neighbourRadius) {
        List<Double> relativeScores = new ArrayList();
        Iterator i$ = colorRegions.iterator();

        while(i$.hasNext()) {
            ColorRegion colorRegion = (ColorRegion)i$.next();
            relativeScores.add(getRelativeScore(colorRegion, scoreImage, neighbourRadius));
        }

        return relativeScores;
    }

    public static double getMeanRelativeScore(List<ColorRegion> colorRegions, ImageProcessor scoreImage, int neighbourRadius) {
        return Tools.getMean(getRelativeScores(colorRegions, scoreImage, neighbourRadius));
    }

    private static Set<Point> getNeighbours(List<Point> points, int radius, int maxWidth, int maxHeight) {
        Set<Point> neighbours = new HashSet();
        Iterator i$ = points.iterator();

        while(i$.hasNext()) {
            Point p = (Point)i$.next();
            neighbours.addAll(getNeighbours(p, radius, maxWidth, maxHeight));
        }

        return neighbours;
    }

    public static Set<Point> getOutline(ColorRegion colorRegion, int radius, int maxWidth, int maxHeight) {
        Set<Point> outline = new HashSet();
        Iterator i$ = colorRegion.getPixels().iterator();

        while(i$.hasNext()) {
            Point p = (Point)i$.next();
            Iterator it = getNeighbours(p, radius, maxWidth, maxHeight).iterator();

            while(it.hasNext()) {
                Point candidate = (Point)it.next();
                if (!colorRegion.getPixels().contains(candidate)) {
                    outline.add(candidate);
                }
            }
        }

        return outline;
    }

    private static Set<Point> getNeighbours(Point p, int radius, int maxWidth, int maxHeight) {
        Set<Point> neighbours = new HashSet();

        for(int dx = -radius; dx <= radius; ++dx) {
            for(int dy = -radius; dy <= radius; ++dy) {
                Point neighbour = new Point(p.x + dx, p.y + dy);
                if (neighbour.x > 0 && neighbour.x < maxWidth && neighbour.y > 0 && neighbour.y < maxHeight) {
                    neighbours.add(neighbour);
                }
            }
        }

        return neighbours;
    }

    public static List<ColorRegion> getRegionsFromImageProcessor(ImageProcessor imageProcessor, double deltaEToBeSimilar) {
        return getRegionsFromImageProcessor(imageProcessor, deltaEToBeSimilar, (ImageProcessor)null);
    }

    public static List<ColorRegion> pointListsToColorRegions(List<List<Point>> clusters) {
        List<ColorRegion> colorRegions = new LinkedList();
        Iterator i$ = clusters.iterator();

        while(i$.hasNext()) {
            List<Point> cluster = (List)i$.next();
            colorRegions.add(new ColorRegion(cluster));
        }

        return colorRegions;
    }

    public static List<ColorRegion> getRegionsFromImageProcessor(ImageProcessor imageProcessor, double deltaEToBeSimilar, ImageProcessor mask) {
        return getRegionsFromImageProcessor(imageProcessor, deltaEToBeSimilar, mask, -1);
    }

    public static List<ColorRegion> getRegionsFromImageProcessor(ImageProcessor imageProcessor, double deltaEToBeSimilar, ImageProcessor mask, int ignoreValue) {
        List<ColorRegion> regions = new LinkedList();
        Grow grow = new Grow(imageProcessor, deltaEToBeSimilar);

        for(int x = 0; x < imageProcessor.getWidth(); ++x) {
            for(int y = 0; y < imageProcessor.getHeight(); ++y) {
                if ((mask == null || mask.getPixel(x, y) != 0) && imageProcessor.getPixel(x, y) != ignoreValue && !grow.doneWith(x, y)) {
                    ColorRegion region = grow.expand(new Point(x, y), mask);
                    regions.add(region);
                }
            }
        }

        return regions;
    }

    public static double overlap(ColorRegion region, ImageProcessor mask) {
        if (region.size() == 0) {
            return 0.0D;
        } else {
            int overlaps = 0;
            Iterator i$ = region.getPixels().iterator();

            while(i$.hasNext()) {
                Point p = (Point)i$.next();
                if (mask.getPixel(p.x, p.y) != 0) {
                    ++overlaps;
                }
            }

            return (double)overlaps / (double)region.size();
        }
    }
}
