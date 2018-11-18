package basics;

import ij.process.ImageProcessor;

public class SaveImageProgressListener extends SystemOutProgressListener
{
    public void updateImage(ImageProcessor i)
    {
        Tools.save(i);
    }
}