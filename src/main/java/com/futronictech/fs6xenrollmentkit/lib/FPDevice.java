/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.futronictech.fs6xenrollmentkit.lib;

import com.futronictech.fs6xenrollmentkit.interfaces.ICallBack;
import com.futronictech.fs6xenrollmentkit.ui.MyIcon;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * @author slyeung
 */
public class FPDevice {
    private int m_ImageWidth = 0;
    private int m_ImageHeight = 0;
    private byte[] m_pBuffer = null;
    private byte[] m_pBufferResult = null;
    private byte[] m_pBufferSubf = null;
    private SubfCoord[] m_Subf = null;
    private SegmParam m_ParamSeg = null;
    private FtrNativeLibrary m_libNative = null;
    private boolean m_IsDeviceOpened = false;
    public BufferedImage m_hImage;
    public int m_ImageFormat = 0;
    private String m_strDeviceVersionInfo;
    private byte m_nDeviceCompatibility = 0;
    public static boolean m_bStopOperation = false;
    private ACCEPTED_IMAGE[] m_aiImage;
    private boolean m_bSegmentation;
    private boolean m_bAngle;
    private boolean m_bSound;
    private boolean m_bAutoCapture;
    private int[] m_nNfiq;
    private boolean m_bUnavailable;
    private int m_nUnavailableFingers;
    private String m_strErrMsg;
    private String m_strDiagnostic;
    private int m_nDiagnosticCode;
    private byte m_nDeviceID;
    private byte m_nACTL;
    private byte m_ftCurrent;
    private boolean m_bPreview = false;
    private ICallBack m_CallBack;
    private FINGER_SEGMENT[] m_FingerSegments = new FINGER_SEGMENT[10];
    private FPVerify m_verifyFP = null;
    private WorkerThread m_WorkerThread = null;

    public byte getFingerToCapture() {
        return fingerToCapture;
    }

    public void setFingerToCapture(byte fingerToCapture) {
        this.fingerToCapture = fingerToCapture;
    }

    private byte fingerToCapture;
    private boolean m_bIsScanning = false;
    private boolean m_bIsRoll = false;
    public boolean m_bIsImageSaved = false;

    public static MyIcon m_FingerImage = null;
    public static JLabel m_ShowArea = null;

    public static final int IMAGE_FORMAT_1600_1500 = 0;
    public static final int IMAGE_FORMAT_800_750 = 1;
    public static final int IMAGE_FORMAT_UNKNOWN = 2;

    public static final int FTR_OPTIONS_INVERT_IMAGE = 0x00000040;
    public static final int FTR_OPTIONS_PREVIEW_MODE = 0x00000080;
    public static final int FTR_OPTIONS_IMAGE_FORMAT_MASK = 0x00000700;
    public static final int FTR_OPTIONS_IMAGE_FORMAT_1 = 0x00000100;
    public static final int FTR_OPTIONS_ELIMINATE_BACKGROUND = 0x00000800;

    public FPDevice() {
        int i;
        m_IsDeviceOpened = false;
        m_Subf = new SubfCoord[4];
        m_ParamSeg = new SegmParam();
        m_libNative = new FtrNativeLibrary();
        m_verifyFP = new FPVerify();
        m_pBuffer = null;
        m_pBufferSubf = null;
        m_bAutoCapture = false;
        m_nNfiq = new int[1];
        m_bIsImageSaved = false;
        m_ImageFormat = IMAGE_FORMAT_UNKNOWN;
        m_aiImage = new ACCEPTED_IMAGE[ConstantDefs.NUMBER_FINGER_TYPES];
        for (i = 0; i < ConstantDefs.NUMBER_FINGER_TYPES; i++)
            m_aiImage[i] = new ACCEPTED_IMAGE();
        for (i = 0; i < 4; i++)
            m_Subf[i] = new SubfCoord();
        for (i = 0; i < 10; i++)
            m_FingerSegments[i] = new FINGER_SEGMENT();
        m_bSegmentation = m_bAngle = m_bSound = m_bUnavailable = false;
        m_nUnavailableFingers = 0;
        m_nDeviceCompatibility = 127;    //UnKnown device
        m_strErrMsg = null;
        m_nDeviceID = 0;
        m_strDiagnostic = null;
        m_nDiagnosticCode = 0;
        m_nACTL = ConstantDefs.AUTO_CAPTURE_DEFAULT_LEVEL;

    }

    public FtrNativeLibrary getM_libNative() {
        return m_libNative;
    }

    public void setM_libNative(FtrNativeLibrary m_libNative) {
        this.m_libNative = m_libNative;
    }


    public void FreeBuffer() {
        m_pBuffer = null;
        m_pBufferSubf = null;
        for (int i = 0; i < ConstantDefs.NUMBER_FINGER_TYPES; i++) {
            m_aiImage[i].pAcceptedImage = null;
            m_aiImage[i].nImageHeight = m_aiImage[i].nImageWidth = 0;
            m_aiImage[i].nNFIQ = m_aiImage[i].nAnsiFingerPosition = 0;
            m_aiImage[i].nNumberAmp = m_aiImage[i].nNumberSegments = 0;
        }
        m_bIsImageSaved = false;
        m_verifyFP.Init();
    }

    public boolean Open() {
        if (m_IsDeviceOpened)
            Close();
        if (m_libNative.OpenDevice()) {
            byte[] byDC = new byte[1];
            byDC[0] = 0;
            m_strDeviceVersionInfo = m_libNative.GetVersionInfo(byDC);
            if (m_strDeviceVersionInfo != null)
                m_nDeviceCompatibility = byDC[0];
            m_IsDeviceOpened = true;
            return true;
        }
        return false;
    }

    public void Close() {
        if (m_IsDeviceOpened) {
            m_libNative.CloseDevice();
        }
        m_strDeviceVersionInfo = null;
        m_nDeviceCompatibility = 0;
        m_IsDeviceOpened = false;
    }

    public String GetDeviceInfo() {
        return m_strDeviceVersionInfo;
    }

    public boolean GetImageSize(int[] nWidth, int[] nHeight) {
        if (m_libNative.GetImageSize()) {
            nWidth[0] = m_libNative.GetImageWidth();
            nHeight[0] = m_libNative.GetImageHeight();
            return true;
        }
        return false;
    }

    public boolean GetImage() {
        if (m_libNative.GetImageSize()) {
            if (m_pBuffer != null)
                m_pBuffer = null;
            int width, height;
            width = m_libNative.GetImageWidth();
            height = m_libNative.GetImageHeight();
            m_pBuffer = new byte[width * height];
            if (m_libNative.GetFrame(m_pBuffer)) {
                m_hImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
                DataBuffer db1 = m_hImage.getRaster().getDataBuffer();
                for (int i = 0; i < db1.getSize(); i++) {
                    db1.setElem(i, m_pBuffer[i]);
                }
                return true;
            }
        }
        return false;
    }

    public boolean CanSegmentation() {
        if (!m_IsDeviceOpened)
            if (!Open())
                return false;
        return (m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 ||
                m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64 ||
                m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_50);
    }

    public boolean CanRoll() {
        return CanSegmentation();
    }

    public boolean CanSlaps() {
        if (!m_IsDeviceOpened)
            if (!Open())
                return false;
        return (m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64);
    }

    public void SetImageFormat(int nImageFormat) {
        m_ImageFormat = nImageFormat;
    }

    public void SetSegmentation(boolean bSegmentation) {
        m_bSegmentation = bSegmentation;
    }

    public void SetAngle(boolean bAngle) {
        m_bAngle = bAngle;
    }

    public void SetSound(boolean bSound) {
        m_bSound = bSound;
    }

    public void SetAutoCapture(boolean bAuto) {
        m_bAutoCapture = bAuto;
    }

    public void SetCurrentFinger(byte ftFinger) {
        m_ftCurrent = ftFinger;
    }

    public void SetPreviewMode(boolean bPreview) {
        m_bPreview = bPreview;
    }

    public void SetCallback(ICallBack callback) {
        m_CallBack = callback;
    }

    public String GetErrorMessage() {
        return m_strErrMsg;
    }

    public String GetDiagnostic(int[] nDiagnosticCode) {
        nDiagnosticCode[0] = m_nDiagnosticCode;
        return m_strDiagnostic;
    }

    public void SetErrorMessage(String strErrMsg) {
        m_strErrMsg = strErrMsg;
    }

    public void SetACTL(byte nActl) {
        m_nACTL = nActl;
    }

    public boolean IsUnavailable(int[] nUnavailableFingers) {
        nUnavailableFingers[0] = m_nUnavailableFingers;
        return m_bUnavailable;
    }

    public boolean PrepareSegmentation(int[] nWidth, int[] nHeight) {
        if (m_ImageFormat == IMAGE_FORMAT_UNKNOWN)
            return false;
        if (m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64) {
            if (m_ImageFormat == IMAGE_FORMAT_1600_1500) {
                if (!m_libNative.SetOptions(FTR_OPTIONS_PREVIEW_MODE, FTR_OPTIONS_PREVIEW_MODE))    //preview mode
                {
                    SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                    Close();
                    return false;
                }
            } else {
                if (!m_libNative.SetOptions(FTR_OPTIONS_IMAGE_FORMAT_MASK, FTR_OPTIONS_IMAGE_FORMAT_1)) {
                    SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                    Close();
                    return false;
                }
            }
        }
        if (!m_libNative.SetOptions(FTR_OPTIONS_INVERT_IMAGE, FTR_OPTIONS_INVERT_IMAGE)) {
            SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
            Close();
            return false;
        }
        if (!m_libNative.GetImageSize()) {
            Close();
            return false;
        }
        m_ImageWidth = m_libNative.GetImageWidth();
        m_ImageHeight = m_libNative.GetImageHeight();
        nWidth[0] = m_ImageWidth;
        nHeight[0] = m_ImageHeight;
        return true;
    }

    public boolean PrepareSegmentationPreview() {
        if (m_ImageFormat == IMAGE_FORMAT_UNKNOWN)
            return false;

        if (m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_50) {
            if (m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64) {
                if (!m_libNative.SetOptions(FTR_OPTIONS_IMAGE_FORMAT_MASK, m_ImageFormat * FTR_OPTIONS_IMAGE_FORMAT_1)) {
                    SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                    Close();
                    return false;
                }
            }
            ShowImageText(Color.ORANGE, "PLEASE REMOVE FINGER");
            if (m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64) {
                if (m_ftCurrent == ConstantDefs.FT_LEFT_4_FINGERS)
                    LedControl.SetLeft4Leds(m_libNative, true, true, (byte) 1, m_bSound);
                else if (m_ftCurrent == ConstantDefs.FT_2_THUMBS)
                    LedControl.SetThumb2Leds(m_libNative, true, true, (byte) 1, m_bSound);
                else if (m_ftCurrent == ConstantDefs.FT_RIGHT_4_FINGERS)
                    LedControl.SetRight4Leds(m_libNative, true, true, (byte) 1, m_bSound);
                else if (m_ftCurrent == ConstantDefs.FT_PLAIN_LEFT_THUMB)
                    LedControl.SetSingleLed(m_libNative, true, true, (byte) 4, (byte) 1, m_bSound);
                else if (m_ftCurrent == ConstantDefs.FT_PLAIN_RIGHT_THUMB)
                    LedControl.SetSingleLed(m_libNative, true, true, (byte) 5, (byte) 1, m_bSound);
                else
                    LedControl.SetSingleLed(m_libNative, true, true, (byte) (m_ftCurrent - 3), (byte) 1, m_bSound);

                if (!m_libNative.SetOptions(FTR_OPTIONS_PREVIEW_MODE, 0)) {
                    SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                    Close();
                    return false;
                }
            }
            if (m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_50) {
                while (!m_libNative.SetOptions(FTR_OPTIONS_ELIMINATE_BACKGROUND, FTR_OPTIONS_ELIMINATE_BACKGROUND))    //Calibrate background for normal mode
                {
                    if (m_libNative.GetErrorCode() != FPErrorMessage.FTR_ERROR_FINGER_IS_PRESENT) {
                        SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                        Close();
                        return false;
                    }
                }
            } else if (m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64) {
                try {
                    Thread.sleep(300); // a little delay for changing another fingers
                } catch (InterruptedException ex) {
                    Logger.getLogger(FPDevice.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64) {
                if (m_ImageFormat == IMAGE_FORMAT_1600_1500) {
                    if (!m_libNative.SetOptions(FTR_OPTIONS_PREVIEW_MODE, FTR_OPTIONS_PREVIEW_MODE)) {
                        SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                        Close();
                        return false;
                    }
                    if (m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60) {
                        while (!m_libNative.SetOptions(FTR_OPTIONS_ELIMINATE_BACKGROUND, FTR_OPTIONS_ELIMINATE_BACKGROUND))    //Calibrate background for preview mode
                        {
                            if (m_libNative.GetErrorCode() != FPErrorMessage.FTR_ERROR_FINGER_IS_PRESENT) {
                                SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                                Close();
                                return false;
                            }
                        }
                    } else    //FS64
                    {
                        try {
                            Thread.sleep(300); // a little delay for changing another fingers
                        } catch (InterruptedException ex) {
                            Logger.getLogger(FPDevice.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else    // 800 * 750
                {
                    try {
                        Thread.sleep(200); // a little delay for changing another fingers
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FPDevice.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (m_ftCurrent == ConstantDefs.FT_LEFT_4_FINGERS)
                    LedControl.SetLeft4Leds(m_libNative, true, false, (byte) 2, false);
                else if (m_ftCurrent == ConstantDefs.FT_2_THUMBS)
                    LedControl.SetThumb2Leds(m_libNative, true, false, (byte) 2, false);
                else if (m_ftCurrent == ConstantDefs.FT_RIGHT_4_FINGERS)
                    LedControl.SetRight4Leds(m_libNative, true, false, (byte) 2, false);
                else if (m_ftCurrent == ConstantDefs.FT_PLAIN_LEFT_THUMB)
                    LedControl.SetSingleLed(m_libNative, true, false, (byte) 4, (byte) 2, false);
                else if (m_ftCurrent == ConstantDefs.FT_PLAIN_RIGHT_THUMB)
                    LedControl.SetSingleLed(m_libNative, true, false, (byte) 5, (byte) 2, false);
                else
                    LedControl.SetSingleLed(m_libNative, true, false, (byte) (m_ftCurrent - 3), (byte) 2, false);
            }
            ShowImageText(Color.GREEN, null);
        }
        if (m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64) {
            if (m_ImageFormat == IMAGE_FORMAT_1600_1500) {
                if (!m_libNative.SetOptions(FTR_OPTIONS_PREVIEW_MODE, FTR_OPTIONS_PREVIEW_MODE))    //preview mode
                {
                    SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                    Close();
                    return false;
                }
            }
        }
        if (!m_libNative.SetOptions(FTR_OPTIONS_INVERT_IMAGE, FTR_OPTIONS_INVERT_IMAGE)) {
            SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
            Close();
            return false;
        }
        if (!m_libNative.GetImageSize()) {
            SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
            Close();
            return false;
        }
        if (m_pBuffer != null)
            m_pBuffer = null;
        if (m_pBufferResult != null)
            m_pBufferResult = null;
        m_ImageWidth = m_libNative.GetImageWidth();
        m_ImageHeight = m_libNative.GetImageHeight();
        m_pBuffer = new byte[m_ImageWidth * m_ImageHeight];
        m_pBufferResult = new byte[m_ImageWidth * m_ImageHeight];
        m_pBufferSubf = new byte[m_ImageWidth * m_ImageHeight];
        m_bPreview = true;
        return true;
    }

    public boolean PrepareSegmentationCapture() {
        m_FingerImage.setPreviewMode(false);
        if ((m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64) && (m_ImageFormat == IMAGE_FORMAT_1600_1500)) {
            if (!m_libNative.SetOptions(FTR_OPTIONS_PREVIEW_MODE, 0))    //non-preview mode
            {
                SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                Close();
                return false;
            }
            if (!m_libNative.SetOptions(FTR_OPTIONS_IMAGE_FORMAT_MASK, 0)) {
                SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                Close();
                return false;
            }
            if (!m_libNative.GetImageSize()) {
                SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                Close();
                return false;
            }
            if (m_pBuffer != null)
                m_pBuffer = null;
            if (m_pBufferResult != null)
                m_pBufferResult = null;
            m_ImageWidth = m_libNative.GetImageWidth();
            m_ImageHeight = m_libNative.GetImageHeight();
            m_pBuffer = new byte[m_ImageWidth * m_ImageHeight];
            m_pBufferResult = new byte[m_ImageWidth * m_ImageHeight];
            m_pBufferSubf = new byte[m_ImageWidth * m_ImageHeight];
        }
        return true;
    }

    public boolean Scan() {
        if (m_ShowArea == null) {
            SetErrorMessage("Please SetShowImageHandler first!");
            return false;
        }
        if (m_bIsScanning) {
            SetErrorMessage("Another scanning existed already!");
            return false;
        }
        if (!Open())
            return false;
        m_WorkerThread = null;
        m_bIsRoll = false;
        m_bStopOperation = false;
        int[] width = new int[1];
        int[] height = new int[1];
        width[0] = height[0] = 0;
        if (!PrepareSegmentation(width, height))
            return false;
        int width2 = m_ShowArea.getWidth();
        int height2 = m_ShowArea.getHeight();
        m_FingerImage = new MyIcon((width[0] > width2 ? width2 : width[0]), (height[0] > height2 ? height2 : height[0]));
        m_ShowArea.setIcon(m_FingerImage);

        if (!PrepareSegmentationPreview())
            return false;
        if (m_bStopOperation)
            return false;
        // start thread
        m_WorkerThread = new WorkerThread();
        m_WorkerThread.start();

        switch (fingerToCapture){
            case ConstantDefs.FT_LEFT_LITTLE:
                TurnOffLed();
                LedControl.SetSingleLed(m_libNative, true, true, (byte) 0, (byte) 2, false);
                break;
            case ConstantDefs.FT_LEFT_RING:
                TurnOffLed();
                LedControl.SetSingleLed(m_libNative, true, true, (byte) 1, (byte) 2, false);
                break;
            case ConstantDefs.FT_LEFT_MIDDLE:
                TurnOffLed();
                LedControl.SetSingleLed(m_libNative, true, true, (byte) 2, (byte) 2, false);
                break;
            case ConstantDefs.FT_LEFT_INDEX:
                TurnOffLed();
                LedControl.SetSingleLed(m_libNative, true, true, (byte) 3, (byte) 2, false);
                break;
            case ConstantDefs.FT_LEFT_THUMB:
                TurnOffLed();
                LedControl.SetSingleLed(m_libNative, true, true, (byte) 4, (byte) 2, false);
                break;
            case ConstantDefs.FT_RIGHT_THUMB:
                TurnOffLed();
                LedControl.SetSingleLed(m_libNative, true, true, (byte) 5, (byte) 2, false);
                break;
            case ConstantDefs.FT_RIGHT_INDEX:
                TurnOffLed();
                LedControl.SetSingleLed(m_libNative, true, true, (byte) 6, (byte) 2, false);
                break;
            case ConstantDefs.FT_RIGHT_MIDDLE:
                TurnOffLed();
                LedControl.SetSingleLed(m_libNative, true, true, (byte) 7, (byte) 2, false);
                break;
            case ConstantDefs.FT_RIGHT_RING:
                TurnOffLed();
                LedControl.SetSingleLed(m_libNative, true, true, (byte) 8, (byte) 2, false);
                break;
            case ConstantDefs.FT_RIGHT_LITTLE:
                TurnOffLed();
                LedControl.SetSingleLed(m_libNative, true, true, (byte) 9, (byte) 2, false);
                break;
            case ConstantDefs.FT_PLAIN_FINGER:
                TurnOffLed();
                break;
        }
        return true;
    }

    public boolean RollScan() {
        if (m_ShowArea == null) {
            SetErrorMessage("Please SetShowImageHandler first!");
            return false;
        }
        if (m_bIsScanning) {
            SetErrorMessage("Another scanning existed already!");
            return false;
        }
        if (!Open())
            return false;
        m_WorkerThread = null;
        m_bIsRoll = true;
        int[] width = new int[1];
        int[] height = new int[1];
        width[0] = height[0] = 0;
        if (!PrepareRolling(width, height))
            return false;
        int width2 = m_ShowArea.getWidth();
        int height2 = m_ShowArea.getHeight();
        m_FingerImage = new MyIcon((width[0] > width2 ? width2 : width[0]), (height[0] > height2 ? height2 : height[0]));
        m_ShowArea.setIcon(m_FingerImage);
        // start thread
        m_WorkerThread = new WorkerThread();
        m_WorkerThread.start();
        return true;
    }

    public boolean DoScan() {
        if ((m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_50) && m_bSegmentation) {
            if (!DoSegmentScan())
                return false;
        } else {
            if (!DoFlatScan())
                return false;
        }
        return true;
    }

    public boolean DoFlatScan() {
        m_bStopOperation = false;
        int nErrCode = 0;
        do {
            if (!m_libNative.GetFrame(m_pBuffer)) {
                nErrCode = m_libNative.GetErrorCode();
                if ((nErrCode != FPErrorMessage.FTR_ERROR_EMPTY_FRAME) && (nErrCode != FPErrorMessage.FTR_ERROR_MOVABLE_FINGER)) {
                    SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                    return false;
                }
                m_FingerImage.setSubfCoord(null, 0.0);
                m_FingerImage.setNfiq(0);
                ShowImage(m_ImageWidth, m_ImageHeight, null);
            } else {
                nErrCode = 0;
                m_nNfiq[0] = 0;
                m_libNative.MathImageNFIQ(m_pBuffer, m_ImageWidth, m_ImageHeight, m_nNfiq);
                m_FingerImage.setNfiq(m_nNfiq[0]);
                ShowImage(m_ImageWidth, m_ImageHeight, m_pBuffer);
            }
        } while (m_bPreview && !m_bStopOperation);

        if (!m_bPreview) //capture
        {
            if (nErrCode == 0) {
                m_nDiagnosticCode = 0;
                m_CallBack.OnAction(0);
            } else {
                SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                m_CallBack.OnAction(4);
            }
        }
        return true;
    }

    public boolean DoSegmentScan() {
        m_FingerImage.setPreviewMode(true);
        boolean bRC = true;
        for (int i = 0; i < 4; i++) {
            m_Subf[i] = new SubfCoord();
            m_Subf[i].err = 1;
        }
        // Set the param for segment
        if ((m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64) && m_ImageFormat == IMAGE_FORMAT_1600_1500)
            m_ParamSeg.nParamFing = 4;
        else
            m_ParamSeg.nParamFing = 2;
        if (m_bAngle)
            m_ParamSeg.nParamAngle = 1;
        else
            m_ParamSeg.nParamAngle = 0;
        m_ParamSeg.nParamNfiq = 2;  //QFUTR;	//show quality value for preview mode
        if (m_ftCurrent == ConstantDefs.FT_PLAIN_LEFT_THUMB || m_ftCurrent == ConstantDefs.FT_PLAIN_RIGHT_THUMB) {
            m_ParamSeg.nWidthSubf = 500;
            m_ParamSeg.nHeightSubf = 600;
            m_ParamSeg.nParamFing = 1;
        } else {
            m_ParamSeg.nWidthSubf = 320;    //XSIZE;
            m_ParamSeg.nHeightSubf = 480;   //YSIZE;
        }
        m_ParamSeg.nParamFixedSize = 1;         //FIXEDSIZE;
        int[] nAutoThreshold = new int[1];
        boolean[] bError = new boolean[1];
        nAutoThreshold[0] = 0;
        bError[0] = false;
        boolean bPrepareCapture = false;
        m_bUnavailable = false;
        m_bStopOperation = false;
        do {
            if (m_bPreview) {
                if ((m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64) && m_ImageFormat == IMAGE_FORMAT_1600_1500) {
                    if (!m_libNative.MathScanFrameSegmentPreviewAuto(m_pBuffer, m_pBufferResult, null, m_ParamSeg, m_Subf, bError, nAutoThreshold)) {
                        SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                        return false;
                    }
                    m_FingerImage.setSubfCoord(m_Subf, m_ParamSeg.dAngle);
                    ShowImage(m_ImageWidth, m_ImageHeight, m_pBufferResult);
                } else {
                    if (m_libNative.GetFrame(m_pBuffer)) {
                        if (!m_libNative.MathImageSegmentAuto(m_pBuffer, m_ImageWidth, m_ImageHeight, m_pBufferResult, null, m_ParamSeg, m_Subf, bError, nAutoThreshold)) {
                            SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                            return false;
                        }
                        m_FingerImage.setSubfCoord(m_Subf, m_ParamSeg.dAngle);
                        ShowImage(m_ImageWidth, m_ImageHeight, m_pBufferResult);
                    } else {
                        m_FingerImage.setSubfCoord(null, 0.0);
                        ShowImage(m_ImageWidth, m_ImageHeight, null);
                    }
                }
                if (m_bAutoCapture) {
                    boolean bDetectedFinger = false;
                    int nSegOK = 0;
                    if (m_ftCurrent == ConstantDefs.FT_PLAIN_LEFT_THUMB || m_ftCurrent == ConstantDefs.FT_PLAIN_RIGHT_THUMB) {
                        if (m_Subf[0].err == 0 && m_Subf[0].nfiq < 4)
                            bDetectedFinger = true;
                    } else {
                        for (int j = 0; j < m_ParamSeg.nParamFing; j++) {
                            if (m_Subf[j].err == 0 && m_Subf[j].qfutr < 4)        //v2.1
                                nSegOK++;
                        }
                        if (nSegOK == m_ParamSeg.nParamFing)
                            bDetectedFinger = true;
                    }
                    if ((bError[0] && (nAutoThreshold[0] >= m_nACTL)) || bDetectedFinger) {
                        m_bPreview = false;
                    }
                }
                try {
                    Thread.sleep(150);      // for Linux, it is necessary to add some delay
                } catch (InterruptedException ex) {
                    Logger.getLogger(FPDevice.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                m_CallBack.OnAction(2);
                if (!bPrepareCapture) {
                    m_FingerImage.setSubfCoord(null, 0.0);
                    m_FingerImage.setText(Color.GREEN, "DO NOT REMOVE FINGER");
                    ShowImage(m_ImageWidth, m_ImageHeight, null);
                    if (!PrepareSegmentationCapture())
                        return false;
                    bPrepareCapture = true;
                }
                if (!m_libNative.GetFrame(m_pBuffer)) {
                    SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                    return false;
                }
                m_ParamSeg.nParamNfiq = 1;  //set to NFIQ in capture mode
                if (!m_libNative.MathImageSegmentAuto(m_pBuffer, m_ImageWidth, m_ImageHeight, m_pBufferResult, m_pBufferSubf, m_ParamSeg, m_Subf, bError, nAutoThreshold)) {
                    SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
                    return false;
                }
                m_strDiagnostic = "";
                m_nDiagnosticCode = 0;
                m_nUnavailableFingers = 0;
                if (m_ftCurrent == ConstantDefs.FT_PLAIN_LEFT_THUMB || m_ftCurrent == ConstantDefs.FT_PLAIN_RIGHT_THUMB) {
                    if (m_Subf[0].err != 0) {
                        m_strDiagnostic = "Please evaluate the image - Missing Finger.\r\n";
                        m_nDiagnosticCode = 2;
                    } else {
                        if (m_Subf[0].nfiq > 3) {
                            m_strDiagnostic = "Please evaluate the image - Image Quality (NFIQ) > 3.\r\n";
                            m_nDiagnosticCode = 1;
                        }
                    }
                } else {
                    int i;
                    for (i = 0; i < m_ParamSeg.nParamFing; i++) {
                        if (m_Subf[i].err != 0 && (m_ftCurrent < ConstantDefs.FT_PLAIN_LEFT_THUMB)) {
                            m_bUnavailable = true;
                            m_nUnavailableFingers++;
                        }
                    }
                    if (m_bUnavailable) {
                        m_strDiagnostic = "Please evaluate the image - Missing Finger.\r\n";
                        m_nDiagnosticCode = 2;
                    } else {
                        // - ftrMathAPI.dll v1.0.0.71, add parameter to detect left/right hand
                        if ((m_ftCurrent == ConstantDefs.FT_LEFT_4_FINGERS && m_ParamSeg.nHandType != 1)
                                || (m_ftCurrent == ConstantDefs.FT_RIGHT_4_FINGERS && m_ParamSeg.nHandType != 2)) {
                            m_strDiagnostic = "Please evaluate the image - Wrong hand!\r\n";
                            m_nDiagnosticCode = 10;
                        }
                    }
                    for (i = 0; i < m_ParamSeg.nParamFing; i++) {
                        if (m_Subf[i].err == 0 && m_Subf[i].nfiq > 3) {
                            m_strDiagnostic += "Please evaluate the image - Image Quality (NFIQ) > 3.\r\n";
                            m_nDiagnosticCode += 1;
                            break;
                        }
                    }
                }
                if (m_nDiagnosticCode == 0)
                    m_strDiagnostic = "OK";

                m_FingerImage.setText(Color.GREEN, null);
                m_FingerImage.setSubfCoord(m_Subf, m_ParamSeg.dAngle);
                ShowImage(m_ImageWidth, m_ImageHeight, m_pBufferResult);
                //get the NFIQ
                m_libNative.MathImageNFIQ(m_pBuffer, m_ImageWidth, m_ImageHeight, m_nNfiq);
                m_CallBack.OnAction(0);
                break;
            }
        } while (bRC && !m_bStopOperation);
        return true;
    }

    public void Stop() {
        m_bStopOperation = true;
        if (m_WorkerThread != null) {
            m_WorkerThread.cancel();
            m_WorkerThread = null;
        }
    }

    public void TurnOffLed() {
        if (!m_IsDeviceOpened)
            if (!Open())
                return;
        LedControl.SetSingleLed(m_libNative, false, false, (byte) 0, (byte) 1, false);
    }

    public boolean PrepareRolling(int[] nWidth, int[] nHeight) {
        if (m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64) {
            if (!m_libNative.SetOptions(FTR_OPTIONS_IMAGE_FORMAT_MASK, FTR_OPTIONS_IMAGE_FORMAT_1))    //800*750 format
            {
                Close();
                return false;
            }
        }
        if ((m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64) && m_ftCurrent >= ConstantDefs.FT_ROLLED_LEFT_LITTLE) {
            LedControl.SetSingleLed(m_libNative, true, true, (byte) (m_ftCurrent - ConstantDefs.FT_ROLLED_LEFT_LITTLE), (byte) 1, m_bSound);
        }

        if (!m_libNative.SetOptions(FTR_OPTIONS_INVERT_IMAGE, FTR_OPTIONS_INVERT_IMAGE)) {
            SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
            Close();
            return false;
        }
        if ((m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_60 || m_nDeviceCompatibility == ConstantDefs.FTR_DEVICE_USB_2_0_TYPE_64) && m_ftCurrent >= ConstantDefs.FT_ROLLED_LEFT_LITTLE) {
            LedControl.SetSingleLed(m_libNative, true, false, (byte) (m_ftCurrent - ConstantDefs.FT_ROLLED_LEFT_LITTLE), (byte) 2, false);
        }

        if (!m_libNative.GetImageSize()) {
            SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
            Close();
            return false;
        }
        if (m_pBuffer != null)
            m_pBuffer = null;
        m_ImageWidth = m_libNative.GetImageWidth();
        m_ImageHeight = m_libNative.GetImageHeight();
        m_pBuffer = new byte[m_ImageWidth * m_ImageHeight];
        nWidth[0] = m_ImageWidth;
        nHeight[0] = m_ImageHeight;
        return true;
    }

    public boolean DoRoll() {
        boolean bRC = false;
        if (!m_libNative.RollStart()) {
            SetErrorMessage(FPErrorMessage.GetErrorMessage(m_libNative.GetErrorCode()));
            Close();
            return false;
        }
        m_bUnavailable = true;
        m_nUnavailableFingers = 1;
        boolean bIsAborted = false;
        //String strRollingInfo;
        m_FingerImage.setRollingParameters(null);
        m_bStopOperation = false;
        boolean bCanStop = false;
        do {
            FTRSCAN_ROLL_FRAME_PARAMETERS FrameParameters = new FTRSCAN_ROLL_FRAME_PARAMETERS();
            bRC = m_libNative.RollGetFrameParameters(FrameParameters, m_pBuffer);
            if (!bCanStop) {
                bCanStop = true;
                m_CallBack.OnAction(3);    //Enable buttons
            }
            if (!bRC) {
                int nErrCode = m_libNative.GetErrorCode();
                switch (nErrCode) {
                    case FPErrorMessage.FTR_ERROR_ROLL_ABORTED:
                        SetErrorMessage("Operation is canceled by user.");
                        bIsAborted = true;
                        break;
                    case FPErrorMessage.FTR_ERROR_ROLL_NOT_STARTED:
                        SetErrorMessage("The roll operation is not started.");
                        bIsAborted = true;
                        break;
                    case FPErrorMessage.FTR_ERROR_ROLL_ALREADY_STARTED:
                        SetErrorMessage("Operation is already started.");
                        bIsAborted = true;
                        break;
                    case FPErrorMessage.FTR_ERROR_ROLL_PROGRESS_DATA:
                        m_FingerImage.setRollingParameters(FrameParameters);
                        ShowImageText(Color.GREEN, null);
                        ShowImage(m_ImageWidth, m_ImageHeight, m_pBuffer);
                        break;
                    case FPErrorMessage.FTR_ERROR_ROLL_PROGRESS_REMOVE_FINGER:
                        m_FingerImage.setRollingParameters(null);
                        ShowImageText(Color.ORANGE, "Remove your finger from the scanner");
                        break;
                    case FPErrorMessage.FTR_ERROR_ROLL_PROGRESS_PUT_FINGER:
                        m_FingerImage.setRollingParameters(null);
                        ShowImageText(Color.GREEN, "Place your finger on the scanner to roll");
                        break;
                    case FPErrorMessage.FTR_ERROR_ROLL_PROGRESS_POST_PROCESSING:
                        m_FingerImage.setRollingParameters(null);
                        ShowImageText(Color.GREEN, "Post processing......");
                        break;
                    default:
                        break;
                }
                if (bIsAborted)
                    break;    //break do loop
            } else {
                // We've got a final image
                m_bUnavailable = false;
                m_nUnavailableFingers = 0;
                //get the NFIQ
                m_nNfiq[0] = 0;
                m_libNative.MathImageNFIQ(m_pBuffer, m_ImageWidth, m_ImageHeight, m_nNfiq);
                ShowImageText(Color.GREEN, null);
                m_FingerImage.setNfiq(m_nNfiq[0]);
                ShowImage(m_ImageWidth, m_ImageHeight, m_pBuffer);
                if (m_nNfiq[0] > 0 && m_nNfiq[0] < 4) {
                    m_strDiagnostic = "OK";
                    m_nDiagnosticCode = 0;
                } else {
                    m_strDiagnostic = "Please evaluate the image - Image Quality (NFIQ) > 3.";
                    m_nDiagnosticCode = 1;
                }
                m_CallBack.OnAction(1);    //Roll Completed
            }
        } while (!bRC);
        Close();
        return bRC;
    }

    /*******************************************************
     * Verify the captured image with enroll fingers
     *******************************************************/
    public boolean VerifyImage(byte ftCurrent) {
        if (ftCurrent > ConstantDefs.FT_PLAIN_RIGHT_THUMB) {
            SetErrorMessage("Invalid Finger Type!");
            return false;
        }
        byte nIndex = ftCurrent;
        int[] nScore = new int[1];
        int[] nMatchIndex = new int[1];
        String strMsg;
        boolean bWrong = false;
        nScore[0] = 0;
        nMatchIndex[0] = 0;
        //check PLAIN LEFT/RIGHT THUMB first
        if (nIndex == ConstantDefs.FT_PLAIN_LEFT_THUMB || nIndex == ConstantDefs.FT_PLAIN_RIGHT_THUMB) {
            if (m_Subf[0].err != 0)    // only one segmented image
            {
                SetErrorMessage("Invalid image");
                return false;
            }
            if (m_verifyFP.Identify(m_libNative, m_pBufferSubf, m_Subf[0].ws, m_Subf[0].hs, nMatchIndex, nScore))    //matched
            {
                if (nIndex == ConstantDefs.FT_PLAIN_LEFT_THUMB) {
                    if ((nMatchIndex[0] != ConstantDefs.FT_LEFT_THUMB) && (nMatchIndex[0] != ConstantDefs.FT_ROLLED_LEFT_THUMB)) {
                        strMsg = String.format("Wrong finger!\t Captured finger is %s", FPMapping.GetFileName((byte) nMatchIndex[0]));
                        SetErrorMessage(strMsg);
                        return false;
                    }
                } else //FT_PLAIN_RIGHT_THUMB
                {
                    if ((nMatchIndex[0] != ConstantDefs.FT_RIGHT_THUMB) && (nMatchIndex[0] != ConstantDefs.FT_ROLLED_RIGHT_THUMB)) {
                        strMsg = String.format("Wrong finger!\t Captured finger is %s", FPMapping.GetFileName((byte) nMatchIndex[0]));
                        SetErrorMessage(strMsg);
                        return false;
                    }
                }
            } else // not matched
            {
                //check if reference finger exist
                if (m_aiImage[nIndex - 16].pAcceptedImage != null || m_aiImage[nIndex - 6].pAcceptedImage != null) {
                    strMsg = String.format("Failed to check the finger sequence!\tMatch score is %d.", nScore[0]);
                    SetErrorMessage(strMsg);
                    strMsg += "\r\n\r\nDo you want to continue to accept the image?\r\n\r\n";
                    int nResponse = JOptionPane.showConfirmDialog(null,
                            strMsg,
                            "Attention", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (nResponse == JOptionPane.NO_OPTION)
                        return false;
                }
            }
        } else if (nIndex >= ConstantDefs.FT_ROLLED_LEFT_LITTLE) {
            if (m_verifyFP.Identify(m_libNative, m_pBuffer, m_ImageWidth, m_ImageHeight, nMatchIndex, nScore))    //matched
            {
                if (nMatchIndex[0] != (nIndex - 10) && nMatchIndex[0] != nIndex && (nMatchIndex[0] > ConstantDefs.FT_RIGHT_4_FINGERS)) {
                    strMsg = String.format("Wrong finger!\t Captured finger is %s", FPMapping.GetFileName((byte) nMatchIndex[0]));
                    SetErrorMessage(strMsg);
                    return false;
                }
            } else {
                //check if reference finger exist
                if (m_aiImage[nIndex - 10].pAcceptedImage != null) {
                    strMsg = String.format("Failed to check the finger sequence!\tMatch score is %d.", nScore[0]);
                    SetErrorMessage(strMsg);
                    strMsg += "\r\n\r\nDo you want to continue to accept the image?\r\n\r\n";
                    int nResponse = JOptionPane.showConfirmDialog(null,
                            strMsg,
                            "Attention", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (nResponse == JOptionPane.NO_OPTION)
                        return false;
                }
            }
        } else if (nIndex >= ConstantDefs.FT_LEFT_LITTLE)    //for flat finger
        {
            if (m_verifyFP.Identify(m_libNative, m_pBuffer, m_ImageWidth, m_ImageHeight, nMatchIndex, nScore))    //matched
            {
                if (nMatchIndex[0] != nIndex && nMatchIndex[0] > ConstantDefs.FT_RIGHT_4_FINGERS) {
                    strMsg = String.format("Wrong finger!\t Captured finger is %s", FPMapping.GetFileName((byte) nMatchIndex[0]));
                    SetErrorMessage(strMsg);
                    return false;
                }
            }
        } else    // if( nIndex >= FT_2_THUMBS )
        {
            strMsg = "Wrong finger! Captured finger is\t";
            String strTemp;
            if (m_bSegmentation) {
                byte[] pTemp = null;
                for (int i = 0; i < m_ParamSeg.nParamFing; i++) {
                    if (m_Subf[i].err == 0) {
                        pTemp = new byte[m_Subf[i].ws * m_Subf[i].hs];
                        System.arraycopy(m_pBufferSubf, i * m_Subf[i].ws * m_Subf[i].hs, pTemp, 0, m_Subf[i].ws * m_Subf[i].hs);
                        if (m_verifyFP.Identify(m_libNative, pTemp, m_Subf[i].ws, m_Subf[i].hs, nMatchIndex, nScore)) {
                            if (!FPMapping.IsMatchedFingerInSlaps((byte) nIndex, (byte) nMatchIndex[0])) {
                                strTemp = String.format("%d. %s. \t", i + 1, FPMapping.GetFileName((byte) nMatchIndex[0]));
                                strMsg += strTemp;
                                bWrong = true;
                            }
                        }
                        pTemp = null;
                    }
                }
            } else {
                if (m_verifyFP.Identify(m_libNative, m_pBuffer, m_ImageWidth, m_ImageHeight, nMatchIndex, nScore)) {
                    if (nMatchIndex[0] != nIndex) {
                        strTemp = String.format("%s. \t", FPMapping.GetFileName((byte) nMatchIndex[0]));
                        strMsg += strTemp;
                        bWrong = true;
                    }
                }
            }
            if (bWrong) {
                SetErrorMessage(strMsg);
                return false;
            }
        }
        return true;
    }

    /*******************************************************
     * Save the image to buffer
     *******************************************************/
    public boolean SaveAcceptedImage(byte ftCurrent) {
        if (ftCurrent > ConstantDefs.FT_PLAIN_RIGHT_THUMB) {
            SetErrorMessage("Invalid Finger Type!");
            return false;
        }
        byte nIndex = ftCurrent;
        //check PLAIN LEFT/RIGHT THUMB first
        if (nIndex == ConstantDefs.FT_PLAIN_LEFT_THUMB || nIndex == ConstantDefs.FT_PLAIN_RIGHT_THUMB) {
            if (m_Subf[0].err == 0)    // only one segmented image
            {
                m_aiImage[nIndex].pAcceptedImage = new byte[m_Subf[0].ws * m_Subf[0].hs];
                if (m_aiImage[nIndex].pAcceptedImage == null) {
                    SetErrorMessage("Not enough memory!");
                    return false;
                }
                m_aiImage[nIndex].nImageWidth = m_Subf[0].ws;
                m_aiImage[nIndex].nImageHeight = m_Subf[0].hs;
                System.arraycopy(m_pBufferSubf, 0, m_aiImage[nIndex].pAcceptedImage, 0, m_Subf[0].ws * m_Subf[0].hs);
                m_aiImage[nIndex].nFingerType = nIndex;
                m_aiImage[nIndex].nNFIQ = m_Subf[0].nfiq;
                m_aiImage[nIndex].nNumberSegments = 0;
                SetAnsiFingerPosition(nIndex);
                m_verifyFP.Enroll(m_libNative, m_aiImage[nIndex].pAcceptedImage, m_aiImage[nIndex].nImageWidth, m_aiImage[nIndex].nImageHeight, nIndex - 16);
                m_bIsImageSaved = true;
                return true;
            }
            SetErrorMessage("Invalid image");
            return false;
        }
        m_aiImage[nIndex].pAcceptedImage = new byte[m_ImageWidth * m_ImageHeight];
        if (m_aiImage[nIndex].pAcceptedImage == null) {
            return false;
        }
        m_aiImage[nIndex].nImageWidth = m_ImageWidth;
        m_aiImage[nIndex].nImageHeight = m_ImageHeight;
        System.arraycopy(m_pBuffer, 0, m_aiImage[nIndex].pAcceptedImage, 0, m_ImageWidth * m_ImageHeight);
        m_aiImage[nIndex].nFingerType = ftCurrent;
        m_aiImage[nIndex].nNumberSegments = 0;
        m_aiImage[nIndex].nNFIQ = m_nNfiq[0];

        SetAnsiFingerPosition(nIndex);

        if (nIndex < 3 && m_bSegmentation)    //check segmentation images
        {
            int nSubIndex = 0;
            int nOffset = 0;
            for (int i = 0; i < m_ParamSeg.nParamFing; i++) {
                if (m_Subf[i].err == 0) {
                    if (ftCurrent == ConstantDefs.FT_LEFT_4_FINGERS) {
                        nOffset = 0;
                        if (m_aiImage[nIndex].nNumberAmp > 0) {
                            boolean bSet = false;
                            for (int k = 10; k > 6; k--)    //10.9.8.7
                            {
                                bSet = false;
                                for (int j = 0; j < m_aiImage[nIndex].nNumberAmp; j++)    //check the AmpCode first
                                {
                                    if (m_aiImage[nIndex].fAmp[j].FingerId == (byte) k) {
                                        bSet = true;
                                        break;
                                    }
                                }
                                if (!bSet) {
                                    for (int j = 0; j < i; j++) {
                                        if (m_FingerSegments[j].FingerId == (byte) k) {
                                            bSet = true;
                                            break;
                                        }
                                    }
                                }
                                if (!bSet) {
                                    m_FingerSegments[i + nOffset].FingerId = (byte) k;
                                    break;
                                }
                            }
                        } else
                            m_FingerSegments[i + nOffset].FingerId = (byte) (10 - i);    //10. Left little finger
                    } else if (ftCurrent == ConstantDefs.FT_2_THUMBS) {
                        nOffset = 4;
                        if (m_aiImage[nIndex].nNumberAmp > 1)
                            break;
                        else if (m_aiImage[nIndex].nNumberAmp == 1) {
                            if (m_aiImage[nIndex].fAmp[0].FingerId == 6)
                                m_FingerSegments[i + nOffset].FingerId = 1;    // 1. Right Thumb
                            else
                                m_FingerSegments[i + nOffset].FingerId = 6;    // 6. Left Thumb
                        } else {
                            if (i == 0)
                                m_FingerSegments[i + nOffset].FingerId = 6;    // 6. Left Thumb
                            else
                                m_FingerSegments[i + nOffset].FingerId = 1;    // 1. Right Thumb
                        }
                    } else if (ftCurrent == ConstantDefs.FT_RIGHT_4_FINGERS) {
                        nOffset = 6;
                        if (m_aiImage[nIndex].nNumberAmp > 0) {
                            boolean bSet = false;
                            for (int k = 2; k < 6; k++)    //2.3.4.5
                            {
                                bSet = false;
                                for (int j = 0; j < m_aiImage[nIndex].nNumberAmp; j++)    //check the AmpCode first
                                {
                                    if (m_aiImage[nIndex].fAmp[j].FingerId == (byte) k) {
                                        bSet = true;
                                        break;
                                    }
                                }
                                if (!bSet) {
                                    for (int j = 6; j < i + 6; j++) {
                                        if (m_FingerSegments[j].FingerId == (byte) k) {
                                            bSet = true;
                                            break;
                                        }
                                    }
                                }
                                if (!bSet) {
                                    m_FingerSegments[i + nOffset].FingerId = (byte) k;
                                    break;
                                }
                            }
                        } else
                            m_FingerSegments[i + nOffset].FingerId = (byte) (2 + i);    // 2. Right index finger
                    }
                    m_FingerSegments[i + nOffset].NFIQ = (byte) m_Subf[i].nfiq;
                    if (m_bAngle) {
                        m_FingerSegments[i + nOffset].Top = m_FingerSegments[i + nOffset].Bottom
                                = m_FingerSegments[i + nOffset].Left = m_FingerSegments[i + nOffset].Right = 0;    //unknow bounding box, set to 0
                    } else {
                        m_FingerSegments[i + nOffset].Top = (short) (m_Subf[i].ys - m_Subf[i].hs / 2);
                        m_FingerSegments[i + nOffset].Bottom = (short) (m_Subf[i].ys + m_Subf[i].hs / 2);
                        m_FingerSegments[i + nOffset].Left = (short) (m_Subf[i].xs - m_Subf[i].ws / 2);
                        m_FingerSegments[i + nOffset].Right = (short) (m_Subf[i].xs + m_Subf[i].ws / 2);
                    }
                    switch (m_FingerSegments[i + nOffset].FingerId) {
                        case 10:
                            nSubIndex = 3;
                            break;
                        case 9:
                            nSubIndex = 4;
                            break;
                        case 8:
                            nSubIndex = 5;
                            break;
                        case 7:
                            nSubIndex = 6;
                            break;
                        case 6:
                            nSubIndex = 7;
                            break;
                        case 1:
                            nSubIndex = 8;
                            break;
                        case 2:
                            nSubIndex = 9;
                            break;
                        case 3:
                            nSubIndex = 10;
                            break;
                        case 4:
                            nSubIndex = 11;
                            break;
                        case 5:
                            nSubIndex = 12;
                            break;
                    }
                    m_aiImage[nIndex].nNumberSegments++;
                    m_aiImage[nSubIndex].pAcceptedImage = new byte[m_Subf[i].ws * m_Subf[i].hs];
                    if (m_aiImage[nSubIndex].pAcceptedImage == null)
                        return false;
                    m_aiImage[nSubIndex].nImageWidth = m_Subf[i].ws;
                    m_aiImage[nSubIndex].nImageHeight = m_Subf[i].hs;
                    System.arraycopy(m_pBufferSubf, i * m_Subf[i].ws * m_Subf[i].hs, m_aiImage[nSubIndex].pAcceptedImage, 0, m_Subf[i].ws * m_Subf[i].hs);
                    m_aiImage[nSubIndex].nFingerType = (byte) nSubIndex;
                    m_aiImage[nSubIndex].nNFIQ = m_Subf[i].nfiq;

                    m_verifyFP.Enroll(m_libNative, m_aiImage[nSubIndex].pAcceptedImage, m_aiImage[nSubIndex].nImageWidth, m_aiImage[nSubIndex].nImageHeight, nSubIndex);

                    SetAnsiFingerPosition(nSubIndex);
                }
            }
        } else {
            m_verifyFP.Enroll(m_libNative, m_aiImage[nIndex].pAcceptedImage, m_aiImage[nIndex].nImageWidth, m_aiImage[nIndex].nImageHeight, nIndex);
        }
        m_bIsImageSaved = true;
        return true;
    }

    public void ResetAmpNumber(byte ftCurrent) {
        m_aiImage[ftCurrent].nNumberAmp = 0;

        int i;
        if (ftCurrent == ConstantDefs.FT_LEFT_4_FINGERS) {
            for (i = ConstantDefs.FT_LEFT_LITTLE; i <= ConstantDefs.FT_LEFT_INDEX; i++)
                m_aiImage[i].nNumberAmp = 0;
            for (i = ConstantDefs.FT_ROLLED_LEFT_LITTLE; i <= ConstantDefs.FT_ROLLED_LEFT_INDEX; i++)
                m_aiImage[i].nNumberAmp = 0;
        } else if (ftCurrent == ConstantDefs.FT_2_THUMBS) {
            m_aiImage[ConstantDefs.FT_LEFT_THUMB].nNumberAmp = 0;
            m_aiImage[ConstantDefs.FT_RIGHT_THUMB].nNumberAmp = 0;
            m_aiImage[ConstantDefs.FT_ROLLED_LEFT_THUMB].nNumberAmp = 0;
            m_aiImage[ConstantDefs.FT_ROLLED_RIGHT_THUMB].nNumberAmp = 0;
            m_aiImage[ConstantDefs.FT_PLAIN_LEFT_THUMB].nNumberAmp = 0;
            m_aiImage[ConstantDefs.FT_PLAIN_RIGHT_THUMB].nNumberAmp = 0;
        } else if (ftCurrent == ConstantDefs.FT_RIGHT_4_FINGERS) {
            for (i = ConstantDefs.FT_RIGHT_INDEX; i <= ConstantDefs.FT_RIGHT_LITTLE; i++)
                m_aiImage[i].nNumberAmp = 0;
            for (i = ConstantDefs.FT_ROLLED_RIGHT_INDEX; i <= ConstantDefs.FT_ROLLED_RIGHT_LITTLE; i++)
                m_aiImage[i].nNumberAmp = 0;
        }
    }

    public void SetFingerAmpCode(byte ftCurrent, byte nFingerIndex, byte nAmpCode) {
        int nIndex = (int) ftCurrent;
        int i = m_aiImage[nIndex].nNumberAmp;
        byte ftSegmentFinger = 0;
        byte ftRollFinger = 0;

        if (ftCurrent == ConstantDefs.FT_LEFT_4_FINGERS) {
            m_aiImage[nIndex].fAmp[i].FingerId = (byte) (10 - nFingerIndex);
            ftSegmentFinger = (byte) (nFingerIndex + ConstantDefs.FT_LEFT_LITTLE);
        } else if (ftCurrent == ConstantDefs.FT_2_THUMBS) {
            if (nFingerIndex == 0) {
                m_aiImage[nIndex].fAmp[i].FingerId = 6;    // 6. Left Thumb
                ftSegmentFinger = ConstantDefs.FT_LEFT_THUMB;
            } else {
                m_aiImage[nIndex].fAmp[i].FingerId = 1;    // 1. Right Thumb
                ftSegmentFinger = ConstantDefs.FT_RIGHT_THUMB;
            }
        } else if (ftCurrent == ConstantDefs.FT_RIGHT_4_FINGERS) {
            m_aiImage[nIndex].fAmp[i].FingerId = (byte) (2 + nFingerIndex);    // 2. Right index finger
            ftSegmentFinger = (byte) (nFingerIndex + ConstantDefs.FT_RIGHT_INDEX);
        }
        SetAnsiFingerPosition(nIndex);
        m_aiImage[nIndex].fAmp[i].AMPCode = nAmpCode;
        m_aiImage[nIndex].nNumberAmp++;
        // Set the associated segment/roll/plain finger AMP code
        if (ftCurrent <= ConstantDefs.FT_RIGHT_4_FINGERS) {
            //flat fingers
            m_aiImage[ftSegmentFinger].nNumberAmp = 1;
            m_aiImage[ftSegmentFinger].fAmp[0].AMPCode = nAmpCode;
            m_aiImage[ftSegmentFinger].fAmp[0].FingerId = m_aiImage[nIndex].fAmp[i].FingerId;
            m_aiImage[ftSegmentFinger].pAcceptedImage = null;
            //rolled fingers
            ftRollFinger = (byte) (ftSegmentFinger + 10);
            SetAnsiFingerPosition(ftRollFinger);
            m_aiImage[ftRollFinger].nNumberAmp = 1;
            m_aiImage[ftRollFinger].fAmp[0].AMPCode = nAmpCode;
            m_aiImage[ftRollFinger].fAmp[0].FingerId = m_aiImage[nIndex].fAmp[i].FingerId;
            m_aiImage[ftRollFinger].pAcceptedImage = null;
            //thumbs
            if (ftSegmentFinger == ConstantDefs.FT_LEFT_THUMB) {
                SetAnsiFingerPosition(ConstantDefs.FT_PLAIN_LEFT_THUMB);
                m_aiImage[ConstantDefs.FT_PLAIN_LEFT_THUMB].nNumberAmp = 1;
                m_aiImage[ConstantDefs.FT_PLAIN_LEFT_THUMB].fAmp[0].AMPCode = nAmpCode;
                m_aiImage[ConstantDefs.FT_PLAIN_LEFT_THUMB].fAmp[0].FingerId = m_aiImage[nIndex].fAmp[i].FingerId;
                m_aiImage[ConstantDefs.FT_PLAIN_LEFT_THUMB].pAcceptedImage = null;
            } else if (ftSegmentFinger == ConstantDefs.FT_RIGHT_THUMB) {
                SetAnsiFingerPosition(ConstantDefs.FT_PLAIN_RIGHT_THUMB);
                m_aiImage[ConstantDefs.FT_PLAIN_RIGHT_THUMB].nNumberAmp = 1;
                m_aiImage[ConstantDefs.FT_PLAIN_RIGHT_THUMB].fAmp[0].AMPCode = nAmpCode;
                m_aiImage[ConstantDefs.FT_PLAIN_RIGHT_THUMB].fAmp[0].FingerId = m_aiImage[nIndex].fAmp[i].FingerId;
                m_aiImage[ConstantDefs.FT_PLAIN_RIGHT_THUMB].pAcceptedImage = null;
            }
        }
    }

    public void SetAnsiFingerPosition(int nIndex) {
        switch (nIndex) {
            case 0:
                m_aiImage[nIndex].nAnsiFingerPosition = 14;
                m_aiImage[nIndex].it = ACCEPTED_IMAGE.IMPRESSION_TYPE_PLAIN;
                break;
            case 1:
                m_aiImage[nIndex].nAnsiFingerPosition = 15;
                m_aiImage[nIndex].it = ACCEPTED_IMAGE.IMPRESSION_TYPE_PLAIN;
                break;
            case 2:
                m_aiImage[nIndex].nAnsiFingerPosition = 13;
                m_aiImage[nIndex].it = ACCEPTED_IMAGE.IMPRESSION_TYPE_PLAIN;
                break;
            case 13:
                m_aiImage[nIndex].nAnsiFingerPosition = 10;
                m_aiImage[nIndex].it = ACCEPTED_IMAGE.IMPRESSION_TYPE_ROLLED;
                break;
            case 14:
                m_aiImage[nIndex].nAnsiFingerPosition = 9;
                m_aiImage[nIndex].it = ACCEPTED_IMAGE.IMPRESSION_TYPE_ROLLED;
                break;
            case 15:
                m_aiImage[nIndex].nAnsiFingerPosition = 8;
                m_aiImage[nIndex].it = ACCEPTED_IMAGE.IMPRESSION_TYPE_ROLLED;
                break;
            case 16:
                m_aiImage[nIndex].nAnsiFingerPosition = 7;
                m_aiImage[nIndex].it = ACCEPTED_IMAGE.IMPRESSION_TYPE_ROLLED;
                break;
            case 17:
                m_aiImage[nIndex].nAnsiFingerPosition = 6;
                m_aiImage[nIndex].it = ACCEPTED_IMAGE.IMPRESSION_TYPE_ROLLED;
                break;
            case 18:
                m_aiImage[nIndex].nAnsiFingerPosition = 1;
                m_aiImage[nIndex].it = ACCEPTED_IMAGE.IMPRESSION_TYPE_ROLLED;
                break;
            case 19:
                m_aiImage[nIndex].nAnsiFingerPosition = 2;
                m_aiImage[nIndex].it = ACCEPTED_IMAGE.IMPRESSION_TYPE_ROLLED;
                break;
            case 20:
                m_aiImage[nIndex].nAnsiFingerPosition = 3;
                m_aiImage[nIndex].it = ACCEPTED_IMAGE.IMPRESSION_TYPE_ROLLED;
                break;
            case 21:
                m_aiImage[nIndex].nAnsiFingerPosition = 4;
                m_aiImage[nIndex].it = ACCEPTED_IMAGE.IMPRESSION_TYPE_ROLLED;
                break;
            case 22:
                m_aiImage[nIndex].nAnsiFingerPosition = 5;
                m_aiImage[nIndex].it = ACCEPTED_IMAGE.IMPRESSION_TYPE_ROLLED;
                break;
            case 23:    //FT_PLAIN_LEFT_THUMB
                m_aiImage[nIndex].nAnsiFingerPosition = 12;
                m_aiImage[nIndex].it = ACCEPTED_IMAGE.IMPRESSION_TYPE_PLAIN;
                break;
            case 24:    //FT_PLAIN_RIGHT_THUMB
                m_aiImage[nIndex].nAnsiFingerPosition = 11;
                m_aiImage[nIndex].it = ACCEPTED_IMAGE.IMPRESSION_TYPE_PLAIN;
                break;
        }
    }

    public void SetImageHandler(MyIcon fingerImage, JLabel showArea) {
        m_FingerImage = fingerImage;
        m_ShowArea = showArea;
    }

    public void SetShowImageHandler(JLabel showArea) {
        m_ShowArea = showArea;
    }

    public void ShowImage(int width, int height, byte[] pImage) {
        if (pImage == null) {
            m_hImage = null;
        } else {
            m_hImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            DataBuffer db1 = m_hImage.getRaster().getDataBuffer();
            for (int i = 0; i < db1.getSize(); i++) {
                db1.setElem(i, pImage[i]);
            }
        }
        m_FingerImage.setImage(m_hImage);
        m_FingerImage.setImageSize(width, height);
        m_ShowArea.repaint();
    }

    public void ShowImageText(Color colorText, String strTextMiddle) {
        m_FingerImage.setText(colorText, strTextMiddle);
        m_FingerImage.setNfiq(0);
        m_ShowArea.repaint();
    }

    private boolean GetAmpCode(byte ftFinger, byte[] nAmpCode) {
        if (ftFinger < ConstantDefs.FT_LEFT_LITTLE)
            return false;
        boolean bRet = false;
        if (m_aiImage[ftFinger].nNumberAmp > 0) {
            if (m_aiImage[ftFinger].fAmp[0].AMPCode > 0) {
                nAmpCode[0] = m_aiImage[ftFinger].fAmp[0].AMPCode;
                bRet = true;
            }
        }
        return bRet;
    }

    //check the rolled/plain thumbs finger is available or not 
    public boolean IsFingerUnavailable(byte ftCurrent) {
        if (ftCurrent < ConstantDefs.FT_LEFT_LITTLE)
            return false;
        byte[] nAmpCode = new byte[1];
        nAmpCode[0] = 0;
        return GetAmpCode(ftCurrent, nAmpCode);
    }

    /*******************************************************
     * Show the image from saved buffer
     *******************************************************/
    public boolean ShowAcceptedImage(byte ftCurrent, JLabel labelShowArea) {
        byte nIndex = ftCurrent;
        int width2 = labelShowArea.getWidth();
        int height2 = labelShowArea.getHeight();
        MyIcon iconImage = new MyIcon(width2, height2);
        if (m_aiImage[nIndex].pAcceptedImage == null) {
            iconImage.setImage(null);
            if (ftCurrent >= ConstantDefs.FT_LEFT_LITTLE) {
                byte[] nAmpCode = new byte[1];
                nAmpCode[0] = 0;
                if (GetAmpCode(ftCurrent, nAmpCode)) {
                    String strAmpCode = null;
                    if (nAmpCode[0] == 1)
                        strAmpCode = "Amputated";
                    else if (nAmpCode[0] == 2)
                        strAmpCode = "Bandaged";
                    iconImage.setText(Color.red, strAmpCode);
                }
            }
            labelShowArea.setIcon(iconImage);
            labelShowArea.repaint();
        } else {
            BufferedImage hImage = new BufferedImage(m_aiImage[nIndex].nImageWidth, m_aiImage[nIndex].nImageHeight, BufferedImage.TYPE_BYTE_GRAY);
            DataBuffer db1 = hImage.getRaster().getDataBuffer();
            for (int i = 0; i < db1.getSize(); i++) {
                db1.setElem(i, m_aiImage[nIndex].pAcceptedImage[i]);
            }
            iconImage.setImage(hImage);
            labelShowArea.setIcon(iconImage);
            labelShowArea.repaint();
        }
        return true;
    }

    public BufferedImage getFingerPrintImage(byte ftCurrent) {
        byte nIndex = ftCurrent;

        BufferedImage hImage = new BufferedImage(m_aiImage[nIndex].nImageWidth, m_aiImage[nIndex].nImageHeight, BufferedImage.TYPE_BYTE_GRAY);
        DataBuffer db1 = hImage.getRaster().getDataBuffer();
        for (int i = 0; i < db1.getSize(); i++) {
            db1.setElem(i, m_aiImage[nIndex].pAcceptedImage[i]);
        }

        return hImage;
    }


    public boolean SaveAcceptedImageToFile(String strFullQualifiedFolderName, String strFolderName, boolean bBMP, boolean bWSQ) {
        String strFileName;
        String strWsqFileName;
        if (bWSQ) {
            if (!Open())
                return false;
        }
        for (int i = 0; i < ConstantDefs.NUMBER_FINGER_TYPES; i++) {
            if (m_aiImage[i].pAcceptedImage != null) {
                strFileName = strFolderName + "_" + FPMapping.GetFileName((byte) i);
                strWsqFileName = strFileName;
                if (bBMP) {
                    strFileName += ".bmp";
                    File file = new File(strFullQualifiedFolderName, strFileName);
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        MyBitmapFile fileBMP = new MyBitmapFile(m_aiImage[i].nImageWidth, m_aiImage[i].nImageHeight, m_aiImage[i].pAcceptedImage);
                        out.write(fileBMP.toBytes());
                        out.close();
                    } catch (Exception e) {
                    }
                }
                if (bWSQ) {
                    strWsqFileName += (".wsq");
                    int[] nWsqSize = new int[1];
                    nWsqSize[0] = 0;
                    byte[] pTempWsq = new byte[m_aiImage[i].nImageWidth * m_aiImage[i].nImageHeight];
                    if (!ConvertRAWToWSQ(m_aiImage[i].pAcceptedImage, m_aiImage[i].nImageWidth, m_aiImage[i].nImageHeight, pTempWsq, nWsqSize))
                        return false;
                    File file = new File(strFullQualifiedFolderName, strWsqFileName);
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        out.write(pTempWsq, 0, nWsqSize[0]);
                        out.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
        if (bWSQ)
            Close();
        return true;
    }

    //EFTS -  Electronic Fingerprint Transmission Specification
    public boolean ExportToEFTS(String strFileName, byte nRecordType) {
        if (nRecordType != ConstantDefs.ETFS_RECORD_TYPE_ANSI_NIST_ITL_1_2007_4 && nRecordType != ConstantDefs.ETFS_RECORD_TYPE_ANSI_NIST_ITL_1_2007_14
                && nRecordType != ConstantDefs.ETFS_RECORD_TYPE_ANSI_381_2004 && nRecordType != ConstantDefs.ETFS_RECORD_TYPE_ISO_IEC_19794_4) {
            JOptionPane.showMessageDialog(null, "Invalid record type!", "Export", JOptionPane.OK_OPTION);
            return false;
        }
        //open device
        if (!Open())
            return false;
        boolean bRet = false;
        if (nRecordType == ConstantDefs.ETFS_RECORD_TYPE_ANSI_NIST_ITL_1_2007_4)
            bRet = ExportToAnsi2007(strFileName, (byte) 4);
        else if (nRecordType == ConstantDefs.ETFS_RECORD_TYPE_ANSI_NIST_ITL_1_2007_14)
            bRet = ExportToAnsi2007(strFileName, (byte) 14);
        else
            bRet = ExportToOtherStd(strFileName, nRecordType);
        Close();
        return bRet;
    }

    public boolean ExportToAnsi2007(String strFileName, byte nRecordType) {
        AnsiITL2007 ansi = new AnsiITL2007();
        byte[][] pWsqImage = new byte[ConstantDefs.NUMBER_FINGER_TYPES][];
        int[] nWsqSize = new int[1];
        short[] nDeviceID = new short[1];
        nDeviceID[0] = 0;
        nWsqSize[0] = 0;
        String strDevice = ConstantDefs.GetDeviceName(m_nDeviceCompatibility, nDeviceID);
        ansi.AddRecordType2(strDevice);
        ansi.m_nType = nRecordType;
        int nSegOffset = 0;
        int i;
        byte[] pTempWsq = null;
        for (i = 0; i < ConstantDefs.NUMBER_FINGER_TYPES; i++)
            pWsqImage[i] = null;
        // Slaps
        for (i = 0; i < 3; i++) {
            if (i == 0)
                nSegOffset = 0;
            else if (i == 1)
                nSegOffset = 4;
            else
                nSegOffset = 6;
            if (i == 1 && nRecordType == 4)    // Record Type4 can not save FT_2_THUMBS
                continue;
            if (m_aiImage[i].pAcceptedImage != null) {
                pTempWsq = new byte[m_aiImage[i].nImageWidth * m_aiImage[i].nImageHeight];
                if (!ConvertRAWToWSQ(m_aiImage[i].pAcceptedImage, m_aiImage[i].nImageWidth, m_aiImage[i].nImageHeight, pTempWsq, nWsqSize))
                    return false;
                pWsqImage[i] = new byte[nWsqSize[0]];
                System.arraycopy(pTempWsq, 0, pWsqImage[i], 0, nWsqSize[0]);
                if (nRecordType == 14)
                    ansi.AddRecordType14(pWsqImage[i], nWsqSize[0], m_aiImage[i].nImageWidth, m_aiImage[i].nImageHeight, m_aiImage[i].it,
                            "WSQ20", "Demo", "", m_aiImage[i].nAnsiFingerPosition, m_aiImage[i].nNumberSegments, m_FingerSegments, nSegOffset, m_aiImage[i].nNumberAmp, m_aiImage[i].fAmp, m_aiImage[i].nNFIQ);
                else if (nRecordType == 4)
                    ansi.AddRecordType4(pWsqImage[i], nWsqSize[0], (short) m_aiImage[i].nImageWidth, (short) m_aiImage[i].nImageHeight, m_aiImage[i].it, (byte) m_aiImage[i].nAnsiFingerPosition);
            } else {
                if (m_aiImage[i].nNumberAmp > 0 && nRecordType == 14) {
                    ansi.AddRecordType14(null, 0, 0, 0, m_aiImage[i].it,
                            "NONE", "Demo", "", m_aiImage[i].nAnsiFingerPosition, 0, null, 0, m_aiImage[i].nNumberAmp, m_aiImage[i].fAmp, m_aiImage[i].nNFIQ);
                }
            }
        }
        if (nRecordType == 4)    //save FT_PLAIN_LEFT_THUMB & FT_PLAIN_RIGHT_THUMB
        {
            for (i = 23; i < 25; i++) {
                if (m_aiImage[i].pAcceptedImage != null) {
                    pTempWsq = new byte[m_aiImage[i].nImageWidth * m_aiImage[i].nImageHeight];
                    if (!ConvertRAWToWSQ(m_aiImage[i].pAcceptedImage, m_aiImage[i].nImageWidth, m_aiImage[i].nImageHeight, pTempWsq, nWsqSize))
                        return false;
                    pWsqImage[i] = new byte[nWsqSize[0]];
                    System.arraycopy(pTempWsq, 0, pWsqImage[i], 0, nWsqSize[0]);
                    ansi.AddRecordType4(pWsqImage[i], nWsqSize[0], (short) m_aiImage[i].nImageWidth, (short) m_aiImage[i].nImageHeight, m_aiImage[i].it, (byte) m_aiImage[i].nAnsiFingerPosition);
                }
            }
        }
        // Rolled fingers
        for (i = 13; i < 23; i++) {
            if (m_aiImage[i].pAcceptedImage != null)    //FT_ROLLED_LEFT_LITTLE
            {
                pTempWsq = new byte[m_aiImage[i].nImageWidth * m_aiImage[i].nImageHeight];
                if (!ConvertRAWToWSQ(m_aiImage[i].pAcceptedImage, m_aiImage[i].nImageWidth, m_aiImage[i].nImageHeight, pTempWsq, nWsqSize))
                    return false;
                pWsqImage[i] = new byte[nWsqSize[0]];
                System.arraycopy(pTempWsq, 0, pWsqImage[i], 0, nWsqSize[0]);
                if (nRecordType == 14)
                    ansi.AddRecordType14(pWsqImage[i], nWsqSize[0], m_aiImage[i].nImageWidth, m_aiImage[i].nImageHeight, m_aiImage[i].it,
                            "WSQ20", "Demo", "", m_aiImage[i].nAnsiFingerPosition, 0, null, 0, 0, null, m_aiImage[i].nNFIQ);
                else if (nRecordType == 4)
                    ansi.AddRecordType4(pWsqImage[i], nWsqSize[0], (short) m_aiImage[i].nImageWidth, (short) m_aiImage[i].nImageHeight, m_aiImage[i].it, (byte) m_aiImage[i].nAnsiFingerPosition);
            } else {
                if (m_aiImage[i].nNumberAmp > 0 && nRecordType == 14) {
                    m_aiImage[i].fAmp[0].FingerId = (byte) m_aiImage[i].nAnsiFingerPosition;
                    ansi.AddRecordType14(null, 0, 0, 0, m_aiImage[i].it,
                            "NONE", "Demo", "", m_aiImage[i].nAnsiFingerPosition, 0, null, 0, 1, m_aiImage[i].fAmp, m_aiImage[i].nNFIQ);
                }
            }
        }
        boolean bRet = ansi.SaveRecord(strFileName);
        for (i = 0; i < ConstantDefs.NUMBER_FINGER_TYPES; i++) {
            pWsqImage[i] = null;
        }
        return bRet;
    }

    public boolean ExportToOtherStd(String strFileName, byte nRecordType) {
        FPDataInterchange diFP = new FPDataInterchange();
        byte nStd;
        if (nRecordType == ConstantDefs.ETFS_RECORD_TYPE_ANSI_381_2004)
            nStd = ConstantDefs.FIR_STD_ANSI;
        else
            nStd = ConstantDefs.FIR_STD_ISO;
        if (!diFP.Initialize(m_libNative, nStd, m_nDeviceID))
            return false;
        byte[][] pWsqImage = new byte[ConstantDefs.NUMBER_FINGER_TYPES][];
        int[] nWsqSize = new int[1];
        int i;
        boolean bRet = true;
        byte nNumFinger = 0;
        byte[] pTempWsq = null;
        nWsqSize[0] = 0;
        for (i = 0; i < ConstantDefs.NUMBER_FINGER_TYPES; i++)
            pWsqImage[i] = null;

        for (i = 0; i < (ConstantDefs.NUMBER_FINGER_TYPES - 2); i++) {
            if (i >= ConstantDefs.FT_LEFT_LITTLE && i <= ConstantDefs.FT_RIGHT_LITTLE)    //skip
                continue;
            if (m_aiImage[i].pAcceptedImage != null) {
                pTempWsq = new byte[m_aiImage[i].nImageWidth * m_aiImage[i].nImageHeight];
                bRet = ConvertRAWToWSQ(m_aiImage[i].pAcceptedImage, m_aiImage[i].nImageWidth, m_aiImage[i].nImageHeight, pTempWsq, nWsqSize);
                if (!bRet)
                    break;
                pWsqImage[i] = new byte[nWsqSize[0]];
                System.arraycopy(pTempWsq, 0, pWsqImage[i], 0, nWsqSize[0]);
                bRet = diFP.AddImage(pWsqImage[i], nWsqSize[0], m_aiImage[i].nImageWidth, m_aiImage[i].nImageHeight, (byte) m_aiImage[i].nAnsiFingerPosition, (byte) m_aiImage[i].nNFIQ, m_aiImage[i].it);
                if (!bRet)
                    break;
                nNumFinger++;
            }
        }
        if (bRet && (nNumFinger > 0)) {
            bRet = diFP.SaveRecord(strFileName);
        }
        for (i = 0; i < ConstantDefs.NUMBER_FINGER_TYPES; i++) {
            pWsqImage[i] = null;
        }
        diFP.Terminate();
        return (bRet && (nNumFinger > 0));
    }

    public boolean ConvertRAWToWSQ(byte[] pRaw, int nRawWidth, int nRawHeight, byte[] pWsq, int[] nWsqSize) {
        if (pRaw == null || pWsq == null || nRawWidth <= 0 || nRawHeight <= 0) {
            JOptionPane.showMessageDialog(null, "No Image!", "Convert to wsq ERROR", JOptionPane.OK_OPTION);
            return false;
        }
        byte[] pWSQImage = null;
        pWSQImage = new byte[nRawWidth * nRawHeight];
        boolean bRet = m_libNative.WsqFromRAWImage(pRaw, nRawWidth, nRawHeight, 2.25f, nWsqSize, pWSQImage);
        if (!bRet)    //error occurs
        {
            pWSQImage = null;
            JOptionPane.showMessageDialog(null, "ftrWSQ_FromRAWImage return false!", "Write wsq file", JOptionPane.OK_OPTION);
            return false;
        }
        System.arraycopy(pWSQImage, 0, pWsq, 0, nWsqSize[0]);
        return true;
    }

    private class WorkerThread extends Thread {
        private boolean m_bRun = false;

        public WorkerThread() {
            m_bRun = false;
        }

        public void run() {
            m_bRun = true;
            m_bIsScanning = true;
            if (m_bIsRoll)
                DoRoll();
            else
                DoScan();
            m_bIsScanning = false;
        }

        public void cancel() {
            if (!m_bRun)
                return;
            m_bStopOperation = true;
            if (m_bIsRoll && m_bIsScanning) {
                m_libNative.RollAbort(); // Abort roll operation asynchronously
            }
            try {
                this.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(FPDevice.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
