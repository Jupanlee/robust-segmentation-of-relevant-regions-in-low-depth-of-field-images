package basics.pointSetOperations.clustering.dbscan;

import basics.MColor;
import basics.Tools;
import deviationScoreRegions.DeviationScoreRegions_Tuned;
import evaluation.Batch;
import ij.process.ImageProcessor;
import java.awt.Point;

public class ColorScoreDBSCAN extends ColorDBSCAN_lab
{
    double preBlur = 1.25D;
    int preBlurIterations = 3;
    double blur = this.preBlur;
    double scoreImageBlur = 50.0D;
    static double labDifferencePower = 2.0D;
    private ImageProcessor scoreImage;
    double maxDeltaEToMean = 40.0D;
    double maxDeltaEToCore = 15.0D;
    double maxDeltaEBonus = 0.0D;

    private void calcScoreImage(ImageProcessor imageProcessor)
    {
        ImageProcessor preBlurred = Tools.blur(imageProcessor, this.preBlur, this.preBlurIterations);
        ImageProcessor difference = Tools.difference(DeviationScoreRegions_Tuned.getEdges(preBlurred), DeviationScoreRegions_Tuned.getEdges(Tools.blur(preBlurred, this.blur)));
        this.scoreImage = Tools.power(difference, labDifferencePower);

        Tools.save(this.scoreImage);
        this.scoreImage = Tools.blur(this.scoreImage, this.scoreImageBlur);
        this.scoreImage.multiply(2.0D);
        Tools.save(this.scoreImage);
    }

    public ColorScoreDBSCAN(int minPts, int epsilon) {
        super(minPts, epsilon);
    }

    protected boolean similar(Point neighbour, Point corePoint)
    {
        double distanceToMean = this.meanClusterColor == null ? 0.0D : this.labColorField[neighbour.x][neighbour.y].getLabDeltaE(this.meanClusterColor);
        double distanceToCorePoint = this.labColorField[neighbour.x][neighbour.y].getLabDeltaE(this.labColorField[corePoint.x][corePoint.y]);

        double bonus = this.maxDeltaEBonus * Tools.minDivideMax(this.scoreImage.getPixelValue(neighbour.x, neighbour.y), this.scoreImage.getPixelValue(corePoint.x, corePoint.y));

        return (distanceToCorePoint <= this.maxDeltaEToCore + bonus) && (distanceToMean <= this.maxDeltaEToMean + bonus);
    }

    public ImageProcessor run(ImageProcessor original)
    {
        calcScoreImage(original);
        return super.run(original);
    }

    public static void main(String[] args) {
        int size = 500;
        int epsilon = size / 50;
        int minPts = epsilon * epsilon / 2;
        Batch.run(new ColorScoreDBSCAN(minPts, epsilon), size);
    }
}