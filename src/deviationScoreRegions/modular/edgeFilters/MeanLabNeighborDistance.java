package deviationScoreRegions.modular.edgeFilters;

import basics.MImage;
import basics.Tools;
import ij.process.ImageProcessor;
import java.awt.Rectangle;

public class MeanLabNeighborDistance
        implements EdgeFilter
{
    private final int radius = 1;
    private final int power = 1;

    public void run(ImageProcessor ip)
    {
        ImageProcessor edges = ip.duplicate().convertToByte(true);
        MImage mImage = new MImage(ip);
        Rectangle roi = ip.getRoi();
        for (int x = roi.x; x < roi.x + roi.width; x++) {
            for (int y = roi.y; y < roi.y + roi.height; y++) {
                double deltaE = Tools.euklidDistance(mImage.getLab(x, y), mImage.getNeigbourMeanLab(x, y, 1, true));
                int value = (int)Math.pow(deltaE, 1.0D);
                edges.putPixel(x, y, value);
            }
        }
        ip.copyBits(edges, 0, 0, 0);
    }
}