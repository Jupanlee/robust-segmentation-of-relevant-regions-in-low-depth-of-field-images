package evaluation;

public class SegmentationResult
{
    private int truePositives = 0;
    private int trueNegatives = 0;
    private int falsePositives = 0;
    private int falseNegatives = 0;
    private int runtimeinMS = 0;

    public int getFalseNegatives()
    {
        return this.falseNegatives;
    }

    public void setFalseNegatives(int falseNegatives) {
        this.falseNegatives = falseNegatives;
    }

    public int getFalsePositives() {
        return this.falsePositives;
    }

    public void setFalsePositives(int falsePositives) {
        this.falsePositives = falsePositives;
    }

    public int getRuntimeinMS() {
        return this.runtimeinMS;
    }

    public void setRuntimeinMS(int runtimeinMS) {
        this.runtimeinMS = runtimeinMS;
    }

    public int getTrueNegatives() {
        return this.trueNegatives;
    }

    public void setTrueNegatives(int trueNegatives) {
        this.trueNegatives = trueNegatives;
    }

    public int getTruePositives() {
        return this.truePositives;
    }

    public void setTruePositives(int truePositives) {
        this.truePositives = truePositives;
    }

    public SegmentationResult(int truePositives, int trueNegatives, int falsePositives, int falseNegatives)
    {
        this.truePositives = truePositives;
        this.trueNegatives = trueNegatives;
        this.falseNegatives = falseNegatives;
        this.falsePositives = falsePositives;
    }
}