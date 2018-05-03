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

import JSON.JSONArray;
import JSON.JSONObject;
import JSON.JSONWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import static utilities.LFDMS_Constants.CAMERAS_DETAILS_HM;

/**
 * @author tuxjsmith@gmail.com
 */
public class LFDMS_OpenConfiguration {

    public LFDMS_OpenConfiguration () {
    }

    /**
     * Read details from the configuration file. If the file does not exist<br>
     * then create a new one.
     * 
     * [TODO]
     *      Unit test.
     * [/]
     */
    public static void openConfiguration () {

        if ( !new File ( System.getProperty ("user.dir") + System.getProperty ("file.separator") + "configuration.json" ).exists () ) {
            
            writeDefaultConfiguration ();
        }
        
        try (BufferedReader br = new BufferedReader ( new FileReader ( System.getProperty ("user.dir") + System.getProperty ("file.separator") + "configuration.json" ))) {

            String line_s, jsonLine_s = "";

            while ((line_s = br.readLine ()) != null) {

                jsonLine_s += line_s;
            }

            JSONObject jsonObject = new JSONObject (jsonLine_s);

            //<editor-fold defaultstate="collapsed" desc="Global configuration settings">
            
            /*
                [TODO] 
                    Can global_cd be final ?
                [/TODO]
            */
            LFDMS_CameraDetails global_cd = new LFDMS_CameraDetails ();
            {

                if (jsonObject.has ("number_of_cameras")) {

                    global_cd.setProperty ("number_of_cameras", jsonObject.getString ("number_of_cameras"));
                }
                else {

                    global_cd.setProperty ("number_of_cameras", "2");
                }

                if (jsonObject.has ("audio_db_location")) {

                    global_cd.setProperty ("audio_db_location", jsonObject.getString ("audio_db_location"));
                }
                else {

                    global_cd.setProperty ("audio_db_location", System.getProperty ("user.dir"));
                }
                
                if (jsonObject.has ("camera_resolution")) {
                    
                    global_cd.setProperty ("camera_resolution", jsonObject.getString ("camera_resolution"));
                }
                else {
                    
                    global_cd.setProperty ("camera_resolution", jsonObject.getString ("320,240"));
                }
                
                CAMERAS_DETAILS_HM.put ("global", global_cd);
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Individual camera settings">
            if (jsonObject.has ("cameras")) {

                JSONArray jsonArray = jsonObject.getJSONArray ("cameras");

                for (int i = 0; i < jsonArray.length (); i++) {

                    JSONObject cameraObject = jsonArray.getJSONObject (i);

                    /*
                        [TODO] 
                            Another final candidate.
                        [/TODO]
                    */
                    LFDMS_CameraDetails cd = new LFDMS_CameraDetails ((cameraObject.has ("number")) ? cameraObject.getString ("number") : Integer.toString (CAMERAS_DETAILS_HM.size () - 1),
                                                          (cameraObject.has ("maximum_db_size")) ? cameraObject.getString ("maximum_db_size") : "10",
                                                          (cameraObject.has ("db_location")) ? cameraObject.getString ("db_location") : System.getProperty ("user.dir"),
                                                          (cameraObject.has ("enabled")) ? cameraObject.getString ("enabled") : "yes",
                                                          (cameraObject.has ("iconify_at_start")) ? cameraObject.getString ("iconify_at_start") : "no",
                                                          (cameraObject.has ("hide")) ? cameraObject.getString ("hide") : "no",
                                                          (cameraObject.has ("start_recording_at_startup")) ? cameraObject.getString ("start_recording_at_startup") : "yes",
//                                                          (cameraObject.has ("record_video_and_audio_by_default")) ? cameraObject.getString ("record_video_and_audio_by_default") : "yes",
                                                          (cameraObject.has ("record_audio_only_by_default")) ? cameraObject.getString ("record_audio_only_by_default") : "no",
                                                          (cameraObject.has ("mute_playback_by_default")) ? cameraObject.getString ("mute_playback_by_default") : "no",
                                                          (cameraObject.has ("screen_location")) ? cameraObject.getString ("screen_location") : "0,0");

                    CAMERAS_DETAILS_HM.put (cd.getProperty ("number"), cd);
                }
            }
            //</editor-fold>
        }
        catch (IOException ex) {

            System.err.println (ex.getMessage ());
        }
    }

    /**
     * Create a default configuration file.
     * 
     * [TODO]
     *      Unit test.
     * [/]
     */
    public static void writeDefaultConfiguration () {

        final Integer NUMBER_OF_CAMERAS = 2;

        try (FileWriter writer = new FileWriter ( System.getProperty ("user.dir") + System.getProperty ("file.separator") + "configuration.json" )) {

            JSONWriter jsonWriter = new JSONWriter (writer);
            
            /*
                we create a single object here 
                and then add keys to it, cameras is a key but with an array as its value
                then at the very end we close this object
            */
            jsonWriter.object ()
                    .key ("audio_db_location").value (System.getProperty ("user.dir"))
                    .key ("number_of_cameras").value (NUMBER_OF_CAMERAS.toString ())
                    .key ("camera_resolution").value ("640,480");
            
            //<editor-fold defaultstate="collapsed" desc="Create an array of cameras">
            jsonWriter.key ("cameras");
            {
                jsonWriter.array ();
                {
                    for (int i = 0; i < NUMBER_OF_CAMERAS; i++) {

                        jsonWriter.object ()
                                .key ("number").value (Integer.toString (i))
                                .key ("maximum_db_size").value ("10")
                                .key ("db_location").value (System.getProperty ("user.dir"))
                                .key ("enabled").value ("yes")
                                .key ("iconify_at_start").value ("no")
                                .key ("hide").value ("no")
                                .key ("start_recording_at_startup").value ("yes")
//                                .key ("record_video_and_audio_by_default").value ("yes")
                                .key ("record_audio_only_by_default").value ("no")
                                .key ("mute_playback_by_default").value ("no")
                                .key ("screen_location").value (Integer.toString (i*75) + "," + Integer.toString (i*60));
                        
                        jsonWriter.endObject ();
                    }
                }
                jsonWriter.endArray ();
            }
            //</editor-fold>
            
            /*
                close object
            */
            jsonWriter.endObject ();
        }
        catch (IOException ioe) {

            System.err.println (ioe.getMessage ());
            
            System.exit (0); 
        }
    }
}
