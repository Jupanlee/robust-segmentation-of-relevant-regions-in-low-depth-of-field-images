package others.morphological;

import basics.Tools;
import java.awt.Point;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

public class FlatRegions
{
    boolean sorted = false;

    private Vector<Vector<BinaryRegion>> regionsList = new Vector();

    public FlatRegions() {
        for (RegionType regionType : RegionType.values())
            this.regionsList.add(new Vector());
    }

    public Vector<BinaryRegion> getRegions(RegionType regionType)
    {
        return (Vector)this.regionsList.get(regionType.ordinal());
    }

    public void initFlatRegions(short[][] pixels, int minRegionValue, int maxRegionValue)
    {
        this.sorted = false;

        int width = pixels.length;
        int height = pixels[0].length;
        boolean[][] visited = Tools.newBoolArray2d(width, height, false);

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                if (visited[x][y] != false) {
                    continue;
                }
                int currentRegionPixelValue = pixels[x][y];

                BinaryRegion currentRegion = new BinaryRegion(width, height, currentRegionPixelValue);

                LinkedList pointsToSearch = new LinkedList();
                pointsToSearch.add(new Point(x, y));

                while (!pointsToSearch.isEmpty())
                {
                    Point point = (Point)pointsToSearch.removeFirst();

                    if ((point.x < visited.length) && (point.y < visited[0].length) && (point.x >= 0) && (point.y >= 0))
                    {
                        int pixelValue = pixels[point.x][point.y];

                        if ((visited[point.x][point.y] == false) && (pixelValue == currentRegionPixelValue))
                        {
                            visited[point.x][point.y] = true;

                            currentRegion.addPointToRegion(point.x, point.y);

                            pointsToSearch.add(new Point(point.x + 1, point.y));
                            pointsToSearch.add(new Point(point.x - 1, point.y));
                            pointsToSearch.add(new Point(point.x, point.y + 1));
                            pointsToSearch.add(new Point(point.x, point.y - 1));
                        }
                    }

                }

                if (currentRegionPixelValue < minRegionValue)
                    getRegions(RegionType.smaller).add(currentRegion);
                else if (currentRegionPixelValue > maxRegionValue)
                    getRegions(RegionType.higher).add(currentRegion);
                else
                    getRegions(RegionType.target).add(currentRegion);
            }
    }

    public void sortRegions()
    {
        for (Vector region : this.regionsList) {
            Collections.sort(region);
        }
        this.sorted = true;
    }

    public void boxPlot(double percentage) {
        boxPlot(percentage, percentage);
    }

    public void boxPlot(double leftPercentage, double rightPercentage) {
        for (Vector regions : this.regionsList)
        {
            int leftIndex = (int)Math.round(leftPercentage * regions.size());
            int rightIndex = (int)Math.round(rightPercentage * regions.size());

            for (int i = 0; i < leftIndex; i++) {
                regions.remove(0);
            }

            for (int i = 0; i < rightIndex; i++)
                regions.remove(regions.size() - 1);
        }
    }

    public void removeDuplicates()
    {
        if (!this.sorted) sortRegions();

        for (Vector regions : this.regionsList)
            for (int i = regions.size() - 1; i > 0; i--)
                if (((BinaryRegion)regions.get(i)).getPixelCount() == ((BinaryRegion)regions.get(i - 1)).getPixelCount())
                    regions.remove(i);
    }

    public static enum RegionType
    {
        smaller, target, higher;
    }
}