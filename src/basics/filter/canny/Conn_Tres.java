package basics.filter.canny;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.gui.TrimmedButton;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.plugin.PlugIn;
import ij.plugin.frame.PasteController;
import ij.plugin.frame.PlugInFrame;
import ij.plugin.frame.Recorder;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.PrintStream;

public class Conn_Tres extends PlugInFrame
        implements PlugIn, Measurements, Runnable, ActionListener, AdjustmentListener, ItemListener
{
    static final int RED = 0;
    static final int BLACK_AND_WHITE = 1;
    static final int OVER_UNDER = 2;
    static final String[] modes = { "Red", "Black & White", "Over/Under" };
    static final double defaultMinThreshold = 85.0D;
    static final double defaultMaxThreshold = 170.0D;
    static boolean fill1 = true;
    static boolean fill2 = true;
    static boolean useBW = true;
    static boolean backgroundToNaN = true;
    static Frame instance;
    static int mode = 0;
    thresholdplot plot = new thresholdplot();
    Thread thread;
    int minValue = -1;
    int maxValue = -1;
    int sliderRange = 256;
    boolean doAutoAdjust;
    boolean doReset;
    boolean doApplyLut;
    boolean doStateChange;
    boolean doSet;
    Panel panel;
    Button autoB;
    Button resetB;
    Button applyB;
    Button setB;
    int previousImageID;
    int previousImageType;
    double previousMin;
    double previousMax;
    ImageJ ij;
    double minThreshold;
    double maxThreshold;
    int minusHyst = 5;
    int plusHyst = 5;
    int minusHystValue;
    int plusHystValue;
    Scrollbar minSlider;
    Scrollbar maxSlider;
    Scrollbar minhystSlider;
    Scrollbar maxhystSlider;
    Label label1;
    Label label2;
    Label label3;
    Label label4;
    boolean done;
    boolean invertedLut;
    int lutColor;
    static Choice choice;
    boolean firstActivation;
    ImagePlus impOrig;
    ImageProcessor ipOrig;
    static final int RESET = 0;
    static final int AUTO = 1;
    static final int HIST = 2;
    static final int APPLY = 3;
    static final int STATE_CHANGE = 4;
    static final int MIN_THRESHOLD = 5;
    static final int MAX_THRESHOLD = 6;
    static final int SET = 7;
    static final int MINUS_HYST = 8;
    static final int PLUS_HYST = 9;

    public Conn_Tres()
    {
        super("Connection Thresholding");
        if (instance != null) {
            instance.toFront();
            return;
        }

        WindowManager.addWindow(this);
        instance = this;

        IJ.register(PasteController.class);

        this.ij = IJ.getInstance();
        Font font = new Font("SansSerif", 0, 10);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        int y = 0;
        c.gridx = 0;
        c.gridy = (y++);
        c.gridwidth = 2;
        c.fill = 1;
        c.anchor = 10;
        c.insets = new Insets(10, 10, 0, 10);
        add(this.plot, c);
        this.plot.addKeyListener(this.ij);

        this.minSlider = new Scrollbar(0, this.sliderRange / 3, 1, 0, this.sliderRange);
        c.gridx = 0;
        c.gridy = (y++);
        c.gridwidth = 1;
        c.weightx = (IJ.isMacintosh() ? 90.0D : 100.0D);
        c.fill = 2;
        c.insets = new Insets(5, 10, 0, 0);
        add(this.minSlider, c);
        this.minSlider.addAdjustmentListener(this);
        this.minSlider.addKeyListener(this.ij);
        this.minSlider.setUnitIncrement(1);

        c.gridx = 1;
        c.gridwidth = 1;
        c.weightx = (IJ.isMacintosh() ? 10.0D : 0.0D);
        c.insets = new Insets(5, 0, 0, 10);
        this.label1 = new Label("      ", 2);
        this.label1.setFont(font);
        add(this.label1, c);

        this.maxSlider = new Scrollbar(0, this.sliderRange * 2 / 3, 1, 0, this.sliderRange);
        c.gridx = 0;
        c.gridy = (y++);
        c.gridwidth = 1;
        c.weightx = 100.0D;
        c.insets = new Insets(0, 10, 0, 0);
        add(this.maxSlider, c);
        this.maxSlider.addAdjustmentListener(this);
        this.maxSlider.addKeyListener(this.ij);
        this.maxSlider.setUnitIncrement(1);

        c.gridx = 1;
        c.gridwidth = 1;
        c.weightx = 0.0D;
        c.insets = new Insets(0, 0, 0, 10);
        this.label2 = new Label("      ", 2);
        this.label2.setFont(font);
        add(this.label2, c);

        this.minhystSlider = new Scrollbar(0, 5, 1, 0, this.sliderRange);
        c.gridx = 0;
        c.gridy = (y++);
        c.gridwidth = 1;
        c.weightx = (IJ.isMacintosh() ? 90.0D : 100.0D);
        c.fill = 2;
        c.insets = new Insets(0, 10, 0, 0);
        add(this.minhystSlider, c);
        this.minhystSlider.addAdjustmentListener(this);
        this.minhystSlider.addKeyListener(this.ij);
        this.minhystSlider.setUnitIncrement(1);

        c.gridx = 1;
        c.gridwidth = 1;
        c.weightx = (IJ.isMacintosh() ? 10.0D : 0.0D);
        c.insets = new Insets(0, 0, 0, 10);
        this.label3 = new Label("      ", 2);
        this.label3.setFont(font);
        add(this.label3, c);

        this.maxhystSlider = new Scrollbar(0, 5, 1, 0, this.sliderRange);
        c.gridx = 0;
        c.gridy = (y++);
        c.gridwidth = 1;
        c.weightx = (IJ.isMacintosh() ? 90.0D : 100.0D);
        c.fill = 2;
        c.insets = new Insets(0, 10, 0, 0);
        add(this.maxhystSlider, c);
        this.maxhystSlider.addAdjustmentListener(this);
        this.maxhystSlider.addKeyListener(this.ij);
        this.maxhystSlider.setUnitIncrement(1);

        c.gridx = 1;
        c.gridwidth = 1;
        c.weightx = (IJ.isMacintosh() ? 10.0D : 0.0D);
        c.insets = new Insets(0, 0, 0, 10);
        this.label4 = new Label("      ", 2);
        this.label4.setFont(font);
        add(this.label4, c);

        int trim = IJ.isMacOSX() ? 11 : 0;
        this.panel = new Panel();
        this.autoB = new TrimmedButton("Auto", trim);
        this.autoB.addActionListener(this);
        this.autoB.addKeyListener(this.ij);
        this.panel.add(this.autoB);
        this.applyB = new TrimmedButton("Multi", trim);
        this.applyB.addActionListener(this);
        this.applyB.addKeyListener(this.ij);
        this.panel.add(this.applyB);
        this.resetB = new TrimmedButton("Hyst", trim);
        this.resetB.addActionListener(this);
        this.resetB.addKeyListener(this.ij);
        this.panel.add(this.resetB);
        this.setB = new TrimmedButton("Set", trim);
        this.setB.addActionListener(this);
        this.setB.addKeyListener(this.ij);
        this.panel.add(this.setB);
        c.gridx = 0;
        c.gridy = (y++);
        c.gridwidth = 2;
        c.insets = new Insets(0, 5, 10, 5);
        add(this.panel, c);

        addKeyListener(this.ij);

        pack();
        GUI.center(this);
        this.firstActivation = true;
        if (IJ.isMacOSX()) {
            setResizable(false);
        }
        setVisible(true);

        this.thread = new Thread(this, "Hysteresis_");

        this.thread.start();
        this.impOrig = WindowManager.getCurrentImage();
        if (this.impOrig != null)
            this.ipOrig = setup(this.impOrig);
    }

    public synchronized void adjustmentValueChanged(AdjustmentEvent e)
    {
        if (e.getSource() == this.minSlider)
            this.minValue = this.minSlider.getValue();
        else if (e.getSource() == this.maxSlider)
            this.maxValue = this.maxSlider.getValue();
        else if (e.getSource() == this.minhystSlider)
            this.minusHystValue = this.minhystSlider.getValue();
        else {
            this.plusHystValue = this.maxhystSlider.getValue();
        }
        notify();
    }

    public synchronized void actionPerformed(ActionEvent e)
    {
        Button b = (Button)e.getSource();
        if (b == null) {
            return;
        }
        if (b == this.resetB)
            this.doReset = true;
        else if (b == this.autoB)
            this.doAutoAdjust = true;
        else if (b == this.applyB)
            this.doApplyLut = true;
        else if (b == this.setB) {
            this.doSet = true;
        }
        notify();
    }

    public synchronized void itemStateChanged(ItemEvent e)
    {
        mode = choice.getSelectedIndex();

        this.doStateChange = true;
        notify();
    }

    ImageProcessor setup(ImagePlus imp)
    {
        int type = imp.getType();
        if (type == 4) {
            return null;
        }
        ImageProcessor ip = imp.getProcessor();
        boolean minMaxChange = false;
        boolean not8Bits = (type == 1) || (type == 2);
        if (not8Bits) {
            if ((ip.getMin() == this.plot.stackMin) && (ip.getMax() == this.plot.stackMax)) {
                minMaxChange = false;
            } else if ((ip.getMin() != this.previousMin) || (ip.getMax() != this.previousMax)) {
                minMaxChange = true;
                this.previousMin = ip.getMin();
                this.previousMax = ip.getMax();
            }
        }
        int id = imp.getID();
        if ((minMaxChange) || (id != this.previousImageID) || (type != this.previousImageType))
        {
            if ((not8Bits) && (minMaxChange)) {
                ip.resetMinAndMax();
                imp.updateAndDraw();
            }
            this.invertedLut = imp.isInvertedLut();
            this.minThreshold = ip.getMinThreshold();
            this.maxThreshold = ip.getMaxThreshold();
            ImageStatistics stats = this.plot.setHistogram(imp, false);
            if (this.minThreshold == -808080.0D) {
                autoSetLevels(ip, stats);
            } else {
                this.minThreshold = scaleDown(ip, this.minThreshold);
                this.maxThreshold = scaleDown(ip, this.maxThreshold);
            }
            scaleUpAndSet(ip, this.minThreshold, this.maxThreshold, this.minusHyst, this.plusHyst);
            updateLabels(imp, ip);
            updatePlot();
            updateScrollBars();
            imp.updateAndDraw();
        }
        this.previousImageID = id;
        this.previousImageType = type;
        return ip;
    }

    void autoSetLevels(ImageProcessor ip, ImageStatistics stats)
    {
        if ((stats == null) || (stats.histogram == null)) {
            this.minThreshold = 85.0D;
            this.maxThreshold = 170.0D;
            return;
        }
        int threshold = ip.getAutoThreshold(stats.histogram);

        int count1 = 0;

        int count2 = 0;
        for (int i = 0; i < 256; i++) {
            if (i < threshold)
                count1 += stats.histogram[i];
            else {
                count2 += stats.histogram[i];
            }
        }
        boolean unbalanced = (count1 / count2 > 1.25D) || (count2 / count1 > 1.25D);

        if (unbalanced) {
            if (stats.max - stats.dmode > stats.dmode - stats.min) {
                this.minThreshold = threshold;
                this.maxThreshold = stats.max;
            } else {
                this.minThreshold = stats.min;
                this.maxThreshold = threshold;
            }
        }
        else if (ip.isInvertedLut()) {
            this.minThreshold = threshold;
            this.maxThreshold = 255.0D;
        } else {
            this.minThreshold = 0.0D;
            this.maxThreshold = threshold;
        }

        if (Recorder.record)
            Recorder.record("setAutoThreshold");
    }

    void scaleUpAndSet(ImageProcessor ip, double minThreshold, double maxThreshold, int minus, int plus)
    {
        double min = scaleUp(ip, minThreshold);
        double max = scaleUp(ip, maxThreshold);

        byte[] rLUT2 = new byte[256];
        byte[] gLUT2 = new byte[256];
        byte[] bLUT2 = new byte[256];

        for (int i = 0; i < 256; i++)
        {
            double ii = scaleUp(ip, i);
            if ((ii >= min) && (ii <= max)) {
                rLUT2[i] = -1;
                gLUT2[i] = 0;
                bLUT2[i] = 0;
            }
            else if ((ii >= min - minus) && (ii < min)) {
                rLUT2[i] = 0;
                gLUT2[i] = 0;
                bLUT2[i] = -1;
            }
            else if ((ii > max) && (ii <= max + plus)) {
                rLUT2[i] = 0;
                gLUT2[i] = -1;
                bLUT2[i] = 0;
            }
            else
            {
                rLUT2[i] = 0;
                gLUT2[i] = 0;
                bLUT2[i] = 0;
            }
        }

        ColorModel cm = new IndexColorModel(8, 256, rLUT2, gLUT2, bLUT2);
        ip.setColorModel(cm);
    }

    double scaleDown(ImageProcessor ip, double threshold)
    {
        if ((ip instanceof ByteProcessor)) {
            return threshold;
        }
        double min = ip.getMin();
        double max = ip.getMax();
        if (max > min) {
            return (threshold - min) / (max - min) * 255.0D;
        }
        return -808080.0D;
    }

    double scaleUp(ImageProcessor ip, double threshold)
    {
        double min = ip.getMin();
        double max = ip.getMax();
        if (max > min) {
            return min + threshold / 255.0D * (max - min);
        }
        return -808080.0D;
    }

    void updatePlot()
    {
        this.plot.minThreshold = this.minThreshold;
        this.plot.maxThreshold = this.maxThreshold;
        this.plot.mode = mode;
        this.plot.repaint();
    }

    void updateLabels(ImagePlus imp, ImageProcessor ip)
    {
        double min = scaleUp(imp.getProcessor(), this.minThreshold);
        double max = scaleUp(imp.getProcessor(), this.maxThreshold);

        this.label1.setText("" + IJ.d2s(min, 2));
        this.label2.setText("" + IJ.d2s(max, 2));
        this.label3.setText("" + this.minusHyst);
        this.label4.setText("" + this.plusHyst);
    }

    void updateScrollBars()
    {
        this.minSlider.setValue((int)this.minThreshold);
        this.maxSlider.setValue((int)this.maxThreshold);
        this.minhystSlider.setValue(this.minusHyst);
        this.maxhystSlider.setValue(this.plusHyst);
    }

    void doMasking(ImagePlus imp, ImageProcessor ip)
    {
        ImageProcessor mask = imp.getMask();
        if (mask != null)
            ip.reset(mask);
    }

    void adjustMinThreshold(ImagePlus imp, ImageProcessor ip, double value)
    {
        if ((IJ.altKeyDown()) || (IJ.shiftKeyDown())) {
            double width = this.maxThreshold - this.minThreshold;
            if (width < 1.0D) {
                width = 1.0D;
            }
            this.minThreshold = value;
            this.maxThreshold = (this.minThreshold + width);
            if (this.minThreshold + width > 255.0D) {
                this.minThreshold = (255.0D - width);
                this.maxThreshold = (this.minThreshold + width);
                this.minSlider.setValue((int)this.minThreshold);
            }
            this.maxSlider.setValue((int)this.maxThreshold);
            scaleUpAndSet(ip, this.minThreshold, this.maxThreshold, this.minusHyst, this.plusHyst);
            return;
        }
        this.minThreshold = value;
        if (this.maxThreshold < this.minThreshold) {
            this.maxThreshold = this.minThreshold;
            this.maxSlider.setValue((int)this.maxThreshold);
        }
        double min = ip.getMin();
        double mint = scaleUp(ip, this.minThreshold);
        if (mint - this.minusHyst < min) {
            this.minusHyst = (int)(mint - min);
            this.minhystSlider.setValue(this.minusHyst);
        }
        scaleUpAndSet(ip, this.minThreshold, this.maxThreshold, this.minusHyst, this.plusHyst);
    }

    void adjustMinusHyst(ImagePlus imp, ImageProcessor ip, double value)
    {
        this.minusHyst = (int)value;
        double min = ip.getMin();
        double mint = scaleUp(ip, this.minThreshold);
        if (mint - this.minusHyst < min) {
            this.minusHyst = (int)(mint - min);
            this.minhystSlider.setValue(this.minusHyst);
        }

        scaleUpAndSet(ip, this.minThreshold, this.maxThreshold, this.minusHyst, this.plusHyst);
    }

    void adjustPlusHyst(ImagePlus imp, ImageProcessor ip, double value)
    {
        this.plusHyst = (int)value;
        double max = ip.getMax();
        double maxt = scaleUp(ip, this.maxThreshold);
        if (maxt + this.plusHyst > max) {
            this.plusHyst = (int)(max - maxt);
            this.maxhystSlider.setValue(this.plusHyst);
        }

        scaleUpAndSet(ip, this.minThreshold, this.maxThreshold, this.minusHyst, this.plusHyst);
    }

    void adjustMaxThreshold(ImagePlus imp, ImageProcessor ip, int cvalue)
    {
        this.maxThreshold = cvalue;
        if (this.minThreshold > this.maxThreshold) {
            this.minThreshold = this.maxThreshold;
            this.minSlider.setValue((int)this.minThreshold);
        }
        double max = ip.getMax();
        double maxt = scaleUp(ip, this.maxThreshold);
        if (maxt + this.plusHyst > max) {
            this.plusHyst = (int)(max - maxt);
            this.maxhystSlider.setValue(this.plusHyst);
        }
        scaleUpAndSet(ip, this.minThreshold, this.maxThreshold, this.minusHyst, this.plusHyst);
        IJ.setKeyUp(18);
        IJ.setKeyUp(16);
    }

    void doSet(ImagePlus imp, ImageProcessor ip)
    {
        double level1 = ip.getMinThreshold();
        double level2 = ip.getMaxThreshold();
        if (level1 == -808080.0D) {
            level1 = scaleUp(ip, 85.0D);
            level2 = scaleUp(ip, 170.0D);
        }
        Calibration cal = imp.getCalibration();
        int digits = ((ip instanceof FloatProcessor)) || (cal.calibrated()) ? 2 : 0;
        level1 = cal.getCValue(level1);
        level2 = cal.getCValue(level2);
        GenericDialog gd = new GenericDialog("Set Threshold Levels");
        gd.addNumericField("Lower Threshold Level: ", level1, digits);
        gd.addNumericField("Upper Threshold Level: ", level2, digits);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        level1 = gd.getNextNumber();
        level2 = gd.getNextNumber();
        level1 = cal.getRawValue(level1);
        level2 = cal.getRawValue(level2);
        if (level2 < level1) {
            level2 = level1;
        }
        double minDisplay = ip.getMin();
        double maxDisplay = ip.getMax();
        ip.resetMinAndMax();
        double minValue = ip.getMin();
        double maxValue = ip.getMax();
        if (level1 < minValue) {
            level1 = minValue;
        }
        if (level2 > maxValue) {
            level2 = maxValue;
        }
        boolean outOfRange = (level1 < minDisplay) || (level2 > maxDisplay);
        if (outOfRange)
            this.plot.setHistogram(imp, false);
        else {
            ip.setMinAndMax(minDisplay, maxDisplay);
        }

        this.minThreshold = scaleDown(ip, level1);
        this.maxThreshold = scaleDown(ip, level2);
        scaleUpAndSet(ip, this.minThreshold, this.maxThreshold, this.minusHyst, this.plusHyst);
        updateScrollBars();
        if (Recorder.record)
            if (imp.getBitDepth() == 32) {
                Recorder.record("setThreshold", ip.getMinThreshold(), ip.getMaxThreshold());
            } else {
                int min = (int)ip.getMinThreshold();
                int max = (int)ip.getMaxThreshold();
                if (cal.isSigned16Bit()) {
                    min = (int)cal.getCValue(level1);
                    max = (int)cal.getCValue(level2);
                }
                Recorder.record("setThreshold", min, max);
            }
    }

    void changeState(ImagePlus imp, ImageProcessor ip)
    {
        scaleUpAndSet(ip, this.minThreshold, this.maxThreshold, this.minusHyst, this.plusHyst);
        updateScrollBars();
    }

    void autoThreshold(ImagePlus imp, ImageProcessor ip)
    {
        ip.resetThreshold();
        this.previousImageID = 0;
        setup(imp);
    }

    public ImagePlus doTrin(ImagePlus imaplus, double T1, double T2, int minhyst, int plushyst)
    {
        int ns = imaplus.getStack().getSize();
        if (ns > 1) {
            ImageStack st = imaplus.getStack();
            ImageStack res = new ImageStack(st.getWidth(), st.getHeight());
            for (int s = 1; s <= ns; s++) {
                res.addSlice("", trin(st.getProcessor(s), T1, T2, minhyst, plushyst));
            }
            return new ImagePlus("Multi_Thresholding", res);
        }
        return new ImagePlus("Multi_Thresholding", trin(imaplus.getProcessor(), T1, T2, minhyst, plushyst));
    }

    public ImagePlus doHyst(ImagePlus imaplus, double T1, double T2, int minhyst, int plushyst)
    {
        int ns = imaplus.getStack().getSize();
        if (ns > 1) {
            ImageStack st = imaplus.getStack();
            ImageStack res = new ImageStack(st.getWidth(), st.getHeight());
            for (int s = 1; s <= ns; s++) {
                res.addSlice("", hyst(st.getProcessor(s), T1, T2, minhyst, plushyst));
            }
            return new ImagePlus("Hysteresis_Thresholding", res);
        }
        return new ImagePlus("Hysteresis_Thresholding", hyst(imaplus.getProcessor(), T1, T2, minhyst, plushyst));
    }

    private ByteProcessor trin(ImageProcessor ima, double T1, double T2, int minhyst, int plushyst)
    {
        int la = ima.getWidth();
        int ha = ima.getHeight();
        ByteProcessor res = new ByteProcessor(la, ha);

        double min = scaleUp(ima, this.minThreshold);
        double max = scaleUp(ima, this.maxThreshold);

        for (int x = 0; x < la; x++) {
            for (int y = 0; y < ha; y++) {
                float pix = ima.getPixelValue(x, y);
                if ((pix >= min) && (pix <= max))
                    res.putPixelValue(x, y, 255.0D);
                else if ((pix >= min - minhyst) && (pix < min))
                    res.putPixelValue(x, y, 100.0D);
                else if ((pix <= max + plushyst) && (pix > max)) {
                    res.putPixelValue(x, y, 200.0D);
                }
            }
        }
        return res;
    }

    private ByteProcessor hyst(ImageProcessor ima, double T1, double T2, int minhyst, int plushyst)
    {
        int la = ima.getWidth();
        int ha = ima.getHeight();

        boolean change = true;

        ByteProcessor trin = new ByteProcessor(la, ha);
        double min = scaleUp(ima, this.minThreshold);
        double max = scaleUp(ima, this.maxThreshold);

        for (int x = 0; x < la; x++) {
            for (int y = 0; y < ha; y++) {
                float pix = ima.getPixelValue(x, y);
                if ((pix >= min) && (pix <= max))
                    trin.putPixelValue(x, y, 255.0D);
                else if (((pix >= min - minhyst) && (pix < min)) || ((pix <= max + plushyst) && (pix > max))) {
                    trin.putPixelValue(x, y, 128.0D);
                }
            }
        }

        ByteProcessor res = (ByteProcessor)trin.duplicate();

        while (change) {
            change = false;
            for (int x = 1; x < la - 1; x++) {
                for (int y = 1; y < ha - 1; y++) {
                    if (res.getPixelValue(x, y) == 255.0F) {
                        if (res.getPixelValue(x + 1, y) == 128.0F) {
                            change = true;
                            res.putPixelValue(x + 1, y, 255.0D);
                        }
                        if (res.getPixelValue(x - 1, y) == 128.0F) {
                            change = true;
                            res.putPixelValue(x - 1, y, 255.0D);
                        }
                        if (res.getPixelValue(x, y + 1) == 128.0F) {
                            change = true;
                            res.putPixelValue(x, y + 1, 255.0D);
                        }
                        if (res.getPixelValue(x, y - 1) == 128.0F) {
                            change = true;
                            res.putPixelValue(x, y - 1, 255.0D);
                        }
                        if (res.getPixelValue(x + 1, y + 1) == 128.0F) {
                            change = true;
                            res.putPixelValue(x + 1, y + 1, 255.0D);
                        }
                        if (res.getPixelValue(x - 1, y - 1) == 128.0F) {
                            change = true;
                            res.putPixelValue(x - 1, y - 1, 255.0D);
                        }
                        if (res.getPixelValue(x - 1, y + 1) == 128.0F) {
                            change = true;
                            res.putPixelValue(x - 1, y + 1, 255.0D);
                        }
                        if (res.getPixelValue(x + 1, y - 1) == 128.0F) {
                            change = true;
                            res.putPixelValue(x + 1, y - 1, 255.0D);
                        }
                    }
                }
            }
            if (change) {
                for (int x = la - 2; x > 0; x--) {
                    for (int y = ha - 2; y > 0; y--) {
                        if (res.getPixelValue(x, y) == 255.0F) {
                            if (res.getPixelValue(x + 1, y) == 128.0F) {
                                change = true;
                                res.putPixelValue(x + 1, y, 255.0D);
                            }
                            if (res.getPixelValue(x - 1, y) == 128.0F) {
                                change = true;
                                res.putPixelValue(x - 1, y, 255.0D);
                            }
                            if (res.getPixelValue(x, y + 1) == 128.0F) {
                                change = true;
                                res.putPixelValue(x, y + 1, 255.0D);
                            }
                            if (res.getPixelValue(x, y - 1) == 128.0F) {
                                change = true;
                                res.putPixelValue(x, y - 1, 255.0D);
                            }
                            if (res.getPixelValue(x + 1, y + 1) == 128.0F) {
                                change = true;
                                res.putPixelValue(x + 1, y + 1, 255.0D);
                            }
                            if (res.getPixelValue(x - 1, y - 1) == 128.0F) {
                                change = true;
                                res.putPixelValue(x - 1, y - 1, 255.0D);
                            }
                            if (res.getPixelValue(x - 1, y + 1) == 128.0F) {
                                change = true;
                                res.putPixelValue(x - 1, y + 1, 255.0D);
                            }
                            if (res.getPixelValue(x + 1, y - 1) == 128.0F) {
                                change = true;
                                res.putPixelValue(x + 1, y - 1, 255.0D);
                            }
                        }
                    }
                }
            }
        }

        for (int x = 0; x < la; x++) {
            for (int y = 0; y < ha; y++) {
                if (res.getPixelValue(x, y) == 128.0F) {
                    res.putPixelValue(x, y, 0.0D);
                }
            }
        }
        return res;
    }

    void runThresholdCommand()
    {
        Recorder.recordInMacros = true;
        IJ.run("Convert to Mask");
        Recorder.recordInMacros = false;
    }

    public void run()
    {
        while (!this.done) {
            synchronized (this) {
                try {
                    wait(); } catch (InterruptedException e) {
                }
            }
            doUpdate();
        }
    }

    void doUpdate()
    {
        int min = this.minValue;
        int max = this.maxValue;
        int minus = this.minusHystValue;
        int plus = this.plusHystValue;
        System.out.println("" + this.minusHystValue + " " + this.plusHystValue);
        int action;
        if (this.doReset) {
            action = 0;
        }
        else
        {
            if (this.doAutoAdjust) {
                action = 1;
            }
            else
            {
                if (this.doApplyLut) {
                    action = 3;
                }
                else
                {
                    if (this.doStateChange) {
                        action = 4;
                    }
                    else
                    {
                        if (this.doSet) {
                            action = 7;
                        }
                        else
                        {
                            if (this.minValue >= 0) {
                                action = 5;
                            }
                            else
                            {
                                if (this.maxValue >= 0) {
                                    action = 6;
                                }
                                else
                                {
                                    if (this.minusHystValue >= 0) {
                                        action = 8;
                                    }
                                    else
                                    {
                                        if (this.plusHystValue >= 0)
                                            action = 9;
                                        else
                                            return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        this.minValue = -1;
        this.maxValue = -1;
        this.minusHystValue = -1;
        this.plusHystValue = -1;
        this.doReset = false;
        this.doAutoAdjust = false;
        this.doApplyLut = false;
        this.doStateChange = false;
        this.doSet = false;

        switch (action)
        {
            case 0:
                doHyst(this.impOrig, this.minThreshold, this.maxThreshold, this.minusHyst, this.plusHyst).show();
                break;
            case 1:
                autoThreshold(this.impOrig, this.ipOrig);
                break;
            case 3:
                doTrin(this.impOrig, this.minThreshold, this.maxThreshold, this.minusHyst, this.plusHyst).show();
                break;
            case 4:
                changeState(this.impOrig, this.ipOrig);
                break;
            case 7:
                doSet(this.impOrig, this.ipOrig);
                break;
            case 5:
                adjustMinThreshold(this.impOrig, this.ipOrig, min);
                break;
            case 6:
                adjustMaxThreshold(this.impOrig, this.ipOrig, max);
                break;
            case 8:
                adjustMinusHyst(this.impOrig, this.ipOrig, minus);
                break;
            case 9:
                adjustPlusHyst(this.impOrig, this.ipOrig, plus);
            case 2:
        }
        updatePlot();
        updateLabels(this.impOrig, this.ipOrig);
        this.ipOrig.setLutAnimation(true);
        this.impOrig.updateAndDraw();
    }

    public void windowClosing(WindowEvent e)
    {
        close();
    }

    public void close()
    {
        super.close();
        instance = null;
        this.done = true;
        synchronized (this) {
            notify();
        }
    }

    public void windowActivated(WindowEvent e)
    {
        super.windowActivated(e);

        if (this.impOrig != null) {
            if (!this.firstActivation) {
                this.previousImageID = 0;
                setup(this.impOrig);
            }
            this.firstActivation = false;
        }
    }
}