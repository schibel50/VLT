/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vlt;

import java.util.ArrayList;

/**
 *
 * @author Ryan
 */
public class Part {
    
    String name;
    ArrayList<Port> ports;
    boolean activeLow;
    
    public Part(String name){
        this.name=name;
        ports = new ArrayList<>();
        activeLow = false;
    }
    
    public void copy(Part part){
        this.name = part.name;
        for(int i = 0; i < part.ports.size(); i++)
            this.ports.add(part.ports.get(i));
        this.activeLow = part.activeLow;
    }
}
