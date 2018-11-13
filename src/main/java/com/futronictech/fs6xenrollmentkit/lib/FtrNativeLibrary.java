/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.futronictech.fs6xenrollmentkit.lib;

import java.io.File;

/**
 * Futronic APIs native functions
 * @author slyeung
 */
public class FtrNativeLibrary {
     //1.ftrScanAPI
    public native boolean OpenDevice();
    public native boolean CloseDevice();
    public native String GetVersionInfo(byte[] byDeviceCompatibility);
    public native boolean GetImageSize();
    public native boolean IsFingerPresent();
    public native boolean GetFrame(byte[] pImage);
    public native boolean GetImage2(int nDose, byte[] pImage);
    public native boolean SetOptions(int Mask, int Flag);
    public native boolean Save7Bytes(byte[] buffer);
    public native boolean Restore7Bytes(byte[] buffer);
    public native boolean SetNewAuthorizationCode(byte[] SevenBytesAuthorizationCode);
    public native boolean SaveSecret7Bytes(byte[] SevenBytesAuthorizationCode, byte[] buffer);
    public native boolean RestoreSecret7Bytes(byte[] SevenBytesAuthorizationCode, byte[] buffer);
    public native boolean SetDiodesStatus(int GreenDiodeStatus, int RedDiodeStatus );
    public native boolean GetDiodesStatus(byte[] Status); //2 bytes - 1st:Green, 2nd:Red
    public native boolean ControlPin3(int dwParam1, int dwParam2, int dwPeriod);
    public native boolean RollStart();
    public native boolean RollGetFrameParameters(FTRSCAN_ROLL_FRAME_PARAMETERS pFrameParameters, byte[] pBuffer);
    public native boolean RollAbort();
    //2.ftrMathAPI
    public native boolean MathImageNFIQ(byte[] pBuffer, int nWidth, int nHeight, int[] nNfiq);
    public native boolean MathScanFrameSegment(byte[] pBuffer, byte[] pBufferResult, byte[] pBufferSubf, SegmParam Param, SubfCoord[] Subf, boolean[] bError);
    public native boolean MathScanFrameSegmentPreviewAuto(byte[] pBuffer,byte[] pBufferResult, byte[] pBufferSubf, SegmParam Param, SubfCoord[] Subf, boolean[] bError, int[] nAutoThresh);
    public native boolean MathImageSegmentAuto(byte[] pBuffer,int nWidth, int nHeight, byte[] pBufferResult, byte[] pBufferSubf, SegmParam Param, SubfCoord[] Subf, boolean[] bError, int[] nAutoThresh);
    //3.ftrWSQ
    public native boolean WsqFromRAWImage(byte[] pRawImage, int nWidth, int nHeight, float fBitrate, int[] nWsqSize, byte[] pWsqImg);
    //4. ftrNBIS
    public native boolean NbisGetMinutiaeXYTQ(xytq_struct ostruct, byte[] idata, int iw, int ih);
    public native boolean NbisBozorth3Verify(xytq_struct pstruct, xytq_struct gstruct, int[] score );
    public native boolean NbisBozorth3SetBaseProbe(long[] pHandle, xytq_struct pstruct);
    public native boolean NbisBozorth3Identify(long pHandle, xytq_struct gstruct, int[] score);
    public native boolean NbisBozorth3ReleaseBaseProbe(long pHandle);
    //5. ftrbiomdi
    public native boolean BiomdiNewFIR(long[] pFir, int nStdFormat, short nDeviceID);
    public native boolean BiomdiFreeFIR(long pFir);
    public native boolean BiomdiFIRAddImage(long pFir, byte[] pImage, int nImageSize, int nWidth, int nHeight, byte nFingerPosition, byte nNFIQ, byte nImpressionType);
    public native boolean BiomdiGetFIRDataSize(long pFir, int[] nSize);
    public native boolean BiomdiGetFIRData(long pFir, int nSize, byte[]pData);
    private static final String  LIB_PATH = "C:\\Users\\SEAMFIX\\Desktop\\futronic\\Futronic";
    static {
//        System.load(LIB_PATH.concat(File.separator.concat("ftrBiomdi.dll")));
//        System.load(LIB_PATH.concat(File.separator.concat("ftrMathAPI.dll")));
//        System.load(LIB_PATH.concat(File.separator.concat("ftrNBIS.dll")));
//        System.load(LIB_PATH.concat(File.separator.concat("ftrScanAPI.dll")));
//        System.load(LIB_PATH.concat(File.separator.concat("ftrWSQ.dll")));
//
//        System.load("C:\\Users\\SEAMFIX\\Desktop\\futronic\\Futronic\\ftrAnsiSDK.dll");
//        System.load("C:\\Users\\SEAMFIX\\Desktop\\futronic\\Futronic\\ftrAnsiSDKJni.dll");
//        System.load("C:\\Users\\SEAMFIX\\Desktop\\futronic\\Futronic\\ftrJavaNativeAPIs.dll");

        System.loadLibrary("ftrBiomdi");
        System.loadLibrary("ftrMathAPI");
        System.loadLibrary("ftrNBIS");
        System.loadLibrary("ftrScanAPI");
        System.loadLibrary("ftrWSQ");

        System.loadLibrary("ftrAnsiSDK");
        System.loadLibrary("ftrAnsiSDKJni");
        System.loadLibrary("ftrJavaNativeAPIs");

    }
    
    public int GetImageWidth()
    {
        return m_ImageWidth;
    }
    
    public int GetImageHeight()
    {
        return m_ImageHeight;
    }
    
    public int GetErrorCode()
    {
        return m_ErrorCode;
    }
    
    private int m_ImageWidth;
    private int m_ImageHeight;
    private long m_hDevice;
    private int m_ErrorCode;
}
