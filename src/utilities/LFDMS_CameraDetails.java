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

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tuxjsmith@gmail.com
 */
public class LFDMS_CameraDetails {
    
    final private ConcurrentHashMap<String, String> PROPERTIES_HM = new ConcurrentHashMap ();

    public LFDMS_CameraDetails () {}
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/]
     * @param number
     * @param maximum_db_size
     * @param db_location
     * @param enabled
     * @param iconify_at_start
     * @param hide
     * @param start_recording_at_startup
     * @param record_video_and_audio_by_default
     * @param record_audio_only_by_default
     * @param mute_playback_by_default
     * @param screen_location 
     */
    public LFDMS_CameraDetails (String number,
                          String maximum_db_size,
                          String db_location,
                          String enabled,
                          String iconify_at_start,
                          String hide,
                          String start_recording_at_startup,
//                          String record_video_and_audio_by_default,
                          String record_audio_only_by_default,
                          String mute_playback_by_default,
                          String screen_location) {
        
        PROPERTIES_HM.put ("number", number);
        PROPERTIES_HM.put ("maximum_db_size", maximum_db_size);
        PROPERTIES_HM.put ("db_location", db_location);
        PROPERTIES_HM.put ("enabled", enabled);
        PROPERTIES_HM.put ("iconify_at_start", iconify_at_start);
        PROPERTIES_HM.put ("hide", hide);
        PROPERTIES_HM.put ("start_recording_at_startup", start_recording_at_startup);
//        PROPERTIES_HM.put ("record_video_and_audio_by_default", record_video_and_audio_by_default);
        PROPERTIES_HM.put ("record_audio_only_by_default", record_audio_only_by_default);
        PROPERTIES_HM.put ("mute_playback_by_default", mute_playback_by_default);
        PROPERTIES_HM.put ("screen_location", screen_location);
    }
    
    /**
     * [TODO]
     *      More informative parameter name.
     *      Documentation.
     *      Unit test.
     * [/]
     * @param s
     * @return 
     */
    public String getProperty (String s) {
        
        return PROPERTIES_HM.get (s);
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/]
     * @param key
     * @param value 
     */
    public void setProperty (String key,
                             String value) {
        
        PROPERTIES_HM.put (key, value);
    }
}
