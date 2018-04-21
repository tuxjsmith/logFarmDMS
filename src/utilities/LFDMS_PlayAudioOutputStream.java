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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * @author tuxjsmith@gmail.com
 */
public class LFDMS_PlayAudioOutputStream extends Thread {

    private static SourceDataLine playbackLine = null;
    private static AudioFormat audioFormat;
    private static DataLine.Info dataLineInfo;
    private static ByteArrayOutputStream byteArrayOutputStream;
    private static ConcurrentHashMap<Integer, ByteArrayOutputStream> playBuffer_hm = new ConcurrentHashMap ();


    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/]
     * @param af
     * @param dbaos 
     */
    public LFDMS_PlayAudioOutputStream (AudioFormat af,
                                        ByteArrayOutputStream dbaos) {
    
        openPlay (af,
                  dbaos);
    }
    
    /**
     * [TODO]
     *      Document.
     *      Unit test.
     * [/]
     * @param af
     * @param buffer
     */
    public LFDMS_PlayAudioOutputStream (AudioFormat af,
                                        ConcurrentHashMap<Integer, ByteArrayOutputStream> buffer) {
    
        openPlayForBuffer ( af,
                            buffer );
    }
    
    /**
     * [TODO]
     *      Document.
     *      Unit test.
     * [/]
     * @param af
     * @param buffer
     */
    public static final void openPlayForBuffer ( AudioFormat af,
                                                 ConcurrentHashMap<Integer, ByteArrayOutputStream> buffer ) {
        
        playBuffer_hm = buffer;

        audioFormat = af;

        dataLineInfo = new DataLine.Info (SourceDataLine.class,
                                            af);
        
        if ( playbackLine != null ) {
        
            playbackLine.drain ();
            playbackLine.flush ();
        }
        
        if ( playbackLine == null
             || !playbackLine.isOpen ()) {
            
            try {

                playbackLine = (SourceDataLine) AudioSystem.getLine (dataLineInfo);

                playbackLine.open (audioFormat);
                playbackLine.start ();
            }
            catch (LineUnavailableException | NullPointerException ex) {

                System.err.println ("PlayAudio [1] " + ex.getMessage ());

                stopPlay ();
            }
        }
    }
    
    /**
     * [TODO]
     *      Document.
     *      Unit test.
     * [/]
     * @param af
     * @param dbaos
     */
    public static final void openPlay ( AudioFormat af,
                                        ByteArrayOutputStream dbaos ) {
        
        byteArrayOutputStream = dbaos;

        audioFormat = af;

        dataLineInfo = new DataLine.Info (SourceDataLine.class,
                                            af);
        
        if ( playbackLine != null ) {
        
            playbackLine.drain ();
            playbackLine.flush ();
        }
        
        if ( playbackLine == null
             || !playbackLine.isOpen ()) {
            
            try {

                playbackLine = (SourceDataLine) AudioSystem.getLine (dataLineInfo);

                playbackLine.open (audioFormat);
                playbackLine.start ();
            }
            catch (LineUnavailableException | NullPointerException ex) {

                System.err.println ("PlayAudio [1] " + ex.getMessage ());

                stopPlay ();
            }
        }
    }
    
    /**
     * [TODO]
     *      Document.
     *      Unit test.
     * [/]
     */
    public static final void stopPlay () {
        
        if ( playbackLine != null ) {
            
            playbackLine.stop ();
            playbackLine.flush ();
            playbackLine.drain ();
            playbackLine.close ();
            
            try {
                
                if ( byteArrayOutputStream != null ) {
                
                    byteArrayOutputStream.flush ();
                    byteArrayOutputStream.close ();
                }
            }
            catch (IOException ioe) {
                
                System.err.println ( ioe.getMessage () );
            }
        }
        
        playBuffer_hm.clear ();
    }
    
    /**
     * Is playbackLine already being used for play back.
     * 
     * [TODO]
     *      Documentation.
     *      Unit Test.
     * [/]
     * @return 
     */
    public static Boolean isPlaying () {
        
        if ( playbackLine != null ) {
        
            return playbackLine.isOpen ();
        }
        else {
            
            return Boolean.FALSE;
        }
    }
    
    /**
     * [TODO]
     *      Document.
     *      Unit test.
     * [/]
     */
    private void playSingleSound () {

        if ( byteArrayOutputStream != null ) {

            try {

                byte[] data = byteArrayOutputStream.toByteArray ();

                byteArrayOutputStream.flush ();
                byteArrayOutputStream.reset ();
                byteArrayOutputStream.close ();

                try (final AudioInputStream AUDIO_INPUT_STREAM
                        = new AudioInputStream (new BufferedInputStream (new ByteArrayInputStream (data)),
                                                audioFormat,
                                                data.length)) {

                    final byte BUFFER2[] = new byte[1024];
                    int count;

                    while ((count = AUDIO_INPUT_STREAM.read (BUFFER2,
                                                             0,
                                                             1024)) > 0) {

                        if ( playbackLine != null ) {

                            playbackLine.write (BUFFER2,
                                                0,
                                                count);
                        }
                    }    
                }
            }
            catch (IOException ioe) {

                stopPlay ();

                System.err.println ("LFDMS_PlayAudioOutputStream [3] " + ioe.getMessage ());
            }
        }
    }
    
    /**
     * [TODO]
     *      Document.
     *      Unit test.
     * [/]
     */
    private void playBufferOfSounds () {
        
        System.out.println ( LFDMS_Status.isMuted () );
        
        if ( !LFDMS_Status.isMuted () ) {
        
            /*
                loop required to convert byte to Byte.
             */
            final ArrayList<Byte> DATA_AL = new ArrayList ();

            for ( int i = 0; i < playBuffer_hm.size (); i++ ) {

                for ( byte b : playBuffer_hm.get ( i ).toByteArray () ) {

                    DATA_AL.add ( b );
                }
            }

            playBuffer_hm.clear ();

            final byte[] DATA = new byte[ DATA_AL.size () ];

            for ( int i = 0; i < DATA_AL.size (); i++ ) {

    //            DATA[ i ] = DATA_AL.get ( i ).byteValue ();

                DATA[ i ] = DATA_AL.get ( i );
            }

            /*
                We have the audio bytes so now push them in to the audio pipe.
            */
            try ( final AudioInputStream AUDIO_INPUT_STREAM
                    = new AudioInputStream ( new BufferedInputStream ( new ByteArrayInputStream ( DATA ) ),
                            audioFormat,
                            DATA.length ) ) {

                final byte BUFFER2[] = new byte[ 1024 ];
                int count;

                while ( ( count = AUDIO_INPUT_STREAM.read ( BUFFER2,
                        0,
                        1024 ) ) > 0 ) {

                    if ( playbackLine != null ) {

                        playbackLine.write ( BUFFER2,
                                0,
                                count );
                    }
                }
            }
            catch ( IOException ioe ) {

                stopPlay ();

                System.err.println ( "LFDMS_PlayAudioOutputStream [3] " + ioe.getMessage () );
            }
        }
    }
    
    @Override
    public void run () {

        if (playBuffer_hm.isEmpty ()) {
            
            playSingleSound ();
        }
        else {
            
            playBufferOfSounds ();
        }
    }
}
