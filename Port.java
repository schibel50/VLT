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
public class Port {
    
    String name;
    Part part; //if part is null, this is an input/output to the whole circuit/module
    byte IO;//if -1, output; if +1, input
    
    public Port(String name,Part part){
        this.name = name;
        this.part = part;
        this.IO = 0;
    }
    
    public Port(String name, byte IO){
        this.name = name;
        this.IO = IO;
        this.part=null;
    }
}
