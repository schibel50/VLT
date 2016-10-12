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
public class Compiler {
    
    public ArrayList<String> code;
    public Module module;
    public String[] operators = {"+","-"};
    
    public int numWires;
    public int numAdders;
    
    public Compiler(ArrayList<String> code){
        this.code = code;
        numWires=0;
        numAdders=0;
    }//ctor
    
     /**
     * compile method converts the input .v file to the output EDIF layout
     */
    public void compile(){
        //first sweep: scan for any 'modules'
        char currentChar = ' ';
        int i,j;
        module = new Module("NAME");
        for(i=0;i<code.size();i++){
//                if(code[i].substring(0,8).equals("module "))
//                    module = new Module();
                
                //add inputs to the module
                if(code.get(i).substring(0,5).equals("input")){
                    if(code.get(i).substring(5,6).equals(" ")){
                        module.addInputs(code.get(i).substring(6).split("\\,"));
                    }
                }
                //add outputs to the module
                if(code.get(i).substring(0,6).equals("output")){
                    if(code.get(i).substring(6,7).equals(" ")){
                        module.addOutputs(code.get(i).substring(6).split("\\,"));
                    }
                }
                //add wires to the module
                if(code.get(i).substring(0,4).equals("wire")){
                    if(code.get(i).substring(4,5).equals(" ")){
                        module.addWires(code.get(i).substring(6).split("\\,"));
                    }
                }
                //assign wires
                if(code.get(i).substring(0,6).equals("assign")){
                    if(code.get(i).substring(6,7).equals(" ")){
                        assign(code.get(i).substring(7));
                    }
                }
                
////////////////////////////////////////////////////////////////////////////////////////////////                
//            for(j=0;currentChar!='\n';j++){
//                if(code.get(i).charAt(j)=='i'){ //add inputs
//                    if(code.get(i).substring(j,j+5).equals("input")){
//                        j+=5;
//                        while(code.get(i).charAt(j)==' ') j++;
//                            String name="";
//                            while((code.get(i).charAt(j)!=' ')&&(code.get(i).charAt(j)!=',')){
//                                name+=code.get(i).charAt(j);
//                            }
//                            module.addInput(name);
//                        
//                    }
//                }
//                
//                if(code.get(i).charAt(j)=='o'){ //add outputs
//                    if(code.get(i).substring(j,j+6).equals("output")){
//                        j+=6;
//                        while(code.get(i).charAt(j)==' ') j++;
//                            String name="";
//                            while((code.get(i).charAt(j)!=' ')&&(code.get(i).charAt(j)!=',')){
//                                name+=code.get(i).charAt(j);
//                            }
//                            module.addOutput(name);
//                        
//                    }
//                }
//                
//                if(code.get(i).charAt(j)=='w'){ //add wires
//                    if(code.get(i).substring(j,j+4).equals("wire")){
//                        j+=4;
//                        while(code.get(i).charAt(j)==' ') j++;
//                            String name="";
//                            while((code.get(i).charAt(j)!=' ')&&(code.get(i).charAt(j)!=',')){
//                                name+=code.get(i).charAt(j);
//                            }
//                            module.addWire(name);
//                        
//                    }
//                }
//                
//                if(code.get(i).charAt(j)=='a'){ //assign statements
//                    if(code.get(i).substring(j,j+6).equals("assign")){
//                        j+=6;
//                        while(code.get(i).charAt(j)==' ') j++;
//                            String name="";
//                            while((code.get(i).charAt(j)!=' ')&&(code.get(i).charAt(j)!=',')
//                                    &&(code.get(i).charAt(j)!='=')){
//                                name+=code.get(i).charAt(j);
//                            }
//                            module.setWire(name);
//                        
//                    }
//                }
//                
//            }
        }
        
        //second sweep: read through code in order, sans modules
    }
    /**
     * Determines the left side of the equation
     * @param statement the statement
     */
    public void assign(String statement){
        int j=0;
        String lse="";
        while(statement.charAt(j)==' ') j++;
        while((statement.charAt(j)!=' ')&&(statement.charAt(j)!='=')){
            lse+=statement.charAt(j);
        }
        
        while((statement.charAt(j)==' ')||(statement.charAt(j)=='=')) j++;
        assign2(statement.substring(j));
    }
    /**
     * Determines the right side of the equation, recursively
     * @param statement the statement
     * @return a name of a Wire
     */
    public String assign2(String statement){
        int j=0,k=0;
        String lop=null,op=null,rop=null; //left of op, op, right of op
        while(j<statement.length()){
            switch(statement.charAt(j)){
                case ' ':
                    break;
                
                case '(':
                    k=statement.length()-1;
                    while(statement.charAt(k)!=')') k--;
                    String temp;
                    temp = assign2(statement.substring(j+1,k));
                    if(op==null)
                        lop=temp;
                    else{
                        rop=temp;
                    }
                    j=k;
                    break;
                    
                case '+':
                    op="+";
                    break;
                    
                case '-':
                    op="-";
                    break;
                    
                case ';':
                    break;
                    
                default:
                    if(lop==null) lop="";
                    if(op==null)
                        lop+=statement.charAt(j);
                    else{
                        if(rop==null) rop="";
                        rop+=statement.charAt(j);
                    }
                    break;
            }
            j++;
        }
        return finalAssign(lop,op,rop);
    }
    /**
     * Creates Part, Wires for given operator and Wire names
     * @param lop name left of operator
     * @param op the operator
     * @param rop name right of operator
     * @return name of the wire to represent result of operation
     */
    public String finalAssign(String lop,String op,String rop){
        int i,j;
        for(i=0;!lop.equals(module.wires.get(i).name);i++);
        Wire tempR = module.wires.get(i);
        for(j=0;!rop.equals(module.wires.get(j).name);j++);
        Wire tempL = module.wires.get(j);
        int k;
        for(k=0;!op.equals(operators[k]);k++);
        switch(k){
            case 0: //ADDER
                Adder newAdder = new Adder("ADD"+numAdders);
                module.parts.add(newAdder);
                tempR.ports.add(newAdder.ports.get(0));
                tempL.ports.add(newAdder.ports.get(1));
                module.addWire("MISC"+numWires).ports.add(newAdder.ports.get(3));
                return ("MISC"+numWires);
                
            case 1:
                Subtractor newSub = new Subtractor("ADD"+numAdders);
                module.parts.add(newSub);
                tempR.ports.add(newSub.ports.get(0));
                tempL.ports.add(newSub.ports.get(1));
                module.addWire("MISC"+numWires).ports.add(newSub.ports.get(3));
                return ("MISC"+numWires);
        }
        return null;
    }
}
