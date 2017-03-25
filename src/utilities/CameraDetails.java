/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;

import java.util.concurrent.ConcurrentHashMap;

public class CameraDetails {
    
    final private ConcurrentHashMap<String, String> PROPERTIES_HM = new ConcurrentHashMap ();

    public CameraDetails () {}
    
    public CameraDetails (String number,
                          String maximum_db_size,
                          String db_location,
                          String enabled,
                          String iconify_at_start,
                          String hide,
                          String start_recording_at_startup,
                          String record_video_and_audio_by_default,
//                          String record_video_only_by_default,
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
        PROPERTIES_HM.put ("record_video_and_audio_by_default", record_video_and_audio_by_default);
//        PROPERTIES_HM.put ("record_video_only_by_default", record_video_only_by_default);
        PROPERTIES_HM.put ("record_audio_only_by_default", record_audio_only_by_default);
        PROPERTIES_HM.put ("mute_playback_by_default", mute_playback_by_default);
        PROPERTIES_HM.put ("screen_location", screen_location);
    }
    
    public String getProperty (String s) {
        
        return PROPERTIES_HM.get (s);
    }
    
    public void setProperty (String key,
                             String value) {
        
        PROPERTIES_HM.put (key, value);
    }
}
