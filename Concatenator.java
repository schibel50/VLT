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
public class Concatenator extends Part{
    
    public Concatenator(String name, int numIns){
        super(name);
        for(int i = 0; i < numIns; i++)
            ports.add(new Port("in-"+i, this));
        ports.add(new Port("out",this));
    }
}
