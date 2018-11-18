package deviationScoreRegions.grow;

import basics.MColor;
import basics.Tools;
import basics.javaAddons.DEBUG;
import basics.javaAddons.MQueue;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

public class Grow
{
    private double minEuklidDistToBeSimilar;
    private int width;
    private int height;
    protected MColor[][] labColorField = (MColor[][])null;
    private boolean[][] doneWith;
    private Color debugColor;
    public ImageProcessor debugInfos;
    private double debugInfoChance = 0.005D;
    private MQueue<Point> seeds;

    protected Grow()
    {
    }

    protected void initGrow(ImageProcessor imageProcessor, double minEuklidDistToBeSimilar)
    {
        this.minEuklidDistToBeSimilar = minEuklidDistToBeSimilar;
        this.width = imageProcessor.getWidth();
        this.height = imageProcessor.getHeight();
        this.debugInfos = new ColorProcessor(this.width, this.height);
        this.doneWith = new boolean[this.width][this.height];
        this.seeds = new MQueue(imageProcessor.getPixelCount());
        this.labColorField = new MColor[this.width][this.height];
        for (int x = 0; x < this.width; x++)
            for (int y = 0; y < this.height; y++)
                this.labColorField[x][y] = new MColor(imageProcessor.getPixel(x, y));
    }

    public boolean doneWith(int x, int y)
    {
        return this.doneWith[x][y];
    }

    public Grow(ImageProcessor imageProcessor, double minEuklidDistToBeSimilar) {
        initGrow(imageProcessor, minEuklidDistToBeSimilar);
    }

    public ColorRegion expand(Point p) {
        return expand(p, null);
    }

    public ColorRegion expand(Point root_pixel, ImageProcessor mask) {
        if (DEBUG.getVerbose()) {
            this.debugColor = Tools.randomColor(10);
        }
        if (this.doneWith[root_pixel.x][root_pixel.y] != 0) {
            return null;
        }

        ColorRegion currentColorRegion = new ColorRegion();

        this.seeds.clear();
        this.seeds.add(root_pixel);

        while (!this.seeds.isEmpty()) {
            Point seed = (Point)this.seeds.removeFirst();
            if (this.doneWith[seed.x][seed.y] == 0)
            {
                addPixelToRegion(seed, currentColorRegion);

                for (Point neighbour : Tools.get8Neighbourhood(seed, this.width, this.height)) {
                    if ((mask == null) || (mask.getPixel(neighbour.x, neighbour.y) != 0)) {
                        if (similar(root_pixel, neighbour))
                            this.seeds.add(neighbour);
                        else {
                            currentColorRegion.addOutlinePixel(neighbour);
                        }
                    }
                }
            }
        }

        return currentColorRegion;
    }

    private void addPixelToRegion(Point p, ColorRegion colorRegion) {
        this.doneWith[p.x][p.y] = 1;
        colorRegion.add(p);

        if (DEBUG.getVerbose()) {
            this.debugInfos.putPixel(p.x, p.y, this.debugColor.getRGB());
            if (!Tools.chance(this.debugInfoChance));
        }
    }

    protected boolean similar(Point reference, Point newPoint) {
        MColor labA = this.labColorField[reference.x][reference.y];
        MColor labB = this.labColorField[newPoint.x][newPoint.y];
        return labA.getLabDeltaE(labB) <= this.minEuklidDistToBeSimilar;
    }

    public List<ColorRegion> getRegions(ImageProcessor imageProcessor, double deltaEToBeSimilar) {
        initGrow(imageProcessor, this.minEuklidDistToBeSimilar);
        List regions = new LinkedList();

        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++) {
                if (!doneWith(x, y)) {
                    ColorRegion region = expand(new Point(x, y));
                    regions.add(region);
                }
            }
        }
        if (DEBUG.getVerbose()) {
            Tools.save(this.debugInfos);
        }

        return regions;
    }
}