package evaluation;

import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Segments
{
    private int width;
    private int height;
    private Segment[][] segments;

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    private void initSegments(int width, int height)
    {
        this.width = width;
        this.height = height;

        this.segments = new Segment[width][height];
    }

    public Segments(int width, int height)
    {
        initSegments(width, height);
    }

    public Segments(String imageFileName)
    {
        try
        {
            BufferedImage image = ImageIO.read(new File(imageFileName));

            initSegments(image.getWidth(), image.getHeight());

            loadFromImage(image);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Segments(ImageProcessor imageProcessor)
    {
        initSegments(imageProcessor.getWidth(), imageProcessor.getHeight());

        loadFromImage(imageProcessor.getBufferedImage());
    }

    private void loadFromImage(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++)
            for (int y = 0; y < image.getHeight(); y++)
            {
                int color = image.getRGB(x, y);

                Segment s = null;
                if (color == Color.black.getRGB())
                    s = Segment.background;
                else if (color == Color.white.getRGB())
                    s = Segment.objectOfInterest;
                else {
                    s = Segment.focus;
                }
                addSegment(s, x, y);
            }
    }

    public void setSegment(int x, int y, Segment s)
    {
        this.segments[x][y] = s;
    }

    public Segment getSegment(int x, int y) {
        return this.segments[x][y];
    }

    public void addSegment(Segment s, int x, int y) {
        this.segments[x][y] = s;
    }
}