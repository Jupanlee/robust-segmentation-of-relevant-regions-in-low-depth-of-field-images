package newTry;

import evaluation.Batch;
import evaluation.Batch.Batchable;
import ij.process.ImageProcessor;

public class DSRG
        implements Batch.Batchable
{
    public static void main(String[] args)
    {
        Batch.run(new DSRG(), 500, "../../images/base");
    }

    public ImageProcessor run(ImageProcessor original)
    {
        return original;
    }
}