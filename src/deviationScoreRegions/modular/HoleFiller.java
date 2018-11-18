package deviationScoreRegions.modular;

import ij.process.ImageProcessor;
import java.awt.Point;
import java.util.Collection;
import java.util.LinkedList;

public class HoleFiller
{
    private boolean[][] visited;

    private void getHole(ImageProcessor imageProcessor, Hole hole, int x, int y)
    {
        if ((x >= 0) && (y >= 0) && (x < this.visited.length) && (y < this.visited[0].length) && (this.visited[x][y] == 0)) {
            this.visited[x][y] = 1;
            if (imageProcessor.getPixel(x, y) == 0)
            {
                hole.pixels.add(new Point(x, y));
                hole.radius += 1;

                getHole(imageProcessor, hole, x + 1, y);
                getHole(imageProcessor, hole, x - 1, y);
                getHole(imageProcessor, hole, x, y + 1);
                getHole(imageProcessor, hole, x, y - 1);
            }
        }
    }

    public ImageProcessor fill(ImageProcessor imageProcessor, int size) {
        ImageProcessor filled = imageProcessor.duplicate();
        this.visited = new boolean[imageProcessor.getWidth()][imageProcessor.getHeight()];

        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++) {
                if (this.visited[x][y] == 0) {
                    Hole hole = new Hole(null);
                    getHole(imageProcessor, hole, x, y);
                    if (!hole.pixels.isEmpty()) {
                        for (Point p : hole.pixels) {
                            imageProcessor.putPixel(p.x, p.y, 255);
                        }
                    }
                }
            }
        }

        return filled;
    }

    private class Hole
    {
        Collection<Point> pixels = new LinkedList();
        int radius = 0;

        private Hole()
        {
        }
    }
}