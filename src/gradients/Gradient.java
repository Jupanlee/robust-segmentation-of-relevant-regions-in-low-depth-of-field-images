package gradients;

import ij.process.ImageProcessor;

public class Gradient
{
    ImageProcessor imageProcessor;
    int x;
    int y;
    float[] prewittY = { -0.1666667F, -0.1666667F, -0.1666667F, 0.0F, 0.0F, 0.0F, 0.1666667F, 0.1666667F, 0.1666667F };

    float[] prewittX = { -0.1666667F, 0.0F, 0.1666667F, -0.1666667F, 0.0F, 0.1666667F, -0.1666667F, 0.0F, 0.1666667F };

    float[] sobelY = { -0.125F, -0.25F, -0.125F, 0.0F, 0.0F, 0.0F, 0.125F, 0.25F, 0.125F };
    float[] sobelX = { -0.125F, 0.0F, 0.125F, -0.25F, 0.0F, 0.25F, -0.125F, 0.0F, 0.125F };

    float[] optimizedX = { -0.09375F, 0.0F, 0.09375F, -0.3125F, 0.0F, 0.3125F, -0.09375F, 0.0F, 0.09375F };

    float[] optimizedY = { -0.09375F, -0.3125F, -0.09375F, 0.0F, 0.0F, 0.0F, 0.09375F, 0.3125F, 0.09375F };
    double Dx;
    double Dy;

    private double calc(float[] kernel, int x, int y, ImageProcessor imageProcessor)
    {
        if ((x - 1 < 0) || (y - 1 < 0) || (x + 1 >= imageProcessor.getWidth()) || (y + 1 >= imageProcessor.getHeight())) {
            return 0.0D;
        }

        double[] convolvedValues = new double[9];

        convolvedValues[0] = (imageProcessor.getPixel(x - 1, y - 1) * kernel[0]);
        convolvedValues[1] = (imageProcessor.getPixel(x, y - 1) * kernel[1]);
        convolvedValues[2] = (imageProcessor.getPixel(x + 1, y - 1) * kernel[2]);

        convolvedValues[3] = (imageProcessor.getPixel(x - 1, y) * kernel[3]);
        convolvedValues[4] = (imageProcessor.getPixel(x, y) * kernel[4]);
        convolvedValues[5] = (imageProcessor.getPixel(x + 1, y) * kernel[5]);

        convolvedValues[6] = (imageProcessor.getPixel(x - 1, y + 1) * kernel[6]);
        convolvedValues[7] = (imageProcessor.getPixel(x, y + 1) * kernel[7]);
        convolvedValues[8] = (imageProcessor.getPixel(x + 1, y + 1) * kernel[8]);

        double sum = 0.0D;
        for (double value : convolvedValues) {
            sum += value;
        }

        return sum;
    }

    public Gradient(ImageProcessor imageProcessor, int x, int y) {
        this.x = x;
        this.y = y;
        this.imageProcessor = imageProcessor;

        float[] kernelX = this.sobelX;
        float[] kernelY = this.sobelY;

        this.Dx = calc(kernelX, x, y, imageProcessor);
        this.Dy = calc(kernelY, x, y, imageProcessor);
    }

    public double getStrength() {
        return Math.min(255.0D, Math.sqrt(this.Dx * this.Dx + this.Dy * this.Dy));
    }

    public double getDirection() {
        return Math.atan2(this.Dy, this.Dx);
    }
}