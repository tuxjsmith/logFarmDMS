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

import java.awt.Cursor;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import java.sql.*;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import static logfarmdms.LFDMS_GUI.isNumeric;
import static utilities.LFDMS_Constants.CAMERAS_DETAILS_HM;
import utilities.LFDMS_DbInit;

/**
 * @author tuxjsmith@gmail.com
 */
public class LFDMS_ExportGui extends javax.swing.JFrame {

    private final JFileChooser FILE_CHOOSER;
    private final JToggleButton GUI_RECORD_TOGGLE_BUTTON;
    private final JLabel GUI_STATUS_LABEL;
    private final Connection CAMERA_DATABASE_CONNECTION;
    private final Integer CAMERA_NUMBER;
    private final java.net.URL URL = this.getClass ().getResource ("/res/icon_64.png");
    private final javax.swing.ImageIcon II = new javax.swing.ImageIcon (URL);
    private final java.awt.Image FRAME_ICON = II.getImage ();
    
    /** Creates new form ExportGui
     * 
     * [TODO]
     * 
     *      Unit test.
     * [/]
     * 
     * @param guiRecordToggleButton gui record toggle button so we can stop and start it
     * @param guiStatusLabel gui status label so we can change the text
     * @param cameraDbConnection camera database connection
     * @param cameraNumber used to help identify the export directory
    */
    public LFDMS_ExportGui (JToggleButton guiRecordToggleButton,
                            JLabel guiStatusLabel,
                            Connection cameraDbConnection,
                            Integer cameraNumber) {
        
        initComponents ();
        
        setFrameIcon (FRAME_ICON);
        
        CAMERA_NUMBER = cameraNumber;
        
        CAMERA_DATABASE_CONNECTION = cameraDbConnection;
//        AUDIO_DATABASE_CONNECTION = audioDbConnection;
        
        GUI_RECORD_TOGGLE_BUTTON = guiRecordToggleButton;
        
        GUI_STATUS_LABEL = guiStatusLabel;
        
        jSpinner1.setValue (5);
        
        //<editor-fold defaultstate="collapsed" desc="Stops the directories in the filechooser from being editable.">
        
        /*
            source: http://www.coderanch.com/t/555535/GUI/java/FileChooser-readOnly
        */
        final Boolean OLD = UIManager.getBoolean("FileChooser.readOnly");
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        FILE_CHOOSER = new JFileChooser ();
        UIManager.put("FileChooser.readOnly", OLD);

        FILE_CHOOSER.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        FILE_CHOOSER.setMultiSelectionEnabled (false);
        FILE_CHOOSER.setSelectedFile (new File (System.getProperty ("user.dir")));
        FILE_CHOOSER.setDialogTitle ("location to export video");
        FILE_CHOOSER.setApproveButtonText ("Open");
        
        /*
            Examining a FileChooser's parts.
        */
//        for (int i = 0; i < FILE_CHOOSER.getComponents ().length; i++) {
//
//            if (FILE_CHOOSER.getComponent (i) instanceof java.awt.Container) {
//
//                for (int n = 0; n < ((javax.swing.JPanel) FILE_CHOOSER.getComponent (i)).getComponents ().length; n++) {
//
//                    System.out.println (((javax.swing.JPanel) FILE_CHOOSER.getComponent (i)).getComponent (n).toString ());
//                }
//            }
//        }
        //</editor-fold>
        
        jLabel3.setText (System.getProperty ("user.dir") + File.separator); 
    }
    
    private void setFrameIcon (Image image) {
        
        setIconImage (image);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();

        setTitle("export to video file");
        setMinimumSize(new java.awt.Dimension(355, 190));
        setResizable(false);

        jPanel3.setBackground(new java.awt.Color(51, 51, 51));
        jPanel3.setMinimumSize(new java.awt.Dimension(10, 44));
        jPanel3.setPreferredSize(new java.awt.Dimension(300, 80));
        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(204, 153, 0));
        jLabel1.setText("---");
        jLabel1.setDoubleBuffered(true);
        jLabel1.setPreferredSize(new java.awt.Dimension(250, 25));
        jPanel3.add(jLabel1);

        jLabel3.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(204, 153, 0));
        jLabel3.setDoubleBuffered(true);
        jLabel3.setMinimumSize(new java.awt.Dimension(300, 30));
        jLabel3.setPreferredSize(new java.awt.Dimension(300, 30));
        jPanel3.add(jLabel3);

        jButton1.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/document-open.png"))); // NOI18N
        jButton1.setToolTipText("choose location");
        jButton1.setDoubleBuffered(true);
        jButton1.setPreferredSize(new java.awt.Dimension(34, 34));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton1);

        getContentPane().add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel4.setBackground(new java.awt.Color(51, 51, 51));
        jPanel4.setPreferredSize(new java.awt.Dimension(300, 100));
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jSpinner1.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jSpinner1.setDoubleBuffered(true);
        jSpinner1.setMinimumSize(new java.awt.Dimension(100, 30));
        jSpinner1.setPreferredSize(new java.awt.Dimension(100, 30));
        jSpinner1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner1StateChanged(evt);
            }
        });
        jPanel4.add(jSpinner1);

        jLabel2.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(204, 153, 0));
        jLabel2.setText("minutes");
        jLabel2.setDoubleBuffered(true);
        jLabel2.setPreferredSize(new java.awt.Dimension(50, 25));
        jPanel4.add(jLabel2);

        jPanel5.setBackground(new java.awt.Color(51, 51, 51));
        jPanel5.setPreferredSize(new java.awt.Dimension(100, 25));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel5);

        jPanel6.setBackground(new java.awt.Color(51, 51, 51));
        jPanel6.setPreferredSize(new java.awt.Dimension(300, 25));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel6);

        jButton2.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/object-rotate-right.png"))); // NOI18N
        jButton2.setToolTipText("export");
        jButton2.setDoubleBuffered(true);
        jButton2.setPreferredSize(new java.awt.Dimension(34, 34));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton2);

        getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * [TODO]
     *      Rename button.
     *      Documentation.
     *      Unit test or black box test.
     * [/]
     * @param evt 
     */
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        
        FILE_CHOOSER.setSelectedFile (new File (System.getProperty ("user.dir") + File.separator));
        
        int returnVal = FILE_CHOOSER.showOpenDialog (this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            jLabel3.setText (FILE_CHOOSER.getSelectedFile () + File.separator); 
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * [TODO]
     *      Rename button.
     *      Documentation.
     *      Unit test or black box test.
     * [/]
     * @param evt 
     */
    private void jSpinner1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner1StateChanged
        
        if (((Integer)jSpinner1.getValue ()) < 1) jSpinner1.setValue (1);
    }//GEN-LAST:event_jSpinner1StateChanged

    /**
     * [TODO]
     *      Move to database stuff class.
     *      Rename this button.
     *      Documentation.
     *      Unit test or black box test.
     * [/]
     * @param evt 
     */
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        
        new Thread () {
            
            @Override
            public void run () {
                
                AudioFormat format = getFormat ();

                Boolean wasRecordButtonSelected_b = Boolean.FALSE;
                
                if (GUI_RECORD_TOGGLE_BUTTON != null
                    && GUI_RECORD_TOGGLE_BUTTON.isSelected ()) {
                    
                    GUI_RECORD_TOGGLE_BUTTON.doClick ();
                    
                    wasRecordButtonSelected_b = Boolean.TRUE;
                }
                
                setCursor (new Cursor (Cursor.WAIT_CURSOR));
                
                String outDir = null; 
                
                try {
                    
                    outDir = FILE_CHOOSER.getSelectedFile ().getCanonicalPath () + System.getProperty ("file.separator") + "camera_" + CAMERA_NUMBER + "_" +jLabel1.getText ().replaceAll(" ", "_").replaceAll (":", "-") + "_export";
                }
                catch (IOException ex) {
                    
                    System.err.println (ex.getMessage ());
                }
                
                try {
    
                    if (!new File (outDir).exists ()) {

                        new File (outDir).mkdir ();
                    }

                    final String SQL_COMMAND = "select fileBytes, date from fileData where date >= datetime(\"" + jLabel1.getText () +"\",'+0 minutes') and date <= datetime(\"" + jLabel1.getText () + "\",'+" + jSpinner1.getValue () + " minutes')";

                    final Statement STATEMENT;
                    STATEMENT = CAMERA_DATABASE_CONNECTION.createStatement ();

                    final ResultSet RESULT_SET;
                    RESULT_SET = STATEMENT.executeQuery (SQL_COMMAND);

                    Integer statusExportTickCounter = 0;
                    
                    while (RESULT_SET.next ()) {

                        GUI_STATUS_LABEL.setText ("exporting ");
                        statusExportTickCounter++;
                        if (statusExportTickCounter <= 2) GUI_STATUS_LABEL.setText ("exporting \\");
                        else if (statusExportTickCounter > 2 && statusExportTickCounter <= 4) GUI_STATUS_LABEL.setText ("exporting |");
                        else if (statusExportTickCounter > 4 && statusExportTickCounter <= 6) GUI_STATUS_LABEL.setText ("exporting /");
                        else if (statusExportTickCounter > 6 && statusExportTickCounter < 8) GUI_STATUS_LABEL.setText ("exporting --");
                        else statusExportTickCounter = 0;
                        
                        BufferedImage image_bi;
                        
                        /*
                            try with resouces
                        */
                        try (BufferedInputStream bin = new BufferedInputStream (new ByteArrayInputStream (RESULT_SET.getBytes (1)))) {

                            image_bi = ImageIO.read (bin);
                        }

                        byte[] bytesOut;

                        /*
                            try with resources
                        */
                        try (final ByteArrayOutputStream BYTE_ARRAY_OUTPUT_STREAM = new ByteArrayOutputStream ()) {

                            ImageIO.write (image_bi, "jpg", BYTE_ARRAY_OUTPUT_STREAM);

                            bytesOut = BYTE_ARRAY_OUTPUT_STREAM.toByteArray ();
                        }

                        /*
                            try with resources
                        */
                        try (FileOutputStream fos_fullSize = new FileOutputStream (outDir + System.getProperty ("file.separator") + RESULT_SET.getString (2).replaceAll(" ", "_").replaceAll (":", "-").replaceAll("\\.", "_") + ".jpg")) {

                            fos_fullSize.write (bytesOut);
                        }
                    }

                    RESULT_SET.close ();

                    STATEMENT.closeOnCompletion ();

                    final String ADUIO_SQL_COMMAND = "select fileBytes, date from fileData where date >= datetime(\"" + jLabel1.getText () +"\",'0 minutes') and date <= datetime(\"" + jLabel1.getText () + "\",'" + jSpinner1.getValue () + " minutes')";

                    final Statement AUDIO_STATEMENT = LFDMS_DbInit.getAudioDatabaseConnection ().createStatement ();

                    final ResultSet AUDIO_RESULT_SET = AUDIO_STATEMENT.executeQuery (ADUIO_SQL_COMMAND);

                    while (AUDIO_RESULT_SET.next ()) {

                        try (ByteArrayInputStream input = new ByteArrayInputStream (AUDIO_RESULT_SET.getBytes (1));
                             AudioInputStream ais = new AudioInputStream (input, format, AUDIO_RESULT_SET.getBytes (1).length / format.getFrameSize ())) {

                            AudioSystem.write (ais, AudioFileFormat.Type.WAVE, new File (outDir + System.getProperty ("file.separator") + AUDIO_RESULT_SET.getString (2).replaceAll(" ", "_").replaceAll (":", "-").replaceAll("\\.", "_") + ".wav")); 
                        }
                    }

                    AUDIO_RESULT_SET.close ();

                    AUDIO_STATEMENT.closeOnCompletion ();
                }
                catch (SQLException | IOException ex) {

                    System.err.println ("LFDMS_ExportGui.jButton2ActionPerformed " + ex.getMessage ());
                }
                
                /*
                    create video file script
                */
                try (FileWriter fw = new FileWriter (outDir + System.getProperty ("file.separator") + "createVideo.sh");
                     BufferedWriter bw = new BufferedWriter (fw)) {

                    /* motion jpeg */
                    //fw.write ("sox *.wav output.wav\nmencoder mf://*.jpg -mf w=320:h=240:fps=1:type=jpg -ovc copy -oac copy -o output.avi -audiofile output.wav\n");

                    /* mpeg 4 */
                    if (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",").length == 2
                        && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim ())
                        && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim ())) {
                        
                        bw.write ("sox *.wav output.wav\nmencoder mf://*.jpg -mf w=" + CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim () + ":h=" + CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim () + ":fps=4:type=jpg -ovc lavc -lavcopts vcodec=mpeg4:mbd=2:trell -oac copy -vf scale=640:480 -o output.avi -audiofile output.wav\n");
                    }
                    else {
                        
                        bw.write ("sox *.wav output.wav\nmencoder mf://*.jpg -mf w=:h=480:fps=4:type=jpg -ovc lavc -lavcopts vcodec=mpeg4:mbd=2:trell -oac copy -vf scale=640:480 -o output.avi -audiofile output.wav\n");
                    }
                    
                    File f = new File (outDir + System.getProperty ("file.separator") + "createVideo.sh");
                    f.setExecutable (true);
                }
                catch (IOException ioe) {

                    System.err.println ("createVideo.sh " + ioe.getMessage ());
                }
                
                try (FileWriter fw = new FileWriter (outDir + System.getProperty ("file.separator") + "createVideo.bat");
                     BufferedWriter bw = new BufferedWriter (fw)) {

                    /* motion jpeg */
                    //fw.write ("sox *.wav output.wav\nmencoder mf://*.jpg -mf w=320:h=240:fps=1:type=jpg -ovc copy -oac copy -o output.avi -audiofile output.wav\n");

                    /* mpeg 4 */
                    if (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",").length == 2
                        && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim ())
                        && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim ())) {
                        
                        bw.write ("sox *.wav output.wav\nmencoder mf://*.jpg -mf w=" + CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim () + ":h=" + CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim () + ":fps=4:type=jpg -ovc lavc -lavcopts vcodec=mpeg4:mbd=2:trell -oac copy -vf scale=640:480 -o output.avi -audiofile output.wav\n");
                    }
                    else {
                        
                        bw.write ("sox *.wav output.wav\nmencoder mf://*.jpg -mf w=:h=480:fps=4:type=jpg -ovc lavc -lavcopts vcodec=mpeg4:mbd=2:trell -oac copy -vf scale=640:480 -o output.avi -audiofile output.wav\n");
                    }
                }
                catch (IOException ioe) {

                    System.err.println ("createVideo.bat " + ioe.getMessage ());
                }
                
                setCursor (new Cursor (Cursor.DEFAULT_CURSOR));
                
                if (wasRecordButtonSelected_b
                    && GUI_RECORD_TOGGLE_BUTTON != null
                    && !GUI_RECORD_TOGGLE_BUTTON.isSelected ()) {
                    
                    GUI_RECORD_TOGGLE_BUTTON.doClick ();
                }
            }
        }.start ();
    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSpinner jSpinner1;
    // End of variables declaration//GEN-END:variables

    /**
     * [TODO]
     *      More informative parameter name.
     *      Documentation.
     *      Unit test.
     * [/]
     * @param s 
     */
    public void setDate (String s) {
        
        jLabel1.setText (s.split ("\\.")[0]);
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/]
     * @return 
     */
    private AudioFormat getFormat () {

        final float SAMPLE_RATE = 11025;
        final int SAMPLE_SIZE = 8;
        final int CHANNELS = 1;
        final boolean SIGNED = true;
        final boolean BIG_ENDIAN = true;

        return new AudioFormat (SAMPLE_RATE,
                                SAMPLE_SIZE,
                                CHANNELS,
                                SIGNED,
                                BIG_ENDIAN);
    }
}
