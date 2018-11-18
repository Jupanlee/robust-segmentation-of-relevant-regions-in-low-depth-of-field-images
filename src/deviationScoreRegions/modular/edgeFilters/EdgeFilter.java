package deviationScoreRegions.modular.edgeFilters;

import ij.process.ImageProcessor;

public abstract interface EdgeFilter
{
    public abstract void run(ImageProcessor paramImageProcessor);
}