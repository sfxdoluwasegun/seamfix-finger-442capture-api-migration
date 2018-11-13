/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.futronictech.fs6xenrollmentkit.ui;

import com.futronictech.fs6xenrollmentkit.lib.FTRSCAN_ROLL_FRAME_PARAMETERS;
import com.futronictech.fs6xenrollmentkit.lib.SubfCoord;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.*;
import javax.imageio.ImageIO;
import javax.swing.Icon;

/**
 *
 * @author slyeung
 */
public class MyIcon implements Icon
{
    private Object m_objSync;
    private int m_IconWidth;
    private int m_IconHeight;
    private int m_ImageWidth;
    private int m_ImageHeight;
    private Image m_Image;
    private Color m_ColorTextMiddle;
    private String m_TextMiddle;
    private int m_nNfiq = 0;
    private double m_dAngle = 0.0;
    private FTRSCAN_ROLL_FRAME_PARAMETERS m_RollFrameParameters = null;
    private boolean m_bPreview;
    private SubfCoord[] m_SubfCoord = null;
    private float m_ScaleWidth = 1.0f;
    private float m_ScaleHeight = 1.0f;
    private int m_ShowIconWidth;
    private int m_ShowIconHeight;
    private int m_OffsetX;
    private int m_OffsetY;
    
    public MyIcon()
    {
        m_Image = null;
        m_IconWidth = 800;
        m_IconHeight = 750;
        m_objSync = new Object();
    }

    public MyIcon(int width, int height)
    {
        m_Image = null;
        m_IconWidth = width;
        m_IconHeight = height;
        m_objSync = new Object();
    }

    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        synchronized(m_objSync)
        {
            if( m_Image != null )
            {
                CalculateImageCenter();
                g.drawImage( m_Image, x+m_OffsetX, y+m_OffsetY, m_ShowIconWidth, m_ShowIconHeight, null );
            }
            else
            {
                g.setColor(Color.white);
                g.fillRect( x, y, getIconWidth(), getIconHeight() );      
            }
            if( m_bPreview )
            {
                g.setColor(Color.green);
                g.setFont(new Font("TimesRoman", Font.BOLD, 16)); 
                g.drawString("Preview", x+5, y+15);
            }
            if( m_TextMiddle != null )
            {
                g.setColor(m_ColorTextMiddle);
                g.setFont(new Font("TimesRoman", Font.BOLD, 25)); 
                g.drawString(m_TextMiddle, x+(m_IconWidth/4), y+(m_IconHeight/2));
            }
            if( m_nNfiq > 0 )
            {
                DrawNFIQString(g, x, y);
            }
            if( m_RollFrameParameters != null )
            {
                if( m_RollFrameParameters.dwFrameIndex > 0 )
                {
                    g.setColor(Color.red);
                    String strRollingInfo = String.format("Time: %dms, Dosage: %d, Index: %d, Contrast: %d",
                                                        m_RollFrameParameters.dwFrameTimeMs, m_RollFrameParameters.dwFrameDose, m_RollFrameParameters.dwFrameIndex, m_RollFrameParameters.dwFrameContrast / 256 );
                    g.drawString(strRollingInfo, x+5, y+m_IconHeight-20);
                    int nStep = 800/128;
                    int left, top;
                    int height = 10;
                    int width = m_RollFrameParameters.dwFrameIndex*nStep;
                    if( m_RollFrameParameters.dwDirection == 1 )
                    {
                        left = x;
                        top = y;
                    }
                    else
                    {
                        left = 800+x-m_RollFrameParameters.dwFrameIndex*nStep;
                        top = y;
                    }			
                    g.fillRect(left, top, width, height);
                }
            }        
            if( m_SubfCoord != null )
            {
                DrawSubfCoordString(g, x, y);
            }
        }
    }
    
    private void CalculateImageCenter()
    {
        double dW, dH, dM;
        m_OffsetX = m_OffsetY = 0;
        if(  m_IconWidth >= m_ImageWidth )
            dW = 1.0;
        else
            dW = (double)m_ImageWidth / (double)m_IconWidth;
        if( m_IconHeight >= m_ImageHeight )
            dH = 1.0;
        else
            dH = (double)m_ImageHeight / (double)m_IconHeight;
        dM = ( dW > dH ? dW : dH );
        m_ShowIconHeight = (int) (m_ImageHeight / dM);
        m_ShowIconWidth = (int) (m_ImageWidth / dM);
        if( m_IconWidth > m_ShowIconWidth )
        {
            m_OffsetX = (m_IconWidth - m_ShowIconWidth) / 2;
        }
        if( m_IconHeight > m_ShowIconHeight )
        {
            m_OffsetY = (m_IconHeight - m_ShowIconHeight) / 2;
        }
    }
    
    private void DrawNFIQString(Graphics g, int x, int y)
    {
        if(m_nNfiq<4)
            g.setColor(Color.green);
        else if(m_nNfiq == 4 )
            g.setColor(Color.yellow);
        else if(m_nNfiq == 5)
            g.setColor(Color.red);
        int xs = x + m_IconWidth - 15;
        int ys = y + m_IconHeight - 5;                   
        g.fillRect(xs-5, ys-15, 20, 20);
        g.setColor(Color.black);
        g.setFont(new Font("TimesRoman", Font.BOLD, 12)); 
        g.drawString(String.format("%d", m_nNfiq),xs,ys);
    }
   
    private void DrawSubfCoordString(Graphics g, int x, int y)
    {
        int nCount = m_SubfCoord.length;
        int nNfiq;
        for( int i=0; i<nCount; i++)
        {
            if(m_SubfCoord[i].err == 0)
            {
                if(m_bPreview)
                    nNfiq = m_SubfCoord[i].qfutr;
                else
                    nNfiq = m_SubfCoord[i].nfiq;
                if(nNfiq<4)
                    g.setColor(Color.green);
                else if(nNfiq == 4 )
                    g.setColor(Color.yellow);
                else if(nNfiq == 5)
                    g.setColor(Color.red);
                int[] xs, ys;
                xs = new int[1];
                ys = new int[1];
                if( m_dAngle != 0.0 )
                {
                    xs[0] = m_SubfCoord[i].xs;
                    ys[0] = m_SubfCoord[i].ys;
                    ReCalculateCoordinates(m_SubfCoord[i].ws, m_SubfCoord[i].hs, m_dAngle, xs, ys);
                    xs[0] = x + (int)(xs[0] / m_ScaleWidth) - 15;
                    ys[0] = y + (int)(ys[0] / m_ScaleHeight) - 5;                   
                }
                else
                {
                    xs[0] = x + (int)( (m_SubfCoord[i].xs + m_SubfCoord[i].ws/2) / m_ScaleWidth ) - 15;
                    ys[0] = y + (int)( (m_SubfCoord[i].ys + m_SubfCoord[i].hs/2) / m_ScaleHeight ) - 5;                   
                }                
                g.fillRect(xs[0]-5, ys[0]-15, 20, 20);
                g.setColor(Color.black);
                g.setFont(new Font("TimesRoman", Font.BOLD, 12)); 
                g.drawString(String.format("%d", nNfiq),xs[0],ys[0]);
            }
        }
    }
    
    private void ReCalculateCoordinates( int nWidth, int nHeight, double dAngle, int[] nX, int[] nY )
    {
	//1. calculate the radian
	double alpha = atan( (double)( ( nWidth/2.0 ) / (nHeight/2.0) ));
	//2. calculate the raidus
	double radius = (nWidth/2) / (sin( alpha ) );
	//3. calculate the x, y
	double y = radius * cos( alpha - dAngle );
	double x = radius * sin( alpha - dAngle );
	nX[0] = nX[0] + (int)x;
	nY[0] = nY[0] + (int)y;
    }
    
    public int getIconWidth()
    {
        return m_IconWidth;
    }
    
    public void setIconWidth( int width)
    {
         m_IconWidth = width;
    }

    public int getIconHeight()
    {
        return m_IconHeight;
    }

    public void setIconHeight(int height)
    {
        m_IconHeight = height;
    }

    public boolean LoadImage( String path )
    {
        boolean bRetCode = false;
        Image newImg;
        try
        {
            File f = new File( path );
            newImg = ImageIO.read( f );
            bRetCode = true;
            setImage( newImg );
        }
        catch( IOException e )
        {
        }

        return bRetCode;
    }

    public void setImage( Image Img )
    {
        if( Img != null )
        {
            m_ImageWidth = Img.getWidth(null);
            m_ImageHeight = Img.getHeight(null);
            m_Image = Img.getScaledInstance( getIconWidth(), getIconHeight(), Image.SCALE_FAST);
        }
        else
            m_Image = null;
    }

    public void setText( Color colorTextMiddle, String strTextMiddle )
    {
        m_ColorTextMiddle = colorTextMiddle;
        m_TextMiddle = strTextMiddle;
    }

    public void setNfiq( int nNfiq )
    {
        m_nNfiq = nNfiq;
    }
    
    public void setRollingParameters(FTRSCAN_ROLL_FRAME_PARAMETERS FrameParameters)
    {
        m_RollFrameParameters = FrameParameters;
    }
    
    public void setSubfCoord(SubfCoord[] pSubfCoord, double dAngle)
    {
        m_SubfCoord = pSubfCoord;
        m_dAngle = dAngle;
    }
    
    public void setPreviewMode(boolean bPreview)
    {
        m_bPreview = bPreview;
    }
    
    public void setImageSize(int width, int height)
    {
        m_ScaleWidth = (float) (width / m_IconWidth);
        m_ScaleHeight = (float) (height / m_IconHeight);        
    }
    
}