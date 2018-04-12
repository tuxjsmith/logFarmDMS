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
import logfarmdms.LFDMS_GUI;

/**
 * @author tuxjsmith@gmail.com
 */
public interface LFDMS_Constants {

    String VERSION = "3.01 alpha";
    
    /*
        GUIs are not the same as cameras, this collection is used to clean up
        and close timers when the application is closed.
    */
    ConcurrentHashMap<Integer, LFDMS_GUI> GUIS_HM = new ConcurrentHashMap ();
    
    /*
        Use this collection to store camera numbers and OpenCV camera objects.
    */
    ConcurrentHashMap<Integer, Object> CAMERAS_HM = new ConcurrentHashMap ();
    
    /*
        Camera preferences retrieved from the configuration file.
    */
    ConcurrentHashMap<String, LFDMS_CameraDetails> CAMERAS_DETAILS_HM = new ConcurrentHashMap ();
}
