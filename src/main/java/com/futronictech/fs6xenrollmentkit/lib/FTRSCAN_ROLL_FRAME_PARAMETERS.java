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
public class FTRSCAN_ROLL_FRAME_PARAMETERS {
    public int dwSize;
    public int dwFlags;
    public int dwStatus;
    public int dwRollingResult;
    public int dwDirection;
    public int dwFrameIndex;
    public int dwFrameDose;
    public int dwFrameContrast;
    public int dwFrameTimeMs;
    
    public FTRSCAN_ROLL_FRAME_PARAMETERS(){
        dwSize = dwFlags = dwStatus = dwRollingResult = dwDirection = dwFrameIndex = dwFrameDose = dwFrameContrast = dwFrameTimeMs = 0;
    }
}
