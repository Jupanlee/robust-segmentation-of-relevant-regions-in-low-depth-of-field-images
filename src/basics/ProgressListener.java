package basics;

import ij.process.ImageProcessor;

public abstract interface ProgressListener
{
    public abstract void progressUpdate(double paramDouble, String paramString);

    public abstract void updateImage(ImageProcessor paramImageProcessor);
}