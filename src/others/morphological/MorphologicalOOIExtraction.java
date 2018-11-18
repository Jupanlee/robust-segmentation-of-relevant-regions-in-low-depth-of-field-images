package others.morphological;

import basics.Tools;
import basics.javaAddons.DEBUG;
import evaluation.Batch;
import evaluation.Batch.Batchable;
import ij.process.ImageProcessor;

public class MorphologicalOOIExtraction
        implements Batch.Batchable
{
    private int se_size;
    private int hosSize;
    private double DSF;

    public static void main(String[] args)
    {
        int size = 600;

        Batch.run(new MorphologicalOOIExtraction(), size, "../../images/tmp");
    }

    public MorphologicalOOIExtraction() {
        this.se_size = 31;
        this.hosSize = 1;
        this.DSF = 100.0D;
    }

    public MorphologicalOOIExtraction(int se_size, int hosSize, double DSF) {
        this.se_size = se_size;
        this.hosSize = hosSize;
        this.DSF = DSF;
    }

    public ImageProcessor getHosMap(ImageProcessor i) {
        return HOSMap.hosMap(i.convertToByte(true), this.hosSize, this.DSF);
    }

    public ImageProcessor run(ImageProcessor original) {
        ImageProcessor grayScaled = original.convertToByte(true);
        if (DEBUG.getVerbose()) {
            Tools.showImage("hosMapIp", grayScaled, "original image", true);
        }

        ImageProcessor hosMapIp = HOSMap.hosMap(grayScaled, this.hosSize, this.DSF);
        if (DEBUG.getVerbose()) {
            Tools.showImage("hosMapIp", hosMapIp, "hosMapIp", true);
        }

        ImageProcessor simplyfiedHosMap = Morphological.morphologicalClosingOpeningByReconstruction(hosMapIp, this.se_size, this.se_size);
        if (DEBUG.getVerbose()) {
            Tools.showImage("simplyfiedHosMap", simplyfiedHosMap, "simplyfiedHosMap", true);
        }

        OOIRegions ooiRegions = new OOIRegions();
        ooiRegions.initFlatRegions(simplyfiedHosMap);
        ooiRegions.merge();

        ImageProcessor finalMergedMask = Tools.createImageProcessorFromArray(ooiRegions.getOOIMask().getPixels(), true);
        if (DEBUG.getVerbose()) {
            Tools.save(finalMergedMask);
        }

        ImageProcessor croped = grayScaled.duplicate();
        original.setMask(finalMergedMask);
        croped.copyBits(finalMergedMask, 0, 0, 2);
        if (DEBUG.getVerbose()) {
            Tools.showImage("croped", croped, "croped", true);
        }

        return croped;
    }

    public String toString()
    {
        return "Morph";
    }
}