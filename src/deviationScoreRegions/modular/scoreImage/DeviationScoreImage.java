package deviationScoreRegions.modular.scoreImage;

import basics.Tools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.modular.Statistics;
import deviationScoreRegions.modular.edgeFilters.EdgeFilter;
import deviationScoreRegions.modular.edgeFilters.MeanLabNeighborDistance;
import ij.plugin.filter.GaussianBlur;
import ij.process.ImageProcessor;
import java.io.IOException;
import java.io.PrintStream;

public class DeviationScoreImage extends ScoreImage
{
    private final EdgeFilter edgeFilter;
    private final int iterations;
    private final double blurRadius;
    private final double preBlur;

    public DeviationScoreImage(EdgeFilter edgeFilter, int iterations, double blurRadius, double preBlur)
    {
        this.edgeFilter = edgeFilter;
        this.iterations = iterations;
        this.blurRadius = blurRadius;
        this.preBlur = preBlur;
    }

    public DeviationScoreImage() {
        this(new MeanLabNeighborDistance(), 10, 0.95D, 0.0D);
    }

    private ImageProcessor[] getEdges(ImageProcessor ip) {
        ImageProcessor[] edges = new ImageProcessor[this.iterations];
        GaussianBlur gaussianBlur = new GaussianBlur();

        double blur = this.blurRadius;
        System.out.println("blur radius == " + blur);

        ImageProcessor blured = ip.duplicate();
        if (this.preBlur > 0.0D) {
            System.out.println("preblur with " + this.preBlur);
            gaussianBlur.blur(blured, this.preBlur);
        }

        for (int i = 0; i < edges.length; i++) {
            edges[i] = blured.duplicate();
            this.edgeFilter.run(edges[i]);
            if ((DEBUG.getVerbose()) && (i == 0)) {
                ImageProcessor debug = edges[0].duplicate();
                debug.sqr();
                Tools.save(debug);
            }

            gaussianBlur.blur(blured, blur);
        }

        return edges;
    }

    public ImageProcessor generateScore(ImageProcessor original)
    {
        ImageProcessor[] edges = getEdges(original);
        this.scoreImageProcessor = (this.iterations > 1 ? Statistics.standardDeviation(edges) : edges[0].duplicate().convertToByte(true));

        this.scoreImageProcessor = smooth(this.scoreImageProcessor, 0.2D);
        this.scoreImageProcessor.sqr();
        return this.scoreImageProcessor;
    }

    public static ImageProcessor smooth(ImageProcessor imageProcessor, double megapixel) {
        ImageProcessor med = megapixel < imageProcessor.getPixelCount() ? Tools.resize(imageProcessor, megapixel) : imageProcessor.duplicate();
        med = med.convertToByte(true);
        med.smooth();
        return med.resize(imageProcessor.getWidth(), imageProcessor.getHeight());
    }

    public static void main(String[] args) throws IOException
    {
        EdgeFilter edgeFilter = new MeanLabNeighborDistance();
        int size = 512;
        for (String fileName : Tools.getFilesFromDirectory("../../images/schwierig", ".jpg")) {
            ImageProcessor original = Tools.loadImageProcessor(fileName, size).convertToByte(true);
            ScoreImage scoreImage = new DeviationScoreImage(edgeFilter, 2, 0.85D, 0.0D);
            scoreImage.generateScore(original);
            Tools.save(scoreImage.getImageProcessor());
        }
    }
}