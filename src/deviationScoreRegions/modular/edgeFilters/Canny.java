package deviationScoreRegions.modular.edgeFilters;

import basics.filter.canny.CannyEdgeDetection;
import ij.process.ImageProcessor;

public class Canny
        implements EdgeFilter
{
    public void run(ImageProcessor i)
    {
        double radius = 3.0D;
        float alpha = 0.5F;
        float upper = 100.0F;
        float lower = 50.0F;

        ImageProcessor edges = CannyEdgeDetection.run(i.convertToByte(true), radius, alpha, upper, lower);
        i.copyBits(edges, 0, 0, 0);
    }
}