package evaluation;

import basics.Tools;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SegmentationResults
{
    private Map<SegmentationResultType, List<Integer>> results = new HashMap();

    public void add(SegmentationResultType s, int value) {
        List values = (List)this.results.get(s);
        if (values == null) {
            values = new LinkedList();
        }
        values.add(Integer.valueOf(value));
        this.results.put(s, values);
    }

    public List<Integer> get(SegmentationResultType s) {
        return (List)this.results.get(s);
    }

    public String getResults() {
        String resultString = "";

        for (SegmentationResultType segmentationResultType : SegmentationResultType.values()) {
            resultString = resultString + "mean " + segmentationResultType.name() + "\t" + Tools.getMean(get(segmentationResultType)) + "\n";
            resultString = resultString + "min " + segmentationResultType.name() + "\t" + Collections.min(get(segmentationResultType)) + "\n";
            resultString = resultString + "max " + segmentationResultType.name() + "\t" + Collections.max(get(segmentationResultType)) + "\n";
            resultString = resultString + "stdev " + segmentationResultType.name() + "\t" + Tools.standardDeviation(get(segmentationResultType)) + "\n";
        }

        return resultString;
    }

    public static enum SegmentationResultType
    {
        truePositive, trueNegative, falsePositive, falseNegative, runtime;
    }
}