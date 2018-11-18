package evaluation;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutorTest extends MThread
{
    String id;

    public ExecutorTest(String id)
    {
        this.id = id;
    }

    public void run()
    {
        int i = 10;
        while (i-- > 0)
            try {
                System.out.println(this.id + " " + i);
                Thread.sleep(1000L);
            } catch (InterruptedException ex) {
                Logger.getLogger(ExecutorTest.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    public static void main(String[] args) throws InterruptedException
    {
        new ExecutorTest("A").start();
        new ExecutorTest("B").start();
        new ExecutorTest("C").start();
    }
}