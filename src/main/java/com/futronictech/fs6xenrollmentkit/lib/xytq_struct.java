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
public class xytq_struct {
    public static final int MAX_BOZORTH_MINUTIAE = 200;
    public static final int MAX_FILE_MINUTIAE = 1000;

    public int nrows;
    public int[] xcol = null;
    public int[] ycol = null;
    public int[] thetacol = null;
    public int[] qcol = null;
    
    public xytq_struct()
    {
        nrows = 0;
        xcol = new int[ MAX_FILE_MINUTIAE ];
        ycol = new int[ MAX_FILE_MINUTIAE ];
        thetacol = new int[ MAX_FILE_MINUTIAE ];
        qcol = new int[ MAX_FILE_MINUTIAE ];
    }
}
