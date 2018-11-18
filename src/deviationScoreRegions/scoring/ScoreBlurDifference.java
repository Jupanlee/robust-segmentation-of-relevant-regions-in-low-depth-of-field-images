package deviationScoreRegions.scoring;

import basics.Tools;
import basics.javaAddons.DEBUG;
import evaluation.Batch.Batchable;
import ij.process.ImageProcessor;
import java.io.IOException;

public class ScoreBlurDifference
        implements Batch.Batchable
{
    private double power;
    private double secondBlur;
    private double firstBlur;
    private boolean userDefined = false;

    public static void main(String[] args) throws IOException {
        for (String fileName : Tools.getFilesFromDirectory("data/batch/images/base", ".jpg"))
            for (int size = 500; size <= 500; size += 100) {
                ImageProcessor original = Tools.resize(Tools.loadImageProcessor(fileName), size);
                Tools.save(new ScoreBlurDifference().run(original));
            }
    }

    public ScoreBlurDifference()
    {
    }

    public ScoreBlurDifference(double power, double firstBlur, double secondBlur)
    {
        this.power = power;
        this.secondBlur = secondBlur;
        this.firstBlur = firstBlur;
        this.userDefined = true;
    }

    public ImageProcessor run(ImageProcessor i) {
        if (!this.userDefined) {
            double value = Math.log(Math.sqrt(i.getPixelCount()));
            this.firstBlur = (value * 0.3D);
            this.secondBlur = (this.firstBlur + Math.log(this.firstBlur));
            this.power = (1.0D + Math.sqrt(value));
        }

        return Tools.power(blurDifference(i, this.firstBlur, this.secondBlur, false), this.power);
    }

    public static ImageProcessor blurDifference(ImageProcessor original, double blurA, double blurB, boolean additive) {
        ImageProcessor a = Tools.blur(original, blurA);
        ImageProcessor b = additive ? Tools.blur(a, blurB) : Tools.blur(original, blurB);
        return Tools.difference(a, b).convertToByte(true);
    }

    public ImageProcessor test(ImageProcessor imageProcessor)
    {
        if (DEBUG.getVerbose()) {
            Tools.save(imageProcessor);
        }
        ImageProcessor differenceImage = run(imageProcessor);
        if (DEBUG.getVerbose()) {
            Tools.save(differenceImage);
        }
        differenceImage = Tools.blur(differenceImage, 200.0D);
        differenceImage = Tools.multiply(differenceImage, 0.99D, 1.0D, 255);
        return differenceImage;
    }
}