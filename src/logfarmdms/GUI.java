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

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.sql.*;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import static org.bytedeco.javacpp.opencv_core.CV_FONT_HERSHEY_SCRIPT_COMPLEX;
import org.bytedeco.javacpp.opencv_core.CvFont;
import static org.bytedeco.javacpp.opencv_core.cvInitFont;
import static org.bytedeco.javacpp.opencv_highgui.CV_CAP_PROP_FRAME_HEIGHT;
import static org.bytedeco.javacpp.opencv_highgui.CV_CAP_PROP_FRAME_WIDTH;
import org.bytedeco.javacpp.opencv_highgui.CvCapture;
import static org.bytedeco.javacpp.opencv_highgui.cvCreateCameraCapture;
import static org.bytedeco.javacpp.opencv_highgui.cvQueryFrame;
import static org.bytedeco.javacpp.opencv_highgui.cvSetCaptureProperty;
import utilities.Constants;
import static utilities.Constants.CAMERAS_DETAILS_HM;
import static utilities.Constants.CAMERAS_HM;
import utilities.OpenConfiguration;
import utilities.PlayAudio;

/**
 * @author tuxjsmith
 */
public class GUI extends javax.swing.JFrame implements Constants {

    /*
        [TODO] 
            Replace variables with a PROPERTIES_HM collection.
        [/]
    */
    
    private final Object FONT = new CvFont ();
    private final Timer CAPTURE_TIMER = new Timer (), 
                        RECORD_AUDIO_TIMER = new Timer (), 
                        AUDIO_PLAYBACK_TIMER = new Timer (),
                        HIDE_TIMER = new Timer ();
    private final BigScreen BIG_SCREEN;
    private final ExportGui EXPORT_GUI;
    /*
        Identifies which camera this GUI should operate. The default GUI
        will operate camera: 0 (zero)
    */
    private final Integer CAMERA_NUMBER;
    
    /*
        [TODO] 
            Can playback toggle button's state be used instead of: showPlayBackImages_b
        [/]
    */
    private Boolean showPlayBackImages_b = Boolean.FALSE, sliderHasBeenMoved_b = Boolean.FALSE;
    
    /*
        [TODO] 
            Move this value to either PROPERTIES_HM or a separate 'status' 
            class. 
    
            Audio is always recoded. Although The user has the option to 
            record audio, there isn't an option to record video only.
    
            The current testAudioKeepGoing is a bit clunky I think;
            could do better.
        [/]
    
        testAudioKeepGoing () sets captureAudio_b
    */
    private static Boolean captureAudio_b = Boolean.TRUE;
    
    /*
        For audio recording.
    
        [TODO] 
            Investigate why these finals aren't static and located in 
            Constants.java ?
    
            A separate RecordAudio class would be useful. 
            We do have a PlayAudio class.
        [/]
    */
    private final AudioFormat AUDIO_FORMAT = getFormat ();
    private final DataLine.Info CAPTURE_DATA_LINE_INFO = new DataLine.Info (TargetDataLine.class, AUDIO_FORMAT);
    private TargetDataLine line = null;
    
    /*
        [TODO] 
    
            Clarify how many database connections we should expect when
            the application is running. 
              
            - one for the audio
            - one for all cameras even though we read and right to many
              tables.
    
              Need to remind myself how SQLite works; one connection per
              table ? Are SQLite databases and tables synonymous ?
    
              If only one connection required can we make the connection 
              declarations final ?
    
              INIT_CAMERA_DATABASE () is where we use these declarations and
              it looks like databases and tables are the same thing from SQLite's
              point of view.
    
              That's cool but I do think database stuff should have a class of
              it's own.
        [/]
    
        Declared here so that playToggleButton can stop and start it
        used by PlaybackAudioTimerTask
    */
    private Connection camera_database_connection; //audio_database_connection;
    private Connection audio_database_connection;
    
    /*
        [TODO] 
            I think these would better serve inside a 'status' class or
            PROPERTIES_HM.
        [/]
        
        If either of these are true then the audio database connection is not 
        closed, if both are false then it is.
    */
    private Boolean audioPlayback_b = Boolean.FALSE, 
                    audioRecord_b = Boolean.FALSE;
    private Integer audioPlaybackRowId_i = -1;
    
    /*
        [TODO] 
            These could be candidates for Constants.java.
        [/]
    */
    private java.net.URL url = this.getClass ().getResource ("icon_64.png");
    private javax.swing.ImageIcon ii = new javax.swing.ImageIcon (url);
    private java.awt.Image frameIcon = ii.getImage ();
    
    /**
     * Default GUI constructor.
     * 
     * [TODO] 
     *      Documentation.
     *      Unit test.
     * [/]
     */
    public GUI () {
        
        initComponents ();
        
        /*
            Get configuration details from the configuration file (JSON).
        
            Only main-gui because there's only one configuration file.
        */
        OpenConfiguration.openConfiguration ();
        
        //<editor-fold defaultstate="collapsed" desc="Record-audio initialisation.">
        /*
            Only main-gui because we only record from a single microphone.
        */
        try {
            
            line = (TargetDataLine) AudioSystem.getLine (CAPTURE_DATA_LINE_INFO);

            line.open (AUDIO_FORMAT);

            line.start ();
        }
        catch (IllegalArgumentException | LineUnavailableException  | NullPointerException ex) {
         
            System.err.println ("GUI " + ex.getMessage ());
        }
        //</editor-fold>
        
        CAMERA_NUMBER = 0;
        
        //<editor-fold defaultstate="collapsed" desc="Camera collection.">
        /*
            Populated by: OpenConfiguration.openConfiguration
        */
        if (CAMERAS_DETAILS_HM.containsKey (CAMERA_NUMBER.toString ())
            && CAMERAS_DETAILS_HM.get (CAMERA_NUMBER.toString ()).getProperty ("enabled").equals ("yes")) {
        
            INIT_CAMERA_DATABASE ();
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
        INIT_AUDIO_DATABASE ();
        
        /*
            [BUG]
                Trouble is, if there aren't any video frames then the audio will 
                not be played back; might be able to export it though.
            [/]
        */
        RECORD_AUDIO_TIMER.schedule (new captureAudioTimerTask (), 500, 1000);
        //</editor-fold>
        
        setTitle ("logFarmDMS :: " + CAMERA_NUMBER);
        
        setIconImage (frameIcon);
        
        /*
            big screen
        */
        BIG_SCREEN = new BigScreen (playToggleButton, audioPlaybackRowId_i);
        BIG_SCREEN.setTitle ("logFarm DMS :: " + CAMERA_NUMBER + " :: live"); 
        /*
            set the initial location of bigScreen
        */
        BIG_SCREEN.setLocation (getX () + (getWidth()/2), 
                                getY () + (getHeight ()/2));
        /*
            end big screen
        */
         
        /*
            export gui
        */
        EXPORT_GUI = new ExportGui (recordToggleButton,
                                    statusBarLabel,
                                    camera_database_connection,
                                    audio_database_connection,
                                    CAMERA_NUMBER);

        EXPORT_GUI.setLocation (getX () + (getWidth()/2), 
                                getY () + (getHeight ()/2));
        /*
            end export gui
        */

        /*
            camera globals
        */
        cvInitFont ((CvFont) FONT, CV_FONT_HERSHEY_SCRIPT_COMPLEX, 0.5, 0.5);
        /*
            end camera globals
        */

        /*
            loop to create a collection of cameras connected to the computer
            loop count is set in configuration.json
        */
        for (int i = 0; i < Integer.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("number_of_cameras")); i++) {

            if (CAMERAS_DETAILS_HM.containsKey (Integer.toString (i))
                && CAMERAS_DETAILS_HM.get (Integer.toString (i)).getProperty ("enabled").equals ("yes")) {
            
                final Object CAMERA = cvCreateCameraCapture (i);

                if (CAMERA != null) {

                    CAMERAS_HM.put (i, CAMERA);

                    /*
                        height and width
                    */ 
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
                    /*
                        end height and width
                    */
                }
            }
        }

        /*
            spawn additional guis
        */
        if (!CAMERAS_HM.isEmpty ()) {

            for (Enumeration<Integer> e = CAMERAS_HM.keys (); e.hasMoreElements (); ) {

                final Integer KEY = e.nextElement ();

                if (KEY > 0) {

                    GUI gui = new GUI (KEY, audio_database_connection);
                    GUIS_HM.put (KEY, gui);

                    /*
                        set GUI screen location
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
        /*
            end spawn additional guis
        */
        
        /*
            if camera 0 (main-gui camera) is enabled
        
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
     * @param audioDbConnection 
     */
    public GUI (Integer camNum,
                Connection audioDbConnection) {
        
        initComponents ();
        
        CAMERA_NUMBER = camNum;
        
        /*
            camera database
        */
        if (CAMERAS_DETAILS_HM.containsKey (CAMERA_NUMBER.toString ())
            && CAMERAS_DETAILS_HM.get (CAMERA_NUMBER.toString ()).getProperty ("enabled").equals ("yes")) {
        
            INIT_CAMERA_DATABASE ();
        }
        /*
            end camera database
        */
        
        /*
            sub-GUI so use main-gui's audio db connection
        */
        audio_database_connection = audioDbConnection;
        /*
            end init audio db
        */
        
        setDefaultCloseOperation (DO_NOTHING_ON_CLOSE); 
        
        setTitle ("logFarmDMS :: " + CAMERA_NUMBER);
        
        setIconImage (frameIcon);
        
        /*
            big screen
        */
        BIG_SCREEN = new BigScreen (playToggleButton, audioPlaybackRowId_i);
        BIG_SCREEN.setTitle ("logFarm DMS :: " + CAMERA_NUMBER + " :: live"); 
        /*
            set the initial location of bigScreen
        */
        BIG_SCREEN.setLocation (getX () + (getWidth()/2), 
                                getY () + (getHeight ()/2));
        /*
            end big screen
        */
        
        /*
            export gui
        */
        EXPORT_GUI = new ExportGui (recordToggleButton,
                                    statusBarLabel,
                                    camera_database_connection,
                                    audio_database_connection,
                                    CAMERA_NUMBER);
        
        EXPORT_GUI.setLocation (getX () + (getWidth()/2), 
                                getY () + (getHeight ()/2));
        /*
            end export gui
        */

        /*
            set sub-gui configuration values
        */
        setCameraConfigs ();
    }

    /**
    *   [TODO]
    *       Required a unit test.
    *       Needs to return a value.       
    *   [/]
    * 
    * @return void
    */
    final public void setCameraConfigs () {
        
        if (CAMERAS_DETAILS_HM.containsKey (CAMERA_NUMBER.toString ())
            && CAMERAS_DETAILS_HM.get (CAMERA_NUMBER.toString ()).getProperty ("enabled").equals ("yes")) {

            if (!CAMERAS_DETAILS_HM.get (CAMERA_NUMBER.toString ()).getProperty ("start_recording_at_startup").equals ("yes")) {
                
                recordToggleButton.setSelected (Boolean.FALSE);
            
                recordToggleButton.setIcon (new javax.swing.ImageIcon (getClass ().getResource ("/logfarmdms/media-record.png")));

                if (!statusBarLabel.getText ().equals (" not recording")) statusBarLabel.setText (" not recording");
            }
            
            if (CAMERAS_DETAILS_HM.get (CAMERA_NUMBER.toString ()).getProperty ("iconify_at_start").equals ("yes")) {
                
                this.setState (JFrame.ICONIFIED);
            }
            
            if (CAMERAS_DETAILS_HM.get (CAMERA_NUMBER.toString ()).getProperty ("hide").equals ("yes")) {
                
                this.setState (JFrame.ICONIFIED);
                
                HIDE_TIMER.schedule (new HideTimerTask (), 1000);
            }
            
            if (CAMERAS_DETAILS_HM.get (CAMERA_NUMBER.toString ()).getProperty ("mute_playback_by_default").equals ("yes")) {
                
                BIG_SCREEN.getMuteAudioMenuItem ().doClick ();
                
                setTitle (getTitle () + " :: muted");
            }
            
            if (CAMERAS_DETAILS_HM.get (CAMERA_NUMBER.toString ()).getProperty ("record_audio_only_by_default").equals ("yes")) {
                
                /*
                    if this is not called and audio recording is using a USB camera 
                    then the microphone may not work until at least a single image
                    is pulled from it, perhaps this sparks the webcam's microphone
                    into action
                */
                BufferedImage bi = cvQueryFrame ((CvCapture) CAMERAS_HM.get (CAMERA_NUMBER)).getBufferedImage ();
                
                audioOnlyRadioButton.doClick ();
            }
            
            /*
                start record and playback timers
            */
            CAPTURE_TIMER.schedule (new CaptureTimerTask (), 250, 250);

            AUDIO_PLAYBACK_TIMER.schedule (new PlaybackAudioTimerTask (), 5000, 5000);
            /*
                end record and playback timers
            */
            
            setSliderMaximumValue ();
        }
        /*
            the main-gui has not been enabled by the configuration file
        */
        else {
            
            recordToggleButton.setSelected (Boolean.FALSE);
            
            recordToggleButton.setIcon (new javax.swing.ImageIcon (getClass ().getResource ("/logfarmdms/media-record.png")));
            
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
            
            /*
                because this is the main-gui iconify it
            */
            setState (JFrame.ICONIFIED); 
        }
    }
    
    /**
     * [TODO] 
     *      Move to a separate class.
     *  
     *      Documentation.
     * [/]
     * 
     * @return Boolean for unit testing 
    */
    final public Boolean INIT_CAMERA_DATABASE () {
        
        try {

            /*
                Make sure org.sqlite.JDBC is in the class path
                and throw a ClassNotFoundException if it isn't.
            
                For development it is located here: 
                opencv_for_logfarmDMS/sqlite-jdbc-3.8.11.2.jar
            */
            Class.forName ("org.sqlite.JDBC");

            if (!new File ( CAMERAS_DETAILS_HM.get(CAMERA_NUMBER.toString ()).getProperty ("db_location") + System.getProperty ("file.separator") + "logFarmDMS_" + CAMERA_NUMBER + ".db" ).exists ()) {

                camera_database_connection = DriverManager.getConnection ("jdbc:sqlite:" + CAMERAS_DETAILS_HM.get(CAMERA_NUMBER.toString ()).getProperty ("db_location") + System.getProperty ("file.separator") + "logFarmDMS_" + CAMERA_NUMBER + ".db" );

                final Statement STATEMENT;
                STATEMENT = camera_database_connection.createStatement ();
                STATEMENT.setQueryTimeout (30);  // set timeout to 30 sec.

                STATEMENT.executeUpdate ("create table fileData (date text,"
                                                              + "fileBytes blob)");

                STATEMENT.closeOnCompletion ();
            }
            else {

                camera_database_connection = DriverManager.getConnection ("jdbc:sqlite:" +
                                CAMERAS_DETAILS_HM.get(CAMERA_NUMBER.toString ()).getProperty ("db_location") + System.getProperty ("file.separator") + 
                                "logFarmDMS_" + CAMERA_NUMBER + ".db" );
                
//                setSliderMaximumValue ();
            }
        }
        catch (ClassNotFoundException | SQLException  | NullPointerException ex) {

            System.err.println ("initCameraDatabase " + ex.getMessage ());
            
            JOptionPane.showMessageDialog (this, "<html><body>"
                    + "Sorry, the database file path is wrong.<br><br>"
                    + "This path is set in <b>configuration.json</b>.<br><br>"
                    + "Solution: edit or delete <b>configuration.json</b><br> "
                    + "and try again."
                    + "</body></html>", "Database path is wrong", JOptionPane.ERROR_MESSAGE);
            
            System.exit (0); 
            
            return Boolean.FALSE;
        }
        
        return Boolean.TRUE;
    }
    
    /**
     * [TODO]
     *      Documentation.
     * [/]
     * 
     * @return Boolean for unit testing 
     */
    final public Boolean INIT_AUDIO_DATABASE () {
        
        try {
    
            /*
                calling this seems to simply make sure the class in the path
                and throws a ClassNotFoundException if it isn't
            */
            Class.forName ("org.sqlite.JDBC");
            /*
                create an audio database if one does not exist
            */
            if (!new File (CAMERAS_DETAILS_HM.get("global").getProperty ("audio_db_location") + System.getProperty ("file.separator") + "logFarmDMSaudio.db").exists ()) {

                audio_database_connection = DriverManager.getConnection ("jdbc:sqlite:" + CAMERAS_DETAILS_HM.get("global").getProperty ("audio_db_location") + System.getProperty ("file.separator") + "logFarmDMSaudio.db");

                final Statement STATEMENT;
                STATEMENT = audio_database_connection.createStatement ();
                STATEMENT.setQueryTimeout (30);  // set timeout to 30 sec.

                STATEMENT.executeUpdate ("create table fileData (date text,"
                                                              + "fileBytes blob)");

                STATEMENT.closeOnCompletion ();
            }
            else {
                
                audio_database_connection = DriverManager.getConnection ("jdbc:sqlite:" + CAMERAS_DETAILS_HM.get("global").getProperty ("audio_db_location") + System.getProperty ("file.separator") + "logFarmDMSaudio.db");
            }
            /*
                end create audio database
            */
        }
        catch (ClassNotFoundException | SQLException  | NullPointerException ex) {

            System.err.println ("initAudioDatabase " + ex.getMessage ());
            
            return Boolean.FALSE;
        }
        
        return Boolean.TRUE;
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
        setMaximumSize(new java.awt.Dimension(300, 240));
        setMinimumSize(new java.awt.Dimension(300, 240));
        setPreferredSize(new java.awt.Dimension(300, 240));
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
        recordToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/logfarmdms/media-playback-stop.png"))); // NOI18N
        recordToggleButton.setSelected(true);
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

        playToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/logfarmdms/media-playback-start.png"))); // NOI18N
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
        stepBackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/logfarmdms/go-previous.png"))); // NOI18N
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
        stepForwardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/logfarmdms/go-previous-rtl.png"))); // NOI18N
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
        exportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/logfarmdms/object-rotate-right.png"))); // NOI18N
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
            the main GUI is closing so deactivate everything
        */
        else {
         
            captureAudio_b = Boolean.FALSE;
            showPlayBackImages_b = Boolean.FALSE;
            
            cleanUp ();
            
            /*
                loop through all GUIs and stop the timers
            */
            for (Enumeration<Integer> e = GUIS_HM.keys (); e.hasMoreElements (); ) {
                
                final Integer KEY = e.nextElement ();
                
                GUIS_HM.get (KEY).cleanUp ();
            }
            
            try {
            
                if (audio_database_connection != null) {
                    
                    audio_database_connection.close ();
                }
                
                /*
                    capture timer closes camera_database_connection
                */
            }
            catch (SQLException  | NullPointerException ex) {
                
                System.err.println ("formWindowClosing " + ex.getMessage ());
            }
            
            line.stop ();
            line.drain ();
            line.close ();
        }
    }//GEN-LAST:event_formWindowClosing

    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    public void cleanUp () {
        
        audioPlayback_b = Boolean.FALSE; 
        audioRecord_b = Boolean.FALSE;
        
        if (CAPTURE_TIMER != null) CAPTURE_TIMER.cancel ();
        if (RECORD_AUDIO_TIMER != null) RECORD_AUDIO_TIMER.cancel ();
        if (AUDIO_PLAYBACK_TIMER != null) AUDIO_PLAYBACK_TIMER.cancel ();
        if (HIDE_TIMER != null) HIDE_TIMER.cancel ();
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private void liveImageLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_liveImageLabelMouseClicked

        if (playToggleButton.isSelected ()) playToggleButton.doClick ();
        
        /*
            the user has chosen to show live video
            so we will not display playback images
        */
        showPlayBackImages_b = Boolean.FALSE;

        BIG_SCREEN.setTitle ("logFarmDMS :: " + CAMERA_NUMBER + " :: live");
        
        BIG_SCREEN.setVisible (true);
    }//GEN-LAST:event_liveImageLabelMouseClicked

    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private void recordToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordToggleButtonActionPerformed

        testAudioKeepGoing ();
        
        if (!recordToggleButton.isSelected ()) {

            recordToggleButton.setIcon (new javax.swing.ImageIcon (getClass ().getResource ("/logfarmdms/media-record.png")));
            
            if (!statusBarLabel.getText ().equals (" not recording")) statusBarLabel.setText (" not recording");
        }
        else {

            recordToggleButton.setIcon (new javax.swing.ImageIcon (getClass ().getResource ("/logfarmdms/media-playback-stop.png")));
        }
    }//GEN-LAST:event_recordToggleButtonActionPerformed

    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private void playToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playToggleButtonActionPerformed

        if (!playToggleButton.isSelected ()) {

            /*
                CaptureTimerTask checks for: playToggleButton.isSelected ()
            */
            
            playToggleButton.setIcon (new javax.swing.ImageIcon (getClass ().getResource ("/logfarmdms/media-playback-start.png")));
        }
        else {

            showBigScreen ();
            
            playToggleButton.setIcon (new javax.swing.ImageIcon (getClass ().getResource ("/logfarmdms/media-playback-stop.png")));           
        }
    }//GEN-LAST:event_playToggleButtonActionPerformed

    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private void playbackSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playbackSliderMouseReleased

        sliderHasBeenMoved_b = Boolean.TRUE;
        
        showBigScreen (); 
    }//GEN-LAST:event_playbackSliderMouseReleased

    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private void stepBackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepBackButtonActionPerformed

        showBigScreen ();
        
        if (playbackSlider.getValue () > 1) {
            
            playbackSlider.setValue (playbackSlider.getValue () - 1);
            
            sliderHasBeenMoved_b = Boolean.TRUE;
        }
    }//GEN-LAST:event_stepBackButtonActionPerformed

    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private void stepForwardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepForwardButtonActionPerformed
        
        showBigScreen ();
        
        if (playbackSlider.getValue () < playbackSlider.getMaximum ()) {
            
            playbackSlider.setValue (playbackSlider.getValue () + 1);
            
            sliderHasBeenMoved_b = Boolean.TRUE;
        }
    }//GEN-LAST:event_stepForwardButtonActionPerformed

    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        
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
        
        setSliderMaximumValue ();
        
        if (!showPlayBackImages_b) showPlayBackImages_b = Boolean.TRUE;
            
        BIG_SCREEN.setTitle ("logFarmDMS :: " + CAMERA_NUMBER + " :: playback");
            
        if (!BIG_SCREEN.isVisible ()) BIG_SCREEN.setVisible (Boolean.TRUE); 
        
        audioPlaybackRowId_i = -1;
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
        catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger (GUI.class.getName ()).log (java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger (GUI.class.getName ()).log (java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger (GUI.class.getName ()).log (java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger (GUI.class.getName ()).log (java.util.logging.Level.SEVERE, null, ex);
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
    
        try {
            
            if (camera_database_connection.isClosed ()) {
                    
                camera_database_connection = DriverManager.getConnection ("jdbc:sqlite:" +
                            CAMERAS_DETAILS_HM.get(CAMERA_NUMBER.toString ()).getProperty ("db_location") + System.getProperty ("file.separator") + 
                            "logFarmDMS_" + CAMERA_NUMBER + ".db" );
            }
            
            audioPlaybackRowId_i = -1;
            
            String sql = "select rowid from fileData order by rowid desc limit 1";

            Statement statement;
            statement = camera_database_connection.createStatement ();

            ResultSet resultSet;
            resultSet = statement.executeQuery (sql);

            Integer rowCount = 0;

            while (resultSet.next ()) {

                rowCount = resultSet.getInt (1);
            }
            
            resultSet.close ();
            
            statement.closeOnCompletion ();
            
            //
            
            sql = "select rowid from fileData order by rowid asc limit 1";

            statement = camera_database_connection.createStatement ();

            resultSet = statement.executeQuery (sql);

            Integer minimum = 1;

            while (resultSet.next ()) {

                minimum = resultSet.getInt (1);
            }
            
            resultSet.close ();
            
            statement.closeOnCompletion ();
            
            //
            
            playbackSlider.setMinimum (minimum);
            
//            if (rowCount > 55) {
//                
//                playbackSlider.setMaximum (rowCount - 50);
//                
//                System.out.println ("rowCount - 50 " + (rowCount - 50));
//            }
            
            playbackSlider.setMaximum (rowCount);
        }
        catch (SQLException  | NullPointerException ex) {

            System.err.println ("setSliderMaximumValue " + ex.getMessage ());
            
            return Boolean.FALSE;
        }
        
        return Boolean.TRUE;
    }

    private static class RunnableImpl implements Runnable {

        public RunnableImpl () {
        }

        @Override
        public void run () {

            new GUI ().setVisible (true);
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
    public javax.swing.JToggleButton recordToggleButton;
    private javax.swing.JLabel statusBarLabel;
    private javax.swing.JButton stepBackButton;
    private javax.swing.JButton stepForwardButton;
    private javax.swing.JRadioButton videoAudioRadioButton;
    // End of variables declaration//GEN-END:variables

    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    public class CaptureTimerTask extends TimerTask {

        private Integer statusRecordTickCounter = 0;
  
        private final BufferedImage FULL_SIZE_BI, CAMERA_BUFFERED_IMAGE;
        
        public CaptureTimerTask () {
            
            /*
                initialise BufferedImage when this class is instantiated
            */
            if (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",").length == 2
                && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim ())
                && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim ())) {

                FULL_SIZE_BI = new BufferedImage (Integer.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim ()),
                                                  Integer.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim ()),
                                                  8);
                
                CAMERA_BUFFERED_IMAGE = new BufferedImage (Integer.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim ()),
                                                           Integer.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim ()),
                                                           8);
            }
            else {

                FULL_SIZE_BI = new BufferedImage (640, 480, 8);
                
                CAMERA_BUFFERED_IMAGE = new BufferedImage (640, 480, 8);
            }
        }
        
        @Override
        public void run () {

            /*
                a null is generated if there isn't enough bandwidth for multiple
                cameras
            
                need to change the error message
            */
            try {
                
                /*
                    one of the video checkboxes is selected
                */
                if (videoAudioRadioButton.isSelected ()) {

                    BufferedImage bi = cvQueryFrame ((CvCapture) CAMERAS_HM.get (CAMERA_NUMBER)).getBufferedImage ();

                    if (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",").length == 2
                        && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim ())
                        && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim ())) {

    //                    IplImage implimage = cvQueryFrame ((CvCapture) CAMERAS_HM.get (CAMERA_NUMBER)); 
    //                    OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
    //                    Java2DFrameConverter paintConverter = new Java2DFrameConverter();
    //                    frame = GRABBER_CONVERTER.convert(cvQueryFrame ((CvCapture) CAMERAS_HM.get (CAMERA_NUMBER)));

                        CAMERA_BUFFERED_IMAGE.getGraphics ().drawImage (bi, //PAINT_CONVERTER.getBufferedImage(frame,1),  
                                                                        0, 0,
                                                                        Integer.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim ()),
                                                                        Integer.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim ()),
                                                                        null);
                    }
                    else {

                        CAMERA_BUFFERED_IMAGE.getGraphics ().drawImage (bi,
                                                                        0, 0,
                                                                        640, 480,
                                                                        null);
                    }

                    bi.flush ();
                }
                else {
                    
                    /*
                        we create a separate Graphics object otherwise the font
                        setting is lost
                    */
                    Graphics g = CAMERA_BUFFERED_IMAGE.getGraphics ();

                    if (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",").length == 2
                        && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim ())
                        && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim ())) {

                        g.clearRect (0, 0, 
                                     Integer.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim ()),
                                     Integer.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim ()));
                    }
                    else {

                        g.clearRect (0, 0, 640, 480);
                    }

                    g.setFont (new Font (Font.SANS_SERIF, Font.PLAIN, 84));

                    g.drawString ("audio only", 100, 240);

                    g.setFont (new Font (Font.SANS_SERIF, Font.PLAIN, 52));
                    GregorianCalendar calendar = new GregorianCalendar ();
                    g.drawString (calendar.get (Calendar.YEAR) + "-" 
                                                + ((calendar.get (Calendar.MONTH) + 1 < 10) ? "0" + (calendar.get (Calendar.MONTH) + 1) : (calendar.get (Calendar.MONTH) + 1)) + "-"
                                                + ((calendar.get (Calendar.DATE) < 10) ? "0" + calendar.get (Calendar.DATE) : calendar.get (Calendar.DATE)) + " "
                                                + ((calendar.get (Calendar.HOUR_OF_DAY) < 10) ? "0" + calendar.get (Calendar.HOUR_OF_DAY) : calendar.get (Calendar.HOUR_OF_DAY)) + ":" 
                                                + ((calendar.get (Calendar.MINUTE) < 10) ? "0" + calendar.get (Calendar.MINUTE) : calendar.get (Calendar.MINUTE)) + ":"
                                                + ((calendar.get (Calendar.SECOND) < 10) ? "0" + calendar.get (Calendar.SECOND) : calendar.get (Calendar.SECOND)),
                                                  30, 450);
                }

                if (getState () != JFrame.ICONIFIED) {

                    liveImageLabel.setIcon (new ImageIcon (CAMERA_BUFFERED_IMAGE.getScaledInstance (liveImageLabel.getWidth (),
                                                                                                    liveImageLabel.getHeight (),
                                                                                                    Image.SCALE_DEFAULT)));
                }

                /*
                    show live image in bigScreen
                */
                if (BIG_SCREEN.isVisible ()
                    && BIG_SCREEN.getState () != JFrame.ICONIFIED
                    && !showPlayBackImages_b) {

                    BIG_SCREEN.setBufferedImage (CAMERA_BUFFERED_IMAGE);
                }

                /*
                    insert image into database
                */
                if (camera_database_connection.isClosed ()) {
                    
                    camera_database_connection = DriverManager.getConnection ("jdbc:sqlite:" +
                                CAMERAS_DETAILS_HM.get(CAMERA_NUMBER.toString ()).getProperty ("db_location") + System.getProperty ("file.separator") + 
                                "logFarmDMS_" + CAMERA_NUMBER + ".db" );
                }
                
                /*
                    if the record button is selected
                */
                if (recordToggleButton.isSelected ()) {

                    statusRecordTickCounter++;
                    if (statusRecordTickCounter <= 2) statusBarLabel.setText (" recording \\");
                    else if (statusRecordTickCounter > 2 && statusRecordTickCounter <= 4) statusBarLabel.setText (" recording |");
                    else if (statusRecordTickCounter > 4 && statusRecordTickCounter <= 6) statusBarLabel.setText (" recording /");
                    else if (statusRecordTickCounter > 6 && statusRecordTickCounter < 8) statusBarLabel.setText (" recording --");
                    else statusRecordTickCounter = 0;

                    byte[] bytesOut;
                    
                    try (ByteArrayOutputStream BYTE_ARRAY_OUTPUT_STREAM = new ByteArrayOutputStream ()) {
                        
                        /*
                            if this is set to jpg then OpenJDK will crash
                        */
                        ImageIO.write (CAMERA_BUFFERED_IMAGE, "jpg", BYTE_ARRAY_OUTPUT_STREAM);
                        CAMERA_BUFFERED_IMAGE.flush ();
                        
                        bytesOut = BYTE_ARRAY_OUTPUT_STREAM.toByteArray ();
                        BYTE_ARRAY_OUTPUT_STREAM.flush ();
                    }
                    
                    final String SQL_COMMAND = "insert into fileData (fileBytes, date) values(?,(select (STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW', 'localtime'))))";
                    PreparedStatement pstmt = camera_database_connection.prepareStatement (SQL_COMMAND); 
                    pstmt.setBytes (1, bytesOut);
                    pstmt.executeUpdate ();
                    pstmt.closeOnCompletion ();
                    
                    bytesOut = null;
                    
                    if ((new File (CAMERAS_DETAILS_HM.get (CAMERA_NUMBER.toString ()).getProperty ("db_location") + System.getProperty ("file.separator")
                            + "logFarmDMS_" + CAMERA_NUMBER + ".db").length () / 1048576) > (Integer.valueOf (CAMERAS_DETAILS_HM.get (CAMERA_NUMBER.toString ()).getProperty ("maximum_db_size")) * 1000)) {

                        /*
                            get highest rowid
                        */
                        final Statement STMT = camera_database_connection.createStatement ();

                        String sql = "select rowid from fileData order by rowid desc limit 1";

                        ResultSet resultSet = STMT.executeQuery (sql);

                        Integer highest = 0;
                        
                        while (resultSet.next ()) {

                            highest = resultSet.getInt (1);
                        }

                        //
                        
                        /*
                            get lowest rowid
                        */
                        sql = "select rowid from fileData order by rowid asc limit 1";

                        resultSet = STMT.executeQuery (sql);

                        Integer lowest = 0;
                        
                        while (resultSet.next ()) {

                            lowest = resultSet.getInt (1);
                        }
                        
                        resultSet.close ();
                        STMT.closeOnCompletion ();
                        
                        /*
                            guessed there are 36000 rows per gigabyte
                            highest - lowest will give us the number of rows in
                            the database
                        */
                        if ((highest - lowest) >= 36000 * Integer.valueOf (CAMERAS_DETAILS_HM.get (CAMERA_NUMBER.toString ()).getProperty ("maximum_db_size"))) {

                            final Statement STATEMENT = camera_database_connection.createStatement (); 

                            /*
                                delete the oldest 1 minute of video frames
                            */
                            sql = "delete from fileData where rowid < (" + lowest + " + 240)";

//                            System.out.println (sql);

                            STATEMENT.executeUpdate (sql);

                            STATEMENT.closeOnCompletion ();

                            playbackSlider.setMinimum (lowest);
                            
                            /*
                                recording, database is maximum size, database 
                                files have been deleted so reset slider values
                            */
                            setSliderMaximumValue ();
                        }
                        else {
                            
                            /*
                                the database file will remain this size
                            
                                for testing :: show me: highest - lowest
                            */
//                            System.out.println ("CaptureTimerTask [10.2] :: (highest - lowest): " + (highest - lowest));
                        }
                    }
                        
                    /*
                        recording, the database is not too big so just increment 
                        the slider's maximum value
                    */
                    playbackSlider.setMaximum (playbackSlider.getMaximum () + 1);
                }
                /*
                    we are not recording anymore and the playback slider is at
                    the maximum position so unselect the playback button
                */
                else {
                    
                    if (playToggleButton.isSelected ()
                        && playbackSlider.getValue () >= playbackSlider.getMaximum ()) playToggleButton.doClick ();
                }

                /*
                    play back
                */
                if (playToggleButton.isSelected ()
                    || sliderHasBeenMoved_b) {

                    /*
                        don't automatically move the playback slider
                    
                        if sliderHasBeenMoved_b is true (above) then the 
                        user has manually moved the slider, either forwards 
                        or backwards so we don't want to automatically
                        move it at this very moment, it will move next time
                    */
                    if (sliderHasBeenMoved_b) {

                        sliderHasBeenMoved_b = Boolean.FALSE;
                    }
                    else {
    
                        playbackSlider.setValue (playbackSlider.getValue () + 1);
                    }
                    
                    final Statement STATEMENT = camera_database_connection.createStatement ();

                    String sql = ("select fileBytes, date from fileData where rowid = " + playbackSlider.getValue ());
                    
//                    System.out.println (sql);

                    final ResultSet RESULTSET;
                    RESULTSET = STATEMENT.executeQuery (sql);

                    byte[] data = null;

                    while (RESULTSET.next ()) {

                        data = RESULTSET.getBytes (1);

                        playbackDateLabel.setText (RESULTSET.getString (2));
                    }

                    RESULTSET.close ();
                    
                    if (data != null) {

                        try (final ByteArrayInputStream BA_IS = new ByteArrayInputStream (data);
                             final BufferedInputStream BIN = new BufferedInputStream (BA_IS)) {

                            if (BIG_SCREEN.isVisible ()
                                    && BIG_SCREEN.getState () != JFrame.ICONIFIED) {

                                if (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",").length == 2
                                        && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim ())
                                        && isNumeric (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim ())) {

                                     FULL_SIZE_BI.getGraphics ().drawImage (ImageIO.read (BIN),
                                                                            0, 0,
                                                                            Integer.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[0].trim ()),
                                                                            Integer.valueOf (CAMERAS_DETAILS_HM.get ("global").getProperty ("camera_resolution").split (",")[1].trim ()), 
                                                                            null);
                                }
                                else {

                                    FULL_SIZE_BI.getGraphics ().drawImage (ImageIO.read (BIN),
                                                                           0, 0,
                                                                           640, 480,
                                                                           null);
                                }
                                
                                BIG_SCREEN.setBufferedImage (FULL_SIZE_BI);
                                
                                FULL_SIZE_BI.flush ();
                            }
                        }
                    }
                    else {

                        camera_database_connection = DriverManager.getConnection ("jdbc:sqlite:" + 
                                CAMERAS_DETAILS_HM.get(CAMERA_NUMBER.toString ()).getProperty ("db_location") + System.getProperty ("file.separator") + 
                                "logFarmDMS_" + CAMERA_NUMBER + ".db" );
                        
//                        setSliderMaximumValue ();
                    }

                    STATEMENT.closeOnCompletion ();
                }
                
                camera_database_connection.close ();
            }
            catch (IOException | SQLException ex) { 

                System.err.println ("Capture [1] " + ex.getMessage ());
            }
            catch (NullPointerException ex) { 

//                System.err.println ("It's possible that there isn't enough USB bandwidth on this computer\nfor multiple cameras.");
                System.err.println ("Capture [2] " + ex.getMessage ());
            }
        }
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private static void testAudioKeepGoing () {
        
        for (Enumeration<Integer> e = GUIS_HM.keys (); e.hasMoreElements (); ) {

            final Integer KEY = e.nextElement ();

            if (GUIS_HM.get (KEY).recordToggleButton.isSelected ()) {

                captureAudio_b = Boolean.TRUE;

                break;
            }

            captureAudio_b = Boolean.FALSE;
        }
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private class captureAudioTimerTask extends TimerTask {
        
        @Override
        public void run () {
 
            if (captureAudio_b) {
                
                recordAudioForAfewSeconds ();
            }
        }
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private class PlaybackAudioTimerTask extends TimerTask {
        
        private final ByteArrayOutputStream DATA_BYTE_ARRAY_OUTPUTSTREAM = new ByteArrayOutputStream (1024);
        
        public PlaybackAudioTimerTask () {}
        
        @Override
        public void run () {
            
            if (playToggleButton.isSelected ()
                && !BIG_SCREEN.getMuteAudioMenuItem ().isSelected ()) {
                
                audioPlayback_b = Boolean.TRUE;
                
                /*
                    if playback slider is at the end and we are no longer recording (!recordToggleButton.isSelected)
                    then don't get-and-play audio
                */
                try {
                    
                    if (audio_database_connection.isClosed ()) {
                        
                        audio_database_connection = DriverManager.getConnection ("jdbc:sqlite:" + 
                                CAMERAS_DETAILS_HM.get("global").getProperty ("audio_db_location") + 
                                System.getProperty ("file.separator") + "logFarmDMSaudio.db");
                    }
                    
                    /*
                        get audio
                    */
                    final Statement AUDIO_STATEMENT = audio_database_connection.createStatement ();

                    /*
                         select date from fileData where date >= datetime("2016-07-16 14:04:38.449") and date <= datetime("2016-07-16 14:04:38.449", '+5 seconds');
                    */
                    String sql;
                    
                    if (audioPlaybackRowId_i < 1) {
                    
                        sql = "select fileBytes, rowid from fileData where date >= datetime('" + playbackDateLabel.getText () + "', '+0 seconds') and date <= datetime('" + playbackDateLabel.getText () + "', '+5 seconds') order by date desc limit 1";
                    }
                    else {
                        
                        sql = "select fileBytes from fileData where rowid = " + audioPlaybackRowId_i;
                    }
                    
//                    System.out.println (sql);
                    
                    final ResultSet AUDIO_RESULTSET;
                    AUDIO_RESULTSET = AUDIO_STATEMENT.executeQuery (sql);
                    
                    /*
                        closed when there aren't any more audio frames
                        this seems to happen when playback catches up with
                        audio recording
                    */
                    if (AUDIO_RESULTSET.isClosed ()) {

                        audioPlaybackRowId_i = -1;
                        
                        stepBackButton.doClick ();
                        //
                        stepBackButton.doClick ();
                    }
                    else {

                        while (AUDIO_RESULTSET.next ()) {

                            /*
                                we wont have a second result field unless 
                                audioPlaybackRowId_i < 1
                            */
                            if (audioPlaybackRowId_i < 1) {

                                audioPlaybackRowId_i = AUDIO_RESULTSET.getInt (2);
                            }
                            
                            /*
                                push audio bytes into an output stream buffer
                            */
                            DATA_BYTE_ARRAY_OUTPUTSTREAM.write (AUDIO_RESULTSET.getBytes (1));

                            /*
                                play that buffer in a separate thread
                                otherwise it would lock this action
                            */
                            final PlayAudio PLAY_AUDIO = new PlayAudio(AUDIO_FORMAT, DATA_BYTE_ARRAY_OUTPUTSTREAM);
                            PLAY_AUDIO.start ();
                        }
                        
                        AUDIO_RESULTSET.close ();
                    }
                    
                    AUDIO_STATEMENT.closeOnCompletion ();
                    
                    if (!audioRecord_b) {
                    
                        audio_database_connection.close ();
                    }
                    
                    audioPlaybackRowId_i++;

//                    if (data != null) {
//                        
//                        try (final BufferedInputStream DATA_BYTE_ARRAY_INPUT_STREAM = new BufferedInputStream (new ByteArrayInputStream (data))) {
//
//                            try (final AudioInputStream AUDIO_INPUT_STREAM = new AudioInputStream (DATA_BYTE_ARRAY_INPUT_STREAM,
//                                                                                                   AUDIO_FORMAT,
//                                                                                                   data.length / AUDIO_FORMAT.getFrameSize ())) {
//
//                                final byte BUFFER2[] = new byte[1024];
//
//                                int count;
//
//                                while ((count = AUDIO_INPUT_STREAM.read (BUFFER2,
//                                                                         0,
//                                                                         BUFFER2.length)) > 0) {
//
//                                    playbackLine.write (BUFFER2,
//                                                0,
//                                                count);
//                                }
//
//                                playbackLine.flush ();
//                                
//                                AUDIO_INPUT_STREAM.close ();
//                            }
//                            
//                            DATA_BYTE_ARRAY_INPUT_STREAM.close ();
//                        }
//                    }
//                    
//                    data = null;
                }
                catch (IOException | SQLException  | NullPointerException ex) { // IOException | LineUnavailableException ex) {

                    System.err.println ("PlaybackAudioTimerTask " + ex.getMessage ());
                }
                
                audioPlayback_b = Boolean.FALSE;
            }
            else {
                
                if (!audioPlaybackRowId_i.equals (-1)) audioPlaybackRowId_i = -1;
            }
        }
    }
    
    /**
     * [TODO]
     *      Documentation.
     * [/] 
     * 
     * @return Boolean for Unit testing      
    */
    public Boolean recordAudioForAfewSeconds () {

        /*
            5150, (130) fills the gap between each audio frame
        */
        final Long NOW = System.currentTimeMillis () + 5130;

        final byte BUFFER[] = new byte[1024];
        
        audioRecord_b = Boolean.TRUE;

        try (final ByteArrayOutputStream AUDIO_BA_OS = new ByteArrayOutputStream ()) {
            
            if (audio_database_connection.isClosed ()) {
            
                audio_database_connection = DriverManager.getConnection ("jdbc:sqlite:" + 
                        CAMERAS_DETAILS_HM.get("global").getProperty ("audio_db_location") + 
                        System.getProperty ("file.separator") + "logFarmDMSaudio.db");
            }

            while (System.currentTimeMillis () < NOW) {

                int count = line.read (BUFFER, 0, BUFFER.length);

                if (count > 0) {

                    AUDIO_BA_OS.write (BUFFER, 0, count);
                }
            }

            line.flush ();
            
            //

            byte[] bytesOut = AUDIO_BA_OS.toByteArray ();
            AUDIO_BA_OS.flush ();
            
            final String SQL_COMMAND = "insert into fileData (fileBytes, date) values(?,(select (STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW', 'localtime'))))";

            final PreparedStatement PSTMT = audio_database_connection.prepareStatement (SQL_COMMAND);

            PSTMT.setBytes (1, bytesOut);
            
            PSTMT.executeUpdate ();
            
            PSTMT.closeOnCompletion ();
            
            bytesOut = null;
            
            if (!audioPlayback_b) {
            
                audio_database_connection.close ();
            }
        }
        catch (IOException | SQLException  | NullPointerException ex) {

            System.err.println ("recordAudioForAfewSeconds " + ex.getMessage ());
            
            return Boolean.FALSE;
        }
        
        audioRecord_b = Boolean.FALSE;
        
        return Boolean.TRUE;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
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
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    private class HideTimerTask extends TimerTask {

        @Override
        public void run () {
            
            setVisible (Boolean.FALSE);   
        }
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * 
     *      This can be moved to a separate class.
     * [/] 
     */
    public static boolean isNumeric (String s) {

        if (s.length () <= 0) {

            return false;
        }

        for (int i = 0; i < s.length (); i++) {

            if (!Pattern.matches ("[0-9]", Character.toString (s.charAt (i)))) { 

                return false;
            }
        }

        return true;
    }
}
