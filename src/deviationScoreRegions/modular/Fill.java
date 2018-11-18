package deviationScoreRegions.modular;

import basics.Tools;
import deviationScoreRegions.modular.scoreImage.DeviationScoreImage;
import ij.process.FloodFiller;
import ij.process.ImageProcessor;
import java.io.IOException;

public class Fill
{
    public ImageProcessor run(ImageProcessor i)
    {
        ImageProcessor imageProcessor = i.duplicate();

        int width = imageProcessor.getWidth();
        int height = imageProcessor.getHeight();
        int X = width - 1; int Y = height - 1;
        int[][] pixel = new int[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixel[x][y] = imageProcessor.getPixel(x, y);
            }
        }

        FloodFiller ff = new FloodFiller(imageProcessor);
        imageProcessor.setColor(127);

        for (int y = 0; y < height; y++) {
            if (imageProcessor.getPixel(0, y) == 0) {
                ff.fill8(0, y);
            }
            if (imageProcessor.getPixel(X, y) == 0) {
                ff.fill8(X, y);
            }
        }
        for (int x = 0; x < width; x++) {
            if (imageProcessor.getPixel(x, 0) == 0) {
                ff.fill8(x, 0);
            }
            if (imageProcessor.getPixel(x, Y) == 0) {
                ff.fill8(x, Y);
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (imageProcessor.getPixel(x, y) == 0)
                    imageProcessor.putPixel(x, y, 255);
                else {
                    imageProcessor.putPixel(x, y, pixel[x][y]);
                }
            }
        }

        return imageProcessor;
    }

    public static void main(String[] args) throws IOException {
        for (String fileName : Tools.getFilesFromDirectory("../../images/base", ".jpg")) {
            ImageProcessor original = Tools.resize(Tools.loadImageProcessor(fileName), 500);
            ImageProcessor scoreImageProcessor = new DeviationScoreImage().generateScore(original);
            scoreImageProcessor.threshold(128);

            Tools.save(scoreImageProcessor);
            new Fill().run(scoreImageProcessor);
            Tools.save(scoreImageProcessor);
        }
    }
}