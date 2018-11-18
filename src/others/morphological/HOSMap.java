package others.morphological;

import basics.Tools;
import evaluation.UnsupervisedFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.util.Iterator;
import java.util.Vector;

public class HOSMap
        implements PlugInFilter, UnsupervisedFilter
{
    public static ImageProcessor hosMap(ImageProcessor ip, int radius)
    {
        return hosMap(ip, radius, 100.0D);
    }

    public static ImageProcessor hosMap(ImageProcessor ip, int radius, double dsf) {
        return hosMap(ip, radius, dsf, 4.0D);
    }

    public static ImageProcessor hosMap(ImageProcessor ip, int radius, double downScalingFactor, double power) {
        ImageProcessor hosMapIp = ip.duplicate();

        int w = ip.getWidth();
        int h = ip.getHeight();

        for (int x = 0; x < w; x++) {
            IJ.showProgress(x, w);
            for (int y = 0; y < h; y++)
            {
                Point center = new Point(x, y);

                Vector neighbours = Tools.getNeighbours(ip, center, radius, true);

                double sampleMean = Tools.getMean(neighbours);

                double forthOrderMoment = 0.0D;

                for (Iterator i$ = neighbours.iterator(); i$.hasNext(); ) { int neighbor = ((Integer)i$.next()).intValue();
                    forthOrderMoment += Math.pow(neighbor - sampleMean, power);
                }

                forthOrderMoment /= neighbours.size();

                forthOrderMoment = Math.min(255.0D, forthOrderMoment / downScalingFactor);

                hosMapIp.putPixel(x, y, (short)(int)Math.round(forthOrderMoment));
            }
        }

        return hosMapIp;
    }

    public int setup(String arg, ImagePlus imp)
    {
        if (imp.getType() == 4);
        return 31;
    }

    public Object[] getDefaultParams()
    {
        Object[] params = { Integer.valueOf(1) };
        return params;
    }

    public void run(ImageProcessor ip) {
        Object[] params = { Integer.valueOf(1) };
        run(ip, params);
    }

    public void run(ImageProcessor ip, Object[] params)
    {
        int radius = ((Integer)params[0]).intValue();
        ip = hosMap(ip, radius);
    }
}