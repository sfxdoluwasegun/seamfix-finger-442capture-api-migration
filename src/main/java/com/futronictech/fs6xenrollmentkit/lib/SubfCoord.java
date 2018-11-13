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
public class SubfCoord {
    public int xs, ys;
    public int ws, hs;
    public int err;
    public int nfiq;
    public int qfutr;
    
    public SubfCoord(){
        xs = ys= ws = hs = nfiq = qfutr = 0;
        err = 1;
    }
}
