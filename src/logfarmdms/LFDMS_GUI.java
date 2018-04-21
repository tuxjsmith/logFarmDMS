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

import java.awt.image.BufferedImage;
import java.sql.*;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UnsupportedLookAndFeelException;
import static org.bytedeco.javacpp.opencv_core.CV_FONT_HERSHEY_SCRIPT_COMPLEX;
import org.bytedeco.javacpp.opencv_core.CvFont;
import static org.bytedeco.javacpp.opencv_core.cvInitFont;
import static org.bytedeco.javacpp.opencv_highgui.CV_CAP_PROP_FRAME_HEIGHT;
import static org.bytedeco.javacpp.opencv_highgui.CV_CAP_PROP_FRAME_WIDTH;
import org.bytedeco.javacpp.opencv_highgui.CvCapture;
import static org.bytedeco.javacpp.opencv_highgui.cvCreateCameraCapture;
import static org.bytedeco.javacpp.opencv_highgui.cvQueryFrame;
import static org.bytedeco.javacpp.opencv_highgui.cvSetCaptureProperty;
import utilities.LFDMS_AudioInit;
import static utilities.LFDMS_Constants.CAMERAS_DETAILS_HM;
import static utilities.LFDMS_Constants.CAMERAS_HM;
import utilities.LFDMS_DbInit;
import utilities.LFDMS_OpenConfiguration;
import utilities.LFDMS_Timers;
import utilities.LFDMS_Constants;
import utilities.LFDMS_PlayAudioOutputStream;
import utilities.LFDMS_Status;

/**
 * [TODO]
 *      Images: Duplicate images reside in the 'res' and 'src' directories,
 *      which ones do we use ? Remove those we don't.
 * [/]
 * 
 * @author tuxjsmith@gmail.com
 */
public final class LFDMS_GUI extends javax.swing.JFrame implements LFDMS_Constants {

    /*
        [TODO] 
            Replace variables with a PROPERTIES_HM collection.
        [/]
    */
    private final ConcurrentHashMap<String, Object> PROPERTIES_HM = new ConcurrentHashMap ();
    
    private final Object FONT = new CvFont ();
    private final LFDMS_Status STATUS = new LFDMS_Status ();
    private final LFDMS_Timers TIMERS = new LFDMS_Timers (this);
    private final LFDMS_DbInit DB_STUFF = new LFDMS_DbInit ();
    private final LFDMS_BigScreen BIG_SCREEN;
    private final LFDMS_ExportGui EXPORT_GUI;
    /*
        Identifies which camera this LFDMS_GUI should operate. The default LFDMS_GUI
        will operate camera: 0 (zero)
    */
    private final Integer CAMERA_NUMBER;
    
    /*
        [TODO] 
            These could be candidates for LFDMS_Constants.java.
        [/]
    */
    private final java.net.URL URL = this.getClass ().getResource ("/res/icon_64.png");
    private final javax.swing.ImageIcon II = new javax.swing.ImageIcon (URL);
    private final java.awt.Image FRAME_ICON = II.getImage ();
    
    private static LFDMS_GUI lfdmsHeadGui = null;
    
    /**
     * Default GUI constructor.
     * 
     * [TODO] 
     *      Documentation.
     *      Unit test.
     * [/]
     */
    public LFDMS_GUI () {
        
        initComponents ();
        
        /*
            Get configuration details from the configuration file (JSON).
        
            Only main-gui calls this method because there's only one 
            configuration file.
        */
        LFDMS_OpenConfiguration.openConfiguration ();
        
        /*
            Only main-gui because we only record from a single microphone.
        */
        LFDMS_AudioInit.AUDIO_INIT ();
        
        CAMERA_NUMBER = 0;
        
        //<editor-fold defaultstate="collapsed" desc="Camera collection.">
        /*
            Populated by: LFDMS_OpenConfiguration.openConfiguration
        */
        if (CAMERAS_DETAILS_HM.containsKey (CAMERA_NUMBER.toString ())
            && CAMERAS_DETAILS_HM.get (CAMERA_NUMBER.toString ()).getProperty ("enabled").equals ("yes")) {
        
            DB_STUFF.INIT_CAMERA_DATABASE (CAMERA_NUMBER.toString ());
        }
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Audio database initialisation.">
        /*
            Other clients use the same audio database connection to play back 
            audio we pass this connection to those other clients
        
            [TODO] 
                A database class for access to connections would be better. 
            [/]
        */
        LFDMS_DbInit.INIT_AUDIO_DATABASE ();
        
        /*
            [BUG]
                Trouble is, if there aren't any video frames then the audio will 
                not be played back; might be able to export it though.
            [/]
        */
//        LFDMS_Timers.getRecordAudioTimer ().schedule (LFDMS_Timers.GET_NEW_CAPTURE_AUDIO_TIMERTASK (), 500, 5000);
        //</editor-fold>
        
        setTitle ("logFarmDMS :: " + CAMERA_NUMBER);
        
        setIconImage (FRAME_ICON);
        
        //<editor-fold defaultstate="collapsed" desc="Big screen instantiation.">
        /*
            [TODO]
                Big screen init appears twice, here and the other constructor.
                Can we move it to a method so constructors can call that.
            [/]
        */
        BIG_SCREEN = new LFDMS_BigScreen (playToggleButton ) ; //, LFDMS_Status.getAudioPlaybackRowId ());
        BIG_SCREEN.setTitle ("logFarm DMS :: " + CAMERA_NUMBER + " :: live"); 
        /*
            set the initial location of bigScreen
        */
        BIG_SCREEN.setLocation (getX () + (getWidth()/2), 
                                getY () + (getHeight ()/2));
        //</editor-fold>
         
        //<editor-fold defaultstate="collapsed" desc="Export LFDMS_GUI instantiation.">
        /*
            [TODO]
                Export LFDMS_GUI init appears twice, here and the other constructor.
                Can we move it to a method so constructors can call that.
            [/]
        */
        EXPORT_GUI = new LFDMS_ExportGui (recordToggleButton,
                                    statusBarLabel,
                                    getDbStuff ().getCameraDatabaseConnection (),
                                    getCameraNumber ());

        EXPORT_GUI.setLocation (getX () + (getWidth()/2), 
                                getY () + (getHeight ()/2));
        //</editor-fold>

        /*
            camera globals
        */
        cvInitFont ((CvFont) FONT, CV_FONT_HERSHEY_SCRIPT_COMPLEX, 0.5, 0.5);
        /*
            end camera globals
        */

        /*
            Loop over a collection of camera details and create a collection
            of OpenCV camera objects.
        
            The reason for two camera collections:
            - CAMERAS_DETAILS_HM :: Saved preferences from the configuration file.
            - CAMERAS_HM :: OpenCV camera objects.
        
            Both collections have a numerical key but of type: String
        */
        for (int i = 0; i < Integer.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("number_of_cameras")); i++) {

            if (CAMERAS_DETAILS_HM.containsKey (Integer.toString (i))
                && CAMERAS_DETAILS_HM.get (Integer.toString (i)).getProperty ("enabled").equals ("yes")) {
            
                final Object CAMERA = cvCreateCameraCapture (i);

                if (CAMERA != null) {

                    CAMERAS_HM.put (i, CAMERA);

                    //<editor-fold defaultstate="collapsed" desc="Set height and width of each camera capture image."> 
                    if (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",").length == 2
                        && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim ())
                        && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim ())) {

                        cvSetCaptureProperty ((CvCapture) CAMERAS_HM.get (i),
                                              CV_CAP_PROP_FRAME_WIDTH,
                                              Double.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim ()));

                        cvSetCaptureProperty ((CvCapture) CAMERAS_HM.get (i),
                                              CV_CAP_PROP_FRAME_HEIGHT,
                                              Double.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim ()));

                    }
                    else {

                        cvSetCaptureProperty ((CvCapture) CAMERAS_HM.get (i),
                                              CV_CAP_PROP_FRAME_WIDTH,
                                              640);

                        cvSetCaptureProperty ((CvCapture) CAMERAS_HM.get (i),
                                              CV_CAP_PROP_FRAME_HEIGHT,
                                              480);
                    }
                    //</editor-fold>
                }
            }
        }

        //<editor-fold defaultstate="collapsed" desc="Spawn additional GUIs.">
        if (!CAMERAS_HM.isEmpty ()) {

            for (Enumeration<Integer> e = CAMERAS_HM.keys (); e.hasMoreElements (); ) {

                final Integer KEY = e.nextElement ();

                if (KEY > 0) {

                    LFDMS_GUI gui = new LFDMS_GUI ( KEY );
//                                                   DB_STUFF.getAudioDatabaseConnection ());
                    GUIS_HM.put (KEY, gui);

                    /*
                        set LFDMS_GUI screen location
                    */
                    if (CAMERAS_DETAILS_HM.containsKey (Integer.toString (KEY))) {

                        gui.setLocation (Integer.valueOf (CAMERAS_DETAILS_HM.get (Integer.toString (KEY)).getProperty ("screen_location").split (",")[0].trim ()), 
                                         Integer.valueOf (CAMERAS_DETAILS_HM.get (Integer.toString (KEY)).getProperty ("screen_location").split (",")[1].trim ()));
                    }
                    else {

                        gui.setLocation (getX () + ((getWidth ()/2)*KEY),
                                         getY () + ((getHeight ()/2)*KEY));
                    }

                    gui.setVisible (Boolean.TRUE);
                }
            }
        }
        else {
            
            audioOnlyRadioButton.setSelected (Boolean.TRUE);
            videoAudioRadioButton.setEnabled (Boolean.FALSE);
        }
        //</editor-fold>
        
        /*
            Starts the automatic recording process.
        
            If camera 0 (main-gui camera) is enabled then
            set configuration values
        */
        setCameraConfigs ();
    }
    
    /**
     * GUI constructor for additional cameras.
     * 
     * [TODO] 
     *      Unit test.
     * [/]
     * 
     * @param camNum
     */
    public LFDMS_GUI ( Integer camNum ) {
        
        initComponents ();
        
        CAMERA_NUMBER = camNum;
        
        /*
            camera database
        */
        if (CAMERAS_DETAILS_HM.containsKey (CAMERA_NUMBER.toString ())
            && CAMERAS_DETAILS_HM.get (CAMERA_NUMBER.toString ()).getProperty ("enabled").equals ("yes")) {
        
            DB_STUFF.INIT_CAMERA_DATABASE (CAMERA_NUMBER.toString ());
        }
        /*
            end camera database
        */
        
        setDefaultCloseOperation (DO_NOTHING_ON_CLOSE); 
        
        setTitle ("logFarmDMS :: " + CAMERA_NUMBER);
        
        setIconImage (FRAME_ICON);
        
        //<editor-fold defaultstate="collapsed" desc="Big screen instantiation.">
        /*
            [TODO]
                Big screen init appears twice, here and the other constructor.
                Can we move it to a method so constructors can call that.
            [/]
        */
        BIG_SCREEN = new LFDMS_BigScreen ( playToggleButton ); //, LFDMS_Status.getAudioPlaybackRowId ());
        BIG_SCREEN.setTitle ("logFarm DMS :: " + CAMERA_NUMBER + " :: live"); 
        /*
            set the initial location of bigScreen
        */
        BIG_SCREEN.setLocation (getX () + (getWidth()/2), 
                                getY () + (getHeight ()/2));
        //</editor-fold>
         
        //<editor-fold defaultstate="collapsed" desc="Export LFDMS_GUI instantiation.">
        /*
            [TODO]
                Export LFDMS_GUI init appears twice, here and the other constructor.
                Can we move it to a method so constructors can call that.
            [/]
        */
        EXPORT_GUI = new LFDMS_ExportGui (recordToggleButton,
                                          statusBarLabel,
                                          getDbStuff ().getCameraDatabaseConnection (),
    //                                    DB_STUFF.getAudioDatabaseConnection (),
                                          getCameraNumber ());

        EXPORT_GUI.setLocation (getX () + (getWidth()/2), 
                                getY () + (getHeight ()/2));
        //</editor-fold>

        /*
            Starts the automatic recording process.
        
            Set this gui's configuration values.
        */
        setCameraConfigs ();
    }

    /**
     * Starts the automatic recording process.
     * 
     * Various controls have their states set by this method. 
     * 
     * [TODO]
     *      Documentation.
     *      Required a unit test.
     *      Needs to return a value.       
     * [/]
     * 
     * [BUG]
     *      Looks like recording might be starting automatically even when 
     *      start_recording_at_startup != "yes" 
     * [/]
    */
    final public void setCameraConfigs () {
        
        /*
            Make sure we have configuration details for this camera number
            and the configuration says this camera should be enabled.
        */
        if ( CAMERAS_DETAILS_HM.containsKey (getCameraNumber ().toString ())
             && CAMERAS_DETAILS_HM.get (getCameraNumber ().toString ()).getProperty ("enabled").equals ("yes") ) {

            if (CAMERAS_DETAILS_HM.get (CAMERA_NUMBER.toString ()).getProperty ("start_recording_at_startup").equals ("yes")) {
                
                getRecordToggleButton ().doClick ();
            }
            
            if (CAMERAS_DETAILS_HM.get (getCameraNumber ().toString ()).getProperty ("iconify_at_start").equals ("yes")) {
                
                this.setState (JFrame.ICONIFIED);
            }
            
            if (CAMERAS_DETAILS_HM.get (getCameraNumber ().toString ()).getProperty ("hide").equals ("yes")) {
                
                this.setState (JFrame.ICONIFIED);
                
                TIMERS.getHideTimer ().schedule (TIMERS.GET_NEW_HIDE_TIMERTASK (), 1000);
            } 
            
            if (CAMERAS_DETAILS_HM.get (getCameraNumber ().toString ()).getProperty ("mute_playback_by_default").equals ("yes")) {
                
                /*
                    [TODO]
                        Need an audio mute mechanism.
                    [/]
                */
                setTitle (getTitle () + " :: muted");
            }
            
            if (CAMERAS_DETAILS_HM.get (getCameraNumber ().toString ()).getProperty ("record_audio_only_by_default").equals ("yes")) {
                
                /*
                    [STRANGENESS]
                
                        Audio only from a webcam's microphone.
                        
                        If we are recording audio from a USB microphone, then
                        it may not work until at least a single image is pulled 
                        from the camera. Perhaps this action sparks the webcam's
                        microphone into aciton.
                    [/]
                */
                final BufferedImage BI = cvQueryFrame ((CvCapture) CAMERAS_HM.get (getCameraNumber ())).getBufferedImage ();
                
                audioOnlyRadioButton.doClick ();
            }
            
            /*
                [BUG]
                    Needs to take into consideration:
            
                    CAMERA_NUMBER.toString ()).getProperty ("start_recording_at_startup")
                [/]
            
                Start capture/live-feed timer.
            */
            TIMERS.getCaptureTimer ().schedule (TIMERS.getNewCaptureTimerTask (), 250, 250);
            /*
                End capture timers.
            */
            
            /*
                [TODO]
                    Documentation: Explain why we do this here.
                [/]
            */
            setSliderMaximumValue ();
        }
        /*
            The configuration says don't enabled the controls on this LFDMS_GUI.
        */
        else {
            
            recordToggleButton.setSelected (Boolean.FALSE);
            
            recordToggleButton.setIcon (new javax.swing.ImageIcon (getClass ().getResource ("/res/media-record.png")));
            
            if (!statusBarLabel.getText ().equals (" not recording")) statusBarLabel.setText (" not recording");
            
            recordToggleButton.setEnabled (Boolean.FALSE);
            videoAudioRadioButton.setEnabled (Boolean.FALSE);
            audioOnlyRadioButton.setEnabled (Boolean.FALSE);
            playbackSlider.setEnabled (Boolean.FALSE);
            playToggleButton.setEnabled (Boolean.FALSE);
            stepBackButton.setEnabled (Boolean.FALSE);
            stepForwardButton.setEnabled (Boolean.FALSE);
            exportButton.setEnabled (Boolean.FALSE);
            
            BIG_SCREEN.getImageLabel ().setText ("<html><bod><center>if you can see this text<br>then the camera has been disabled in:<br><b>configuration.json</b><center></body></html>"); 
            
            setState (JFrame.ICONIFIED); 
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        recordPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        recordToggleButton = new javax.swing.JToggleButton();
        liveImageLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        audioOnlyRadioButton = new javax.swing.JRadioButton();
        videoAudioRadioButton = new javax.swing.JRadioButton();
        playbackPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        playbackDateLabel = new javax.swing.JLabel();
        playbackSlider = new javax.swing.JSlider();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        playToggleButton = new javax.swing.JToggleButton();
        jPanel7 = new javax.swing.JPanel();
        stepBackButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        stepForwardButton = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        exportButton = new javax.swing.JButton();
        statusBarLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("logFarm DMS");
        setMinimumSize(new java.awt.Dimension(300, 240));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        recordPanel.setInheritsPopupMenu(true);
        recordPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        recordPanel.setPreferredSize(new java.awt.Dimension(10, 85));
        recordPanel.setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(51, 51, 51));
        jPanel1.setInheritsPopupMenu(true);
        jPanel1.setMinimumSize(new java.awt.Dimension(40, 20));
        jPanel1.setPreferredSize(new java.awt.Dimension(40, 10));

        recordToggleButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        recordToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/media-record.png"))); // NOI18N
        recordToggleButton.setToolTipText("start/stop recording");
        recordToggleButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 0)));
        recordToggleButton.setDoubleBuffered(true);
        recordToggleButton.setInheritsPopupMenu(true);
        recordToggleButton.setMinimumSize(new java.awt.Dimension(32, 32));
        recordToggleButton.setPreferredSize(new java.awt.Dimension(32, 32));
        recordToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recordToggleButtonActionPerformed(evt);
            }
        });
        jPanel1.add(recordToggleButton);

        recordPanel.add(jPanel1, java.awt.BorderLayout.WEST);

        liveImageLabel.setBackground(new java.awt.Color(102, 102, 102));
        liveImageLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 0)));
        liveImageLabel.setDoubleBuffered(true);
        liveImageLabel.setMinimumSize(new java.awt.Dimension(115, 80));
        liveImageLabel.setOpaque(true);
        liveImageLabel.setPreferredSize(new java.awt.Dimension(115, 80));
        liveImageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                liveImageLabelMouseClicked(evt);
            }
        });
        recordPanel.add(liveImageLabel, java.awt.BorderLayout.EAST);

        jPanel3.setBackground(new java.awt.Color(51, 51, 51));
        jPanel3.setInheritsPopupMenu(true);
        jPanel3.setMinimumSize(new java.awt.Dimension(10, 33));
        jPanel3.setPreferredSize(new java.awt.Dimension(140, 100));
        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        audioOnlyRadioButton.setBackground(new java.awt.Color(51, 51, 51));
        buttonGroup1.add(audioOnlyRadioButton);
        audioOnlyRadioButton.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        audioOnlyRadioButton.setForeground(new java.awt.Color(255, 204, 0));
        audioOnlyRadioButton.setText("audio only");
        audioOnlyRadioButton.setToolTipText("record audio only");
        audioOnlyRadioButton.setDoubleBuffered(true);
        audioOnlyRadioButton.setInheritsPopupMenu(true);
        jPanel3.add(audioOnlyRadioButton);

        videoAudioRadioButton.setBackground(new java.awt.Color(51, 51, 51));
        buttonGroup1.add(videoAudioRadioButton);
        videoAudioRadioButton.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        videoAudioRadioButton.setForeground(new java.awt.Color(255, 204, 0));
        videoAudioRadioButton.setSelected(true);
        videoAudioRadioButton.setText("video and audio");
        videoAudioRadioButton.setToolTipText("record video and audio");
        videoAudioRadioButton.setDoubleBuffered(true);
        videoAudioRadioButton.setInheritsPopupMenu(true);
        jPanel3.add(videoAudioRadioButton);

        recordPanel.add(jPanel3, java.awt.BorderLayout.CENTER);

        getContentPane().add(recordPanel, java.awt.BorderLayout.NORTH);

        playbackPanel.setBackground(new java.awt.Color(51, 51, 51));
        playbackPanel.setInheritsPopupMenu(true);
        playbackPanel.setMinimumSize(new java.awt.Dimension(10, 89));
        playbackPanel.setPreferredSize(new java.awt.Dimension(10, 100));
        playbackPanel.setLayout(new java.awt.BorderLayout());

        jPanel4.setBackground(new java.awt.Color(51, 51, 51));
        jPanel4.setInheritsPopupMenu(true);
        jPanel4.setPreferredSize(new java.awt.Dimension(10, 60));
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 8));

        playbackDateLabel.setBackground(new java.awt.Color(51, 51, 51));
        playbackDateLabel.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        playbackDateLabel.setForeground(new java.awt.Color(255, 204, 0));
        playbackDateLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        playbackDateLabel.setText("---");
        playbackDateLabel.setDoubleBuffered(true);
        playbackDateLabel.setMinimumSize(new java.awt.Dimension(290, 25));
        playbackDateLabel.setOpaque(true);
        playbackDateLabel.setPreferredSize(new java.awt.Dimension(290, 25));
        jPanel4.add(playbackDateLabel);

        playbackSlider.setBackground(new java.awt.Color(51, 51, 51));
        playbackSlider.setMinimum(1);
        playbackSlider.setValue(1);
        playbackSlider.setDoubleBuffered(true);
        playbackSlider.setInheritsPopupMenu(true);
        playbackSlider.setPreferredSize(new java.awt.Dimension(290, 16));
        playbackSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                playbackSliderMouseReleased(evt);
            }
        });
        jPanel4.add(playbackSlider);

        playbackPanel.add(jPanel4, java.awt.BorderLayout.CENTER);

        jPanel5.setBackground(new java.awt.Color(51, 51, 51));
        jPanel5.setInheritsPopupMenu(true);
        jPanel5.setMinimumSize(new java.awt.Dimension(10, 60));
        jPanel5.setPreferredSize(new java.awt.Dimension(10, 40));
        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel6.setBackground(new java.awt.Color(51, 51, 51));
        jPanel6.setInheritsPopupMenu(true);
        jPanel6.setPreferredSize(new java.awt.Dimension(50, 50));

        playToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/media-playback-start.png"))); // NOI18N
        playToggleButton.setToolTipText("start / stop playback");
        playToggleButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 0)));
        playToggleButton.setDoubleBuffered(true);
        playToggleButton.setInheritsPopupMenu(true);
        playToggleButton.setMinimumSize(new java.awt.Dimension(32, 32));
        playToggleButton.setPreferredSize(new java.awt.Dimension(32, 32));
        playToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playToggleButtonActionPerformed(evt);
            }
        });
        jPanel6.add(playToggleButton);

        jPanel5.add(jPanel6, java.awt.BorderLayout.WEST);

        jPanel7.setBackground(new java.awt.Color(51, 51, 51));
        jPanel7.setInheritsPopupMenu(true);
        jPanel7.setMinimumSize(new java.awt.Dimension(10, 20));

        stepBackButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        stepBackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/go-previous.png"))); // NOI18N
        stepBackButton.setToolTipText("skip back one frame");
        stepBackButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 0)));
        stepBackButton.setDoubleBuffered(true);
        stepBackButton.setInheritsPopupMenu(true);
        stepBackButton.setMinimumSize(new java.awt.Dimension(32, 32));
        stepBackButton.setPreferredSize(new java.awt.Dimension(32, 32));
        stepBackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepBackButtonActionPerformed(evt);
            }
        });
        jPanel7.add(stepBackButton);

        jPanel2.setBackground(new java.awt.Color(51, 51, 51));
        jPanel2.setInheritsPopupMenu(true);
        jPanel2.setPreferredSize(new java.awt.Dimension(20, 10));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        jPanel7.add(jPanel2);

        stepForwardButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        stepForwardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/go-previous-rtl.png"))); // NOI18N
        stepForwardButton.setToolTipText("skip forward one frame");
        stepForwardButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 0)));
        stepForwardButton.setDoubleBuffered(true);
        stepForwardButton.setInheritsPopupMenu(true);
        stepForwardButton.setMinimumSize(new java.awt.Dimension(32, 32));
        stepForwardButton.setPreferredSize(new java.awt.Dimension(32, 32));
        stepForwardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepForwardButtonActionPerformed(evt);
            }
        });
        jPanel7.add(stepForwardButton);

        jPanel5.add(jPanel7, java.awt.BorderLayout.CENTER);

        jPanel8.setBackground(new java.awt.Color(51, 51, 51));
        jPanel8.setInheritsPopupMenu(true);
        jPanel8.setPreferredSize(new java.awt.Dimension(50, 10));

        exportButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        exportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/object-rotate-right.png"))); // NOI18N
        exportButton.setToolTipText("export video from current slider position");
        exportButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 0)));
        exportButton.setDoubleBuffered(true);
        exportButton.setInheritsPopupMenu(true);
        exportButton.setMinimumSize(new java.awt.Dimension(32, 32));
        exportButton.setPreferredSize(new java.awt.Dimension(32, 32));
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });
        jPanel8.add(exportButton);

        jPanel5.add(jPanel8, java.awt.BorderLayout.EAST);

        playbackPanel.add(jPanel5, java.awt.BorderLayout.SOUTH);

        getContentPane().add(playbackPanel, java.awt.BorderLayout.CENTER);

        statusBarLabel.setBackground(new java.awt.Color(51, 51, 51));
        statusBarLabel.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        statusBarLabel.setForeground(new java.awt.Color(255, 204, 0));
        statusBarLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusBarLabel.setText(" stopped");
        statusBarLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        statusBarLabel.setDoubleBuffered(true);
        statusBarLabel.setMinimumSize(new java.awt.Dimension(44, 30));
        statusBarLabel.setOpaque(true);
        statusBarLabel.setPreferredSize(new java.awt.Dimension(52, 30));
        getContentPane().add(statusBarLabel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/]
     * @param evt 
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        if (recordToggleButton.isSelected ()) recordToggleButton.doClick ();
        if (playToggleButton.isSelected ()) playToggleButton.doClick ();

        BIG_SCREEN.setVisible (Boolean.FALSE);
        EXPORT_GUI.setVisible (Boolean.FALSE);
        
        /*
            this gui is not the main-gui
        */
        if (getDefaultCloseOperation () == DO_NOTHING_ON_CLOSE) {
            
            setState (ICONIFIED);
        }
        /*
            the main LFDMS_GUI is closing so deactivate everything
        */
        else {
         
            LFDMS_Status.setCaptureAudio ( Boolean.FALSE );
//            getStatus ().setShowPlayBackImages ( Boolean.FALSE );
            
            if ( LFDMS_Timers.getRecordAudioTimer () != null) {

                LFDMS_Timers.getRecordAudioTimer ().cancel ();
            }

            if ( LFDMS_Timers.getAudioPlaybackTimer () != null ) {

                LFDMS_Timers.getAudioPlaybackTimer ().cancel ();
            }
            
            cleanUp ();
            
            /*
                loop through all GUIs and stop the timers
            */
            for (Enumeration<Integer> e = GUIS_HM.keys (); e.hasMoreElements (); ) {
                
                final Integer KEY = e.nextElement ();
                
                GUIS_HM.get (KEY).cleanUp ();
            }
            
            LFDMS_AudioInit.getLine ().stop ();
            LFDMS_AudioInit.getLine ().drain ();
            LFDMS_AudioInit.getLine ().close ();
        }
    }//GEN-LAST:event_formWindowClosing

    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    public void cleanUp () {
        
        LFDMS_Status.setAudioPlaybackOwner ( null ); 
//        LFDMS_Status.setAudioRecord ( Boolean.FALSE );
        
        if (TIMERS.getCaptureTimer () != null) {
            
            TIMERS.getCaptureTimer ().cancel ();
        }
        
        if (TIMERS.getHideTimer () != null) {
            
            TIMERS.getHideTimer ().cancel ();
        }
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private void liveImageLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_liveImageLabelMouseClicked

        if (playToggleButton.isSelected ()) {
            
            playToggleButton.doClick ();
        }
        
        if ( STATUS.getSliderHasBeenMoved () ) {

            STATUS.setSliderHasBeenMoved ( Boolean.FALSE );
        }
        
        /*
            the user has chosen to show live video
            so we will not display playback images
        */
//        getStatus ().setShowPlayBackImages ( Boolean.FALSE );

        BIG_SCREEN.setTitle ("logFarmDMS :: " + getCameraNumber () + " :: live");
        
        BIG_SCREEN.setVisible (true);
    }//GEN-LAST:event_liveImageLabelMouseClicked

    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private void recordToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordToggleButtonActionPerformed

        LFDMS_Status.setCaptureAudio ( testAudioKeepGoing () );
        
        /*
            User has just unselected the record button.
        */
        if ( !recordToggleButton.isSelected () ) {

            getRecordToggleButton ().setIcon (new javax.swing.ImageIcon (getClass ().getResource ("/res/media-record.png")));
            
            /*
                If non of the other GUIs are recording then we can stop audio
                recording.
            */
            if (!LFDMS_Status.getCaptureAudio ()) {
                
                LFDMS_Timers.getRecordAudioTimer ().cancel ();
            }
            
            getStatusBarLabel ().setText ( " not recording" );
        }
        /*
            User has started recording from a camera.
        */
        else {
            
            recordToggleButton.setIcon (new javax.swing.ImageIcon (getClass ().getResource ("/res/media-playback-stop.png")));
            
            /*
                If non of the other GUIs are recording then we can start audio
                recording.
            */
            if (!LFDMS_Status.getCaptureAudio ()) {
                
                LFDMS_Timers.getRecordAudioTimer ().schedule (LFDMS_Timers.GET_NEW_CAPTURE_AUDIO_TIMERTASK (), 0, AUDIO_DURATION_I);
            }
            
            /*
                Status label ascii spinner is set by captureTimerTask.
            */
        }
    }//GEN-LAST:event_recordToggleButtonActionPerformed

    private void stopGuiPlayback () {
        
        if ( !STATUS.getSliderHasBeenMoved () ) {

            STATUS.setSliderHasBeenMoved ( Boolean.TRUE );
        }
        
        if (!getStepBackButton ().isEnabled ()) {
            
            getStepBackButton ().setEnabled ( Boolean.TRUE );
        }
        
        if (!getStepForwardButton ().isEnabled ()) {
            
            getStepForwardButton ().setEnabled ( Boolean.TRUE );
        }
        
        if (!getExportButton ().isEnabled ()) {
            
            getExportButton ().setEnabled ( Boolean.TRUE );
        }
        
        LFDMS_PlayAudioOutputStream.stopPlay ();
            
        LFDMS_Timers.PlaybackAudioTimerTask.clearBuffers ();

        /*
            Change the playback button icon to the play-circle icon.
        */
        playToggleButton.setIcon (new javax.swing.ImageIcon (getClass ().getResource ("/res/media-playback-start.png")));

        /*
            Not likely since we always record audio and so if this
            button has changed state to unselected then we were recording
            up until this point.
        */
        if ( LFDMS_Status.getAudioPlaybackOwner () == null ) {

            /*
                do nothing
            */
            System.out.println ("playback owner is already null");
        }
        else if ( !LFDMS_Status.getAudioPlaybackOwner ().equals ( this ) ) {

            /*
                again do nothing since this is none of our business.
            */
            System.out.println ("playback owner is already another GUI");
        } 
        else {

            /*
                If there are other GUIs currently playing back then
                transfer play back ownership to the first found.

                This procedure will not stop audio playback timer but will
                start the playing back from the position of the new owner's
                slider.
            */
            autoTransferAudioPlaybackOwnership ();

            /*
                if audio owner equals 'this' then we need to cancel the
                audio playback
            */
            if (LFDMS_Status.getAudioPlaybackOwner () == null) {

                LFDMS_Timers.getAudioPlaybackTimer ().cancel ();
            }
            else if ( LFDMS_Status.getAudioPlaybackOwner ().equals ( this ) ) {

                LFDMS_Timers.getAudioPlaybackTimer ().cancel ();

                LFDMS_Status.setAudioPlaybackOwner ( null );
            }
            else {

                /*
                    foo
                */
            }
        }
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private void playToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playToggleButtonActionPerformed

        /*
            Unselected / stopped playback.
        */
        if (!playToggleButton.isSelected ()) {
            
            stopGuiPlayback ();
        }
        /*
            Playback button is selected.
        */
        else {

            showBigScreen ();
            
            /*
                Change the playback button icon to the stop-square icon.
            */
            playToggleButton.setIcon (new javax.swing.ImageIcon (getClass ().getResource ("/res/media-playback-stop.png")));   
            
            if ( LFDMS_Status.getAudioPlaybackOwner () == null ) {
                
                LFDMS_Timers.setAudioPlaybackTimer (); 
                 
                LFDMS_Timers.getAudioPlaybackTimer ().schedule (LFDMS_Timers.getNewPlaybackAudioTimerTask (), 0, AUDIO_DURATION_I);
                
                LFDMS_Status.setAudioPlaybackOwner ( this );
            }
            
            if ( getStepBackButton ().isEnabled () ) {
            
                getStepBackButton ().setEnabled ( Boolean.FALSE );
            }

            if ( getStepForwardButton ().isEnabled () ) {

                getStepForwardButton ().setEnabled ( Boolean.FALSE );
            }

            if ( getExportButton ().isEnabled ()) {

                getExportButton ().setEnabled ( Boolean.FALSE );
            }
        }
    }//GEN-LAST:event_playToggleButtonActionPerformed

    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private void playbackSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playbackSliderMouseReleased

        if (playToggleButton.isSelected ()) {
            
            playToggleButton.doClick ();
        }
        
        if ( !STATUS.getSliderHasBeenMoved () ) {
        
            STATUS.setSliderHasBeenMoved ( Boolean.TRUE ); 
        }
        
        TIMERS.displaySinglePlaybackImage ();
        
        showBigScreen (); 
    }//GEN-LAST:event_playbackSliderMouseReleased

    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private void stepBackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepBackButtonActionPerformed

        if ( !STATUS.getSliderHasBeenMoved () ) {

            STATUS.setSliderHasBeenMoved ( Boolean.TRUE );
        }
        
        showBigScreen ();
        
        if (! this.getPlayToggleButton ().isSelected ()) {
//            && playbackSlider.getValue () >= 1) {
            
            playbackSlider.setValue (( playbackSlider.getValue () > 1 ) ? playbackSlider.getValue () - 1 : 1);
            
            TIMERS.displaySinglePlaybackImage ();
        }
    }//GEN-LAST:event_stepBackButtonActionPerformed

    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private void stepForwardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepForwardButtonActionPerformed
        
        if ( !STATUS.getSliderHasBeenMoved () ) {

            STATUS.setSliderHasBeenMoved ( Boolean.TRUE );
        }
        
        showBigScreen ();
        
        if (! this.getPlayToggleButton ().isSelected ()
            && playbackSlider.getValue () < playbackSlider.getMaximum ()) {
            
            playbackSlider.setValue (playbackSlider.getValue () + 1);
            
            TIMERS.displaySinglePlaybackImage ();
        }
    }//GEN-LAST:event_stepForwardButtonActionPerformed

    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        
        /*
            [TODO]
                Would prefer we maintain status and states in a sparate
                class.
            [/]
        */
        if (!playbackDateLabel.getText ().equals ("---")) {
            
            EXPORT_GUI.setDate (playbackDateLabel.getText ()); 
            EXPORT_GUI.setVisible (Boolean.TRUE); 
        }
        else {
            
            JOptionPane.showMessageDialog (this, "Please use the slider to choose a date.", "No date selected", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_exportButtonActionPerformed

    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private void showBigScreen () {
        
        if (!BIG_SCREEN.isVisible ()) {
            
            setSliderMaximumValue ();

            BIG_SCREEN.setTitle ("logFarmDMS :: " + getCameraNumber () + " :: playback");
            
            BIG_SCREEN.setVisible (Boolean.TRUE);
        } 
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main (String args[]) {

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels ()) {
                if ("Nimbus".equals (info.getName ())) {
                    javax.swing.UIManager.setLookAndFeel (info.getClassName ());
                    break;
                }
            }
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            
            System.err.println (ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater (new RunnableImpl ());
    }

    /**
     * [TODO]
     *      Documentation.
     * [/] 
     *
     * This should only be called when we know a database connection is open
     * 
     * @return Boolean for unit testing        
    */
    final public Boolean setSliderMaximumValue () {
    
        final ResetSliderValues RESET_SLIDER_VALUES = new ResetSliderValues ();
        RESET_SLIDER_VALUES.start ();
        
        return Boolean.TRUE;
    }

    private static class RunnableImpl implements Runnable {

        public RunnableImpl () {
        }

        @Override
        public void run () {

            lfdmsHeadGui = new LFDMS_GUI ();
            lfdmsHeadGui.setVisible (true);
        }
    }
 
    /**
     * Are any of the GUIs' Record buttons selected.
     * 
     * [TODO]
     *      Documentation.
     *      Move to a separate class.
     *      Unit test, return value.
     * [/] 
     */
    private Boolean testAudioKeepGoing () {
        
        Boolean b = Boolean.FALSE;
        
        for (Enumeration<Integer> e = GUIS_HM.keys (); e.hasMoreElements (); ) {

            final Integer KEY = e.nextElement ();

            if ( !GUIS_HM.get (KEY).equals ( this )
                 && GUIS_HM.get (KEY).recordToggleButton.isSelected () ) {

                b = Boolean.TRUE;

                break;
            }
        }
        
        return b;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Move to a separate class.
     *      Unit test, return value.
     * [/] 
     */
    private static void autoTransferAudioPlaybackOwnership () {
        
        LFDMS_Status.setAudioPlaybackOwner ( null );
        
        if (LFDMS_GUI.lfdmsHeadGui.getPlayToggleButton ().isSelected ()) {
        
            LFDMS_Status.setAudioPlaybackOwner ( lfdmsHeadGui ); 
        }
        else {
            
            for (Enumeration<Integer> e = GUIS_HM.keys (); e.hasMoreElements (); ) {

                final Integer KEY = e.nextElement ();

                if ( GUIS_HM.get (KEY).playToggleButton.isSelected () ) {

                    LFDMS_Status.setAudioPlaybackOwner ( GUIS_HM.get (KEY) ); 

                    break;
                }    
            }
        }
    }
     
    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * 
     *      This can be moved to a separate class.
     * [/] 
     * 
     * @param s - String that could be a number.
     * @return Boolean - true if String only contains numerical characters.
     */
    public static Boolean isNumeric (String s) {

        if (s.length () <= 0) {

            return Boolean.FALSE;
        }

        for (int i = 0; i < s.length (); i++) {

            if (!Pattern.matches ("[0-9]", Character.toString (s.charAt (i)))) { 

                return Boolean.FALSE;
            }
        }

        return true;
    }
   
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return 
     */
    public javax.swing.JRadioButton getVideoAudioRadioButton () {
        
        return videoAudioRadioButton;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return the CAMERA_NUMBER
     */
    public Integer getCameraNumber () {
        
        return CAMERA_NUMBER;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return 
     */
    public javax.swing.JLabel getLiveImageLabel () {
        
        return liveImageLabel;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return 
     */
    public LFDMS_BigScreen getBigScreen () {
        
        return BIG_SCREEN;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return 
     */
    public javax.swing.JToggleButton getRecordToggleButton () {
        
        return recordToggleButton;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return 
     */
    public javax.swing.JLabel getStatusBarLabel () {
        
        return statusBarLabel;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return 
     */
    public javax.swing.JSlider getPlaybackSlider () {
        
        return playbackSlider;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return 
     */
    public javax.swing.JToggleButton getPlayToggleButton () {
        
        return playToggleButton;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return 
     */
    public javax.swing.JLabel getPlaybackDateLabel () {
    
        return playbackDateLabel;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return the DB_STUFF
     */
    public LFDMS_DbInit getDbStuff () {
     
        return DB_STUFF;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return 
     */
    public javax.swing.JButton getStepBackButton () {
        
        return stepBackButton;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return 
     */
    public javax.swing.JButton getStepForwardButton () {
        
        return stepForwardButton;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return 
     */
    public javax.swing.JButton getExportButton () {
        
        return exportButton;
    }
    
    /**
     * @return the STATUS
     */
    public LFDMS_Status getStatus () {
     
        return STATUS;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * 
     * @param key
     * @param value 
     */
    public void setProperty (String key,
                             Object value) {
        
        PROPERTIES_HM.put (key, value);
    }
    
    public Object getPropertyValue (String key) {
        
        return PROPERTIES_HM.get (key);
    }
    
    private class ResetSliderValues extends Thread {

        @Override
        public void run () {

            try {

                if ( getDbStuff ().getCameraDatabaseConnection ().isClosed () ) {

                    getDbStuff ().setCameraDatabaseConnection ( DriverManager.getConnection ( "jdbc:sqlite:"
                            + CAMERAS_DETAILS_HM.get ( getCameraNumber ().toString () ).getProperty ( "db_location" )
                            + System.getProperty ( "file.separator" )
                            + "logFarmDMS_" + getCameraNumber () + ".db" ) );
                }
                
                /*
                    We reuse sql, statement and resultSet so we don't make them final.
                 */
                String sql = "select rowid from fileData order by rowid desc limit 1";

                Statement statement;
                statement = getDbStuff ().getCameraDatabaseConnection ().createStatement ();

                ResultSet resultSet;
                resultSet = statement.executeQuery ( sql );

                Integer rowCount = 0;

                while ( resultSet.next () ) {

                    rowCount = resultSet.getInt ( 1 );
                }

                resultSet.close ();

                statement.closeOnCompletion ();

                //
                sql = "select rowid from fileData order by rowid asc limit 1";

                statement = getDbStuff ().getCameraDatabaseConnection ().createStatement ();

                resultSet = statement.executeQuery ( sql );

                Integer minimum = 1;

                while ( resultSet.next () ) {

                    minimum = resultSet.getInt ( 1 );
                }

                resultSet.close ();

                statement.closeOnCompletion ();

                //
                playbackSlider.setMinimum ( minimum );
                playbackSlider.setMaximum ( rowCount );
            }
            catch ( SQLException | NullPointerException ex ) {

                System.err.println ( "LFDMS_GUI.ResetSliderValues " + ex.getMessage () );
            }
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton audioOnlyRadioButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton exportButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JLabel liveImageLabel;
    private javax.swing.JToggleButton playToggleButton;
    private javax.swing.JLabel playbackDateLabel;
    private javax.swing.JPanel playbackPanel;
    private javax.swing.JSlider playbackSlider;
    private javax.swing.JPanel recordPanel;
    private javax.swing.JToggleButton recordToggleButton;
    private javax.swing.JLabel statusBarLabel;
    private javax.swing.JButton stepBackButton;
    private javax.swing.JButton stepForwardButton;
    private javax.swing.JRadioButton videoAudioRadioButton;
    // End of variables declaration//GEN-END:variables
   
}
