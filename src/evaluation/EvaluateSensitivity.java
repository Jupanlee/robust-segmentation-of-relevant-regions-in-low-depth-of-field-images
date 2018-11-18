package evaluation;

public class EvaluateSensitivity extends ImageTest
{
    public String getTestDescription()
    {
        return "Sensitivity";
    }

    public double calculateScore()
    {
        double a = getTruePositives();
        double c = getFalseNegatives();

        return a / (a + c);
    }
}