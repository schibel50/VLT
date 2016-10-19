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
}
