/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.futronictech.fs6xenrollmentkit.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author slyeung
 */
public class FPDataInterchange {

    public static final int FIR_STD_ANSI = 1;
    public static final int FIR_STD_ISO = 2;

    private long[] m_pFir = new long[1];
    private int[] m_nSize = new int[1];
    private byte[] m_pRecord = null;
    private FtrNativeLibrary m_libNative = null;

    public FPDataInterchange()
    {
        m_pFir[0] = 0;
        m_nSize[0] = 0;
        m_pRecord = null;
        m_libNative = null;
    }
    
    public boolean Initialize(FtrNativeLibrary libNative, byte nFirStd, byte nDeviceID)
    {
        m_nSize[0]=0;
        m_libNative = libNative;
	if( nFirStd != FIR_STD_ANSI && nFirStd != FIR_STD_ISO)
            return false;
	if( m_pFir[0] != 0 )
	{
            m_libNative.BiomdiFreeFIR( m_pFir[0] );
            m_pFir[0] = 0;
	}
	return m_libNative.BiomdiNewFIR(m_pFir, nFirStd, nDeviceID);
    }

    public void Terminate()
    {
	if( m_pFir[0] != 0)
	{
            m_libNative.BiomdiFreeFIR( m_pFir[0] );
            m_pFir[0] = 0;
	}
	m_pRecord = null;
    }

    public boolean AddImage(byte[] pImage, int nImageSize, int nWidth, int nHeight, byte nFingerPosition, byte nNFIQ, byte nImpressionType)
    {
	if( m_pFir[0] == 0 || pImage == null || nWidth <=0 || nHeight <= 0 )
            return false;
	boolean bRet = m_libNative.BiomdiFIRAddImage( m_pFir[0], pImage, nImageSize, nWidth, nHeight, nFingerPosition, nNFIQ, nImpressionType );
        return bRet;
    }

    public boolean SaveRecord( String strFileName )
    {
	if( m_pFir[0] == 0 )
            return false;
        m_nSize[0] = 0;
	if( !m_libNative.BiomdiGetFIRDataSize( m_pFir[0], m_nSize ) )
            return false;
	m_pRecord = new byte[m_nSize[0]];
	if( m_libNative.BiomdiGetFIRData(m_pFir[0], m_nSize[0], m_pRecord) )
	{
            //Save to file
            FileOutputStream fs = null;
            File f = null;
            try
            {
                f = new File( strFileName );
                fs = new FileOutputStream( f );
                fs.write(m_pRecord, 0, (int)m_nSize[0]);
                fs.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FPDataInterchange.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(FPDataInterchange.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        }
        return false;
    }
}
