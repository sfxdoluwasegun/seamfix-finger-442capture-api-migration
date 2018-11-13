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
public class SegmParam {
    public int nParamFing;
    public int nParamAngle;
    public int nParamNfiq;
    public int nParamFixedSize;
    public int nWidthSubf;
    public int nHeightSubf;
	public int nHandType;
    public long dwTimeScan;
    public double dAngle;
    public int nErr;
	
    public SegmParam(){
        nParamFing = nParamAngle = nParamNfiq = nParamFixedSize = nWidthSubf = nHeightSubf = nHandType = 0;
        dwTimeScan = 0;
        dAngle = 0.0;
		nErr = 0;
    }
}
