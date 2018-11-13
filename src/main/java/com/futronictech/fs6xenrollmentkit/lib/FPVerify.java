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
public class FPVerify {
    public static final int FINGER_TYPE_NUMBER	 = 23;

    public static int m_nThreshold = 25;    
    private xytq_struct[] m_pTemplates = null;
    
    private class VERIFIED_RECORD
    {
        public int nIndex;
        public int nScore;
        public VERIFIED_RECORD()
        {
            nIndex = nScore = 0;
        }
    };

    public FPVerify()
    {
        m_nThreshold = 25;
        m_pTemplates = new xytq_struct[FINGER_TYPE_NUMBER];
        Init();
    }
    
    public void Init()
    {
        for( int i=0; i<FINGER_TYPE_NUMBER; i++ )
        {
           m_pTemplates[i] = null;
        }       
    }

    public boolean Enroll( FtrNativeLibrary m_libNative, byte[] pImage, int nWidth, int nHeight, int nIndex )
    {
        if( nIndex <0 || nIndex > (FINGER_TYPE_NUMBER-1) )	
            return false;
        m_pTemplates[nIndex] = new xytq_struct();
        return m_libNative.NbisGetMinutiaeXYTQ( m_pTemplates[nIndex], pImage, nWidth, nHeight );
    }


    public boolean Identify( FtrNativeLibrary m_libNative, byte[] pImage2, int nWidth2, int nHeight2, int[] nIndex, int[] nScore )
    {
	xytq_struct pstruct = new xytq_struct();
	boolean bRet = m_libNative.NbisGetMinutiaeXYTQ( pstruct, pImage2, nWidth2, nHeight2 );
	if( !bRet )
        {
            pstruct = null;
            return false;
        }        
	long[] pHandle = new long[1];
	bRet =  m_libNative.NbisBozorth3SetBaseProbe( pHandle, pstruct );
	if( !bRet )
        {
            pstruct = null;
            return false;
        }
        VERIFIED_RECORD[] recVerify = new VERIFIED_RECORD[FINGER_TYPE_NUMBER];
	int i;
	int ms[] = new int[1];
	int nCount = 0;
	int nMaxScore = 0;

	for( i=0; i<FINGER_TYPE_NUMBER; i++ )
            recVerify[i] = new VERIFIED_RECORD();
	
	for( i=0; i<FINGER_TYPE_NUMBER; i++ )
	{
            if( m_pTemplates[i] == null )
                continue;
            ms[0] = 0;
            bRet = m_libNative.NbisBozorth3Identify( pHandle[0], m_pTemplates[i], ms );
            if( !bRet )
            {
                //MessageBox(NULL, _T("Failed to call ftrBozorth3Identify!"), _T("Error"), MB_OK|MB_ICONSTOP);
                m_libNative.NbisBozorth3ReleaseBaseProbe( pHandle[0] );	
                pstruct = null;
                return false;
            }
            if( nMaxScore < ms[0] )
                nMaxScore = ms[0];
            if( ms[0] > m_nThreshold )
            {
                recVerify[nCount].nIndex = i;
                recVerify[nCount].nScore = ms[0];
                nCount ++;
            }
	}	
	m_libNative.NbisBozorth3ReleaseBaseProbe( pHandle[0] );	
	pstruct = null;

	if( nCount == 0 )
	{
            nScore[0] = nMaxScore;
            return false;
	}

	nScore[0] = recVerify[0].nScore;
	nIndex[0] = recVerify[0].nIndex;
	for( i=1; i<nCount; i++ )
	{
            if( recVerify[i].nScore > recVerify[i-1].nScore )
            {
                nScore[0] = recVerify[i].nScore;
                nIndex[0] = recVerify[i].nIndex;
            }
	}
	return true;
    }

    public boolean Verify(FtrNativeLibrary m_libNative, byte[] pImg1, int nW1, int nH1, byte[] pImg2, int nW2, int nH2, int[] nScore)
    {
	xytq_struct pstruct = new xytq_struct();
	boolean bRet = m_libNative.NbisGetMinutiaeXYTQ( pstruct, pImg1, nW1, nH1 );
	if( !bRet )
	{
            pstruct = null;
            return false;
	}
	xytq_struct gstruct = new xytq_struct();
	bRet = m_libNative.NbisGetMinutiaeXYTQ( gstruct, pImg2, nW2, nH2 );
	if( !bRet )
	{
            pstruct = null;
            gstruct = null;
            return false;
	}
	int[] ms = new int[1];
        ms[0] = 0;
	bRet = m_libNative.NbisBozorth3Verify( pstruct, gstruct, ms );
	if( bRet )
            if( ms[0] < m_nThreshold )
                bRet = false;	//Verify failed
	gstruct = null;
	pstruct = null;
	nScore[0] = ms[0];
	return bRet;
    }
}