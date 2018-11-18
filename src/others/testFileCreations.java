package others;

public class testFileCreationspackage others;

import basics.javaAddons.DEBUG;
        import java.io.File;

public class testFileCreations
{
    public static void main(String[] args)
    {
        int files = 10;
        DEBUG.initProgress("Creating Files", files);
        for (int i = 0; i < files; i++) {
            DEBUG.printProgressBar("Creating Files");
            new File("test").mkdir();
            new File("test/" + i + ".txt");
        }
    }
} {
}
