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
public class NAND4_VLT extends Part{
    public NAND4_VLT(String name){
        super(name);
        ports.add(new Port("A",this));
        ports.add(new Port("B",this));
        ports.add(new Port("C",this));
        ports.add(new Port("D",this));
        ports.add(new Port("Y",this));
    }
}
