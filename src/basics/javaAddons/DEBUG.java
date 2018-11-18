//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics.javaAddons;

import basics.Tools;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DEBUG {
    private static Map<String, DEBUG.Progreess> progressMap = new HashMap();
    private static boolean verbose = true;
    private static String logFileName = "log.txt";

    public DEBUG() {
    }

    public static void initProgress(String description, int max) {
        DEBUG.Progreess progreess = (DEBUG.Progreess)progressMap.get(description);
        if (progreess == null) {
            progreess = new DEBUG.Progreess();
            progressMap.put(description, progreess);
        }

        progreess.current = 0;
        progreess.max = max;
    }

    public static void printProgressBar(String description) {
        DEBUG.Progreess progress = (DEBUG.Progreess)progressMap.get(description);
        if (progress.current == 0) {
            System.out.print(description + " ");
        }

        if (progress != null) {
            ++progress.current;
            if (progress.current % (progress.max / 10) == 0) {
                System.out.print(".");
            }

            if (progress.current == progress.max) {
                System.out.println("DONE");
            }
        }

    }

    public static void println(String text) {
        if (verbose) {
            System.out.println(text);
        }

    }

    public static void print(String text) {
        if (verbose) {
            System.out.print(text);
        }

    }

    public static void setVerbose(boolean v) {
        verbose = v;
    }

    public static boolean getVerbose() {
        return verbose;
    }

    public static void log(String text, boolean lineBreak) throws IOException {
        if (lineBreak) {
            println(text);
        } else {
            print(text);
        }

        String breakString = lineBreak ? "\n" : "";
        Tools.appendToFile(logFileName, text + breakString);
    }

    public static void log(String text) throws IOException {
        log(text, true);
    }

    public static void clearLog() {
        Tools.deleteFile(logFileName);
    }

    private static class Progreess {
        int max;
        int current;

        private Progreess() {
        }
    }
}
