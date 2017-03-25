/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;

import java.util.concurrent.ConcurrentHashMap;
import logfarmdms.GUI;


public interface Constants {

    String VERSION = "3.01 alpha";
    
    /*
        guis are not the same as cameras, this collection is used to clean up
        and close timers when the application is closed
    */
    ConcurrentHashMap<Integer, GUI> GUIS_HM = new ConcurrentHashMap ();
    
    /*
        use this collection to store camera numbers and capture objects (the cameras)
    */
    ConcurrentHashMap<Integer, Object> CAMERAS_HM = new ConcurrentHashMap ();
    
    /*
        camera configuration details
    */
    ConcurrentHashMap<String, CameraDetails> CAMERAS_DETAILS_HM = new ConcurrentHashMap ();
}
