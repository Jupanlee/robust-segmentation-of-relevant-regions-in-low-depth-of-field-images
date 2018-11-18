//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions.modular.edgeFilters;

import basics.ColorConversions;
import basics.MImage;
import basics.Tools;
import evaluation.Batch.Batchable;
import gradients.PixelOperation;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

public class OldScoreLabDifference extends PixelOperation implements Batchable {
    public OldScoreLabDifference() {
    }

    public double getPixelColor(ImageProcessor imageProcessor, int x, int y) {
        Vector<Integer> neighbours = Tools.getNeighbourRGBValues(imageProcessor, x, y, false);
        double[] mean = ColorConversions.mean(ColorConversions.getLab(neighbours));
        double[] centerColor = ColorConversions.getLab(imageProcessor.getPixel(x, y));
        double deltaE = Tools.euklidDistance(mean, centerColor);
        return Math.pow(deltaE, 2.0D);
    }

    public static ImageProcessor test(ImageProcessor ip) {
        ImageProcessor edges = new ByteProcessor(ip.getWidth(), ip.getHeight());
        MImage mImage = new MImage(ip);

        for(int x = 0; x < ip.getWidth(); ++x) {
            for(int y = 0; y < ip.getHeight(); ++y) {
                double deltaE = Tools.euklidDistance(mImage.getLab(x, y), mImage.getNeigbourMeanLab(x, y, 1, true));
                int value = (int)Math.pow(deltaE, 2.0D);
                edges.putPixel(x, y, value);
            }
        }

        return edges;
    }

    public ImageProcessor run(ImageProcessor imageProcessor) {
        return this.run(imageProcessor, 0.95D, 10);
    }

    public ImageProcessor run(ImageProcessor imageProcessor, double blurRadius, int iterations) {
        ImageProcessor result = new ByteProcessor(imageProcessor.getWidth(), imageProcessor.getHeight());
        return PixelOperation.renderImage(Tools.blur(imageProcessor, blurRadius, iterations), result, this);
    }

    public static void main(String[] args) throws IOException {
        int size = 400;
        Iterator i$ = Tools.getFilesFromDirectory("../../images/schwierig", ".jpg").iterator();

        while(i$.hasNext()) {
            String fileName = (String)i$.next();
            ImageProcessor imageProcessor = Tools.loadImageProcessor(fileName, size);
            Tools.save(Tools.write("test", test(Tools.blur(imageProcessor, 0.95D, 10))));
            Tools.save(Tools.write("OldScoreLabDifference", (new OldScoreLabDifference()).run(imageProcessor)));
        }

    }
}
