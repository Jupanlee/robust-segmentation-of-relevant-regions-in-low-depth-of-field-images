package basics.math;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class Statistics
{
    private List<Integer> numbers;
    boolean sorted = false;

    private void sort() {
        if (this.sorted) return;
        Collections.sort(this.numbers);
        this.sorted = true;
    }

    public Statistics(List<Integer> numbers)
    {
        this.numbers = new Vector(numbers);
    }

    public List<Integer> quantil(double fromPercentage, double toPercentage) {
        sort();
        int fromIndex = (int)Math.round(fromPercentage * this.numbers.size());
        int toIndex = (int)Math.round(toPercentage * this.numbers.size());
        return this.numbers.subList(fromIndex, toIndex);
    }

    public int median() {
        sort();
        if (this.numbers.size() == 0) return 0;
        return ((Integer)this.numbers.get(this.numbers.size() / 2)).intValue();
    }

    public double mean() {
        if (this.numbers.size() == 0) return 0.0D;

        double mean = 0.0D;
        for (Iterator i$ = this.numbers.iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();
            mean += i;
        }

        return mean / this.numbers.size();
    }
}