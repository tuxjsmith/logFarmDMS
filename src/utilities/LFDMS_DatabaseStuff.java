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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import static utilities.LFDMS_Constants.CAMERAS_DETAILS_HM;

/**
 * @author tuxjsmith@gmail.com
 */
public class LFDMS_DatabaseStuff {

    /*
        A new instance of cameraDatabaseConnection is created for each camera.
    */
    private Connection cameraDatabaseConnection;
    
    /*
        There will only be a single audio database connection used for playback
        by all cameras so it's available from LFDMS_DatabaseStuff as a static
        object.
    */
    private static Connection audioDatabaseConnection;
    
    public LFDMS_DatabaseStuff () {}
    
    /**
     * [TODO]
     *      Unit test
     * [/]
     * @return the cameraDatabaseConnection
     */
    public Connection getCameraDatabaseConnection () {
       
        return cameraDatabaseConnection;
    }

    /**
     * [TODO]
     *      Unit test
     * [/]
     * @param dbc the cameraDatabaseConnection to set
     */
    public void setCameraDatabaseConnection ( Connection dbc ) {
        
        cameraDatabaseConnection = dbc;
    }

    /**
     * [TODO]
     *      Unit test
     * [/]
     * @return the audioDatabaseConnection
     */
    public static Connection getAudioDatabaseConnection () {
        
        return audioDatabaseConnection;
    }

    /**
     * [TODO]
     *      Unit test
     * [/]
     * @param dbc the audioDatabaseConnection to set
     */
    public static void setAudioDatabaseConnection ( Connection dbc ) {
        
        audioDatabaseConnection = dbc;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/]
     * 
     * @param cameraNumber_s
     * @return 
     */
    public final Boolean INIT_CAMERA_DATABASE (String cameraNumber_s) {
        
        try {

            /*
                Make sure org.sqlite.JDBC is in the class path
                and throw a ClassNotFoundException if it isn't.
            
                For development it is located here: 
                opencv_for_logfarmDMS/sqlite-jdbc-3.8.11.2.jar
            */
            Class.forName ("org.sqlite.JDBC");

            if (!new File ( CAMERAS_DETAILS_HM.get(cameraNumber_s).getProperty ("db_location") + System.getProperty ("file.separator") + "logFarmDMS_" + cameraNumber_s + ".db" ).exists ()) {

                setCameraDatabaseConnection ( DriverManager.getConnection ("jdbc:sqlite:" 
                                                                                    + CAMERAS_DETAILS_HM.get(cameraNumber_s).getProperty ("db_location") 
                                                                                    + System.getProperty ("file.separator") 
                                                                                    + "logFarmDMS_" + cameraNumber_s + ".db" ) );

                final Statement STATEMENT = getCameraDatabaseConnection ().createStatement ();
                STATEMENT.setQueryTimeout (30);  // set timeout to 30 sec.

                STATEMENT.executeUpdate ("create table fileData (date text,"
                                                              + "fileBytes blob)");

                STATEMENT.closeOnCompletion ();
            }
            else {

                setCameraDatabaseConnection ( DriverManager.getConnection ("jdbc:sqlite:" +
                                                       CAMERAS_DETAILS_HM.get(cameraNumber_s).getProperty ("db_location") 
                                                       + System.getProperty ("file.separator") + 
                                                       "logFarmDMS_" + cameraNumber_s + ".db" ) );
            }
        }
        catch (ClassNotFoundException | SQLException  | NullPointerException ex) {

            System.err.println ("initCameraDatabase " + ex.getMessage ());
            
            JOptionPane.showMessageDialog (null, "<html><body>"
                    + "Sorry, the database file path is wrong.<br><br>"
                    + "This path is set in <b>configuration.json</b>.<br><br>"
                    + "Solution: edit or delete <b>configuration.json</b><br> "
                    + "and try again."
                    + "</body></html>", "Database path is wrong", JOptionPane.ERROR_MESSAGE);
            
            System.exit (0); 
            
            return Boolean.FALSE;
        }
        
        return Boolean.TRUE;
    }
    
    /**
     * [TODO]
     *      Documentation.
     *      Unit test.
     * [/]
     * 
     * @return Boolean for unit testing 
     */
    public static final Boolean INIT_AUDIO_DATABASE () {
        
        try {
    
            /*
                calling this seems to simply make sure the class in the path
                and throws a ClassNotFoundException if it isn't
            */
            Class.forName ("org.sqlite.JDBC");
            /*
                create an audio database if one does not exist
            */
            if (!new File (CAMERAS_DETAILS_HM.get("global").getProperty ("audio_db_location") 
                           + System.getProperty ("file.separator") 
                           + "logFarmDMSaudio.db").exists ()) {

                LFDMS_DatabaseStuff.setAudioDatabaseConnection (DriverManager.getConnection ("jdbc:sqlite:" 
                                                                                  + CAMERAS_DETAILS_HM.get("global").getProperty ("audio_db_location") 
                                                                                  + System.getProperty ("file.separator") 
                                                                                  + "logFarmDMSaudio.db") );

                final Statement STATEMENT = LFDMS_DatabaseStuff.getAudioDatabaseConnection ().createStatement ();
                STATEMENT.setQueryTimeout (30);  // set timeout to 30 sec.

                STATEMENT.executeUpdate ("create table fileData (date text,"
                                                              + "fileBytes blob)");

                STATEMENT.closeOnCompletion ();
            }
            else {
                
                LFDMS_DatabaseStuff.setAudioDatabaseConnection ( DriverManager.getConnection ("jdbc:sqlite:" 
                                                                                   + CAMERAS_DETAILS_HM.get("global").getProperty ("audio_db_location") 
                                                                                   + System.getProperty ("file.separator") 
                                                                                   + "logFarmDMSaudio.db") ); 
            }
            /*
                end create audio database
            */
        }
        catch (ClassNotFoundException | SQLException  | NullPointerException ex) {
            
            /*
                [TODO]
                    An error message similar to the camera database error message.
                [/]
            */

            System.err.println ("initAudioDatabase " + ex.getMessage ());
            
            return Boolean.FALSE;
        }
        
        return Boolean.TRUE;
    }
    
}
