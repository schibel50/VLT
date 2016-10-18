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
    public String[] operators = {"+","-",">=","<=","==","!=","<",">","<<",">>",
        "!","~","&","|","~&","~|","^","~^","^~","*","/","%","&&","||","?",":"};
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
        
        char currentChar = ' ';
        int i,j;
        module = new Module("NAME");
        for(i=0;i<code.size();i++){
//            if(code[i].substring(0,8).equals("module "))
//                module = new Module();
            
            if(!code.get(i).isEmpty()){ //if the line is empyy, skip it!
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
            switch(statement.charAt(j)){ //act based on the current char
                case ' ':
                    break;
                
                case '(':
//                    k=statement.length()-1;
//                    while(statement.charAt(k)!=')') k--;
                    int count=1;
                    k=j+1;
                    while(count>0){
                        if(statement.charAt(k)=='(')
                            count++;
                        if(statement.charAt(k)==')')
                            count--;
                        k++;
                    }
                    String temp = assign2(statement.substring(j+1,k-1));
                    if(op==null)
                        lop=temp;
                    else{
                        rop=temp;
                    }
                    j=k-1;
                    break;
                    
                case '+':
                    op="+";
                    break;
                    
                case '-':
                    op="-";
                    break;
                    
                case '*':
                    op="*";
                    break;
                    
                case '/':
                    op="/";
                    
                case '%':
                    op="%";
                    
                case '>':
                    if(op==null)
                        op="";
                    op+=">";
                    break;
                    
                case '<':
                    if(op==null)
                        op="";
                    op+="<";
                    break;
                    
                case '=':
                    if(op==null)
                        op="";
                    op+="=";
                    break;
                    
                case '!':
                    op="!";
                    break;
                    
                case '~':
                    if(op==null)
                        op="";
                    op+="~";
                    break;
                    
                case '&':
                    if(op==null)
                        op="";
                    op+="&";
                    break;
                    
                case '|':
                    if(op==null)
                        op="";
                    op+="|";
                    break;
                    
                case '^':
                    if(op==null)
                        op="";
                    op+="^";
                    
                case '?':
                    op="?";
                    
                case ':':
                    op=":";
                    
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
            return null;
        else
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
        Wire tempR=null, tempL=null;
        for (Wire wire : module.wires) {
            if (rop.equals(wire.name)) tempR = wire;
            if (lop.equals(wire.name)) tempL = wire;
        }
        
        int k;
        for(k=0;!op.equals(operators[k]);k++);
        switch(k){
            case 0: //ADDER
                Adder newAdder = new Adder("ADD"+numAdders);
                module.parts.add(newAdder);
                tempR.ports.add(newAdder.ports.get(0));
                tempL.ports.add(newAdder.ports.get(1));
                module.addWire("MISC"+numWires).ports.add(newAdder.ports.get(3));
                numWires++; numAdders++;
                return ("MISC"+(numWires-1));
                
            case 1: //SUBTRACTOR
                Subtractor newSub = new Subtractor("ADD"+numAdders);
                module.parts.add(newSub);
                tempR.ports.add(newSub.ports.get(0));
                tempL.ports.add(newSub.ports.get(1));
                module.addWire("MISC"+numWires).ports.add(newSub.ports.get(3));
                numWires++; numAdders++;
                return ("MISC"+(numWires-1));
        }
        return null;
    }
}
