package basics.filter.canny;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.plugin.filter.Analyzer;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.StackStatistics;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

class thresholdplot extends Canvas
        implements Measurements, MouseListener
{
    static final int WIDTH = 256;
    static final int HEIGHT = 48;
    double minThreshold = 85.0D;
    double maxThreshold = 170.0D;
    int[] histogram;
    Color[] hColors;
    int hmax;
    Image os;
    Graphics osg;
    int mode;
    double stackMin;
    double stackMax;

    public thresholdplot()
    {
        addMouseListener(this);
        setSize(257, 49);
    }

    public Dimension getPreferredSize()
    {
        return new Dimension(257, 49);
    }

    ImageStatistics setHistogram(ImagePlus imp, boolean useStackMinAndMax)
    {
        ImageProcessor ip = imp.getProcessor();
        ImageStatistics stats = null;
        if (!(ip instanceof ByteProcessor)) {
            if (useStackMinAndMax) {
                stats = new StackStatistics(imp);
                if (imp.getLocalCalibration().isSigned16Bit()) {
                    stats.min += 32768.0D;
                    stats.max += 32768.0D;
                }
                this.stackMin = stats.min;
                this.stackMax = stats.max;
                ip.setMinAndMax(this.stackMin, this.stackMax);
            } else {
                this.stackMin = (this.stackMax = 0.0D);
            }
            Calibration cal = imp.getCalibration();
            if ((ip instanceof FloatProcessor)) {
                int digits = Math.max(Analyzer.getPrecision(), 2);
                IJ.showStatus("min=" + IJ.d2s(ip.getMin(), digits) + ", max=" + IJ.d2s(ip.getMax(), digits));
            } else {
                IJ.showStatus("min=" + (int)cal.getCValue(ip.getMin()) + ", max=" + (int)cal.getCValue(ip.getMax()));
            }
            ip = ip.convertToByte(true);
            ip.setColorModel(ip.getCurrentColorModel());
        }
        ip.setRoi(imp.getRoi());
        if (stats == null) {
            stats = ImageStatistics.getStatistics(ip, 25, null);
        }
        int maxCount2 = 0;
        this.histogram = stats.histogram;
        for (int i = 0; i < stats.nBins; i++) {
            if ((this.histogram[i] > maxCount2) && (i != stats.mode)) {
                maxCount2 = this.histogram[i];
            }
        }
        this.hmax = stats.maxCount;
        if ((this.hmax > maxCount2 * 2) && (maxCount2 != 0)) {
            this.hmax = (int)(maxCount2 * 1.5D);
            this.histogram[stats.mode] = this.hmax;
        }
        this.os = null;

        ColorModel cm = ip.getColorModel();
        if (!(cm instanceof IndexColorModel)) {
            return null;
        }
        IndexColorModel icm = (IndexColorModel)cm;
        int mapSize = icm.getMapSize();
        if (mapSize != 256) {
            return null;
        }
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];
        icm.getReds(r);
        icm.getGreens(g);
        icm.getBlues(b);
        this.hColors = new Color[256];
        for (int i = 0; i < 256; i++) {
            this.hColors[i] = new Color(r[i] & 0xFF, g[i] & 0xFF, b[i] & 0xFF);
        }
        return stats;
    }

    public void update(Graphics g)
    {
        paint(g);
    }

    public void paint(Graphics g)
    {
        if (g == null) {
            return;
        }
        if (this.histogram != null) {
            if ((this.os == null) && (this.hmax > 0)) {
                this.os = createImage(256, 48);
                this.osg = this.os.getGraphics();
                this.osg.setColor(Color.white);
                this.osg.fillRect(0, 0, 256, 48);
                this.osg.setColor(Color.gray);
                for (int i = 0; i < 256; i++) {
                    if (this.hColors != null) {
                        this.osg.setColor(this.hColors[i]);
                    }
                    this.osg.drawLine(i, 48, i, 48 - 48 * this.histogram[i] / this.hmax);
                }
                this.osg.dispose();
            }
            if (this.os == null) {
                return;
            }
            g.drawImage(this.os, 0, 0, this);
        } else {
            g.setColor(Color.white);
            g.fillRect(0, 0, 256, 48);
        }
        g.setColor(Color.black);
        g.drawRect(0, 0, 256, 48);
        if (this.mode == 0) {
            g.setColor(Color.red);
        } else if (this.mode == 2) {
            g.setColor(Color.blue);
            g.drawRect(1, 1, (int)this.minThreshold - 2, 48);
            g.drawRect(1, 0, (int)this.minThreshold - 2, 0);
            g.setColor(Color.green);
            g.drawRect((int)this.maxThreshold + 1, 1, 256 - (int)this.maxThreshold, 48);
            g.drawRect((int)this.maxThreshold + 1, 0, 256 - (int)this.maxThreshold, 0);
            return;
        }
        g.drawRect((int)this.minThreshold, 1, (int)(this.maxThreshold - this.minThreshold), 48);
        g.drawLine((int)this.minThreshold, 0, (int)this.maxThreshold, 0);
    }

    public void mousePressed(MouseEvent e)
    {
    }

    public void mouseReleased(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mouseEntered(MouseEvent e)
    {
    }
}