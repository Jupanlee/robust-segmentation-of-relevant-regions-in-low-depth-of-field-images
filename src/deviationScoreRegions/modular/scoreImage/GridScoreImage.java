package deviationScoreRegions.modular.scoreImage;

import basics.Tools;
import deviationScoreRegions.modular.edgeFilters.EdgeFilter;
import deviationScoreRegions.modular.edgeFilters.MeanLabNeighborDistance;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Rectangle;
import java.io.IOException;

public class GridScoreImage extends ScoreImage
{
    private final int maxDepth;
    private final EdgeFilter edgeFilter;
    private final double minEdgesMeanValue;
    private final int edgesStartBin;
    private final int size;
    private ImageProcessor imageProcessor;

    private GridScoreImage(int maxDepth, EdgeFilter edgeFilter, double minEdgesMeanValue, int size, int edgesStartBin)
    {
        this.maxDepth = maxDepth;
        this.edgeFilter = edgeFilter;
        this.minEdgesMeanValue = minEdgesMeanValue;
        this.size = size;
        this.edgesStartBin = edgesStartBin;
    }

    private GridScoreImage() {
        this(8, new MeanLabNeighborDistance(), 33.0D, 1024, 25);
    }

    private void split(Rectangle roi, int depth) {
        if (depth < this.maxDepth)
        {
            double blur = 1.1D + depth / this.maxDepth * 0.2D;
            this.imageProcessor.setRoi(roi);
            ImageProcessor a = this.imageProcessor.crop();
            ImageProcessor b = Tools.blur(this.imageProcessor.crop(), blur);

            ImageProcessor edges = Tools.difference(a, b).convertToByte(true);

            edges.sqr();

            if (depth < 2) {
                Tools.save(Tools.write("mean == " + Tools.formatNumber(Tools.mean(edges, this.edgesStartBin)) + "\nblur == " + Tools.formatNumber(blur), edges));
            }

            if (Tools.mean(edges, this.edgesStartBin) > this.minEdgesMeanValue)
            {
                double value = Math.pow(depth / this.maxDepth, 2.0D) * 256.0D;
                incPixels(this.scoreImageProcessor, roi, value);

                int halfWidth = roi.width / 2;
                int halfHeight = roi.height / 2;

                split(new Rectangle(roi.x, roi.y, halfWidth, halfHeight), depth + 1);
                split(new Rectangle(roi.x + halfWidth, roi.y, halfWidth, halfHeight), depth + 1);
                split(new Rectangle(roi.x, roi.y + halfHeight, halfWidth, halfHeight), depth + 1);
                split(new Rectangle(roi.x + halfWidth, roi.y + halfHeight, halfWidth, halfHeight), depth + 1);
            }
        }
    }

    private static void incPixels(ImageProcessor i, Rectangle roi, double value) {
        for (int x = roi.x; x < roi.x + roi.width; x++)
            for (int y = roi.y; y < roi.y + roi.height; y++)
                i.putPixelValue(x, y, i.getPixelValue(x, y) + value);
    }

    public ImageProcessor generateScore(ImageProcessor imageProcessor)
    {
        this.imageProcessor = imageProcessor.resize(this.size, this.size);
        this.scoreImageProcessor = new ByteProcessor(this.size, this.size);
        split(new Rectangle(0, 0, this.size, this.size), 0);
        Tools.save(this.scoreImageProcessor);
        return null;
    }

    public static void main(String[] args) throws IOException {
        for (String fileName : Tools.getFilesFromDirectory("../../images/schwierig", ".jpg")) {
            ImageProcessor i = Tools.loadImageProcessor(fileName, 1024);
            new GridScoreImage().generateScore(i);
        }
    }
}