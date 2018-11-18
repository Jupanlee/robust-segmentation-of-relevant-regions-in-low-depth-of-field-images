package basics;

import basics.convexHull.ConvexHullTools;
import basics.javaAddons.DEBUG;
import basics.javaAddons.MQueue;
import basics.math.Statistics;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageWindow;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.plugin.filter.GaussianBlur;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.imageio.ImageIO;

public class Tools
{
    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    private static int randomSeed;
    private static Random randomGenerator;
    private static Random random;
    private static Map<String, ImagePlus> images;
    private static ImagePlus imagePlus;
    static int savedCount;

    public static void showImageStack(List<ImageProcessor> imageProcessors)
    {
        int maxWidth = 0;
        int maxHeight = 0;
        for (ImageProcessor i : imageProcessors) {
            if (i.getWidth() > maxWidth) maxWidth = i.getWidth();
            if (i.getHeight() > maxHeight) maxHeight = i.getHeight();
        }

        ImageStack stack = new ImageStack(maxWidth, maxHeight);
        for (ImageProcessor img : imageProcessors) {
            stack.addSlice("", img.resize(maxWidth, maxHeight));
        }

        new ImagePlus("debug", stack).show();
    }

    public static double mean(ImageProcessor imageProcessor, int startBin)
    {
        int[] histogram = imageProcessor.getHistogram();
        int sum = 0;
        int pixelCount = 0;

        for (int binNr = startBin; binNr < histogram.length; binNr++) {
            int pixelInBin = histogram[binNr];
            sum += pixelInBin * binNr;
            pixelCount += pixelInBin;
        }

        return sum / pixelCount;
    }

    public static void setMask(ImageProcessor orignal, ImageProcessor mask) {
        orignal.setMask(mask.resize(orignal.getWidth(), orignal.getHeight()));
    }

    public static ImageProcessor resize(ImageProcessor imageProcessor, double megapixel) {
        int E6 = 1000000;
        if (megapixel <= 0.0D) {
            return clear(imageProcessor);
        }
        double factor = Math.sqrt(megapixel * 1000000.0D / imageProcessor.getPixelCount());
        int width = (int)Math.round(imageProcessor.getWidth() * factor);
        int height = (int)Math.round(imageProcessor.getHeight() * factor);
        return imageProcessor.resize(width, height);
    }

    public static ImageProcessor clear(ImageProcessor imageProcessor) {
        ImageProcessor cleared = imageProcessor.duplicate();
        cleared.setColor(Color.black);
        cleared.fill(new Roi(0, 0, cleared.getWidth(), cleared.getHeight()));
        return cleared;
    }

    public static ImageProcessor maskBackground(ImageProcessor imageProcessor, Color c) {
        ImageProcessor coloredBackground = imageProcessor.duplicate();
        ImageProcessor mask = imageProcessor.getMask();
        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++) {
                if ((mask != null) && (mask.getPixelValue(x, y) == 0.0F)) {
                    coloredBackground.putPixel(x, y, c.getRGB());
                }
            }
        }

        return coloredBackground;
    }

    public double minDivMax(double a, double b)
    {
        return Math.min(a, b) / Math.max(a, b);
    }

    public static ImageProcessor addNoise(ImageProcessor imageProcessor, double radius) {
        ImageProcessor noised = imageProcessor.duplicate();
        noised.noise(radius);
        return noised;
    }

    public static ImageProcessor addBorder(ImageProcessor imageProcessor, int size) {
        if (size == 0) {
            return imageProcessor.duplicate();
        }
        int width = imageProcessor.getWidth() + size * 2;
        int height = imageProcessor.getHeight() + size * 2;
        ImageProcessor bordered = new ByteProcessor(width, height);
        bordered.copyBits(imageProcessor, size, size, 0);
        return bordered;
    }

    public static ImageProcessor removeBorder(ImageProcessor imageProcessor, int size) {
        if (size == 0) {
            return imageProcessor.duplicate();
        }
        ImageProcessor tmp = imageProcessor.duplicate();
        tmp.copyBits(tmp, -size, -size, 0);

        int width = imageProcessor.getWidth() - size * 2;
        int height = imageProcessor.getHeight() - size * 2;
        ImageProcessor unbordered = new ByteProcessor(width, height);
        unbordered.copyBits(tmp, 0, 0, 0);
        return unbordered;
    }

    public static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(cal.getTime());
    }

    public static ImageProcessor close(ImageProcessor i, int size, boolean excludeBorder)
    {
        int borderSize = excludeBorder ? size : 0;
        return removeBorder(erode(dilate(addBorder(i, borderSize), size), size), borderSize);
    }
    public static ImageProcessor close(ImageProcessor i, int size) {
        return close(i, size, false);
    }

    public static ImageProcessor open(ImageProcessor i, int iterations) {
        return dilate(erode(i, iterations), iterations);
    }

    public static ImageProcessor dilate(ImageProcessor ip, int iterations) {
        ImageProcessor result = ip.duplicate();
        for (int i = 0; i < iterations; i++) {
            result.erode();
        }
        return result;
    }

    public static ImageProcessor erode(ImageProcessor ip, int iterations) {
        ImageProcessor result = ip.duplicate();
        for (int i = 0; i < iterations; i++) {
            result.dilate();
        }
        return result;
    }

    public static ImageProcessor threshold(ImageProcessor image, double threshold) {
        ImageProcessor result = image.duplicate();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getPixelValue(x, y) < threshold) {
                    result.putPixel(x, y, 0);
                }
            }
        }
        return result;
    }

    public static <Item> void fill(Item[][] itemField, Item value) {
        for (int x = 0; x < itemField.length; x++)
            for (int y = 0; y < itemField[x].length; y++)
                itemField[x][y] = value;
    }

    public static <Item> Item removeFirst(Collection collection)
    {
        Iterator iterator = collection.iterator();
        Object item = iterator.next();
        iterator.remove();
        return item;
    }

    public static MColor[][] getColorField(ImageProcessor imageProcessor) {
        MColor[][] colorField = new MColor[imageProcessor.getWidth()][imageProcessor.getHeight()];
        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++) {
                colorField[x][y] = new MColor(imageProcessor.getPixel(x, y));
            }
        }
        return colorField;
    }

    public static ImageProcessor write(String text, ImageProcessor imageProcessor) {
        return write(text, imageProcessor, Color.green);
    }

    public static ImageProcessor write(String text, ImageProcessor imageProcessor, Color c) {
        ImageProcessor result = new ColorProcessor(imageProcessor.getWidth(), imageProcessor.getHeight());
        result.copyBits(imageProcessor, 0, 0, 0);

        int lines = new StringTokenizer(text, "\r\n").countTokens() + 1;

        int breite = (int)Math.sqrt(imageProcessor.getPixelCount());
        int size = (int)(breite * 0.05D);

        result.setColor(c);
        result.setFont(new Font("Helvetica", 0, size));
        result.drawString(text, size / 2, imageProcessor.getHeight() - size * lines);

        return result;
    }

    public static ImageProcessor copyBits(ImageProcessor i1, ImageProcessor i2, int blitterMode) {
        ImageProcessor result = i1.duplicate();
        result.copyBits(i2, 0, 0, blitterMode);
        return result;
    }

    public static ImageProcessor difference(ImageProcessor a, ImageProcessor b)
    {
        ImageProcessor differenceImage = b.duplicate();
        differenceImage.copyBits(a, 0, 0, 8);
        return differenceImage;
    }

    public static ImageProcessor median(ImageProcessor imageProcessor, int radius) {
        ImageProcessor result = imageProcessor.duplicate();

        if (radius > 0) {
            int size = radius * 2 + 1;
            float[] medianKernel = new float[size * size];
            Arrays.fill(medianKernel, 1.0F);
            result.convolve(medianKernel, size, size);
        }

        return result;
    }

    public static int rgbToInt(int[] rgb) {
        return new Color(rgb[0], rgb[1], rgb[2]).getRGB();
    }

    public static int[] rgbToArray(int rgb) {
        int[] RGB = new int[3];
        Color c = new Color(rgb);
        RGB[0] = c.getRed();
        RGB[1] = c.getGreen();
        RGB[2] = c.getBlue();
        return RGB;
    }

    public static boolean deleteFile(String fileName) {
        File f = new File(fileName);
        if (f.exists()) {
            return f.delete();
        }
        return true;
    }

    public static void appendToFile(String fileName, String text) throws IOException
    {
        if (!new File(fileName).exists()) {
            new File(fileName).createNewFile();
        }

        BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
        out.write(text);
        System.out.println(text);
        out.close();
    }

    public static void appendToFile(String fileName, String[] text) throws IOException {
        if (!new File(fileName).exists()) {
            new File(fileName).createNewFile();
        }

        BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
        for (String s : text) {
            out.write(s);
            out.write("\t");
        }
        out.write("\n");
        out.close();
    }

    private static void shiftColor(ImageProcessor imageProcessor, int x, int y, float h, float s, float v, boolean modulo)
    {
        float[] hsv = getHSV(imageProcessor.getPixel(x, y));

        hsv[0] += h;
        hsv[1] += s;
        hsv[2] += v;

        if (modulo)
        {
            hsv[0] %= 1.0F;
            hsv[1] %= 1.0F;
            hsv[2] %= 1.0F;
        }
        else {
            if (hsv[0] > 1.0F) {
                hsv[0] = 1.0F;
            }
            if (hsv[1] > 1.0F) {
                hsv[1] = 1.0F;
            }
            if (hsv[2] > 1.0F) {
                hsv[2] = 1.0F;
            }

            if (hsv[0] < 0.0F) {
                hsv[0] = 0.0F;
            }
            if (hsv[1] < 0.0F) {
                hsv[1] = 0.0F;
            }
            if (hsv[2] < 0.0F) {
                hsv[2] = 0.0F;
            }
        }

        imageProcessor.putPixel(x, y, getRGB(hsv));
    }

    public static ImageProcessor powerRgb(ImageProcessor imageProcessor, double power) {
        ImageProcessor powered = imageProcessor.duplicate();

        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++)
            {
                int rgb = imageProcessor.getPixel(x, y);

                int r = (int)Math.min(255L, Math.round(Math.pow(new Color(rgb).getRed(), power)));
                int g = (int)Math.min(255L, Math.round(Math.pow(new Color(rgb).getGreen(), power)));
                int b = (int)Math.min(255L, Math.round(Math.pow(new Color(rgb).getBlue(), power)));

                powered.putPixel(x, y, new Color(r, g, b).getRGB());
            }
        }
        return powered;
    }

    public static ImageProcessor colorThreshold(float[] hsvThreshold, ImageProcessor imageProcessor, Color color) {
        ImageProcessor result = imageProcessor.duplicate();
        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++) {
                float[] hsv = getHSV(result.getPixel(x, y));
                if ((hsv[1] < hsvThreshold[1]) || (hsv[2] < hsvThreshold[2]) || (hsv[0] < hsvThreshold[0])) {
                    result.putPixel(x, y, color.getRGB());
                }
            }
        }
        return result;
    }

    public static Set<Point> arrayToPoints(double[][] field, double min)
    {
        Set points = new HashSet();

        for (int x = 0; x < field.length; x++) {
            for (int y = 0; y < field[0].length; y++) {
                if (field[x][y] >= min) {
                    points.add(new Point(x, y));
                }
            }
        }

        return points;
    }

    public static double getMean(double[][] field)
    {
        double sum = 0.0D;
        int count = 0;
        for (int x = 0; x < field.length; x++) {
            for (int y = 0; y < field[x].length; y++) {
                sum += field[x][y];
                count++;
            }
        }
        return sum / count;
    }

    public static ImageProcessor createImageProcessor(float[] max, int width, int height)
    {
        ImageProcessor image = new ColorProcessor(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.putPixel(x, y, getRGB(randomHSV(max)));
            }
        }
        return image;
    }

    public static void main(String[] args)
    {
        List imgs = new LinkedList();
        imgs.add(addNoise(new ColorProcessor(200, 200), 50.0D));
        imgs.add(addNoise(new ColorProcessor(200, 200), 250.0D));
        showImageStack(imgs);
    }

    public static int getRGB(float[] hsv)
    {
        return Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
    }

    public static int[] getRGB(double[] hsv) {
        int[] rgb = new int[3];
        Color c = new Color(Color.HSBtoRGB((float)hsv[0], (float)hsv[1], (float)hsv[2]));
        rgb[0] = c.getRed();
        rgb[1] = c.getGreen();
        rgb[2] = c.getBlue();
        return rgb;
    }

    public static float[] randomHSV(float[] max) {
        float[] hsv = new float[3];

        hsv[0] = ((float)randomGenerator.nextDouble() * max[0]);
        hsv[1] = ((float)randomGenerator.nextDouble() * max[1]);
        hsv[2] = ((float)randomGenerator.nextDouble() * max[2]);

        return hsv;
    }

    public static double minDivideMax(double a, double b)
    {
        if ((a == 0.0D) && (b == 0.0D)) {
            return 0.0D;
        }
        double result = Math.abs(Math.min(a, b) / Math.max(a, b));
        assert ((result >= 0.0D) && (result <= 1.0D)) : result;
        return result;
    }

    public static double getSum(double[][] doubleField, Point p, int width, int height)
    {
        double sum = 0.0D;
        for (int x = p.x; x < p.x + width; x++) {
            for (int y = p.y; y < p.y + height; y++) {
                if ((x < doubleField.length) && (y < doubleField[x].length)) {
                    sum += doubleField[x][y];
                }
            }
        }
        return sum;
    }

    public static double getSum(double[][] doubleField, boolean normed) {
        double sum = 0.0D;

        for (int x = 0; x < doubleField.length; x++) {
            for (int y = 0; y < doubleField[x].length; y++) {
                sum += doubleField[x][y];
            }
        }

        if (normed) {
            sum /= doubleField.length * doubleField[0].length;
        }

        return sum;
    }

    public static double getSum(double[][] doubleField, Set<Point> points)
    {
        double sum = 0.0D;
        for (Point p : points) {
            sum += doubleField[p.x][p.y];
        }
        return sum;
    }

    public static double round(double v, int decimalPlaces) {
        double faktor = Math.pow(10.0D, decimalPlaces);
        return Math.round(v * faktor) / faktor;
    }

    public static ImageProcessor newImageProcessor(int[][][] labColors, int width, int height) {
        ImageProcessor imageProcessor = new ColorProcessor(width, height);
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++);
        }
        return imageProcessor;
    }

    public static ImageProcessor newImageProcessor(double[][] pixels, int width, int height) {
        ImageProcessor imageProcessor = new ByteProcessor(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                imageProcessor.putPixelValue(x, y, pixels[x][y]);
            }
        }
        return imageProcessor;
    }

    public static <Nr extends Number> Number getMedian(Collection<Nr> numbers) {
        List sorted = new LinkedList(numbers);
        Collections.sort(sorted);
        return (Number)sorted.get(sorted.size() / 2);
    }

    public static int getMedian(ImageProcessor imageProcessor, double fromPercentage, double toPercentage) {
        List quantil = new Statistics(getIntValuesFromImageProcessor(imageProcessor, 0)).quantil(fromPercentage, toPercentage);
        return new Statistics(quantil).median();
    }

    public static ImageProcessor multiplyQuantil(ImageProcessor original, double fromPercentage, double toPercentage) {
        ImageProcessor imageProcessor = original.duplicate();

        double median = getMedian(imageProcessor, fromPercentage, toPercentage);

        imageProcessor.multiply(Color.white.getRGB() / median);
        return imageProcessor;
    }

    public static ImageProcessor multiply(ImageProcessor original, double fromPercentage, double toPercentage, int value)
    {
        ImageProcessor imageProcessor = original.duplicate();
        List quantil = new Statistics(getIntValuesFromImageProcessor(imageProcessor, 0)).quantil(fromPercentage, toPercentage);

        double median = new Statistics(quantil).median();
        imageProcessor.multiply(value / median);
        return imageProcessor;
    }

    public static void power(double[][] doubles, double power) {
        for (int x = 0; x < doubles.length; x++)
            for (int y = 0; y < doubles[x].length; y++)
                doubles[x][y] = Math.pow(doubles[x][y], power);
    }

    public static ImageProcessor power(ImageProcessor imageProcessor, double power)
    {
        ImageProcessor result = imageProcessor.duplicate();

        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++) {
                int rgbOriginal = imageProcessor.getPixel(x, y);
                result.putPixel(x, y, (int)Math.round(Math.pow(rgbOriginal, power)));
            }
        }

        return result;
    }

    public static ImageProcessor drawHSVHistogram(ImageProcessor imageProcessor, int bins, int binPixelWidth, int heightPerHistogram) {
        return drawistogram(getHSVHistogram(imageProcessor, bins), imageProcessor, bins, binPixelWidth, heightPerHistogram);
    }

    public static ImageProcessor drawistogram(int[][] hsvHistogram, ImageProcessor imageProcessor, int bins, int binPixelWidth, int heightPerHistogram) {
        ImageProcessor result = new ColorProcessor((binPixelWidth + 1) * bins, heightPerHistogram * 3);

        Color[] hcolors = new Color[bins];
        for (int binNr = 0; binNr < bins; binNr++) {
            hcolors[binNr] = colorFromHsv(new float[] { binNr / bins, 1.0F, 1.0F });
        }

        ImageProcessor h_histogram = drawHistogram(hsvHistogram[0], binPixelWidth, heightPerHistogram, hcolors);
        result.copyBits(h_histogram, 0, 0 * heightPerHistogram, 0);

        ImageProcessor s_histogram = drawHistogram(hsvHistogram[1], binPixelWidth, heightPerHistogram, Color.white);
        result.copyBits(s_histogram, 0, 1 * heightPerHistogram, 0);

        ImageProcessor v_histogram = drawHistogram(hsvHistogram[2], binPixelWidth, heightPerHistogram, Color.white);
        result.copyBits(v_histogram, 0, 2 * heightPerHistogram, 0);

        return result;
    }

    public static Color newColor(double intensity) {
        int v = (int)(intensity * 255.0D);
        return new Color(v, v, v);
    }

    public static Color colorFromHsv(float[] hsb)
    {
        int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        return new Color(rgb);
    }

    public static int[][] getHSVHistogram(ImageProcessor imageProcessor, int bins)
    {
        double binsize = 1.0D / bins;
        int[][] hsvHistogram = new int[3][bins + 1];
        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++)
            {
                float[] hsv = getHSV(imageProcessor.getPixel(x, y));
                hsvHistogram[0][(int)(hsv[0] / binsize)] += 1;
                hsvHistogram[1][(int)(hsv[1] / binsize)] += 1;
                hsvHistogram[2][(int)(hsv[2] / binsize)] += 1;
            }

        }

        return hsvHistogram;
    }

    public static ImageProcessor drawHistogram(int[] histogram, int binPixelWidth, int height, Color[] colors) {
        ImageProcessor histogramImage = new ColorProcessor((binPixelWidth + 1) * histogram.length, height);

        int max = getMax(histogram);

        for (int i = 0; i < histogram.length; i++) {
            histogramImage.setColor(colors[(i % colors.length)]);
            int x = i * binPixelWidth + i;
            int y = height - height * histogram[i] / max;
            fillRect(histogramImage, x, y, binPixelWidth, height - y);
        }

        return histogramImage;
    }

    public static void fillRect(ImageProcessor imageProcessor, int x, int y, int width, int height)
    {
        imageProcessor.fillPolygon(getPolygon(x, y, width, height));
    }

    public static Polygon newPolygon(Collection<Point> points, boolean repeatFirst) {
        Polygon poly = new Polygon();
        for (Point p : points) {
            poly.addPoint(p.x, p.y);
        }

        if (repeatFirst) {
            Iterator i$ = points.iterator(); if (i$.hasNext()) { Point p = (Point)i$.next();
                poly.addPoint(p.x, p.y);
                return poly;
            }
        }

        return poly;
    }

    public static Polygon getPolygon(int x, int y, int width, int height) {
        Polygon p = new Polygon();
        p.addPoint(x, y);
        p.addPoint(x, y + height);
        p.addPoint(x + width, y + height);
        p.addPoint(x + width, y);
        return p;
    }

    public static ImageProcessor drawHistogram(int[] histogram, int binPixelWidth, int height, Color color) {
        Color[] colors = { color };
        return drawHistogram(histogram, binPixelWidth, height, colors);
    }

    public static ImageProcessor quantilize(ImageProcessor imageProcessor, double percentage)
    {
        ImageProcessor result = imageProcessor.duplicate();

        int[] histogram = result.getHistogram();

        int pixelCount = 0;
        int histogramThreshold = histogram.length;
        for (int i = histogram.length - 1; i >= 0; i--)
        {
            pixelCount += histogram[i];

            if (pixelCount / imageProcessor.getPixelCount() >= percentage) {
                histogramThreshold = i;
                break;
            }
        }

        System.out.println("threshold == " + histogramThreshold);
        result.threshold(histogramThreshold);

        return result;
    }

    private static void drawCluster(List<Point> cluster, ImageProcessor original, ImageProcessor clusterImage, boolean meanColor, boolean drawConvexHull, Random random) {
        Color color = meanColor ? new Color(getMeanColor(cluster, original)) : randomColor(50, random);
        drawPoints(cluster, clusterImage, color);
        if (drawConvexHull)
            clusterImage.drawPolygon(ConvexHullTools.get(cluster));
    }

    public static ImageProcessor drawClusters(List<List<Point>> clusters, ImageProcessor original, boolean meanColor, boolean drawConvexHull, Random random)
    {
        ImageProcessor clusterImage = newBlank(original, Color.black);

        for (List cluster : clusters) {
            drawCluster(cluster, original, clusterImage, meanColor, drawConvexHull, random);
        }
        return clusterImage;
    }

    public static ImageProcessor drawClusters(List<List<Point>> clusters, ImageProcessor original, boolean meanColor, boolean drawConvexHull) {
        return drawClusters(clusters, original, meanColor, drawConvexHull, new Random());
    }

    public static ImageProcessor newBlank(ImageProcessor original, Color fill) {
        ImageProcessor duplicate = original.duplicate();
        duplicate.setColor(fill);
        duplicate.fill();
        return duplicate;
    }

    public static double[] getMeanLab(List<Point> points, ImageProcessor imageProcessor) {
        double[][] labColors = new double[3][points.size()];

        for (int i = 0; i < points.size(); i++) {
            Point p = (Point)points.get(i);
            double[] hsv = ColorConversions.getLab(imageProcessor.getPixel(p.x, p.y));
            labColors[0][i] = hsv[0];
            labColors[1][i] = hsv[1];
            labColors[2][i] = hsv[2];
        }

        double[] mean = new double[3];
        mean[0] = getMean(labColors[0]);
        mean[1] = getMean(labColors[1]);
        mean[2] = getMean(labColors[2]);

        return mean;
    }

    public static int getMeanColor(List<Point> points, ImageProcessor imageProcessor) {
        float[][] hsvColors = new float[3][points.size()];

        for (int i = 0; i < points.size(); i++) {
            Point p = (Point)points.get(i);
            float[] hsv = getHSV(imageProcessor.getPixel(p.x, p.y));
            hsvColors[0][i] = hsv[0];
            hsvColors[1][i] = hsv[1];
            hsvColors[2][i] = hsv[2];
        }

        float h_mean = getMeanFloat(hsvColors[0]);
        float s_mean = getMeanFloat(hsvColors[1]);
        float v_mean = getMeanFloat(hsvColors[2]);

        return Color.HSBtoRGB(h_mean, s_mean, v_mean);
    }

    public static double[][] multiplyDoubleFields(double[][] a, double[][] b) {
        double[][] result = new double[a.length][a[0].length];

        for (int x = 0; x < a.length; x++) {
            for (int y = 0; y < a[x].length; y++) {
                a[x][y] *= b[x][y];
            }
        }

        return result;
    }

    public static double[][] multiplyDoubleFields(double[][] a, double multiplier) {
        double[][] result = new double[a.length][a[0].length];

        for (int x = 0; x < a.length; x++) {
            for (int y = 0; y < a[x].length; y++) {
                a[x][y] *= multiplier;
            }
        }

        return result;
    }

    public static double[][] getNeighbourhood(Point p, int size, double[][] field) {
        int fromX = Math.max(0, p.x - size);
        int fromY = Math.max(0, p.y - size);
        int toX = Math.min(field.length - 1, p.x + size);
        int toY = Math.min(field[0].length - 1, p.y + size);

        double[][] result = new double[toX - fromX + 1][toY - fromY + 1];

        for (int x = fromX; x <= toX; x++) {
            for (int y = fromY; y <= toY; y++) {
                result[(x - fromX)][(y - fromY)] = field[x][y];
            }
        }

        return result;
    }

    public static double[][] getNeighbourhood(Point p, int size, int[][] field) {
        int fromX = Math.max(0, p.x - size);
        int fromY = Math.max(0, p.y - size);
        int toX = Math.min(field.length - 1, p.x + size);
        int toY = Math.min(field[0].length - 1, p.y + size);

        double[][] result = new double[toX - fromX + 1][toY - fromY + 1];

        for (int x = fromX; x <= toX; x++) {
            for (int y = fromY; y <= toY; y++) {
                result[(x - fromX)][(y - fromY)] = field[x][y];
            }
        }

        return result;
    }

    public static Set<Point> getNeighbourPoints(Point p, int size, int width, int height, boolean includingCenter) {
        Set result = new HashSet();
        for (int dx = -size; dx <= size; dx++) {
            for (int dy = -size; dy <= size; dy++) {
                if ((includingCenter) || (dx != 0) || (dy != 0)) {
                    Point neighbour = new Point(p.x + dx, p.y + dy);
                    if ((neighbour.x < width) && (neighbour.y < height) && (neighbour.x >= 0) && (neighbour.y >= 0)) {
                        result.add(neighbour);
                    }
                }
            }
        }
        return result;
    }

    public static Set<Point> getDisjunktPoints(Set<Point> listA, Set<Point> listB) {
        Set disjunktPoints = new HashSet();

        for (Point a : listA) {
            if (!listB.contains(a)) {
                disjunktPoints.add(a);
            }
        }

        for (Point b : listB) {
            if (!listA.contains(b)) {
                disjunktPoints.add(b);
            }
        }

        return disjunktPoints;
    }

    public static Set<Point> difference(Set<Point> listA, Set<Point> listB) {
        Set result = new HashSet(listA);
        result.removeAll(listB);
        return result;
    }

    public static List<Point> get8Neighbourhood(Point p, int width, int height) {
        List result = new LinkedList();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if ((dx != 0) || (dy != 0)) {
                    Point neighbour = new Point(p.x + dx, p.y + dy);
                    if ((neighbour.x < width) && (neighbour.y < height) && (neighbour.x >= 0) && (neighbour.y >= 0)) {
                        result.add(neighbour);
                    }
                }
            }
        }
        return result;
    }

    public static Color randomColor()
    {
        return randomColor(0);
    }

    public static Color randomColor(int min, int seed) {
        if (randomSeed != seed) {
            randomSeed = seed;
            randomGenerator = new Random(seed);
        }

        return randomColor(min, randomGenerator);
    }

    public static Color randomColor(int min, Random rnd) {
        int r = (int)(rnd.nextDouble() * (255 - min)) + min;
        int g = (int)(rnd.nextDouble() * (255 - min)) + min;
        int b = (int)(rnd.nextDouble() * (255 - min)) + min;
        return new Color(r, g, b);
    }

    public static Color randomColor(int min) {
        return randomColor(min, new Random());
    }

    public static int pointInListOfPoints(Point p, List<List<Point>> listOfPoints) {
        for (int i = 0; i < listOfPoints.size(); i++) {
            if (((List)listOfPoints.get(i)).contains(p)) {
                return i;
            }
        }

        return -1;
    }

    public static float[] getMeanValues(float[][] colors) {
        float[] mean = new float[3];
        mean[0] = getMeanFloat(colors[0]);
        mean[1] = getMeanFloat(colors[1]);
        mean[2] = getMeanFloat(colors[2]);
        return mean;
    }

    public static double euklidDistance(int[] a, int[] b) {
        int sum = 0;

        for (int i = 0; i < a.length; i++) {
            sum = (int)(sum + Math.pow(a[i] - b[i], 2.0D));
        }

        return Math.sqrt(sum);
    }

    public static double euklidDistance(double[] a, double[] b) {
        double sum = 0.0D;

        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2.0D);
        }

        return Math.sqrt(sum);
    }

    public static double euklidDistance(double a, double b) {
        return Math.sqrt(Math.pow(a - b, 2.0D));
    }

    public static float[] getMeanHSVValues(Vector<Integer> rgbValues) {
        float[][] hsvValues = getHSV_Values(rgbValues);
        return getMeanValues(hsvValues);
    }

    public static float[][] getHSV_Values(Vector<Integer> rgbValues) {
        float[][] hsvValues = new float[3][rgbValues.size()];
        for (int i = 0; i < rgbValues.size(); i++) {
            float[] hsv = getHSV(((Integer)rgbValues.get(i)).intValue());
            hsvValues[0][i] = hsv[0];
            hsvValues[1][i] = hsv[1];
            hsvValues[2][i] = hsv[2];
        }
        return hsvValues;
    }

    public static double[] getHSV(int r, int g, int b) {
        float[] hsv = getHSV(new Color(r, g, b).getRGB());
        double[] hsvD = new double[3];
        hsvD[0] = hsv[0];
        hsvD[1] = hsv[1];
        hsvD[2] = hsv[2];
        return hsvD;
    }

    public static double[] getHSV(int[] rgb) {
        float[] hsv = getHSV(new Color(rgb[0], rgb[1], rgb[2]).getRGB());
        double[] hsvD = new double[3];
        hsvD[0] = hsv[0];
        hsvD[1] = hsv[1];
        hsvD[2] = hsv[2];
        return hsvD;
    }

    public static float[] getHSV(int rgb) {
        int r = (rgb & 0xFF0000) >> 16;
        int g = (rgb & 0xFF00) >> 8;
        int b = rgb & 0xFF;
        return Color.RGBtoHSB(r, g, b, null);
    }

    public static boolean[][] blankFieldFromPpints(List<Point> points)
    {
        int maxX = 0;
        int maxY = 0;
        for (Point p : points) {
            if (p.x > maxX) {
                maxX = p.x;
            }
            if (p.y > maxY) {
                maxY = p.y;
            }
        }

        boolean[][] result = new boolean[maxX + 1][maxY + 1];

        return result;
    }

    public static boolean[][] pointsToFieldArray(List<Point> points)
    {
        boolean[][] result = blankFieldFromPpints(points);

        for (Point p : points) {
            result[p.x][p.y] = 1;
        }

        return result;
    }

    public static ImageProcessor cropToMask(ImageProcessor original, ImageProcessor mask) {
        ImageProcessor crop = original.duplicate();
        if (mask == null) {
            return crop;
        }
        crop.copyBits(mask, 0, 0, 2);
        crop.setMask(mask);
        return crop;
    }

    public static ImageProcessor cropToMask(ImageProcessor original, Color background) {
        ImageProcessor cropped = cropToMask(original, original.getMask());
        if (original.getMask() == null) {
            return cropped;
        }
        cropped.setMask(original.getMask());
        return maskBackground(cropped, background);
    }

    public static ImageProcessor cropToMask(ImageProcessor original) {
        return cropToMask(original, original.getMask());
    }

    public static double getL1Dist(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    public static double getL2Dist(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2.0D) + Math.pow(a.y - b.y, 2.0D));
    }

    public static double getL2Dist(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2.0D) + Math.pow(y1 - y2, 2.0D));
    }

    public static List<Point> createPoints(int width, int height) {
        List points = new ArrayList();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++)
            {
                points.add(new Point(x, y));
            }
        }

        return points;
    }

    public static List<Point> imageProcessorToPoints(ImageProcessor original, int threshold) {
        ImageProcessor imageProcessor = original.convertToByte(true).duplicate();
        threshold = Math.max(0, threshold - 1);
        imageProcessor.threshold(threshold);

        List points = new Vector();

        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++) {
                if (imageProcessor.getPixel(x, y) != 0) {
                    points.add(new Point(x, y));
                }
            }
        }

        return points;
    }

    public static void drawPoints(MQueue<Point> points, ImageProcessor canvas, Color color) {
        canvas.setColor(color);
        for (Point p : points)
            canvas.drawPixel(p.x, p.y);
    }

    public static void drawPoints(Collection<Point> points, ImageProcessor canvas, Color color)
    {
        canvas.setColor(color);
        for (Point p : points)
            canvas.drawPixel(p.x, p.y);
    }

    public static <K, V extends Comparable<? super V>> List<K> getKeysSortedByValue(Map<K, V> map)
    {
        int size = map.size();
        List list = new ArrayList(size);
        list.addAll(map.entrySet());
        ValueComparator cmp = new ValueComparator(null);
        Collections.sort(list, cmp);
        List keys = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            keys.set(i, ((Map.Entry)list.get(i)).getKey());
        }
        return keys;
    }

    public static int getLongestSide(ImageProcessor a)
    {
        return Math.max(a.getWidth(), a.getHeight());
    }

    public static int getLongestSide(ImageProcessor a, ImageProcessor b) {
        int maxA = Math.max(a.getWidth(), a.getHeight());
        int maxB = Math.max(b.getWidth(), b.getHeight());
        return Math.max(maxA, maxB);
    }

    public static int random(int n)
    {
        return random.nextInt(n);
    }

    public static boolean chance(double percent)
    {
        return random((int)(1.0D / percent)) == 0;
    }

    public static List<Integer> getHistogram(ImageProcessor imageProcessor) {
        imageProcessor.setRoi(0, 0, imageProcessor.getWidth(), imageProcessor.getHeight());
        int[] ints = imageProcessor.getHistogram();
        List result = new Vector();
        for (int i : ints) {
            result.add(Integer.valueOf(i));
        }
        return result;
    }

    public static List<Integer> getAllUsedBrightnessValues(ImageProcessor imageProcessor) {
        List result = new Vector();

        int[] bins = imageProcessor.getHistogram();
        for (int i = 0; i < bins.length; i++)
        {
            if (bins[i] > 0) {
                result.add(Integer.valueOf(i));
            }
        }

        return result;
    }

    public static ImageProcessor sharpen(ImageProcessor imageProcessor, int times) {
        ImageProcessor sharpend = imageProcessor.duplicate();
        for (int i = 0; i < times; i++)
        {
            sharpend.sharpen();
        }

        return sharpend;
    }

    public static ImageProcessor blur(ImageProcessor imageProcessor, double radius, int iterations) {
        GaussianBlur gb = new GaussianBlur();
        ImageProcessor result = imageProcessor.duplicate();
        for (int i = 0; i < iterations; i++)
        {
            gb.blur(result, radius);
        }

        return result;
    }

    public static ImageProcessor blur(ImageProcessor imageProcessor, double radius) {
        if (radius == 0.0D) {
            return imageProcessor.duplicate();
        }
        ImageProcessor blured = imageProcessor.duplicate();
        new GaussianBlur().blur(blured, radius);
        return blured;
    }

    public static double getMeanDouble(Collection<Double> doubles)
    {
        if (doubles.size() == 0) {
            return 0.0D;
        }

        double mean = 0.0D;

        for (Double d : doubles) {
            mean += d.doubleValue();
        }

        return mean / doubles.size();
    }

    public static float getMeanFloat(float[] floats) {
        if (floats.length == 0) {
            return 0.0F;
        }

        double mean = 0.0D;

        for (float f : floats) {
            mean += f;
        }

        return (float)(mean / floats.length);
    }

    public static Vector<Integer> getIntVectorFromIntsArray(int[] intArray) {
        Vector intVector = new Vector();
        for (int i : intArray) {
            intVector.add(new Integer(i));
        }

        return intVector;
    }

    public static Vector<Double> getDoubleVectorFromIntsArray(int[] ints)
    {
        Vector doubles = new Vector();
        for (int i : ints) {
            doubles.add(new Double(i));
        }

        return doubles;
    }

    public static Vector<Double> getDoubleVectorFromIntegerVector(Collection<Integer> ints) {
        Vector doubles = new Vector();
        for (Integer i : ints) {
            doubles.add(new Double(i.intValue()));
        }
        return doubles;
    }

    public static <Nr extends Number> double sum(Collection<Nr> numbers) {
        double sum = 0.0D;
        for (Number v : numbers) {
            sum += v.doubleValue();
        }
        return sum;
    }

    public static <Nr extends Number> double meanValue(Collection<Nr> numbers) {
        return sum(numbers) / numbers.size();
    }

    public static <Nr extends Number> double standardDeviation(Collection<Nr> numbers) {
        double mean = meanValue(numbers);
        double sum = 0.0D;
        for (Number v : numbers) {
            double diff = v.doubleValue() - mean;
            sum += diff * diff;
        }
        return Math.sqrt(sum / mean);
    }

    public static double getStandardDeviationFromIntsVector(Collection<Integer> ints) {
        return getStandardDeviation(getDoubleVectorFromIntegerVector(ints));
    }

    public static double getStandardDeviation(int[] ints) {
        return getStandardDeviation(getDoubleVectorFromIntsArray(ints));
    }

    public static Vector<Double> getDoublesVectorFromArray(double[] doubles) {
        Vector doublesVector = new Vector();
        for (double d : doubles) {
            doublesVector.add(Double.valueOf(d));
        }
        return doublesVector;
    }

    public static Vector<Double> getDoublesVectorFromArray(float[] floats) {
        Vector result = new Vector();
        for (float f : floats) {
            result.add(new Double(f));
        }
        return result;
    }

    public static double getStandardDeviation(float[] floats) {
        return getStandardDeviation(getDoublesVectorFromArray(floats));
    }

    public static double getStandardDeviation(double[] doubles) {
        return getStandardDeviation(getDoublesVectorFromArray(doubles));
    }

    public static double getStandardDeviation(Collection<Double> doubles) {
        if (doubles.size() <= 1) {
            return 0.0D;
        }

        double mean = getMeanDouble(doubles);

        double sum = 0.0D;
        for (Double d : doubles) {
            sum += Math.pow(d.doubleValue() - mean, 2.0D);
        }

        return Math.sqrt(sum / (doubles.size() - 1));
    }

    public static void showImage(String windowName, ImageProcessor imageProcessor, String title)
    {
        showImage(windowName, imageProcessor, title, false);
    }

    public static void showImage(String windowName, ImageProcessor imageProcessor, String title, boolean saveFile)
    {
        imageProcessor = imageProcessor.duplicate();

        if (saveFile) {
            save(imageProcessor);
        }

        ImagePlus imagePlus = (ImagePlus)images.get(windowName);
        if (imagePlus == null) {
            imagePlus = new ImagePlus(windowName);
            int posX = Math.max(imageProcessor.getWidth(), imageProcessor.getHeight()) * images.size();

            imagePlus.setTitle(title);
            imagePlus.setProcessor(imageProcessor);
            imagePlus.show();

            imagePlus.getWindow().setLocation(posX, 10);

            images.put(windowName, imagePlus);
        } else {
            imagePlus.setTitle(title);
            imagePlus.setProcessor(imageProcessor);
            imagePlus.show();
        }
    }

    public static void showImageInNewWindow(ImageProcessor ip)
    {
        new ImagePlus("Image", ip).show();
    }

    public static void showImage(ImageProcessor ip) {
        showImage(ip, "image");
    }

    public static void showImage(ImageProcessor ip, String title) {
        imagePlus.setTitle(title);
        imagePlus.setProcessor(ip);
        imagePlus.show();
    }

    public static void printIntArray(int[] array, int ignoreValue, String format) {
        System.out.print("(");
        for (int i = 0; i < array.length; i++) {
            if (array[i] != ignoreValue) {
                System.out.print(formatNumber(array[i], format));
                if (i < array.length - 1) {
                    System.out.print(", ");
                }
            }
        }
        System.out.println(")");
    }

    public static void printIntArray(int[] array, int ignoreValue) {
        printIntArray(array, ignoreValue, "0");
    }

    public static void printDoubleArray(double[] array, double ignoreValue, String formatStr) {
        System.out.print("(");
        for (int i = 0; i < array.length; i++) {
            if (array[i] != ignoreValue) {
                System.out.print(new DecimalFormat(formatStr).format(array[i]));
                if (i < array.length - 1) {
                    System.out.print(", ");
                }
            }
        }
        System.out.println(")");
    }

    public static short[][] randomShortArray(int width, int height, short min, short max, double value_ws) {
        short[][] result = new short[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (Math.random() > 1.0D - value_ws) {
                    short value = (short)(int)Math.round(Math.random() * (max - min));
                    value = (short)(value + min);
                    result[x][y] = value;
                }
            }
        }
        return result;
    }

    public static ImageProcessor invert(ImageProcessor imageProcessor) {
        ImageProcessor result = imageProcessor.duplicate();
        result.invert();
        return result;
    }

    public static boolean equalPixels(ImageProcessor a, ImageProcessor b) {
        if ((a.getWidth() != b.getWidth()) || (a.getHeight() != b.getHeight())) {
            return false;
        }
        for (int x = 0; x < a.getWidth(); x++) {
            for (int y = 0; y < a.getHeight(); y++) {
                if (a.getPixel(x, y) != b.getPixel(x, y)) {
                    DEBUG.println(a.getPixel(x, y) + " != " + b.getPixel(x, y) + " at position (" + x + "," + y + ")");
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean equalHistograms(ImageProcessor a, ImageProcessor b)
    {
        if ((a == null) || (b == null)) {
            return false;
        }

        int[] histogramA = a.getHistogram();
        int[] histogramB = b.getHistogram();

        for (int i = 0; i < histogramA.length; i++) {
            if (histogramA[i] != histogramB[i]) {
                return false;
            }
        }
        return true;
    }

    public static void makeDirectory(String dirName) {
        new File(dirName).mkdirs();
    }

    public static void copyImageProcessor(ImageProcessor source, ImageProcessor destination) {
        destination.copyBits(source, 0, 0, 0);
    }

    public static float[] createArray(float stdValue, int times) {
        float[] result = new float[times];
        int i = 0;
        for (; i < times; i++) {
            result[i] = stdValue;
        }
        return result;
    }

    public static ImageProcessor createImageProcessorFromArray(double[][] array) {
        int width = array.length;
        if (width == 0) {
            return new ByteProcessor(0, 0);
        }
        int height = array[0].length;
        ImageProcessor ip = new FloatProcessor(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                ip.putPixelValue(x, y, array[x][y]);
            }
        }
        return ip;
    }

    public static ImageProcessor createImageProcessorFromArray(short[][] array, boolean binary)
    {
        int width = array.length;
        if (width == 0) {
            return new ByteProcessor(0, 0);
        }
        int height = array[0].length;
        ImageProcessor ip = new ByteProcessor(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int value = array[x][y];
                if ((binary) &&
                        (value != 0)) {
                    value = 255;
                }

                ip.putPixel(x, y, value);
            }
        }
        return ip;
    }

    public static ImageProcessor createImageProcessor(boolean[][] array)
    {
        int width = array.length;
        int height = array[0].length;
        ImageProcessor ip = new ByteProcessor(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (array[x][y] != 0) {
                    ip.putPixelValue(x, y, 255.0D);
                }
            }
        }
        return ip;
    }

    public static double[][] getDoubles(ImageProcessor imageProcessor) {
        int width = imageProcessor.getWidth();
        int height = imageProcessor.getHeight();

        double[][] result = new double[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                result[x][y] = imageProcessor.getPixelValue(x, y);
            }
        }

        return result;
    }

    public static short[][] getValuesFromImageProcessor(ImageProcessor imageProcessor) {
        int width = imageProcessor.getWidth();
        int height = imageProcessor.getHeight();
        short[][] result = new short[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                result[x][y] = (short)imageProcessor.getPixel(x, y);
            }
        }
        return result;
    }

    public static int[][] getIntArrayFromImageProcessor(ImageProcessor imageProcessor) {
        int width = imageProcessor.getWidth();
        int height = imageProcessor.getHeight();
        int[][] result = new int[width][height];
        int x = 0;
        for (; x < width; x++) {
            int y = 0;
            for (; y < height; y++) {
                result[x][y] = imageProcessor.getPixel(x, y);
            }
        }
        return result;
    }

    public static List<Integer> getIntValuesFromImageProcessor(ImageProcessor imageProcessor, int ignoreValue)
    {
        List result = new Vector();

        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++)
            {
                int value = imageProcessor.getPixel(x, y);
                if (value != ignoreValue) {
                    result.add(Integer.valueOf(value));
                }
            }
        }

        return result;
    }

    public static short[][] duplicateArray(short[][] array) {
        short[][] result = new short[array.length][array[0].length];
        for (int x = 0; x < array.length; x++) {
            for (int y = 0; y < array[x].length; y++) {
                result[x][y] = array[x][y];
            }
        }
        return result;
    }

    public static int[][] duplicateArray(int[][] array) {
        int[][] result = new int[array.length][array[0].length];
        for (int x = 0; x < array.length; x++) {
            for (int y = 0; y < array[x].length; y++) {
                result[x][y] = array[x][y];
            }
        }
        return result;
    }

    public static ImageProcessor pointWise(ImageProcessor O, ImageProcessor Or, Method method)
    {
        ImageProcessor result = O.duplicate();
        int x = 0;
        for (; x < O.getWidth(); x++) {
            int y = 0;
            for (; y < O.getHeight(); y++) {
                int oValue = O.getPixel(x, y);
                int orValue = Or.getPixel(x, y);
                if (method.equals(Method.min))
                    result.putPixel(x, y, (short)Math.min(oValue, orValue));
                else {
                    result.putPixel(x, y, (short)Math.max(oValue, orValue));
                }
            }
        }
        return result;
    }

    public static short[][] pointWise(short[][] imageA, short[][] imageB, Method method) {
        short[][] result = new short[imageA.length][imageA[0].length];
        int x = 0;
        for (; x < imageA.length; x++) {
            int y = 0;
            for (; y < imageA[0].length; y++) {
                short a = imageA[x][y];
                short b = imageB[x][y];
                if (method.equals(Method.min))
                    result[x][y] = (short)Math.min(a, b);
                else {
                    result[x][y] = (short)Math.max(a, b);
                }
            }
        }
        return result;
    }

    public static String formatNumber(double d)
    {
        return formatNumber(d, "0.00");
    }

    public static String formatNumber(double d, String formatStr) {
        return new DecimalFormat(formatStr).format(d);
    }

    public static void printShortArray(short[][] intArray, int highlightX, int highlightY, String formatStr, boolean codeFormatted) {
        if (codeFormatted) {
            System.out.print("{");
        }
        for (int x = 0; x < intArray.length; x++) {
            if (codeFormatted) {
                System.out.print("{");
            }
            for (int y = 0; y < intArray[0].length; y++) {
                short arrayValue = intArray[x][y];
                String string = new DecimalFormat(formatStr).format(arrayValue);
                if ((x == highlightX) && (y == highlightY))
                    System.err.print(string + " ");
                else {
                    System.out.print(string + " ");
                }
                if ((codeFormatted) && (y < intArray[0].length - 1)) {
                    System.out.print(",");
                }
            }
            if (codeFormatted) {
                System.out.print("}");
                if (x < intArray.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
        if (codeFormatted) {
            System.out.println("}");
        }
        System.out.println();
    }

    public static void printShortArray(short[][] intArray, String formatStr, String caption) {
        System.out.println(caption);
        printShortArray(intArray, formatStr);
    }

    public static void printShortArray(short[][] intArray, String formatStr)
    {
        printShortArray(intArray, -1, -1, formatStr, false);
    }

    public static void setPixelsToImageProcessor(ImageProcessor ip, int[][] pixels) {
        int width = pixels.length;
        int height = pixels[0].length;
        int x = 0;
        for (; x < width; x++) {
            int y = 0;
            for (; y < height; y++)
                ip.putPixel(x, y, pixels[x][y]);
        }
    }

    public static ImageProcessor loadImageProcessor(BufferedImage i)
    {
        int type = i.getColorModel().getColorSpace().getType();
        ImageProcessor imageProcessor = type == 10 ? new ByteProcessor(i) : new ColorProcessor(i);
        imageProcessor.setRoi(0, 0, imageProcessor.getWidth(), imageProcessor.getHeight());
        return imageProcessor;
    }

    public static ImageProcessor loadImageProcessor(String fileName, double megapixel) throws IOException {
        return resize(loadImageProcessor(fileName), megapixel);
    }

    public static ImageProcessor loadImageProcessor(String fileName, int size) throws IOException {
        return resize(loadImageProcessor(fileName), size);
    }

    public static ImageProcessor loadImageProcessor(String fileName) throws IOException {
        if (!new File(fileName).exists()) {
            return null;
        }
        BufferedImage bufferedImage = ImageIO.read(new File(fileName));
        return loadImageProcessor(bufferedImage);
    }

    public static void fillROI(ImageProcessor imageProcessor) {
        imageProcessor.setRoi(0, 0, imageProcessor.getWidth(), imageProcessor.getHeight());
    }

    public static ImageProcessor resize(ImageProcessor imageProcessor, int longestSide)
    {
        if (longestSide == 0) {
            ImageProcessor result = imageProcessor.duplicate();
            fillROI(result);
            return result;
        }
        double longSideForSource = Math.max(imageProcessor.getWidth(), imageProcessor.getHeight());
        double longSideForDest = longestSide;
        double multiplier = longSideForDest / longSideForSource;
        int destWidth = (int)(imageProcessor.getWidth() * multiplier);
        int destHeight = (int)(imageProcessor.getHeight() * multiplier);
        fillROI(imageProcessor);
        return imageProcessor.resize(destWidth, destHeight);
    }

    public static ImageProcessor resize(ImageProcessor imageProcessor, int longestSide, int interpolationMethod)
    {
        if (longestSide == 0) {
            ImageProcessor result = imageProcessor.duplicate();
            fillROI(result);
            return result;
        }
        double longSideForSource = Math.max(imageProcessor.getWidth(), imageProcessor.getHeight());
        double longSideForDest = longestSide;
        double multiplier = longSideForDest / longSideForSource;
        int destWidth = (int)(imageProcessor.getWidth() * multiplier);
        int destHeight = (int)(imageProcessor.getHeight() * multiplier);
        fillROI(imageProcessor);
        imageProcessor.setInterpolationMethod(interpolationMethod);
        return imageProcessor.resize(destWidth, destHeight);
    }

    public static String getDir(String fileName) {
        int sep = fileName.lastIndexOf(47);
        return fileName.substring(0, sep);
    }

    public static String getDirectoryFromFileName(String fileName) {
        return new File(fileName).getAbsolutePath();
    }

    public static void saveToFile(ImageProcessor ip, String fileName) {
        makeDirectory(getDirectoryFromFileName(fileName));

        Image i = ip.createImage();
        BufferedImage bi = toBufferedImage(i, 1);
        try {
            ImageIO.write(bi, getExtension(fileName), new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(ImageProcessor org, String fileName)
    {
        ImageProcessor ip = org.duplicate();
        makeDirectory(getDirectoryFromFileName(fileName));
        saveToFile(ip, fileName);
    }

    public static void save(BufferedImage i) {
        if (i != null)
            save(loadImageProcessor(i));
    }

    public static void save(ImageProcessor org)
    {
        savedCount += 1;
        String dir = System.getProperty("user.home") + "/tmp/doftmp/";
        String fileName = dir + savedCount + ".png";
        save(org, fileName);
    }

    public static String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(46) + 1, fileName.length());
    }

    public static String getNameWithoutExtension(String fileNameWithPathAndExtension) {
        String nameOnly = new File(fileNameWithPathAndExtension).getName();
        return nameOnly.substring(0, nameOnly.lastIndexOf(46));
    }

    public static void showGrayscaleImageInNewWindow(ImageProcessor imageProcessor, String title, double scale) {
        int width = imageProcessor.getWidth();
        int height = imageProcessor.getHeight();
        ImagePlus newImage = NewImage.createByteImage(title, width, height, 1, 4);
        ImageProcessor inv_ip = newImage.getProcessor();
        inv_ip.copyBits(imageProcessor, 0, 0, 0);
        newImage.show();
        newImage.updateAndDraw();
    }

    public static void showGrayscaleImageInNewWindow(ImageProcessor imageProcessor, String title) {
        showGrayscaleImageInNewWindow(imageProcessor, title, 1.0D);
    }

    public static float[] scaleMatrix(int[] values, float scale)
    {
        float[] result = new float[values.length];
        int i = 0;
        for (; i < values.length; i++) {
            values[i] *= scale;
        }
        return result;
    }

    public static float[] rotateMatrix90(float[] values) {
        float[] result = new float[values.length];
        return null;
    }

    public static int cutToRange(int min, int max, int value) {
        int result = value;
        if (result < min) {
            result = min;
        }
        if (result > max) {
            result = max;
        }
        return result;
    }

    public static Vector<Integer> getNeighbourRGBValues(ImageProcessor ip, int centerX, int centerY, boolean includingCenterPoint) {
        return getNeighbours(ip, new Point(centerX, centerY), 1, includingCenterPoint);
    }

    public static Vector<Integer> getNeighbours(ImageProcessor ip, int centerX, int centerY, int radius, boolean includingCenterPoint) {
        return getNeighbours(ip, new Point(centerX, centerY), radius, includingCenterPoint);
    }

    public static Vector<Integer> getNeighbours(ImageProcessor ip, Point center, int radius, boolean includingCenterPoint) {
        Vector neighbours = new Vector();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++)
            {
                int x = center.x + dx;
                int y = center.y + dy;

                if ((!includingCenterPoint) && (x == center.x) && (y == center.y))
                    continue;
                if ((x >= 0) && (y >= 0) && (x < ip.getWidth()) && (y < ip.getHeight())) {
                    neighbours.add(Integer.valueOf(ip.getPixel(x, y)));
                }
            }

        }

        return neighbours;
    }

    public static Vector<Short> getNeighbours(short[][] ip, Point center, int radius) {
        Vector neighbours = new Vector();

        int dx = -radius;
        for (; dx <= radius; dx++) {
            int dy = -radius;
            for (; dy <= radius; dy++)
            {
                int x = center.x + dx;
                int y = center.y + dy;

                if ((x >= 0) && (y >= 0) && (x < ip.length) && (y < ip[0].length)) {
                    neighbours.add(Short.valueOf(ip[x][y]));
                }
            }
        }

        return neighbours;
    }

    public static Dimension getMaxDimension(List<Point> points)
    {
        int maxX = -2147483648;
        int maxY = -2147483648;

        for (Point p : points) {
            if (p.x > maxX) {
                maxX = p.x;
            }
            if (p.y > maxY) {
                maxY = p.y;
            }
        }
        return new Dimension(maxX, maxY);
    }

    public static double getMax(double[] doubles) {
        return getMax(getDoublesVectorFromArray(doubles));
    }

    public static int getMax(int[] array) {
        int max = -2147483648;

        int[] arr$ = array; int len$ = arr$.length; for (int i$ = 0; i$ < len$; i$++) { Integer i = Integer.valueOf(arr$[i$]);
            if (i.intValue() > max) {
                max = i.intValue();
            }
        }

        return max;
    }

    public static short getMax(short[][] values) {
        short max = values[0][0];
        int x = 0;
        for (; x < values.length; x++) {
            int y = 0;
            for (; y < values[0].length; y++) {
                short value = values[x][y];
                if (value > max) {
                    max = value;
                }
            }
        }
        return max;
    }

    public static int getMax(int[][] values) {
        int max = values[0][0];
        int x = 0;
        for (; x < values.length; x++) {
            int y = 0;
            for (; y < values[0].length; y++) {
                int value = values[x][y];
                if (value > max) {
                    max = value;
                }
            }
        }
        return max;
    }

    public static short getMaxShort(Vector<Short> values) {
        short max = ((Short)values.get(0)).shortValue();
        for (Iterator i$ = values.iterator(); i$.hasNext(); ) { short value = ((Short)i$.next()).shortValue();
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public static double getMax(Collection<Double> values) {
        double max = 4.9E-324D;
        for (Iterator i$ = values.iterator(); i$.hasNext(); ) { double value = ((Double)i$.next()).doubleValue();
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public static short getMinShort(Vector<Short> values) {
        short min = ((Short)values.get(0)).shortValue();
        for (Iterator i$ = values.iterator(); i$.hasNext(); ) { short value = ((Short)i$.next()).shortValue();
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    public static double getMin(Vector<Double> values) {
        double min = ((Double)values.get(0)).doubleValue();
        for (Iterator i$ = values.iterator(); i$.hasNext(); ) { double value = ((Double)i$.next()).doubleValue();
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    public static void grayScaleToBinaryImage(ImageProcessor ip) {
        int x = 0;
        for (; x < ip.getWidth(); x++) {
            int y = 0;
            for (; y < ip.getHeight(); y++)
                if (ip.getPixel(x, y) > 0)
                    ip.putPixel(x, y, 255);
        }
    }

    public static void sleep(int ms)
    {
        try
        {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean[] newBoolArray(int length, boolean stdValue) {
        boolean[] result = new boolean[length];
        int x = 0;
        for (; x < length; x++) {
            result[x] = stdValue;
        }
        return result;
    }

    public static boolean[][] newBoolArray2d(int width, int height, boolean stdValue) {
        boolean[][] result = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                result[x][y] = stdValue;
            }
        }
        return result;
    }

    public static double getMean(double[] doubles) {
        double mean = 0.0D;
        for (double d : doubles) {
            mean += d;
        }
        mean /= doubles.length;
        return mean;
    }

    public static double getMean(int[] ints)
    {
        return getMean(getIntVectorFromIntsArray(ints));
    }

    public static <Nr extends Number> double getMean(Collection<Nr> numbers) {
        double mean = 0.0D;
        for (Number number : numbers) {
            mean += number.doubleValue();
        }
        mean /= numbers.size();
        return mean;
    }

    public static String getCurrentDir() {
        String dir = "";
        try {
            dir = new File(".").getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dir;
    }

    public static Vector<String> getAllFilesFromDirectoryWithSubfolders(String dir, String[] suffixes) {
        Vector files = new Vector();
        listDirectory(dir, files, suffixes);
        return files;
    }

    public static Vector<String> getAllFilesFromDirectoryWithSubfolders(String dir, String suffix) {
        String[] suffixes = { suffix };
        return getAllFilesFromDirectoryWithSubfolders(dir, suffixes);
    }

    public static int getDirectoryCount(String dir) {
        return getAllFolders(dir).size();
    }

    public static List<String> getAllFolders(String dir) {
        List dirList = new LinkedList();
        String[] listing = new File(dir).list();

        for (String item : listing) {
            String dirName = dir + "/" + item;
            File f = new File(dirName);
            if (f.isDirectory()) {
                dirList.add(dirName);
            }
        }

        return dirList;
    }

    private static void listDirectory(String dir, Vector<String> files, String[] suffixes) {
        File currentDirectory = new File(dir);
        String[] listing = currentDirectory.list();
        for (int k = 0; k < listing.length; k++) {
            String separator = "/";
            if (new File(dir + separator + listing[k]).isDirectory()) {
                listDirectory(dir + separator + listing[k], files, suffixes);
            } else {
                String fileName = dir + separator + listing[k];
                boolean endsWithSuffix = false;
                for (String suffix : suffixes) {
                    if (fileName.endsWith(suffix)) {
                        endsWithSuffix = true;
                    }
                }
                if (endsWithSuffix)
                    files.add(fileName);
            }
        }
    }

    public static Vector<String> getFilesFromDirectory(String dirName, String suffix, boolean processSubfolders)
    {
        return processSubfolders ? getAllFilesFromDirectoryWithSubfolders(dirName, suffix) : getFilesFromDirectory(dirName, suffix);
    }

    public static Vector<String> getFilesFromDirectory(String dirName, String suffix) {
        if (!new File(dirName).isDirectory()) {
            System.err.println(dirName + " is not a valid directory.");
        }
        if (!new File(dirName).exists()) {
            System.err.println(dirName + " does not exist.");
        }

        Vector allFiles = new Vector();
        File f = new File(dirName);
        for (String nameOnly : f.list()) {
            if ((nameOnly.endsWith(suffix)) && (!new File(dirName + "/" + nameOnly).isDirectory())) {
                allFiles.add(dirName + "/" + nameOnly);
            }
        }
        return allFiles;
    }

    public static BufferedImage toBufferedImage(Image image, int imageType)
    {
        Label dummyObserver = new Label();
        int width = image.getWidth(dummyObserver);
        int height = image.getHeight(dummyObserver);

        BufferedImage bImage = new BufferedImage(width, height, imageType);

        bImage.getGraphics().drawImage(image, 0, 0, dummyObserver);
        return bImage;
    }

    public static double[] difference(double[] a, double[] b) {
        int maxLength = Math.max(a.length, b.length);
        int minLength = Math.max(a.length, b.length);
        double[] difference = new double[maxLength];

        for (int i = 0; i < maxLength; i++) {
            if (i < minLength)
                a[i] -= b[i];
            else {
                difference[i] = 1.7976931348623157E+308D;
            }
        }

        return difference;
    }

    public static double[] differenceAbs(float[] a, float[] b) {
        int maxLength = Math.max(a.length, b.length);
        int minLength = Math.max(a.length, b.length);
        double[] difference = new double[maxLength];

        for (int i = 0; i < maxLength; i++) {
            if (i < minLength)
                difference[i] = Math.abs(a[i] - b[i]);
            else {
                difference[i] = 1.7976931348623157E+308D;
            }
        }

        return difference;
    }

    public static int[] difference(int[] a, int[] b) {
        int maxLength = Math.max(a.length, b.length);
        int minLength = Math.max(a.length, b.length);
        int[] difference = new int[maxLength];

        for (int i = 0; i < maxLength; i++) {
            if (i < minLength)
                a[i] -= b[i];
            else {
                difference[i] = 2147483647;
            }
        }

        return difference;
    }

    public static short[][] difference(short[][] a, short[][] b) {
        int width = a.length;
        int height = a[0].length;
        short[][] difference = new short[width][height];
        int x = 0;
        for (; x < width; x++) {
            int y = 0;
            for (; y < height; y++)
            {
                difference[x][y] = (short)Math.abs(a[x][y] - b[x][y]);
            }
        }
        return difference;
    }

    public static int intersect(short[][] a, short[][] b, short ignoreValue) {
        int intersection = 0;
        int x = 0;
        for (; x < a.length; x++) {
            int y = 0;
            for (; y < a[0].length; y++) {
                if ((a[x][y] == b[x][y]) && (a[x][y] != ignoreValue)) {
                    intersection++;
                }
            }
        }
        return intersection;
    }

    public static int getPixelCount(ImageProcessor i, int value) {
        int count = 0;
        for (int x = 0; x < i.getWidth(); x++) {
            for (int y = 0; y < i.getHeight(); y++) {
                if (i.getPixel(x, y) == value) {
                    count++;
                }
            }
        }

        return count;
    }

    public static int getPixelCount(ImageProcessor i, double fromValue, double toValue) {
        int count = 0;
        for (int x = 0; x < i.getWidth(); x++) {
            for (int y = 0; y < i.getHeight(); y++) {
                double pixelValue = i.getPixelValue(x, y);
                if ((pixelValue >= fromValue) && (pixelValue <= toValue)) {
                    count++;
                }
            }
        }

        return count;
    }

    public static int getPixelCount(short[][] pixels, short value) {
        int pixelCount = 0;
        int x = 0;
        for (; x < pixels.length; x++) {
            int y = 0;
            for (; y < pixels[0].length; y++) {
                if (pixels[x][y] == value) {
                    pixelCount++;
                }
            }
        }
        return pixelCount;
    }

    public static String addCounterToFileName(String fileName) {
        File f = new File(fileName);
        String path = f.getPath();
        String name = f.getName();
        int count = 0;
        String fileNameWithCounter = path + "/" + count + "-" + name;
        return fileNameWithCounter;
    }

    public static String getNameOnly(String fileName) {
        File f = new File(fileName);
        return f.getName().substring(0, f.getName().lastIndexOf("."));
    }

    public static String getFileNameWithoutDirectory(String fileName) {
        return new File(fileName).getName();
    }

    public static void showProgress(int current, int max)
    {
        System.out.println(current * 100 / max);
    }

    static
    {
        randomSeed = 0;
        randomGenerator = new Random(randomSeed);

        random = new Random();

        images = new HashMap();

        imagePlus = new ImagePlus();

        savedCount = 0;
    }

    public static enum Method
    {
        min, max;
    }

    private static final class ValueComparator<V extends Comparable<? super V>>
            implements Comparator<Map.Entry<?, V>>
    {
        public int compare(Map.Entry<?, V> o1, Map.Entry<?, V> o2)
        {
            return ((Comparable)o1.getValue()).compareTo(o2.getValue());
        }
    }
}