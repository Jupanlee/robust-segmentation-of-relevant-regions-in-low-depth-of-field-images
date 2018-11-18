//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics.filter.canny;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.PlugInFrame;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.image.ImageProducer;
import java.net.URL;

public class Frap_Norm implements PlugIn {
    private ImagePlus img;
    private ImagePlus prebleach;
    private ImagePlus postbleach;
    private ImageStack stack;
    private ImageWindow current;
    private ImageProcessor ippre;
    private ImageProcessor ippost;
    private int preref = 1;
    private int postref = 2;
    private Frap_Norm.Region frap;
    private Frap_Norm.Region ref;
    private Frap_Norm.Region base;
    private Frap_Norm.Region whole;
    private float alphaD = 0.5F;
    private float upperTreshold = 50.0F;
    private float lowerTreshold = 100.0F;
    private double radius = 5.0D;
    private int nSlices = 1;
    private ResultsTable rtable = new ResultsTable();
    private ResultsTable normtable = new ResultsTable();
    private boolean doublenorm = true;
    private boolean autocalc = false;
    private boolean image = false;
    private int[] time;
    private int interval = 1;
    float wholepre;
    float frappre;

    public Frap_Norm() {
    }

    public void run(String arg) {
        if (IJ.versionLessThan("1.36")) {
            IJ.error("Version 1.36 or higher required.\nPlease update your ImageJ.");
        }

        if (arg.equals("about")) {
            this.showAbout();
        } else if (arg.equals("Pic")) {
            this.showPic();
        } else {
            this.image = this.getNewImage();
            this.frap = new Frap_Norm.Region("FRAP region", this.nSlices);
            this.ref = new Frap_Norm.Region("Reference", this.nSlices);
            this.base = new Frap_Norm.Region("Background", this.nSlices);
            this.whole = new Frap_Norm.Region("Whole cell", this.nSlices);
            Frap_Norm.Display display = new Frap_Norm.Display();
            GUI.center(display);
            display.setVisible(true);
            if (this.image) {
                this.displayGuidance(this.img);
            }

        }
    }

    private boolean getNewImage() {
        this.img = WindowManager.getCurrentImage();
        this.current = WindowManager.getCurrentWindow();
        if (this.img == null) {
            IJ.showMessage("No images open.");
            return false;
        } else {
            this.nSlices = this.img.getStackSize();
            if (this.nSlices < 2) {
                IJ.error("Stack required");
                return false;
            } else {
                this.time = new int[this.nSlices];
                return true;
            }
        }
    }

    private void displayGuidance(ImagePlus img) {
        this.stack = img.getStack();
        this.ippre = CannyEdgeDetection.run(this.stack.getProcessor(this.preref), this.radius, this.alphaD, this.upperTreshold, this.lowerTreshold);
        this.prebleach = new ImagePlus("Pre Bleach", this.ippre);
        this.prebleach.show();
        this.ippost = CannyEdgeDetection.run(this.stack.getProcessor(this.postref), this.radius, this.alphaD, this.upperTreshold, this.lowerTreshold);
        this.postbleach = new ImagePlus("Post Bleach", this.ippost);
        this.postbleach.show();
    }

    private void Settings() {
        GenericDialog gd = new GenericDialog("Parameters");
        gd.addNumericField("median filter radius", this.radius, 2);
        gd.addNumericField("Deriche alpha value", (double)this.alphaD, 2);
        gd.addNumericField("Hysteresis High threshold", (double)this.upperTreshold, 2);
        gd.addNumericField("Hysteresis Low threshold", (double)this.lowerTreshold, 2);
        gd.addNumericField("Pre Bleaching slice", (double)this.preref, 0);
        gd.addNumericField("Post Bleaching slice", (double)this.postref, 0);
        gd.addNumericField("Time units between slides", (double)this.interval, 0);
        gd.showDialog();
        this.radius = gd.getNextNumber();
        this.alphaD = (float)gd.getNextNumber();
        this.upperTreshold = (float)gd.getNextNumber();
        this.lowerTreshold = (float)gd.getNextNumber();
        this.preref = (int)gd.getNextNumber();
        this.postref = (int)gd.getNextNumber();
        this.interval = (int)gd.getNextNumber();
        this.Reset();
    }

    public void displayResults(String label, int slice, double area, double mean) {
        this.rtable.incrementCounter();
        this.rtable.addLabel("Region", label);
        this.rtable.addValue("Slice", (double)slice);
        this.rtable.addValue("Area", area);
        this.rtable.addValue("Intensity", mean);
        this.rtable.show("Measurements");
    }

    public void Normalize() {
        float[] frapnorm = new float[this.nSlices];
        this.frappre = 0.0F;
        this.wholepre = 0.0F;
        this.normtable.reset();

        int i;
        for(i = 0; i < this.nSlices; ++i) {
            this.time[i] = (i + 1 - this.postref) * this.interval;
        }

        for(i = 0; i < this.postref - 1; ++i) {
            this.wholepre += this.whole.intensity[i] - this.base.intensity[i];
            this.frappre += this.frap.intensity[i] - this.base.intensity[i];
        }

        this.wholepre /= (float)this.preref;
        this.frappre /= (float)this.preref;

        for(i = 0; i < this.nSlices; ++i) {
            frapnorm[i] = (this.frap.intensity[i] - this.base.intensity[i]) / this.frappre;
        }

        if (this.doublenorm) {
            for(i = 0; i < this.nSlices; ++i) {
                frapnorm[i] *= this.wholepre / (this.whole.intensity[i] - this.base.intensity[i]);
            }
        }

        for(i = 0; i < this.nSlices; ++i) {
            this.normtable.incrementCounter();
            this.normtable.addLabel(" ", "Intensities for slice " + (i + 1) + " :");
            this.normtable.addValue("Normalized", (double)frapnorm[i]);
            this.normtable.addValue("Frap", (double)this.frap.intensity[i]);
            this.normtable.addValue("Whole", (double)this.whole.intensity[i]);
            this.normtable.addValue("Base", (double)this.base.intensity[i]);
            this.normtable.addValue("time", (double)this.time[i]);
            if (this.ref.intensity != null) {
                this.normtable.addValue("Ref", (double)this.ref.intensity[i]);
            }
        }

        this.normtable.show("Normalization Results");
    }

    public void Reset() {
        this.setNull(this.frap);
        this.setNull(this.base);
        this.setNull(this.ref);
        this.setNull(this.whole);
        this.rtable.reset();
    }

    private void setNull(Frap_Norm.Region reg) {
        reg.area = new float[this.nSlices];
        reg.intensity = new float[this.nSlices];
        reg.slice = new int[this.nSlices];
    }

    private void showAbout() {
        IJ.showMessage("About FRAP_Norm", "This plugin is specifically constructed to extract data from stacks of images\nfor FRAP analysis. It implements a median filter, deriche filter and subsequent\nhysteresis to find the outline of the different regions in the cell. Parameters\nfor this filters can be put in the settings, together with the time interval and\nthe slice numbers for the prebleach and postbleach guidance images.\n \nIt is written originally for analysis of chromatin dynamics in  Arabidopsis thaliana,\nand uses the normalization methods outlined in Phair et al (2004):\nMeasurement of dynamic protein binding to chromatin in vivo using photobleaching \nmicroscopy, Methods Enzymol 375, 393-414.\nBoth single and double normalization can be carried out.\n \nProcedure :\nUse the ROI-tools, e.g. the wand tool or the rectangular tool to select\na region of interest. Use the \"Set ROI\" button next to the corresponding region\nto save the ROI for that region. After defining the different ROIs, select the slice\nyou want to measure and press \"Apply to Image\" for a region. This will show up\nthe ROI for that region. Adjust the ROI by dragging it if necessary, and press\nthe \"Measure\" button. This will show up the measurement in the results window.\n \nWhen all measurements are done, you can press the \"Normalize\" button to normalize\nthe measurements. Normalization is done as outlined in Phair et al(2004), and can be\ndone with (double) or without (single) the use of the whole cell measurements.\nMeasurement of the reference area is not obliged for normalization.\n \nUse the \"Reset\" button to erase all measurements.Use the \"New image\" button after\nloading a new image for resetting all.Using this button will not erase the setted ROIs! \n \n---------------------------------------------------------------------------------------\n \nwritten by Joris FA Meys (2009). More info : jorismeys@gmail.com\nCanny-deriche and hysteresis plugins written by Thomas Boudier\nThis plugin is part of the public domain");
    }

    private void showPic() {
        ImageJ ij = IJ.getInstance();
        URL url = this.getClass().getResource("/aboutFA.jpg");
        if (url != null) {
            Image img = null;

            try {
                img = ij.createImage((ImageProducer)url.getContent());
            } catch (Exception var5) {
                ;
            }

            if (img != null) {
                ImagePlus imp = new ImagePlus("", img);
                ImageWindow.centerNextImage();
                imp.show();
            }
        } else {
            this.showAbout();
        }

    }

    private class Region implements ActionListener {
        Panel panel;
        Button measurebutton;
        Button applybutton;
        Button setbutton;
        Roi roi;
        float[] intensity;
        float[] area;
        int[] slice;
        String label;

        public Region(String label, int length) {
            this.label = label;
            this.intensity = new float[length];
            this.area = new float[length];
            this.slice = new int[length];
            this.panel = new Panel();
            this.panel.setLayout(new GridLayout(1, 4));
            this.panel.add(new Label(label + " :"));
            this.setbutton = new Button("Set ROI");
            this.setbutton.addActionListener(this);
            this.applybutton = new Button("Apply to Image");
            this.applybutton.addActionListener(this);
            this.measurebutton = new Button("Measure");
            this.measurebutton.addActionListener(this);
            this.panel.add(this.setbutton);
            this.panel.add(this.applybutton);
            this.panel.add(this.measurebutton);
            this.roi = null;
        }

        public void actionPerformed(ActionEvent e) {
            String action = e.getActionCommand();
            if (Frap_Norm.this.image) {
                if (action.equals("Set ROI")) {
                    this.roi = this.roiSet();
                } else if (action.equals("Apply to Image")) {
                    this.Apply(this.roi);
                } else if (action.equals("Measure")) {
                    this.imgMeasure();
                }
            } else {
                IJ.showMessage("No images open. \nOpen an image and click \"New image\".");
            }

        }

        private Roi roiSet() {
            ImagePlus img = this.getImage();
            if (img == null) {
                return null;
            } else {
                Roi roi = img.getRoi();
                if (roi == null) {
                    IJ.showMessage("No ROI selected");
                    return null;
                } else {
                    return roi;
                }
            }
        }

        private void Apply(Roi roi) {
            WindowManager.setCurrentWindow(Frap_Norm.this.current);
            ImagePlus img = this.getImage();
            if (img != null) {
                if (roi == null) {
                    IJ.showMessage("No ROI available. Set ROI first");
                } else {
                    img.setRoi(roi);
                }
            }
        }

        private void imgMeasure() {
            ImagePlus img = this.getImage();
            if (img != null) {
                Roi roi = img.getRoi();
                if (!Frap_Norm.this.autocalc) {
                    this.Measure(img, roi);
                } else {
                    for(int i = 1; i <= Frap_Norm.this.nSlices; ++i) {
                        img.setSlice(i);
                        this.Measure(img, roi);
                    }

                    img.setSlice(1);
                }
            }
        }

        private void Measure(ImagePlus slice, Roi roi) {
            ImageProcessor ip = Frap_Norm.this.img.getProcessor();
            ip.setRoi(roi);
            ImageStatistics stats = ImageStatistics.getStatistics(ip, 3, (Calibration)null);
            int index = Frap_Norm.this.img.getCurrentSlice() - 1;
            this.slice[index] = index + 1;
            this.area[index] = (float)stats.area;
            this.intensity[index] = (float)stats.mean;
            Frap_Norm.this.displayResults(this.label, index + 1, stats.area, stats.mean);
        }

        private ImagePlus getImage() {
            ImagePlus imp = WindowManager.getCurrentImage();
            if (imp == null) {
                IJ.showMessage("There are no images open.");
                return null;
            } else {
                return imp;
            }
        }
    }

    private class Display extends PlugInFrame implements ActionListener, ItemListener {
        Frame window;
        Panel normpanel;
        Panel autopanel;
        Panel resetpanel;
        Choice norm;
        Choice auto;

        public Display() {
            super("FRAP Analysis");
            if (this.window != null) {
                this.window.toFront();
            } else {
                this.window = this;
                this.setLayout(new GridLayout(10, 1, 5, 5));
                this.normpanel = new Panel();
                Choice norm = new Choice();
                this.normpanel.add(new Label("Normalization :"));
                norm.add("Double");
                norm.add("Single");
                norm.addItemListener(this);
                this.normpanel.add(norm);
                this.autopanel = new Panel();
                Choice auto = new Choice();
                this.autopanel.add(new Label("Measurement :"));
                auto.add("Manual");
                auto.add("Automatic");
                auto.addItemListener(this);
                this.autopanel.add(auto);
                this.resetpanel = new Panel();
                this.resetpanel.setLayout(new GridLayout(1, 2));
                Button resetbutton = new Button("Reset");
                resetbutton.addActionListener(this);
                Button imagebutton = new Button("New image");
                imagebutton.addActionListener(this);
                this.resetpanel.add(resetbutton);
                this.resetpanel.add(imagebutton);
                this.window.add(Frap_Norm.this.frap.panel);
                this.window.add(Frap_Norm.this.base.panel);
                this.window.add(Frap_Norm.this.whole.panel);
                this.window.add(Frap_Norm.this.ref.panel);
                this.add(this.autopanel);
                this.add(this.normpanel);
                this.addButton("Normalize");
                this.add(this.resetpanel);
                this.addButton("Settings");
                this.addButton("Help");
                this.pack();
            }
        }

        public void windowClosing(WindowEvent e) {
            super.windowClosing(e);
            this.window = null;
        }

        private void addButton(String label) {
            Button b = new Button(label);
            b.addActionListener(this);
            this.window.add(b);
        }

        public void actionPerformed(ActionEvent e) {
            String action = e.getActionCommand();
            if (action.equals("Normalize")) {
                if (Frap_Norm.this.image) {
                    Frap_Norm.this.Normalize();
                }
            } else if (action.equals("Reset")) {
                Frap_Norm.this.Reset();
            } else if (action.equals("Settings")) {
                Frap_Norm.this.Settings();
            } else if (action.equals("Help")) {
                Frap_Norm.this.showAbout();
            } else if (action.equals("New image")) {
                if (Frap_Norm.this.image) {
                    Frap_Norm.this.prebleach.close();
                    Frap_Norm.this.postbleach.close();
                }

                Frap_Norm.this.image = Frap_Norm.this.getNewImage();
                if (Frap_Norm.this.image) {
                    Frap_Norm.this.Reset();
                    Frap_Norm.this.displayGuidance(Frap_Norm.this.img);
                }
            }

        }

        public void itemStateChanged(ItemEvent e) {
            String item = (String)e.getItem();
            if (item.equals("Double")) {
                Frap_Norm.this.doublenorm = true;
            }

            if (item.equals("Single")) {
                Frap_Norm.this.doublenorm = false;
            }

            if (item.equals("Manual")) {
                Frap_Norm.this.autocalc = false;
            }

            if (item.equals("Automatic")) {
                Frap_Norm.this.autocalc = true;
            }

        }
    }
}
