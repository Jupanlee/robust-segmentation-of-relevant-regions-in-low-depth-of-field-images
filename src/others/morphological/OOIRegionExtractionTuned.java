package others.morphological;

import basics.Tools;
import evaluation.Batch;
import evaluation.Batch.Batchable;
import ij.process.ImageProcessor;
import java.io.PrintStream;

public class OOIRegionExtractionTuned
        implements Batch.Batchable
{
    int morphSize;
    int hosSize;
    double dsf;

    public OOIRegionExtractionTuned(int morphSize, int hosSize, double dsf)
    {
        this.morphSize = morphSize;
        this.hosSize = hosSize;
        this.dsf = dsf;
    }

    public ImageProcessor run(ImageProcessor original)
    {
        ImageProcessor grayscaled = original.convertToByte(true);
        Tools.showImage("original image", grayscaled, "original image");

        int size = AutoParameter.boxplotMean(HOSMap.hosMap(grayscaled, this.hosSize, this.dsf), 0.9D, 0.0D, 0.25D) + 1;
        System.out.println("AutoParameter.boxplotMean == " + size);

        ImageProcessor hosMapIp = HOSMap.hosMap(grayscaled, this.hosSize, this.dsf);
        Tools.showImage("hosMapIp", hosMapIp, "hosMapIp");

        ImageProcessor simplyfiedHosMap = Morphological.morphologicalClosingOpeningByReconstruction(hosMapIp, size, size);
        Tools.showImage("simplyfiedHosMap", simplyfiedHosMap, "simplyfiedHosMap");

        OOIRegions ooiRegions = new OOIRegions();
        ooiRegions.initFlatRegions(simplyfiedHosMap);
        ooiRegions.merge();

        ImageProcessor finalMergedMask = Tools.createImageProcessorFromArray(ooiRegions.getOOIMask().getPixels(), true);
        Tools.showImage("mask", finalMergedMask, "merged mask");
        original.setMask(finalMergedMask);
        grayscaled.setMask(finalMergedMask);

        return Tools.cropToMask(grayscaled);
    }

    public static void main(String[] args) {
        int morphSize = 31;
        int hosSize = 1;
        double dsf = 450.0D;
        int size = 0;
        Batch.run(new OOIRegionExtractionTuned(morphSize, hosSize, dsf), size, "data/batch/images/fuzzy");
    }
}