package evaluation.similarity.similarity;

import basics.Tools;
import evaluation.similarity.similarity.distances.DistanceMetric;
import evaluation.similarity.similarity.distances.JeffreyDivergence;
import evaluation.similarity.similarity.distances.NormedJeffreyDivergence;
import ij.process.ImageProcessor;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

public class ColorHistrogramSimilarity
{
    static DistanceMetric distanceMetric = new JeffreyDivergence();

    public static int[] getColorHistogram(ImageProcessor imageProcessor, int bins) {
        imageProcessor = imageProcessor.convertToByte(true);

        int maxColorValue = 256;

        double colorCountPerBin = maxColorValue / bins;
        int[] colorHistogram = new int[bins];

        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++) {
                int color = imageProcessor.getPixel(x, y);

                if (color <= 0)
                    continue;
                int binNr = (int)(color / colorCountPerBin);
                colorHistogram[binNr] += 1;
            }

        }

        return colorHistogram;
    }

    private static int getSum(int[] histogram) {
        int sum = 0;
        for (int i : histogram) {
            sum += i;
        }
        return sum;
    }

    private static double getSimilarityScore(ImageProcessor imageA, ImageProcessor imageB)
    {
        int longestSide = Tools.getLongestSide(imageA, imageB);
        ImageProcessor imageANormed = Tools.resize(imageA, longestSide);
        ImageProcessor imageBNormed = Tools.resize(imageB, longestSide);

        return distanceMetric.getDistance(imageANormed.getHistogram(), imageBNormed.getHistogram());
    }

    private Vector<ImageProcessor> getSimilarImages(ImageProcessor reference, Vector<ImageProcessor> compareToImages, int scoreThreshold) {
        Vector similarToReferenceImages = new Vector();

        int[] referenceHistogram = reference.getHistogram();

        for (ImageProcessor compareToImage : compareToImages)
        {
            double score = distanceMetric.getDistance(referenceHistogram, compareToImage.getHistogram());
            if (score >= scoreThreshold) similarToReferenceImages.add(compareToImage);
        }

        return similarToReferenceImages;
    }

    public static void main(String[] args) throws IOException
    {
        int bins = 128;
        int size = 400;

        for (Iterator i$ = Tools.getAllFilesFromDirectoryWithSubfolders("data/similarity", ".jpg").iterator(); i$.hasNext(); )
        {
            String referenceFileName = (String)i$.next();

            Vector similarToReference = new Vector();
            ImageProcessor reference = Tools.resize(Tools.loadImageProcessor(referenceFileName), size);
            ImageProcessor referencewithDofExtraction = null;
            Tools.showImage("window1", reference, referenceFileName);

            int[] histogramReference = getColorHistogram(reference, bins);
            int[] histogramReferenceWithDofExtraction = getColorHistogram(referencewithDofExtraction, bins);

            for (String compareToFileName : Tools.getAllFilesFromDirectoryWithSubfolders("data/similarity", ".jpg"))
                if (!compareToFileName.equals(referenceFileName)) {
                    ImageProcessor compareTo = Tools.resize(Tools.loadImageProcessor(compareToFileName), size);
                    ImageProcessor compareTowithDofExtraction = null;

                    int[] histogramCompareTo = getColorHistogram(compareTo, bins);
                    int[] histogramCompareToWithDofExtraction = getColorHistogram(compareTowithDofExtraction, bins);
                    Tools.showImage("window2", compareTo, compareToFileName);

                    int distance = (int)new NormedJeffreyDivergence().getDistance(histogramReference, histogramCompareTo);
                    int distanceWithDofExtraction = (int)new NormedJeffreyDivergence().getDistance(histogramReferenceWithDofExtraction, histogramCompareToWithDofExtraction);

                    System.out.println(referenceFileName + " vs. " + compareToFileName + " -> " + distanceWithDofExtraction + " (" + distance + ")");
                }
        }
        String referenceFileName;
        int[] histogramReference;
        int[] histogramReferenceWithDofExtraction;
    }
}