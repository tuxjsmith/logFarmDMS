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
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class PlayAudio extends Thread {

    private SourceDataLine playbackLine = null;
    private final AudioFormat AUDIO_FORMAT;
    private final DataLine.Info DATA_LINE_INFO;
    private final ByteArrayOutputStream DBA_OS;

    public PlayAudio (AudioFormat af,
                      ByteArrayOutputStream dbaos) {

        DBA_OS = dbaos;

        AUDIO_FORMAT = af;

        DATA_LINE_INFO = new DataLine.Info (SourceDataLine.class,
                                            af);
        
        try {

            playbackLine = (SourceDataLine) AudioSystem.getLine (DATA_LINE_INFO);

            playbackLine.open (AUDIO_FORMAT);
            playbackLine.start ();
        }
        catch (LineUnavailableException | NullPointerException ex) {

            System.err.println ("PlayAudio [1] " + ex.getMessage ());

            playbackLine.close ();
        }
    }

    @Override
    public void run () {

//        System.out.println ("play audio :: dataByteArrayOutputStream.size " + DBA_OS.size ());

        try {

            byte[] data = DBA_OS.toByteArray ();
            DBA_OS.flush ();
            DBA_OS.reset ();
//            DBA_OS.close ();

            try (final AudioInputStream AUDIO_INPUT_STREAM
                    = new AudioInputStream (new BufferedInputStream (new ByteArrayInputStream (data)),
                                            AUDIO_FORMAT,
                                            data.length)) {

                final byte BUFFER2[] = new byte[1024];
                int count;

                while ((count = AUDIO_INPUT_STREAM.read (BUFFER2,
                                                         0,
                                                         1024)) > 0) {

                    playbackLine.write (BUFFER2,
                                        0,
                                        count);
                }
                
                playbackLine.flush ();
                playbackLine.stop ();
                playbackLine.close ();
            }
        }
        catch (IOException ioe) {

            playbackLine.stop ();
            playbackLine.close ();
            
            System.err.println ("playAudio [2] " + ioe.getMessage ());
        }

//        System.out.println ("thread terminated " + this.getName () + "\n");
    }
}
