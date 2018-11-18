//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions.grow;

import basics.MColor;
import basics.Tools;
import basics.javaAddons.DEBUG;
import basics.javaAddons.MQueue;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Grow {
    private double minEuklidDistToBeSimilar;
    private int width;
    private int height;
    protected MColor[][] labColorField = (MColor[][])null;
    private boolean[][] doneWith;
    private Color debugColor;
    public ImageProcessor debugInfos;
    private double debugInfoChance = 0.005D;
    private MQueue<Point> seeds;

    protected Grow() {
    }

    protected void initGrow(ImageProcessor imageProcessor, double minEuklidDistToBeSimilar) {
        this.minEuklidDistToBeSimilar = minEuklidDistToBeSimilar;
        this.width = imageProcessor.getWidth();
        this.height = imageProcessor.getHeight();
        this.debugInfos = new ColorProcessor(this.width, this.height);
        this.doneWith = new boolean[this.width][this.height];
        this.seeds = new MQueue(imageProcessor.getPixelCount());
        this.labColorField = new MColor[this.width][this.height];

        for(int x = 0; x < this.width; ++x) {
            for(int y = 0; y < this.height; ++y) {
                this.labColorField[x][y] = new MColor(imageProcessor.getPixel(x, y));
            }
        }

    }

    public boolean doneWith(int x, int y) {
        return this.doneWith[x][y];
    }

    public Grow(ImageProcessor imageProcessor, double minEuklidDistToBeSimilar) {
        this.initGrow(imageProcessor, minEuklidDistToBeSimilar);
    }

    public ColorRegion expand(Point p) {
        return this.expand(p, (ImageProcessor)null);
    }

    public ColorRegion expand(Point root_pixel, ImageProcessor mask) {
        if (DEBUG.getVerbose()) {
            this.debugColor = Tools.randomColor(10);
        }

        if (this.doneWith[root_pixel.x][root_pixel.y]) {
            return null;
        } else {
            ColorRegion currentColorRegion = new ColorRegion();
            this.seeds.clear();
            this.seeds.add(root_pixel);

            label43:
            while(true) {
                Point seed;
                do {
                    if (this.seeds.isEmpty()) {
                        return currentColorRegion;
                    }

                    seed = (Point)this.seeds.removeFirst();
                } while(this.doneWith[seed.x][seed.y]);

                this.addPixelToRegion(seed, currentColorRegion);
                Iterator i$ = Tools.get8Neighbourhood(seed, this.width, this.height).iterator();

                while(true) {
                    Point neighbour;
                    do {
                        if (!i$.hasNext()) {
                            continue label43;
                        }

                        neighbour = (Point)i$.next();
                    } while(mask != null && mask.getPixel(neighbour.x, neighbour.y) == 0);

                    if (this.similar(root_pixel, neighbour)) {
                        this.seeds.add(neighbour);
                    } else {
                        currentColorRegion.addOutlinePixel(neighbour);
                    }
                }
            }
        }
    }

    private void addPixelToRegion(Point p, ColorRegion colorRegion) {
        this.doneWith[p.x][p.y] = true;
        colorRegion.add(p);
        if (DEBUG.getVerbose()) {
            this.debugInfos.putPixel(p.x, p.y, this.debugColor.getRGB());
            if (Tools.chance(this.debugInfoChance)) {
                ;
            }
        }

    }

    protected boolean similar(Point reference, Point newPoint) {
        MColor labA = this.labColorField[reference.x][reference.y];
        MColor labB = this.labColorField[newPoint.x][newPoint.y];
        return labA.getLabDeltaE(labB) <= this.minEuklidDistToBeSimilar;
    }

    public List<ColorRegion> getRegions(ImageProcessor imageProcessor, double deltaEToBeSimilar) {
        this.initGrow(imageProcessor, this.minEuklidDistToBeSimilar);
        List<ColorRegion> regions = new LinkedList();

        for(int x = 0; x < imageProcessor.getWidth(); ++x) {
            for(int y = 0; y < imageProcessor.getHeight(); ++y) {
                if (!this.doneWith(x, y)) {
                    ColorRegion region = this.expand(new Point(x, y));
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
