package com.seamfix.scanners;

import com.futronictech.fs6xenrollmentkit.interfaces.IScanCompleteEventListener;
import com.futronictech.fs6xenrollmentkit.lib.ConstantDefs;
import com.futronictech.fs6xenrollmentkit.lib.FPDevice;
import com.futronictech.fs6xenrollmentkit.interfaces.ICallBack;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class FutronicFS64 implements ICallBack {
    public void setM_nScanType(byte m_nScanType) {
        this.m_nScanType = m_nScanType;
    }

    public byte m_nScanType = ConstantDefs.DEVICE_SCAN_TYPE_SLAPS;    //bit0. Slaps 1.Single 2.Rolled
    private int m_nDiagnosticCode = 0;
    public byte m_nSequence = 0;
    private static FPDevice m_DevFP = null;
    public static  FutronicFS64 futronicFS64;
    private String deviceInfo = "";
    private Timer m_Timer = null;
    private JLabel showLabel;
    public boolean m_bAskUnavailabilityReason;
    private IScanCompleteEventListener eventListener;

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    public String getType() {
        return type;
    }

    private String name;
    private String model;
    private String serialNumber = "N/A";
    private String type;
    private Date loadDate = new Date();

    public ArrayList<BufferedImage> getLeftHandImages() {
        return leftHandImages;
    }

    public ArrayList<BufferedImage> getRightHandImages() {
        return rightHandImages;
    }

    public ArrayList<BufferedImage> getTwoThumbsImages() {
        return twoThumbsImages;
    }


    ArrayList<BufferedImage> leftHandImages;
    ArrayList<BufferedImage> rightHandImages;
    ArrayList<BufferedImage> twoThumbsImages;

    public BufferedImage getFingerImage() {
        return fingerImage;
    }

    public void setFingerImage(BufferedImage fingerImage) {
        this.fingerImage = fingerImage;
    }

    private BufferedImage fingerImage;

    public boolean isAcquisitionSuccess() {
        return acquisitionSuccess;
    }

    private boolean acquisitionSuccess;

    public String getErrorMessage() {
        return errorMessage;
    }

    private String errorMessage;

    public FutronicFS64(FPDevice device) {
        m_DevFP = device;
        leftHandImages = new ArrayList<BufferedImage>();
        rightHandImages = new ArrayList<BufferedImage>();
        twoThumbsImages = new ArrayList<BufferedImage>();
    }

    public static FutronicFS64 getInstance() {
        FPDevice fpDevice;
        synchronized (FutronicFS64.class) {
            if (futronicFS64 == null) {
                fpDevice = new FPDevice();
                futronicFS64 = new FutronicFS64(fpDevice);
            } else {
                futronicFS64 = null;
                fpDevice = new FPDevice();
                futronicFS64 = new FutronicFS64(fpDevice);
            }
        }
        return futronicFS64;
    }

    public void SetShowImageHandler(JLabel label){
        m_DevFP.SetShowImageHandler(label);
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public boolean initialize() {
        try {
            if (m_DevFP.Open()) {
                deviceInfo = m_DevFP.GetDeviceInfo();

                String[] split = deviceInfo.split(",");
                String[] nameSplit = split[0].split(":");
                this.name = nameSplit[1];
                this.model = nameSplit[1];
                if (model.equalsIgnoreCase("FS64")){
                    this.type = "4-4-2";
                }
                m_DevFP.SetAutoCapture(true);
                m_DevFP.SetSound(true);
                m_DevFP.SetSegmentation(true);
                m_bAskUnavailabilityReason = false;
                return true;
            } else {
                return false;
            }
        }
        catch (IllegalStateException ex){
            ex.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public void exit() {
        if (m_DevFP != null) {
            m_DevFP.Stop();
            m_DevFP.TurnOffLed();
            m_DevFP.Close();
        }
    }

    public void OnAction(int nAction) {
        if (nAction == 0) {
            if (m_DevFP.VerifyImage(m_nSequence)) {

                int[] nCode = new int[1];
                errorMessage = m_DevFP.GetDiagnostic(nCode);
                m_nDiagnosticCode = nCode[0];
                if (m_nDiagnosticCode == 0) {
                    acquisitionSuccess = true;
                    eventListener.onScanComplete(acquisitionSuccess, errorMessage, m_nSequence);
                } else {
                    acquisitionSuccess = false;
                    eventListener.onScanComplete(acquisitionSuccess, errorMessage, m_nSequence);
                }
            } else {
                acquisitionSuccess = false;
                errorMessage = m_DevFP.GetErrorMessage();
                eventListener.onScanComplete(acquisitionSuccess, errorMessage, m_nSequence);
            }
        } else if (nAction == 1) {

        } else if (nAction == 2) {
            //acquisitionSuccess = false;
        } else if (nAction == 4) {
            acquisitionSuccess = false;
            errorMessage = m_DevFP.GetErrorMessage();
            eventListener.onScanComplete(acquisitionSuccess, errorMessage, m_nSequence);
        }


    }


    public void AcceptImage(byte finger) {
        if (m_bAskUnavailabilityReason) {
            int[] nUnavailableFingers = new int[1];
            nUnavailableFingers[0] = 0;
            boolean bUnavailable = m_DevFP.IsUnavailable(nUnavailableFingers);
            m_DevFP.ResetAmpNumber(m_nSequence);
        }
        try {
            m_DevFP.SaveAcceptedImage(m_nSequence);
        }
        catch (IllegalStateException ex){
            ex.printStackTrace();
        }
        catch (Exception e){
            System.out.println("Error while saving fingerprint image to memory");
            errorMessage = "Error while saving fingerprint image to memory";
            eventListener.onScanComplete(false, errorMessage, finger);
            e.printStackTrace();
        }


        if (m_nSequence >= ConstantDefs.FT_ROLLED_LEFT_LITTLE) {
            byte nSeq = m_nSequence;
            for (byte i = nSeq; i <= ConstantDefs.FT_PLAIN_RIGHT_THUMB; i++) {
                if (!m_DevFP.IsFingerUnavailable(i))
                    break;
                else
                    m_nSequence++;
            }
        }

        try {
            switch (finger) {
                case ConstantDefs
                        .FT_LEFT_4_FINGERS:
                    leftHandImages.clear();
                    leftHandImages.add(0, m_DevFP.getFingerPrintImage(ConstantDefs.FT_LEFT_LITTLE));
                    leftHandImages.add(1, m_DevFP.getFingerPrintImage(ConstantDefs.FT_LEFT_RING));
                    leftHandImages.add(2, m_DevFP.getFingerPrintImage(ConstantDefs.FT_LEFT_MIDDLE));
                    leftHandImages.add(3, m_DevFP.getFingerPrintImage(ConstantDefs.FT_LEFT_INDEX));
                    m_nSequence = ConstantDefs.FT_RIGHT_4_FINGERS;
                    break;
                case ConstantDefs.FT_RIGHT_4_FINGERS:
                    rightHandImages.clear();
                    rightHandImages.add(0, m_DevFP.getFingerPrintImage(ConstantDefs.FT_RIGHT_INDEX));
                    rightHandImages.add(1, m_DevFP.getFingerPrintImage(ConstantDefs.FT_RIGHT_MIDDLE));
                    rightHandImages.add(2, m_DevFP.getFingerPrintImage(ConstantDefs.FT_RIGHT_RING));
                    rightHandImages.add(3, m_DevFP.getFingerPrintImage(ConstantDefs.FT_RIGHT_LITTLE));

                    m_nSequence = ConstantDefs.FT_2_THUMBS;
                    break;
                case ConstantDefs.FT_2_THUMBS:
                    twoThumbsImages.clear();
                    twoThumbsImages.add(0, m_DevFP.getFingerPrintImage(ConstantDefs.FT_LEFT_THUMB));
                    twoThumbsImages.add(1, m_DevFP.getFingerPrintImage(ConstantDefs.FT_RIGHT_THUMB));
                    //m_nSequence = ConstantDefs.NUMBER_FINGER_TYPES;
                    //eventListener.onScanComplete(true, "Scan Completed", m_nSequence);
                    break;
                case ConstantDefs.FT_PLAIN_LEFT_THUMB:
                    setFingerImage(m_DevFP.getFingerPrintImage(ConstantDefs.FT_PLAIN_LEFT_THUMB).getSubimage(70, 40, 380, 520));
                    //m_nSequence = ConstantDefs.NUMBER_FINGER_TYPES;
                    //eventListener.onScanComplete(true, "Scan Completed", m_nSequence);
                    break;
            }

        }
        catch (Exception ex){
            System.out.println("Error getting saved images from memory");
            errorMessage = "Error getting saved images from memory";
            eventListener.onScanComplete(false, errorMessage, finger);
            ex.printStackTrace();
        }

    }

    public BufferedImage getFingerPrintImage(byte finger){
        return m_DevFP.getFingerPrintImage(finger);
    }

    public void SetLabelToFixedSize(JLabel label) {
        int width = label.getWidth();
        int height = label.getHeight();
        Dimension dm = new Dimension(width, height);
        label.setMinimumSize(dm);
        label.setMaximumSize(dm);
        label.setPreferredSize(dm);
    }

    /*********************************************************
     * Start scanning fingerprint
     *
     *********************************************************/
    public void StartScanning() {
        m_DevFP.SetCurrentFinger(m_nSequence);

        try {
            if (m_nSequence < 3) {

                if (m_nSequence == 1) {
                    m_DevFP.SetImageFormat(FPDevice.IMAGE_FORMAT_800_750);
                } else {
                    m_DevFP.SetImageFormat(FPDevice.IMAGE_FORMAT_1600_1500);
                }

                if (!m_DevFP.Scan()) {
                    errorMessage = m_DevFP.GetErrorMessage();
                }
            } else if (m_nSequence < ConstantDefs.FT_ROLLED_LEFT_LITTLE) {

                m_DevFP.SetImageFormat(FPDevice.IMAGE_FORMAT_800_750);

                if (!m_DevFP.Scan()) {
                    errorMessage = m_DevFP.GetErrorMessage();
                }

            } else if (m_nSequence < ConstantDefs.FT_ROLLED_RIGHT_LITTLE + 1) {
                m_DevFP.SetImageFormat(FPDevice.IMAGE_FORMAT_800_750);
                if (!m_DevFP.RollScan()) {
                    errorMessage = m_DevFP.GetErrorMessage();
                }
            } else if (m_nSequence < ConstantDefs.FT_PLAIN_RIGHT_THUMB + 1) {
                m_DevFP.SetImageFormat(FPDevice.IMAGE_FORMAT_800_750);

                if (!m_DevFP.Scan()) {
                    errorMessage = m_DevFP.GetErrorMessage();
                }

            } else {
                m_DevFP.TurnOffLed();
            }
        }
        catch (Exception ex){
            System.out.println("Error occurred during scan");
            errorMessage = "Error occurred during scan";
            eventListener.onScanComplete(false, errorMessage, m_nSequence);
            ex.printStackTrace();
        }


    }

    private void StartOperation() {
        m_DevFP.SetCallback(this);
        StartScanning();
    }

    public void setM_nSequence(byte finger) {
        m_nSequence = finger;
    }

    public void runCapture(final byte finger) {

        m_nSequence = finger;
        if (m_nSequence == ConstantDefs.FT_2_THUMBS) {
            m_nScanType = ConstantDefs.DEVICE_SCAN_TYPE_2THUMBS;
        } else if (m_nSequence == ConstantDefs.FT_LEFT_4_FINGERS || m_nSequence == ConstantDefs.FT_RIGHT_4_FINGERS) {
            m_nScanType = ConstantDefs.DEVICE_SCAN_TYPE_SLAPS;
        } else if (m_nSequence > ConstantDefs.FT_RIGHT_4_FINGERS) {
            m_nScanType = ConstantDefs.DEVICE_SCAN_TYPE_FLAT_FINGER;
        }

        m_Timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                StartOperation();
            }
        };
        m_Timer.schedule(timerTask, 20);
    }

    public void runSingleCapture(){
        m_nSequence = ConstantDefs.FT_PLAIN_LEFT_THUMB;
        m_nScanType = ConstantDefs.DEVICE_SCAN_TYPE_FLAT_FINGER;
        m_Timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                StartOperation();
            }
        };
        m_Timer.schedule(timerTask, 20);
    }

    public void scanCompleteEventHandler(IScanCompleteEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void TurnOffLed(){
        m_DevFP.TurnOffLed();
    }

    public boolean CanSlaps(){
        return m_DevFP.CanSlaps();
    }

    public void Stop(){
        m_DevFP.Stop();
    }

    public void ShowAcceptedImage(byte finger, JLabel label){
        this.showLabel = label;
        m_DevFP.ShowAcceptedImage(finger, showLabel);
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append((this.name == null) || (this.name.trim().length() == 0) ? "N/A" : this.name.replaceAll("\\n", " "))
                .append("\nSerial: ").append((this.serialNumber == null) || (this.serialNumber.trim().length() == 0) ? "N/A" : this.serialNumber)
                .append("\nType: ").append(this.type)
                .append("\nLoaded: ").append(this.loadDate)
                .append("\nModel: ").append(this.model)
        ;
        return sb.toString();
    }

}
