package deviationScoreRegions.GUI;

import ij.process.ImageProcessor;

public abstract interface DebugInfoShower
{
    public abstract void showImage(ImageProcessor paramImageProcessor);

    public abstract void showText(String paramString);
}