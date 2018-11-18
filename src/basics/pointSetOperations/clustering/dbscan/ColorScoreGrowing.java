package basics.pointSetOperations.clustering.dbscan;

import basics.MColor;
import basics.Tools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.DeviationScoreRegions_Tuned;
import deviationScoreRegions.grow.ColorRegionTools;
import deviationScoreRegions.grow.Grow;
import evaluation.Batch;
import evaluation.Batch.Batchable;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.util.List;

public class ColorScoreGrowing extends Grow
        implements Batch.Batchable
{
    double minEuklidDistToBeSimilar = 20.0D;
    double preBlur = 1.5D;
    int preBlurIterations = 1;
    double blur = 2.0D;
    double scoreImageBlur = 0.0D;
    int scoreImageThreshold = 63;
    static double labDifferencePower = 1.0D;
    private ImageProcessor scoreImage;
    double maxDeltaEBonus = 0.0D;

    public static void main(String[] args) {
        DEBUG.setVerbose(false);
        Batch.run(new ColorScoreGrowing(), 500);
    }

    private Point findPoint(ImageProcessor i, Point p, double dX, double dY, double shiftX, double shiftY, double maxShift)
    {
        return null;
    }

    private void calcScoreImage(ImageProcessor imageProcessor) {
        ImageProcessor preBlurred = Tools.blur(imageProcessor, this.preBlur, this.preBlurIterations);
        ImageProcessor difference = Tools.difference(DeviationScoreRegions_Tuned.getEdges(preBlurred), DeviationScoreRegions_Tuned.getEdges(Tools.blur(preBlurred, this.blur)));
        this.scoreImage = Tools.power(difference, labDifferencePower);

        Tools.save(this.scoreImage);

        this.scoreImage.medianFilter();

        Tools.save(Tools.cropToMask(imageProcessor, this.scoreImage));
    }

    protected boolean similar(Point reference, Point newPoint)
    {
        MColor labA = this.labColorField[reference.x][reference.y];
        MColor labB = this.labColorField[newPoint.x][newPoint.y];

        double bonus = this.maxDeltaEBonus * Tools.minDivideMax(this.scoreImage.getPixelValue(reference.x, reference.y), this.scoreImage.getPixelValue(newPoint.x, newPoint.y));
        double deltaE = this.minEuklidDistToBeSimilar + bonus;

        return labA.getLabDeltaE(labB) <= deltaE;
    }

    public ImageProcessor run(ImageProcessor imageProcessor)
    {
        calcScoreImage(imageProcessor);
        initGrow(imageProcessor, this.minEuklidDistToBeSimilar);
        List regions = getRegions(imageProcessor, this.minEuklidDistToBeSimilar);
        return ColorRegionTools.draw(regions, imageProcessor.getWidth(), imageProcessor.getHeight());
    }
}