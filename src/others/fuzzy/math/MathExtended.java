package others.fuzzy.math;

import java.util.Iterator;
import java.util.List;

public class MathExtended
{
    public static float logd(float x)
    {
        return (float)(Math.log(x) / Math.log(2.0D));
    }

    public static float sum(List<Float> floats) {
        float sum = 0.0F;
        for (Iterator i$ = floats.iterator(); i$.hasNext(); ) { float f = ((Float)i$.next()).floatValue();
            sum += f;
        }
        return sum;
    }
}