/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vlt;

/**
 *
 * @author Parker
 */
public class DFF extends Part{
    
    public DFF(String name){
        super(name);
        ports.add(new Port("D",this));
        ports.add(new Port("clk",this));
        ports.add(new Port("Q",this));
        ports.add(new Port("QN",this));
        ports.add(new Port("Pre",this));
        ports.add(new Port("Clear",this));
    }

}