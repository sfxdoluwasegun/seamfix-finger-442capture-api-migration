/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.futronictech.fs6xenrollmentkit.lib;

import com.futronictech.fs6xenrollmentkit.ui.MyIcon;
import javax.swing.JLabel;

/**
 *
 * @author slyeung
 */
public class RollScanThread implements Runnable {
    private Thread m_WorkedThread;
    private static Object m_SyncRoot = null;
    private static FPDevice m_devFP = null;
    private static JLabel m_LabelShowArea = null;
    private MyIcon m_FingerPrintImage = null;

    public RollScanThread(FPDevice devFP)
    {
        m_devFP = devFP;
    }
    
    public boolean StartRoll(Object syncRoot, JLabel lableShowArea)
    {        
        m_SyncRoot = syncRoot;
        m_LabelShowArea = lableShowArea;
        if( !InitDevice() )
            return false;
        m_WorkedThread = new Thread( this, "RollScanThread" );
        m_WorkedThread.start();
        return true;
    }

    public boolean InitDevice()
    {
        //m_devFP = new FPDevice();
        if( !m_devFP.Open() )
            return false;
        int[] width = new int[1];
        int[] height = new int[1];
        width[0] = height[0] = 0;
        if( !m_devFP.PrepareRolling(width, height))
            return false;
        
        int width2 = m_LabelShowArea.getWidth();
        int height2 = m_LabelShowArea.getHeight();
        m_FingerPrintImage = new MyIcon((width[0] > width2 ? width2 : width[0]), (height[0]>height2 ? height2 : height[0]));
        m_LabelShowArea.setIcon( m_FingerPrintImage );
        m_devFP.SetImageHandler(m_FingerPrintImage, m_LabelShowArea);       
        return true;
    }
    
    public void run() {
        try
        {
            synchronized( m_SyncRoot )
            {
                RollScanProcess();
            }
        }
        finally
        {
            m_devFP.Close();
        }       
    }
    
    private void RollScanProcess()
    {
        m_devFP.DoRoll();
    }
}
