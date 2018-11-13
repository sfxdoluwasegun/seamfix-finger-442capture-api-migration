package com.futronictech.fs6xenrollmentkit.interfaces;

public interface IScanCompleteEventListener {
    void onScanComplete(boolean isValid, String message, byte finger);
}
