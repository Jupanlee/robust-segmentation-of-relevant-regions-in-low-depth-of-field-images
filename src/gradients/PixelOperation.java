package gradients;

import ij.process.ImageProcessor;

public abstract class PixelOperation
{
    public abstract double getPixelColor(ImageProcessor paramImageProcessor, int paramInt1, int paramInt2);

    public static ImageProcessor renderImage(ImageProcessor original, ImageProcessor renderToImage, PixelOperation pixelOperation)
    {
        double[][] doublePixels = getDoulbePixels(original, pixelOperation);
        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                renderToImage.putPixelValue(x, y, doublePixels[x][y]);
            }
        }
        return renderToImage;
    }

    public static ImageProcessor renderImage(ImageProcessor original, PixelOperation pixelOperation) {
        ImageProcessor resultImage = original.duplicate();
        return renderImage(original, resultImage, pixelOperation);
    }

    public static double[][] getDoulbePixels(ImageProcessor imageProcessor, PixelOperation pixelOperation) {
        double[][] doublePixels = new double[imageProcessor.getWidth()][imageProcessor.getHeight()];
        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++) {
                doublePixels[x][y] = pixelOperation.getPixelColor(imageProcessor, x, y);
            }
        }
        return doublePixels;
    }
}