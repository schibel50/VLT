/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vlt;

/**
 *
 * @author Ryan
 */
public class TSB_VLT extends Part{
    public TSB_VLT(String name){
        super(name);
        ports.add(new Port("A1",this));
        ports.add(new Port("Y1",this));
        ports.add(new Port("G1",this));
    }
}
