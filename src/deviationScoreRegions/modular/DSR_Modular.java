//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions.modular;

import basics.ProgressListener;
import basics.SaveImageProgressListener;
import basics.Tools;
import deviationScoreRegions.modular.edgeFilters.MeanLabNeighborDistance;
import deviationScoreRegions.modular.scoreImage.DeviationScoreImage;
import deviationScoreRegions.modular.scoreImage.ScoreImage;
import evaluation.Batch;
import evaluation.Batch.Batchable;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

public class DSR_Modular implements Batchable {
    private final ScoreImage scoreImage;
    private final int maxSize;
    private final double dist;
    private final double rel;
    private ProgressListener progressListener;

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public DSR_Modular(ScoreImage scoreImage, int maxSize, double dist, double rel) {
        this.progressListener = new SaveImageProgressListener();
        this.scoreImage = scoreImage;
        this.maxSize = maxSize;
        this.dist = dist;
        this.rel = rel;
    }

    public DSR_Modular() {
        this(new DeviationScoreImage(new MeanLabNeighborDistance(), 2, 0.925D, 1.25D), 450, 20.0D, 0.75D);
    }

    public DSR_Modular(ProgressListener progressListener) {
        this();
        this.progressListener = progressListener;
    }

    public ImageProcessor run(ImageProcessor original) {
        this.progressListener.progressUpdate(0.0D, "Deviation Scoring");
        this.progressListener.updateImage(original);
        this.scoreImage.generateScore(original);
        this.progressListener.updateImage(this.scoreImage.getImageProcessor());
        this.progressListener.progressUpdate(0.1D, "Score Clustering");
        this.scoreImage.maxSize(this.maxSize);
        ScoreClustering scoreClustering = new ScoreClustering(this.scoreImage);
        this.progressListener.progressUpdate(0.2D, "Mask Approximation");
        ApproximationMask approximationMask = new ApproximationMask(scoreClustering, this.progressListener);
        this.progressListener.updateImage(approximationMask.getImageProcessor());
        this.progressListener.progressUpdate(0.5D, "Region Scoring");
        ImageProcessor mask = (new ScoreRegions(this.dist, this.rel, this.progressListener)).removeLowScoreRegions(original, approximationMask);
        this.progressListener.updateImage(mask);
        this.progressListener.progressUpdate(0.1D, "Done.");
        original.setMask(mask.resize(original.getWidth(), original.getHeight()));
        return Tools.maskBackground(original, Color.blue);
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException {
        Batch.run(new DSR_Modular(), 800, "../../images/schwierig");
    }
}
