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
public class DFF_VLT extends Part{
    
    public DFF_VLT(String name){
        super(name);
        ports.add(new Port("S'",this));
        ports.add(new Port("R'",this));
        ports.add(new Port("Ck",this));
        ports.add(new Port("D",this));
        ports.add(new Port("Q",this));
        ports.add(new Port("Q'",this));
    }

}