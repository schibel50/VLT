/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vlt;

import java.util.ArrayList;
import java.lang.String;
/**
 *
 * @author Parker
 */
public class Compiler2 {
    public ArrayList<String> code;//all the code being evaluated
    public ArrayList<Module> modules;//all of the modules being evaluated
    //names of pre-defined modules
    public String[] preDefinedMods = {"and","or","xor","nand","nor","xnor","buf","not","bufif1","bufif0","notif1","notif0","DFF"};
    public Module currModule;//the current module being evaluated
    public EWriter edif;//where to write the EDIF
    //all of the possible operators
    public String[] operators = {"+","-",">=","<=",">","<","==","!=","!","?",":","*","&","|","^","~","&&","||","~&","~|","~^","^~",">>","<<","%","/","=","1","0","(",")","{","}"};
    public String[] letters = {"A","B","C","D","E","F"};

    //keep track of the number of wires and each component/part
    public int numWires;
    public int numAdders;
    public int numSubtractors;
    public int numComparators;
    public int numMuxs;
    public int numInverters;
    public int numOR2s;
    public int numOR3s;
    public int numOR4s;
    public int numAND2s;
    public int numAND3s;
    public int numAND4s;
    public int numXOR2s;
    public int numNAND2s;
    public int numNAND3s;
    public int numNAND4s;
    public int numNOR2s;
    public int numNOR3s;
    public int numNOR4s;
    public int numXNOR2s;
    public int numXSANDs;
    public int numXSORs;
    public int numMultipliers;
    public int numDividers;
    public int numConcats;
    public int numBuffs;
    public int numBSRs;
    public int numBSLs;
    public int numSplitters;
    public int numCombiners;
    public int numExtras;
    public int numExtraWires;
    public int numDFFs;
    public int numTSBs;
    
    //wires that lead to ground, vcc, or indicate a floating pin
    public Wire GND;
    public Wire VCC;
    public Wire FLOAT;
    
    public Compiler2(ArrayList<String> code){
        //initialize everything
        this.code=code;
        this.modules = new ArrayList<Module>();
        numWires=0;
        numAdders=0;
        numSubtractors=0;
        numComparators=0;
        numMuxs=0;
        numInverters=0;
        numOR2s=0;
        numOR3s=0;
        numOR4s=0;
        numAND2s=0;
        numAND3s=0;
        numAND4s=0;
        numXOR2s=0;
        numNAND2s=0;
        numNAND3s=0;
        numNAND4s=0;
        numNOR2s=0;
        numNOR3s=0;
        numNOR4s=0;
        numXNOR2s=0;
        numXSANDs=0;
        numXSORs=0;
        numMultipliers=0;
        numDividers=0;
        numConcats=0;
        numBuffs=0;
        numBSRs=0;
        numBSLs=0;
        numSplitters=0;
        numCombiners=0;
        numExtras=0;
        numExtraWires=0;
        numDFFs=0;
        numBuffs=0;
        numTSBs=0;
    }
    
    /*
    This method finds the name of each module, its IO names,
    and the lines of code associated with the module
    */
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
                    for(int k = 0; k < tempIO.length; k++)
                        tempIO[k] = tempIO[k].trim();
                }
                if(code.get(i).length() > 8){
                    if(code.get(i).substring(0,9).equals("endmodule")){
                        end = i;
                    }
                }
                tempName=tempName.trim();
                if(start != -1 && end != -1 && tempIO != null && !(tempName.equals("dff"))){
                    modules.add(new Module(tempName,start,end,tempIO));
                    tempName = "";
                    start = -1;
                    end = -1;
                    tempIO = null;
                }
                else if(tempName.equals("DFF")){
                    tempName="";
                    start=-1;
                    end=-1;
                    tempIO=null;
                }
            }
        }
    }
    
    /*
    This method compiles the actual code of each module
    */
    public void compile(){
        modules.get(0).wires.add(new Wire("gnd",1));
        modules.get(0).wires.get(modules.get(0).wires.size()-1).ports.add(new Port("GND",(byte)1));
        GND = modules.get(0).wires.get(modules.get(0).wires.size()-1);
        modules.get(0).wires.add(new Wire("vcc",1));
        modules.get(0).wires.get(modules.get(0).wires.size()-1).ports.add(new Port("VCC",(byte)1));
        VCC = modules.get(0).wires.get(modules.get(0).wires.size()-1);
        modules.get(0).wires.add(new Wire("float",1));
        modules.get(0).wires.get(modules.get(0).wires.size()-1).ports.add(new Port("FLOAT",(byte)1));
        FLOAT = modules.get(0).wires.get(modules.get(0).wires.size()-1);
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
                            else{
                                String[] temp = code.get(i).substring(6).split("\\,");
                                for(int k = 0; k < temp.length; k++)
                                    temp[k] = temp[k].trim();
                                if(temp[temp.length-1].charAt(temp[temp.length-1].length()-1)==';');
                                    temp[temp.length-1]=temp[temp.length-1].substring(0,temp[temp.length-1].length()-1);
                                currModule.addInputs(temp,1);
                            }
                        }
                        else
                            ioHelper(code.get(i),1);
                    }
                    //add all the outputs
                    else if(code.get(i).substring(0,6).equals("output") && code.get(i).length() > 5){
                        if(code.get(i).substring(6,7).equals(" ")){
                            if(code.get(i).charAt(7)=='[')
                                ioHelper(code.get(i),2);
                            else{
                                String[] temp = code.get(i).substring(7).split("\\,");
                                for(int k = 0; k < temp.length; k++)
                                    temp[k] = temp[k].trim();
                                if(temp[temp.length-1].charAt(temp[temp.length-1].length()-1)==';');
                                    temp[temp.length-1]=temp[temp.length-1].substring(0,temp[temp.length-1].length()-1);
                                currModule.addOutputs(temp,1);
                            }
                        }
                        else
                            ioHelper(code.get(i),2);
                    }
                    //add all the predefined wires and regs
                    //TEST
                    else if(code.get(i).substring(0,4).equals("wire") && code.get(i).length() > 4){
                        if(code.get(i).substring(4,5).equals(" ")){
                            if(code.get(i).charAt(5)=='[')
                                ioHelper(code.get(i),3);
                            else{
                                String[] temp = code.get(i).substring(5).split("\\,");
                                int gnd = 0;
                                int vcc = 0;
                                for(int k = 0; k < temp.length; k++){
                                    temp[k] = temp[k].trim();
                                    if(temp[k].equals("gnd")||temp[k].equals("ground"))
                                        gnd++;
                                    else if(temp[k].equals("vcc"))
                                        vcc++;
                                }
                                if(gnd==0&&vcc==0){
                                    if(temp[temp.length-1].charAt(temp[temp.length-1].length()-1)==';');
                                        temp[temp.length-1]=temp[temp.length-1].substring(0,temp[temp.length-1].length()-1);
                                    currModule.addWires(temp,1);
                                }
                            }
                        }
                        else
                            ioHelper(code.get(i),3);
                    }
                    else if(code.get(i).substring(0,3).equals("reg") && code.get(i).length() > 3){
                        if(code.get(i).substring(3,4).equals(" ")){
                            if(code.get(i).charAt(4)=='[')
                                ioHelper(code.get(i),3);
                            else{
                                String[] temp = code.get(i).substring(4).split("\\,");
                                for(int k = 0; k < temp.length; k++)
                                    temp[k] = temp[k].trim();
                                if(temp[temp.length-1].charAt(temp[temp.length-1].length()-1)==';');
                                    temp[temp.length-1]=temp[temp.length-1].substring(0,temp[temp.length-1].length()-1);
                                currModule.addWires(temp,1);
                            }
                        }
                        else
                            ioHelper(code.get(i),3);
                    }
                    //assign all the predefined wires
                    else if(code.get(i).substring(0,6).equals("assign") && code.get(i).length() > 5){
                        if(code.get(i).substring(6,7).equals(" "))
                            assign(code.get(i).substring(7));
                    }
                    //compile an 'always' block
                    //compile an 'always' block
                    else if(code.get(i).substring(0,6).equals("always")){
                        ArrayList<String> myAlways = new ArrayList<String>();
                        int count=0;
                        myAlways.add(code.get(i));
                        i++;
                       do{
                            if(!code.get(i).isEmpty()){
                                if((code.get(i).length() > 2)&&(code.get(i).length() < 7)){
                                    if(code.get(i).substring(0,3).equals("end"))
                                    count--;
                                }
                                if(code.get(i).length() > 4){
                                    if(code.get(i).substring(0,5).equals("begin"))
                                    count++;
                                }

                                myAlways.add(code.get(i));
                            }
                            i++;
                        }while(count>0);
                       
                        always(myAlways);
                    }
                    //if the line matches nothing, make sure it isn't another module
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
                                    if(tempPorts[l] == null||tempPorts[l].equals("")||tempPorts[l].equals(" "))
                                        tempPorts[l] = "float";
                                    tempPorts[l] = tempPorts[l].trim();
                                }
                                currModule.parts.add(new Module_Part("MOD_"+modName,tempPorts));
                            }
                        }
                        for(int k = 0; k < preDefinedMods.length; k++){
                            if(preDefinedMods[k].equals(modName)){
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
                                preMod(tempPorts,k);
                            }
                        }
                    }
                }
            }
        }
        currModule = modules.get(0);
        insertMod(currModule);
        if(!GND.ports.get(0).name.equals("GND"))
            GND.ports.add(0,new Port("GND",(byte)1));
        if(!VCC.ports.get(0).name.equals("VCC")){
            VCC.ports.add(0,new Port("VCC",(byte)1));
        }
//        part2gate();
        redundantIOPorts();
        for(int i=1;i<currModule.wires.get(2).ports.size();){
            Wire temp = newWire();
            temp.ports.add(new Port(currModule.wires.get(2).ports.get(i)));
            currModule.wires.get(2).ports.remove(i);
        }
        modules.get(0).wires.remove(2);
        VCC VccPart = new VCC("VCC$1");
        Ground GndPart = new Ground("GND$1");
        currModule.parts.add(0,VccPart);
        currModule.parts.add(0,GndPart);
        currModule.wires.get(0).ports.remove(0);
        currModule.wires.get(0).ports.add(0,GndPart.ports.get(0));
        currModule.wires.get(1).ports.remove(0);
        currModule.wires.get(1).ports.add(0,VccPart.ports.get(0));
        if(currModule.wires.get(0).ports.size()==1){
            currModule.wires.remove(0);
            currModule.parts.remove(0);
        }
        else if(currModule.wires.get(1).ports.size()==1){
            currModule.wires.remove(1);
            currModule.parts.remove(1);
        }
        if(currModule.wires.get(0).name.equals("vcc")){
            if(currModule.wires.get(0).ports.size()==1){
                currModule.wires.remove(0);
                currModule.parts.remove(0);
            }
        }
//        addBuffers();
        edif = new EWriter(modules.get(0));
        edif.write();
    }
    
    public String newName(String name){
        String temp = name;
        int a =0;
        while(temp.charAt(a)!='$')
            a++;
        temp = temp.substring(0,a+1);
        if(name.contains("NAND2$")){
            temp = temp + ((numNAND2s/6)+1) + letters[numNAND2s%6];
            numNAND2s++;
        }
        else if(name.contains("NAND3$")){
            temp = temp + ((numNAND3s/6)+1) + letters[numNAND3s%6];
            numNAND3s++;
        }
        else if(name.contains("NAND4$")){
            temp = temp + ((numNAND4s/6)+1) + letters[numNAND4s%6];
            numNAND4s++;
        }
        else if(name.contains("AND2$")){
            temp = temp + ((numAND2s/6)+1) + letters[numAND2s%6];
            numAND2s++;
        }
        else if(name.contains("AND3$")){
            temp = temp + ((numAND3s/6)+1) + letters[numAND3s%6];
            numAND3s++;
        }
        else if(name.contains("AND4$")){
            temp = temp + ((numAND4s/6)+1) + letters[numAND4s%6];
            numAND4s++;
        }
        else if(name.contains("NOR2$")){
            temp = temp + ((numNOR2s/6)+1) + letters[numNOR2s%6];
            numNOR2s++;
        }
        else if(name.contains("NOR3$")){
            temp = temp + ((numNOR3s/6)+1) + letters[numNOR3s%6];
            numNOR3s++;
        }
        else if(name.contains("NOR4$")){
            temp = temp + ((numNOR4s/6)+1) + letters[numNOR4s%6];
            numNOR4s++;
        }
        else if(name.contains("OR2$")){
            temp = temp + ((numOR2s/6)+1) + letters[numOR2s%6];
            numOR2s++;
        }
        else if(name.contains("OR3$")){
            temp = temp + ((numOR3s/6)+1) + letters[numOR3s%6];
            numOR3s++;
        }
        else if(name.contains("OR4$")){
            temp = temp + ((numOR4s/6)+1) + letters[numOR4s%6];
            numOR4s++;
        }
        else if(name.contains("XOR2$")){
            temp = temp + ((numXOR2s/6)+1) + letters[numXOR2s%6];
            numXOR2s++;
        }
        else if(name.contains("XNOR2$")){
            temp = temp + ((numXNOR2s/6)+1) + letters[numXNOR2s%6];
            numXNOR2s++;
        }
        else if(name.contains("BUFF$")){
            temp = temp + ((numBuffs/6)+1) + letters[numBuffs%6];
            numBuffs++;
        }
        else if(name.contains("INV$")){
            temp = temp + ((numInverters/6)+1) + letters[numInverters%6];
            numInverters++;
        }
        else if(name.contains("DFF$")){
            temp = temp + ((numDFFs/2)+1) + letters[numDFFs%2];
            numDFFs++;
        }
        else if(name.contains("TSB$")){
            temp=temp+((numTSBs/2)+1)+letters[numTSBs%2];
            numTSBs++;
        }
        else{
            temp = temp+numExtras;
            numExtras++;
        }
        return temp;
    }
    
    public void preMod(String[] myPorts, int myMod){
        String type="";
        Wire[] myWires = new Wire[myPorts.length];
        for(int i=0;i<myPorts.length;i++){
            if(!(myPorts[i].equals("")||myPorts[i].equals(" "))){
                if(myPorts[i].charAt(myPorts[i].length()-1)==']'){
                    myPorts[i]=myPorts[i].replace('[', '_');
                    myPorts[i] = myPorts[i].substring(0,myPorts[i].length()-1);
                }
            }
        }
        for(int i = 0; i < myPorts.length; i++){
            for(Wire wire : currModule.wires){
                if(wire.name.equals(myPorts[i])){
                    myWires[i]=wire;
                }
                else if(myPorts[i].equals("ground")||myPorts[i].equals("gnd")){
                    myWires[i]=GND;
                }
                else if(myPorts[i].equals("vcc")){
                    myWires[i]=VCC;
                }
            }
        }
        for(int i=0;i<myPorts.length;i++){
            if(myWires[i]==null){
                myWires[i]=FLOAT;
            }
        }
        if(myMod==0){
            if(myWires.length==3)
                assign(myWires[0].name+" = "+myWires[1].name+ " & "+myWires[2].name+";");
            else if(myWires.length==4){
                AND3 myAND3 = new AND3(newName("AND3$"));
                currModule.parts.add(myAND3);
                myWires[0].ports.add(myAND3.ports.get(3));
                myWires[1].ports.add(myAND3.ports.get(0));
                myWires[2].ports.add(myAND3.ports.get(1));
                myWires[3].ports.add(myAND3.ports.get(2));
            }
            else if(myWires.length==5){
                AND4 myAND4 = new AND4(newName("AND4$"));
                currModule.parts.add(myAND4);
                myWires[0].ports.add(myAND4.ports.get(4));
                myWires[1].ports.add(myAND4.ports.get(0));
                myWires[2].ports.add(myAND4.ports.get(1));
                myWires[3].ports.add(myAND4.ports.get(2));
                myWires[4].ports.add(myAND4.ports.get(3));
            }
        }
        else if(myMod==1){
            if(myWires.length==3)
                assign(myWires[0].name+" = "+myWires[1].name+ " | "+myWires[2].name+";");
            else if(myWires.length==4){
                OR3 myOR3 = new OR3(newName("OR3$"));
                currModule.parts.add(myOR3);
                myWires[0].ports.add(myOR3.ports.get(3));
                myWires[1].ports.add(myOR3.ports.get(0));
                myWires[2].ports.add(myOR3.ports.get(1));
                myWires[3].ports.add(myOR3.ports.get(2));
            }
            else if(myWires.length==5){
                OR4 myOR4 = new OR4(newName("OR4$"));
                currModule.parts.add(myOR4);
                myWires[0].ports.add(myOR4.ports.get(4));
                myWires[1].ports.add(myOR4.ports.get(0));
                myWires[2].ports.add(myOR4.ports.get(1));
                myWires[3].ports.add(myOR4.ports.get(2));
                myWires[4].ports.add(myOR4.ports.get(3));
            }
        }
        else if(myMod==2){
            assign(myWires[0].name+" = "+myWires[1].name+" ^ "+myWires[2].name+";");
        }
        else if(myMod==3){
            if(myWires.length==3)
                assign(myWires[0].name+" = "+myWires[1].name+ " ~ "+myWires[2].name+";");
            else if(myWires.length==4){
                NAND3 myNAND3 = new NAND3(newName("NAND3$"));
                currModule.parts.add(myNAND3);
                myWires[0].ports.add(myNAND3.ports.get(3));
                myWires[1].ports.add(myNAND3.ports.get(0));
                myWires[2].ports.add(myNAND3.ports.get(1));
                myWires[3].ports.add(myNAND3.ports.get(2));
            }
            else if(myWires.length==5){
                NAND4 myNAND4 = new NAND4(newName("NAND4$"));
                currModule.parts.add(myNAND4);
                myWires[0].ports.add(myNAND4.ports.get(4));
                myWires[1].ports.add(myNAND4.ports.get(0));
                myWires[2].ports.add(myNAND4.ports.get(1));
                myWires[3].ports.add(myNAND4.ports.get(2));
                myWires[4].ports.add(myNAND4.ports.get(3));
            }
        }
        else if(myMod==4){
            if(myWires.length==3)
                assign(myWires[0].name+" = "+myWires[1].name+ " ~| "+myWires[2].name+";");
            else if(myWires.length==4){
                NOR3 myNOR3 = new NOR3(newName("NOR3$"));
                currModule.parts.add(myNOR3);
                myWires[0].ports.add(myNOR3.ports.get(3));
                myWires[1].ports.add(myNOR3.ports.get(0));
                myWires[2].ports.add(myNOR3.ports.get(1));
                myWires[3].ports.add(myNOR3.ports.get(2));
            }
            else if(myWires.length==5){
                NOR4 myNOR4 = new NOR4(newName("NOR4$"));
                currModule.parts.add(myNOR4);
                myWires[0].ports.add(myNOR4.ports.get(4));
                myWires[1].ports.add(myNOR4.ports.get(0));
                myWires[2].ports.add(myNOR4.ports.get(1));
                myWires[3].ports.add(myNOR4.ports.get(2));
                myWires[4].ports.add(myNOR4.ports.get(3));
            }
        }
        else if(myMod==2){
            assign(myWires[0].name+" = "+myWires[1].name+" ~^ "+myWires[2].name+";");
        }
        else if(myMod==6){
            Buffer newBuff = new Buffer(newName("BUFF$"));
            myWires[myWires.length-1].ports.add(newBuff.ports.get(0));
            for(int i = 0; i < myWires.length-1;i++)
                myWires[i].ports.add(newBuff.ports.get(1));
            currModule.parts.add(newBuff);
        }
        else if(myMod==7){
            Inverter newInv = new Inverter(newName("INV$"));
            myWires[myWires.length-1].ports.add(newInv.ports.get(0));
            for(int i = 0; i < myWires.length-1;i++)
                myWires[i].ports.add(newInv.ports.get(1));
            currModule.parts.add(newInv);
        }
        else if(myMod==8){
            TSB newTSB = new TSB(newName("TSB$"));
            myWires[myWires.length-1].ports.add(newTSB.ports.get(0));
            for(int i = 0; i < myWires.length-1;i++)
                myWires[i].ports.add(newTSB.ports.get(1));
            currModule.parts.add(newTSB);
        }
//        else if(myMod==8){
//            Buffer newBuff = new Buffer("Buff"+numBuffs);
//            numBuffs++;
//            AND newAnd = new AND("AND"+numANDs);
//            numANDs++;
//            currModule.parts.add(newBuff);
//            currModule.parts.add(newAnd);
//        }
//        else if(myMod==9){
//            
//        }
        else if(myMod==12){
            DFF newDff = new DFF(newName("DFF$"));
            for(int i=0;i<myWires.length;i++)
                myWires[i].ports.add(newDff.ports.get(i));
            currModule.parts.add(newDff);
        }
    }
    
    public void redundantIOPorts(){
        for(int i=0;i<currModule.wires.size();i++){
            for(int j=0;j<currModule.wires.get(i).ports.size();j++){
                if(currModule.wires.get(i).ports.get(j).part==null){
                    if(!currModule.wires.get(i).ports.get(j).name.equals(currModule.wires.get(i).name)){
                        for(int k=0;k<currModule.wires.size();k++){
                            if(currModule.wires.get(i).ports.get(j).name.equals(currModule.wires.get(k).name)){
                                for(int l=0;l<currModule.wires.get(i).ports.size();l++){
                                    if(currModule.wires.get(i).ports.get(l).part!=null)
                                        currModule.wires.get(k).ports.add(currModule.wires.get(i).ports.get(l));
                                }
                                currModule.wires.remove(i);
                                i--;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void addBuffers(){
        Buffer currBuff;
        Wire currWire;
        for(int i=0;i<currModule.wires.size();i++){
            for(int j=0;j<currModule.wires.get(i).ports.size();j++){
                if(currModule.wires.get(i).ports.get(j).part==null&&
                        currModule.wires.get(i).ports.get(j).IO==(byte)1&&
                        currModule.wires.get(i).ports.get(j).name.equals(currModule.wires.get(i).name)){
                    currBuff = new Buffer(newName("BUFF$"));
                    currWire = newWire();
                    currModule.parts.add(currBuff);
                    currWire.ports.add(currBuff.ports.get(1));
                    for(int k=0;k<currModule.wires.get(i).ports.size();k++){
                        if(currModule.wires.get(i).ports.get(k).part!=null){
                           currWire.ports.add(currModule.wires.get(i).ports.get(k));
                           currModule.wires.get(i).ports.remove(k);
                           k--;
                        }
                    }
                    currModule.wires.get(i).ports.add(currBuff.ports.get(0));
                }
            }
        }
    }
    
    public void insertMod(Module module){
        for(int i=0;i<module.parts.size();i++){
            if(module.parts.get(i).name.contains("MOD_")){
                Module tempMod = null;
                for(int j=0;j<modules.size();j++){
                    if(modules.get(j).name.equals(module.parts.get(i).name.substring(4))){
                        tempMod = new Module("temp",-1,-1,modules.get(j).ioNames);
                        for(Wire wire : modules.get(j).wires){
                            Wire tempW = new Wire(wire);
                            tempMod.wires.add(tempW);
                        }
                        for(Part part : modules.get(j).parts){
                            Part tempP = new Part(part);
                            tempMod.parts.add(tempP);
                        }
                    }
                }
                for(int j=0;j<tempMod.parts.size();j++){
                    if(tempMod.parts.get(j).name.contains("MOD_")){
                        insertMod(tempMod);
                    }
                }
                ArrayList<Wire[]> myWires = new ArrayList<Wire[]>();
                for(int j=0;j<module.parts.get(i).ports.size();j++){
                    if(module.parts.get(i).ports.get(j).name.endsWith("]")){
                        Wire[] temp = new Wire[1];
                        module.parts.get(i).ports.get(j).name = module.parts.get(i).ports.get(j).name.replace('[','_');
                        module.parts.get(i).ports.get(j).name = module.parts.get(i).ports.get(j).name.substring(0,module.parts.get(i).ports.get(j).name.length()-1);
                        for(Wire wire : module.wires){
                            if(wire.name.equals(module.parts.get(i).ports.get(j).name))
                                temp[0]=wire;
                        }
                        myWires.add(j,temp);
                    }
                    else{
                        int wordSize=module.parts.get(i).ports.get(j).name.length();
                        int size=0;
                        for(Wire wire : module.wires){
                            if(wire.name.length()>=wordSize){
                                    if(wire.name.length()>wordSize&&wire.name.substring(0,wordSize).equals(module.parts.get(i).ports.get(j).name)){
                                        if(wire.name.charAt(wordSize)=='_')
                                        size++;
                                    }
                                    else if(wire.name.length()==wordSize&&wire.name.equals(module.parts.get(i).ports.get(j).name))
                                        size++;
                            }
                        }
                        if(size==1){
                            Wire[] temp = new Wire[1];
                            for(Wire wire : module.wires){
                                if(wire.name.equals(module.parts.get(i).ports.get(j).name)){
                                    temp[0]=wire;
                                }
                            }
                            myWires.add(j,temp);
                        }
                        else{
                            Wire[] temp = new Wire[size];
                            for(Wire wire : module.wires){
                                if(wire.name.length()>=wordSize){
                                    if(wire.name.length()>wordSize&&wire.name.substring(0,wordSize).equals(module.parts.get(i).ports.get(j).name)){
                                        if(wire.name.charAt(wordSize)=='_')
                                        temp[Integer.parseInt(wire.name.substring(wordSize+1))]=wire;
                                    }
                                    else if(wire.name.length()==wordSize&&wire.name.equals(module.parts.get(i).ports.get(j).name))
                                        temp[Integer.parseInt(wire.name.substring(wordSize+1))]=wire;
                                }
                            }
                            myWires.add(j,temp);
                        }
                    }
                }
                for(int j=0;j<tempMod.parts.size();j++){
                    String currName = tempMod.parts.get(j).name;
                    if(!tempMod.parts.get(j).name.substring(0,3).equals("MOD"))
                        tempMod.parts.get(j).name = newName(tempMod.parts.get(j).name);
                    for(Wire wire : tempMod.wires){
                        for(int k=0;k<wire.ports.size();k++){
                            if(wire.ports.get(k).part==null){
                                wire.ports.remove(k);
                                k--;
                            }
                            else if(wire.ports.get(k).part.name.equals(currName)){
                                Part temp = new Part(tempMod.parts.get(j));
                                wire.ports.get(k).part=temp;
                            }
                            else{
                                Part temp = new Part(wire.ports.get(k).part);
                                wire.ports.get(k).part=temp;
                            }
                        }
                    }
                    Part temp = new Part(tempMod.parts.get(j));
                    module.parts.add(temp);
                }
                ArrayList<Wire[]> modWires = new ArrayList<Wire[]>();
                for(int j=0;j<tempMod.ioNames.length;j++){
                    int wordSize=tempMod.ioNames[j].length();
                    int size=0;
                    for(Wire wire : tempMod.wires){
                        if(wire.name.length()>=wordSize){
                            if(wire.name.substring(0,wordSize).equals(tempMod.ioNames[j]))
                                size++;
                        }
                    }
                    if(size==1){
                        Wire[] temp = new Wire[1];
                        for(Wire wire : tempMod.wires){
                            if(wire.name.equals(tempMod.ioNames[j])){
                                temp[0]=wire;
                            }
                        }
                        modWires.add(j,temp);
                    }
                    else{
                        Wire[] temp = new Wire[size];
                        for(Wire wire : tempMod.wires){
                            if(wire.name.length()>=wordSize){
                                if(wire.name.substring(0,wordSize).equals(tempMod.ioNames[j])){
                                    temp[Integer.parseInt(wire.name.substring(wordSize+1))]=wire;
                                }
                            }
                        }
                        modWires.add(j,temp);
                    }
                }
                for(int j=0;j<modWires.size();j++){
                    for(int k=0;k<modWires.get(j).length;k++){
                        for(int l=0;l<tempMod.wires.size();l++){
                            if(tempMod.wires.get(l).name.equals(modWires.get(j)[k].name))
                                tempMod.wires.remove(l);
                        }
                        if(!myWires.get(j)[0].name.equals("float")){
                            for(Port port : modWires.get(j)[k].ports){
                                Port temp = new Port(port);
                                myWires.get(j)[k].ports.add(temp);
                            }
                        }
                    }
                }
                for(Wire wire : tempMod.wires){
                    Wire temp = new Wire(wire);
                    if(temp.name.equals("gnd")||temp.name.equals("ground")||temp.name.equals("GND")){
                        for(Port port : temp.ports){
                            Port temp2 = new Port(port);
                            modules.get(0).wires.get(0).ports.add(temp2);
                        }
                    }
                    else if(temp.name.equals("vcc")||temp.name.equals("VCC")){
                        for(Port port : temp.ports){
                            Port temp2 = new Port(port);
                            modules.get(0).wires.get(1).ports.add(temp2);
                        }
                    }
                    else{
                        temp.name="MISC_"+numWires;numWires++;
                        module.wires.add(temp);
                    }
                }
                module.parts.remove(i);
                i--;
            }
        }
    }
    
//    public void insertMod(Module module){
//        for(int i=0;i<module.parts.size();i++){
//            if(module.parts.get(i).name.contains("MOD_")){
//                if(module.parts.get(i).name.contains("enableLogic")){
//                    System.out.println();
//                }
//                ArrayList<Wire> newWires = new ArrayList<Wire>();
//                ArrayList<Part> newParts = new ArrayList<Part>();
//                String[] modIOs = null;
//                for(int j=0;j<modules.size();j++){
//                    if(modules.get(j).name.equals(module.parts.get(i).name.substring(4))){
//                        modIOs=modules.get(j).ioNames;
//                        for(Wire wire : modules.get(j).wires){
//                            Wire tempW = new Wire(wire);
//                            newWires.add(tempW);
//                        }
//                        for(Part part : modules.get(j).parts){
//                            Part temp = new Part(part);
//                            newParts.add(temp);
//                        }
//                    }
//                }
//                ArrayList<Wire[]> myWires = new ArrayList<Wire[]>();
//                for(int j=0;j<module.parts.get(i).ports.size();j++){
//                    if(module.parts.get(i).ports.get(j).name.endsWith("]")){
//                        Wire[] temp = new Wire[1];
//                        module.parts.get(i).ports.get(j).name = module.parts.get(i).ports.get(j).name.replace('[','_');
//                        module.parts.get(i).ports.get(j).name = module.parts.get(i).ports.get(j).name.substring(0,module.parts.get(i).ports.get(j).name.length()-1);
//                        for(Wire wire : module.wires){
//                            if(wire.name.equals(module.parts.get(i).ports.get(j).name))
//                                temp[0]=wire;
//                        }
//                        myWires.add(j,temp);
//                    }
//                    else{
//                        int wordSize=module.parts.get(i).ports.get(j).name.length();
//                        int size=0;
//                        for(Wire wire : module.wires){
//                            if(wire.name.length()>=wordSize){
//                                    if(wire.name.length()>wordSize&&wire.name.substring(0,wordSize).equals(module.parts.get(i).ports.get(j).name)){
//                                        if(wire.name.charAt(wordSize)=='_')
//                                        size++;
//                                    }
//                                    else if(wire.name.length()==wordSize&&wire.name.equals(module.parts.get(i).ports.get(j).name))
//                                        size++;
//                            }
//                        }
//                        if(size==1){
//                            Wire[] temp = new Wire[1];
//                            for(Wire wire : module.wires){
//                                if(wire.name.equals(module.parts.get(i).ports.get(j).name)){
//                                    temp[0]=wire;
//                                }
//                            }
//                            myWires.add(j,temp);
//                        }
//                        else{
//                            Wire[] temp = new Wire[size];
//                            for(Wire wire : module.wires){
//                                if(wire.name.length()>=wordSize){
//                                    if(wire.name.length()>wordSize&&wire.name.substring(0,wordSize).equals(module.parts.get(i).ports.get(j).name)){
//                                        if(wire.name.charAt(wordSize)=='_')
//                                        temp[Integer.parseInt(wire.name.substring(wordSize+1))]=wire;
//                                    }
//                                    else if(wire.name.length()==wordSize&&wire.name.equals(module.parts.get(i).ports.get(j).name))
//                                        temp[Integer.parseInt(wire.name.substring(wordSize+1))]=wire;
//                                }
//                            }
//                            myWires.add(j,temp);
//                        }
//                    }
//                }
//                for(int j=0;j<newParts.size();j++){
//                    String currName = newParts.get(j).name;
//                    if(!newParts.get(j).name.substring(0,3).equals("MOD"))
//                        newParts.get(j).name = newParts.get(j).name+"_"+numExtras;numExtras++;
//                    for(Wire wire : newWires){
//                        for(int k=0;k<wire.ports.size();k++){
//                            if(wire.ports.get(k).part==null){
//                                wire.ports.remove(k);
//                                k--;
//                            }
//                            else if(wire.ports.get(k).part.name.equals(currName)){
//                                Part temp = new Part(newParts.get(j));
//                                wire.ports.get(k).part=temp;
//                            }
//                            else{
//                                Part temp = new Part(wire.ports.get(k).part);
//                                wire.ports.get(k).part=temp;
//                            }
//                        }
//                    }
//                    Part temp = new Part(newParts.get(j));
//                    newParts.get(j).name = currName;
//                    module.parts.add(temp);
//                }
//                ArrayList<Wire[]> modWires = new ArrayList<Wire[]>();
//                for(int j=0;j<modIOs.length;j++){
//                    int wordSize=modIOs[j].length();
//                    int size=0;
//                    for(Wire wire : newWires){
//                        if(wire.name.length()>=wordSize){
//                            if(wire.name.substring(0,wordSize).equals(modIOs[j]))
//                                size++;
//                        }
//                    }
//                    if(size==1){
//                        Wire[] temp = new Wire[1];
//                        for(Wire wire : newWires){
//                            if(wire.name.equals(modIOs[j])){
//                                temp[0]=wire;
//                            }
//                        }
//                        modWires.add(j,temp);
//                    }
//                    else{
//                        Wire[] temp = new Wire[size];
//                        for(Wire wire : newWires){
//                            if(wire.name.length()>=wordSize){
//                                if(wire.name.substring(0,wordSize).equals(modIOs[j])){
//                                    temp[Integer.parseInt(wire.name.substring(wordSize+1))]=wire;
//                                }
//                            }
//                        }
//                        modWires.add(j,temp);
//                    }
//                }
//                
//                for(int j=0;j<modWires.size();j++){
//                    for(int k=0;k<modWires.get(j).length;k++){
//                        for(int l=0;l<newWires.size();l++){
//                            if(newWires.get(l).name.equals(modWires.get(j)[k].name))
//                                newWires.remove(l);
//                        }
//                        if(!myWires.get(j)[0].name.equals("float")){
//                            for(Port port : modWires.get(j)[k].ports){
//                                Port temp = new Port(port);
//                                myWires.get(j)[k].ports.add(temp);
//                            }
//                        }
//                    }
//                }
//                for(Wire wire : newWires){
//                    Wire temp = new Wire(wire);
//                    if(temp.name.equals("gnd")||temp.name.equals("ground")||temp.name.equals("GND")){
//                        for(Port port : temp.ports){
//                            Port temp2 = new Port(port);
//                            modules.get(0).wires.get(0).ports.add(temp2);
//                        }
//                    }
//                    else if(temp.name.equals("vcc")||temp.name.equals("VCC")){
//                        for(Port port : temp.ports){
//                            Port temp2 = new Port(port);
//                            modules.get(0).wires.get(1).ports.add(temp2);
//                        }
//                    }
//                    else{
//                        temp.name+="_"+numExtraWires;numExtraWires++;
//                        module.wires.add(temp);
//                    }
//                }
//                module.parts.remove(i);
//                i--;
//            }
//        }
//    }
    /*
    After all of the modules have been evaluated, this method inserts
    modules into the main module wherever they were evaualated in 
    the code.
    */
//    public void insertMod(Module module){
//        for(int i=0;i<module.parts.size();i++){
//            if(module.parts.get(i).name.contains("MOD_")){
//                String[] myIONames;
//                ArrayList<Wire[]> myWires = new ArrayList<Wire[]>();
//                for(int j=0;j<modules.size();j++){
//                    if(modules.get(j).name.equals(module.parts.get(i).name.substring(4))){
//                        myIONames = modules.get(j).ioNames;
//                        Module newMod = new Module("temp",-1,-1,myIONames);
//                        ArrayList<Wire> newWires = new ArrayList<Wire>();
//                        ArrayList<Part> newParts = new ArrayList<Part>();
//                        for(Wire wire : modules.get(j).wires){
//                            Wire temp = new Wire(wire);
//                            newWires.add(temp);
//                        }
//                        for(Part part : modules.get(j).parts){
//                            Part temp = new Part(part);
//                            if(!temp.name.substring(0,3).equals("MOD"))
//                                temp.name+="_"+numExtras;numExtras++;
//                            for(Wire wire : newWires){
//                                for(int k=0;k<wire.ports.size();k++){
//                                    if(wire.ports.get(k).part!=null){
//                                        if(wire.ports.get(k).part.name.equals(part.name))
//                                            wire.ports.get(k).part=temp;
//                                    }
//                                }
//                            }
//                            newParts.add(temp);
//                        }
//                        newMod.parts=newParts;
//                        newMod.wires=newWires;
//                        for(int k=0;k<module.parts.get(i).ports.size();k++){
//                            if(module.parts.get(i).ports.get(k).name.endsWith("]")){
//                                Wire[] temp = new Wire[1];
//                                module.parts.get(i).ports.get(k).name = module.parts.get(i).ports.get(k).name.replace('[','_');
//                                module.parts.get(i).ports.get(k).name = module.parts.get(i).ports.get(k).name.substring(0,module.parts.get(i).ports.get(k).name.length()-1);
//                                for(Wire wire : module.wires){
//                                    if(wire.name.equals(module.parts.get(i).ports.get(k).name))
//                                        temp[0]=wire;
//                                }
//                                myWires.add(k,temp);
//                            }
//                            else{
//                                int wordSize=module.parts.get(i).ports.get(k).name.length();
//                                int size=0;
//                                for(Wire wire : module.wires){
//                                    if(wire.name.length()>=wordSize){
//                                            if(wire.name.length()>wordSize&&wire.name.substring(0,wordSize).equals(module.parts.get(i).ports.get(k).name)){
//                                                if(wire.name.charAt(wordSize)=='_')
//                                                size++;
//                                            }
//                                            else if(wire.name.length()==wordSize&&wire.name.equals(module.parts.get(i).ports.get(k).name))
//                                                size++;
//                                    }
//                                }
//                                if(size==1){
//                                    Wire[] temp = new Wire[1];
//                                    for(Wire wire : module.wires){
//                                        if(wire.name.equals(module.parts.get(i).ports.get(k).name)){
//                                            temp[0]=wire;
//                                        }
//                                    }
//                                    myWires.add(k,temp);
//                                }
//                                else{
//                                    Wire[] temp = new Wire[size];
//                                    for(Wire wire : module.wires){
//                                        if(wire.name.length()>=wordSize){
//                                            if(wire.name.length()>wordSize&&wire.name.substring(0,wordSize).equals(module.parts.get(i).ports.get(k).name)){
//                                                if(wire.name.charAt(wordSize)=='_')
//                                                temp[Integer.parseInt(wire.name.substring(wordSize+1))]=wire;
//                                            }
//                                            else if(wire.name.length()==wordSize&&wire.name.equals(module.parts.get(i).ports.get(k).name))
//                                                temp[Integer.parseInt(wire.name.substring(wordSize+1))]=wire;
//                                        }
//                                    }
//                                    myWires.add(k,temp);
//                                }
//                            }
//                        }
////                        for(int k=0;k<newMod.parts.size();k++){
////                            for(int m=0;m<newMod.wires.size();m++){
////                                for(int l=0;l<newMod.wires.get(m).ports.size();l++){
////                                    if(newMod.wires.get(m).ports.get(l).part!=null &&
////                                            !newMod.wires.get(m).ports.get(l).part.name.equals(newMod.parts.get(k).name))
////                                        newMod.wires.get(m).ports.get(l).part=newMod.parts.get(k);
////                                }
////                            }
////                        }
//                        for(int k=0;k<newMod.parts.size();k++){
//                            Part temp = new Part(newMod.parts.get(k));
//                            module.parts.add(temp);
//                        }
//                        ArrayList<Wire[]> modWires = new ArrayList<Wire[]>();
//                        for(int k=0;k<myIONames.length;k++){
//                            int wordSize=myIONames[k].length();
//                            int size=0;
//                            for(Wire wire : newWires){
//                                if(wire.name.length()>=wordSize){
//                                    if(wire.name.substring(0,wordSize).equals(myIONames[k]))
//                                        size++;
//                                }
//                            }
//                            if(size==1){
//                                Wire[] temp = new Wire[1];
//                                for(Wire wire : newWires){
//                                    if(wire.name.equals(myIONames[k])){
//                                        temp[0]=wire;
//                                    }
//                                }
//                                modWires.add(k,temp);
//                            }
//                            else{
//                                Wire[] temp = new Wire[size];
//                                for(Wire wire : newWires){
//                                    if(wire.name.length()>=wordSize){
//                                        if(wire.name.substring(0,wordSize).equals(myIONames[k])){
//                                            temp[Integer.parseInt(wire.name.substring(wordSize+1))]=wire;
//                                        }
//                                    }
//                                }
//                                modWires.add(k,temp);
//                            }
//                        }
//                        for(int k=0;k<modWires.size();k++){
//                            for(int l=0;l<modWires.get(k).length;l++){
//                                for(int m=0;m<modWires.get(k)[l].ports.size();m++){
//                                    if(k<myWires.size())
//                                        myWires.get(k)[l].ports.add(modWires.get(k)[l].ports.get(m));
//                                    else
//                                        FLOAT.ports.add(modWires.get(k)[l].ports.get(m));
//                                }
//                            }
//                        }
//                        for(int k=0;k<newMod.wires.size();k++){
//                            boolean add=true;
//                            for(int l=0;l<myIONames.length;l++){
//                                if(newMod.wires.get(k).name.contains(myIONames[l]))
//                                    add=false;
//                            }
//                            if(add){
//                                boolean justAdd=true;
//                                for(int l=0;l<newMod.wires.get(k).ports.size();l++){
//                                    for(int m=0;m<myIONames.length;m++){
//                                        if(newMod.wires.get(k).ports.get(l).name.equals(myIONames[m])){
//                                            for(int n=0;n<newMod.wires.get(k).ports.size();n++){
//                                                if(newMod.wires.get(k).ports.get(n).part!=null)
//                                                    for(int o=0;o<myWires.get(m).length;o++){
//                                                        myWires.get(m)[o].ports.add(newMod.wires.get(k).ports.get(n));
//                                                        justAdd=false;
//                                                    }
//                                            }
//                                        }
//                                    }
//                                }
//                                if(justAdd){
//                                    Wire temp = new Wire(newMod.wires.get(k));
//                                    temp.name+="_"+numExtraWires;
//                                    numExtraWires++;
//                                    module.wires.add(temp);
//                                }
//                            }
//                        }
////                        for(int k=0;k<myWires.size();k++){
////                            for(int l=0;l<myIONames.length;l++){
////                                for(int m=0;m<myWires.get(k).length;m++){
////                                    for(int n=0;n<myWires.get(k)[m].ports.size();n++){
////                                        if(myWires.get(k)[m].ports.get(n).name.equals(myIONames[l]))
////                                            if(myWires.get(k)[m].ports.get(n).part==null)
////                                                myWires.get(k)[m].ports.remove(n);
////                                    }
////                                }
////                            }
////                        }
//                        for(int k=0;k<myWires.size();k++){
//                            for(int m=0;m<myWires.get(k).length;m++){
//                                for(int n=0;n<myWires.get(k)[m].ports.size();n++){
//                                    if(myWires.get(k)[m].ports.get(n).part==null&&!(myWires.get(k)[m].ports.get(n).name.equals(myWires.get(k)[m].name)))
//                                        myWires.get(k)[m].ports.remove(n);
//                                }
//                            }
//                        }
//                        module.parts.remove(i);
//                        newMod.clear();
//                        i--;
//                    }
//                }
//            }
//        }
//    }
    
    /*
    Whenever an input, output, or wire has more than one bit,
    this method helps create the desired number of bits and attaches
    a wire to each one
    */
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
                String[] tempI = io.substring(a).split("\\,");
                for(int k = 0; k < tempI.length; k++)
                    tempI[k] = tempI[k].trim();
                if(tempI[tempI.length-1].charAt(tempI[tempI.length-1].length()-1)==';');
                    tempI[tempI.length-1]=tempI[tempI.length-1].substring(0,tempI[tempI.length-1].length()-1);
                currModule.addInputs(tempI, finalSize);
                break;
            case 2:
                String[] tempO = io.substring(a).split("\\,");
                for(int k = 0; k < tempO.length; k++)
                    tempO[k] = tempO[k].trim();
                if(tempO[tempO.length-1].charAt(tempO[tempO.length-1].length()-1)==';');
                    tempO[tempO.length-1]=tempO[tempO.length-1].substring(0,tempO[tempO.length-1].length()-1);
                currModule.addOutputs(tempO, finalSize);
                break;
            case 3:
                String[] tempW = io.substring(a).split("\\,");
                for(int k = 0; k < tempW.length; k++)
                    tempW[k] = tempW[k].trim();
                if(tempW[tempW.length-1].charAt(tempW[tempW.length-1].length()-1)==';');
                    tempW[tempW.length-1]=tempW[tempW.length-1].substring(0,tempW[tempW.length-1].length()-1);
                currModule.addWires(tempW, finalSize);
                break;
        }
    }
    
     /*
    This method handles any assign statement in a module
    */
    public String assign(String statement){
        //break up each expression and connect to operator
        ArrayList<String> myStatements = new ArrayList<String>();
        //get to the start of the right side of the equation
        //go through the right side of the equation and add to the Arraylist
        //until the ; is reached
        String temp = null;
        int a=0;
        int notOps=0;
        while(statement.charAt(a)!=';'){
            switch(statement.charAt(a)){
                case ' ':
                    if(temp != null){
                        myStatements.add(temp);
                        temp=null;
                        notOps++;
                    }
                    break;
                case '=':
                    myStatements.add(""+statement.charAt(a));
                    break;
                case '(':
                    myStatements.add("" + statement.charAt(a));
                    break;
                case ')':
                    if(temp!=null){
                        myStatements.add(temp);
                        temp=null;
                        notOps++;
                    }
                    myStatements.add("" + statement.charAt(a));
                    break;
                case '{':
                    myStatements.add("" + statement.charAt(a));
                    break;
                case '}': 
                    if(temp!=null){
                        myStatements.add(temp);
                        temp=null;
                        notOps++;
                    }
                    myStatements.add("" + statement.charAt(a));
                    break;
                case ',':
                    if(temp!=null){
                        myStatements.add(temp);
                        temp=null;
                        notOps++;
                    }
                    break;
                case '>':
                    if(statement.charAt(a+1)=='='){
                        myStatements.add(statement.substring(a,a+2));
                        a++;
                    }
                    else{
                        myStatements.add("" + statement.charAt(a));
                    }
                    break;
                case '<':
                    if(statement.charAt(a+1)=='='){
                        myStatements.add(statement.substring(a,a+2));
                        a++;
                    }
                    else{
                        myStatements.add("" + statement.charAt(a));
                    }
                    break;
                case '?':
                    myStatements.add("" + statement.charAt(a));
                    break;
                case ':':
                    myStatements.add("" + statement.charAt(a));
                    break;
                case '!':
                    myStatements.add("" + statement.charAt(a));
                    break;
                case '~':
                    myStatements.add("" + statement.charAt(a));
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
        }
        if(!(myStatements.get(2).equals("supply1")||myStatements.get(2).equals("supply0"))){
            ArrayList<Wire[]> wireSpots = new ArrayList<Wire[]>();
            for(int i=0;i<myStatements.size();i++){
                if(myStatements.get(i).charAt(myStatements.get(i).length()-1)==']'){
                    wireSpots.add(new Wire[1]);
                    int d = 0;
                    while(myStatements.get(i).charAt(d)!= '[')
                        d++;
                    int spot = Integer.parseInt(myStatements.get(i).substring(d+1, myStatements.get(i).length()-1));
                    for(Wire wire : currModule.wires){
                        if(wire.name.length()>=myStatements.get(i).length()-2){
                            if(wire.name.equals(myStatements.get(i).substring(0,d)+"_"+spot)){
                                wireSpots.get(wireSpots.size()-1)[0]=wire;
                                myStatements.remove(i);
                                myStatements.add(i,""+(wireSpots.size()-1));
                                break;
                            }
                        }
                    }
                }
                else{
                    boolean wireYes = true;
                    for(String op : operators){
                        if(myStatements.get(i).equals(op)){
                            wireYes = false;
                        }
                    }
                    if(wireYes){
                        int size=0;
                        for(Wire wire : currModule.wires){
                            if(wire.name.length()==myStatements.get(i).length()){
                                if(myStatements.get(i).equals(wire.name.substring(0,myStatements.get(i).length()))){
                                    size++;
                                }
                            }
                            else if(wire.name.length()>myStatements.get(i).length()){
                                if(myStatements.get(i).equals(wire.name.substring(0,myStatements.get(i).length()))){
                                    if(wire.name.charAt(myStatements.get(i).length())=='_')
                                        size++;
                                    else if(size==0)
                                        size++;
                                }
                            }
                        }
                        wireSpots.add(new Wire[size]);
                        if(size>1){
                            for(Wire wire : currModule.wires){
                                if(wire.name.length()>=myStatements.get(i).length()){
                                    if(wire.name.substring(0, myStatements.get(i).length()).equals(myStatements.get(i))){
                                        int d = 0;
                                        while(wire.name.charAt(d)!= '_')
                                            d++;
                                        int spot = Integer.parseInt(wire.name.substring(d+1));
                                        wireSpots.get(wireSpots.size()-1)[spot]=wire;
            //                            wireSpots[arraySpot][spot]=wire;
                                    }
                                }
                            }
                            myStatements.remove(i);
                            myStatements.add(i,""+(wireSpots.size()-1));
    //                        arraySpot++;
                        }
                        else{
                            for(Wire wire : currModule.wires){
                                if(wire.name.length()>=myStatements.get(i).length()){
                                    if(wire.name.equals(myStatements.get(i))){
                                        wireSpots.get(wireSpots.size()-1)[0]=wire;
                                        myStatements.remove(i);
                                        myStatements.add(i,""+(wireSpots.size()-1));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return evaluate(myStatements,wireSpots);
        }
        return "";
    }
    
    
    public String evaluate(ArrayList<String> myStatements, ArrayList<Wire[]> wireSpots){
        ArrayList<String> tempList = new ArrayList<String>();
        for(int i=0;i<myStatements.size();i++){
            if(myStatements.get(i).equals("(")){
                myStatements.remove(i);
                if(myStatements.get(i-1).equals("!")||myStatements.get(i-1).equals("~")){
                    tempList.add(myStatements.get(i-1));
                    myStatements.remove(i-1);
                    i--;
                }
                int there = 0;
                while(!myStatements.get(i).equals(")") || there!=0){
                    if(myStatements.get(i).equals("("))
                        there++;
                    if(there!=0&&myStatements.get(i).equals(")"))
                        there--;
                    tempList.add(myStatements.get(i));
                    myStatements.remove(i);
                }
                myStatements.remove(i);
                int currSize=tempList.size();
                boolean turnary = false;
                if(i<myStatements.size()){
                    if(myStatements.get(i).equals("?"))
                        turnary=true;
                }
                else{
                    for(int l=0;l<currSize;l++){
                        if(tempList.get(l).equals(">=")||tempList.get(l).equals("<=")||
                           tempList.get(l).equals(">")||tempList.get(l).equals("<")||
                           tempList.get(l).equals("==")||tempList.get(l).equals("!="))
                            turnary=true;
                    }
                }
                if(turnary){
                    while(!myStatements.get(i).equals("?"))
                        i++;
                    for(int j=0;j<4;j++){
                        tempList.add(myStatements.get(i));
                        myStatements.remove(i);
                    }
                }
                myStatements.add(i,parens(tempList,wireSpots));
                for(int j=i+1;j<myStatements.size();j++){
                    try{
                        int newNum = Integer.parseInt(myStatements.get(j));
                        newNum = newNum-1;
                        myStatements.remove(j);
                        myStatements.add(j,""+newNum);
                    }
                    catch(NumberFormatException e){}
                }
                tempList.clear();
            }
        }
        for(int i=0;i<myStatements.size();i++){
            if(myStatements.get(i).equals("*")){
                i--;
                for(int j = 0; j < 3; j++){
                    tempList.add(myStatements.get(i));
                    myStatements.remove(i);
                }
                myStatements.add(i,multiplier(tempList,wireSpots));
                tempList.clear();
            }
        }
        for(int i = 0; i < myStatements.size(); i++){
            if(myStatements.get(i).equals("+")){
                i--;
                for(int j = 0; j < 3; j++){
                    tempList.add(myStatements.get(i));
                    myStatements.remove(i);
                }
                if(i <= myStatements.size()-1){
                    if(myStatements.get(i).equals("+")){
                        tempList.add(myStatements.get(i));
                        myStatements.remove(i);
                        tempList.add(myStatements.get(i));
                        myStatements.remove(i);
                    }
                }
                myStatements.add(i,adder(tempList,wireSpots));
                tempList.clear();
            }
        }
        for(int i=0;i<myStatements.size();i++){
            if(myStatements.get(i).equals("-")){
                i--;
                for(int j = 0; j < 3; j++){
                    tempList.add(myStatements.get(i));
                    myStatements.remove(i);
                }
                myStatements.add(i,subtractor(tempList,wireSpots));
                tempList.clear();
            }
        }
        for(int i=0;i<myStatements.size();i++){
            if(myStatements.get(i).equals("&")||myStatements.get(i).equals("&&")){
                i--;
                int size=3;
                if(myStatements.get(i).equals("~")){
                    size=4;
                    i--;
                }
                for(int j=0;j<size;j++){
                    tempList.add(myStatements.get(i));
                    myStatements.remove(i);
                }
                if(tempList.size()==3)
                    myStatements.add(i,and(tempList,wireSpots));
                else if(tempList.size()==4)
                    myStatements.add(i,nand(tempList,wireSpots));
                for(int j=i+1;j<myStatements.size();j++){
                    try{
                        int newNum = Integer.parseInt(myStatements.get(j));
                        newNum = newNum-1;
                        myStatements.remove(j);
                        myStatements.add(j,""+newNum);
                    }
                    catch(NumberFormatException e){}
                }
                tempList.clear();
            }
        }
        for(int i=0;i<myStatements.size();i++){
            if(myStatements.get(i).equals("|")||myStatements.get(i).equals("||")){
                i--;
                int size=3;
                if(myStatements.get(i).equals("~")){
                    size=4;
                    i--;
                }
                for(int j=0;j<size;j++){
                    tempList.add(myStatements.get(i));
                    myStatements.remove(i);
                }
                if(tempList.size()==3)
                    myStatements.add(i,or(tempList,wireSpots));
                else if(tempList.size()==4)
                    myStatements.add(i,nor(tempList,wireSpots));
                for(int j=i+1;j<myStatements.size();j++){
                    try{
                        int newNum = Integer.parseInt(myStatements.get(j));
                        newNum = newNum-1;
                        myStatements.remove(j);
                        myStatements.add(j,""+newNum);
                    }
                    catch(NumberFormatException e){}
                }
                tempList.clear();
            }
        }
        for(int i=0;i<myStatements.size();i++){
            if(myStatements.get(i).equals("^")){
                i--;
                int size=3;
                if(myStatements.get(i).equals("~")){
                    size=4;
                    i--;
                }
                for(int j=0;j<size;j++){
                    tempList.add(myStatements.get(i));
                    myStatements.remove(i);
                }
                if(tempList.size()==3)
                    myStatements.add(i,xor(tempList,wireSpots));
                else if(tempList.size()==4)
                    myStatements.add(i,xnor(tempList,wireSpots));
                tempList.clear();
            }
        }
        for(int i=0;i<myStatements.size();i++){
            if(myStatements.get(i).equals("{")){
                myStatements.remove(i);
                while(!myStatements.get(i).equals("}")){
                    tempList.add(myStatements.get(i));
                    myStatements.remove(i);
                }
                myStatements.remove(i);
                myStatements.add(i,concatenate(tempList,wireSpots));
                tempList.clear();
            }
        }
        for(int i=0;i<myStatements.size();i++){
            if(myStatements.get(i).equals(">")){
                i--;
                for(int j=0;j<4;j++){
                    tempList.add(myStatements.get(i));
                    myStatements.remove(i);
                }
                myStatements.add(i,bitShiftR(tempList,wireSpots));
                tempList.clear();
            }
        }
        for(int i=0;i<myStatements.size();i++){
            if(myStatements.get(i).equals("<")){
                i--;
                for(int j=0;j<4;j++){
                    tempList.add(myStatements.get(i));
                    myStatements.remove(i);
                }
                myStatements.add(i,bitShiftL(tempList,wireSpots));
                tempList.clear();
            }
        }
        for(int i=0;i<myStatements.size();i++){
            if(myStatements.get(i).equals("=")){
                i--;
                for(int j = 0; j < 3; j++){
                    tempList.add(myStatements.get(i));
                    myStatements.remove(i);
                }
                String action = equalSign(tempList,wireSpots);
                if(action.equals("0"))
                    return "";
                else
                    return ""+action;
            }
        }
        return""+myStatements.get(0);
    }
    
    /**
     * Computes logic for 'always' blocks
     * @param block the code within the block
     * @return 
     */
    public String always(ArrayList<String> block){
        char type = ' ';
        String clk="";
        String line = block.get(0);
        for(int i=8;line.charAt(i)!=')';i++){ //retrieve sensitivity list
            if(line.length()-i>=7){
                if(line.substring(i,i+7).equals("posedge")){ //sequential
                    type='p';
                    i+=6;
                }
                else if(line.substring(i,i+7).equals("negedge")){ //sequential
                    type='n';
                    i+=6;
                }
            }
            else if(line.charAt(i)=='*'){ //combinational
                type='*';
                break;
            }else if(line.charAt(i)!=' '){ //get name of clk signal (if applicable)
                clk+=line.charAt(i);
            }
        }
        block.remove(0);
        while (block.size()>0) { //go through the rest of the block
            ArrayList<String> output = scan(0,block,null);
            for(int k=0;k<output.size();k+=2){
                if(output.size()>1){
                    Wire wire2 = currModule.getWire(output.get(k+1));
                    if(wire2==null){ //for vectors
                        for(int i=0;(wire2=currModule.getWire(output.get(k+1)+"_"+i))!=null;i++){
                            Wire wire = currModule.getWire(output.get(k)+"_"+i);
                            wire2.ports.addAll(wire.ports);
                            currModule.wires.remove(wire);
                        }
                    }else{ //for non-vectors
                        Wire wire = currModule.getWire(output.get(k));
                        wire2.ports.addAll(wire.ports);
                        currModule.wires.remove(wire);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Helper method to always
     * @param index 
     * @param block list of lines in the always block
     * @param ifLogic conditional from if; used for else if or else
     * @return code for what kind of statement it was
     */
    public ArrayList<String> scan(int index,ArrayList<String> block,String ifLogic){
        
        String[] ops = {"","",""}; //[lop,op,rop]
        String line = block.get(index);
        String out = null;
        int type=0;
        int i;
        
        if(line.length()>=3){
            if(line.substring(0,3).equals("if ")||line.substring(0,3).equals("if(")){
                ops[1]=line.substring(2);
                type=1;
            }
        }if(line.length()>=4){
            if(line.substring(0,4).equals("else")){
                type=3;
            }
        }if(line.length()>=8){
            if(line.substring(0,8).equals("else if ")||line.substring(0,8).equals("else if(")){
                ops[1]=line.substring(8);
                type=1;
            }
            //break;
        }if(line.length()>=3){
            if(line.substring(0,3).equals("end"))
                type=-2;
        }if(line.length()>=7){
            if(line.substring(0,7).equals("endcase"))
                type=-3;
        }if(line.length()>=5){
            if(line.substring(0,5).equals("begin")){
                type=-1;
            }
        }if(line.length()>=4){
            if(line.substring(0,4).equals("case")){
                int j=0;
                for(i=4;i<line.length();i++)
                    if((line.charAt(i)!='(')&&(line.charAt(i)!=')')&&(line.charAt(i)!=' '))
                        ops[1]+=line.charAt(i);
                type=4;
            }
        }if(type==0){
            int j=0;
            for(i=0;i<line.length();i++){ //retrieve statement logic
                if(line.charAt(i)==';'){
                    break;
                }else if(line.charAt(i)=='='){
                    ops[1]=line.substring(i+1);
                    break;
                }else if(line.charAt(i)==39){
                    type=2;
                    j=1;
                }else if(line.charAt(i)==':'){
                    type=2;
                    ops[2]=line.substring(i+1);
                    break;
                }else if(line.charAt(i)!=' '){
                    ops[j]+=line.charAt(i);
                }
            }
        }
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> temp;
        ArrayList<String> list2; //for else block statements
        ArrayList<String> list3 = new ArrayList<>();
        Wire wire, wire2; //internal wires to be used
        switch(type){
            case 0: //'assign' statement
                if(currModule.getWire(ops[1].replaceAll("\\s","").replaceAll(";",""))!=null){ //see if we are just setting a wire to an existing wire
//                    String[] star = new String[2];
//                    star[0]=ops[1];
//                    star[1]=wire.name;
//                    preMod(star,6);
                    list.add(ops[1].replaceAll("\\s","").replaceAll(";",""));
                    list.add(ops[0]);
                }else if(currModule.getWire(ops[1].replaceAll("\\s","").replaceAll(";","")+"_0")!=null){ //check again, but for vectors this time
                    list.add(ops[1].replaceAll("\\s","").replaceAll(";",""));
                    list.add(ops[0]);
                }else{
                    wire = newWire();
                    assign(wire.name+" ="+ops[1]);
                    list.add(wire.name);
                    list.add(ops[0]);
                }
                block.remove(index);
                return list; //{output wire name, 'true' output wire name}
                
            case 1: //if block or else if block
                block.remove(index+1); //remove 'begin'
                temp = scan(index+1,block,ops[1]);
                while(!temp.get(0).equals("end")){
                    list.addAll(temp);
                    temp = scan(index+1,block,ops[1]);
                }
                
                list2 = scan(index+1,block,ops[1]); //for else/else if
                
                for(int k=0;k<list.size();k+=2){
                    if(list.size()>1){
                        int m; String ifFalse=null;
                        for(m=0;ifFalse==null;m+=2){
                            if(m>=list2.size()){
                                ifFalse="gnd";
                            }else if(list2.get(m+1).equals(list.get(k+1))){
                                ifFalse=list2.get(m);
                                list2.remove(m);
                                list2.remove(m);
                            }
                        }
                        list3.addAll(createMux(list.get(k+1),list.get(k),ifFalse,ops[1]));
//                        wire = newWire();
//                        assign(wire.name+" = "+ops[1]+" ? vcc : gnd;");
//                        wire2 = newWire();
//                        assign(wire2.name+" = ("+wire.name+") ? "+list.get(k)+" : " + ifFalse + ";");
//                        list3.add(wire2.name);
//                        list3.add(list.get(k+1));
                    }
                }
                
                for(int k=0;k<list2.size();k+=2){
                    if(list.size()>1){
                        list3.addAll(createMux(list.get(k+1),"gnd",list2.get(k),ops[1]));
//                        wire = newWire();
//                        assign(wire.name+" = "+ops[1]+" ? vcc : gnd;");
//                        wire2 = newWire();
//                        assign(wire2.name+" = ("+wire.name+") ? gnd : " + list2.get(k) + ";");
//                        list3.add(wire2.name);
//                        list3.add(list2.get(k+1));
                    }
                }
                
                block.remove(index);
                return list3; //{output wire name, 'true' output wire name,...}
                
            case 3: //else block
                block.remove(index+1); //remove 'begin'
                temp = scan(index+1,block,null);
                while(!temp.get(0).equals("end")){
                    list.addAll(temp);
                    temp = scan(index+1,block,null);
                }
                
                block.remove(index);
                return list;
                
            case 4: //case block
                temp = scan(index+1,block,null);
                ArrayList<String> last=null; //hold the previous case list
                ArrayList<ArrayList<String>> cases = new ArrayList<>(); //set size based on select bit width
                ArrayList<String> def=null;
                String in = ops[1];
                //int j=0;
                if(currModule.getWire(in)!=null){ //if the bit size is one
                    cases.add(null);
                    cases.add(null);
                }else //if the bit size is more than one
                    while(cases.size()<Math.pow(2,currModule.getWire(in+"_0").size)){ //fill the list with null lists before getting the cases
                        cases.add(null);
                    }
                
                while(!temp.get(0).equals("endcase")){ //retrieve all the 'cases'
                    if(temp.get(0).equals("default")){
                        temp.remove(0);
                        def = temp;
                    }else{
                        int num = Integer.parseInt(temp.get(0));
                        temp.remove(0);
                        cases.set(num,temp);
                    }
                    temp = scan(index+1,block,null);
                }
                
                list3 = caseHelper(cases,def,in,0); //create cascaded muxes with all the cases
                
                block.remove(index);
                return list3;
                
            case 2: //a 'case' in a case block
                //ops={1,b0,statement}
                if(ops[0].equals("default"))
                    list.add("default");
                else
                    list.add(""+numberMaker(ops[1]));
                block.set(index,ops[2]);
                
                if(block.get(index).equals("")||block.get(index).equals(" ")){
                    block.remove(index+1); //remove the 'begin'
                    temp = scan(index+1,block,null);
                    while(!temp.get(0).equals("end")){
                    list.addAll(temp);
                    temp = scan(index+1,block,null);
                    }
                    block.remove(index);
                }else{
                    temp = scan(index,block,null);
                    list.addAll(temp);
                }
                return list; //{case id,stuff...}
                
            case -1: //begin statement
                list.add("begin");
                block.remove(index);
                return list;
            case -2: //end statement
                list.add("end");
                block.remove(index);
                return list;
            case -3: //endcase statement
                list.add("endcase");
                block.remove(index);
                return list;
            }
        
        return null;
    }
    /**
     * convert number in 1b'0 format to an integer
     * @param in
     * @return the integer
     */
    public int numberMaker(String in){
        int total=0;
        in = in.substring(1);
        char[] digits = in.toCharArray();
        for(int j=0;j<digits.length;j++){
            if(digits[j]==0x31)
                total+=Math.pow(2,digits.length-j-1);
        }
        return total;
    }
    
    /**
     * Handle the case statements by creating cascaded muxes
     * @param cases
     * @param def
     * @param in
     * @param i
     * @return 
     */
    public ArrayList<String> caseHelper(ArrayList<ArrayList<String>> cases,ArrayList<String> def,String in,int i){
        String ifOne="gnd";
        ArrayList<String> list1=null;
        ArrayList<ArrayList<String>> out = new ArrayList<>();
        while(out.size()<cases.size()/2) //fill the list with null slots
            out.add(null);
        for(int w=0;w<cases.size();w+=2){
            list1=null;
            if((cases.get(w)!=null)||(cases.get(w+1)!=null)){
                if(cases.get(w)==null) cases.set(w,def); //set default
                else if(cases.get(w+1)==null) cases.set(w+1,def); //set default
                list1 = new ArrayList<>();
                if(cases.get(w)!=null) //make sure that there is a case for the current vale
                    for(int j=0;j<cases.get(w).size();j+=2){ //loop through all statements in an even case
                        int k;
                        if(cases.get(w+1)!=null){
                            if(!cases.get(w+1).isEmpty()) //ensure that there is a case for current value +1
                                for(k=0;k<cases.get(w+1).size();k+=2){ //loop through all statementse in the next odd case
                                    if(cases.get(w).get(j+1).equals(cases.get(w+1).get(k+1))){
                                        ifOne = cases.get(w+1).get(k);
                                        cases.get(w+1).remove(k);
                                        cases.get(w+1).remove(k);
                                        break;
                                    }
                                }
                        }
                        list1.addAll(createMux(cases.get(w).get(j+1),ifOne,cases.get(w).get(j),"("+in+"_"+i+")"));
//                        Wire wire = newWire();
//                        assign(wire.name+" = ("+in+"_"+i+") ? "+ifOne+" : "+cases.get(w).get(j)+";");
//                        list1.add(wire.name);
//                        list1.add(cases.get(w).get(j+1));
                        ifOne="gnd";
                    }
                
                if(cases.get(w+1)!=null){
                    if(!cases.get(w+1).isEmpty()) //ensure that there is a case for current value +1
                        for(int k=0;k<cases.get(w+1).size();k+=2){ //loop through all the rest of the statements in the odd case
                            list1.addAll(createMux(cases.get(w+1).get(k+1),cases.get(w+1).get(k),"gnd","("+in+"_"+i+")"));
//                            Wire wire = newWire();
//                            assign(wire.name+" = ("+in+"_"+i+") ? "+cases.get(w+1).get(k)+" : gnd;");
//                            list1.add(wire.name);
//                            list1.add(cases.get(w+1).get(k+1));
                        }
                }
            }
            
            out.set(w/2,list1);
        }
        if(out.size()>=2)
            return caseHelper(out,def,in,i+1);
        return list1;
    }
    
    /**
     * Helper method to create multiple muxes if necessary (for vectors)
     * @param outputName
     * @param ifOne the one condition
     * @param ifZero the zero condition
     * @param cond the conditional logic
     * @return [output wire, final output wire]
     */
    public ArrayList<String> createMux(String outputName,String ifOne,String ifZero,String cond){
        ArrayList<String> list = new ArrayList<>();
        Wire wire = newWire();
        assign(wire.name+" = "+cond+" ? vcc : gnd;");
        Wire w = currModule.getWire(outputName);
        int currentWire = numWires;
        if(w==null){ //for vectors
            for(int i=0;currModule.getWire(outputName+"_"+i)!=null;i++){
                Wire wire2 = new Wire("MISC_"+currentWire+"_"+i,1);
                currModule.wires.add(wire2);
                numWires++;
                if(ifZero.equals("gnd")||ifZero.equals("vcc"))
                    if(ifOne.equals("gnd")||ifOne.equals("vcc"))
                        assign(wire2.name+" = ("+wire.name+") ? "+ ifOne +" : " + ifZero +";");
                    else
                        assign(wire2.name+" = ("+wire.name+") ? "+ ifOne +"_"+i+" : " + ifZero +";");
                
                else if(ifOne.equals("gnd")||ifOne.equals("vcc"))
                    if(ifZero.equals("gnd")||ifZero.equals("vcc"))
                        assign(wire2.name+" = ("+wire.name+") ? "+ ifOne +" : " + ifZero +";");
                    else
                    assign(wire2.name+" = ("+wire.name+") ? "+ ifOne +" : " + ifZero +"_"+i+";");
                else
                    assign(wire2.name+" = ("+wire.name+") ? "+ ifOne +"_"+i+" : " + ifZero +"_"+i+";");
            }
        }else{ //for non-vectors
            Wire wire2 = newWire();
            assign(wire2.name+" = ("+wire.name+") ? "+ ifOne +" : " + ifZero +";");
        }
        list.add("MISC_"+currentWire);
        list.add(outputName);
        return list;
    }
    
     /*
    This method handles the many different types of cases
    involved with parentheses in an assign statement
    */
    public String parens(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        /*
        TODO
        (+,-) -> find bitSize, new Wire[bitSize]
        (*) -> find bitSize, new Wire[bitSize*2]
        (|,&,^,||,&&,~|,~&,~^,^~) -> find bitSize, new Wire[1]
        ({}) -> find both bitSizes, new Wire[bitSize1 + bitSize2]
        (>>,<<) -> find bitSize, new Wire[bitSize]
        */
        boolean invert=false;
        if(myStatement.get(0).equals("!")||myStatement.get(0).equals("~")){
            invert=true;
            myStatement.remove(0);
        }
        for(int i=0;i<myStatement.size();i++){
            if(myStatement.get(i).equals("(")){
                ArrayList<String> tempList = new ArrayList<String>();
                int there = 0;
                while(!myStatement.get(i).equals(")") || there!=0){
                    if(myStatement.get(i).equals("("))
                        there++;
                    if(there!=0&&myStatement.get(i).equals(")"))
                        there--;
                    tempList.add(myStatement.get(i));
                    myStatement.remove(i);
                }
                parens(tempList,wireSpots);
                tempList.clear();
            }
        }
        String temp ="";
        if(myStatement.size()==1){}
        else if(myStatement.size()>3){
            if(myStatement.get(3).equals("?")){
                temp = comparator(myStatement,wireSpots);
                myStatement.clear();
                myStatement.add(temp);
            }
            else if(myStatement.get(1).equals("?")){
                temp = mux(myStatement,wireSpots);
                myStatement.clear();
                myStatement.add(temp);
            }
            else{
                temp=evaluate(myStatement,wireSpots);
                myStatement.clear();
                myStatement.add(temp);
            }
        }
        else{
            temp=evaluate(myStatement,wireSpots);
            myStatement.clear();
            myStatement.add(temp);
        }
        if(invert){
            return invert(myStatement,wireSpots);
        }
        else{
            return myStatement.get(0);
        }
    }
    
     /*
    When the end of an assign statement has been reached, this method
    performs the final conversion to connect the left side of the 
    statement to final wire on the right side
    */
    public String equalSign(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int left = Integer.parseInt(myStatement.get(0));
        int right = Integer.parseInt(myStatement.get(2));
        int bitSize = wireSpots.get(left).length;
        boolean input = false;
        boolean output = false;
        for(Wire wire : wireSpots.get(right)){
            for(Port port : wire.ports){
                if(port.part==null)
                    input=true;
            }
        }
        for(Wire wire : wireSpots.get(left)){
            for(Port port : wire.ports){
                if(port.part==null)
                    output=true;
            }
        }
        if(!input){
            for(int i=0;i<bitSize;i++){
                for(Port port : wireSpots.get(right)[i].ports){
                    wireSpots.get(left)[i].ports.add(port);
                }
            }
            for(int i=0;i<wireSpots.get(right).length;i++){
                for(int j=0;j<currModule.wires.size();j++){
                    if(currModule.wires.get(j).name.equals(wireSpots.get(right)[i].name)){
                        currModule.wires.remove(j);
                        j--;
                    }
                }
            }
        }
        else if(output){
            for(int i=0;i<bitSize;i++){
                for(Port port : wireSpots.get(left)[i].ports){
                    wireSpots.get(right)[i].ports.add(port);
                }
            }
            for(int i=0;i<wireSpots.get(left).length;i++){
                for(int j=0;j<currModule.wires.size();j++){
                    if(currModule.wires.get(j).name.equals(wireSpots.get(left)[i].name)){
                        currModule.wires.remove(j);
                        j--;
                    }
                }
            }
        }
        else{
            for(int i=0;i<bitSize;i++){
                for(Port port : wireSpots.get(left)[i].ports){
                    wireSpots.get(right)[i].ports.add(port);
                }
                for(Port port : wireSpots.get(right)[i].ports){
                    if(port.part==null)
                        wireSpots.get(left)[i].ports.add(port);
                }
            }
        }
        wireSpots.remove(left);
        wireSpots.remove(right-1);
        return ""+(wireSpots.size()-1);
    }
    
     /*
     This method connects wires to a nand gate
    */
    public String nand(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int left = Integer.parseInt(myStatement.get(0));
        int right = Integer.parseInt(myStatement.get(3));
        NAND newNand = new NAND(newName("NAND2$"));
        currModule.parts.add(newNand);
        wireSpots.get(left)[0].ports.add(newNand.ports.get(0));
        wireSpots.get(right)[0].ports.add(newNand.ports.get(1));
        wireSpots.remove(left);
        wireSpots.remove(right-1);
        wireSpots.add(left,new Wire[1]);
        wireSpots.get(left)[0]=newWire();
        wireSpots.get(left)[0].ports.add(newNand.ports.get(2));
        return ""+(left);
    }
    
    /*
     This method connects wires to a nor gate
    */
    public String nor(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int left = Integer.parseInt(myStatement.get(0));
        int right = Integer.parseInt(myStatement.get(3));
        NOR newNor = new NOR(newName("NOR2$"));
        currModule.parts.add(newNor);
        wireSpots.get(left)[0].ports.add(newNor.ports.get(0));
        wireSpots.get(right)[0].ports.add(newNor.ports.get(1));
        wireSpots.remove(left);
        wireSpots.remove(right-1);
        wireSpots.add(left,new Wire[1]);
        wireSpots.get(left)[0]=newWire();
        wireSpots.get(left)[0].ports.add(newNor.ports.get(2));
        return ""+(left);
    }
    
    /*
     This method connects wires to an xnor gate
    */
    public String xnor(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int left = Integer.parseInt(myStatement.get(0));
        int right = Integer.parseInt(myStatement.get(3));
        XNOR newXnor = new XNOR(newName("XNOR2$"));
        currModule.parts.add(newXnor);
        wireSpots.get(left)[0].ports.add(newXnor.ports.get(0));
        wireSpots.get(right)[0].ports.add(newXnor.ports.get(1));
        wireSpots.remove(left);
        wireSpots.remove(right-1);
        wireSpots.add(left,new Wire[1]);
        wireSpots.get(left)[0]=newWire();
        wireSpots.get(left)[0].ports.add(newXnor.ports.get(2));
        return ""+(left);
    }
    
    /*
     This method connects wires to an and gate
    */
    public String and(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int left = Integer.parseInt(myStatement.get(0));
        int right = Integer.parseInt(myStatement.get(2));
        AND newAnd = new AND(newName("AND2$"));
        currModule.parts.add(newAnd);
        for(int i=0;i<wireSpots.get(left).length;i++)
            wireSpots.get(left)[i].ports.add(newAnd.ports.get(0));
        for(int i=0;i<wireSpots.get(right).length;i++)
            wireSpots.get(right)[i].ports.add(newAnd.ports.get(1));
        wireSpots.remove(left);
        wireSpots.remove(right-1);
        wireSpots.add(left,new Wire[1]);
        wireSpots.get(left)[0]=newWire();
        wireSpots.get(left)[0].ports.add(newAnd.ports.get(2));
        return ""+(left);
    }
    
    /*
     This method connects wires to an or gate
    */
    public String or(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int left = Integer.parseInt(myStatement.get(0));
        int right = Integer.parseInt(myStatement.get(2));
        OR newOr = new OR(newName("OR2$"));
        currModule.parts.add(newOr);
        for(int i=0;i<wireSpots.get(left).length;i++)
            wireSpots.get(left)[i].ports.add(newOr.ports.get(0));
        for(int i=0;i<wireSpots.get(right).length;i++)
            wireSpots.get(right)[i].ports.add(newOr.ports.get(1));
        wireSpots.remove(left);
        wireSpots.remove(right-1);
        wireSpots.add(left,new Wire[1]);
        wireSpots.get(left)[0]=newWire();
        wireSpots.get(left)[0].ports.add(newOr.ports.get(2));
        return ""+(left);
    }
    
    /*
     This method connects wires to an xor gate
    */
    public String xor(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int left = Integer.parseInt(myStatement.get(0));
        int right = Integer.parseInt(myStatement.get(2));
        XOR newXor = new XOR(newName("XOR2$"));
        currModule.parts.add(newXor);
        wireSpots.get(left)[0].ports.add(newXor.ports.get(0));
        wireSpots.get(right)[0].ports.add(newXor.ports.get(1));
        wireSpots.remove(left);
        wireSpots.remove(right-1);
        wireSpots.add(left,new Wire[1]);
        wireSpots.get(left)[0]=newWire();
        wireSpots.get(left)[0].ports.add(newXor.ports.get(2));
        return ""+(left);
    }
    
    /*
     This method inverts a wire
    */
    public String invert(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int wire = Integer.parseInt(myStatement.get(0));
        N_Inverter newInvert = new N_Inverter(newName("INV$"));
        currModule.parts.add(newInvert);
        Wire[] outWires = new Wire[wireSpots.get(wire).length];
        for(int i=0;i<wireSpots.get(wire).length;i++)
            outWires[i]=newWire();
        for(int i=0;i<wireSpots.get(wire).length;i++){
            wireSpots.get(wire)[i].ports.add(newInvert.ports.get(0));
            outWires[i].ports.add(newInvert.ports.get(1));
        }
        wireSpots.remove(wire);
        wireSpots.add(wire,outWires);
        return""+(wire);
    }
    
    public String concatenate(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int total = 0;
        for(int i=0;i<myStatement.size();i++){
            total = total + wireSpots.get(Integer.parseInt(myStatement.get(i))).length;
        }
        Wire[] newWires = new Wire[total];
        int spot=0;
        for(int i=0;i<myStatement.size();i++){
            for(int j=0;j<wireSpots.get(Integer.parseInt(myStatement.get(i))).length;j++){
                newWires[spot]=wireSpots.get(Integer.parseInt(myStatement.get(i)))[j];
                spot++;
            }
        }
        wireSpots.remove(Integer.parseInt(myStatement.get(0)));
        wireSpots.add(Integer.parseInt(myStatement.get(0)),newWires);
        return""+(Integer.parseInt(myStatement.get(0)));
    }
    
    public String bitShiftR(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int wire = Integer.parseInt(myStatement.get(0));
        int size = Integer.parseInt(myStatement.get(3));
        Wire[] newWires = new Wire[wireSpots.get(wire).length];
        for(int i=wireSpots.get(wire).length-1;i>(size-1);i--)
            newWires[i-size]=wireSpots.get(wire)[i];
        for(int i=0;i<newWires.length;i++){
            if(newWires[i]==null)
                newWires[i]=GND;
        }
        wireSpots.remove(wire);
        wireSpots.add(wire,newWires);
        return""+(wire);
    }
    
    public String bitShiftL(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int wire = Integer.parseInt(myStatement.get(0));
        int size = Integer.parseInt(myStatement.get(3));
        Wire[] newWires = new Wire[wireSpots.get(wire).length];
        for(int i=0;i<(wireSpots.get(wire).length-size);i++)
            newWires[i+size]=wireSpots.get(wire)[i];
        for(int i=0;i<newWires.length;i++){
            if(newWires[i]==null)
                newWires[i]=GND;
        }
        wireSpots.remove(wire);
        wireSpots.add(wire,newWires);
        return""+(wire);
    }
    
    /*
    This method creates a comparator and connects the necessary
    wires to the necessary ports
    */
    public String comparator(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int left = Integer.parseInt(myStatement.get(0));
        int right = Integer.parseInt(myStatement.get(2));
        int bitSize;
        boolean inverted=false;
        if(Integer.parseInt(myStatement.get(4))!=1)
            inverted=true;
        if(wireSpots.get(left).length>wireSpots.get(right).length)
            bitSize=wireSpots.get(left).length;
        else
            bitSize=wireSpots.get(right).length;
        Comparator newComp = new Comparator("COMP$"+numComparators);numComparators++;
        currModule.parts.add(newComp);
        for(int i=0;i<bitSize;i++){
            if(i<wireSpots.get(left).length)
                wireSpots.get(left)[i].ports.add(newComp.ports.get(0));
            else
                GND.ports.add(newComp.ports.get(0));
        }
        for(int i=0;i<bitSize;i++){
            if(i<wireSpots.get(right).length)
                wireSpots.get(right)[i].ports.add(newComp.ports.get(1));
            else
                GND.ports.add(newComp.ports.get(1));
        }
        wireSpots.remove(left);
        wireSpots.remove(right-1);
        Wire[] myWires;
        switch(myStatement.get(1)){
            case ">=":
                if(inverted)
                    myWires = new Wire[4];
                else
                    myWires = new Wire[3];
                myWires[0]=newWire();
                myWires[1]=newWire();
                myWires[2]=newWire();
                OR newOR = new OR(newName("OR2$"));
                currModule.parts.add(newOR);
                myWires[0].ports.add(newComp.ports.get(2));
                myWires[0].ports.add(newOR.ports.get(0));
                myWires[1].ports.add(newComp.ports.get(3));
                myWires[1].ports.add(newOR.ports.get(1));
                myWires[2].ports.add(newOR.ports.get(2));
                Wire[] newWires;
                if(myWires.length==4){
                    Inverter newInv = new Inverter(newName("INV$"));
                    currModule.parts.add(newInv);
                    myWires[2].ports.add(newInv.ports.get(0));
                    myWires[3]=newWire();
                    myWires[3].ports.add(newInv.ports.get(1));
                    newWires = new Wire[1];
                    newWires[0]=myWires[3];
                }
                else{
                    newWires = new Wire[1];
                    newWires[0]=myWires[2];
                }
                wireSpots.add(left,newWires);
                return ""+(left);
            case "<=":
                if(inverted)
                    myWires = new Wire[4];
                else
                    myWires = new Wire[3];
                myWires[0]=newWire();
                myWires[1]=newWire();
                myWires[2]=newWire();
                OR newOR2 = new OR(newName("OR2$"));
                currModule.parts.add(newOR2);
                myWires[0].ports.add(newComp.ports.get(4));
                myWires[0].ports.add(newOR2.ports.get(0));
                myWires[1].ports.add(newComp.ports.get(3));
                myWires[1].ports.add(newOR2.ports.get(1));
                myWires[2].ports.add(newOR2.ports.get(2));
                Wire[] newWires2;
                if(myWires.length==4){
                    Inverter newInv = new Inverter(newName("INV$"));
                    currModule.parts.add(newInv);
                    myWires[2].ports.add(newInv.ports.get(0));
                    myWires[3]=newWire();
                    myWires[3].ports.add(newInv.ports.get(1));
                    newWires2 = new Wire[1];
                    newWires2[0]=myWires[3];
                }
                else{
                    newWires2 = new Wire[1];
                    newWires2[0]=myWires[2];
                }
                wireSpots.add(left,newWires2);
                return ""+(left);
            case ">":
                if(inverted){
                    Inverter newInv = new Inverter(newName("INV$"));
                    currModule.parts.add(newInv);
                    myWires = new Wire[2];
                    myWires[0]=newWire();
                    myWires[1]=newWire();
                    myWires[0].ports.add(newComp.ports.get(2));
                    myWires[0].ports.add(newInv.ports.get(0));
                    myWires[1].ports.add(newInv.ports.get(1));
                    newWires2 = new Wire[1];
                    newWires2[0]=myWires[1];
                }
                else{
                    newWires2 = new Wire[1];
                    newWires2[0]=newWire();
                    newWires2[0].ports.add(newComp.ports.get(2));
                }
                wireSpots.add(left,newWires2);
                return ""+(left);
            case "<":
                if(inverted){
                    Inverter newInv = new Inverter(newName("INV$"));
                    currModule.parts.add(newInv);
                    myWires = new Wire[2];
                    myWires[0]=newWire();
                    myWires[1]=newWire();
                    myWires[0].ports.add(newComp.ports.get(4));
                    myWires[0].ports.add(newInv.ports.get(0));
                    myWires[1].ports.add(newInv.ports.get(1));
                    newWires2 = new Wire[1];
                    newWires2[0]=myWires[1];
                }
                else{
                    newWires2 = new Wire[1];
                    newWires2[0]=newWire();
                    newWires2[0].ports.add(newComp.ports.get(4));
                }
                wireSpots.add(left,newWires2);
                return ""+(left);
            case "==":
                if(inverted){
                    Inverter newInv = new Inverter(newName("INV$"));
                    currModule.parts.add(newInv);
                    myWires = new Wire[2];
                    myWires[0]=newWire();
                    myWires[1]=newWire();
                    myWires[0].ports.add(newComp.ports.get(3));
                    myWires[0].ports.add(newInv.ports.get(0));
                    myWires[1].ports.add(newInv.ports.get(1));
                    newWires2 = new Wire[1];
                    newWires2[0]=myWires[1];
                }
                else{
                    newWires2 = new Wire[1];
                    newWires2[0]=newWire();
                    newWires2[0].ports.add(newComp.ports.get(3));
                }
                wireSpots.add(left,newWires2);
                return ""+(left);
            case "!=":
                if(!inverted){
                    Inverter newInv = new Inverter(newName("INV$"));
                    currModule.parts.add(newInv);
                    myWires = new Wire[2];
                    myWires[0]=newWire();
                    myWires[1]=newWire();
                    myWires[0].ports.add(newComp.ports.get(2));
                    myWires[0].ports.add(newInv.ports.get(0));
                    myWires[1].ports.add(newInv.ports.get(1));
                    newWires2 = new Wire[1];
                    newWires2[0]=myWires[1];
                }
                else{
                    newWires2 = new Wire[1];
                    newWires2[0]=newWire();
                    newWires2[0].ports.add(newComp.ports.get(2));
                }
                wireSpots.add(left,newWires2);
                return ""+(left);
        }
        return "";
    }
    
    /*
    This method creates a multiplexer and connects the necessary
    wires to the necessary ports
    */
    public String mux(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int in1 = Integer.parseInt(myStatement.get(2));
        int in2 = Integer.parseInt(myStatement.get(4));
        int sel = Integer.parseInt(myStatement.get(0));
        int bitSize;
        if(wireSpots.get(in1).length>wireSpots.get(in2).length)
            bitSize=wireSpots.get(in1).length;
        else
            bitSize=wireSpots.get(in2).length;
        Mux newMux = new Mux("MUX$"+numMuxs);numMuxs++;
        currModule.parts.add(newMux);
        for(int i=0;i<bitSize;i++){
            if(i<wireSpots.get(in1).length)
                wireSpots.get(in1)[i].ports.add(newMux.ports.get(1));
            else
                GND.ports.add(newMux.ports.get(1));
        }
        for(int i=0;i<bitSize;i++){
            if(i<wireSpots.get(in2).length)
                wireSpots.get(in2)[i].ports.add(newMux.ports.get(0));
            else
                GND.ports.add(newMux.ports.get(0));
        }
        for(int i=0;i<wireSpots.get(sel).length;i++)
            wireSpots.get(sel)[i].ports.add(newMux.ports.get(2));
        wireSpots.remove(in1);
        wireSpots.remove(in2-1);
        wireSpots.add(new Wire[bitSize]);
        for(int i=0;i<bitSize;i++){
            wireSpots.get(wireSpots.size()-1)[i]=newWire();
            wireSpots.get(wireSpots.size()-1)[i].ports.add(newMux.ports.get(3));
        }
        return ""+(wireSpots.size()-1);
    }
    
    /*
    This method creates a multiplier and connects the necessary
    wires to the necessary ports
    */
    public String multiplier(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int left = Integer.parseInt(myStatement.get(0));
        int right = Integer.parseInt(myStatement.get(2));
        int bitSize;
        if(wireSpots.get(left).length>wireSpots.get(right).length)
            bitSize=wireSpots.get(left).length;
        else
            bitSize=wireSpots.get(right).length;
        Multiplier newMultiplier = new Multiplier("MULT$"+numMultipliers);
        numMultipliers++;
        currModule.parts.add(newMultiplier);
        for(int i=0;i<bitSize;i++){
            if(i<wireSpots.get(left).length)
                wireSpots.get(left)[i].ports.add(newMultiplier.ports.get(0));
            else
                GND.ports.add(newMultiplier.ports.get(0));
        }
        for(int i=0;i<bitSize;i++){
            if(i<wireSpots.get(right).length)
                wireSpots.get(right)[i].ports.add(newMultiplier.ports.get(1));
            else
                GND.ports.add(newMultiplier.ports.get(1));
        }
        wireSpots.remove(left);
        wireSpots.remove(right-1);
        wireSpots.add(new Wire[bitSize*2]);
        for(int i=0;i<bitSize*2;i++){
            wireSpots.get(wireSpots.size()-1)[i]=newWire();
            wireSpots.get(wireSpots.size()-1)[i].ports.add(newMultiplier.ports.get(2));
        }
        return ""+(wireSpots.size()-1);
    }
    
    /*
    This method creates an adder and connects the necessary
    wires to the necessary ports
    */
    public String adder(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int left = Integer.parseInt(myStatement.get(0));
        int right = Integer.parseInt(myStatement.get(2));
        int bitSize;
        if(wireSpots.get(left).length>wireSpots.get(right).length)
            bitSize=wireSpots.get(left).length;
        else
            bitSize=wireSpots.get(right).length;
        Adder newAdder = new Adder("ADD$"+numAdders);
        numAdders++;
        currModule.parts.add(newAdder);
        for(int i=0;i<bitSize;i++){
            if(i<wireSpots.get(left).length)
                wireSpots.get(left)[i].ports.add(newAdder.ports.get(0));
            else
                GND.ports.add(newAdder.ports.get(0));
        }
        for(int i=0;i<bitSize;i++){
            if(i<wireSpots.get(right).length)
                wireSpots.get(right)[i].ports.add(newAdder.ports.get(1));
            else
                GND.ports.add(newAdder.ports.get(1));
        }
        if(myStatement.size()==5)
            wireSpots.get(Integer.parseInt(myStatement.get(4)))[0].ports.add(newAdder.ports.get(2));
        else
            GND.ports.add(newAdder.ports.get(2));
        wireSpots.remove(left);
        wireSpots.remove(right-1);
        if(myStatement.size()==5)
            wireSpots.remove(Integer.parseInt(myStatement.get(4))-2);
        wireSpots.add(new Wire[bitSize+1]);
        for(int i=0;i<bitSize;i++){
            wireSpots.get(wireSpots.size()-1)[i]=newWire();
            wireSpots.get(wireSpots.size()-1)[i].ports.add(newAdder.ports.get(3));
        }
        wireSpots.get(wireSpots.size()-1)[bitSize]=newWire();
        wireSpots.get(wireSpots.size()-1)[bitSize].ports.add(newAdder.ports.get(4));
        return ""+(wireSpots.size()-1);
    }
    
    /*
    This method creates a subtractor and connects the necessary
    wires to the necessary ports
    */
    public String subtractor(ArrayList<String> myStatement, ArrayList<Wire[]> wireSpots){
        int left = Integer.parseInt(myStatement.get(0));
        int right = Integer.parseInt(myStatement.get(2));
        int bitSize;
        if(wireSpots.get(left).length>wireSpots.get(right).length)
            bitSize=wireSpots.get(left).length;
        else
            bitSize=wireSpots.get(right).length;
        Subtractor newSubtractor = new Subtractor("SUB$"+numSubtractors);
        numSubtractors++;
        currModule.parts.add(newSubtractor);
        for(int i=0;i<bitSize;i++){
            if(i<wireSpots.get(left).length)
                wireSpots.get(left)[i].ports.add(newSubtractor.ports.get(0));
            else
                GND.ports.add(newSubtractor.ports.get(0));
        }
        for(int i=0;i<bitSize;i++){
            if(i<wireSpots.get(right).length)
                wireSpots.get(right)[i].ports.add(newSubtractor.ports.get(1));
            else
                GND.ports.add(newSubtractor.ports.get(1));
        }
        VCC.ports.add(newSubtractor.ports.get(2));
        wireSpots.remove(left);
        wireSpots.remove(right-1);
        wireSpots.add(new Wire[bitSize+1]);
        for(int i=0;i<bitSize;i++){
            wireSpots.get(wireSpots.size()-1)[i]=newWire();
            wireSpots.get(wireSpots.size()-1)[i].ports.add(newSubtractor.ports.get(3));
        }
        wireSpots.get(wireSpots.size()-1)[bitSize]=newWire();
        wireSpots.get(wireSpots.size()-1)[bitSize].ports.add(newSubtractor.ports.get(4));
        return ""+(wireSpots.size()-1);
    }
    
    /*
    Once all of the parts have been created for the main module, this
    method takes all of the parts that are not simple gates and breaks
    them down to the gate level
    */
    public void part2gate(){
        ArrayList<Wire[]> inAndOuts = new ArrayList<Wire[]>();
        for(int i=0;i<currModule.parts.size();i++){
            for(int j=0;j<currModule.parts.get(i).ports.size();j++){
                int size=0;
                for(Wire wire : currModule.wires){
                    for(Port port : wire.ports){
                        if(port.part!=null){
                            if(port.name.equals(currModule.parts.get(i).ports.get(j).name)&&port.part.name.equals(currModule.parts.get(i).name))
                                size++;
                        }
                    }
                }
                inAndOuts.add(new Wire[size]);
                int k=0;
                for(Wire wire : currModule.wires){
                    for(Port port : wire.ports){
                        if(port.part!=null){
                            if(port.name.equals(currModule.parts.get(i).ports.get(j).name)&&port.part.name.equals(currModule.parts.get(i).name)){
                                inAndOuts.get(j)[k]=wire;
                                k++;
                            }
                        }
                    }
                }
            }
            if(currModule.parts.get(i).name.contains("ADD")){
                adderBD(currModule.parts.get(i),inAndOuts);
                removePorts(currModule.parts.get(i),inAndOuts);
                inAndOuts.clear();
                currModule.parts.remove(i);
                i--;
            }
            else if(currModule.parts.get(i).name.contains("SUB")){
                subtractorBD(currModule.parts.get(i),inAndOuts);
                removePorts(currModule.parts.get(i),inAndOuts);
                inAndOuts.clear();
                currModule.parts.remove(i);
                i--;
            }
            else if(currModule.parts.get(i).name.contains("MULT")){
                multiplierBD(currModule.parts.get(i),inAndOuts);
                removePorts(currModule.parts.get(i),inAndOuts);
                inAndOuts.clear();
                currModule.parts.remove(i);
                i--;
            }
            else if(currModule.parts.get(i).name.contains("MUX")){
                muxBD(currModule.parts.get(i),inAndOuts);
                removePorts(currModule.parts.get(i),inAndOuts);
                inAndOuts.clear();
                currModule.parts.remove(i);
                i--;
            }
            else if(currModule.parts.get(i).name.contains("COMP")){
                comparatorBD(currModule.parts.get(i),inAndOuts);
                removePorts(currModule.parts.get(i),inAndOuts);
                inAndOuts.clear();
                currModule.parts.remove(i);
                i--;
            }
            else if(currModule.parts.get(i).name.contains("AND")&&
                    !currModule.parts.get(i).name.contains("NAND")&&
                    (inAndOuts.get(0).length>1||inAndOuts.get(1).length>1)){
                andBD(currModule.parts.get(i),inAndOuts);
                removePorts(currModule.parts.get(i),inAndOuts);
                inAndOuts.clear();
                currModule.parts.remove(i);
                i--;
            }
            else if(currModule.parts.get(i).name.contains("OR")&&
                    !currModule.parts.get(i).name.contains("NOR")&&
                    (inAndOuts.get(0).length>1||inAndOuts.get(1).length>1)){
                orBD(currModule.parts.get(i),inAndOuts);
                removePorts(currModule.parts.get(i),inAndOuts);
                inAndOuts.clear();
                currModule.parts.remove(i);
                i--;
            }
            else if(currModule.parts.get(i).name.contains("INV")&&
                    inAndOuts.get(0).length>1){
                invBD(currModule.parts.get(i),inAndOuts);
                removePorts(currModule.parts.get(i),inAndOuts);
                inAndOuts.clear();
                currModule.parts.remove(i);
                i--;
            }
            else{
                inAndOuts.clear();
            }
        }
    }
    
    /*
    Once a part has been broken down to the gate level, this
    method removes all of the ports associated with the recently
    converted part
    */
    public void removePorts(Part myPart, ArrayList<Wire[]> inAndOuts){
        for(int i=0;i<inAndOuts.size();i++){
            for(int j=0;j<inAndOuts.get(i).length;j++){
                for(int k=0;k<inAndOuts.get(i)[j].ports.size();k++){
                    if(inAndOuts.get(i)[j].ports.get(k).part!=null){
                        if(inAndOuts.get(i)[j].ports.get(k).part.name.equals(myPart.name)){
                            inAndOuts.get(i)[j].ports.remove(k);
                        }
                    }
                }
            }
        }
        for(int i=0;i<3;i++){
            for(int j=0;j<currModule.wires.get(i).ports.size();j++){
                if(currModule.wires.get(i).ports.get(j).part!=null){
                    if(currModule.wires.get(i).ports.get(j).part.name.equals(myPart.name)){
                        currModule.wires.get(i).ports.remove(j);
                    }
                }
            }
        }
    }
    
    public void andBD(Part myPart, ArrayList<Wire[]> inAndOuts){
        if(inAndOuts.get(0).length>inAndOuts.get(1).length){
            Wire[] temp = new Wire[inAndOuts.get(0).length];
            for(int i=0;i<inAndOuts.get(1).length;i++)
                temp[i]=inAndOuts.get(1)[i];
            inAndOuts.remove(1);
            inAndOuts.add(1,temp);
            for(int i=inAndOuts.get(1).length;i<inAndOuts.get(0).length;i++)
                inAndOuts.get(1)[i]=GND;
        }
        else if(inAndOuts.get(1).length>inAndOuts.get(0).length){
            Wire[] temp = new Wire[inAndOuts.get(1).length];
            for(int i=0;i<inAndOuts.get(0).length;i++)
                temp[i]=inAndOuts.get(0)[i];
            inAndOuts.remove(0);
            inAndOuts.add(0,temp);
            for(int i=inAndOuts.get(0).length;i<inAndOuts.get(1).length;i++)
                inAndOuts.get(0)[i]=GND;
        }
        String assignStatement=inAndOuts.get(2)[0].name+" = ";
        for(int i=0;i<inAndOuts.get(0).length;i++){
            assignStatement = assignStatement +"("+inAndOuts.get(0)[i].name+" & "+inAndOuts.get(1)[i].name+")";
            if((i+1)!=inAndOuts.get(0).length)
                assignStatement = assignStatement+" & ";
        }
        assignStatement = assignStatement + ";";
        assign(assignStatement);
    }
    
    public void orBD(Part myPart, ArrayList<Wire[]> inAndOuts){
        if(inAndOuts.get(0).length>inAndOuts.get(1).length){
            Wire[] temp = new Wire[inAndOuts.get(0).length];
            for(int i=0;i<inAndOuts.get(1).length;i++)
                temp[i]=inAndOuts.get(1)[i];
            inAndOuts.remove(1);
            inAndOuts.add(1,temp);
            for(int i=inAndOuts.get(1).length;i<inAndOuts.get(0).length;i++)
                inAndOuts.get(1)[i]=GND;
        }
        else if(inAndOuts.get(1).length>inAndOuts.get(0).length){
            Wire[] temp = new Wire[inAndOuts.get(1).length];
            for(int i=0;i<inAndOuts.get(0).length;i++)
                temp[i]=inAndOuts.get(0)[i];
            inAndOuts.remove(0);
            inAndOuts.add(0,temp);
            for(int i=inAndOuts.get(0).length;i<inAndOuts.get(1).length;i++)
                inAndOuts.get(0)[i]=GND;
        }
        String assignStatement=inAndOuts.get(2)[0].name+" = ";
        for(int i=0;i<inAndOuts.get(0).length;i++){
            assignStatement = assignStatement +"("+inAndOuts.get(0)[i].name+" | "+inAndOuts.get(1)[i].name+")";
            if((i+1)!=inAndOuts.get(0).length)
                assignStatement = assignStatement+" | ";
        }
        assignStatement = assignStatement + ";";
        assign(assignStatement);
    }
    
    public void invBD(Part myPart, ArrayList<Wire[]> inAndOuts){
        for(int i=0;i<inAndOuts.get(0).length;i++)
            assign(inAndOuts.get(1)[i].name+" = ~("+inAndOuts.get(0)[i].name+");");
    }
    
    /*
    Breaks down a multiplier part into gates
    */
    public void multiplierBD(Part myPart, ArrayList<Wire[]> inAndOuts){
        int bitSize=inAndOuts.get(0).length;
        if(bitSize==1){
            assign(inAndOuts.get(2)[0].name+" = "+inAndOuts.get(0)[0].name+" & "+inAndOuts.get(1)[0].name+";");
        }
        else{
            Wire[] newWires = new Wire[(bitSize*bitSize)-1];
            for(int i=0;i<((bitSize*bitSize)-1);i++){
                newWires[i]=newWire();
            }
            Wire[][] multWires = new Wire[bitSize*2][bitSize*2];
            int[] multWireInts=new int[bitSize*2];
            for(int i=0;i<bitSize*2;i++)
                multWireInts[i]=0;
            int spot=0;
            for(int i=0;i<bitSize;i++){
                for(int j=0;j<bitSize;j++){
                    if(i==0 && j==0)
                        assign(inAndOuts.get(2)[0].name+" = "+inAndOuts.get(0)[0].name+ " & "+inAndOuts.get(1)[0].name+";");
                    else{
                        assign(newWires[spot].name+" = "+inAndOuts.get(0)[i].name+" & "+inAndOuts.get(1)[j].name+";");
                        multWires[i+j][multWireInts[i+j]]=newWires[spot];
                        spot++;
                        multWireInts[i+j]++;
                    }
                }
            }
            boolean done = false;
            int next2Go = 1;
            while(!done){
                done = checkWallace(multWires);
                int[] numWires = new int[multWires.length];
                for(int i = 0; i < multWires.length; i++){
                    for(int j = 0; j < multWires[i].length; j++){
                        if(multWires[i][j] != null){
                            numWires[i]++;
                        }
                    }
                }
                for(int i=0;i<multWires.length;i++){
                    if(numWires[i]!=1 && numWires[i]!=0){
                        while(numWires[i]>1){
                            if(numWires[i]-3>=0){
                                Wire[] tempWires = new Wire[5];
                                for(int k=0;k<5;k++)
                                    tempWires[k]=newWire();
                                assign(tempWires[0].name+" = "+multWires[i][0].name+" ^ "+multWires[i][1].name+";");
                                assign(tempWires[1].name+" = "+tempWires[0].name+" ^ "+multWires[i][2].name+";");
                                assign(tempWires[2].name+" = "+tempWires[0].name+" & "+multWires[i][2].name+";");
                                assign(tempWires[3].name+" = "+multWires[i][0].name+" & "+multWires[i][1].name+";");
                                assign(tempWires[4].name+" = "+tempWires[2].name+" | "+tempWires[3].name+";");
                                for(int j = 0; j < (multWires[i].length-3); j++)
                                    multWires[i][j] = multWires[i][j+3];
                                for(int j = (multWires[i].length-3); j < (multWires[i].length-1); j++)
                                    multWires[i][j] = null;
                                int place = 0;
                                while(multWires[i][place] != null)
                                    place++;
                                multWires[i][place] = tempWires[1];
                                place = 0;
                                while(multWires[i+1][place] != null)
                                    place++;
                                multWires[i+1][place] = tempWires[4];
                                numWires[i] = numWires[i]-2;
                                numWires[i+1]++;
                            }
                            else if(numWires[i]-2 >= 0){
                                Wire[] tempWires = new Wire[2];
                                tempWires[0] = newWire();
                                tempWires[1] = newWire();
                                assign(tempWires[0].name+" = "+multWires[i][0].name+" ^ "+multWires[i][1].name+";");
                                assign(tempWires[1].name+" = "+multWires[i][0].name+" ^ "+multWires[i][1].name+";");
                                for(int j = 0; j < (multWires[i].length-2); j++)
                                    multWires[i][j] = multWires[i][j+2];
                                for(int j = (multWires[i].length-2); j < (multWires[i].length-1); j++)
                                    multWires[i][j] = null;
                                int place = 0;
                                while(multWires[i][place] != null)
                                    place++;
                                multWires[i][place] = tempWires[0];
                                place = 0;
                                while(multWires[i+1][place] != null)
                                    place++;
                                multWires[i+1][place] = tempWires[1];
                                numWires[i] = numWires[i]-1;
                                numWires[i+1]++;
                            }
                        }
                    }
                    else if(numWires[i] == 0){}
                    else if(numWires[i] == 1 && next2Go == i){
                        assign(inAndOuts.get(2)[i].name+" = "+multWires[i][0].name+";");
                        next2Go++;
                        multWires[i][0] = null;
                    }
                }
            }
        }
    }
    
    public boolean checkWallace(Wire[][] myWallace){
        for(int i = 0; i < myWallace.length; i++){
            for(int j = 0; j < myWallace[i].length; j++){
                if(myWallace[i][j] != null)
                    return false;
            }
        }
        return true;
    }
    
    /*
    Breaks down a multiplexer part into gates
    */
    public void muxBD(Part myPart, ArrayList<Wire[]> inAndOuts){
        int bitSize=inAndOuts.get(0).length;
        Wire[] newWires = new Wire[bitSize*2];
        for(int i=0;i<(bitSize*2);i++)
            newWires[i]=newWire();
        for(int i=0;i<bitSize;i++){
            assign(newWires[i*2].name+" = "+inAndOuts.get(0)[i].name+" & ~("+inAndOuts.get(2)[0].name+");");
            assign(newWires[(i*2)+1].name+" = "+inAndOuts.get(1)[i].name+" & "+inAndOuts.get(2)[0].name+";");
            assign(inAndOuts.get(3)[i].name+" = "+newWires[i*2].name+" | "+newWires[(i*2)+1].name+";");
        }
    }
    
    /*
    Breaks down a comparator part into gates
    */
    public void comparatorBD(Part myPart, ArrayList<Wire[]> inAndOuts){
        int bitSize=inAndOuts.get(0).length;
        Wire[] newWires;
        if(bitSize==1){
            AND and1 = new AND(newName("AND$"));
            AND and2 = new AND(newName("AND$"));
            XOR xor1 = new XOR(newName("XOR$"));
            Inverter inv1 = new Inverter(newName("INV$"));
            Inverter inv2 = new Inverter(newName("INV$"));
            currModule.parts.add(and1);
            currModule.parts.add(and2);
            currModule.parts.add(xor1);
            currModule.parts.add(inv1);
            currModule.parts.add(inv2);
            newWires = new Wire[2];
            inAndOuts.get(0)[0].ports.add(and1.ports.get(0));
            inAndOuts.get(1)[0].ports.add(inv1.ports.get(0));
            newWires[0].ports.add(inv1.ports.get(1));
            newWires[0].ports.add(and1.ports.get(1));
            if(inAndOuts.get(2).length==0)
                GND.ports.add(and1.ports.get(2));
            else
                inAndOuts.get(2)[0].ports.add(and1.ports.get(2));
            inAndOuts.get(0)[0].ports.add(xor1.ports.get(0));
            inAndOuts.get(1)[0].ports.add(xor1.ports.get(1));
            if(inAndOuts.get(3).length==0)
                GND.ports.add(xor1.ports.get(2));
            else
                inAndOuts.get(3)[0].ports.add(xor1.ports.get(2));
            inAndOuts.get(0)[0].ports.add(inv2.ports.get(0));
            inAndOuts.get(1)[0].ports.add(and2.ports.get(0));
            newWires[1].ports.add(inv2.ports.get(1));
            newWires[1].ports.add(and2.ports.get(1));
            if(inAndOuts.get(4).length==0)
                GND.ports.add(and2.ports.get(2));
            else
                inAndOuts.get(4)[0].ports.add(and2.ports.get(2));
        }
        else{
            int compNum = bitSize/4;
            newWires = new Wire[37];
            if(bitSize%4 != 0)
                compNum++;
            for(int i=0;i<compNum;i++){
                if(i!=0){
                    newWires[0]=newWires[36];
                    newWires[1]=newWires[35];
                    newWires[2]=newWires[34];
                }
                else{
                    newWires[0]=GND;
                    newWires[1]=VCC;
                    newWires[2]=GND;
                }
                for(int j=3;j<34;j++)
                    newWires[j]=newWire();
                assign(newWires[3].name+" = "+inAndOuts.get(0)[i*4].name+" ~& "+inAndOuts.get(1)[i*4].name+";");
                assign(newWires[4].name+" = "+inAndOuts.get(0)[(i*4)+1].name+" ~& "+inAndOuts.get(1)[(i*4)+1].name+";");
                assign(newWires[5].name+" = "+inAndOuts.get(0)[(i*4)+2].name+" ~& "+inAndOuts.get(1)[(i*4)+2].name+";");
                assign(newWires[6].name+" = "+inAndOuts.get(0)[(i*4)+3].name+" ~& "+inAndOuts.get(1)[(i*4)+3].name+";");
                assign(newWires[7].name+" = "+inAndOuts.get(1)[i*4].name+" & "+newWires[3].name+";");
                assign(newWires[8].name+" = "+newWires[3].name+" & "+inAndOuts.get(0)[i*4].name+";");
                assign(newWires[9].name+" = "+inAndOuts.get(1)[(i*4)+1].name+" & "+newWires[4].name+";");
                assign(newWires[10].name+" = "+newWires[4].name+" & "+inAndOuts.get(0)[(i*4)+1].name+";");
                assign(newWires[11].name+" = "+inAndOuts.get(1)[(i*4)+2].name+" & "+newWires[5].name+";");
                assign(newWires[12].name+" = "+newWires[5].name+" & "+inAndOuts.get(0)[(i*4)+2].name+";");
                assign(newWires[13].name+" = "+inAndOuts.get(1)[(i*4)+3].name+" & "+newWires[6].name+";");
                assign(newWires[14].name+" = "+newWires[6].name+" & "+inAndOuts.get(0)[(i*4)+3].name+";");
                assign(newWires[15].name+" = "+newWires[7].name+" ~| "+newWires[8].name+";");
                assign(newWires[16].name+" = "+newWires[9].name+" ~| "+newWires[10].name+";");
                assign(newWires[17].name+" = "+newWires[11].name+" ~| "+newWires[12].name+";");
                assign(newWires[18].name+" = "+newWires[13].name+" ~| "+newWires[14].name+";");
                assign(newWires[19].name+" = "+inAndOuts.get(0)[(i*4)+3].name+" & "+newWires[6].name+";");
                assign(newWires[20].name+" = "+inAndOuts.get(1)[(i*4)+3].name+" & "+newWires[6].name+";");
                assign(newWires[21].name+" = ("+inAndOuts.get(0)[(i*4)+2].name+" & "+newWires[5].name+" & "+newWires[18].name+");");
                assign(newWires[22].name+" = ("+inAndOuts.get(0)[(i*4)+1].name+" & "+newWires[4].name+" & "+newWires[18].name+" & "+newWires[17].name+");");
                assign(newWires[23].name+" = ("+inAndOuts.get(0)[i*4].name+" & "+newWires[3].name+" & "+newWires[18].name+" & "+newWires[17].name+" & "+newWires[16].name+");");
                assign(newWires[24].name+" = ("+newWires[18].name+" & "+newWires[16].name+" & "+newWires[17].name+" & "+newWires[15].name+" & "+newWires[2].name+");");
                assign(newWires[25].name+" = ("+newWires[18].name+" & "+newWires[17].name+" & "+newWires[16].name+" & "+newWires[15].name+" & "+newWires[1].name+");");
                assign(newWires[26].name+" = ("+newWires[1].name+" & "+newWires[15].name+" & "+newWires[16].name+" & "+newWires[17].name+" & "+newWires[18].name+");");
                assign(newWires[27].name+" = ("+newWires[0].name+" & "+newWires[15].name+" & "+newWires[16].name+" & "+newWires[17].name+" & "+newWires[18].name+");");
                assign(newWires[28].name+" = ("+newWires[16].name+" & "+newWires[17].name+" & "+newWires[18].name+" & "+newWires[3].name+" & "+inAndOuts.get(1)[i*4].name+");");
                assign(newWires[29].name+" = ("+newWires[17].name+" & "+newWires[18].name+" & "+newWires[4].name+" & "+inAndOuts.get(1)[(i*4)+1].name+");");
                assign(newWires[30].name+" = ("+newWires[18].name+" & "+newWires[5].name+" & "+inAndOuts.get(1)[(i*4)+2].name+");");
                assign(newWires[31].name+" = ("+newWires[19].name+" | "+newWires[21].name+" | "+newWires[22].name+" | "+newWires[23].name+" | "+newWires[24].name+" | "+newWires[25].name+");");
                assign(newWires[32].name+" = ("+newWires[18].name+" & "+newWires[17].name+" & "+newWires[1].name+" & "+newWires[16].name+" & "+newWires[15].name+");");
                assign(newWires[33].name+" = ("+newWires[26].name+" | "+newWires[27].name+" | "+newWires[28].name+" | "+newWires[29].name+" | "+newWires[30].name+" | "+newWires[20].name+");");
                if((i+1)==compNum){
                    if(inAndOuts.get(2).length!=0)
                        assign(inAndOuts.get(2)[0].name+" = !("+newWires[33].name+");");
                    else
                        assign(FLOAT.name+" = !("+newWires[33].name+");");
                    if(inAndOuts.get(3).length!=0)
                        assign(inAndOuts.get(3)[0].name+" = !("+newWires[32].name+");");
                    else
                        assign(FLOAT.name+" = !("+newWires[32].name+");");
                    if(inAndOuts.get(4).length!=0)
                        assign(inAndOuts.get(4)[0].name+" = !("+newWires[31].name+");");
                    else
                        assign(FLOAT.name+" = !("+newWires[31].name+");");
                }
                else{
                    newWires[34]=newWire();
                    newWires[35]=newWire();
                    newWires[36]=newWire();
                    assign(newWires[34].name+" = !("+newWires[33].name+");");
                    assign(newWires[35].name+" = !("+newWires[32].name+");");
                    assign(newWires[36].name+" = !("+newWires[31].name+");");
                }
            }
        }
    }
    
    /*
    Breaks down an adder part into gates
    */
    public void adderBD(Part myPart,ArrayList<Wire[]> inAndOuts){
        int bitSize=inAndOuts.get(0).length;
        Wire cout;
        if(inAndOuts.get(4).length!=0)
            cout = inAndOuts.get(4)[0];
        else
            cout=FLOAT;
        if(inAndOuts.get(2).length==0){
            inAndOuts.remove(2);
            inAndOuts.add(2,new Wire[1]);
            inAndOuts.get(2)[0]=GND;
        }
        for(int i=0;i<bitSize;i++){
            Wire[] myWires = new Wire[4];
            for(int j=0;j<4;j++){
                if(i==(bitSize-1)&&j==3)
                    myWires[j]=cout;
                else
                    myWires[j]=newWire();
            }
            if(inAndOuts.get(4).length!=1){
                inAndOuts.remove(4);
                inAndOuts.add(new Wire[1]);
            }
            inAndOuts.get(4)[0]=myWires[3];
            
            assign(myWires[0].name+" = "+inAndOuts.get(0)[i].name+" ^ "+inAndOuts.get(1)[i].name+";");
            assign(inAndOuts.get(3)[i].name+" = "+myWires[0].name+" ^ "+inAndOuts.get(2)[0].name+";");
            assign(myWires[1].name+" = "+myWires[0].name+" & "+inAndOuts.get(2)[0].name+";");
            assign(myWires[2].name+" = "+inAndOuts.get(0)[i].name+" & "+inAndOuts.get(1)[i].name+";");
            assign(myWires[3].name+" = "+myWires[1].name+" | "+myWires[2].name+";");
            if(i!=(bitSize-1))
                inAndOuts.get(2)[0]=myWires[3];
        }
    }
    
    /*
    Breaks down a subtractor part into gates
    */
    public void subtractorBD(Part myPart,ArrayList<Wire[]> inAndOuts){
        int bitSize=inAndOuts.get(0).length;
        Wire cout;
        if(inAndOuts.get(4).length!=0)
            cout = inAndOuts.get(4)[0];
        else
            cout=FLOAT;
        if(inAndOuts.get(2).length==0){
            inAndOuts.remove(2);
            inAndOuts.add(2,new Wire[1]);
            inAndOuts.get(2)[0]=VCC;
        }
        for(int i=0;i<bitSize;i++){
            Wire[] myWires = new Wire[5];
            for(int j=0;j<5;j++){
                if(i==(bitSize-1)&&j==4)
                    myWires[j]=cout;
                else
                    myWires[j]=newWire();
            }
            if(inAndOuts.get(4).length!=1){
                inAndOuts.remove(4);
                inAndOuts.add(new Wire[1]);
            }
            inAndOuts.get(4)[0]=myWires[3];
            
            assign(myWires[0].name+" = ~("+inAndOuts.get(1)[i].name+");");
            assign(myWires[1].name+" = "+inAndOuts.get(0)[i].name+" ^ "+myWires[0].name+";");
            assign(inAndOuts.get(3)[i].name+" = "+myWires[1].name+" ^ "+inAndOuts.get(2)[0].name+";");
            assign(myWires[2].name+" = "+myWires[1].name+" & "+inAndOuts.get(2)[0].name+";");
            assign(myWires[3].name+" = "+inAndOuts.get(0)[i].name+" & "+myWires[1].name+";");
            assign(myWires[4].name+" = "+myWires[2].name+" | "+myWires[3].name+";");
            if(i!=(bitSize-1))
                inAndOuts.get(2)[0]=myWires[4];
        }
    }
    
    /*
    Creates a new wire to be used
    */
    public Wire newWire(){
        currModule.addWire("MISC_"+numWires,1);
        numWires++;
        return currModule.wires.get(currModule.wires.size()-1);
    }
    
    public Wire newWire(int size){
        currModule.addWire("MISC_"+numWires,size);
        numWires++;
        return currModule.wires.get(currModule.wires.size()-1);
    }
}