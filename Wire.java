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
public class Wire {
    String name;
    ArrayList<Port> ports;
    
    public Wire(String name){
        this.name = name;
        ports = new ArrayList<>();
    }
    
    public void addPort(Port port){
        ports.add(port);
    }
    
    public void copy(Wire wire){
        for(Port port : wire.ports){
            this.ports.add(port);
        }
    }
}