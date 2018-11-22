package others.fuzzy.filters;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ToolsPhilipp
{
    public static boolean showImages = true;

    public static ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

//    public static Iterable<String> getAllFilesInDir(String dirName, String fileType)
//    {
//        FileFilter ff = new FileFilter(fileType)
//        {
//            public boolean accept(File pathname)
//            {
//                return (pathname.toString().endsWith(fileType)) && (!pathname.toString().endsWith(".db")) && (!pathname.isDirectory());
//            }
//        };
//        File f = new File(dirName);
//        File[] files = f.listFiles(ff);
//
//        List fileStringList = new ArrayList();
//        for (File file : files) {
//            fileStringList.add(file.getName());
//        }
//
//        return fileStringList;
//    }

    public static Iterable<String> getAllSubdirs(String dirName)
    {
        FileFilter ff = new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.isDirectory();
            }
        };
        File f = new File(dirName);
        File[] files = f.listFiles(ff);

        List fileStringList = new ArrayList();
        for (File file : files) {
            fileStringList.add(file.getName());
        }

        return fileStringList;
    }

    public static ImageProcessor loadImageProcessor(String fileName)
    {
        ImagePlus iplus = IJ.openImage(fileName);
        return iplus.getChannelProcessor();
    }

    public static String getNameWithoutExtension(String fileName) {
        int end = fileName.indexOf(".");
        return fileName.substring(0, end);
    }

    public static void saveToFile(ImageProcessor imageProcessor, String saveFileName) {
        ImagePlus imp = new ImagePlus("image", imageProcessor);

        File f = new File(saveFileName);
        f.getParentFile().mkdir();
        IJ.save(imp, saveFileName);
        try {
            Thread.sleep(200L);
        } catch (InterruptedException ex) {
            Logger.getLogger(ToolsPhilipp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void showImage(ImageProcessor ip, String title)
    {
        if (!showImages) return;

        ImagePlus iplus = new ImagePlus(title, ip);
        iplus.show();
    }

    public static boolean[][] roiMapFromMap(String map)
    {
        ImageProcessor mapIp = IJ.openImage(map).getChannelProcessor();

        boolean[][] roiMap = new boolean[mapIp.getWidth()][mapIp.getHeight()];

        for (int x = 0; x < roiMap.length; x++) {
            for (int y = 0; y < roiMap[0].length; y++) {
                if (mapIp.getPixelValue(x, y) == 0.0F) {
                    roiMap[x][y] = true;
                    mapIp.set(x, y, 0);
                }
                else if (mapIp.getPixelValue(x, y) < 255.0F) {
                    System.err.println("Map should only contain values 0, 255: " + map);
                } else {
                    mapIp.set(x, y, 255);
                }
            }
        }

        showImage(mapIp, "map");

        return roiMap;
    }
}