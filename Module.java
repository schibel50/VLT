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
public class Module {
    
    String name;
//    ArrayList<Input> inputs;
//    ArrayList<Output> outputs;
    ArrayList<Part> parts;
    ArrayList<Wire> wires;
    
    public Module(String name){
        this.name = name;
    }

    /**
     * Set a Wire's connection logic
     * @param name name of the Wire to set
     */
    public void setWire(String name){
        
    }
    /**
     * Add an input to the module
     * @param name name of the input
     */
    public void addInput(String name){
        wires.add(new Wire(name));
        wires.get(wires.size()-1).addPort(new Port(name,(byte)1));
    }
    /**
     * Add an output to the module
     * @param name name of the output
     */
    public void addOutput(String name){
        wires.add(new Wire(name));
        wires.get(wires.size()-1).addPort(new Port(name,(byte)-1));
    }
    /**
     * Add an Wire to the module
     * @param name name of the Wire
     * @return a reference to the new Wire
     */
    public Wire addWire(String name){
        wires.add(new Wire(name));
        return wires.get(wires.size()-1);
    }
    
    /**
     * Add inputs to the module
     * @param names list of names, separated by commas
     */
    public void addInputs(String[] names){
        int i;
        for(i=0;i<names.length;i++){
            wires.add(new Wire(names[i].replaceAll("\\s","").replaceAll(";","")));
            wires.get(wires.size()-1).addPort(new Port(names[i].replaceAll("\\s","").replaceAll(";",""),(byte)1));
        }
    }
    /**
     * Add outputs to the module
     * @param names list of names, separated by commas
     */
    public void addOutputs(String[] names){
        int i;
        for(i=0;i<names.length;i++){
            wires.add(new Wire(names[i].replaceAll("\\s","").replaceAll(";","")));
            wires.get(wires.size()-1).addPort(new Port(names[i].replaceAll("\\s","").replaceAll(";",""),(byte)-1));
        }
    }
    /**
     * Add Wires to the module
     * @param names list of names, separated by commas
     */
    public void addWires(String[] names){
        int i;
        for(i=0;i<names.length;i++)
            wires.add(new Wire(names[i].replaceAll("\\s","").replaceAll(";","")));
    }
}
