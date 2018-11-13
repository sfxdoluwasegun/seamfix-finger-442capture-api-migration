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
public class ACCEPTED_IMAGE {    
    
    public static final byte IMPRESSION_TYPE_PLAIN = 0;     //Live	-scan plain
    public static final byte IMPRESSION_TYPE_ROLLED = 1;    //Live-scan rolled
    
    public class FINGER_AMP
    {
	public byte FingerId;
	public byte AMPCode;	//0, 1- Amputation XX, 2- Unable to print(e.g. Bandaged) UP
        public FINGER_AMP()
        {
            FingerId = AMPCode = 0;
        }
    };
    
    public byte nFingerType;
    public int nImageWidth;
    public int nImageHeight;
    public int nNFIQ;
    public int nAnsiFingerPosition;
    public int nNumberSegments;
    public int nNumberAmp;
    public FINGER_AMP[] fAmp;
    public byte it;
    public byte[] pAcceptedImage;
    
    public ACCEPTED_IMAGE()
    {
        pAcceptedImage = null;
        nImageHeight = nImageWidth = 0;
        nNFIQ = nAnsiFingerPosition = 0;
        nNumberAmp = nNumberSegments = 0;
        it = 0;
        fAmp = new FINGER_AMP[4];
        for(int i=0; i<4; i++)
            fAmp[i] = new FINGER_AMP();
    }
    
}
