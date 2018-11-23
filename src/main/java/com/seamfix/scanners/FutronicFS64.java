package com.seamfix.scanners;

import com.futronictech.fs6xenrollmentkit.interfaces.IScanCompleteEventListener;
import com.futronictech.fs6xenrollmentkit.lib.ConstantDefs;
import com.futronictech.fs6xenrollmentkit.lib.FPDevice;
import com.futronictech.fs6xenrollmentkit.interfaces.ICallBack;
import lombok.extern.log4j.Log4j;

import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

@Log4j
public class FutronicFS64 implements ICallBack {

    public byte m_nScanType = ConstantDefs.DEVICE_SCAN_TYPE_SLAPS;    //bit0. Slaps 1.Single 2.Rolled
    private int diagnosticCode = 0;
    public byte nSequence = 0;
    private static FPDevice fpDevice = null;
    private String deviceInfo = "";
    private Timer timer = null;
    private JLabel showLabel;
    public boolean m_bAskUnavailabilityReason;
    private IScanCompleteEventListener eventListener;
    private boolean isScanCompleted = false;

    private String name;
    private String model;
    private String serialNumber = "N/A";
    private String type;
    private Date loadDate = new Date();

    private ArrayList<BufferedImage> leftHandImages;
    private ArrayList<BufferedImage> rightHandImages;
    private ArrayList<BufferedImage> twoThumbsImages;

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

    public FutronicFS64(){
        this(new FPDevice());
    }

    public FutronicFS64(FPDevice device) {
        fpDevice = device;
        leftHandImages = new ArrayList<BufferedImage>();
        rightHandImages = new ArrayList<BufferedImage>();
        twoThumbsImages = new ArrayList<BufferedImage>();
    }

    public void SetShowImageHandler(JLabel label) {
        fpDevice.SetShowImageHandler(label);
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public boolean initialize() {
        try {
            if (isOpen()) {
                fpDevice.SetAutoCapture(true);
                fpDevice.SetSound(true);
                fpDevice.SetSegmentation(true);
                m_bAskUnavailabilityReason = false;
                return true;
            } else {
                return false;
            }
        } catch (IllegalStateException ex) {
            log.error(ex.getMessage(), ex);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean isOpen(){
        if (fpDevice != null && fpDevice.Open()){
            deviceInfo = fpDevice.GetDeviceInfo();
            String[] split = deviceInfo.split("\n");
            String[] nameSplit = split[0].split(": ");
            this.name = "Futronic";
            this.model = nameSplit[1];
            if (model.contains("FS64")) {
                this.type = ConstantDefs.SCANNER_TYPE_MULTIPLE;
            }
            else {
                this.type = ConstantDefs.SCANNER_TYPE_SINGLE;
            }

            return true;
        } else {
            return false;
        }
    }

    public void exit() {
        if (fpDevice != null) {
            //end every scan process
            eventListener.onScanComplete(false, "exiting", (byte) -1);
            fpDevice.Stop();
            fpDevice.TurnOffLed();
            fpDevice.Close();
        }
    }

    public void refresh(){
        //fpDevice.Open();
        fpDevice.SetAutoCapture(true);
        fpDevice.SetSound(true);
        fpDevice.SetSegmentation(true);
    }

    public void OnAction(int nAction) {
        if (nAction == 0) {
            if (fpDevice.VerifyImage(nSequence)) {

                int[] nCode = new int[1];
                errorMessage = fpDevice.GetDiagnostic(nCode);
                diagnosticCode = nCode[0];
                if (diagnosticCode == 0) {
                    acquisitionSuccess = true;
                    isScanCompleted = true;
                    eventListener.onScanComplete(acquisitionSuccess, errorMessage, nSequence);
                } else {
                    acquisitionSuccess = false;
                    isScanCompleted = true;
                    eventListener.onScanComplete(acquisitionSuccess, errorMessage, nSequence);
                }
            } else {
                acquisitionSuccess = false;
                errorMessage = fpDevice.GetErrorMessage();
                isScanCompleted = true;
                eventListener.onScanComplete(acquisitionSuccess, errorMessage, nSequence);
            }
        } else if (nAction == 1) {

        } else if (nAction == 2) {
            //acquisitionSuccess = false;
        } else if (nAction == 4) {
            acquisitionSuccess = false;
            errorMessage = fpDevice.GetErrorMessage();
            isScanCompleted = true;
            eventListener.onScanComplete(acquisitionSuccess, errorMessage, nSequence);
        }


    }

    public ArrayList<BufferedImage> getLeftHandImages() {
        return leftHandImages;
    }

    public ArrayList<BufferedImage> getRightHandImages() {
        return rightHandImages;
    }

    public ArrayList<BufferedImage> getTwoThumbsImages() {
        return twoThumbsImages;
    }

    public void AcceptImage(byte finger) {
        if (m_bAskUnavailabilityReason) {
            int[] nUnavailableFingers = new int[1];
            nUnavailableFingers[0] = 0;
            boolean bUnavailable = fpDevice.IsUnavailable(nUnavailableFingers);
            fpDevice.ResetAmpNumber(nSequence);
        }
        try {
            fpDevice.SaveAcceptedImage(nSequence);
        } catch (IllegalStateException ex) {
            log.error(ex.getMessage(), ex);
        } catch (Exception e) {
            log.error("Error while saving fingerprint image to memory", e);
            errorMessage = "Error while saving fingerprint image to memory";
            eventListener.onScanComplete(false, errorMessage, ConstantDefs.UNKNOWN_FINGER);
            log.error(e.getMessage(), e);
        }


        if (nSequence >= ConstantDefs.FT_ROLLED_LEFT_LITTLE) {
            byte nSeq = nSequence;
            for (byte i = nSeq; i <= ConstantDefs.FT_PLAIN_RIGHT_THUMB; i++) {
                if (!fpDevice.IsFingerUnavailable(i))
                    break;
                else
                    nSequence++;
            }
        }

        try {
            switch (finger) {
                case ConstantDefs
                        .FT_LEFT_4_FINGERS:
                    leftHandImages.clear();
                    leftHandImages.add(0, fpDevice.getFingerPrintImage(ConstantDefs.FT_LEFT_LITTLE));
                    leftHandImages.add(1, fpDevice.getFingerPrintImage(ConstantDefs.FT_LEFT_RING));
                    leftHandImages.add(2, fpDevice.getFingerPrintImage(ConstantDefs.FT_LEFT_MIDDLE));
                    leftHandImages.add(3, fpDevice.getFingerPrintImage(ConstantDefs.FT_LEFT_INDEX));
                    nSequence = ConstantDefs.FT_RIGHT_4_FINGERS;
                    break;
                case ConstantDefs.FT_RIGHT_4_FINGERS:
                    rightHandImages.clear();
                    rightHandImages.add(0, fpDevice.getFingerPrintImage(ConstantDefs.FT_RIGHT_INDEX));
                    rightHandImages.add(1, fpDevice.getFingerPrintImage(ConstantDefs.FT_RIGHT_MIDDLE));
                    rightHandImages.add(2, fpDevice.getFingerPrintImage(ConstantDefs.FT_RIGHT_RING));
                    rightHandImages.add(3, fpDevice.getFingerPrintImage(ConstantDefs.FT_RIGHT_LITTLE));

                    nSequence = ConstantDefs.FT_2_THUMBS;
                    break;
                case ConstantDefs.FT_2_THUMBS:
                    twoThumbsImages.clear();
                    twoThumbsImages.add(0, fpDevice.getFingerPrintImage(ConstantDefs.FT_LEFT_THUMB));
                    twoThumbsImages.add(1, fpDevice.getFingerPrintImage(ConstantDefs.FT_RIGHT_THUMB));

                    break;
                case ConstantDefs.FT_PLAIN_LEFT_THUMB:
                    setFingerImage(fpDevice.getFingerPrintImage(ConstantDefs.FT_PLAIN_LEFT_THUMB).getSubimage(70, 40, 380, 520));
                    break;
            }

        } catch (Exception ex) {
            log.error("Error getting saved images from memory", ex);
            errorMessage = "Error getting saved images from memory";
            eventListener.onScanComplete(false, errorMessage, ConstantDefs.UNKNOWN_FINGER);
            log.error(ex.getMessage(),ex);
        }

    }

    public BufferedImage getFingerPrintImage(byte finger) {
        return fpDevice.getFingerPrintImage(finger);
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
        fpDevice.SetCurrentFinger(nSequence);

        try {
            if (nSequence < 3) {

                if (nSequence == 1) {
                    fpDevice.SetImageFormat(FPDevice.IMAGE_FORMAT_800_750);
                } else {
                    fpDevice.SetImageFormat(FPDevice.IMAGE_FORMAT_1600_1500);
                }

                if (!fpDevice.Scan()) {
                    errorMessage = fpDevice.GetErrorMessage();
                }
            } else if (nSequence < ConstantDefs.FT_ROLLED_LEFT_LITTLE) {

                fpDevice.SetImageFormat(FPDevice.IMAGE_FORMAT_800_750);

                if (!fpDevice.Scan()) {
                    errorMessage = fpDevice.GetErrorMessage();
                }

            } else if (nSequence < ConstantDefs.FT_ROLLED_RIGHT_LITTLE + 1) {
                fpDevice.SetImageFormat(FPDevice.IMAGE_FORMAT_800_750);
                if (!fpDevice.RollScan()) {
                    errorMessage = fpDevice.GetErrorMessage();
                }
            } else if (nSequence < ConstantDefs.FT_PLAIN_RIGHT_THUMB + 1) {
                fpDevice.SetImageFormat(FPDevice.IMAGE_FORMAT_800_750);

                if (!fpDevice.Scan()) {
                    errorMessage = fpDevice.GetErrorMessage();
                }

            } else {
                fpDevice.TurnOffLed();
            }
        } catch (Exception ex) {
            log.error("Error occurred during scan", ex);
            errorMessage = "Error occurred during scan";
            eventListener.onScanComplete(false, errorMessage, ConstantDefs.UNKNOWN_FINGER);
            log.error(ex.getMessage(), ex);
        }


    }

    private void StartOperation() {
        fpDevice.SetCallback(this);
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                StartScanning();
            }
        };
        timer.schedule(timerTask, 100);
    }

    private void terminateAfterTimeOut(long delay){
        Timer mTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (!isScanCompleted){
                    fpDevice.Stop();
                    eventListener.onScanComplete(false,ConstantDefs.CAPTURE_TIME_ELAPSED, ConstantDefs.UNKNOWN_FINGER);
                }
                isScanCompleted = false;
            }
        };
        mTimer.schedule(timerTask, delay);
    }

    public void setnSequence(byte finger) {
        nSequence = finger;
    }

    public void runCapture(final byte finger, long timeOut) {

        nSequence = finger;
        if (nSequence == ConstantDefs.FT_2_THUMBS) {
            m_nScanType = ConstantDefs.DEVICE_SCAN_TYPE_2THUMBS;
        } else if (nSequence == ConstantDefs.FT_LEFT_4_FINGERS || nSequence == ConstantDefs.FT_RIGHT_4_FINGERS) {
            m_nScanType = ConstantDefs.DEVICE_SCAN_TYPE_SLAPS;
        } else if (nSequence > ConstantDefs.FT_RIGHT_4_FINGERS) {
            m_nScanType = ConstantDefs.DEVICE_SCAN_TYPE_FLAT_FINGER;
        }

        StartOperation();
        terminateAfterTimeOut(timeOut);
    }

    public void runSingleCapture(byte finger, long timeOut) {
        capture(finger, timeOut);
    }

    public void runSingleCapture(long timeOut) {
        capture(null, timeOut);
    }

    private void capture(Byte finger, long timeOut){
        refresh();
        nSequence = ConstantDefs.FT_PLAIN_LEFT_THUMB;
        m_nScanType = ConstantDefs.DEVICE_SCAN_TYPE_FLAT_FINGER;
        if (finger == null){
            fpDevice.setFingerToCapture(ConstantDefs.FT_PLAIN_FINGER);
        }else {
            fpDevice.setFingerToCapture(finger);
        }

        StartOperation();
        terminateAfterTimeOut(timeOut);
    }

    public void scanCompleteEventHandler(IScanCompleteEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void TurnOffLed() {
        fpDevice.TurnOffLed();
    }

    public boolean CanSlaps() {
        return fpDevice.CanSlaps();
    }

    public void Stop() {
        fpDevice.Stop();
    }

    public void ShowAcceptedImage(byte finger, JLabel label) {
        this.showLabel = label;
        fpDevice.ShowAcceptedImage(finger, showLabel);
    }

    public void setM_nScanType(byte m_nScanType) {
        this.m_nScanType = m_nScanType;
    }

    public void reset() {
        setFingerImage(null);
        leftHandImages = new ArrayList<BufferedImage>();
        rightHandImages = new ArrayList<BufferedImage>();
        twoThumbsImages = new ArrayList<BufferedImage>();
        fpDevice.setFingerToCapture((byte) -1);
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    public String getType() {
        return type;
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
