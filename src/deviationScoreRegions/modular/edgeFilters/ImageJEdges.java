package deviationScoreRegions.modular.edgeFilters;

import ij.process.ImageProcessor;

public class ImageJEdges
        implements EdgeFilter
{
    public void run(ImageProcessor i)
    {
        i.findEdges();
    }
}