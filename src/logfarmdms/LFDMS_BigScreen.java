/*
 * Copyright (c) 2018, tuxjsmith@gmail.com, paulb@logfarm.net
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package logfarmdms;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import static logfarmdms.LFDMS_GUI.isNumeric;
import static utilities.LFDMS_Constants.CAMERAS_DETAILS_HM;
import utilities.LFDMS_Status;

/**
 * [TODO]
 *      - Option and configuration option to mute audio on play back.
 *      - Window title will say that audio is enabled or muted.
 * [/]
 * 
 * @author tuxjsmith@gmail.com
 */
public class LFDMS_BigScreen extends javax.swing.JFrame {

    private final BufferedImage BI;
    /*
        [TODO]
            Can mainGuiPlaybackToggleButton be final ?
        [/]
    */
    private final JToggleButton PLAYBACK_TOGGLE_BUTTON;
    private final java.net.URL URL = this.getClass ().getResource ("/res/icon_64.png");
    private final javax.swing.ImageIcon II = new javax.swing.ImageIcon (URL);
    private final java.awt.Image FRAMEICON = II.getImage ();
    private String unmutedTitle = "";
    private Integer guiRowId = -1;
    
    /** 
     * Creates new form BigScreen
     * 
     * [TODO]
     *      Documentation.
     *      Unit test.
     * 
     *      Might be better to pass the screen's parent GUI and get the toggle
     *      button from there, which would also allow for future GUI interrogation.
     *      
     *      rid, row id, can that be obtained from LFDMS_Status ?
     * [/]
     * @param toggleButton main LFDMS_GUI playback button
    */
    public LFDMS_BigScreen ( JToggleButton toggleButton ) {

        initComponents ();
        
        setFrameIcon (FRAMEICON);
        
        PLAYBACK_TOGGLE_BUTTON = toggleButton;
        
        if (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",").length == 2
            && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim ())
            && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim ())) {
            
            BI = new BufferedImage (Integer.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim ()),
                                    Integer.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim ()),
                                    8);
        }
        else {
            
            BI = new BufferedImage (640,
                                    480,
                                    8);
        }
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/]
     * @param image 
     */
    private void setFrameIcon (Image image) {
        
        setIconImage (image);
    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/]
     * @param bi 
     */
    public void setBufferedImage (BufferedImage bi) {

        BI.getGraphics ().drawImage (bi,
                                     0, 0,
                                     bi.getWidth (), bi.getHeight (),
                                     null);
        
        jLabel1.setIcon (new ImageIcon (bi.getScaledInstance (getWidth (),
                                                              getHeight (),
                                                              Image.SCALE_FAST)));
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/]
     */
    private void refreshDisplay () {
        
//        if (getTitle ().equals ("logFarmDMS :: playback")) {
            
            if (BI != null) {
            
                jLabel1.setIcon (new ImageIcon (BI.getScaledInstance (getWidth (),
                                                                      getHeight (),
                                                                      Image.SCALE_FAST)));
            }
//        }
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/]
     * @return 
     */
    public JLabel getImageLabel () {
        
        return jLabel1;
    } 
    
//    public JMenuItem getMenuMute () {
//        
//        return menu_mute;
//    } 

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        menu_copy = new javax.swing.JMenuItem();
        jLabel1 = new javax.swing.JLabel();

        jPopupMenu1.setBackground(new java.awt.Color(51, 51, 51));
        jPopupMenu1.setDoubleBuffered(true);

        menu_copy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        menu_copy.setBackground(new java.awt.Color(51, 51, 51));
        menu_copy.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        menu_copy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/edit-copy.png"))); // NOI18N
        menu_copy.setText("copy image");
        menu_copy.setDoubleBuffered(true);
        menu_copy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_copyActionPerformed(evt);
            }
        });
        jPopupMenu1.add(menu_copy);

        setTitle("logFarm DMS :: live");
        setBackground(new java.awt.Color(0, 0, 0));
        setMinimumSize(new java.awt.Dimension(320, 240));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        jLabel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setFont(new java.awt.Font("SansSerif", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(204, 204, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setComponentPopupMenu(jPopupMenu1);
        jLabel1.setDoubleBuffered(true);
        jLabel1.setOpaque(true);
        getContentPane().add(jLabel1, java.awt.BorderLayout.CENTER);
        jLabel1.getAccessibleContext().setAccessibleName("<html><bod><center>if you can see this text<br>then the camera has been disabled in:<br><b>configuration.json</b><center></body></html>");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * [TODO]
     *      Documentation.
     *      Unit test or black box test.
     * [/]
     * @param evt 
     */
    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        
        if (evt.getKeyCode () == java.awt.event.KeyEvent.VK_R) {
            
            refreshDisplay ();
        }
        
        if (evt.isControlDown ()
            && evt.getKeyCode () == java.awt.event.KeyEvent.VK_C) {
            
            copyImage (this);
        }
    }//GEN-LAST:event_formKeyReleased

    /**
     * [TODO]
     *      Documentation.
     *      Unit test or black box test.
     * [/]
     * @param evt 
     */
    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        
        refreshDisplay ();
    }//GEN-LAST:event_formComponentResized

    /**
     * [TODO]
     *      Documentation.
     *      Unit test or black box test.
     * [/]
     * @param evt 
     */
    private void menu_copyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_copyActionPerformed
        
        copyImage (this);
    }//GEN-LAST:event_menu_copyActionPerformed

    /**
     * [TODO]
     *      Documentation.
     *      Unit test or black box test.
     * [/]
     * @param evt 
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        
        if (PLAYBACK_TOGGLE_BUTTON.isSelected ()) {
            
            PLAYBACK_TOGGLE_BUTTON.doClick ();
        }
    }//GEN-LAST:event_formWindowClosing

    /**
     * [TODO]
     *      Documentation.
     *      Unit test or black box test.
     * [/]
     * @param bs 
     */
    public void copyImage (LFDMS_BigScreen bs) {
        
        Toolkit.getDefaultToolkit ().getSystemClipboard ().setContents (new Transferable () {

            @Override
            public DataFlavor[] getTransferDataFlavors () {
                
                JOptionPane.showMessageDialog (bs, "Image has been copied", "Clipboard copy", JOptionPane.INFORMATION_MESSAGE);
                
                return new DataFlavor[] {DataFlavor.imageFlavor};
            }

            @Override
            public boolean isDataFlavorSupported (DataFlavor flavor) {
                
                throw new UnsupportedOperationException ("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getTransferData (DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                
                return BI;
            }
        }, null);
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JMenuItem menu_copy;
    // End of variables declaration//GEN-END:variables
}
