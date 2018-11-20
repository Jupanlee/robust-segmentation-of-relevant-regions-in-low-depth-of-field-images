package newTry;

import basics.Tools;
import basics.pointSetOperations.clustering.dbscan.ColorDBSCAN_lab;
import deviationScoreRegions.grow.ColorRegion;
import deviationScoreRegions.grow.ColorRegionTools;
import deviationScoreRegions.modular.scoreImage.DeviationScoreImage;
import deviationScoreRegions.modular.scoreImage.ScoreImage;
import evaluation.Batch;
import evaluation.Batch.Batchable;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.io.PrintStream;
import java.util.List;
import others.morphological.Morphological;

public class ColorScoreGrowingNewTry
        implements Batch.Batchable
{
    public static void main(String[] args)
    {
        Batch.run(new ColorScoreGrowingNewTry(), 600, "../../images/schwierig");
    }

    public ImageProcessor run(ImageProcessor original)
    {
        ScoreImage scoreImage = new DeviationScoreImage();
        scoreImage.generateScore(original);

        int neighbourRadius = 1;

        boolean useDBSCAN = false;
        int deltaE = 10;
        List<ColorRegion> colorRegions;
        if (useDBSCAN) {
            ColorDBSCAN_lab colorDbscan = new ColorDBSCAN_lab();
            colorDbscan.setDeltaEToBeSimilar(2147483647.0D);
            colorDbscan.setDeltaEToBeSimilarToMean(deltaE);
            List clusters = colorDbscan.get(original, 1, 4);
            colorRegions = ColorRegionTools.pointListsToColorRegions(clusters);
        } else {
            colorRegions = ColorRegionTools.getRegionsFromImageProcessor(original, deltaE);
        }

        ImageProcessor mask = new ByteProcessor(original.getWidth(), original.getHeight());

        double maxScore = Tools.getMax(ColorRegionTools.getRelativeScores(colorRegions, scoreImage.getImageProcessor(), 1));
        for (ColorRegion colorRegion : colorRegions) {
            double score = ColorRegionTools.getRelativeScore(colorRegion, scoreImage.getImageProcessor(), 1);

            double shift = 0.0D;
            double value = shift + score / maxScore;

            double power = 1.0D;
            double valuePower = Math.pow(value, power);

            double max = Math.pow(1.0D + shift, power);

            for (Point p : colorRegion.getPixels())
                mask.putPixelValue(p.x, p.y, valuePower / max * 255.0D);
        }
        double valuePower;
        double max;
        Tools.save(Tools.write("deltaE = " + deltaE, mask));

        mask.threshold(40);

        int closeSize = (int)(Math.sqrt(mask.getPixelCount()) * 0.015D);
        mask = Morphological.close(mask, closeSize);

        int recSize = (int)(Math.sqrt(mask.getPixelCount()) * 0.2D);
        System.out.println("recSizes=" + recSize);
        mask = Morphological.dilateByReconstruction(mask, recSize);
        Tools.save(mask);

        mask.autoThreshold();
        original.setMask(mask.resize(original.getWidth(), original.getHeight()).convertToByte(true));

        original.setMask(mask.convertToByte(true));
        return original;
    }
}