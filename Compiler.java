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
    //DONE
    public String[] operators = {"+","-",">=","<=",">","<","==","!=","!","?",":","*","/","&","|","^"};
    //TODO
    public String[] operators2 = {">>","<<","~","&&","||","~&","~|","~^","^~","%"};
        
    public int numWires;
    public int numAdders;
    public int numSubtractors;
    public int numComparators;
    public int numMuxs;
    public int numInverters;
    public int numORs;
    public int numANDs;
    public int numXORs;
    public int numMultipliers;
    public int numDividers;
    
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
        numANDs=0;
        numXORs=0;
        numMultipliers=0;
        numDividers=0;
    }
    
    public void compile(){
        module = new Module("NAME");
        for(int i=0; i<code.size(); i++){
            //make sure the line is not empty and is not a comment
            if(!code.get(i).isEmpty() && code.get(i).length() > 4){
                //add all the inputs
                if(code.get(i).substring(0,5).equals("input")){
                    if(code.get(i).substring(5,6).equals(" ")){
                        if(code.get(i).charAt(7)=='[')
                            ioHelper(code.get(i),1);
                        else
                            module.addInputs(code.get(i).substring(6).split("\\,"),1);
                    }
                    else
                        ioHelper(code.get(i),1);
                }
                //add all the outputs
                else if(code.get(i).substring(0,6).equals("output")){
                    if(code.get(i).substring(6,7).equals(" ")){
                        if(code.get(i).charAt(7)=='[')
                            ioHelper(code.get(i),2);
                        else
                            module.addOutputs(code.get(i).substring(7).split("\\,"),1);
                    }
                    else
                        ioHelper(code.get(i),2);
                }
                //add all the predefined wires
                else if(code.get(i).substring(0,4).equals("wire")){
                    if(code.get(i).substring(4,5).equals(" ")){
                        if(code.get(i).charAt(7)=='[')
                            ioHelper(code.get(i),3);
                        else
                        module.addWires(code.get(i).substring(5).split("\\,"),1);
                    }
                    else
                        ioHelper(code.get(i),3);
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
    
    public void ioHelper(String io, int ioType){
        int a = 0;
        int[] size = new int[2];
        int finalSize;
        String temp = null;
        while(io.charAt(a) != '[')
            a++;
        a++;
        for(int i = 0; i < 2; i++){
            temp="";
            while(io.charAt(a) != ':' && io.charAt(a) != ']'){
                temp+=io.charAt(a);
                a++;
            }
            a++;
            size[i] = Integer.parseInt(temp);
        }
        finalSize = (size[0]-size[1])+1;
        while(io.charAt(a) == ' ')
            a++;
        switch(ioType){
            case 1:
                module.addInputs(io.substring(a).split("\\,"), finalSize);
                break;
            case 2:
                module.addOutputs(io.substring(a).split("\\,"), finalSize);
                break;
            case 3:
                module.addWires(io.substring(a).split("\\,"), finalSize);
                break;
        }
    }
    
    public String assign(String statement){
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
                    myStatements.add("" + statement.charAt(a));
                    finStatements.add(Boolean.FALSE);
                    break;
                case ')':
                    if(temp!=null){
                        myStatements.add(temp);
                        finStatements.add(Boolean.FALSE);
                        temp=null;
                    }
                    myStatements.add("" + statement.charAt(a));
                    finStatements.add(Boolean.FALSE);
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
        ArrayList<String> tempList= new ArrayList<String>();
        //while(done){
            //do all the comparators first
        if(myStatements.size() > 1){
            for(int i = 0; i < myStatements.size(); i++){
                if(myStatements.get(i).equals("(")){
                    myStatements.remove(i);
                    while(!myStatements.get(i).equals(")")){
                        tempList.add(myStatements.get(i));
                        myStatements.remove(i);
                        finStatements.remove(i);
                    }
                    myStatements.remove(i);
                    finStatements.remove(i);
                    int currSize = tempList.size();
                    for(int l = 0; l < currSize; l++){
                        if(tempList.get(l).equals(">=")||tempList.get(l).equals("<=")||tempList.get(l).equals(">")||
                                tempList.get(l).equals("<")||tempList.get(l).equals("==")||tempList.get(l).equals("!=")){
                            for(int j = i; j < myStatements.size(); j++){
                                if(myStatements.get(j).equals("?")){
                                    for(int k = 0; k < 4; k++){
                                        tempList.add(myStatements.get(j));
                                        myStatements.remove(j);
                                        finStatements.remove(j);
                                    }
                                }
                            }
                        }
                    }
                    String tempAssign = "";
                    for(String state : tempList){
                        tempAssign+=state;
                        tempAssign+=" ";
                    }
                    module.addWire("MISC"+numWires,1);
                    numWires++;
                    myStatements.add(i,assign("MISC"+(numWires-1)+" = "+tempAssign+";"));
                    finStatements.add(i,Boolean.TRUE);
                    tempList.clear();
                }
            }
            for(int i = 0; i < myStatements.size(); i++){
                if(myStatements.get(i).equals("!")){
                    for(int j = 0; j < 2; j++){
                        tempList.add(myStatements.get(i));
                        myStatements.remove(i);
                        finStatements.remove(i);
                    }
                    myStatements.add(i,not(tempList));
                    tempList.clear();
                    finStatements.add(i,Boolean.TRUE);
                }
            }
            for(int i = 0; i < myStatements.size(); i++){
                if(myStatements.get(i).equals("&")){
                    i--;
                    for(int j = 0; j < 3; j++){
                        tempList.add(myStatements.get(i));
                        myStatements.remove(i);
                        finStatements.remove(i);
                    }
                    myStatements.add(i,and(tempList));
                    tempList.clear();
                    finStatements.add(i,Boolean.TRUE);
                }
            }
            for(int i = 0; i < myStatements.size(); i++){
                if(myStatements.get(i).equals("|")){
                    i--;
                    for(int j = 0; j < 3; j++){
                        tempList.add(myStatements.get(i));
                        myStatements.remove(i);
                        finStatements.remove(i);
                    }
                    myStatements.add(i,and(tempList));
                    tempList.clear();
                    finStatements.add(i,Boolean.TRUE);
                }
            }
            for(int i = 0; i < myStatements.size(); i++){
                if(myStatements.get(i).equals("^")){
                    i--;
                    for(int j = 0; j < 3; j++){
                        tempList.add(myStatements.get(i));
                        myStatements.remove(i);
                        finStatements.remove(i);
                    }
                    myStatements.add(i,xor(tempList));
                    tempList.clear();
                    finStatements.add(i,Boolean.TRUE);
                }
            }
            for(int i = 0; i < myStatements.size(); i++){
                if(myStatements.get(i).equals("*")){
                    i--;
                    for(int j = 0; j < 3; j++){
                        tempList.add(myStatements.get(i));
                        myStatements.remove(i);
                        finStatements.remove(i);
                    }
                    myStatements.add(i,multiplier(tempList));
                    tempList.clear();
                    finStatements.add(i,Boolean.TRUE);
                }
            }
            for(int i = 0; i < myStatements.size(); i++){
                if(myStatements.get(i).equals("/")){
                    i--;
                    for(int j = 0; j < 3; j++){
                        tempList.add(myStatements.get(i));
                        myStatements.remove(i);
                        finStatements.remove(i);
                    }
                    myStatements.add(i,divider(tempList));
                    tempList.clear();
                    finStatements.add(i,Boolean.TRUE);
                }
            }
            for(int i = 0; i < myStatements.size(); i++){
                if(myStatements.get(i).equals("+")){
                    i--;
                    for(int j = 0; j < 3; j++){
                        tempList.add(myStatements.get(i));
                        myStatements.remove(i);
                        finStatements.remove(i);
                    }
                    myStatements.add(i,adder(tempList));
                    tempList.clear();
                    finStatements.add(i,Boolean.TRUE);
                }
                else if(myStatements.get(i).equals("-")){
                    i--;
                    for(int j = 0; j < 3; j++){
                        tempList.add(myStatements.get(i));
                        myStatements.remove(i);
                        finStatements.remove(i);
                    }
                    myStatements.add(i,subtractor(tempList));
                    tempList.clear();
                    finStatements.add(i,Boolean.TRUE);
                }
            }
            for(int i = 0; i < myStatements.size(); i++){
                if(myStatements.get(i).equals(">=") ||
                        myStatements.get(i).equals("<=") ||
                        myStatements.get(i).equals(">") ||
                        myStatements.get(i).equals("<") ||
                        myStatements.get(i).equals("==") ||
                        myStatements.get(i).equals("!=")){
                    i--;
                    for(int j = 0; j < 3; j++){
                        tempList.add(myStatements.get(i));
                        myStatements.remove(i);
                        finStatements.remove(i);
                    }
                    for(int j = 0; j < myStatements.size(); j++){
                        if(myStatements.get(j).equals("?")){
                            for(int k = 0; k < 4; k++){
                                tempList.add(myStatements.get(j));
                                myStatements.remove(j);
                                finStatements.remove(j);
                            }
                        }
                    }
                    myStatements.add(i,comparator(tempList));
                    tempList.clear();
                    finStatements.add(i,Boolean.TRUE);
                }
            }
            for(int i = 0; i < myStatements.size(); i++){
                if(myStatements.get(i).equals("?")){
                    i--;
                    for(int j = 0; j < 5; j++){
                        tempList.add(myStatements.get(i));
                        myStatements.remove(i);
                        finStatements.remove(i);
                    }
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
            return left.name;
    }
    
    public String not(ArrayList<String> myNotStatement){
        Wire notWire = null;
        for(Wire wire : module.wires){
            if(wire.name.equals(myNotStatement.get(1)))
                notWire = wire;
        }
        Inverter newInv = new Inverter("INV"+numInverters);
        module.parts.add(newInv);
        numInverters++;
        notWire.ports.add(newInv.ports.get(0));
        module.addWire("MISC"+numWires,1).ports.add(newInv.ports.get(1));
        numWires++;
        return ("MISC"+(numWires-1));
    }
    
    public String and(ArrayList<String> myAndStatement){
        Wire Left = null;
        Wire Right = null;
        for(Wire wire : module.wires){
            if(wire.name.equals(myAndStatement.get(0)))
                Left = wire;
            if(wire.name.equals(myAndStatement.get(2)))
                Right = wire;
        }
        AND newAnd = new AND("AND"+numANDs);
        numANDs++;
        module.parts.add(newAnd);
        Left.ports.add(newAnd.ports.get(0));
        Right.ports.add(newAnd.ports.get(1));
        module.addWire("MISC"+numWires,1).ports.add(newAnd.ports.get(2));
        numWires++;
        return ("MISC"+(numWires-1)); 
    }
    
    public String or(ArrayList<String> myOrStatement){
        Wire Left = null;
        Wire Right = null;
        for(Wire wire : module.wires){
            if(wire.name.equals(myOrStatement.get(0)))
                Left = wire;
            if(wire.name.equals(myOrStatement.get(2)))
                Right = wire;
        }
        OR newOr = new OR("OR"+numORs);
        numORs++;
        module.parts.add(newOr);
        Left.ports.add(newOr.ports.get(0));
        Right.ports.add(newOr.ports.get(1));
        module.addWire("MISC"+numWires,1).ports.add(newOr.ports.get(2));
        numWires++;
        return ("MISC"+(numWires-1)); 
    }
    
    public String xor(ArrayList<String> myXorStatement){
        Wire Left = null;
        Wire Right = null;
        for(Wire wire : module.wires){
            if(wire.name.equals(myXorStatement.get(0)))
                Left = wire;
            if(wire.name.equals(myXorStatement.get(2)))
                Right = wire;
        }
        XOR newXor = new XOR("XOR"+numXORs);
        numXORs++;
        module.parts.add(newXor);
        Left.ports.add(newXor.ports.get(0));
        Right.ports.add(newXor.ports.get(1));
        module.addWire("MISC"+numWires,1).ports.add(newXor.ports.get(2));
        numWires++;
        return ("MISC"+(numWires-1)); 
    }
    
    public String adder(ArrayList<String> myAddStatement){
        Wire Left = null;
        Wire Right = null;
        int bitSize;
        for(Wire wire : module.wires){
            if(wire.name.equals(myAddStatement.get(0)))
                Left = wire;
            if(wire.name.equals(myAddStatement.get(2)))
                Right = wire;
        }
        if(Left.size > Right.size)
            bitSize = Left.size;
        else
            bitSize = Right.size;
        Adder newAdder = new Adder("ADD"+numAdders);
        numAdders++;
        module.parts.add(newAdder);
        Left.ports.add(newAdder.ports.get(0));
        Right.ports.add(newAdder.ports.get(1));
        module.addWire("MISC"+numWires,bitSize).ports.add(newAdder.ports.get(2));
        numWires++;
        return ("MISC"+(numWires-1));
    }
    
    public String subtractor(ArrayList<String> mySubStatement){
        Wire Left = null;
        Wire Right = null;
        int bitSize;
        for(Wire wire : module.wires){
            if(wire.name.equals(mySubStatement.get(0)))
                Left = wire;
            if(wire.name.equals(mySubStatement.get(2)))
                Right = wire;
        }
        if(Left.size > Right.size)
            bitSize = Left.size;
        else
            bitSize = Right.size;
        Subtractor newSubtractor = new Subtractor("SUB"+numSubtractors);
        numSubtractors++;
        module.parts.add(newSubtractor);
        Left.ports.add(newSubtractor.ports.get(0));
        Right.ports.add(newSubtractor.ports.get(1));
        module.addWire("MISC"+numWires,bitSize).ports.add(newSubtractor.ports.get(2));
        numWires++;
        return ("MISC"+(numWires-1));
    }
    
    public String multiplier(ArrayList<String> myMultStatement){
        Wire Left = null;
        Wire Right = null;
        int bitSize;
        for(Wire wire : module.wires){
            if(wire.name.equals(myMultStatement.get(0)))
                Left = wire;
            if(wire.name.equals(myMultStatement.get(2)))
                Right = wire;
        }
        if(Left.size > Right.size)
            bitSize = Left.size;
        else
            bitSize = Right.size;
        Multiplier newMultiplier = new Multiplier("MULT"+numMultipliers);
        numMultipliers++;
        module.parts.add(newMultiplier);
        Left.ports.add(newMultiplier.ports.get(0));
        Right.ports.add(newMultiplier.ports.get(1));
        module.addWire("MISC"+numWires,bitSize).ports.add(newMultiplier.ports.get(2));
        numWires++;
        return ("MISC"+(numWires-1));
    }
    
    public String divider(ArrayList<String> myDivStatement){
        Wire Left = null;
        Wire Right = null;
        int bitSize;
        for(Wire wire : module.wires){
            if(wire.name.equals(myDivStatement.get(0)))
                Left = wire;
            if(wire.name.equals(myDivStatement.get(2)))
                Right = wire;
        }
        if(Left.size > Right.size)
            bitSize = Left.size;
        else
            bitSize = Right.size;
        Divider newDivider = new Divider("DIV"+numDividers);
        numDividers++;
        module.parts.add(newDivider);
        Left.ports.add(newDivider.ports.get(0));
        Right.ports.add(newDivider.ports.get(1));
        module.addWire("MISC"+numWires,bitSize).ports.add(newDivider.ports.get(2));
        numWires++;
        return ("MISC"+(numWires-1));
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
        Comparator newComp = new Comparator("COMP>="+numComparators);
/*        int temp1 = Integer.parseInt(myCompStatement.get(4));
        int temp2 = Integer.parseInt(myCompStatement.get(6));
        if(temp1 == 1 && temp2 == 0)
            newComp.activeLow = true;
        else
            newComp.activeLow = false;
*/
        numComparators++;
        module.parts.add(newComp);
        Left.ports.add(newComp.ports.get(0));
        Right.ports.add(newComp.ports.get(1));
        switch(myCompStatement.get(1)){
            case ">=":
                module.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(2));
                numWires++;
                module.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(3));
                numWires++;
                OR newORGE = new OR("OR"+numORs);
                numORs++;
                module.parts.add(newORGE);
                module.wires.get(module.wires.size()-2).ports.add(newORGE.ports.get(0));
                module.wires.get(module.wires.size()-1).ports.add(newORGE.ports.get(1));
                module.addWire("MISC"+numWires,1).ports.add(newORGE.ports.get(2));
                numWires++;
                return ("MISC"+(numWires-1));
            case "<=":
                module.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(3));
                numWires++;
                module.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(4));
                numWires++;
                OR newORLE = new OR("OR"+numORs);
                numORs++;
                module.parts.add(newORLE);
                module.wires.get(module.wires.size()-2).ports.add(newORLE.ports.get(0));
                module.wires.get(module.wires.size()-1).ports.add(newORLE.ports.get(1));
                module.addWire("MISC"+numWires,1).ports.add(newORLE.ports.get(2));
                numWires++;
                return ("MISC"+(numWires-1));
            case ">":
                module.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(2));
                numWires++;
                return ("MISC"+(numWires-1));
            case "<":
                module.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(4));
                numWires++;
                return ("MISC"+(numWires-1));
            case "==":
                module.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(3));
                numWires++;
                return ("MISC"+(numWires-1));
            case "!=":
                module.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(3));
                numWires++;
                Inverter newInv = new Inverter("INV"+numInverters);
                numInverters++;
                module.parts.add(newInv);
                module.wires.get(module.wires.size()-1).ports.add(newInv.ports.get(0));
                module.addWire("MISC"+numWires,1).ports.add(newInv.ports.get(1));
                numWires++;
                return ("MISC"+(numWires-1));
        }
        return "";
    }
    
    public String turnary(ArrayList<String> myTurnStatement){
        Wire Left = null;
        Wire Turn1 = null;
        Wire Turn2 = null;
        int bitSize;
        for(Wire wire : module.wires){
            if(wire.name.equals(myTurnStatement.get(0)))
                Left = wire;
            if(wire.name.equals(myTurnStatement.get(2)))
                Turn1 = wire;
            if(wire.name.equals(myTurnStatement.get(4)))
                Turn2 = wire;
        }
        if(Turn1.size > Turn2.size)
            bitSize = Turn1.size;
        else
            bitSize = Turn2.size;
        Mux newMux = new Mux("MUX"+numMuxs);
        numMuxs++;
        module.parts.add(newMux);
        Left.ports.add(newMux.ports.get(2));
        Turn1.ports.add(newMux.ports.get(1));
        Turn2.ports.add(newMux.ports.get(0));
        module.addWire("MISC"+numWires,bitSize).ports.add(newMux.ports.get(3));
        numWires++;
        return ("MISC"+(numWires-1));
    }
}

