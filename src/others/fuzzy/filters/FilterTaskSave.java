package others.fuzzy.filters;

import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.io.File;

public class FilterTaskSave extends FilterTask
{
    protected final String saveFileName;

    public FilterTaskSave(ImageProcessor ip, PlugInFilter filter, String saveFileName)
    {
        super(ip, filter);
        this.saveFileName = saveFileName;
    }

    public ImageProcessor call()
            throws Exception
    {
        File f = new File(this.saveFileName);
        ImageProcessor ip2 = null;

        if (!f.exists()) {
            ip2 = super.call();
            ToolsPhilipp.saveToFile(ip2, this.saveFileName);
        }
        return ip2;
    }
}