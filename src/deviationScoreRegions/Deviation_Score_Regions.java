package deviationScoreRegions;

import basics.Tools;
import basics.javaAddons.DEBUG;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Deviation_Score_Regions
        implements PlugIn
{
    private GenericDialog showDialog()
    {
        GenericDialog gd = new GenericDialog("DOF Extraction Settings");
        gd.addNumericField("size", 400.0D, 0);
        gd.addNumericField("blur", 1.0D, 2);
        gd.addNumericField("blurIterations", 10.0D, 0);
        gd.addNumericField("scoreImageThreshold", 33.0D, 0);
        gd.addNumericField("labDifferencePower", 1.0D, 2);
        gd.addNumericField("finalPower", 4.0D, 2);
        gd.addNumericField("dbscanScoreImageThreshold", 1.0D, 0);
        gd.addNumericField("epsilonPercentage", 0.025D, 3);
        gd.addNumericField("minPtsScorePointDensityMultiplier", 1.0D, 2);
        gd.addNumericField("mainClusterSize", 0.5D, 2);
        gd.addNumericField("closingEpsilonPercentage", 0.05D, 2);
        gd.addNumericField("rawMapDilateSize", 0.0D, 0);
        gd.addNumericField("appMapCloseSize", 15.0D, 0);
        gd.addNumericField("maxScoreRegionSize", 0.2D, 2);
        gd.addNumericField("deltaEToBeSimilar", 25.0D, 1);
        gd.addNumericField("finalMapReconstructSizePercentage", 0.25D, 2);

        gd.addNumericField("relevancyStart", 0.85D, 2);
        gd.addNumericField("relevancyIterationDec", 0.333D, 2);
        gd.addNumericField("minMaskRelevancy", 0.75D, 2);

        gd.addCheckbox("debug", true);
        return gd;
    }

    public void run(String arg) {
        GenericDialog gd = showDialog();

        gd.showDialog();
        if (gd.wasCanceled()) return;

        int imageSize = (int)gd.getNextNumber();
        double blur = gd.getNextNumber();
        int blurIterations = (int)gd.getNextNumber();
        int scoreImageThreshold = (int)gd.getNextNumber();
        double labDifferencePower = gd.getNextNumber();
        double finalPower = gd.getNextNumber();
        int dbscanScoreImageThreshold = (int)gd.getNextNumber();
        double epsilonPercentage = gd.getNextNumber();
        double minPtsScorePointDensityMultiplier = gd.getNextNumber();
        double mainClusterSize = gd.getNextNumber();
        double closingEpsilonPercentage = gd.getNextNumber();
        int rawMapDilateSize = (int)gd.getNextNumber();
        int appMapCloseSize = (int)gd.getNextNumber();
        double maxScoreRegionSize = gd.getNextNumber();
        double deltaEToBeSimilar = gd.getNextNumber();
        double finalMapReconstructSizePercentage = gd.getNextNumber();

        double relevancyStart = gd.getNextNumber();
        double relevancyIterationDec = gd.getNextNumber();
        double minMaskRelevancy = gd.getNextNumber();

        boolean debugVerbose = gd.getNextBoolean();
        boolean saveMaskRelevancyPix = false;
        boolean showExtractedOOIMask = true;

        for (int id : WindowManager.getIDList()) {
            ImageProcessor imageProcessor = WindowManager.getImage(id).getProcessor();
            if (imageSize > 0) imageProcessor = Tools.resize(imageProcessor, imageSize);

            DeviationScoreRegions deviationScoreRegions = new DeviationScoreRegions();

            deviationScoreRegions.imageSize = imageSize;
            deviationScoreRegions.blur = blur;
            deviationScoreRegions.blurIterations = blurIterations;
            deviationScoreRegions.scoreImageThreshold = scoreImageThreshold;
            deviationScoreRegions.labDifferencePower = labDifferencePower;
            deviationScoreRegions.finalPower = finalPower;
            deviationScoreRegions.dbscanScoreImageThreshold = dbscanScoreImageThreshold;
            deviationScoreRegions.epsilonPercentage = epsilonPercentage;
            deviationScoreRegions.minPtsScorePointDensityMultiplier = minPtsScorePointDensityMultiplier;
            deviationScoreRegions.mainClusterSize = mainClusterSize;
            deviationScoreRegions.closingEpsilonPercentage = closingEpsilonPercentage;
            deviationScoreRegions.approxMapDilateSize = rawMapDilateSize;
            deviationScoreRegions.approxMapCloseSize = appMapCloseSize;
            deviationScoreRegions.maxScoreRegionSize = maxScoreRegionSize;
            deviationScoreRegions.deltaEToBeSimilar = deltaEToBeSimilar;
            deviationScoreRegions.approxMapReconstructSizePercentage = finalMapReconstructSizePercentage;
            DEBUG.setVerbose(debugVerbose);
            deviationScoreRegions.saveMaskRelevancyPix = saveMaskRelevancyPix;

            deviationScoreRegions.relevancyStart = relevancyStart;
            deviationScoreRegions.relevancyIterationDec = relevancyIterationDec;
            deviationScoreRegions.minMaskRelevancy = minMaskRelevancy;

            ImagePlus ooi = new ImagePlus("OOI Extracted", deviationScoreRegions.run(imageProcessor));
            ooi.show();
            ooi.repaintWindow();

            if (showExtractedOOIMask) {
                ImagePlus mask = new ImagePlus("OOI Extracted Mask", deviationScoreRegions.mask);
                mask.show();
                mask.repaintWindow();
            }
        }
    }
}