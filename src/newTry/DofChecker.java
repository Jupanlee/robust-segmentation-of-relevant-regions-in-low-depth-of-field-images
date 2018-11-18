package newTry;

import basics.Tools;
import basics.javaAddons.DEBUG;
import basics.pointSetOperations.clustering.dbscan.ColorDBSCAN_lab;
import deviationScoreRegions.grow.ColorRegion;
import deviationScoreRegions.grow.ColorRegionTools;
import deviationScoreRegions.modular.ScoreClustering;
import deviationScoreRegions.modular.scoreImage.DeviationScoreImage;
import deviationScoreRegions.modular.scoreImage.DifferenceScoreImage.DifferenceScoreImageParameter;
import deviationScoreRegions.modular.scoreImage.ScoreImage;
import deviationScoreRegions.modular.scoreImage.ScoreImageTools;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import others.morphological.Morphological;

public class DofChecker
{
    public static void main(String[] args)
            throws Exception
    {
        DEBUG.setVerbose(false);

        int count = 0;
        int maxSize = 800;

        List fileNames = Tools.getFilesFromDirectory("../../images/flickrtmp", ".jpg");
        Collections.shuffle(fileNames, new Random(0L));
        for (String fileName : fileNames) {
            ImageProcessor imageProcessor = Tools.loadImageProcessor(fileName);
            if (Tools.getLongestSide(imageProcessor) > maxSize) {
                imageProcessor = Tools.resize(imageProcessor, maxSize);
            }

            hasDof(imageProcessor);
        }
    }

    private static double getBlackRatio(ImageProcessor imageProcessor) {
        return Tools.getPixelCount(imageProcessor, 0) / imageProcessor.getPixelCount();
    }

    public static double closedScoreImageCover(ScoreImage scoreImage) {
        ImageProcessor closedScoreImage = scoreImage.getImageProcessor();

        int size = (int)(Math.sqrt(closedScoreImage.getPixelCount()) * 0.15D);
        closedScoreImage = Morphological.close(closedScoreImage, size);
        closedScoreImage = Morphological.open(closedScoreImage, size / 2);
        closedScoreImage.threshold(127);
        Tools.save(Tools.write("closedScoreImage", closedScoreImage));

        int whitePixelFromClosedScoreImage = Tools.getPixelCount(closedScoreImage, 1.0D, 1.7976931348623157E+308D);
        int totalPixelsFromScoreImage = scoreImage.getImageProcessor().getWidth() * scoreImage.getImageProcessor().getHeight();

        return whitePixelFromClosedScoreImage / totalPixelsFromScoreImage;
    }

    public static double scoreclustetringDensity(ScoreImage scoreImage) {
        int size = (int)(Math.sqrt(scoreImage.getImageProcessor().getPixelCount()) * 0.01D);
        ImageProcessor closed = Morphological.close(scoreImage.getImageProcessor(), size);
        int closedPixel = Tools.getPixelCount(closed, 1.0D, 1.7976931348623157E+308D);
        int scoreImagePixel = Tools.getPixelCount(scoreImage.getImageProcessor(), 1.0D, 1.7976931348623157E+308D);
        Tools.save(Tools.write("scoreclustetringDensity", closed));
        return scoreImagePixel / (closedPixel + 1);
    }

    public static double scoreClusteringClearness(ScoreClustering scoreClustering) {
        if (scoreClustering.getClusters().isEmpty()) {
            return 0.0D;
        }
        int pointsInBiggestClusters = 0;
        int minClusterSize = ((List)scoreClustering.getClusters().get(0)).size() / 2;
        for (List cluster : scoreClustering.getClusters()) {
            if (cluster.size() >= minClusterSize) {
                pointsInBiggestClusters += cluster.size();
            }
        }

        return pointsInBiggestClusters / scoreClustering.getScorePoints().size();
    }

    public static double overlap(ScoreImage scoreImage, ImageProcessor mask)
    {
        ImageProcessor scoreImageProcessor = scoreImage.getImageProcessor();
        int whiteScoreImagePixel = Tools.getPixelCount(scoreImageProcessor, 1.0D, 1.7976931348623157E+308D);
        ImageProcessor difference = new ColorProcessor(mask.getWidth(), mask.getHeight());
        ImageProcessor dilatedMask = Tools.dilate(mask.convertToByte(true), 3);

        Tools.save(Tools.write("overlap reference", dilatedMask));

        int whiteScoreImagePixelWithinMask = 0;
        for (int x = 0; x < scoreImageProcessor.getWidth(); x++) {
            for (int y = 0; y < scoreImageProcessor.getHeight(); y++) {
                if (scoreImageProcessor.getPixel(x, y) > 0) {
                    if (dilatedMask.getPixel(x, y) > 0) {
                        whiteScoreImagePixelWithinMask++;
                        difference.putPixel(x, y, Color.green.getRGB());
                    } else {
                        difference.putPixel(x, y, Color.red.getRGB());
                    }
                }
            }
        }

        double overlap = whiteScoreImagePixelWithinMask / whiteScoreImagePixel;
        Tools.save(Tools.write("overlap = " + Tools.formatNumber(overlap), difference));
        return overlap;
    }

    public static boolean hasDof(ImageProcessor original) {
        Tools.save(original);

        DifferenceScoreImage.DifferenceScoreImageParameter sp = new DifferenceScoreImage.DifferenceScoreImageParameter();
        sp.setMaxSize(400);
        sp.setSigmaBlur(2.0D);
        sp.setSigmaPreBlur(2.0D);
        ScoreImage scoreImage = new DeviationScoreImage();
        scoreImage.generateScore(original);
        ImageProcessor resized = original.resize(scoreImage.getImageProcessor().getWidth(), scoreImage.getImageProcessor().getHeight());
        ScoreClustering scoreClustering = new ScoreClustering(scoreImage);
        Tools.save(scoreClustering.getScoreImage().getImageProcessor());

        int neighbourRadius = 1;
        int deltaE = 40;

        boolean useDBSCAN = true;

        ImageProcessor regionsImage = new ColorProcessor(resized.getWidth(), resized.getHeight());
        List colorRegions;
        List colorRegions;
        if (useDBSCAN) {
            ColorDBSCAN_lab colorDbscan = new ColorDBSCAN_lab();
            colorDbscan.setDeltaEToBeSimilar(deltaE);
            colorDbscan.setDeltaEToBeSimilarToMean(100.0D);
            List clusters = colorDbscan.get(resized, 1, 4);
            colorRegions = ColorRegionTools.pointListsToColorRegions(clusters);
        } else {
            colorRegions = ColorRegionTools.getRegionsFromImageProcessor(resized, deltaE);
        }

        double meanRelScore = ColorRegionTools.getMeanRelativeScore(colorRegions, scoreImage.getImageProcessor(), 1);
        double maxScore = Tools.getMax(ColorRegionTools.getRelativeScores(colorRegions, scoreImage.getImageProcessor(), 1));
        for (ColorRegion colorRegion : colorRegions) {
            double score = ColorRegionTools.getRelativeScore(colorRegion, scoreImage.getImageProcessor(), 1);
            int v = Math.min(255, (int)(255.0D * score / maxScore));
            ColorRegionTools.draw(regionsImage, colorRegion, new Color(v, v, v));
        }

        ScoreImageTools.emphasise(scoreImage, scoreClustering, 0.5D);
        Tools.save(Tools.write("emphasised scoreImage", scoreImage.getImageProcessor()));

        ImageProcessor colorScoreGrowMask = regionsImage.duplicate().convertToByte(true);
        colorScoreGrowMask.threshold(20);
        Tools.save(colorScoreGrowMask);

        double overlap = overlap(scoreImage, colorScoreGrowMask);
        double scoreClusteringDensity = scoreclustetringDensity(scoreImage);
        double scoreClusteringClearness = scoreClusteringClearness(scoreClustering);

        int whitePixelFromClosedScoreImage = Tools.getPixelCount(colorScoreGrowMask, 1.0D, 1.7976931348623157E+308D);
        int totalPixelsFromScoreImage = scoreImage.getImageProcessor().getWidth() * scoreImage.getImageProcessor().getHeight();
        double closedScoreImageCover = closedScoreImageCover(scoreImage);

        double scoreImageInstability = ScoreImageTools.scoreImageInstability(scoreImage);

        boolean hasDof = (overlap > 0.7D) && (scoreClusteringDensity > 0.33D) && (scoreClusteringClearness > 0.66D) && (closedScoreImageCover > 0.05D) && (closedScoreImageCover < 0.55D);

        Color color = hasDof ? Color.green : Color.red;

        String text = "meanRelScore=" + Tools.formatNumber(meanRelScore) + "\n" + "overlap=" + Tools.formatNumber(overlap) + "\n" + "scoreClusteringDensity=" + Tools.formatNumber(scoreClusteringDensity) + "\n" + "scoreClusteringClearness=" + Tools.formatNumber(scoreClusteringClearness) + "\n" + "closedScoreImageCover=" + Tools.formatNumber(closedScoreImageCover) + "\n" + "maxRelScore=" + Tools.formatNumber(maxScore);

        Tools.save(Tools.write(text, regionsImage, color));

        System.out.println(text.replaceAll("\n", "\t"));

        return hasDof;
    }
}