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
public class ConstantDefs {

    public static final int NUMBER_FINGER_TYPES = 25;

    public static final byte FT_LEFT_4_FINGERS = 0;
    public static final byte FT_2_THUMBS = 1;
    public static final byte FT_RIGHT_4_FINGERS = 2;
    public static final byte FT_LEFT_LITTLE = 3;
    public static final byte FT_LEFT_RING = 4;
    public static final byte FT_LEFT_MIDDLE = 5;
    public static final byte FT_LEFT_INDEX = 6;
    public static final byte FT_LEFT_THUMB = 7;
    public static final byte FT_RIGHT_THUMB = 8;
    public static final byte FT_RIGHT_INDEX = 9;
    public static final byte FT_RIGHT_MIDDLE = 10;
    public static final byte FT_RIGHT_RING = 11;
    public static final byte FT_RIGHT_LITTLE = 12;
    public static final byte FT_ROLLED_LEFT_LITTLE =13;
    public static final byte FT_ROLLED_LEFT_RING = 14;
    public static final byte FT_ROLLED_LEFT_MIDDLE = 15;
    public static final byte FT_ROLLED_LEFT_INDEX = 16;
    public static final byte FT_ROLLED_LEFT_THUMB = 17;
    public static final byte FT_ROLLED_RIGHT_THUMB = 18;
    public static final byte FT_ROLLED_RIGHT_INDEX = 19;
    public static final byte FT_ROLLED_RIGHT_MIDDLE = 20;
    public static final byte FT_ROLLED_RIGHT_RING = 21;
    public static final byte FT_ROLLED_RIGHT_LITTLE = 22;
    public static final byte FT_PLAIN_LEFT_THUMB = 23;			// for Type4 record, 500*600 image
    public static final byte FT_PLAIN_RIGHT_THUMB = 24;
    public static final byte FT_PLAIN_FINGER = 25;
    
    public static final byte DEVICE_SCAN_TYPE_SLAPS = 0x01;
    public static final byte DEVICE_SCAN_TYPE_2THUMBS = 0x02;	//for FS50 
    public static final byte DEVICE_SCAN_TYPE_FLAT_FINGER = 0x04;
    public static final byte DEVICE_SCAN_TYPE_ROLLED_FINGER = 0x08;
    public static final byte AUTO_CAPTURE_DEFAULT_LEVEL = 3;		//0-7
    
    public static final byte ETFS_RECORD_TYPE_ANSI_NIST_ITL_1_2007_4 = 1;
    public static final byte ETFS_RECORD_TYPE_ANSI_NIST_ITL_1_2007_14 = 2;
    public static final byte ETFS_RECORD_TYPE_ANSI_381_2004 = 3;
    public static final byte ETFS_RECORD_TYPE_ISO_IEC_19794_4 = 4;
    
    public static final byte FIR_STD_ANSI = 1;
    public static final byte FIR_STD_ISO	 = 2;
    
    public static final byte TYPE_ANSI_NIST_ITL_1_2007_4	= 1;
    public static final byte TYPE_ANSI_NIST_ITL_1_2007_14 = 2;
    public static final byte TYPE_ANSI_381_2004 = 3;
    public static final byte TYPE_ISO_IEC_19794_4 = 4;

    public static final int FTR_DEVICE_USB_2_0_TYPE_2 = 4;
    public static final int FTR_DEVICE_USB_2_0_TYPE_3 = 5;
    public static final int FTR_DEVICE_USB_2_0_TYPE_4 = 6;
    public static final int FTR_DEVICE_USB_2_0_TYPE_50 = 7;
    public static final int FTR_DEVICE_USB_2_0_TYPE_60 = 8;
    public static final int FTR_DEVICE_USB_2_0_TYPE_25 = 9;
    public static final int FTR_DEVICE_USB_2_0_TYPE_10 = 10;
    public static final int FTR_DEVICE_USB_2_0_TYPE_80W = 11;
    public static final int FTR_DEVICE_USB_2_0_TYPE_80H = 13;
    public static final int FTR_DEVICE_USB_2_0_TYPE_88H = 14;
    public static final int FTR_DEVICE_USB_2_0_TYPE_64 = 15;
    public static final String SCANNER_TYPE = "4-4-2";
    
    public static String GetDeviceName(byte nDeviceCompatibility, short[] nDeviceID)
    {
        String strDevice = "";
        switch(nDeviceCompatibility)
        {
        case FTR_DEVICE_USB_2_0_TYPE_2:
        case FTR_DEVICE_USB_2_0_TYPE_80W:
        case FTR_DEVICE_USB_2_0_TYPE_80H:
            strDevice = "FS80";
            nDeviceID[0] = 0x80;
            break;
	case FTR_DEVICE_USB_2_0_TYPE_3:
        case FTR_DEVICE_USB_2_0_TYPE_88H:
            strDevice = "FS88";
            nDeviceID[0] = 0x88;
            break;
	case FTR_DEVICE_USB_2_0_TYPE_25:
            strDevice = "FS25";
            nDeviceID[0] = 0x25;
            break;
        case FTR_DEVICE_USB_2_0_TYPE_10:
            strDevice = "FS10";
            nDeviceID[0] = 0x10;
            break;            
	case FTR_DEVICE_USB_2_0_TYPE_50:
            strDevice = "FS50";
            nDeviceID[0] = 0x50;
            break;
	case FTR_DEVICE_USB_2_0_TYPE_60:
            strDevice = "FS60";
            nDeviceID[0] = 0x60;
            break;
	case FTR_DEVICE_USB_2_0_TYPE_64:
            strDevice = "FS64";
            nDeviceID[0] = 0x64;
            break;
	default:
            strDevice = "Unknown";
            break;
	}
        return strDevice;
    }
}
