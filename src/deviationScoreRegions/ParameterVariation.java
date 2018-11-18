package deviationScoreRegions;

import basics.javaAddons.DEBUG;
import evaluation.Batch;

public class ParameterVariation
{
    public static void main(String[] args)
    {
        regionScoringParameter();
    }

    public static void regionScoringParameter()
    {
        DEBUG.setVerbose(false);
        for (double minMBO = 1.0D; minMBO >= 0.0D; minMBO -= 0.1D) {
            DeviationScoreRegions_2_differentSizes dsr = new DeviationScoreRegions_2_differentSizes();
            dsr.minMBO = 0.85D;
            dsr.minMaskRelevancy = minMBO;
            Batch.run(dsr, 350, "../../../data/batch/images/tmp");
        }
    }

    public static void colorSimilarity() {
        for (double deltaE = 5.0D; deltaE <= 50.0D; deltaE += 5.0D) {
            DeviationScoreRegions_2_differentSizes dsr = new DeviationScoreRegions_2_differentSizes();
            dsr.deltaEToBeSimilar = deltaE;
            Batch.run(dsr, 500, "../../../data/batch/images/tmp");
        }
    }

    public static void sizeOfStructuringElement() {
        for (double size = 0.0D; size <= 0.5D; size += 0.1D) {
            DeviationScoreRegions_2_differentSizes dsr = new DeviationScoreRegions_2_differentSizes();
            dsr.approxMapReconstructSize = (int)(dsr.maskApproximationSize * size);
            Batch.run(dsr, 800, "../../../data/batch/images/tmp");
        }
    }

    public static void thetaEpsilon()
    {
        for (double thetaEpsilon = 0.005D; thetaEpsilon <= 0.1D; thetaEpsilon += 0.02D) {
            DeviationScoreRegions_2_differentSizes dsr = new DeviationScoreRegions_2_differentSizes();
            dsr.epsilonPercentage = thetaEpsilon;
            Batch.run(dsr, 500, "../data/batch/images/tmp");
        }
    }
}