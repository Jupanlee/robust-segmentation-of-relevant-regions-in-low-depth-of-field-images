package others.morphological;

import basics.Tools;
import basics.math.MMatrix;
import ij.process.ImageProcessor;
import java.io.PrintStream;
import java.util.Vector;

public class OOIRegions
{
    private Vector<BinaryRegion> uncertianRegions = new Vector();
    private BinaryRegion ooiRegion;

    public void initFlatRegions(ImageProcessor ip)
    {
        initFlatRegions(Tools.getValuesFromImageProcessor(ip));
    }

    public void initFlatRegions(short[][] pixels)
    {
        int maxValue = Tools.getMax(pixels);
        int minValue = Math.max(0, (int)(0.08D * maxValue));

        FlatRegions flatRegions = new FlatRegions();
        flatRegions.initFlatRegions(pixels, minValue, maxValue - 1);

        this.ooiRegion = BinaryRegion.union(flatRegions.getRegions(FlatRegions.RegionType.higher));

        this.ooiRegion.setWidth(pixels.length);
        this.ooiRegion.setHeight(pixels[0].length);

        this.uncertianRegions.addAll(flatRegions.getRegions(FlatRegions.RegionType.target));
    }

    private float normalizedOverlappedBoundary(BinaryRegion uncertainRegion)
    {
        BinaryRegion boundaryPixels = uncertainRegion.getBoundaryPixels();
        int boundaryPixelSize = boundaryPixels.getPixelCount();
        int intersection = this.ooiRegion.intersect(boundaryPixels);
        return intersection / boundaryPixelSize;
    }

    public void merge()
    {
        float thresholdTnob = 0.5F;

        boolean changesHappen = true;
        int iteration = 0;

        while (changesHappen)
        {
            changesHappen = false;

            iteration++;

            for (int index = this.uncertianRegions.size() - 1; index >= 0; index--) {
                BinaryRegion uncertainRegion = (BinaryRegion)this.uncertianRegions.get(index);

                float nob = normalizedOverlappedBoundary(uncertainRegion);

                if (nob < thresholdTnob)
                    continue;
                this.ooiRegion.addRegion(uncertainRegion);
                this.uncertianRegions.remove(index);

                changesHappen = true;
            }
        }
    }

    public BinaryRegion getOOIMask()
    {
        return this.ooiRegion;
    }

    public static boolean test() throws Exception {
        boolean OK = true;

        ImageProcessor ip = Tools.loadImageProcessor("test/ooiRegions/testpicture.png");
        ImageProcessor targetHosMap = Tools.loadImageProcessor("test/ooiRegions/targetHosMap.png");
        ImageProcessor targetSimlyfiedHosMap = Tools.loadImageProcessor("test/ooiRegions/targetSimlyfiedHosMap.png");
        ImageProcessor targetOoiMergeResult = Tools.loadImageProcessor("test/ooiRegions/targetOoiMergeResult.png");

        MMatrix dilateSE = new MMatrix(1.0F, 3, 3);
        MMatrix erodeSE = dilateSE;
        int hosSize = 1;

        ip = HOSMap.hosMap(ip, hosSize);

        if (!Tools.equalPixels(ip, targetHosMap)) {
            System.err.println("HOS Map Error!");
            return false;
        }

        ImageProcessor simplyfiedHosMap = Morphological.morphologicalClosingOpeningByReconstruction(ip, dilateSE.getTable(), erodeSE.getTable());

        if (!Tools.equalPixels(simplyfiedHosMap, targetSimlyfiedHosMap)) {
            System.err.println("targetSimlyfiedHosMap Error!");
            return false;
        }

        OOIRegions ooiRegions = new OOIRegions();
        ooiRegions.initFlatRegions(Tools.getValuesFromImageProcessor(simplyfiedHosMap));
        ooiRegions.merge();
        ImageProcessor maskIp = Tools.createImageProcessorFromArray(ooiRegions.getOOIMask().getPixels(), true);

        if (!Tools.equalPixels(maskIp, targetOoiMergeResult)) {
            System.err.println("targetOoiMergeResult Error!");
            return false;
        }

        return OK;
    }
}