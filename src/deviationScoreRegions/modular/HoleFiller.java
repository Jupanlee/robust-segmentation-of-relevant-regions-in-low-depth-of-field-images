//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions.modular;

import ij.process.ImageProcessor;
import java.awt.Point;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class HoleFiller {
    private boolean[][] visited;

    public HoleFiller() {
    }

    private void getHole(ImageProcessor imageProcessor, HoleFiller.Hole hole, int x, int y) {
        if (x >= 0 && y >= 0 && x < this.visited.length && y < this.visited[0].length && !this.visited[x][y]) {
            this.visited[x][y] = true;
            if (imageProcessor.getPixel(x, y) == 0) {
                hole.pixels.add(new Point(x, y));
                ++hole.radius;
                this.getHole(imageProcessor, hole, x + 1, y);
                this.getHole(imageProcessor, hole, x - 1, y);
                this.getHole(imageProcessor, hole, x, y + 1);
                this.getHole(imageProcessor, hole, x, y - 1);
            }
        }

    }

    public ImageProcessor fill(ImageProcessor imageProcessor, int size) {
        ImageProcessor filled = imageProcessor.duplicate();
        this.visited = new boolean[imageProcessor.getWidth()][imageProcessor.getHeight()];

        for(int x = 0; x < imageProcessor.getWidth(); ++x) {
            for(int y = 0; y < imageProcessor.getHeight(); ++y) {
                if (!this.visited[x][y]) {
                    HoleFiller.Hole hole = new HoleFiller.Hole();
                    this.getHole(imageProcessor, hole, x, y);
                    if (!hole.pixels.isEmpty()) {
                        Iterator i$ = hole.pixels.iterator();

                        while(i$.hasNext()) {
                            Point p = (Point)i$.next();
                            imageProcessor.putPixel(p.x, p.y, 255);
                        }
                    }
                }
            }
        }

        return filled;
    }

    private class Hole {
        Collection<Point> pixels;
        int radius;

        private Hole() {
            this.pixels = new LinkedList();
            this.radius = 0;
        }
    }
}
