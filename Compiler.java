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
    public ArrayList<Module> modules;
    public Module currModule;
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
        this.modules = new ArrayList<Module>();
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
    
    public void moduleFinder(){
        String tempName = "";
        int start = -1;
        int end = -1;
        String[] tempIO = null;
        for(int i = 0; i < code.size();i++){
            if(!code.get(i).isEmpty() && code.get(i).length() > 5){
                if(code.get(i).substring(0,6).equals("module")){
                    start = i;
                    int a = 7;
                    while(code.get(i).charAt(a)==' ')
                        a++;
                    while(code.get(i).charAt(a)!='('){
                        tempName+=code.get(i).charAt(a);
                        a++;
                    }
                    int b = a;
                    int size = 1;
                    while(code.get(i).charAt(b)!=')'){
                        if(code.get(i).charAt(b)==',')
                            size++;
                        b++;
                    }
                    tempIO = new String[size];
                    tempIO = code.get(i).substring(a+1,b).split("\\,");
                    for(String name : tempIO)
                        name = name.trim();
                }
                if(code.get(i).length() > 7){
                    if(code.get(i).substring(0,9).equals("endmodule")){
                        end = i;
                    }
                }
                if(start != -1 && end != -1 && tempIO != null){
                    modules.add(new Module(tempName,start,end,tempIO));
                    tempName = "";
                    start = -1;
                    end = -1;
                    tempIO = null;
                }
            }
        }
    }
    
    public void compile(){
        for(int j = 0; j < modules.size(); j++){
            currModule = modules.get(j);
            for(int i=currModule.start; i<currModule.end; i++){
                //make sure the line is not empty and is not a comment
                if(!code.get(i).isEmpty() && code.get(i).length() > 2){
                    //add all the inputs
                    if(code.get(i).substring(0,5).equals("input")){
                        if(code.get(i).substring(5,6).equals(" ")){
                            if(code.get(i).charAt(6)=='[')
                                ioHelper(code.get(i),1);
                            else
                                currModule.addInputs(code.get(i).substring(6).split("\\,"),1);
                        }
                        else
                            ioHelper(code.get(i),1);
                    }
                    //add all the outputs
                    else if(code.get(i).substring(0,6).equals("output") && code.get(i).length() > 5){
                        if(code.get(i).substring(6,7).equals(" ")){
                            if(code.get(i).charAt(7)=='[')
                                ioHelper(code.get(i),2);
                            else
                                currModule.addOutputs(code.get(i).substring(7).split("\\,"),1);
                        }
                        else
                            ioHelper(code.get(i),2);
                    }
                    //add all the predefined wires
                    else if(code.get(i).substring(0,4).equals("wire") && code.get(i).length() > 3){
                        if(code.get(i).substring(4,5).equals(" ")){
                            if(code.get(i).charAt(5)=='[')
                                ioHelper(code.get(i),3);
                            else
                            currModule.addWires(code.get(i).substring(5).split("\\,"),1);
                        }
                        else
                            ioHelper(code.get(i),3);
                    }
                    //assign all the predefined wires
                    else if(code.get(i).substring(0,6).equals("assign") && code.get(i).length() > 5){
                        if(code.get(i).substring(6,7).equals(" "))
                            assign(code.get(i).substring(7));
                    }
                    else if(code.get(i).substring(0,6).equals("always")){
                        ArrayList<String> myAlways = new ArrayList<String>();
                        boolean done = false;
                        while(!done){
                            if(!code.get(i).isEmpty() && code.get(i).length() > 2){
                                if(!code.get(i).substring(0,3).equals("end")){
                                    myAlways.add(code.get(i));
                                    i++;
                                }
                                else
                                    done=true;
                            }
                            else{
                                myAlways.add(code.get(i));
                                i++;
                            }
                        }
                        myAlways.add(code.get(i));
                        i++;
                        always(myAlways);
                    }
                    else{
                        int a = 0;
                        String modName = "";
                        while(code.get(i).charAt(a)==' ')
                            a++;
                        while(code.get(i).charAt(a)!=' ' && 
                                code.get(i).charAt(a)!='('){
                            modName+=code.get(i).charAt(a);
                            a++;
                        }
                        for(int k = 0; k < modules.size(); k++){
                            if(modules.get(k).name.equals(modName)){
                                String tempName = "";
                                while(code.get(i).charAt(a)==' ')
                                    a++;
                                while(code.get(i).charAt(a)!='('){
                                    tempName+=code.get(i).charAt(a);
                                    a++;
                                }
                                int size = 1;
                                int b=a;
                                while(code.get(i).charAt(b)!=')'){
                                    if(code.get(i).charAt(b)==',')
                                        size++;
                                    b++;
                                }
                                String[] tempPorts = code.get(i).substring(a+1,b).split("\\,");
                                for(int l = 0; l < tempPorts.length;l++){
                                    if(tempPorts[l] == null)
                                        tempPorts[l] = "";
                                    tempPorts[l] = tempPorts[l].trim();
                                }
                                currModule.parts.add(new Module_Part(modName,tempPorts));
                            }
                        }
                    }
                }
            }
        }
        for(int i = modules.size(); i > 0; i--){
            insertMod(modules.get(i-1));
        }
        currModule = modules.get(0);
        bit2bits(currModule);
        part2gate(currModule);
        edif = new EWriter(modules.get(0));
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
                currModule.addInputs(io.substring(a).split("\\,"), finalSize);
                break;
            case 2:
                currModule.addOutputs(io.substring(a).split("\\,"), finalSize);
                break;
            case 3:
                currModule.addWires(io.substring(a).split("\\,"), finalSize);
                break;
        }
    }
    
    public String assign(String statement){
        //get the wire we are assigning to
        int a = 0;
        Wire left = null;
        while(statement.charAt(a)!=' '&&statement.charAt(a)!='=')
            a++;
        for(Wire wire : currModule.wires){
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
                    currModule.addWire("MISC"+numWires,1);
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
                    myStatements.add(i,or(tempList));
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
            int count = 0;
            int finalCount = -1;
            for(Wire wire : currModule.wires){
                if(wire.name.equals(myStatements.get(0))){
                    left.copy(wire);
                    finalCount = count;
                }
                count++;
            }
            currModule.wires.remove(finalCount);
            return left.name;
        }
        else{
            int count = 0;
            int finalCount = -1;
            for(Wire wire : currModule.wires){
                if(wire.name.equals(myStatements.get(0))){
                    left.copy(wire);
                    finalCount = count;
                }
                count++;
            }
            left.name=currModule.wires.get(finalCount).name;
            currModule.wires.remove(finalCount);
            return left.name;
        }
    }
    
    public String always(ArrayList<String> myAlwaysStatement){
        System.out.println("Always");
        return "";
    }
    
    public String not(ArrayList<String> myNotStatement){
        Wire notWire = null;
        for(Wire wire : currModule.wires){
            if(wire.name.equals(myNotStatement.get(1)))
                notWire = wire;
        }
        Inverter newInv = new Inverter("INV"+numInverters);
        currModule.parts.add(newInv);
        numInverters++;
        notWire.ports.add(newInv.ports.get(0));
        currModule.addWire("MISC"+numWires,1).ports.add(newInv.ports.get(1));
        numWires++;
        return ("MISC"+(numWires-1));
    }
    
    public String and(ArrayList<String> myAndStatement){
        Wire Left = null;
        Wire Right = null;
        for(Wire wire : currModule.wires){
            if(wire.name.equals(myAndStatement.get(0)))
                Left = wire;
            if(wire.name.equals(myAndStatement.get(2)))
                Right = wire;
        }
        AND newAnd = new AND("AND"+numANDs);
        numANDs++;
        currModule.parts.add(newAnd);
        Left.ports.add(newAnd.ports.get(0));
        Right.ports.add(newAnd.ports.get(1));
        currModule.addWire("MISC"+numWires,1).ports.add(newAnd.ports.get(2));
        numWires++;
        return ("MISC"+(numWires-1)); 
    }
    
    public String or(ArrayList<String> myOrStatement){
        Wire Left = null;
        Wire Right = null;
        for(Wire wire : currModule.wires){
            if(wire.name.equals(myOrStatement.get(0)))
                Left = wire;
            if(wire.name.equals(myOrStatement.get(2)))
                Right = wire;
        }
        OR newOr = new OR("OR"+numORs);
        numORs++;
        currModule.parts.add(newOr);
        Left.ports.add(newOr.ports.get(0));
        Right.ports.add(newOr.ports.get(1));
        currModule.addWire("MISC"+numWires,1).ports.add(newOr.ports.get(2));
        numWires++;
        return ("MISC"+(numWires-1)); 
    }
    
    public String xor(ArrayList<String> myXorStatement){
        Wire Left = null;
        Wire Right = null;
        for(Wire wire : currModule.wires){
            if(wire.name.equals(myXorStatement.get(0)))
                Left = wire;
            if(wire.name.equals(myXorStatement.get(2)))
                Right = wire;
        }
        XOR newXor = new XOR("XOR"+numXORs);
        numXORs++;
        currModule.parts.add(newXor);
        Left.ports.add(newXor.ports.get(0));
        Right.ports.add(newXor.ports.get(1));
        currModule.addWire("MISC"+numWires,1).ports.add(newXor.ports.get(2));
        numWires++;
        return ("MISC"+(numWires-1)); 
    }
    
    public String adder(ArrayList<String> myAddStatement){
        Wire Left = null;
        Wire Right = null;
        int bitSize;
        for(Wire wire : currModule.wires){
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
        currModule.parts.add(newAdder);
        Left.ports.add(newAdder.ports.get(0));
        Right.ports.add(newAdder.ports.get(1));
        currModule.addWire("MISC"+numWires,bitSize).ports.add(newAdder.ports.get(3));
        numWires++;
        return ("MISC"+(numWires-1));
    }
    
    public String subtractor(ArrayList<String> mySubStatement){
        Wire Left = null;
        Wire Right = null;
        int bitSize;
        for(Wire wire : currModule.wires){
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
        currModule.parts.add(newSubtractor);
        Left.ports.add(newSubtractor.ports.get(0));
        Right.ports.add(newSubtractor.ports.get(1));
        currModule.addWire("MISC"+numWires,bitSize).ports.add(newSubtractor.ports.get(2));
        numWires++;
        return ("MISC"+(numWires-1));
    }
    
    public String multiplier(ArrayList<String> myMultStatement){
        Wire Left = null;
        Wire Right = null;
        int bitSize;
        for(Wire wire : currModule.wires){
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
        currModule.parts.add(newMultiplier);
        Left.ports.add(newMultiplier.ports.get(0));
        Right.ports.add(newMultiplier.ports.get(1));
        currModule.addWire("MISC"+numWires,bitSize).ports.add(newMultiplier.ports.get(2));
        numWires++;
        return ("MISC"+(numWires-1));
    }
    
    public String divider(ArrayList<String> myDivStatement){
        Wire Left = null;
        Wire Right = null;
        int bitSize;
        for(Wire wire : currModule.wires){
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
        currModule.parts.add(newDivider);
        Left.ports.add(newDivider.ports.get(0));
        Right.ports.add(newDivider.ports.get(1));
        currModule.addWire("MISC"+numWires,bitSize).ports.add(newDivider.ports.get(2));
        numWires++;
        return ("MISC"+(numWires-1));
    }
    
    public String comparator(ArrayList<String> myCompStatement){
        Wire Left = null;
        Wire Right = null;
        for(Wire wire : currModule.wires){
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
        currModule.parts.add(newComp);
        Left.ports.add(newComp.ports.get(0));
        Right.ports.add(newComp.ports.get(1));
        switch(myCompStatement.get(1)){
            case ">=":
                currModule.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(2));
                numWires++;
                currModule.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(3));
                numWires++;
                OR newORGE = new OR("OR"+numORs);
                numORs++;
                currModule.parts.add(newORGE);
                currModule.wires.get(currModule.wires.size()-2).ports.add(newORGE.ports.get(0));
                currModule.wires.get(currModule.wires.size()-1).ports.add(newORGE.ports.get(1));
                currModule.addWire("MISC"+numWires,1).ports.add(newORGE.ports.get(2));
                numWires++;
                return ("MISC"+(numWires-1));
            case "<=":
                currModule.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(3));
                numWires++;
                currModule.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(4));
                numWires++;
                OR newORLE = new OR("OR"+numORs);
                numORs++;
                currModule.parts.add(newORLE);
                currModule.wires.get(currModule.wires.size()-2).ports.add(newORLE.ports.get(0));
                currModule.wires.get(currModule.wires.size()-1).ports.add(newORLE.ports.get(1));
                currModule.addWire("MISC"+numWires,1).ports.add(newORLE.ports.get(2));
                numWires++;
                return ("MISC"+(numWires-1));
            case ">":
                currModule.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(2));
                numWires++;
                return ("MISC"+(numWires-1));
            case "<":
                currModule.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(4));
                numWires++;
                return ("MISC"+(numWires-1));
            case "==":
                currModule.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(3));
                numWires++;
                return ("MISC"+(numWires-1));
            case "!=":
                currModule.addWire("MISC"+numWires,1).ports.add(newComp.ports.get(3));
                numWires++;
                Inverter newInv = new Inverter("INV"+numInverters);
                numInverters++;
                currModule.parts.add(newInv);
                currModule.wires.get(currModule.wires.size()-1).ports.add(newInv.ports.get(0));
                currModule.addWire("MISC"+numWires,1).ports.add(newInv.ports.get(1));
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
        for(Wire wire : currModule.wires){
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
        currModule.parts.add(newMux);
        Left.ports.add(newMux.ports.get(2));
        Turn1.ports.add(newMux.ports.get(1));
        Turn2.ports.add(newMux.ports.get(0));
        currModule.addWire("MISC"+numWires,bitSize).ports.add(newMux.ports.get(3));
        numWires++;
        return ("MISC"+(numWires-1));
    }
    
    public void insertMod(Module module){
        for(int l = 0; l < module.parts.size();l++){
            if(module.parts.get(l) instanceof Module_Part){
                Wire[] realWires = new Wire[module.parts.get(l).ports.size()];
                int i = 0;
                for(Wire wire : module.wires){
                    if(wire.name.equals(module.parts.get(l).ports.get(i).name)){
                            realWires[i]=wire;
                            i++;
                    }
                }
                String[] dummyWires;
                for(int j = 0; j < modules.size(); j++){
                    if(module.parts.get(l).name.equals(modules.get(j).name)){
                        dummyWires = modules.get(j).ioNames;
                        for(Part part2 : modules.get(j).parts){
                            module.parts.add(part2);
                        }
                        for(Wire wire : modules.get(j).wires){
                            boolean entered = false;
                            for(int k = 0; k < dummyWires.length; k++){
                                if(wire.name.equals(dummyWires[k])){
                                    entered = true;
                                    if(k < realWires.length){
                                        for(Port port : wire.ports){
                                            if(port.part != null)
                                                realWires[k].ports.add(port);
                                        }
                                    }
                                }
                            }
                            if(!entered){
                                module.wires.add(wire);
                                entered = false;
                            }
                        }
                    }
                }
                
//                for(int i = 0; i < modules.size(); i++){
//                    if(part.name.equals(modules.get(i).name)){
//                        for(Wire wire : modules.get(i).wires){
//                            for(Port port : wire.ports){
//                                if(port.part == null){
//                                    for(int j = 0; j < modules.size())
//                                }
//                            }
//                        }
//                    }
//                }
                module.parts.remove(l);
            }
        }
    }
    
    public void bit2bits(Module currModule){
//        ArrayList<Part> newParts = new ArrayList<Part>();
        ArrayList<Wire> newWires = new ArrayList<Wire>();
        for(Wire wire : currModule.wires){
            for(int i = 0; i < wire.size; i++){
                newWires.add(new Wire(wire.name+"-"+i,1));
                for(Port port : wire.ports){
                    if(port.part != null){
//                        Part tempPart = new Part("");
//                        tempPart.copy(port.part);
//                        newParts.add(tempPart);
//                        newParts.get(newParts.size()-1).name = newParts.get(newParts.size()-1).name+"-"+i;
                        newWires.get(newWires.size()-1).ports.add(new Port(port.name+"-"+i,port.part));
                    }   
                    else
                        newWires.get(newWires.size()-1).ports.add(new Port(port.name+"-"+i,port.IO));
                }
            }
        }
//        for(Part part: currModule.parts){
//            
//        }
        System.out.println("stop");
        currModule.wires = newWires;
//        currModule.parts = newParts;
    }
    /**
     * Convert parts not in database into gate layouts
     * @param part the part to be converted
     * @param wires array of Wires [output,input] to the part
     */
    public void part2gate(Module currModule){
        ArrayList<Wire> currWires = new ArrayList<Wire>();
        for(int i = 0; i < currModule.parts.size(); i++){
            for(Wire wire : currModule.wires){
                for(Port port : wire.ports){
                    if(port.part != null){
                        if(port.part.name.equals(currModule.parts.get(i).name)){
                            currWires.add(wire);
                        }
                    }
                }
            }
            if(currWires != null){
                if(currModule.parts.get(i) instanceof Adder){
                    adderBD(currModule.parts.get(i),currWires);
                    for(int j = 0; j < currWires.size(); j++){
                        for(int k = 0; k < currWires.get(j).ports.size(); k++){
                            if(currWires.get(j).ports.get(k).part != null){
                                if(currWires.get(j).ports.get(k).part.name.equals(currModule.parts.get(i).name)){
                                    currWires.get(j).ports.remove(k);
                                    k--;
                                }
                            }
                        }
                    }
                    currModule.parts.remove(i);
                    i--;
                }
//                if(currModule.parts.get(i) instanceof Subtractor)
//                    subtractorBD(currModule.parts.get(i))
            }
        }
        /*
        if(part instanceof Comparator){
            //wires = [EQ,GT,LT,a,b]
            assign(wires[0].name+" = !("+wires[3].name+"^"+wires[4].name+");"); //x=y
            assign(wires[1].name+" = "+wires[3].name+"&(!"+wires[4].name+");"); //x>y
            assign(wires[2].name+" = (!"+wires[3].name+")&"+wires[4].name+";"); //x<y
        }else if(part instanceof Adder){
            //wires = [S,Cout,a,b,Cin]
            currModule.addWire("MISC"+numWires,1);
            assign("MISC"+numWires + " = ("+wires[2].name+"^"+wires[3].name+")");
            assign(wires[0]+" = MISC0"+numWires+"^"+wires[4].name); //S
            assign(wires[1]+" = (Cin&MISC"+numWires+")|("+wires[2].name+"&"+wires[3].name+")"); //Cout
        }else if(part instanceof Subtractor){
            //wires = [S,Cout,a,b,Cin]
            currModule.addWire("MISC"+numWires,1);
            assign("MISC"+numWires + " = ("+wires[2].name+"^(!"+wires[3].name+"))");
            assign(wires[0]+" = MISC0"+numWires+"^"+wires[4].name); //S
            assign(wires[1]+" = (Cin&MISC"+numWires+")|("+wires[2].name+"&(!"+wires[3].name+"))"); //Cout
        }else if(part instanceof BitShiftL){
            //wires = [out,in]
            
        }else if(part instanceof BitShiftR){
            //wires = [out,in]
        }
*/
    }
    public int adderBD(Part addPart, ArrayList<Wire> partWires){
        //find out how many bits, each input and output is associated with
        //some inputs/outputs will always be one bit
        //determine the highest of the two and proceed with integrating into
        //code at bottom.
        int aBit=0;
        int bBit=0;
        int bitSize=0;
        for(Wire wire : partWires){
            for(Port port : wire.ports){
                if(port.part != null){
                    if(port.part.name.equals(addPart.name)){
                        if(port.name.substring(0,1).equals("a"))
                            aBit++;
                        else if(port.name.substring(0,1).equals("b"))
                            bBit++;
                    }
                }
            }
        }
        if(aBit > bBit)
            bitSize=aBit;
        else
            bitSize=bBit;
//        Wire[] myWires = new Wire[5];
//        for(Wire wire : partWires){
//            for(Port port : wire.ports){
//                if(port.part != null){
//                    if(port.part.name.equals(addPart.name)){
//                        if(port.name.equals("a"))
//                            myWires[0] = wire;
//                        else if(port.name.equals("b"))
//                            myWires[1] = wire;
//                        else if(port.name.equals("cin"))
//                            myWires[2] = wire;
//                        else if(port.name.equals("y"))
//                            myWires[3] = wire;
//                        else if(port.name.equals("cout"))
//                            myWires[4] = wire;
//                    }
//                }
//            }
//        }
//        for(int i = 0; i < 5; i++){
//            if(myWires[i] == null){
//                currModule.addWire("MISC"+numWires,1);
//                numWires++;
//                myWires[i] = currModule.wires.get(currModule.wires.size()-1);
//            }
//        }
        //go through wires and find all wires/ports associated with first bit
        //associate them with certain inputs and outputs on the adders
        //run through code below for first bit, find all wires/ports associated
        //with second bit and run through code below for second bit, etc.
        Wire[] myWires = new Wire[5];
        for(int i = 0; i < bitSize; i++){   
            for(Wire wire : partWires){
            for(Port port : wire.ports){
                if(port.part != null){
                    if(port.part.name.equals(addPart.name)){
                        if(port.name.equals("a-"+i))
                            myWires[0] = wire;
                        else if(port.name.equals("b-"+i))
                            myWires[1] = wire;
                        else if(port.name.equals("cin-"+i) && i==0)
                            myWires[2] = wire;
                        else if(port.name.equals("cin-"+i) && i!=0)
                            myWires[2] = myWires[4];
                        else if(port.name.equals("y-"+i))
                            myWires[3] = wire;
                        else if(port.name.equals("cout-"+i))
                            myWires[4] = wire;
                        }
                    }
                }
            }
            for(int j = 0; j < 5; j++){
                if(myWires[j] == null){
                    currModule.addWire("MISC"+numWires,1);
                    numWires++;
                    myWires[j] = currModule.wires.get(currModule.wires.size()-1);
                }
            }
            String[] newWires = {"MISC"+numWires,"MISC"+(numWires+1),"MISC"+(numWires+2)};
            currModule.addWire("MISC"+numWires,1); numWires++;
            currModule.addWire("MISC"+numWires,1); numWires++;
            currModule.addWire("MISC"+numWires,1); numWires++;
            assign(newWires[0]+" = "+myWires[0].name+" ^ "+myWires[1].name+";");
            assign(myWires[3].name+" = "+newWires[0]+" ^ "+myWires[2].name+";");
            assign(newWires[1]+" = "+newWires[0]+" & "+myWires[2].name+";");
            assign(newWires[2]+" = "+myWires[0].name+" & "+myWires[1].name+";");
            assign(myWires[4].name+" = "+newWires[1]+" | "+newWires[2]+";");
        }
        return 5;
    }
}

