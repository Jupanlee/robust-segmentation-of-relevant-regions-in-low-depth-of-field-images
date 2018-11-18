package deviationScoreRegions.modular.scoreImage;

import basics.Tools;
import ij.process.ImageProcessor;

public abstract class ScoreImage
{
    protected ImageProcessor scoreImageProcessor;

    public abstract ImageProcessor generateScore(ImageProcessor paramImageProcessor);

    public ImageProcessor getImageProcessor()
    {
        return this.scoreImageProcessor;
    }

    public void maxSize(int longestSideSize) {
        if (Tools.getLongestSide(this.scoreImageProcessor) > longestSideSize)
            this.scoreImageProcessor = Tools.resize(this.scoreImageProcessor, longestSideSize);
    }
}