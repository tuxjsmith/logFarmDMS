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
package utilities;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import logfarmdms.LFDMS_GUI;
import static logfarmdms.LFDMS_GUI.isNumeric;
import org.bytedeco.javacpp.opencv_highgui;
import static org.bytedeco.javacpp.opencv_highgui.cvQueryFrame;
import static utilities.LFDMS_Constants.CAMERAS_DETAILS_HM;
import static utilities.LFDMS_Constants.CAMERAS_HM;

/**
 * @author tuxjsmith@gmail.com
 */
public class LFDMS_Timers {

    private Timer captureTimer = new Timer (),
            hideTimer = new Timer ();

    private final static Timer RECORD_AUDIO_TIMER = new Timer ();
    private static Timer audioPlaybackTimer = new Timer ();

    private final LFDMS_GUI TIMERS_GUI;

    public LFDMS_Timers ( LFDMS_GUI gui ) {

        TIMERS_GUI = gui;
    }

    /**
     * @return the CAPTURE_TIMER
     */
    public Timer getCaptureTimer () {

        return captureTimer;
    }

    /**
     * @param captureTimer the CAPTURE_TIMER to set
     */
    public void setCaptureTimer ( Timer captureTimer ) {

        this.captureTimer = captureTimer;
    }

    /**
     * @return the RECORD_AUDIO_TIMER
     */
    public static Timer getRecordAudioTimer () {

        return RECORD_AUDIO_TIMER;
    }

    /**
     * @return the AUDIO_PLAYBACK_TIMER
     */
    public static Timer getAudioPlaybackTimer () {

        return audioPlaybackTimer;
    }

    /**
     * @param timer the AUDIO_PLAYBACK_TIMER to set
     */
    public static void setAudioPlaybackTimer ( Timer timer ) {

        audioPlaybackTimer = timer;
    }

    /**
     * @return the HIDE_TIMER
     */
    public Timer getHideTimer () {

        return hideTimer;
    }

    /**
     * @param timer the HIDE_TIMER to set
     */
    public void setHideTimer ( Timer timer ) {

        hideTimer = timer;
    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/] 
     * @return 
     */
    public CaptureTimerTask getNewCaptureTimerTask () {

        return new CaptureTimerTask ();
    }

    /**
     * [TODO]
     *      Documentation.
     * [/] 
     */
    public class CaptureTimerTask extends TimerTask {

        private Integer statusRecordTickCounter = 0;

        private final BufferedImage FULL_SIZE_BI,
                CAMERA_BUFFERED_IMAGE;

        public CaptureTimerTask () {

            /*
                initialise BufferedImage when this class is instantiated
             */
            if ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," ).length == 2
                    && isNumeric ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 0 ].trim () )
                    && isNumeric ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 1 ].trim () ) ) {

                FULL_SIZE_BI = new BufferedImage ( Integer.valueOf ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 0 ].trim () ),
                        Integer.valueOf ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 1 ].trim () ),
                        8 );

                CAMERA_BUFFERED_IMAGE = new BufferedImage ( Integer.valueOf ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 0 ].trim () ),
                        Integer.valueOf ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 1 ].trim () ),
                        8 );
            }
            else {

                FULL_SIZE_BI = new BufferedImage ( 640, 480, 8 );

                CAMERA_BUFFERED_IMAGE = new BufferedImage ( 640, 480, 8 );
            }
        }

        @Override
        public void run () {

            /*
                A null is generated if there isn't enough bandwidth for multiple
                cameras
            
                need to change the error message
             */
            try {

                if ( TIMERS_GUI.getVideoAudioRadioButton ().isSelected () ) {

                    /*
                        Grab a single image from the camera
                    */
                    BufferedImage bi = cvQueryFrame ( ( opencv_highgui.CvCapture ) CAMERAS_HM.get ( TIMERS_GUI.getCameraNumber () ) ).getBufferedImage ();

                    /*
                        [TODO]
                            The following if statement could be a function returning
                            a Boolean since we use it multiple times.
                        [/]
                    
                        Make sure there are width and height resolution parameters: 2
                        and make sure the width and height parameters are numerical.
                    */
                    if ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," ).length == 2
                         && isNumeric ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 0 ].trim () )
                         && isNumeric ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 1 ].trim () ) ) {
                        
                        /*
                            Old stuff that might be informative.
                        
                            IplImage implimage = cvQueryFrame ((CvCapture) CAMERAS_HM.get (CAMERA_NUMBER)); 
                            OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
                            Java2DFrameConverter paintConverter = new Java2DFrameConverter();
                            frame = GRABBER_CONVERTER.convert(cvQueryFrame ((CvCapture) CAMERAS_HM.get (CAMERA_NUMBER)));
                         */
                        
                        /*
                            Create a BufferedImage based on preferences width and height.
                        */
                        CAMERA_BUFFERED_IMAGE.getGraphics ().drawImage ( bi, //PAINT_CONVERTER.getBufferedImage(frame,1),  
                                0, 0,
                                Integer.valueOf ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 0 ].trim () ),
                                Integer.valueOf ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 1 ].trim () ),
                                null );
                    }
                    /*
                        Preferences width and height were invalid so we create a BufferedImage with default dimensions.
                    */
                    else {

                        CAMERA_BUFFERED_IMAGE.getGraphics ().drawImage ( bi,
                                0, 0,
                                640, 480,
                                null );
                    }

                    bi.flush ();
                }
                
                /*
                    We don't want to capture video, audio only.
                */
                else {

                    /*
                        We create a separate Graphics object otherwise the font
                        settings get lost.
                     */
                    Graphics g = CAMERA_BUFFERED_IMAGE.getGraphics ();

                    /*
                        [TODO]
                            Hmm, not sure we need this if statement because we
                            are not recording video.
                        [/]
                    
                        [TODO]
                            The following if statement could be a function returning
                            a Boolean since we use it multiple times.
                        [/]
                    
                        Make sure there are width and height resolution parameters: 2
                        and make sure the width and height parameters are numerical.
                    */
//                    if ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," ).length == 2
//                         && isNumeric ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 0 ].trim () )
//                         && isNumeric ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 1 ].trim () ) ) {
//
//                        g.clearRect ( 0, 0,
//                                      Integer.valueOf ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 0 ].trim () ),
//                                      Integer.valueOf ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 1 ].trim () ) );
//                    }
//                    else {

                        g.clearRect ( 0, 0, 320, 240 );
//                    }

                    g.setFont ( new Font ( Font.SANS_SERIF, Font.PLAIN, 84 ) );

                    g.drawString ( "audio only", 100, 240 );

                    g.setFont ( new Font ( Font.SANS_SERIF, Font.PLAIN, 52 ) );
                    GregorianCalendar calendar = new GregorianCalendar ();
                    g.drawString ( calendar.get ( Calendar.YEAR ) + "-"
                            + ( ( calendar.get ( Calendar.MONTH ) + 1 < 10 ) ? "0" + ( calendar.get ( Calendar.MONTH ) + 1 ) : ( calendar.get ( Calendar.MONTH ) + 1 ) ) + "-"
                            + ( ( calendar.get ( Calendar.DATE ) < 10 ) ? "0" + calendar.get ( Calendar.DATE ) : calendar.get ( Calendar.DATE ) ) + " "
                            + ( ( calendar.get ( Calendar.HOUR_OF_DAY ) < 10 ) ? "0" + calendar.get ( Calendar.HOUR_OF_DAY ) : calendar.get ( Calendar.HOUR_OF_DAY ) ) + ":"
                            + ( ( calendar.get ( Calendar.MINUTE ) < 10 ) ? "0" + calendar.get ( Calendar.MINUTE ) : calendar.get ( Calendar.MINUTE ) ) + ":"
                            + ( ( calendar.get ( Calendar.SECOND ) < 10 ) ? "0" + calendar.get ( Calendar.SECOND ) : calendar.get ( Calendar.SECOND ) ),
                            30, 450 );
                }

                /*
                    If the GUI is not minimised grab a live image and push it
                    on to the live image JLabel as its ImageIcon.
                */
                if ( TIMERS_GUI.getState () != JFrame.ICONIFIED ) {

                    TIMERS_GUI.getLiveImageLabel ().setIcon ( 
                            new ImageIcon ( CAMERA_BUFFERED_IMAGE.getScaledInstance ( TIMERS_GUI.getLiveImageLabel ().getWidth (),
                                                                                      TIMERS_GUI.getLiveImageLabel ().getHeight (),
                                                                                      Image.SCALE_DEFAULT ) ) );
                }

                /*
                    Show live image in bigScreen if bigScreen is visible 
                    and not minimised 
                    and we are not playing back video.
                 */
                if ( TIMERS_GUI.getBigScreen ().isVisible ()
                     && TIMERS_GUI.getBigScreen ().getState () != JFrame.ICONIFIED
                     && !TIMERS_GUI.getStatus ().getShowPlayBackImages () ) {

                    TIMERS_GUI.getBigScreen ().setBufferedImage ( CAMERA_BUFFERED_IMAGE );
                }

                /*
                    We still write to the camera database even though the user
                    only wants to record audio.
                
                    A blank image with date and time is written to the camera
                    database.
                
                    Downside is we are going to use CPU and disk space when we
                    don't need to.
                
                    [TODO]
                        Don't record blank, timestamped images to the video
                        database.
                
                        Playback of audio-only recordings will be effected by 
                        this change.
                    [/]
                
                    Create a camera database connection.
                 */
                if ( TIMERS_GUI.getDbStuff ().getCameraDatabaseConnection ().isClosed () ) {

                    TIMERS_GUI.getDbStuff ().setCameraDatabaseConnection ( DriverManager.getConnection ( "jdbc:sqlite:"
                            + CAMERAS_DETAILS_HM.get ( TIMERS_GUI.getCameraNumber ().toString () ).getProperty ( "db_location" )
                            + System.getProperty ( "file.separator" )
                            + "logFarmDMS_" + TIMERS_GUI.getCameraNumber () + ".db" ) );
                }

                /*
                    If the record button is selected
                 */
                if ( TIMERS_GUI.getRecordToggleButton ().isSelected () ) {

                    // <editor-fold defaultstate="collapsed" desc="Spinning ascii">  
                    statusRecordTickCounter++;
                    if ( statusRecordTickCounter <= 2 ) {
                        TIMERS_GUI.getStatusBarLabel ().setText ( " recording \\" );
                    }
                    else if ( statusRecordTickCounter > 2 && statusRecordTickCounter <= 4 ) {
                        TIMERS_GUI.getStatusBarLabel ().setText ( " recording |" );
                    }
                    else if ( statusRecordTickCounter > 4 && statusRecordTickCounter <= 6 ) {
                        TIMERS_GUI.getStatusBarLabel ().setText ( " recording /" );
                    }
                    else if ( statusRecordTickCounter > 6 && statusRecordTickCounter < 8 ) {
                        TIMERS_GUI.getStatusBarLabel ().setText ( " recording --" );
                    }
                    else {
                        statusRecordTickCounter = 0;
                    }
                    //</editor-fold>
                    
                    /*
                        Insert the camera BufferedImage into the database.
                        If audio only, a blank timestamped image is inserted.
                    */
                    byte[] bytesOut;

                    try ( ByteArrayOutputStream BYTE_ARRAY_OUTPUT_STREAM = new ByteArrayOutputStream () ) {

                        /*
                            [WARNING] 
                                If this is set to jpg then OpenJDK will crash
                            [/]
                         */
                        ImageIO.write ( CAMERA_BUFFERED_IMAGE, 
                                        "jpg", 
                                        BYTE_ARRAY_OUTPUT_STREAM );
                        
                        CAMERA_BUFFERED_IMAGE.flush ();

                        bytesOut = BYTE_ARRAY_OUTPUT_STREAM.toByteArray ();
                        
                        BYTE_ARRAY_OUTPUT_STREAM.flush ();
                    }

                    final String SQL_COMMAND = "insert into fileData (fileBytes, date) values(?,(select (STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW', 'localtime'))))";
                    final PreparedStatement PSTMT = TIMERS_GUI.getDbStuff ().getCameraDatabaseConnection ().prepareStatement ( SQL_COMMAND );
                    PSTMT.setBytes ( 1, bytesOut );
                    PSTMT.executeUpdate ();
                    PSTMT.closeOnCompletion ();

                    /*
                        For garbage collection.
                    */
                    bytesOut = null;

                    /*
                        1 megabyte = 1048576 bytes.
                    
                        [TODO]
                            Make this a constant: 1048576
                        [/]
                    
                        If the database size is larger than the preferences
                        maximum size then make space by removing some of the 
                        oldest images.
                     */
                    if ( ( new File ( CAMERAS_DETAILS_HM.get ( TIMERS_GUI.getCameraNumber ().toString () ).getProperty ( "db_location" )
                                      + System.getProperty ( "file.separator" )
                                      + "logFarmDMS_" + TIMERS_GUI.getCameraNumber () + ".db" ).length () / 1048576 ) > ( Integer.valueOf ( CAMERAS_DETAILS_HM.get ( TIMERS_GUI.getCameraNumber ().toString () ).getProperty ( "maximum_db_size" ) ) * 1000 ) ) {

                        /*
                            get highest rowid
                         */
                        final Statement STMT = TIMERS_GUI.getDbStuff ().getCameraDatabaseConnection ().createStatement ();

                        String sql = "select rowid from fileData order by rowid desc limit 1";

                        ResultSet resultSet = STMT.executeQuery ( sql );

                        Integer highest = 0;

                        while ( resultSet.next () ) {

                            highest = resultSet.getInt ( 1 );
                        }

                        //
                        /*
                            get lowest rowid
                         */
                        sql = "select rowid from fileData order by rowid asc limit 1";

                        resultSet = STMT.executeQuery ( sql );

                        Integer lowest = 0;

                        while ( resultSet.next () ) {

                            lowest = resultSet.getInt ( 1 );
                        }

                        resultSet.close ();
                        STMT.closeOnCompletion ();

                        /*
                            [TODO]
                                Needs a better explanation of the hard coded number.
                                If this figure is valid then make it a constant.
                            [/]
                        
                            guessed there are 36000 rows per gigabyte
                            highest - lowest will give us the number of rows in
                            the database
                         */
                        if ( ( highest - lowest ) >= 36000 * Integer.valueOf ( CAMERAS_DETAILS_HM.get ( TIMERS_GUI.getCameraNumber ().toString () ).getProperty ( "maximum_db_size" ) ) ) {

                            final Statement STATEMENT = TIMERS_GUI.getDbStuff ().getCameraDatabaseConnection ().createStatement ();

                            /*
                                delete the oldest 1 minute of video frames
                             */
                            sql = "delete from fileData where rowid < (" + lowest + " + 240)";

//                            System.out.println (sql);
                            STATEMENT.executeUpdate ( sql );

                            STATEMENT.closeOnCompletion ();

                            TIMERS_GUI.getPlaybackSlider ().setMinimum ( lowest );

                            /*
                                recording, database is maximum size, database 
                                files have been deleted so reset slider values
                             */
                            TIMERS_GUI.setSliderMaximumValue ();
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
                    TIMERS_GUI.getPlaybackSlider ().setMaximum ( TIMERS_GUI.getPlaybackSlider ().getMaximum () + 1 );
                }
                /*
                    we are not recording anymore and the playback slider is at
                    the maximum position so unselect the playback button
                 */
                else {

                    if ( TIMERS_GUI.getPlayToggleButton ().isSelected ()
                            && TIMERS_GUI.getPlaybackSlider ().getValue () >= TIMERS_GUI.getPlaybackSlider ().getMaximum () ) {

                        TIMERS_GUI.getPlayToggleButton ().doClick ();
                    }
                }

                /*
                    play back
                 */
                if ( TIMERS_GUI.getPlayToggleButton ().isSelected ()
                        || TIMERS_GUI.getStatus ().getSliderHasBeenMoved () ) {

                    /*
                        don't automatically move the playback slider
                    
                        if sliderHasBeenMoved_b is true (above) then the 
                        user has manually moved the slider, either forwards 
                        or backwards so we don't want to automatically
                        move it at this very moment, it will move next time
                     */
                    if ( TIMERS_GUI.getStatus ().getSliderHasBeenMoved () ) {

                        TIMERS_GUI.getStatus ().setSliderHasBeenMoved ( Boolean.FALSE );
                    }
                    else {

                        TIMERS_GUI.getPlaybackSlider ().setValue ( TIMERS_GUI.getPlaybackSlider ().getValue () + 1 );
                    }

                    final Statement STATEMENT = TIMERS_GUI.getDbStuff ().getCameraDatabaseConnection ().createStatement ();

                    String sql = ( "select fileBytes, date from fileData where rowid = " + TIMERS_GUI.getPlaybackSlider ().getValue () );

//                    System.out.println (sql);
                    final ResultSet RESULTSET;
                    RESULTSET = STATEMENT.executeQuery ( sql );

                    byte[] data = null;

                    while ( RESULTSET.next () ) {

                        data = RESULTSET.getBytes ( 1 );

                        TIMERS_GUI.getPlaybackDateLabel ().setText ( RESULTSET.getString ( 2 ) );
                    }

                    RESULTSET.close ();

                    if ( data != null ) {

                        try ( final ByteArrayInputStream BA_IS = new ByteArrayInputStream ( data );
                                final BufferedInputStream BIN = new BufferedInputStream ( BA_IS ) ) {

                            if ( TIMERS_GUI.getBigScreen ().isVisible ()
                                    && TIMERS_GUI.getBigScreen ().getState () != JFrame.ICONIFIED ) {

                                if ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," ).length == 2
                                        && isNumeric ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 0 ].trim () )
                                        && isNumeric ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 1 ].trim () ) ) {

                                    FULL_SIZE_BI.getGraphics ().drawImage ( ImageIO.read ( BIN ),
                                            0, 0,
                                            Integer.valueOf ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 0 ].trim () ),
                                            Integer.valueOf ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 1 ].trim () ),
                                            null );
                                }
                                else {

                                    FULL_SIZE_BI.getGraphics ().drawImage ( ImageIO.read ( BIN ),
                                            0, 0,
                                            640, 480,
                                            null );
                                }

                                TIMERS_GUI.getBigScreen ().setBufferedImage ( FULL_SIZE_BI );

                                FULL_SIZE_BI.flush ();
                            }
                        }
                    }
                    else {

                        TIMERS_GUI.getDbStuff ().setCameraDatabaseConnection ( DriverManager.getConnection ( "jdbc:sqlite:"
                                + CAMERAS_DETAILS_HM.get ( TIMERS_GUI.getCameraNumber ().toString () ).getProperty ( "db_location" )
                                + System.getProperty ( "file.separator" )
                                + "logFarmDMS_" + TIMERS_GUI.getCameraNumber () + ".db" ) );
                    }

                    STATEMENT.closeOnCompletion ();
                }

                TIMERS_GUI.getDbStuff ().getCameraDatabaseConnection ().close ();
            }
            catch ( IOException | SQLException ex ) {

                System.err.println ( "Capture [1] " + ex.getMessage () );
            }
            catch ( NullPointerException ex ) {

//                System.err.println ("It's possible that there isn't enough USB bandwidth on this computer\nfor multiple cameras.");
                System.err.println ( "Capture [2] " + ex.getMessage () );
            }
        }

    }

    /**
     * [TODO] Documentation. Unit test. [/]
     *
     * This is unique to each GUI.
     *
     * @return
     */
    public PlaybackAudioTimerTask getNewPlaybackAudioTimerTask () {

        return new PlaybackAudioTimerTask ();
    }

    /**
     * [TODO]
     *      Documentation.
     * [/] 
     */
    public class PlaybackAudioTimerTask extends TimerTask {

        private final ByteArrayOutputStream DATA_BYTE_ARRAY_OUTPUTSTREAM = new ByteArrayOutputStream ( 1024 );

        public PlaybackAudioTimerTask () {
        }

        @Override
        public void run () {

            if ( TIMERS_GUI.getPlayToggleButton ().isSelected ()
                    && !TIMERS_GUI.getBigScreen ().getMuteAudioMenuItem ().isSelected () ) {

                LFDMS_Status.setAudioPlayback ( Boolean.TRUE );

                /*
                    if playback slider is at the end and we are no longer recording (!recordToggleButton.isSelected)
                    then don't get-and-play audio
                 */
                try {

                    if ( LFDMS_DatabaseStuff.getAudioDatabaseConnection ().isClosed () ) {

                        LFDMS_DatabaseStuff.setAudioDatabaseConnection ( DriverManager.getConnection ( "jdbc:sqlite:"
                                + CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "audio_db_location" )
                                + System.getProperty ( "file.separator" )
                                + "logFarmDMSaudio.db" ) );
                    }

                    /*
                        get audio
                     */
                    final Statement AUDIO_STATEMENT = LFDMS_DatabaseStuff.getAudioDatabaseConnection ().createStatement ();

                    /*
                         select date from fileData where date >= datetime("2016-07-16 14:04:38.449") and date <= datetime("2016-07-16 14:04:38.449", '+5 seconds');
                     */
                    String sql;

                    if ( LFDMS_Status.getAudioPlaybackRowId () < 1 ) {

//                        sql = "select fileBytes, rowid from fileData where date >= datetime('" + playbackDateLabel.getText () + "', '+0 seconds') and date <= datetime('" + playbackDateLabel.getText () + "', '+5 seconds') order by date desc limit 1";
                        sql = "select fileBytes, rowid from fileData where date >= datetime('" + TIMERS_GUI.getPlaybackDateLabel ().getText () + "', '+0 seconds') and date <= datetime('" + TIMERS_GUI.getPlaybackDateLabel ().getText () + "', '+5 seconds') order by date desc limit 1";
                    }
                    else {

                        sql = "select fileBytes from fileData where rowid = " + LFDMS_Status.getAudioPlaybackRowId ();
                    }

                    final ResultSet AUDIO_RESULTSET;
                    AUDIO_RESULTSET = AUDIO_STATEMENT.executeQuery ( sql );

                    /*
                        closed when there aren't any more audio frames
                        this seems to happen when playback catches up with
                        audio recording
                     */
                    if ( AUDIO_RESULTSET.isClosed () ) {

                        LFDMS_Status.setAudioPlaybackRowId ( -1 );

                        TIMERS_GUI.getStepBackButton ().doClick ();
                        TIMERS_GUI.getStepBackButton ().doClick ();
                    }
                    else {

                        while ( AUDIO_RESULTSET.next () ) {

                            /*
                                we wont have a second result field unless 
                                audioPlaybackRowId_i < 1
                             */
                            if ( LFDMS_Status.getAudioPlaybackRowId () < 1 ) {

                                LFDMS_Status.setAudioPlaybackRowId ( AUDIO_RESULTSET.getInt ( 2 ) );
                            }

                            /*
                                push audio bytes into an output stream buffer
                             */
                            DATA_BYTE_ARRAY_OUTPUTSTREAM.write ( AUDIO_RESULTSET.getBytes ( 1 ) );

                            /*
                                play that buffer in a separate thread
                                otherwise it would lock this action
                             */
                            final LFDMS_PlayAudioOutputStream PLAY_AUDIO = new LFDMS_PlayAudioOutputStream ( LFDMS_AudioInit.getAudioFormat (),
                                    DATA_BYTE_ARRAY_OUTPUTSTREAM );
                            PLAY_AUDIO.start ();
                        }

                        AUDIO_RESULTSET.close ();
                    }

                    AUDIO_STATEMENT.closeOnCompletion ();

                    if ( !LFDMS_Status.getAudioRecord () ) {

                        LFDMS_DatabaseStuff.getAudioDatabaseConnection ().close ();
                    }

                    LFDMS_Status.incrementAudioPlaybackRowId ();
                }
                catch ( IOException | SQLException | NullPointerException ex ) { // IOException | LineUnavailableException ex) {

                    System.err.println ( "PlaybackAudioTimerTask " + ex.getMessage () );
                }

                LFDMS_Status.setAudioPlayback ( Boolean.FALSE );
            }
            else {

                if ( !LFDMS_Status.getAudioPlaybackRowId ().equals ( -1 ) ) {

                    LFDMS_Status.setAudioPlaybackRowId ( -1 );
                }
            }
        }

    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/] 
     * 
     * @return 
     */
    public static final CaptureAudioTimerTask GET_NEW_CAPTURE_AUDIO_TIMERTASK () {
        
        return new CaptureAudioTimerTask ();
    }
    
    /**
     * [TODO]
     *      Documentation.
     * [/] 
     */
    public static class CaptureAudioTimerTask extends TimerTask {

        @Override
        public void run () {

            if ( LFDMS_Status.getCaptureAudio () ) {

                /*
            5150, (130) fills the gap between each audio frame
                 */
                final Long NOW = System.currentTimeMillis () + 5130;

                final byte BUFFER[] = new byte[ 1024 ];

                LFDMS_Status.setAudioRecord ( Boolean.TRUE );

                try ( final ByteArrayOutputStream AUDIO_BA_OS = new ByteArrayOutputStream () ) {

                    if ( LFDMS_DatabaseStuff.getAudioDatabaseConnection ().isClosed () ) {

                        LFDMS_DatabaseStuff.setAudioDatabaseConnection ( DriverManager.getConnection ( "jdbc:sqlite:"
                                + CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "audio_db_location" )
                                + System.getProperty ( "file.separator" )
                                + "logFarmDMSaudio.db" ) );
                    }

                    while ( System.currentTimeMillis () < NOW ) {

                        int count = LFDMS_AudioInit.getLine ().read ( BUFFER, 0, BUFFER.length );

                        if ( count > 0 ) {

                            AUDIO_BA_OS.write ( BUFFER, 0, count );
                        }
                    }

                    LFDMS_AudioInit.getLine ().flush ();

                    //
                    byte[] bytesOut = AUDIO_BA_OS.toByteArray ();
                    AUDIO_BA_OS.flush ();

                    final String SQL_COMMAND = "insert into fileData (fileBytes, date) values(?,(select (STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW', 'localtime'))))";

                    final PreparedStatement PSTMT = LFDMS_DatabaseStuff.getAudioDatabaseConnection ().prepareStatement ( SQL_COMMAND );

                    PSTMT.setBytes ( 1, bytesOut );

                    PSTMT.executeUpdate ();

                    PSTMT.closeOnCompletion ();

                    bytesOut = null;

                    if ( !LFDMS_Status.getAudioPlayback () ) {

                        LFDMS_DatabaseStuff.getAudioDatabaseConnection ().close ();
                    }
                }
                catch ( IOException | SQLException | NullPointerException ex ) {

                    System.err.println ( "recordAudioForAfewSeconds " + ex.getMessage () );
                }

                LFDMS_Status.setAudioRecord ( Boolean.FALSE );
            }
        }
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/] 
     * 
     * @return 
     */
    public final HideTimerTask GET_NEW_HIDE_TIMERTASK () {
        
        return new HideTimerTask ();
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test, return value.
     * [/] 
     */
    public class HideTimerTask extends TimerTask {

        @Override
        public void run () {
            
            TIMERS_GUI.setVisible (Boolean.FALSE);   
        }
    }
}
