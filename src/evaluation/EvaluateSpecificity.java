package evaluation;

public class EvaluateSpecificity extends ImageTest
{
    public String getTestDescription()
    {
        return "Specificity";
    }

    public double calculateScore()
    {
        double b = getFalsePositives();
        double d = getTrueNegatives();

        return d / (b + d);
    }
}