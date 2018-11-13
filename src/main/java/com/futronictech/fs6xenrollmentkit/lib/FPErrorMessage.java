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
public class FPErrorMessage {
    
    public static final int ERROR_SUCCESS = 0;
    public static final int FTR_ERROR_EMPTY_FRAME = 4306; /* ERROR_EMPTY */
    public static final int FTR_ERROR_MOVABLE_FINGER = 0x20000001;
    public static final int FTR_ERROR_NO_FRAME = 0x20000002;
    public static final int FTR_ERROR_USER_CANCELED = 0x20000003;
    public static final int FTR_ERROR_HARDWARE_INCOMPATIBLE = 0x20000004;
    public static final int FTR_ERROR_FIRMWARE_INCOMPATIBLE = 0x20000005;
    public static final int FTR_ERROR_INVALID_AUTHORIZATION_CODE = 0x20000006;
    public static final int FTR_ERROR_ROLL_NOT_STARTED = 0x20000007;
    public static final int FTR_ERROR_ROLL_PROGRESS_DATA = 0x20000008;
    public static final int FTR_ERROR_ROLL_TIMEOUT = 0x20000009;
    public static final int FTR_ERROR_ROLL_ABORTED = 0x2000000A;
    public static final int FTR_ERROR_ROLL_ALREADY_STARTED = 0x2000000B;
    public static final int FTR_ERROR_ROLL_PROGRESS_REMOVE_FINGER = 0x2000000C;
    public static final int FTR_ERROR_ROLL_PROGRESS_PUT_FINGER = 0x2000000D;
    public static final int FTR_ERROR_ROLL_PROGRESS_POST_PROCESSING = 0x2000000E;
    public static final int FTR_ERROR_FINGER_IS_PRESENT = 0x2000000F;

    public static String GetErrorMessage(int dwError)
    {
        String strErrMsg;
	switch( dwError ) 
	{
	case ERROR_SUCCESS:
            strErrMsg = "OK";
            break;
	case FTR_ERROR_EMPTY_FRAME:	// ERROR_EMPTY
            strErrMsg = "- Empty frame -";
            break;
	case FTR_ERROR_MOVABLE_FINGER:
            strErrMsg = "- Movable finger -";
            break;
	case FTR_ERROR_NO_FRAME:
            strErrMsg = "- Fake finger detected -";
            break;
	case FTR_ERROR_USER_CANCELED:
            strErrMsg = "- User canceled -";
            break;
	case FTR_ERROR_HARDWARE_INCOMPATIBLE:
            strErrMsg = "- Incompatible hardware -";
            break;
	case FTR_ERROR_FIRMWARE_INCOMPATIBLE:
            strErrMsg = "- Incompatible firmware -";
            break;
	case FTR_ERROR_INVALID_AUTHORIZATION_CODE:
            strErrMsg = "- Invalid authorization code -";
            break;
        case FTR_ERROR_ROLL_NOT_STARTED:
            strErrMsg = "- Roll operation is not started -";
            break;
        case FTR_ERROR_ROLL_TIMEOUT:
            strErrMsg = "- Roll operation is timeout -";
            break;            
        case FTR_ERROR_ROLL_ABORTED:
            strErrMsg = "- Roll operation is aborted -";
            break;
        case FTR_ERROR_ROLL_ALREADY_STARTED:
            strErrMsg = "- Roll operation has started already -";
            break;            
	default:
            strErrMsg = String.format( "Unknown return code - %d", dwError );
            break;
	}
        return strErrMsg;
    }
}
