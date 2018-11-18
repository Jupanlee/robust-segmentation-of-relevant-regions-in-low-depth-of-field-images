package evaluation;

public class EvaluateRelevancy extends ImageTest
{
    public String getTestDescription()
    {
        return "Relevancy";
    }

    public double calculateScore()
    {
        double a = getTruePositives();
        double b = getFalsePositives();
        if (a + b == 0.0D) {
            return 0.0D;
        }
        return a / (a + b);
    }
}