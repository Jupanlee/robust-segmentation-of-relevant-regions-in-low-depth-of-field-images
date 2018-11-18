package evaluation;

import ij.process.ImageProcessor;

public class SegmentationCases
{
    private ImageProcessor mask;
    private ImageProcessor reference;

    public SegmentationCases(ImageProcessor mask, ImageProcessor reference)
    {
        int smallestWidth = Math.min(mask.getWidth(), reference.getWidth());
        int smallestHeight = Math.min(mask.getHeight(), reference.getHeight());

        this.reference = reference.resize(smallestWidth, smallestHeight).convertToByte(true);
        this.mask = mask.resize(smallestWidth, smallestHeight).convertToByte(true);
    }

    public int getTruePositives() {
        return countPixel(1, 1);
    }

    public int getTrueNegatives() {
        return countPixel(0, 0);
    }

    public int getFalsePositives() {
        return countPixel(1, 0);
    }

    public int getFalseNegatives() {
        return countPixel(0, 1);
    }

    public String toString()
    {
        return getTruePositives() + "\t" + getTrueNegatives() + "\t" + getFalsePositives() + "\t" + getFalseNegatives();
    }

    private int countPixel(int maskValueToCount, int referenceValueToCount) {
        int count = 0;
        for (int x = 0; x < this.mask.getWidth(); x++) {
            for (int y = 0; y < this.mask.getHeight(); y++) {
                int maskValue = Math.min(1, this.mask.getPixel(x, y));
                int referenceValue = Math.min(1, this.reference.getPixel(x, y));

                if ((maskValue == maskValueToCount) && (referenceValue == referenceValueToCount)) {
                    count++;
                }
            }
        }
        return count;
    }
}