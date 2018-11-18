package evaluation;

import ij.process.ImageProcessor;

public abstract interface UnsupervisedFilter
{
    public abstract void run(ImageProcessor paramImageProcessor, Object[] paramArrayOfObject);

    public abstract Object[] getDefaultParams();
}