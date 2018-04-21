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
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import logfarmdms.LFDMS_GUI;
import static logfarmdms.LFDMS_GUI.isNumeric;
import org.bytedeco.javacpp.opencv_highgui;
import static org.bytedeco.javacpp.opencv_highgui.cvQueryFrame;
import static utilities.LFDMS_Constants.AUDIO_DURATION_I;
import static utilities.LFDMS_Constants.CAMERAS_DETAILS_HM;
import static utilities.LFDMS_Constants.CAMERAS_HM;
import static utilities.LFDMS_Constants.MAX_BUFFERS_I;
import static utilities.LFDMS_Constants.MAX_NUMBER_OF_OS_I;

/**
 * @author tuxjsmith@gmail.com
 */
public class LFDMS_Timers {

    private Timer captureTimer = new Timer (),
                  hideTimer = new Timer ();

    private final static Timer RECORD_AUDIO_TIMER = new Timer ();
    private static Timer audioPlaybackTimer = new Timer ();
    
    private BufferedImage PLAYBACK_BI,
                          CAMERA_BI;

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
     * [TODO] 
     *      Document. 
     *      Unit test. 
     * [/]
     */
    public void setCaptureTimer () {

        captureTimer = new Timer ();
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
     * [TODO] 
     *      Document. 
     *      Unit test. 
     * [/]
     */
    public static void setAudioPlaybackTimer () {

        audioPlaybackTimer = new Timer ();
    }

    /**
     * @return the HIDE_TIMER
     */
    public Timer getHideTimer () {

        return hideTimer;
    }

    /**
     * [TODO] 
     *      Document. 
     *      Unit test. 
     * [/]
     */
    public void setHideTimer () {

        hideTimer = new Timer ();
    }

    /**
     * [TODO]
     *
     * Documentation. Unit test. [/]
     *
     * @return
     */
    public CaptureTimerTask getNewCaptureTimerTask () {

        return new CaptureTimerTask ();
    }

    /**
     * [TODO]
     *      Document.
     *      Unit test.
     * [/]
     */
    public void displaySinglePlaybackImage () {
        
        final Playback PLAYBACK = new Playback ( PLAYBACK_BI );
        PLAYBACK.run ();
    }

     /**
     * [TODO] Documentation. Unit test. [/]
     *
     * @return
     */
    public final HideTimerTask GET_NEW_HIDE_TIMERTASK () {

        return new HideTimerTask ();
    }

    /**
     * [TODO] Documentation. Unit test. [/]
     *
     * This is unique to each GUI.
     *
     * @return
     */
    public static PlaybackAudioTimerTask getNewPlaybackAudioTimerTask () {

        return new PlaybackAudioTimerTask ();
    }
    
    /**
     * [TODO]
     *
     * Documentation. Unit test. [/]
     */
    public class CaptureTimerTask extends TimerTask {

        private Integer statusRecordTickCounter = 0;

        public CaptureTimerTask () {

            /*
                initialise BufferedImage when this class is instantiated
             */
            if ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," ).length == 2
                    && isNumeric ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 0 ].trim () )
                    && isNumeric ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 1 ].trim () ) ) {

                PLAYBACK_BI = new BufferedImage ( Integer.valueOf ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 0 ].trim () ),
                        Integer.valueOf ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 1 ].trim () ),
                        8 );

                CAMERA_BI = new BufferedImage ( Integer.valueOf ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 0 ].trim () ),
                        Integer.valueOf ( CAMERAS_DETAILS_HM.get ( "global" ).getProperty ( "camera_resolution" ).split ( "," )[ 1 ].trim () ),
                        8 );
            }
            else {

                PLAYBACK_BI = new BufferedImage ( 640, 480, 8 );

                CAMERA_BI = new BufferedImage ( 640, 480, 8 );
            }
        }

        @Override
        public void run () {

            // <editor-fold defaultstate="collapsed" desc="Record toggle button is selected."> 
            final LiveFeed LIVE_FEED = new LiveFeed ( CAMERA_BI );
            LIVE_FEED.run ();
            //</editor->

            // <editor-fold defaultstate="collapsed" desc="Record toggle button is selected.">  
            if ( TIMERS_GUI.getRecordToggleButton ().isSelected () ) {

                // <editor-fold defaultstate="collapsed" desc="Recording, spinning ascii status text.">  
                statusRecordTickCounter++;
                if ( statusRecordTickCounter == 1 ) TIMERS_GUI.getStatusBarLabel ().setText ( " recording \\" );
                else if ( statusRecordTickCounter == 2 ) TIMERS_GUI.getStatusBarLabel ().setText ( " recording |" );
                else if ( statusRecordTickCounter == 3 ) TIMERS_GUI.getStatusBarLabel ().setText ( " recording /" );
                else if ( statusRecordTickCounter == 4 ) TIMERS_GUI.getStatusBarLabel ().setText ( " recording --" );
                else if ( statusRecordTickCounter == 5 ) TIMERS_GUI.getStatusBarLabel ().setText ( " recording \\" );
                else if ( statusRecordTickCounter == 6 ) TIMERS_GUI.getStatusBarLabel ().setText ( " recording |" );
                else if ( statusRecordTickCounter == 7 ) TIMERS_GUI.getStatusBarLabel ().setText ( " recording /" );
                else if ( statusRecordTickCounter == 8 ) TIMERS_GUI.getStatusBarLabel ().setText ( " recording --" );
                else if ( statusRecordTickCounter == 9 ) TIMERS_GUI.getStatusBarLabel ().setText ( " recording \\" );
                else statusRecordTickCounter = 0;
                
                //</editor-fold>

                if ( TIMERS_GUI.getVideoAudioRadioButton ().isSelected () ) {
                    
                    final RecordImages RECORD_IMAGES = new RecordImages ( CAMERA_BI );
                    RECORD_IMAGES.run ();
                }
                else {
                    
                     /*
                        we create a separate Graphics object otherwise the font
                        setting is lost
                    */
                    Graphics g = CAMERA_BI.getGraphics ();

                    g.clearRect (0, 0, 
                                 CAMERA_BI.getWidth (),
                                 CAMERA_BI.getHeight ());

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
                    
                    final RecordImages RECORD_IMAGES = new RecordImages ( CAMERA_BI );
                    RECORD_IMAGES.run ();
                }
            }
            //</editor-fold>            

            // <editor-fold defaultstate="collapsed" desc="Playback">
            if ( TIMERS_GUI.getPlayToggleButton ().isSelected () ) {

                if ( TIMERS_GUI.getPlaybackSlider ().getValue () < TIMERS_GUI.getPlaybackSlider ().getMaximum () ) {

                    TIMERS_GUI.getPlaybackSlider ().setValue ( TIMERS_GUI.getPlaybackSlider ().getValue () + 1 );
                }
                else {
                    
                    /*
                        If recording then reset the slider value.
                    */
                    if (TIMERS_GUI.getRecordToggleButton ().isSelected ()) {

                        TIMERS_GUI.setSliderMaximumValue ();
                    }
                    else {
                        
                        TIMERS_GUI.getPlayToggleButton ().doClick ();
                    }
                }

                final Playback PLAYBACK = new Playback ( PLAYBACK_BI );
                PLAYBACK.run ();
            }
            //</editor-fold>
        }
    }
    
    /**
     * [TODO] Document. Unit test. [/]
     */
    private class Playback {

        private final BufferedImage PLAYBACK_BI;

        public Playback ( BufferedImage bi ) {

            PLAYBACK_BI = bi;
        }

        public void run () {

            try {

                final Statement STATEMENT = TIMERS_GUI.getDbStuff ().getCameraDatabaseConnection ().createStatement ();

                final String SQL = "select fileBytes, date from fileData where rowid = " + ((TIMERS_GUI.getPlaybackSlider ().getValue () == 0) ? 1 : TIMERS_GUI.getPlaybackSlider ().getValue ());
                
                final ResultSet RESULTSET;

                RESULTSET = STATEMENT.executeQuery ( SQL );

                byte[] data = null;

                while ( RESULTSET.next () ) {

                    data = RESULTSET.getBytes ( 1 );

                    TIMERS_GUI.getPlaybackDateLabel ().setText ( RESULTSET.getString ( 2 ) );
                }

                RESULTSET.close ();

                STATEMENT.closeOnCompletion ();

                if ( data != null ) {

                    try ( final ByteArrayInputStream BA_IS = new ByteArrayInputStream ( data );
                            final BufferedInputStream BIN = new BufferedInputStream ( BA_IS ) ) {

                        PLAYBACK_BI.getGraphics ().drawImage ( ImageIO.read ( BIN ),
                                0, 0,
                                PLAYBACK_BI.getWidth (),
                                PLAYBACK_BI.getHeight (),
                                null );

                        TIMERS_GUI.getBigScreen ().setBufferedImage ( PLAYBACK_BI );

                        PLAYBACK_BI.flush ();
                    }
                    catch ( final IOException IOE ) {

                        System.err.println ( IOE.getMessage () );
                    }

                    /*
                        For garbage collection.
                     */
                    data = null;
                }
            }
            catch ( SQLException sqle ) {

                System.err.println ( "LFDMS_Timers.CaptureTimerTask " + sqle.getMessage () );
            }
        }

    }

    /**
     * [TODO] Document. Unit test. [/]
     */
    private class RecordImages {

        private final BufferedImage RECORD_BI;

        public RecordImages ( BufferedImage bi ) {

            RECORD_BI = bi;
        }

        public void run () {

            // '%Y-%m-%d %H:%M:%f' Is the same as:
            // calendar = new GregorianCalendar ();
            // imageDateTime_s = calendar.get ( Calendar.YEAR ) + "-"
            //                                  + ( ( calendar.get ( Calendar.MONTH ) + 1 < 10 ) ? "0" + ( calendar.get ( Calendar.MONTH ) + 1 ) : ( calendar.get ( Calendar.MONTH ) + 1 ) ) + "-"
            //                                  + ( ( calendar.get ( Calendar.DATE ) < 10 ) ? "0" + calendar.get ( Calendar.DATE ) : calendar.get ( Calendar.DATE ) ) + " "
            //                                  + ( ( calendar.get ( Calendar.HOUR_OF_DAY ) < 10 ) ? "0" + calendar.get ( Calendar.HOUR_OF_DAY ) : calendar.get ( Calendar.HOUR_OF_DAY ) ) + ":"
            //                                  + ( ( calendar.get ( Calendar.MINUTE ) < 10 ) ? "0" + calendar.get ( Calendar.MINUTE ) : calendar.get ( Calendar.MINUTE ) ) + ":"
            //                                  + ( ( calendar.get ( Calendar.SECOND ) < 10 ) ? "0" + calendar.get ( Calendar.SECOND ) : calendar.get ( Calendar.SECOND ) ) + "."
            //                                  + calendar.get ( Calendar.MILLISECOND );
            
            try ( ByteArrayOutputStream BYTE_ARRAY_OUTPUT_STREAM = new ByteArrayOutputStream () ) {

                final String SQL_COMMAND = "insert into fileData (fileBytes, date) values(?,(select (STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW', 'localtime'))))";
                final PreparedStatement PSTMT = TIMERS_GUI.getDbStuff ().getCameraDatabaseConnection ().prepareStatement ( SQL_COMMAND );

                /*
                    [WARNING] 
                        If this is set to jpg then OpenJDK will crash
                    [/]
                 */
                ImageIO.write ( RECORD_BI,
                        "jpg",
                        BYTE_ARRAY_OUTPUT_STREAM );

                PSTMT.setBytes ( 1, BYTE_ARRAY_OUTPUT_STREAM.toByteArray () );

                PSTMT.executeUpdate ();

                PSTMT.closeOnCompletion ();

                BYTE_ARRAY_OUTPUT_STREAM.flush ();
            }
            catch ( IOException | SQLException ex ) {

                System.err.println ( "LFDMS_Timers.RecordImages " + ex.getMessage () );
            }
        }

    }

    /**
     * [TODO] Document. Unit test. [/]
     */
    private class LiveFeed {

        private final BufferedImage CAPTURE_BI;

        LiveFeed ( BufferedImage captureBi ) {

            CAPTURE_BI = captureBi;
        }

        public void run () {

            CAPTURE_BI.getGraphics ().drawImage ( cvQueryFrame ( ( opencv_highgui.CvCapture ) CAMERAS_HM.get ( TIMERS_GUI.getCameraNumber () ) ).getBufferedImage (),
                    0, 0,
                    CAPTURE_BI.getWidth (),
                    CAPTURE_BI.getHeight (),
                    null );

            /*
                Display the camera image on the live-feed label.
             */
            if ( TIMERS_GUI.getState () != JFrame.ICONIFIED
                    && CAPTURE_BI != null ) {

                TIMERS_GUI.getLiveImageLabel ().setIcon ( new ImageIcon ( CAPTURE_BI.getScaledInstance ( TIMERS_GUI.getLiveImageLabel ().getWidth (),
                        TIMERS_GUI.getLiveImageLabel ().getHeight (),
                        Image.SCALE_DEFAULT ) ) );
            }

            /*
                Display the camera image on the big screen.
             */
            if ( !TIMERS_GUI.getStatus ().getSliderHasBeenMoved () ) {
                
                if ( TIMERS_GUI.getBigScreen ().isVisible ()
                        && TIMERS_GUI.getBigScreen ().getState () != JFrame.ICONIFIED
                        && !TIMERS_GUI.getPlayToggleButton ().isSelected ()
                        && CAPTURE_BI != null ) {

                    TIMERS_GUI.getBigScreen ().setBufferedImage ( CAPTURE_BI );
                }
            }
        }
    }

    /**
     * [TODO] Documentation. [/]
     */
    public static class PlaybackAudioTimerTask extends TimerTask {

        /*
            Buffer: A collection of outputSteams.
                    Each outputStream a collection of audio bytes.
        
            BUFFERS_HM: A collection of Buffers.
        */
        private static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ByteArrayOutputStream>> BUFFERS_HM = new ConcurrentHashMap ();

        private static Integer currentBuffer_i = 0,
                               whichBufferToPlay_i = -1;

        /**
         * [TODO]
         *      Documentation.
         * [/]
         */
        public static void clearBuffers () {
            
            BUFFERS_HM = new ConcurrentHashMap ();
        }
        
        /*
            Initialise a collection of empty buffers.
        */
        public PlaybackAudioTimerTask () {
            
            for (int i = 0; i < MAX_BUFFERS_I; i++) { 
            
                BUFFERS_HM.put ( i, new ConcurrentHashMap<> () );
            }
        }

        @Override
        public void run () {
            
            if ( LFDMS_Status.getAudioPlaybackOwner () != null ) {

                try {

                    /*
                        get audio
                     */
                    final Statement AUDIO_STATEMENT = LFDMS_DbInit.getAudioDatabaseConnection ().createStatement ();

                    /*
                        select date from fileData where date >= datetime("2016-07-16 14:04:38.449") and date <= datetime("2016-07-16 14:04:38.449", '+5 seconds');
                     */
                    String SQL = "select fileBytes from fileData where date >= datetime('" + LFDMS_Status.getAudioPlaybackOwner ().getPlaybackDateLabel ().getText () + "', '+0 seconds') and date <= datetime('" + LFDMS_Status.getAudioPlaybackOwner ().getPlaybackDateLabel ().getText () + "', '+" + ( AUDIO_DURATION_I / 1000 ) + " seconds') order by date desc limit 1";

                    final ResultSet AUDIO_RESULTSET;

                    AUDIO_RESULTSET = AUDIO_STATEMENT.executeQuery ( SQL );

                    final ByteArrayOutputStream DATA_BYTE_ARRAY_OUTPUTSTREAM = new ByteArrayOutputStream ( 1024 );

                    /*
                        closed when there aren't any more audio frames
                        this seems to happen when playback catches up with
                        audio recording
                     */
                    while ( AUDIO_RESULTSET.next () ) {

                        /*
                            Push audio bytes into an outputStream.
                         */
                        DATA_BYTE_ARRAY_OUTPUTSTREAM.write ( AUDIO_RESULTSET.getBytes ( 1 ) );
                    }
                    
                    /*
                        currentBuffer_i is the key of the buffer we are filling.
                        If it not filled (MAX_NUMBER_OF_OS_I) then add another 
                        outputStream.
                    */
                    if ( BUFFERS_HM.get ( currentBuffer_i ).size () < MAX_NUMBER_OF_OS_I ) {

                        /*
                            if MAX_NUMBER_OF_OS_I = 3 then
                            BUFFERS_HM.get ( currentBuffer_i ).size () will be: 0, 1, 2
                            which is why we use it as key value below; 
                            the outputstream keys get incremented.
                        */
                        BUFFERS_HM.get ( currentBuffer_i ).put ( BUFFERS_HM.get ( currentBuffer_i ).size (), 
                                                                 DATA_BYTE_ARRAY_OUTPUTSTREAM );

                        /*
                            how many output streams does the current buffer have ?
                            Is it more than the allowed maximum ?
                        */
                        if ( BUFFERS_HM.get ( currentBuffer_i ).size () >= MAX_NUMBER_OF_OS_I ) {

                            /*
                                The current buffer is full so we need to increment
                                the buffer counter so we start populating the 
                                next buffer.
                            
                                This is also the buffer we send to the audio
                                player.
                            
                                The buffer is emptied by LFDMS_PlayAudioOutputStream.
                            */
                            whichBufferToPlay_i = currentBuffer_i;
                            
                            if ( currentBuffer_i < MAX_BUFFERS_I -1 ) {

                                currentBuffer_i++;
                            }
                            else {

                                currentBuffer_i = 0;
                            }

                            if ( whichBufferToPlay_i > -1 // -1, the initial/ignore value
                                 && !BUFFERS_HM.get ( whichBufferToPlay_i ).isEmpty () ) {

                                final LFDMS_PlayAudioOutputStream PLAY_AUDIO = new LFDMS_PlayAudioOutputStream ( LFDMS_AudioInit.getAudioFormat (),
                                                                                                                 BUFFERS_HM.get ( whichBufferToPlay_i ) );

                                PLAY_AUDIO.start ();
                            }
                        }
                    }

                    AUDIO_RESULTSET.close ();

                    AUDIO_STATEMENT.closeOnCompletion ();
                    
                }
                catch ( IOException | SQLException | NullPointerException ex ) { // IOException | LineUnavailableException ex) {

                    System.err.println ( "LFDMS_Timers.PlaybackAudioTimerTask " + ex.getMessage () );
                }
            }
        }
    }

    /**
     * [TODO] Documentation. Unit test. [/]
     *
     * @return
     */
    public static final CaptureAudioTimerTask GET_NEW_CAPTURE_AUDIO_TIMERTASK () {

        return new CaptureAudioTimerTask ();
    }

    /**
     * [TODO] Documentation. [/]
     */
    public static class CaptureAudioTimerTask extends TimerTask {

        @Override
        public void run () {

            final Long NOW = System.currentTimeMillis () + AUDIO_DURATION_I;

            final byte BUFFER[] = new byte[ 1024 ];

            try ( final ByteArrayOutputStream AUDIO_BA_OS = new ByteArrayOutputStream () ) {

                while ( System.currentTimeMillis () < NOW ) {

                    int count = LFDMS_AudioInit.getLine ().read ( BUFFER, 0, BUFFER.length );

                    if ( count > 0 ) {

                        AUDIO_BA_OS.write ( BUFFER, 0, count );
                    }
                }

                LFDMS_AudioInit.getLine ().flush ();

                byte[] bytesOut = AUDIO_BA_OS.toByteArray ();

                AUDIO_BA_OS.flush ();

                final String SQL_COMMAND = "insert into fileData (fileBytes, date) values(?,(select (STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW', 'localtime'))))";

                final PreparedStatement PSTMT = LFDMS_DbInit.getAudioDatabaseConnection ().prepareStatement ( SQL_COMMAND );

                PSTMT.setBytes ( 1, bytesOut );

                PSTMT.executeUpdate ();

                PSTMT.closeOnCompletion ();

                /*
                    for garbage collection
                */
                bytesOut = null;
            }
            catch ( IOException | SQLException | NullPointerException ex ) {

                System.err.println ( "LFDMS_Timers.recordAudioForAfewSeconds " + ex.getMessage () );
            }
        }

    }

    /**
     * [TODO] Documentation. Unit test, return value. [/]
     */
    public class HideTimerTask extends TimerTask {

        @Override
        public void run () {

            TIMERS_GUI.setVisible ( Boolean.FALSE );
        }

    }
}
