package others.morphological;

import basics.Tools;
import ij.process.ImageProcessor;
import java.io.IOException;
import java.util.Vector;

public class AutoParameter
{
    private static FlatRegions getFlatSortedRegions(ImageProcessor imageProcessor)
    {
        FlatRegions flatRegions = new FlatRegions();
        flatRegions.initFlatRegions(Tools.getValuesFromImageProcessor(imageProcessor), 0, 0);

        flatRegions.sortRegions();
        flatRegions.removeDuplicates();

        return flatRegions;
    }

    private static int pixelCountToRadians(int pixelCount) {
        return (int)Math.sqrt(pixelCount / 3.141592653589793D);
    }

    public static int boxplotMean(ImageProcessor imageProcessor, double left, double right, double maxHolePixelSizePercentage) {
        int meanHolePixelCount = 0;

        int pixelCount = imageProcessor.getWidth() * imageProcessor.getHeight();
        int maxHolePixelSize = (int)Math.round(pixelCount * maxHolePixelSizePercentage);

        FlatRegions flatRegions = getFlatSortedRegions(imageProcessor);

        flatRegions.boxPlot(left, right);

        int size = 0;
        int regionCount = 0;
        for (BinaryRegion binaryRegion : flatRegions.getRegions(FlatRegions.RegionType.target))
        {
            if (binaryRegion.getPixelCount() <= maxHolePixelSize) {
                size += binaryRegion.getPixelCount();
                regionCount++;
            }

        }

        if (regionCount > 0)
            meanHolePixelCount = size / regionCount;
        else {
            meanHolePixelCount = 0;
        }

        return pixelCountToRadians(meanHolePixelCount);
    }

    public static int median(ImageProcessor imageProcessor) {
        FlatRegions flatRegions = getFlatSortedRegions(imageProcessor);
        Vector targetRegions = flatRegions.getRegions(FlatRegions.RegionType.target);
        int medianIndex = targetRegions.size() / 2;
        return pixelCountToRadians(((BinaryRegion)targetRegions.get(medianIndex)).getPixelCount());
    }

    public static int highestRegionAfterBoxplot(ImageProcessor imageProcessor, double percentage)
    {
        FlatRegions flatRegions = getFlatSortedRegions(imageProcessor);

        flatRegions.boxPlot(percentage);

        Vector targetRegions = flatRegions.getRegions(FlatRegions.RegionType.target);

        BinaryRegion highestRegionAfterBoxplott = (BinaryRegion)targetRegions.get(targetRegions.size() - 1);

        return pixelCountToRadians(highestRegionAfterBoxplott.getPixelCount());
    }

    public static void main(String[] args)
            throws IOException
    {
    }
}