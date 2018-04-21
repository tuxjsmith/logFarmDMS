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

import logfarmdms.LFDMS_GUI;

/**
 * @author tuxjsmith@gmail.com
 */
public class LFDMS_Status {

    private static LFDMS_GUI audioPlaybackOwner = null;

    public LFDMS_Status () {}

    private Boolean sliderHasBeenMoved_b = Boolean.FALSE;
    /*
        If either of these are true then the audio database connection is not 
        closed, if both are false then it is.
    */
    private static Boolean captureAudio_b = Boolean.TRUE,
                           muteAudio_b = Boolean.FALSE;

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/] 
     * @return the muteAudio_b
     */
    public static Boolean isMuted () {
        
        return muteAudio_b;
    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/] 
     * @param b the muteAudio_b to set
     */
    public static void setMuteAudio ( Boolean b ) {
        
        muteAudio_b = b;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * 
     * @return the sliderHasBeenMoved_b
     */
    public Boolean getSliderHasBeenMoved () {
        
        return sliderHasBeenMoved_b;
    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/] 
     *
     * @param b the sliderHasBeenMoved_b to set
     */
    public void setSliderHasBeenMoved ( Boolean b ) {
        
        sliderHasBeenMoved_b = b;
    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return the captureAudio_b
     */
    public static Boolean getCaptureAudio () {
        
        return captureAudio_b;
    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @param b the captureAudio_b to set
     */
    public static void setCaptureAudio ( Boolean b ) {
        
        captureAudio_b = b;
    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return the audioPlaybackOwner
     */
    public static LFDMS_GUI getAudioPlaybackOwner () {
        
        return audioPlaybackOwner;
    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @param g the audioPlaybackOwner to set
     */
    public static void setAudioPlaybackOwner ( LFDMS_GUI g ) {
        
        audioPlaybackOwner = g;
    }
}
