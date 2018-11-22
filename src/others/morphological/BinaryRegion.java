package others.morphological;

import basics.Tools;
import java.awt.Point;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

public class BinaryRegion
        implements Comparable<BinaryRegion>
{
    private Set<Point> pixels = new HashSet();
    private int width;
    private int height;
    private int value;
    private int threshold;
    private short maxValue = 255;

    public BinaryRegion(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public BinaryRegion(int width, int height, int value) {
        this.width = width;
        this.height = height;
        this.value = value;
    }

    public Set<Point> getPoints()
    {
        return this.pixels;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return this.height;
    }

    public int getPixelCount() {
        return this.pixels.size();
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getThreshold() {
        return this.threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getMaxValue() {
        return this.maxValue;
    }

    public void setMaxValue(short maxValue) {
        this.maxValue = maxValue;
    }

    public void addPointToRegion(int x, int y)
    {
        if ((x >= this.width) || (y >= this.height)) {
            System.err.println("Error! in addPointToRegion!");
        }

        add(new Point(x, y));
    }

    private void add(Point pixel) {
        if (!this.pixels.contains(pixel))
            this.pixels.add(pixel);
    }

    public short[][] getPixels()
    {
        short[][] pixelTable = new short[this.width][this.height];
        for (Point pixel : this.pixels)
        {
            pixelTable[pixel.x][pixel.y] = 1;
        }

        return pixelTable;
    }

    public int intersect(BinaryRegion pixelRegion)
    {
        BinaryRegion smallerRegion;
        BinaryRegion largerRegion;
        if (pixelRegion.getPixelCount() > getPixelCount()) {
            largerRegion = pixelRegion;
            smallerRegion = this;
        } else {
            largerRegion = this;
            smallerRegion = pixelRegion;
        }

        int intersect = 0;
        for (Point point : smallerRegion.getPoints()) {
            if (largerRegion.contains(point)) {
                intersect++;
            }
        }

        return intersect;
    }

    public void addRegion(BinaryRegion pixelRegion) {
        for (Point pixel : pixelRegion.pixels)
            this.pixels.add(pixel);
    }

    public void print()
    {
        Tools.printShortArray(getPixels(), "0");
    }

    private boolean contains(Point pixel) {
        return this.pixels.contains(pixel);
    }

    private void addNeighbour(int x, int y, BinaryRegion boundaryPixels)
    {
        if ((x >= this.width) || (y >= this.height) || (x < 0) || (y < 0)) {
            return;
        }

        Point neighbour = new Point(x, y);

        if (!this.pixels.contains(neighbour))
        {
            boundaryPixels.add(neighbour);
        }
    }

    public BinaryRegion getBoundaryPixels() {
        BinaryRegion boundaryPixels = new BinaryRegion(this.width, this.height);

        for (Point pixel : this.pixels)
        {
            addNeighbour(pixel.x, pixel.y + 1, boundaryPixels);
            addNeighbour(pixel.x, pixel.y - 1, boundaryPixels);
            addNeighbour(pixel.x + 1, pixel.y, boundaryPixels);
            addNeighbour(pixel.x - 1, pixel.y, boundaryPixels);
        }

        return boundaryPixels;
    }

    public static BinaryRegion getRandomBinaryRegeion(int width, int height, int maxPoints)
    {
        BinaryRegion randomRegion = new BinaryRegion(width, height);
        Random rnd = new Random();
        for (int i = 0; i < maxPoints; i++) {
            int x = rnd.nextInt(width);
            int y = rnd.nextInt(height);
            randomRegion.addPointToRegion(x, y);
        }
        return randomRegion;
    }

    public static BinaryRegion union(Vector<BinaryRegion> regions)
    {
        BinaryRegion unionRegion = new BinaryRegion(2147483647, 2147483647);

        int maxWidth = 0;
        int maxHeight = 0;

        for (BinaryRegion region : regions)
        {
            unionRegion.addRegion(region);

            if (maxWidth < region.getWidth()) maxWidth = region.getWidth();
            if (maxHeight < region.getHeight()) maxHeight = region.getHeight();

        }

        unionRegion.width = maxWidth;
        unionRegion.height = maxHeight;

        return unionRegion;
    }

    public static void main(String[] args)
            throws IOException
    {
        int size = 4;
        int maxPoints = 5;

        BinaryRegion regionA = getRandomBinaryRegeion(size, size, maxPoints);
        BinaryRegion regionB = getRandomBinaryRegeion(size, size, maxPoints);

        System.out.println("regionA");
        Tools.printShortArray(regionA.getPixels(), -1, -1, "00", true);
        System.out.println();

        System.out.println("regionB");
        Tools.printShortArray(regionB.getPixels(), -1, -1, "00", true);
        System.out.println();

        int intersection = regionA.intersect(regionB);
        System.out.println("a intersect b = " + intersection);

        System.out.println("regionA size = " + regionA.getPixelCount());
        System.out.println("regionB size = " + regionB.getPixelCount());
    }

    public int compareTo(BinaryRegion o)
    {
        return getPixelCount() - o.getPixelCount();
    }
}