/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vlt;

import java.util.ArrayList;
import java.lang.String;
import java.lang.Boolean;
/**
 *
 * @author Parker
 */

public class Compiler {
    public ArrayList<String> code;
    public Module module;
    public EWriter edif;
    public String[] operators = {"+","-",">=","!","?:"};
    
    public int numWires;
    public int numAdders;
    public int numSubtractors;
    public int numComparators;
    public int numMuxs;
    public int numInverters;
    public int numORs;
    
    public Compiler(ArrayList<String> code){
        //initialize everything
        this.code=code;
        numWires=0;
        numAdders=0;
        numSubtractors=0;
        numComparators=0;
        numMuxs=0;
        numInverters=0;
        numORs=0;
    }
    
    public void compile(){
        module = new Module("NAME");
        for(int i=0; i<code.size(); i++){
            //make sure the line is not empty and is not a comment
            if(!code.get(i).isEmpty() && code.get(i).length() > 4){
                //add all the inputs
                if(code.get(i).substring(0,5).equals("input")){
                    if(code.get(i).substring(5,6).equals(" "))
                        module.addInputs(code.get(i).substring(6).split("\\,"));
                }
                //add all the outputs
                else if(code.get(i).substring(0,6).equals("output")){
                    if(code.get(i).substring(6,7).equals(" "))
                        module.addOutputs(code.get(i).substring(7).split("\\,"));
                }
                //add all the predefined wires
                else if(code.get(i).substring(0,4).equals("wire")){
                    if(code.get(i).substring(4,5).equals(" "))
                        module.addWires(code.get(i).substring(5).split("\\,"));
                }
                //assign all the predefined wires
                else if(code.get(i).substring(0,6).equals("assign")){
                    if(code.get(i).substring(6,7).equals(" "))
                        assign(code.get(i).substring(7));
                }
            }
        }
        
        edif = new EWriter(module);
        edif.write();
    }
    
    public void assign(String statement){
        //get the wire we are assigning to
        int a = 0;
        Wire left = null;
        while(statement.charAt(a)!=' '&&statement.charAt(a)!='=')
            a++;
        for(Wire wire : module.wires){
            if(wire.name.equals(statement.substring(0,a)))
                left = wire;
        }
        //break up each expression and connect to operator
        ArrayList<String> myStatements = new ArrayList<String>();
        ArrayList<Boolean> finStatements = new ArrayList<Boolean>();
        //get to the start of the right side of the equation
        while(statement.charAt(a)==' '||statement.charAt(a)=='=')
            a++;
        
        //go through the right side of the equation and add to the Arraylist
        //until the ; is reached
        String temp = null;
        while(statement.charAt(a)!=';'){
            switch(statement.charAt(a)){
                case ' ':
                    if(temp != null){
                        myStatements.add(temp);
                        finStatements.add(Boolean.FALSE);
                        temp=null;
                    }
                    break;
                case '(':
                    break;
                case ')':
                    break;
                case '>':
                    if(statement.charAt(a+1)=='='){
                        myStatements.add(statement.substring(a,a+2));
                        finStatements.add(Boolean.FALSE);
                        a++;
                    }
                    else{
                        myStatements.add("" + statement.charAt(a));
                        finStatements.add(Boolean.FALSE);
                    }
                    break;
                case '?':
                    myStatements.add("" + statement.charAt(a));
                    finStatements.add(Boolean.FALSE);
                    break;
                case ':':
                    myStatements.add("" + statement.charAt(a));
                    finStatements.add(Boolean.FALSE);
                    break;
                case '!':
                    myStatements.add("" + statement.charAt(a));
                    finStatements.add(Boolean.FALSE);
                    break;
                default:
                    if(temp==null)
                        temp="";
                    temp+=statement.charAt(a);
            }
            a++;
        }
        if(temp!=null){
            myStatements.add(temp);
            finStatements.add(Boolean.FALSE);
        }
        //things to watch for: (...), !, ?, :
        //perform operations using functions and recursion
        int b=0;
        boolean done = true;
        ArrayList<String> tempList= new ArrayList<String>();
        String tempName = null;
        //while(done){
            //do all the comparators first
        if(myStatements.size() > 1){
            for(int i = 0; i < myStatements.size(); i++){
                if(myStatements.get(i).equals(">=") ||
                        myStatements.get(i).equals("<=") ||
                        myStatements.get(i).equals(">") ||
                        myStatements.get(i).equals("<")){
                    tempList.add(myStatements.get(i-1));
                    tempList.add(myStatements.get(i));
                    tempList.add(myStatements.get(i+1));
                    myStatements.remove(i-1);
                    finStatements.remove(i-1);
                    myStatements.remove(i-1);
                    finStatements.remove(i-1);
                    myStatements.remove(i-1);
                    finStatements.remove(i-1);
                    i--;
                    for(int j = 0; j < myStatements.size(); j++){
                        if(myStatements.get(j).equals("?")){
                            tempList.add(myStatements.get(j));
                            tempList.add(myStatements.get(j+1));
                            tempList.add(myStatements.get(j+2));
                            tempList.add(myStatements.get(j+3));
                            myStatements.remove(j);
                            myStatements.remove(j);
                            myStatements.remove(j);
                            myStatements.remove(j);
                            finStatements.remove(j);
                            finStatements.remove(j);
                            finStatements.remove(j);
                            finStatements.remove(j);
                        }
                    }
                    tempName = comparator(tempList);
                    myStatements.add(i,tempName);
                    tempList.clear();
                    finStatements.add(i,Boolean.TRUE);
                    i++;
                }
                else if(myStatements.get(i).equals("?")){
                    for(int j = 0; j < 5; j++){
                        tempList.add(myStatements.get(i-1));
                        myStatements.remove(i-1);
                        finStatements.remove(i-1);
                    }
                    i--;
                    myStatements.add(i,turnary(tempList));
                    finStatements.add(i,Boolean.TRUE);
                    tempList.clear();
                }
            }
        }
            int count = 0;
            int finalCount = -1;
            for(Wire wire : module.wires){
                if(wire.name.equals(myStatements.get(0))){
                    left.copy(wire);
                    finalCount = count;
                }
                count++;
            }
            module.wires.remove(finalCount);
    }
    
    public String comparator(ArrayList<String> myCompStatement){
        Wire Left = null;
        Wire Right = null;
        for(Wire wire : module.wires){
            if(wire.name.equals(myCompStatement.get(0)))
                Left = wire;
            if(wire.name.equals(myCompStatement.get(2)))
                Right = wire;
        }
        switch(myCompStatement.get(1)){
            case ">=":
                int temp1 = Integer.parseInt(myCompStatement.get(4));
                int temp2 = Integer.parseInt(myCompStatement.get(6));
                Comparator newCompGE = new Comparator("COMP>="+numComparators);
                if(temp1 == 1 && temp2 == 0)
                    newCompGE.activeLow = true;
                else
                    newCompGE.activeLow = false;
                numComparators++;
                module.parts.add(newCompGE);
                Left.ports.add(newCompGE.ports.get(0));
                Right.ports.add(newCompGE.ports.get(1));
                module.addWire("MISC"+numWires).ports.add(newCompGE.ports.get(2));
                numWires++;
                module.addWire("MISC"+numWires).ports.add(newCompGE.ports.get(3));
                numWires++;
                OR newOR = new OR("OR"+numORs);
                numORs++;
                module.parts.add(newOR);
                module.wires.get(module.wires.size()-2).ports.add(newOR.ports.get(0));
                module.wires.get(module.wires.size()-1).ports.add(newOR.ports.get(1));
                module.addWire("MISC"+numWires).ports.add(newOR.ports.get(2));
                numWires++;
                return ("MISC"+(numWires-1));
        }
        return "";
    }
    
    public String turnary(ArrayList<String> myTurnStatement){
        Wire Left = null;
        Wire Turn1 = null;
        Wire Turn2 = null;
        for(Wire wire : module.wires){
            if(wire.name.equals(myTurnStatement.get(0)))
                Left = wire;
            if(wire.name.equals(myTurnStatement.get(2)))
                Turn1 = wire;
            if(wire.name.equals(myTurnStatement.get(4)))
                Turn2 = wire;
        }
        Mux newMux = new Mux("MUX"+numMuxs);
        numMuxs++;
        module.parts.add(newMux);
        Left.ports.add(newMux.ports.get(2));
        Turn1.ports.add(newMux.ports.get(1));
        Turn2.ports.add(newMux.ports.get(0));
        module.addWire("MISC"+numWires).ports.add(newMux.ports.get(3));
        numWires++;
        return ("MISC"+(numWires-1));
    }
}

