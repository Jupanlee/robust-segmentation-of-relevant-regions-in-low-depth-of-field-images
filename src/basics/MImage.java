package basics;

import ij.process.ImageProcessor;

public class MImage
{
    private int width;
    private int height;
    private double[][][] pixel;

    private void initLabPixels(ImageProcessor imageProcessor)
    {
        this.pixel = new double[this.width][this.height][3];
        for (int x = 0; x < imageProcessor.getWidth(); x++)
            for (int y = 0; y < imageProcessor.getHeight(); y++)
                this.pixel[x][y] = ColorConversions.getLab(imageProcessor.getPixel(x, y));
    }

    public MImage(ImageProcessor imageProcessor)
    {
        this.width = imageProcessor.getWidth();
        this.height = imageProcessor.getHeight();
        initLabPixels(imageProcessor);
    }

    public double[] getNeigbourMeanLab(int x, int y, int radius) {
        return getNeigbourMeanLab(x, y, radius, true);
    }

    public double[] getNeigbourMeanLab(int x, int y, int radius, boolean includeCenter) {
        double[] labMean = { 0.0D, 0.0D, 0.0D };

        int count = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if ((nx < 0) || (ny < 0) || (nx >= this.width) || (ny >= this.height) || (
                        (!includeCenter) && (nx == x) && (ny == y))) continue;
                count++;
                for (int i = 0; i < 3; i++) {
                    labMean[i] += this.pixel[nx][ny][i];
                }

            }

        }

        labMean[0] /= count;
        labMean[1] /= count;
        labMean[2] /= count;

        return labMean;
    }

    public double[] getLab(int x, int y) {
        return this.pixel[x][y];
    }
}