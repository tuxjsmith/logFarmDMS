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

/**
 * @author tuxjsmith@gmail.com
 */
public class LFDMS_Status {
    
    public LFDMS_Status () {}

    private Boolean showPlayBackImages_b = Boolean.FALSE, 
                    sliderHasBeenMoved_b = Boolean.FALSE;
    /*
        If either of these are true then the audio database connection is not 
        closed, if both are false then it is.
    */
    private static Boolean audioPlayback_b = Boolean.FALSE, 
                           audioRecord_b = Boolean.FALSE,
                           captureAudio_b = Boolean.TRUE;
    
    /*
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     */
    private static Integer audioPlaybackRowId_i = -1;
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * 
     * @return the showPlayBackImages_b
     */
    public Boolean getShowPlayBackImages () {
     
        return showPlayBackImages_b;
    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @param b the showPlayBackImages_b to set
     */
    public void setShowPlayBackImages ( Boolean b ) {
        
        showPlayBackImages_b = b;
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
     * @return the audioPlayback_b
     */
    public static Boolean getAudioPlayback () {
        
        return audioPlayback_b;
    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @param b the audioPlayback_b to set
     */
    public static void setAudioPlayback ( Boolean b ) {
        
        audioPlayback_b = b;
    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return the audioRecord_b
     */
    public static Boolean getAudioRecord () {
        
        return audioRecord_b;
    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @param b the audioRecord_b to set
     */
    public static void setAudioRecord ( Boolean b ) {
        
        audioRecord_b = b;
    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * @return the audioPlaybackRowId_i
     */
    public static Integer getAudioPlaybackRowId () {
        
        return audioPlaybackRowId_i;
    }

    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     * 
     * @param i the audioPlaybackRowId_i to set
     */
    public static void setAudioPlaybackRowId ( Integer i ) {
        
        audioPlaybackRowId_i = i;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.       
     * [/]
     */
    public static void incrementAudioPlaybackRowId () {
     
        audioPlaybackRowId_i++;
    }
}
