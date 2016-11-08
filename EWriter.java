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
public class EWriter {
    public Module module;
    
    public int numWires;
    public int numAdders;
    
    public ArrayList<String> top,IOs,instances,nets;
    
    public EWriter(Module module){
        this.module = module;
        this.IOs = new ArrayList<>();
        this.instances = new ArrayList<>();
        this.nets = new ArrayList<>();
        this.top = new ArrayList<>();
    }
    
    public void write(){
        
        int h=0,i=0,j=0;
        
        //add the stuff at the top of the EDIF file
        top.add("(edif BB_edif");
        top.add("\t(edifVersion 2 0 0)");
        top.add("\t(edifLevel 0)");
        top.add("\t(keywordMap (keywordLevel 0))");
        top.add("\t(library beigebag");
        top.add("\t\t(edifLevel 0");
        top.add("\t\t(technology (numberDefinition))");
        top.add("\t\t(cell (cellType GENERIC)");
        top.add("\t\t\t(view viewnameddefault (viewtype NETLIST)");
        top.add("\t\t\t\t(interface");

//        NOT SURE WHAT TO DO HERE YET
//        while(h<module.parts.size()){
//            top.add("\t\t\t\t\t(port [name] (direction [direction]))");
//        }
        
        top.add("\t\t\t\t)))");
        top.add("\t)");
        top.add("\t(library DESIGNS (ediflevel 0) (technology (numberDefinition))");
        top.add("\t\t(cell topLevel (cellType GENERIC)");
        top.add("\t\t\t(view view_1 (viewType NETLIST)");
        
        instances.add("\t\t\t\t(contents");
        
        // add instances
        while(i<module.parts.size()){
            Part temp = module.parts.get(i);
            instances.add("\t\t\t\t\t(instance " + temp.name + " (viewRef viewnamedefault");
            instances.add("\t\t\t\t\t\t(cellRef  (libraryRef beigebag))");
            instances.add("\t\t\t\t\t\t))");
            i++;
        }
        
        IOs.add("\t\t\t\t(interface");
        
        // add wires (as nets)
        while(j<module.wires.size()){
            Wire temp = module.wires.get(j);
            nets.add("\t\t\t\t\t(net Net" + j);
            nets.add("\t\t\t\t\t\t(joined");
            int k=0;
            while(k<temp.ports.size()){
                if(temp.ports.get(k).IO==1){ //port is an input
                    IOs.add("\t\t\t\t\t(port " + temp.ports.get(k).name + "(direction input))");
                    nets.add("\t\t\t\t\t\t\t(portRef " + temp.ports.get(k).name);
                }else if(temp.ports.get(k).IO==-1){ //port is an output
                    IOs.add("\t\t\t\t\t(port " + temp.ports.get(k).name + "(direction output))");
                    nets.add("\t\t\t\t\t\t\t(portRef " + temp.ports.get(k).name);
                }else
                    nets.add("\t\t\t\t\t\t\t(portRef " + temp.ports.get(k).name + " (instanceRef " + temp.ports.get(k).part.name + ")");
                k++;
            }
            nets.add("\t\t\t\t\t\t))");
            j++;
        }
        
        IOs.add("\t\t\t\t)");
        
        //add in stuff at the bottom
        nets.add("\t\t\t\t))))");
        nets.add("(design root");
        nets.add("\t\t(cellref toplevel");
        nets.add("\t\t\t(libraryRef DESIGNS))))");
    }
}
