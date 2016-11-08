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
public class Buffer extends Part{
    
    public Buffer(String name){
        super(name);
        ports.add(new Port("in",this));
        ports.add(new Port("out",this));
    }
}