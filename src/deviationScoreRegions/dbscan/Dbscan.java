package deviationScoreRegions.dbscan;

import basics.Tools;
import basics.pointSetOperations.clustering.Query;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.util.List;
import java.util.Random;

public class Dbscan extends AbstractDBSCAN
{
    private Query query;

    public List<Point> range(Point p, int eps)
    {
        return this.query.range(p, eps);
    }

    public List<List<Point>> get(List<Point> points, int eps, int MinPts)
    {
        this.query = new Query(points);
        return super.get(points, eps, MinPts);
    }

    public static ImageProcessor noise(ImageProcessor original, int noisePoints, int value, Point start, int width, int height) {
        ImageProcessor imageProcessor = original.duplicate();
        for (int i = 0; i < noisePoints; i++) {
            int x = start.x + (int)(Math.random() * width);
            int y = start.y + (int)(Math.random() * height);
            imageProcessor.putPixel(x, y, value);
        }
        return imageProcessor;
    }

    public static void main(String[] args) throws Exception
    {
        ImageProcessor img = noise(new ByteProcessor(600, 600), 1000, 255, new Point(0, 0), 600, 600);

        img = noise(img, 500, 255, new Point(50, 60), 100, 50);
        img = noise(img, 400, 255, new Point(200, 300), 40, 80);
        img = noise(img, 300, 255, new Point(300, 300), 100, 50);
        img = noise(img, 200, 255, new Point(400, 20), 100, 200);
        Tools.save(img);

        ImageProcessor clusterImage = new ColorProcessor(img.getWidth(), img.getHeight());
        List clusters = new Dbscan().get(Tools.imageProcessorToPoints(img, 100), 25, 50);
        Tools.showImage("points", img, "dbscan test");
        Tools.showImage("dbscan test", Tools.drawClusters(clusters, clusterImage, false, true, new Random(12345L)), "dbscan test");
    }
}