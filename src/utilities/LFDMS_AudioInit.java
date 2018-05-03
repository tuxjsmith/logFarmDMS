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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 * @author tuxjsmith@gmail.com
 */
public class LFDMS_AudioInit {

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/]
     * @return the line
     */
    public static TargetDataLine getLine () {
        
        return line;
    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/]
     * @param aLine the line to set
     */
    public static void setLine ( TargetDataLine aLine ) {
        
        line = aLine;
    }
    
    private static final AudioFormat AUDIO_FORMAT = getAudioFormat ();
    private static final DataLine.Info CAPTURE_DATA_LINE_INFO = new DataLine.Info (TargetDataLine.class, AUDIO_FORMAT);
    private static TargetDataLine line = null;
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/]
     */
    public static final void AUDIO_INIT () {
    
        try {
            
            setLine ( (TargetDataLine) AudioSystem.getLine (CAPTURE_DATA_LINE_INFO) );

            getLine ().open (AUDIO_FORMAT);

            getLine ().start ();
        }
        catch (IllegalArgumentException | LineUnavailableException  | NullPointerException ex) {
         
            System.err.println ("LFDMS_AudioInit.AUDIO_INIT " + ex.getMessage ());
        }
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/]
     * @return 
     */
    public static AudioFormat getAudioFormat () {

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
