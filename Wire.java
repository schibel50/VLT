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
    int size;
    
    public Wire(String name,int size){
        this.name = name;
        this.size = size;
        ports = new ArrayList<>();
    }
    
    public Wire(Wire wire){
        this.name = wire.name;
        this.size = wire.size;
        this.ports = new ArrayList<>();
        for(Port port : wire.ports){
            Port temp = new Port(port);
            this.ports.add(temp);
        }
    }
    
    public void addPort(Port port){
        ports.add(port);
    }
    
    public void copy(Wire wire){
        this.name=wire.name;
        this.size=wire.size;
        for(Port port : wire.ports){
            this.ports.add(port);
        }
    }
}