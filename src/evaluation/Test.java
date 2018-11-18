package evaluation;

import basics.Tools;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Test extends MThread
{
    int id;

    public Test(int id)
    {
        this.id = id;
    }

    public void run()
    {
        try
        {
            long start = System.currentTimeMillis();
            for (long i = 1L; i <= 10000000L; i += 1L) Math.sin(Math.random());

            start = System.currentTimeMillis();
            Tools.loadImageProcessor("tmp.jpg");

            sleep(1000L);

            System.out.println("CPU time of Thread" + this.id + "  == " + getCPUTime());
        }
        catch (Exception ex)
        {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args)
    {
        setNumberOfThreads(3);
        new Test(1).start();
        new Test(2).start();
        new Test(3).start();
        new Test(4).start();
        new Test(5).start();
    }
}