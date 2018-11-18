package deviationScoreRegions.modular;

import basics.Tools;
import deviationScoreRegions.grow.ColorRegion;
import deviationScoreRegions.grow.ColorRegionTools;
import deviationScoreRegions.modular.scoreImage.DeviationScoreImage;
import deviationScoreRegions.modular.scoreImage.ScoreImage;
import evaluation.Batch;
import evaluation.Batch.Batchable;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.util.List;

public class ColorScoreRegions
        implements Batch.Batchable
{
    private final int neighbourRadius = 1;

    public ImageProcessor run(ImageProcessor original, ScoreImage scoreImage, int size, double blur, double deltaE) {
        ImageProcessor resized = Tools.blur(Tools.resize(original, size), blur);
        Tools.save(resized);
        ImageProcessor regionsImage = new ColorProcessor(resized.getWidth(), resized.getHeight());
        List colorRegions = ColorRegionTools.getRegionsFromImageProcessor(resized, deltaE);

        ImageProcessor scoreImageProcessor = scoreImage.getImageProcessor().resize(resized.getWidth(), resized.getHeight());
        double maxScore = Tools.getMax(ColorRegionTools.getRelativeScores(colorRegions, scoreImageProcessor, 1));
        for (ColorRegion colorRegion : colorRegions) {
            double score = ColorRegionTools.getRelativeScore(colorRegion, scoreImageProcessor, 1);
            int v = Math.min(255, (int)(255.0D * score / maxScore));
            ColorRegionTools.draw(regionsImage, colorRegion, new Color(v, v, v));
        }

        return regionsImage.resize(original.getWidth(), original.getHeight());
    }

    public ImageProcessor run(ImageProcessor i)
    {
        return run(i, new DeviationScoreImage(), 1, 1.0D, 20.0D);
    }

    public static void main(String[] args) {
        Batch.run(new ColorScoreRegions());
    }
}