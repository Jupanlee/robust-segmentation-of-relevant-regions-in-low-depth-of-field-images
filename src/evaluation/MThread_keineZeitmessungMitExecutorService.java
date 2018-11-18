package evaluation;

import basics.Tools;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MThread_keineZeitmessungMitExecutorService extends Thread
{
    private String logFileName = "logfile.txt";
    private static ExecutorService executerService;
    private long startTime = 0L;

    public static void setNumberOfThreads(int nThreads)
    {
        executerService = Executors.newFixedThreadPool(nThreads);
    }

    public synchronized void writeToLog(String text) throws IOException
    {
        Tools.sleep(2000);
        Tools.appendToFile(this.logFileName, text);
    }

    public synchronized void start()
    {
        resetTime();

        executerService.submit(this);
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

    public static void main(String[] args) {
        new MThread_keineZeitmessungMitExecutorService()
        {
            public void run()
            {
                System.out.print("Math... ");
                long start = System.currentTimeMillis();
                for (long i = 1L; i <= 50000000L; i += 1L) Math.sin(Math.random());
                System.out.println("done in " + (System.currentTimeMillis() - start) + "ms.");
                System.out.println("CPU Time == " + getCPUTime());
            }
        }
                .start();
    }

    static
    {
        setNumberOfThreads(Runtime.getRuntime().availableProcessors());
    }
}