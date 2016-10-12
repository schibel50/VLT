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
public class Adder extends Part{
    
    public Adder(String name){
        super(name);
        ports.add(new Port("a",this));
        ports.add(new Port("b",this));
        ports.add(new Port("cin",this));
        ports.add(new Port("y",this));
        ports.add(new Port("cout",this));
    }
}
