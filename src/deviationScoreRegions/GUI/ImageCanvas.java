package deviationScoreRegions.GUI;

import basics.Tools;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Canvas;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.ImageIcon;

public class ImageCanvas extends Canvas
        implements DebugInfoShower
{
    private ImageProcessor imageProcessor = null;
    private String text = "";
    private boolean drawBorder = true;

    public ImageCanvas() {
        isDoubleBuffered();
    }

    public void setText(String text) {
        this.text = text;
    }

    public static BufferedImage toBufferedImage(Image image) {
        if ((image instanceof BufferedImage)) {
            return (BufferedImage)image;
        }

        image = new ImageIcon(image).getImage();

        boolean hasAlpha = false;

        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try
        {
            int transparency = 1;
            if (hasAlpha) {
                transparency = 2;
            }

            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
        }
        catch (HeadlessException e)
        {
        }

        if (bimage == null)
        {
            int type = 1;
            if (hasAlpha) {
                type = 2;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }

        Graphics g = bimage.createGraphics();

        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }

    public void paint(Graphics g)
    {
        FontMetrics fm = g.getFontMetrics();
        int textWidth = (int)fm.getStringBounds(this.text, g).getWidth();
        int textHeight = (int)fm.getStringBounds(this.text, g).getHeight();

        int textX = getWidth() / 2 - textWidth / 2;
        int textY = getHeight() / 2 - textHeight / 2;

        if (this.text != "") g.drawString(this.text, textX, textY);
        if (this.imageProcessor != null) {
            int imageWidth = this.imageProcessor.getWidth();
            int imageHeight = this.imageProcessor.getHeight();

            double fMin = Math.min(getWidth() / imageWidth, getHeight() / imageHeight);
            int w = (int)(imageWidth * fMin);
            int h = (int)(imageHeight * fMin);
            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;
            g.drawImage(this.imageProcessor.getBufferedImage(), x, y, w, h, this);
        }

        if (this.drawBorder)
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

    public void setImageProcessor(ImageProcessor imageProcessor)
    {
        this.text = "";
        this.imageProcessor = imageProcessor;
        repaint();
    }

    public ImageProcessor getImageProcessor() {
        return this.imageProcessor;
    }

    public void setImage(Image i) {
        ImageProcessor imgProc = new ColorProcessor(toBufferedImage(i));
        setImageProcessor(imgProc);
    }

    public void setImageFileName(String fileName) throws IOException {
        this.imageProcessor = Tools.loadImageProcessor(fileName);
        repaint();
    }

    public void showImage(ImageProcessor i)
    {
        setImageProcessor(i);
    }

    public void showText(String text)
    {
    }
}