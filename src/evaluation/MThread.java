package evaluation;

import basics.Tools;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MThread extends Thread
{
    private String logFileName = "logfile.txt";
    private long startTime = 0L;
    private static int maxThreads = Runtime.getRuntime().availableProcessors() + 1;

    public static synchronized void setNumberOfThreads(int nThreads)
    {
        maxThreads = nThreads + 1;
    }

    public synchronized void writeToLog(String text) throws IOException {
        Tools.appendToFile(this.logFileName, text + "\n");
    }

    public synchronized void start()
    {
        while (activeCount() >= maxThreads) try {
            wait(250L);
        } catch (InterruptedException ex) {
            Logger.getLogger(MThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        resetTime();
        super.start();
    }

    public void resetTime() {
        this.startTime = getMillis();
    }

    private long getMillis() {
        return ManagementFactory.getThreadMXBean().getThreadCpuTime(getId()) / 1000000L;
    }

    public long getCPUTime() {
        return getMillis() - this.startTime;
    }
}