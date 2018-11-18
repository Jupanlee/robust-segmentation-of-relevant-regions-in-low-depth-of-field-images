package others.fuzzy.filters;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.concurrent.Callable;

public class FilterTask
        implements Callable<ImageProcessor>
{
    protected final ImageProcessor ip;
    private final PlugInFilter filter;

    public FilterTask(ImageProcessor ip, PlugInFilter filter)
    {
        this.ip = ip;
        this.filter = filter;
    }

    public ImageProcessor call()
            throws Exception
    {
        this.filter.run(this.ip);
        return this.ip;
    }

    public void setup(ImagePlus image) {
        this.filter.setup(null, image);
    }
}