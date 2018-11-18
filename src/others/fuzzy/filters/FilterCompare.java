package others.fuzzy.filters;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.io.PrintStream;

public class FilterCompare
        implements PlugInFilter
{
    private final PlugInFilter p1;
    private final PlugInFilter p2;

    public FilterCompare(PlugInFilter p1, PlugInFilter p2)
    {
        this.p1 = p1;
        this.p2 = p2;
    }

    public void run(ImageProcessor ip)
    {
        ImageProcessor ip1 = ip.duplicate();

        long start = System.currentTimeMillis();
        this.p1.run(ip1);
        System.out.println(this.p1.toString() + ": " + (System.currentTimeMillis() - start) + " ms");

        start = System.currentTimeMillis();
        this.p2.run(ip);
        System.out.println(this.p2.toString() + ": " + (System.currentTimeMillis() - start) + " ms");

        subtract(ip, ip1);
    }

    private void subtract(ImageProcessor ip, ImageProcessor ip1) {
        float[][] pixels1 = ip.getFloatArray();
        float[][] pixels2 = ip1.getFloatArray();

        for (int x = 0; x < pixels1.length; x++)
            for (int y = 0; y < pixels1[0].length; y++)
                ip.set(x, y, (int)Math.abs(pixels1[x][y] - pixels2[x][y]));
    }

    public int setup(String arg, ImagePlus imp)
    {
        this.p1.setup(arg, imp);
        this.p2.setup(arg, imp);
        return 31;
    }
}