package gradients;

import basics.Tools;
import evaluation.Batch;
import evaluation.Batch.Batchable;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.io.IOException;

public class Gradients
        implements Batch.Batchable
{
    private boolean drawDirection = false;
    private static double lengthMultiplier = 2.0D;
    private static double strengthColorPower = 1.5D;
    private static double directionColorMultiplier = 5.0D;
    private static double strongGradients = 5.0D;
    private static double chanceToDrawGradient = 0.025D;
    private static double colorPower = 1.25D;

    public Gradients(boolean drawDirection) {
        this.drawDirection = drawDirection;
    }

    public static void drawGradientDirection(Gradient gradient, ImageProcessor image) {
        double strength = gradient.getStrength();
        double angle = gradient.getDirection();

        int dx = (int)(Math.cos(angle) * strength * lengthMultiplier);
        int dy = (int)(Math.sin(angle) * strength * lengthMultiplier);

        if ((Tools.chance(chanceToDrawGradient)) && (strength > strongGradients)) {
            image.setColor(getDirectionColor(strength));
            image.drawLine(gradient.x, gradient.y, gradient.x + dx, gradient.y + dy);
            image.drawRect(gradient.x - 2, gradient.y - 2, 4, 4);
        }
    }

    private static Color getDirectionColor(double strength)
    {
        int v = (int)Math.min(255.0D, strength * directionColorMultiplier);
        v = (int)Math.min(255.0D, Math.pow(v, colorPower));
        return new Color(0, v, 0);
    }

    private static int getGradientStrengthColor(double strength) {
        int v = (int)Math.min(255.0D, Math.pow(strength, strengthColorPower));
        v = (int)Math.min(255.0D, Math.pow(v, colorPower));
        return new Color(v, v, v).getRGB();
    }

    public static void drawGradientStrength(Gradient gradient, ImageProcessor image) {
        double strength = gradient.getStrength();
        image.putPixel(gradient.x, gradient.y, getGradientStrengthColor(strength));
    }

    public ImageProcessor getImage(ImageProcessor image) {
        image = image.convertToByte(true);

        ImageProcessor gradientImage = new ColorProcessor(image.getWidth(), image.getHeight());
        gradientImage.setColor(Color.black);
        gradientImage.fill();

        for (int x = 0; x < gradientImage.getWidth(); x++) {
            for (int y = 0; y < gradientImage.getHeight(); y++) {
                drawGradientStrength(new Gradient(image, x, y), gradientImage);
            }

        }

        if (this.drawDirection) {
            for (int x = 0; x < gradientImage.getWidth(); x++) {
                for (int y = 0; y < gradientImage.getHeight(); y++) {
                    drawGradientDirection(new Gradient(image, x, y), gradientImage);
                }
            }
        }

        return gradientImage;
    }

    public ImageProcessor run(ImageProcessor i) {
        return getImage(i.convertToByte(true));
    }

    public static void main(String[] args) throws IOException {
        Batch.run(new Gradients(true), 0, "data/batch/images/test");
    }
}