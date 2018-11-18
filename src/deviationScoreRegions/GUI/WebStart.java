//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions.GUI;

import basics.Tools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.DeviationScoreRegions_Tuned;
import ij.process.ImageProcessor;
import java.awt.Canvas;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class WebStart extends JFrame {
    ImageProcessor imageProcessor = null;
    private Canvas canvas1;
    private Canvas canvas2;
    private Canvas canvas3;
    private JCheckBox cbDebugInfos;
    private JButton jButton2;
    private JLabel jLabel2;
    private JTextField tfSize;

    public WebStart() {
        this.initComponents();
        ((ImageCanvas)this.canvas1).setText("Click to add image...");
    }

    private void initComponents() {
        this.jButton2 = new JButton();
        this.tfSize = new JTextField();
        this.jLabel2 = new JLabel();
        this.cbDebugInfos = new JCheckBox();
        this.canvas1 = new ImageCanvas();
        this.canvas2 = new ImageCanvas();
        this.canvas3 = new ImageCanvas();
        this.setDefaultCloseOperation(3);
        this.jButton2.setText("Start segmentation");
        this.jButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                WebStart.this.jButton2ActionPerformed(evt);
            }
        });
        this.tfSize.setText("500");
        this.tfSize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                WebStart.this.tfSizeActionPerformed(evt);
            }
        });
        this.jLabel2.setText("Image Size");
        this.cbDebugInfos.setSelected(true);
        this.cbDebugInfos.setText("show debug informations");
        this.cbDebugInfos.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                WebStart.this.cbDebugInfosActionPerformed(evt);
            }
        });
        this.canvas1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                WebStart.this.canvas1MouseClicked(evt);
            }
        });
        this.canvas2.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                WebStart.this.canvas2MouseClicked(evt);
            }
        });
        this.canvas3.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                WebStart.this.canvas3MouseClicked(evt);
            }
        });
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(562, 562, 562).addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.jButton2).addComponent(this.cbDebugInfos).addGroup(layout.createSequentialGroup().addComponent(this.jLabel2).addGap(18, 18, 18).addComponent(this.tfSize, -2, 54, -2))).addContainerGap(277, 32767)).addGroup(Alignment.TRAILING, layout.createSequentialGroup().addContainerGap(378, 32767).addComponent(this.canvas1, -2, 300, -2).addGap(306, 306, 306)).addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(30, 30, 30).addComponent(this.canvas2, -2, 300, -2).addContainerGap(654, 32767))).addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(46, 46, 46).addComponent(this.canvas3, -2, 300, -2).addContainerGap(638, 32767))));
        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(173, 173, 173).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.jLabel2).addComponent(this.tfSize, -2, -1, -2)).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.cbDebugInfos).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.jButton2).addGap(47, 47, 47).addComponent(this.canvas1, -2, 200, -2).addContainerGap(107, 32767)).addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(35, 35, 35).addComponent(this.canvas2, -1, 220, 32767).addGap(352, 352, 352))).addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, layout.createSequentialGroup().addContainerGap(309, 32767).addComponent(this.canvas3, -2, 200, -2).addGap(98, 98, 98))));
        this.pack();
    }

    private void jButton2ActionPerformed(ActionEvent evt) {
        DEBUG.setVerbose(this.cbDebugInfos.isSelected());
        int size = Integer.parseInt(this.tfSize.getText());
        ImageProcessor resized = Tools.resize(this.imageProcessor, size);
        (new DeviationScoreRegions_Tuned()).run(resized);
        ImageProcessor segmented = Tools.cropToMask(resized);
        ((ImageCanvas)this.canvas1).setImageProcessor(segmented);
    }

    private void tfSizeActionPerformed(ActionEvent evt) {
    }

    private void cbDebugInfosActionPerformed(ActionEvent evt) {
    }

    private void canvas1MouseClicked(MouseEvent evt) {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == 0) {
            try {
                String fileName = chooser.getSelectedFile().getAbsolutePath();
                this.imageProcessor = Tools.loadImageProcessor(fileName);
                ((ImageCanvas)this.canvas1).setImageFileName(fileName);
            } catch (IOException var5) {
                Logger.getLogger(WebStart.class.getName()).log(Level.SEVERE, (String)null, var5);
            }
        }

    }

    private void canvas2MouseClicked(MouseEvent evt) {
    }

    private void canvas3MouseClicked(MouseEvent evt) {
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                (new WebStart()).setVisible(true);
            }
        });
    }
}
