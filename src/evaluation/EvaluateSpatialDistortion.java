package evaluation;

public class EvaluateSpatialDistortion extends ImageTest
{
    public String getTestDescription()
    {
        return "";
    }

    public double calculateScore()
    {
        int sumEstXorRef = 0;
        int sumRef = 0;

        for (int x = 0; x < this.segEvaluate.getWidth(); x++) {
            for (int y = 0; y < this.segEvaluate.getHeight(); y++)
            {
                Segment evaluated = this.segEvaluate.getSegment(x, y);
                Segment reference = this.segReference.getSegment(x, y);

                if (reference != Segment.background) {
                    sumRef++;
                }

                if (((evaluated != Segment.background) || (reference == Segment.background)) && ((evaluated == Segment.background) || (reference != Segment.background)))
                    continue;
                sumEstXorRef++;
            }

        }

        double sd = sumEstXorRef / sumRef;

        return Math.min(1.0D, sd);
    }
}