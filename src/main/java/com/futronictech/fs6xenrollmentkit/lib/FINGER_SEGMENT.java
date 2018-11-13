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
public class FINGER_SEGMENT {
    public short Left;
    public short Right;
    public short Top;
    public short Bottom;
    public byte FingerId;
    public byte NFIQ;
    
    public FINGER_SEGMENT()
    {
        Left = Right = Top = Bottom = 0;
        FingerId = NFIQ = 0;
    }
}
