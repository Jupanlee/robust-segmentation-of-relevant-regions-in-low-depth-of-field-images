package evaluation;

public class EvaluateErrorRate extends ImageTest
{
    public String getTestDescription()
    {
        return "Error-Rate";
    }

    public double calculateScore()
    {
        double tp = getTruePositives();
        double tn = getFalsePositives();
        double fp = getFalsePositives();
        double fn = getFalseNegatives();

        return (fp + fn) / (tp + tn + fp + fn);
    }
}