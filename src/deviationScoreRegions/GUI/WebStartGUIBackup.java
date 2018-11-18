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
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class WebStartGUIBackup extends JFrame {
    ImageProcessor imageProcessor = null;
    private Canvas canvas1;
    private JCheckBox cbDebugInfos;
    private JButton jButton2;
    private JLabel jLabel2;
    private JPanel jPanel1;
    private JTextField tfSize;

    public WebStartGUIBackup() {
        this.initComponents();
        ((ImageCanvas)this.canvas1).setText("Click to add image...");
    }

    private void initComponents() {
        this.jButton2 = new JButton();
        this.tfSize = new JTextField();
        this.jLabel2 = new JLabel();
        this.cbDebugInfos = new JCheckBox();
        this.jPanel1 = new JPanel();
        this.canvas1 = new ImageCanvas();
        this.setDefaultCloseOperation(3);
        this.jButton2.setText("Start segmentation");
        this.jButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                WebStartGUIBackup.this.jButton2ActionPerformed(evt);
            }
        });
        this.tfSize.setText("500");
        this.tfSize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                WebStartGUIBackup.this.tfSizeActionPerformed(evt);
            }
        });
        this.jLabel2.setText("Image Size");
        this.cbDebugInfos.setSelected(true);
        this.cbDebugInfos.setText("show debug informations");
        this.cbDebugInfos.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                WebStartGUIBackup.this.cbDebugInfosActionPerformed(evt);
            }
        });
        this.jPanel1.setBorder(BorderFactory.createBevelBorder(1));
        this.canvas1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                WebStartGUIBackup.this.canvas1MouseClicked(evt);
            }
        });
        GroupLayout jPanel1Layout = new GroupLayout(this.jPanel1);
        this.jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING).addComponent(this.canvas1, -1, 320, 32767));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING).addComponent(this.canvas1, -1, 220, 32767));
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(30, 30, 30).addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.cbDebugInfos).addGroup(layout.createSequentialGroup().addComponent(this.jLabel2).addGap(18, 18, 18).addComponent(this.tfSize, -2, 54, -2).addGap(32, 32, 32).addComponent(this.jButton2)).addComponent(this.jPanel1, -2, -1, -2)).addContainerGap(40, 32767)));
        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(29, 29, 29).addComponent(this.jPanel1, -2, -1, -2).addGap(18, 18, 18).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.jLabel2).addComponent(this.tfSize, -2, -1, -2).addComponent(this.jButton2)).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.cbDebugInfos).addContainerGap(44, 32767)));
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
                Logger.getLogger(WebStartGUIBackup.class.getName()).log(Level.SEVERE, (String)null, var5);
            }
        }

    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                (new WebStartGUIBackup()).setVisible(true);
            }
        });
    }
}
