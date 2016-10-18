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
    public EWriter edif;
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
        
        String currentString;
        int i,j;
        module = new Module("NAME");
        for(i=0;i<code.size();i++){
//            if(code[i].substring(0,8).equals("module "))
//                module = new Module();
            currentString = code.get(i);
            if(!code.get(i).isEmpty() && code.get(i).length() > 4){ //if the line is empyy, skip it!
                //add inputs to the module
                if(code.get(i).substring(0,5).equals("input")){
                    if(code.get(i).substring(5,6).equals(" ")){
                        module.addInputs(code.get(i).substring(6).split("\\,"));
                    }
                }
                //add outputs to the module
                else if(code.get(i).substring(0,6).equals("output")){
                    if(code.get(i).substring(6,7).equals(" ")){
                        module.addOutputs(code.get(i).substring(7).split("\\,"));
                    }
                }
                //add wires to the module
                else if(code.get(i).substring(0,4).equals("wire")){
                    if(code.get(i).substring(4,5).equals(" ")){
                        module.addWires(code.get(i).substring(5).split("\\,"));
                    }
                }
                //assign wires
                else if(code.get(i).substring(0,6).equals("assign")){
                    if(code.get(i).substring(6,7).equals(" ")){
                        assign(code.get(i).substring(7));
                    }
                }
            }
        }
        
        edif = new EWriter(module);
        edif.write();
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
            j++;
        }
        
        while((statement.charAt(j)==' ')||(statement.charAt(j)=='=')) j++;
       assign2(statement.substring(j),lse);
    }
    /**
     * Determines the right side of the equation, recursively
     * @param statement the statement
     * @return a name of a Wire
     */
    public String assign2(String statement,String lse){
        int j=0,k=0;
        String lop=null,op=null,rop=null; //left of op, op, right of op
        while(j<statement.length()){
            switch(statement.charAt(j)){ //act based on the current char
                case ' ':
                    if(rop != null){
                        String temp2 = finalAssign(lop,op,rop,lse,false);
                        lop = temp2;
                        rop = null;
                    }
                    break;
                
                case '(':
                    k=statement.length()-1;
                    while(statement.charAt(k)!=')') k--;
                    String temp;
                    temp = assign2(statement.substring(j+1,k),null);
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
        if(op==null)
            return finalAssign(lop,op,rop,lse,true);
        else
            if(j != statement.length())
                return assign2(finalAssign(lop,op,rop,lse,false) + statement.substring(j),lse);
            else if(lse == null){
                return finalAssign(lop,op,rop,lse,false);
            }
            else
                return finalAssign(lop,op,rop,lse,true);
    }
    /**
     * Creates Part, Wires for given operator and Wire names
     * @param lop name left of operator
     * @param op the operator
     * @param rop name right of operator
     * @return name of the wire to represent result of operation
     */
    public String finalAssign(String lop,String op,String rop,String lse,boolean end){
        int i,j;
        Wire tempLSE = null;
        Wire tempR = null;
        Wire tempL = null;
        if(lop != null){
            for(i=0;!lop.equals(module.wires.get(i).name);i++);
            tempR = module.wires.get(i);
        }
        if(rop != null){
            for(j=0;!rop.equals(module.wires.get(j).name);j++);
            tempL = module.wires.get(j);
        }
        if(lse != null){
            for(j=0;!lse.equals(module.wires.get(j).name);j++);
            tempLSE = module.wires.get(j);
        }
        int k;
        for(k=0;!op.equals(operators[k]);k++);
        switch(k){
            case 0: //ADDER
                Adder newAdder = new Adder("ADD"+numAdders);
                module.parts.add(newAdder);
                tempR.ports.add(newAdder.ports.get(0));
                tempL.ports.add(newAdder.ports.get(1));
                if(!end || lse == null){
                    module.addWire("MISC"+numWires).ports.add(newAdder.ports.get(3));
                    numWires++; numAdders++;
                    return ("MISC"+(numWires-1));
                }
                else{
                    tempLSE.ports.add(newAdder.ports.get(3));
                    numAdders++;
                    return tempLSE.name;
                }
                
            case 1:
                Subtractor newSub = new Subtractor("ADD"+numAdders);
                module.parts.add(newSub);
                tempR.ports.add(newSub.ports.get(0));
                tempL.ports.add(newSub.ports.get(1));
                if(!end || lse == null){
                    module.addWire("MISC"+numWires).ports.add(newSub.ports.get(3));
                    numWires++; numAdders++;
                    return ("MISC"+(numWires-1));
                }
                else{
                    tempLSE.ports.add(newSub.ports.get(3));
                    numAdders++;
                    return tempLSE.name;
                }
        }
        return null;
    }
}
