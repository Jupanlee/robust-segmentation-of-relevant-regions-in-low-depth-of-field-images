package evaluation;

import basics.MColor;
import basics.Tools;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Font;
import java.io.PrintStream;

public class DeltaEVisualisation
{
    public static ImageProcessor get(int stepWidth, int height, int steps, double deltaE, MColor startColor, MColor incColor, Color fontColor)
    {
        ImageProcessor imageProcessor = new ColorProcessor(stepWidth * steps, height);
        for (int step = 0; step < steps; step++)
        {
            MColor differentColor = new MColor(startColor);

            for (int x = step * stepWidth; x < (step + 1) * stepWidth; x++) {
                for (int y = 0; y < height; y++) {
                    imageProcessor.putPixel(x, y, differentColor.getRgbInt());
                }
            }

            double[] hsv = differentColor.getHsv();
            String H = Tools.formatNumber(hsv[0], "0.00");
            String S = Tools.formatNumber(hsv[1], "0.00");
            String V = Tools.formatNumber(hsv[2], "0.00");
            int size = stepWidth / 5;
            imageProcessor.setColor(fontColor);
            imageProcessor.setFont(new Font("Helvetica", 0, size));
            imageProcessor.drawString(H + "\n" + S + "\n" + V + "\n", step * stepWidth, size);

            while (differentColor.getLabDeltaE(startColor) < deltaE) {
                System.out.println(differentColor.getLabDeltaE(startColor));
                differentColor = differentColor.add(incColor);
            }

            startColor = new MColor(differentColor);
        }

        return imageProcessor;
    }

    public static void main(String[] args)
    {
        int deltaE = 5;
        int steps = 10;
        Tools.save(get(500, 500, steps, deltaE, new MColor(new double[] { 0.0D, 1.0D, 1.0D }), new MColor(new double[] { 0.001D, 0.0D, 0.0D }), Color.black));
        Tools.save(get(500, 500, steps, deltaE, new MColor(new double[] { 0.0D, 0.0D, 1.0D }), new MColor(new double[] { 0.0D, 0.001D, 0.0D }), Color.black));
        Tools.save(get(500, 500, steps, deltaE, new MColor(new double[] { 0.0D, 0.0D, 0.0D }), new MColor(new double[] { 0.0D, 0.0D, 0.001D }), Color.white));
    }
}