//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics.pointSetOperations.clustering;

import basics.Tools;
import basics.pointSetOperations.Growing;
import basics.pointSetOperations.Growing.Growable;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class ColorRegions implements Growable {
    private double maxMean;
    private float[] hsvClusterColor;
    private ImageProcessor imageProcessor;
    private int rgbClusterColor;

    public ColorRegions(ImageProcessor imageProcessor, double maxMean) {
        this.imageProcessor = imageProcessor.duplicate();
        this.maxMean = maxMean;
    }

    public List<Point> grow(Point p) {
        return Tools.get8Neighbourhood(p, this.imageProcessor.getWidth(), this.imageProcessor.getHeight());
    }

    public boolean canGrow(Point from, Point to) {
        if (Tools.chance(5.0E-4D)) {
            Tools.showImage("ColorRegions", this.imageProcessor, "processing...");
        }

        double[] differences = Tools.differenceAbs(this.hsvClusterColor, Tools.getHSV(this.imageProcessor.getPixel(to.x, to.y)));
        double mean = Tools.getMean(differences);
        return mean <= this.maxMean;
    }

    public void newRegionAction(Point startPoint) {
        this.hsvClusterColor = Tools.getHSV(this.imageProcessor.getPixel(startPoint.x, startPoint.y));
        this.rgbClusterColor = this.imageProcessor.getPixel(startPoint.x, startPoint.y);
    }

    public void newGrowedPointAction(Point newPoint) {
        this.imageProcessor.putPixel(newPoint.x, newPoint.y, this.rgbClusterColor);
    }

    public List<List<Point>> run() {
        List<List<Point>> result = Growing.grow8Neighbourhood(this.imageProcessor.getWidth(), this.imageProcessor.getHeight(), this);
        Tools.showImage("ColorRegions", this.imageProcessor, "ColorRegions", true);
        return result;
    }

    public static void main(String[] args) throws IOException {
        Iterator i$ = Tools.getFilesFromDirectory("../data/batch/images/base_640", ".jpg").iterator();

        while(i$.hasNext()) {
            String fileName = (String)i$.next();
            ImageProcessor original = Tools.resize(Tools.loadImageProcessor(fileName), 500);
            Tools.showImage("window1", original, "original");
            List<List<Point>> regions = (new ColorRegions(original, 0.17D)).run();
            Tools.showImage("window2", original, "segmented", true);
        }

    }
}
