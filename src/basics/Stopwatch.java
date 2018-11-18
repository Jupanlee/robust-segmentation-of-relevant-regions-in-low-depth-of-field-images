package basics;

import java.io.PrintStream;
import java.util.Vector;

public class Stopwatch
{
    private long startTime;
    private Vector<Integer> times = new Vector();

    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    public int stopVerbose() {
        int time = stop();
        System.out.println(getLastTime());
        return time;
    }

    public int stop() {
        int time = (int)(System.currentTimeMillis() - this.startTime);
        this.times.add(Integer.valueOf(time));
        return time;
    }

    public int getLastTime() {
        return ((Integer)this.times.get(this.times.size() - 1)).intValue();
    }

    public double getMeanTime(int deimalPlaces) {
        return Tools.round(Tools.getMean(this.times), deimalPlaces);
    }

    public String toString()
    {
        return "mean from " + this.times.size() + " stops = " + getMeanTime(2);
    }
}