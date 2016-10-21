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
    int start;
    int end;
    String[] ioNames;
//    ArrayList<Input> inputs;
//    ArrayList<Output> outputs;
    ArrayList<Part> parts;
    ArrayList<Wire> wires;
    
    public Module(String name,int start, int end, String[] ioNames){
        this.name = name;
        this.ioNames = ioNames;
        this.start = start;
        this.end = end;
        parts = new ArrayList<>();
        wires = new ArrayList<>();
    }

    /**
     * Retrieve a Wire of a given name
     * @param name name of the Wire to get
     * @return the Wire
     */
    public Wire getWire(String name){
        for(Wire wire : wires) {
            if (wire.name.equals(name))
                return wire;
        }
        return null;
    }
    /**
     * Add an input to the module
     * @param name name of the input
     */
    public void addInput(String name, int size){
        wires.add(new Wire(name,size));
        wires.get(wires.size()-1).addPort(new Port(name,(byte)1));
    }
    /**
     * Add an output to the module
     * @param name name of the output
     */
    public void addOutput(String name, int size){
        wires.add(new Wire(name,size));
        wires.get(wires.size()-1).addPort(new Port(name,(byte)-1));
    }
    /**
     * Add an Wire to the module
     * @param name name of the Wire
     * @return a reference to the new Wire
     */
    public Wire addWire(String name, int size){
        wires.add(new Wire(name,size));
        return wires.get(wires.size()-1);
    }
    
    /**
     * Add inputs to the module
     * @param names list of names, separated by commas
     */
    public void addInputs(String[] names, int size){
        int i;
        for(i=0;i<names.length;i++){
            wires.add(new Wire(names[i].replaceAll("\\s","").replaceAll(";",""),size));
            wires.get(wires.size()-1).addPort(new Port(names[i].replaceAll("\\s","").replaceAll(";",""),(byte)1));
        }
    }
    /**
     * Add outputs to the module
     * @param names list of names, separated by commas
     */
    public void addOutputs(String[] names, int size){
        int i;
        for(i=0;i<names.length;i++){
            wires.add(new Wire(names[i].replaceAll("\\s","").replaceAll(";",""),size));
            wires.get(wires.size()-1).addPort(new Port(names[i].replaceAll("\\s","").replaceAll(";",""),(byte)-1));
        }
    }
    /**
     * Add Wires to the module
     * @param names list of names, separated by commas
     */
    public void addWires(String[] names, int size){
        int i;
        for(i=0;i<names.length;i++)
            wires.add(new Wire(names[i].replaceAll("\\s","").replaceAll(";",""),size));
    }
    /**
     * Add Registers to the module
     * @param names list of names, separated by commas
     * @param size bit-width of the Registers
     */
    public void addRegs(String[] names, int size){
        int i;
        for(i=0;i<names.length;i++)
            wires.add(new Reg(names[i].replaceAll("\\s","").replaceAll(";",""),size));
    }
}