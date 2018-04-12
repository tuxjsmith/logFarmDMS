/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logfarmdms;
    
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tuxjsmith
 */
public class GUITest {
    
    public GUITest () {
    }
    
    @BeforeClass
    public static void setUpClass () {
        
        System.out.println ("before class");
    }
    
    @AfterClass
    public static void tearDownClass () {
    }
    
    @Before
    public void setUp () {
    }
    
    @After
    public void tearDown () {
    }

    /**
     * Test of cleanUp method, of class GUI.
     */
//    @Test
//    public void testCleanUp () {
//        System.out.println ("cleanUp");
//        LFDMS_GUI instance = new LFDMS_GUI ();
//        instance.cleanUp ();
//        // TODO review the generated test code and remove the default call to fail.
//        fail ("The test case is a prototype.");
//    }

    /**
     * Test of main method, of class LFDMS_GUI.
     */
//    @Test
//    public void testMain () {
//        System.out.println ("main");
//        String[] args = null;
//        LFDMS_GUI.main (args);
//        // TODO review the generated test code and remove the default call to fail.
//        fail ("The test case is a prototype.");
//    }
    
//    @Test
//    public void testSetSliderMaximumValue () {
//        
//        System.out.println ("setSliderMaximumValue");
//        LFDMS_GUI instance = new LFDMS_GUI ();
//        Boolean expResult = Boolean.TRUE,
//                result = instance.setSliderMaximumValue ();
//        assertEquals (expResult, result);
//    }

//    @Test
//    public void testInitCameraDatabase () {
//        
//        System.out.println ("initCameraDatabase");
//        LFDMS_GUI instance = new LFDMS_GUI ();
//        Boolean expResult = Boolean.TRUE,
//                result = instance.INIT_CAMERA_DATABASE ();
//        assertEquals (expResult, result);
//        
//        result = instance.setSliderMaximumValue ();
//        assertEquals (expResult, result);
//    }
    
//    @Test
//    public void testInitAudioDatabase () {
//        
//        System.out.println ("initAudioDatabase");
//        LFDMS_GUI instance = new LFDMS_GUI ();
//        Boolean expResult = Boolean.TRUE,
//                result = instance.INIT_AUDIO_DATABASE ();
//        assertEquals (expResult, result);
//    }
    
    /**
     * Test of recordAudioForAfewSeconds method, of class LFDMS_GUI.
     */
//    @Test
//    public void testRecordAudioForAfewSeconds () {
//        
//        System.out.println ("recordAudioForAfewSeconds");
//        LFDMS_GUI instance = new LFDMS_GUI ();
//        Boolean expResult = Boolean.TRUE,
//        result = instance.recordAudioForAfewSeconds ();
//        assertEquals (expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
////        fail ("The test case is a prototype.");
//    }

    /**
     * Test of isNumeric method, of class LFDMS_GUI.
     */
    @Test
    public void testIsNumeric () {
        
        System.out.println ("isNumeric");
        
        String s = "a";
        boolean expResult = false;
        boolean result = LFDMS_GUI.isNumeric (s);
        assertEquals (expResult, result);
        
        s = "9";
        expResult = true;
        result = LFDMS_GUI.isNumeric (s);
        assertEquals (expResult, result);
    }
    
}
