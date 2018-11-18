package basics;

import ij.process.ImageProcessor;
import java.io.PrintStream;

public class SystemOutProgressListener
        implements ProgressListener
{
    public void progressUpdate(double value, String text)
    {
        System.out.println((int)(value * 100.0D) + "% " + text);
    }

    public void updateImage(ImageProcessor i)
    {
        System.err.println(i.toString());
    }
}