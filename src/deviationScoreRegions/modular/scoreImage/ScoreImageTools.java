//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions.modular.scoreImage;

import basics.Tools;
import deviationScoreRegions.modular.ScoreClustering;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.util.Iterator;
import java.util.List;
import others.morphological.Morphological;

public class ScoreImageTools {
    public ScoreImageTools() {
    }

    public static double morphologicalInstability(ImageProcessor mask, int closeSize) {
        ImageProcessor closedMask = Morphological.close(mask, closeSize);
        closedMask = Morphological.dilateByReconstruction(closedMask, closeSize * 2);
        closedMask.threshold(200);
        Tools.save(Tools.write("morphologicClosed", closedMask));
        ImageProcessor difference = Tools.difference(mask, closedMask);
        double changedPixel = (double)Tools.getPixelCount(difference, 1.0D, 1.7976931348623157E308D);
        double whiteMaskPixel = (double)Tools.getPixelCount(mask, 1.0D, 1.7976931348623157E308D);
        double morphologicalInstability = changedPixel / (whiteMaskPixel + 1.0D);
        Tools.save(Tools.write("morphologicalInstability = " + Tools.formatNumber(morphologicalInstability), difference));
        return morphologicalInstability;
    }

    public static double scoreImageInstability(ScoreImage scoreImage) {
        int closeScoreImageSize = (int)Math.sqrt((double)scoreImage.getImageProcessor().getPixelCount() * 0.01D);
        ImageProcessor closedScoreImageMask = Morphological.close(scoreImage.getImageProcessor(), closeScoreImageSize);
        return morphologicalInstability(closedScoreImageMask, closeScoreImageSize * 2);
    }

    public static void emphasise(ScoreImage scoreImage, ScoreClustering scoreClustering, double emphasiseClusterSize) {
        double biggestClusterSize = scoreClustering.getClusters().isEmpty() ? 0.0D : (double)((List)scoreClustering.getClusters().get(0)).size();
        ImageProcessor imageProcessor = scoreImage.getImageProcessor();
        imageProcessor.threshold(2147483647);
        Iterator i$ = scoreClustering.getClusters().iterator();

        while(true) {
            List cluster;
            do {
                if (!i$.hasNext()) {
                    return;
                }

                cluster = (List)i$.next();
            } while((double)cluster.size() / biggestClusterSize < emphasiseClusterSize);

            Iterator it = cluster.iterator();

            while(it.hasNext()) {
                Point p = (Point)it.next();
                double v = (double)imageProcessor.getPixelValue(p.x, p.y);
                imageProcessor.putPixelValue(p.x, p.y, 255.0D);
            }
        }
    }
}
