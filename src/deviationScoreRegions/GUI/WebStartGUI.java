//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions.GUI;

import basics.ProgressListener;
import basics.Tools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.modular.DSR_Modular;
import evaluation.fuzzy.FuzzySegmentationBatch;
import ij.process.ImageProcessor;
import java.awt.Canvas;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import others.morphological.MorphologicalOOIExtraction;
import others.sirithana.VideoSegmentation;

public class WebStartGUI extends JFrame implements Runnable, ProgressListener {
    ImageProcessor imageProcessor = null;
    ImageProcessor ourResult;
    ImageProcessor morphologicalResult;
    ImageProcessor fuzzyResult;
    ImageProcessor videoResult;
    private Thread segmentationThread;
    private WebStartGUI.Method method = null;
    private List<ImageProcessor> debugImages = new LinkedList();
    private ButtonGroup buttonGroup1;
    private Canvas canvas1;
    private JButton jButton1;
    private JButton jButton2;
    private JButton jButton3;
    private JButton jButton4;
    private JComboBox jComboBox1;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JProgressBar jProgressBar1;
    private Label label1;
    private Label label2;
    private JTextField tfSize;

    public void updateImage(ImageProcessor debugImage) {
        this.debugImages.add(debugImage.duplicate());
        ((ImageCanvas)this.canvas1).setImageProcessor(debugImage);
    }

    public WebStartGUI() throws InterruptedException {
        this.initComponents();
        this.jProgressBar1.setVisible(false);
        ((ImageCanvas)this.canvas1).setText("Loading image...");
        this.jComboBox1ActionPerformed((ActionEvent)null);
    }

    private void initComponents() {
        this.buttonGroup1 = new ButtonGroup();
        this.tfSize = new JTextField();
        this.jLabel2 = new JLabel();
        this.label1 = new Label();
        this.jProgressBar1 = new JProgressBar();
        this.canvas1 = new ImageCanvas();
        this.jComboBox1 = new JComboBox();
        this.label2 = new Label();
        this.jLabel1 = new JLabel();
        this.jButton1 = new JButton();
        this.jButton2 = new JButton();
        this.jButton3 = new JButton();
        this.jButton4 = new JButton();
        this.setDefaultCloseOperation(3);
        this.setTitle("Low DOF Segmentation");
        this.setIconImages((List)null);
        this.setResizable(false);
        this.tfSize.setText("400");
        this.tfSize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                WebStartGUI.this.tfSizeActionPerformed(evt);
            }
        });
        this.jLabel2.setText("Scale to fit within");
        this.label1.setText("start segmentation:");
        this.jProgressBar1.setStringPainted(true);
        this.canvas1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                WebStartGUI.this.canvas1MouseClicked(evt);
            }
        });
        this.jComboBox1.setModel(new DefaultComboBoxModel(new String[]{"lena", "anna", "flower", "butterfly", "bird", "lines", "dog", "<load image...>"}));
        this.jComboBox1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                WebStartGUI.this.jComboBox1ActionPerformed(evt);
            }
        });
        this.label2.setText("Image");
        this.jLabel1.setText("pixels");
        this.jButton1.setText("our");
        this.jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                WebStartGUI.this.jButton1ActionPerformed(evt);
            }
        });
        this.jButton2.setText("morphological");
        this.jButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                WebStartGUI.this.jButton2ActionPerformed(evt);
            }
        });
        this.jButton3.setText("fuzzy");
        this.jButton3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                WebStartGUI.this.jButton3ActionPerformed(evt);
            }
        });
        this.jButton4.setText("video");
        this.jButton4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                WebStartGUI.this.jButton4ActionPerformed(evt);
            }
        });
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.jProgressBar1, -1, 529, 32767).addGroup(layout.createSequentialGroup().addComponent(this.label1, -2, -1, -2).addGap(18, 18, 18).addComponent(this.jButton1).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.jButton2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.jButton3).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.jButton4)).addGroup(layout.createSequentialGroup().addComponent(this.label2, -2, -1, -2).addGap(1, 1, 1).addComponent(this.jComboBox1, -2, 89, -2).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.jLabel2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.tfSize, -2, 65, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.jLabel1, -2, 49, -2)).addComponent(this.canvas1, -2, 450, -2)).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.jComboBox1, -2, -1, -2).addComponent(this.jLabel2).addComponent(this.tfSize, -2, -1, -2).addComponent(this.jLabel1)).addComponent(this.label2, -2, -1, -2)).addGap(13, 13, 13).addComponent(this.canvas1, -2, 300, -2).addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.TRAILING).addComponent(this.label1, -2, -1, -2).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.jButton1).addComponent(this.jButton2).addComponent(this.jButton3).addComponent(this.jButton4))).addPreferredGap(ComponentPlacement.RELATED, 16, 32767).addComponent(this.jProgressBar1, -2, -1, -2).addContainerGap()));
        this.pack();
    }

    private void startSegmentation() {
        this.segmentationThread = new Thread(this);
        this.segmentationThread.start();
    }

    private void canvas1MouseClicked(MouseEvent evt) {
    }

    private void loadSampleRessource() {
        Image i = Toolkit.getDefaultToolkit().createImage(this.getClass().getResource("/resources/" + this.jComboBox1.getSelectedItem().toString() + ".jpg"));
        ((ImageCanvas)this.canvas1).setImage(i);
    }

    private void selectFile() {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == 0) {
            try {
                String fileName = chooser.getSelectedFile().getAbsolutePath();
                ((ImageCanvas)this.canvas1).setImageFileName(fileName);
            } catch (IOException var4) {
                Logger.getLogger(WebStartGUI.class.getName()).log(Level.SEVERE, (String)null, var4);
            }
        }

    }

    private void unselectAllRadioButtons() {
        this.ourResult = null;
        this.morphologicalResult = null;
        this.fuzzyResult = null;
        this.videoResult = null;
    }

    private void jComboBox1ActionPerformed(ActionEvent evt) {
        this.unselectAllRadioButtons();
        if (this.jComboBox1.getSelectedIndex() == this.jComboBox1.getItemCount() - 1) {
            this.selectFile();
        } else {
            this.loadSampleRessource();
        }

        this.imageProcessor = ((ImageCanvas)this.canvas1).getImageProcessor();
    }

    private void jButton1ActionPerformed(ActionEvent evt) {
        this.method = WebStartGUI.Method.our;
        this.startSegmentation();
    }

    private void jButton2ActionPerformed(ActionEvent evt) {
        this.method = WebStartGUI.Method.morph;
        this.startSegmentation();
    }

    private void jButton3ActionPerformed(ActionEvent evt) {
        this.method = WebStartGUI.Method.fuzzy;
        this.startSegmentation();
    }

    private void jButton4ActionPerformed(ActionEvent evt) {
        this.method = WebStartGUI.Method.video;
        this.startSegmentation();
    }

    private void tfSizeActionPerformed(ActionEvent evt) {
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    (new WebStartGUI()).setVisible(true);
                } catch (InterruptedException var2) {
                    Logger.getLogger(WebStartGUI.class.getName()).log(Level.SEVERE, (String)null, var2);
                }

            }
        });
    }

    public void run() {
        this.debugImages.clear();
        DEBUG.setVerbose(false);
        this.setEnabled(false);
        this.jProgressBar1.setValue(0);
        this.jProgressBar1.setString("Segmentation running...");
        this.jProgressBar1.setIndeterminate(true);
        this.jProgressBar1.setVisible(true);
        int size = Integer.parseInt(this.tfSize.getText());
        if (this.method == WebStartGUI.Method.our && this.ourResult == null) {
            this.ourResult = (new DSR_Modular(this)).run(Tools.resize(this.imageProcessor, size));
        }

        if (this.method == WebStartGUI.Method.morph && this.morphologicalResult == null) {
            this.morphologicalResult = (new MorphologicalOOIExtraction()).run(Tools.resize(this.imageProcessor, size));
        }

        if (this.method == WebStartGUI.Method.fuzzy && this.fuzzyResult == null) {
            this.fuzzyResult = (new FuzzySegmentationBatch()).run(Tools.resize(this.imageProcessor, size));
        }

        if (this.method == WebStartGUI.Method.video && this.videoResult == null) {
            this.videoResult = (new VideoSegmentation()).run(Tools.resize(this.imageProcessor, size));
        }

        if (this.method == WebStartGUI.Method.our) {
            ((ImageCanvas)this.canvas1).setImageProcessor(this.ourResult);
        }

        if (this.method == WebStartGUI.Method.morph) {
            ((ImageCanvas)this.canvas1).setImageProcessor(this.morphologicalResult);
        }

        if (this.method == WebStartGUI.Method.fuzzy) {
            ((ImageCanvas)this.canvas1).setImageProcessor(this.fuzzyResult);
        }

        if (this.method == WebStartGUI.Method.video) {
            ((ImageCanvas)this.canvas1).setImageProcessor(this.videoResult);
        }

        this.setEnabled(true);
        this.jProgressBar1.setVisible(false);
        this.canvas1.repaint();
    }

    public void progressUpdate(double value, String text) {
        this.jProgressBar1.setIndeterminate(false);
        int v = (int)(100.0D * value);
        this.jProgressBar1.setValue(v);
        this.jProgressBar1.setString(v + "% (" + text + ")");
    }

    private static enum Method {
        our,
        morph,
        fuzzy,
        video;

        private Method() {
        }
    }
}
