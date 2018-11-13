/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.futronictech.fs6xenrollmentkit.lib;

/**
 *
 * @author slyeung
 */
public class LedControl {
//    private FtrNativeLibrary m_libNative = null;
//    
//    public LedControl( FtrNativeLibrary libNative )
//    {
//        m_libNative = libNative;
//    }
    
    /*************************************************************************
    * Turn on/off the Left 4 Leds
    * bOn : TRUE - On, FALSE - Off
    * bTimed : TRUE - Timed On, using Param2
    *		   FALSE - Timed off, using Param1
    * nRedGreed : Turn on red or green leds
    *			1 - Red
    *			2 - Green
    *			3 - Red + Green
    *************************************************************************/
    public static boolean SetLeft4Leds(FtrNativeLibrary m_libNative, boolean bOn, boolean bTimed, byte nRedGreen, boolean bBuzzer)
    {
        if( m_libNative == null )
            return false;
        int uiP1 = 0;
        int uiP2 = 0;
        if( bOn )
        {
            int uiParam = 0;
            if( nRedGreen == 1 )
                uiParam = 0x55;
            else if( nRedGreen == 2 )
                uiParam = 0xAA;
            else if( nRedGreen == 3 )
                uiParam = 0xFF;
            if( bBuzzer )//&& m_bSound )
                uiParam |= 0x100000;
            if( bTimed )
                uiP2 = uiParam;
            else
                uiP1 = uiParam;
        }
        return m_libNative.ControlPin3( uiP1, uiP2, 0xA0 );
    }

    public static boolean SetRight4Leds(FtrNativeLibrary m_libNative, boolean bOn, boolean bTimed, byte nRedGreen, boolean bBuzzer)
    {
        int uiP1 = 0;
        int uiP2 = 0;
        if( bOn )
        {
            int uiParam = 0;
            if( nRedGreen == 1 )
                uiParam = 0x55000;
            else if( nRedGreen == 2 )
                uiParam = 0xAA000;
            else if( nRedGreen == 3 )
                uiParam = 0xFF000;
            if( bBuzzer )// && m_bSound )
                uiParam |= 0x100000;
            if( bTimed )
                uiP2 = uiParam;
            else
                uiP1 = uiParam;
        }
        return m_libNative.ControlPin3( uiP1, uiP2, 0xA0 );
    }

    public static boolean SetThumb2Leds(FtrNativeLibrary m_libNative, boolean bOn, boolean bTimed, byte nRedGreen, boolean bBuzzer)
    {
        int uiP1 = 0;
        int uiP2 = 0;
        if( bOn )
        {
            int uiParam = 0;
            if( nRedGreen == 1 )
                uiParam = 0x500;
            else if( nRedGreen == 2 )
                uiParam = 0xA00;
            else if( nRedGreen == 3 )
                uiParam = 0xF00;
            if( bBuzzer )// && m_bSound )
                uiParam |= 0x100000;
            if( bTimed )
                uiP2 = uiParam;
            else
                uiP1 = uiParam;
        }
        return m_libNative.ControlPin3( uiP1, uiP2, 0xA0 );
    }

    /***************************************************************************************
            nLed: single LED index
                    0- Left Little   1- Left Ring   2-Left Middle   3-Left Index   4-Left Thumb
                    5- Right Little  6- Right Ring  7-Right Middle  8-Right Index  9-Right Thumb
    ***************************************************************************************/
    public static boolean SetSingleLed(FtrNativeLibrary m_libNative, boolean bOn, boolean bTimed, byte nLed, byte nRedGreen, boolean bBuzzer)
    {
        int uiP1 = 0;
        int uiP2 = 0;
        if( bOn )
        {
            int uiParam = 0;
            uiParam = nRedGreen;	// 1, 2, 3
            uiParam = uiParam << (nLed * 2);
            if( bBuzzer )   //&& m_bSound )
                uiParam |= 0x100000;
            if( bTimed )
                uiP2 = uiParam;
            else
                uiP1 = uiParam;
        }
        return m_libNative.ControlPin3( uiP1, uiP2, 0xA0 );
    }   
}
