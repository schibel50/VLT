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
public class Compiler {
    public ArrayList<String> code;//all the code being evaluated
    public ArrayList<Module> modules;//all of the modules being evaluated
    //names of pre-defined modules
    public String[] preDefinedMods = {"and","or","xor","nand","nor","xnor","buf","not","bufif1","bufif0","notif1","notif0","dff"};
    public Module currModule;//the current module being evaluated
    public EWriter edif;//where to write the EDIF
    //all of the possible operators
    public String[] operators = {"+","-",">=","<=",">","<","==","!=","!","?",":","*","&","|","^","~","&&","||","~&","~|","~^","^~",">>","<<","%","/","=","1","0","(",")","{","}"};

    //keep track of the number of wires and each component/part
    public int numWires;
    public int numAdders;
    public int numSubtractors;
    public int numComparators;
    public int numMuxs;
    public int numInverters;
    public int numORs;
    public int numANDs;
    public int numXORs;
    public int numNANDs;
    public int numNORs;
    public int numXNORs;
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
    
    //wires that lead to ground, vcc, or indicate a floating pin
    public Wire GND;
    public Wire VCC;
    public Wire FLOAT;
    
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
        numNANDs=0;
        numNORs=0;
        numXNORs=0;
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
                if(start != -1 && end != -1 && tempIO != null && !(tempName.equals("dff"))){
                    modules.add(new Module(tempName,start,end,tempIO));
                    tempName = "";
                    start = -1;
                    end = -1;
                    tempIO = null;
                }
                else if(tempName.equals("dff")){
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
                                boolean gndVcc = false;
                                for(int k = 0; k < temp.length; k++){
                                    temp[k] = temp[k].trim();
                                    if(temp[k].equals("gnd")||temp[k].equals("vcc"))
                                        gndVcc=true;
                                }
                                if(!gndVcc){
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
                                currModule.parts.add(new Module_Part(modName,tempPorts));
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
        for(int i=0;i<modules.size();i++){
            insertMod(modules.get(i));
        }
        if(!GND.ports.get(0).name.equals("GND"))
            GND.ports.add(0,new Port("GND",(byte)1));
        if(!VCC.ports.get(0).name.equals("VCC")){
            VCC.ports.add(0,new Port("VCC",(byte)1));
        }
//        part2gate();
        redundantIOPorts();
        modules.get(0).wires.remove(2);
        addBuffers();
        edif = new EWriter(modules.get(0));
        edif.write();
    }
    
    public void preMod(String[] myPorts, int myMod){
        String type="";
        Wire[] myWires = new Wire[myPorts.length];
        for(int i = 0; i < myPorts.length; i++){
            for(Wire wire : currModule.wires){
                for(Port port : wire.ports){
                    if(port.name.equals(myPorts[i]))
                        myWires[i] = wire;
                    else if(wire.name.equals("gnd")&&myPorts[i].equals("gnd"))
                        myWires[i]=GND;
                    else if(wire.name.equals("vcc")&&myPorts[i].equals("vcc"))
                        myWires[i]=VCC;
                }
            }
        }
        for(int i=0;i<myPorts.length;i++){
            if(myWires[i]==null){
                myWires[i]=FLOAT;
            }
        }
        switch(myMod){
            case 0:
                type="&";
                break;
            case 1:
                type="|";
                break;
            case 2:
                type="^";
                break;
            case 3:
                type="&";
                break;
            case 4:
                type="|";
                break;
            case 5:
                type="~^";
                break;
        }
        if(myMod < 3 || myMod == 5){
            String tempAssign=myWires[0].name+" = ";
            tempAssign+=myWires[1].name;
            for(int i = 2; i < myWires.length; i++){
                tempAssign+=" "+type+" "+myWires[i].name;
            }
            tempAssign+=";";
            assign(tempAssign);
        }
        else if(myMod<5){
            String tempAssign=myWires[0].name+" = ";
            tempAssign+=myWires[1].name+"~(";
            for(int i = 2; i < myWires.length; i++){
                tempAssign+=" "+type+" "+myWires[i].name;
            }
            tempAssign+=");";
            assign(tempAssign);
        }
        else if(myMod==6){
            Buffer newBuff = new Buffer("Buff"+numBuffs);
            numBuffs++;
            myWires[myWires.length-1].ports.add(newBuff.ports.get(0));
            for(int i = 0; i < myWires.length-1;i++)
                myWires[i].ports.add(newBuff.ports.get(1));
            currModule.parts.add(newBuff);
        }
        else if(myMod==7){
            Inverter newInv = new Inverter("INV"+numInverters);
            numInverters++;
            myWires[myWires.length-1].ports.add(newInv.ports.get(0));
            for(int i = 0; i < myWires.length-1;i++)
                myWires[i].ports.add(newInv.ports.get(1));
            currModule.parts.add(newInv);
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
            DFF newDff = new DFF("DFF"+numDFFs);numDFFs++;
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
                    currBuff = new Buffer("BUFF"+numBuffs);numBuffs++;
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
    
    /*
    After all of the modules have been evaluated, this method inserts
    modules into the main module wherever they were evaualated in 
    the code.
    */
    public void insertMod(Module module){
        for(int i=0;i<module.parts.size();i++){
            if(module.parts.get(i) instanceof Module_Part){
                String[] myIONames;
                ArrayList<Wire[]> wireSpots = new ArrayList<Wire[]>();
                for(int j=0;j<modules.size();j++){
                    if(module.parts.get(i).name.equals(modules.get(j).name)){
                        myIONames = modules.get(j).ioNames;
                        Module newMod = new Module("temp",-1,-1,myIONames);
                        ArrayList<Wire> newWires = new ArrayList<Wire>();
                        ArrayList<Part> newParts = new ArrayList<Part>();
                        for(Wire wire : modules.get(j).wires){
                            Wire temp = new Wire(wire);
                            newWires.add(temp);
                        }
                        for(Part part : modules.get(j).parts){
                            Part temp = new Part(part);
                            temp.name+="_"+numExtras;numExtras++;
                            for(Wire wire : newWires){
                                for(int k=0;k<wire.ports.size();k++){
                                    if(wire.ports.get(k).part!=null){
                                        if(wire.ports.get(k).part.name.equals(part.name))
                                            wire.ports.get(k).part=temp;
                                    }
                                }
                            }
                            newParts.add(temp);
                        }
                        newMod.parts=newParts;
                        newMod.wires=newWires;
                        for(int k=0;k<module.parts.get(i).ports.size();k++){
                            if(module.parts.get(i).ports.get(k).name.charAt(module.parts.get(i).ports.get(k).name.length()-1)==']'){
                                wireSpots.add(new Wire[1]);
                                int a=module.parts.get(i).ports.get(k).name.length()-1;
                                while(module.parts.get(i).ports.get(k).name.charAt(a)!='[')
                                    a--;
                                String tempName=module.parts.get(i).ports.get(k).name.substring(0,a)+"_"+module.parts.get(i).ports.get(k).name.substring(a+1,module.parts.get(i).ports.get(k).name.length()-1);
                                for(Wire wire : module.wires){
                                    if(wire.name.equals(tempName)){
                                        wireSpots.get(wireSpots.size()-1)[0]=wire;
                                    }
                                }
                            }
                            else{
                                int size=0;
                                for(Wire wire : module.wires){
                                    if(wire.name.equals(module.parts.get(i).ports.get(k).name))
                                        size++;
                                }
                                wireSpots.add(new Wire[size]);
                                if(size!=1){
                                    for(Wire wire : module.wires){
                                        if(wire.name.contains(module.parts.get(i).ports.get(k).name)){
                                            int a=wire.name.length()-1;
                                            while(wire.name.charAt(a)!='_')
                                                a--;
                                            wireSpots.get(wireSpots.size()-1)[Integer.parseInt(wire.name.substring(a+1))]=wire;
                                        }
                                    }
                                }
                                else{
                                    for(Wire wire : module.wires)
                                        if(wire.name.equals(module.parts.get(i).ports.get(k).name))
                                            wireSpots.get(wireSpots.size()-1)[0]=wire;
                                }
                            }
                        }
//                        for(int k=0;k<newMod.parts.size();k++){
//                            for(int m=0;m<newMod.wires.size();m++){
//                                for(int l=0;l<newMod.wires.get(m).ports.size();l++){
//                                    if(newMod.wires.get(m).ports.get(l).part!=null &&
//                                            !newMod.wires.get(m).ports.get(l).part.name.equals(newMod.parts.get(k).name))
//                                        newMod.wires.get(m).ports.get(l).part=newMod.parts.get(k);
//                                }
//                            }
//                        }
                        for(int k=0;k<newMod.parts.size();k++){
                            Part temp = new Part(newMod.parts.get(k));
                            module.parts.add(temp);
                        }
                        ArrayList<Wire[]> wireSpots2 = new ArrayList<Wire[]>();
                        for(int k=0;k<myIONames.length;k++){
                            int size=0;
                            for(int l=0;l<newMod.wires.size();l++){
                                if(newMod.wires.get(l).name.contains(myIONames[k]))
                                    size++;
                            }
                            if(size!=0){
                                wireSpots2.add(new Wire[size]);
                                if(size==1){
                                    for(int l=0;l<newMod.wires.size();l++){
                                        if(newMod.wires.get(l).name.contains(myIONames[k]))
                                            wireSpots2.get(wireSpots2.size()-1)[0]=newMod.wires.get(l);
                                    }
                                }
                                else{
                                    for(int l=0;l<newMod.wires.size();l++){
                                        if(newMod.wires.get(l).name.contains(myIONames[k])){
                                            int a=newMod.wires.get(l).name.length()-1;
                                                while(newMod.wires.get(l).name.charAt(a)!='_')
                                                    a--;
                                                wireSpots.get(wireSpots.size()-1)[Integer.parseInt(newMod.wires.get(l).name.substring(a+1))]=newMod.wires.get(l);
                                        }
                                    }
                                }
                            }
                        }
                        for(int k=0;k<wireSpots2.size();k++){
                            for(int l=0;l<wireSpots2.get(k).length;l++){
                                for(int m=0;m<wireSpots2.get(k)[l].ports.size();m++){
                                    if(k<wireSpots.size())
                                        wireSpots.get(k)[l].ports.add(wireSpots2.get(k)[l].ports.get(m));
                                    else
                                        FLOAT.ports.add(wireSpots2.get(k)[l].ports.get(m));
                                }
                            }
                        }
                        for(int k=0;k<newMod.wires.size();k++){
                            boolean add=true;
                            for(int l=0;l<myIONames.length;l++){
                                if(newMod.wires.get(k).name.contains(myIONames[l]))
                                    add=false;
                            }
                            if(add){
                                boolean justAdd=true;
                                for(int l=0;l<newMod.wires.get(k).ports.size();l++){
                                    for(int m=0;m<myIONames.length;m++){
                                        if(newMod.wires.get(k).ports.get(l).name.equals(myIONames[m])){
                                            for(int n=0;n<newMod.wires.get(k).ports.size();n++){
                                                if(newMod.wires.get(k).ports.get(n).part!=null)
                                                    for(int o=0;o<wireSpots.get(m).length;o++){
                                                        wireSpots.get(m)[o].ports.add(newMod.wires.get(k).ports.get(n));
                                                        justAdd=false;
                                                    }
                                            }
                                        }
                                    }
                                }
                                if(justAdd){
                                    Wire temp = new Wire(newMod.wires.get(k));
                                    temp.name+="_"+numExtraWires;
                                    numExtraWires++;
                                    module.wires.add(temp);
                                }
                            }
                        }
//                        for(int k=0;k<wireSpots.size();k++){
//                            for(int l=0;l<myIONames.length;l++){
//                                for(int m=0;m<wireSpots.get(k).length;m++){
//                                    for(int n=0;n<wireSpots.get(k)[m].ports.size();n++){
//                                        if(wireSpots.get(k)[m].ports.get(n).name.equals(myIONames[l]))
//                                            if(wireSpots.get(k)[m].ports.get(n).part==null)
//                                                wireSpots.get(k)[m].ports.remove(n);
//                                    }
//                                }
//                            }
//                        }
                        for(int k=0;k<wireSpots.size();k++){
                            for(int m=0;m<wireSpots.get(k).length;m++){
                                for(int n=0;n<wireSpots.get(k)[m].ports.size();n++){
                                    if(wireSpots.get(k)[m].ports.get(n).part==null&&!(wireSpots.get(k)[m].ports.get(n).name.equals(wireSpots.get(k)[m].name)))
                                        wireSpots.get(k)[m].ports.remove(n);
                                }
                            }
                        }
                        module.parts.remove(i);
                        newMod.clear();
                        i--;
                    }
                }
            }
        }
    }
    
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
        if(!(myStatements.get(0).equals("gnd")||myStatements.get(0).equals("vcc"))){
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
                if(output.size()>1)
//                    assign(output.get(k)+" = "+output.get(k+1)+";");
                    currModule.getWire(output.get(k+1)).ports.addAll(currModule.getWire(output.get(k)).ports);
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
                        wire = newWire();
                        assign(wire.name+" = "+ops[1]+" ? vcc : gnd;");
                        wire2 = newWire();
                        assign(wire2.name+" = ("+wire.name+") ? "+list.get(k)+" : " + ifFalse + ";");
                        list3.add(wire2.name);
                        list3.add(list.get(k+1));
                    }
                }
                
                for(int k=0;k<list2.size();k+=2){
                    if(list.size()>1){
                        wire = newWire();
                        assign(wire.name+" = "+ops[1]+" ? vcc : gnd;");
                        wire2 = newWire();
                        assign(wire2.name+" = ("+wire.name+") ? gnd : " + list2.get(k) + ";");
                        list3.add(wire2.name);
                        list3.add(list2.get(k+1));
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
                        Wire wire = newWire();
                        assign(wire.name+" = ("+in+"_"+i+") ? "+ifOne+" : "+cases.get(w).get(j)+";");
                        list1.add(wire.name);
                        list1.add(cases.get(w).get(j+1));
                        ifOne="gnd";
                    }
                
                if(cases.get(w+1)!=null){
                    if(!cases.get(w+1).isEmpty()) //ensure that there is a case for current value +1
                        for(int k=0;k<cases.get(w+1).size();k+=2){ //loop through all the rest of the statements in the odd case
                            Wire wire = newWire();
                            assign(wire.name+" = ("+in+"_"+i+") ? "+cases.get(w+1).get(k)+" : gnd;");
                            list1.add(wire.name);
                            list1.add(cases.get(w+1).get(k+1));
                        }
                }
            }
            
            out.set(w/2,list1);
        }
        if(out.size()>=2)
            return caseHelper(out,def,in,i+1);
        return list1;
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
        NAND newNand = new NAND("NAND_"+numNANDs);numNANDs++;
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
        NOR newNor = new NOR("NOR_"+numNORs);numNORs++;
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
        XNOR newXnor = new XNOR("XNOR_"+numXNORs);numXNORs++;
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
        AND newAnd = new AND("AND_"+numANDs);numANDs++;
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
        OR newOr = new OR("OR_"+numORs);numORs++;
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
        XOR newXor = new XOR("XOR_"+numXORs);numXORs++;
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
        N_Inverter newInvert = new N_Inverter("INV_"+numInverters);numInverters++;
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
        Comparator newComp = new Comparator("COMP_"+numComparators);numComparators++;
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
                OR newOR = new OR("OR_"+numORs);
                currModule.parts.add(newOR);
                myWires[0].ports.add(newComp.ports.get(2));
                myWires[0].ports.add(newOR.ports.get(0));
                myWires[1].ports.add(newComp.ports.get(3));
                myWires[1].ports.add(newOR.ports.get(1));
                myWires[2].ports.add(newOR.ports.get(2));
                Wire[] newWires;
                if(myWires.length==4){
                    Inverter newInv = new Inverter("INV_"+numInverters);numInverters++;
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
                OR newOR2 = new OR("OR_"+numORs);
                currModule.parts.add(newOR2);
                myWires[0].ports.add(newComp.ports.get(4));
                myWires[0].ports.add(newOR2.ports.get(0));
                myWires[1].ports.add(newComp.ports.get(3));
                myWires[1].ports.add(newOR2.ports.get(1));
                myWires[2].ports.add(newOR2.ports.get(2));
                Wire[] newWires2;
                if(myWires.length==4){
                    Inverter newInv = new Inverter("INV_"+numInverters);numInverters++;
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
                    Inverter newInv = new Inverter("INV_"+numInverters);numInverters++;
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
                    Inverter newInv = new Inverter("INV_"+numInverters);numInverters++;
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
                    Inverter newInv = new Inverter("INV_"+numInverters);numInverters++;
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
                    Inverter newInv = new Inverter("INV_"+numInverters);numInverters++;
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
        Mux newMux = new Mux("MUX_"+numMuxs);numMuxs++;
        currModule.parts.add(newMux);
        for(int i=0;i<bitSize;i++){
            if(i<wireSpots.get(in1).length)
                wireSpots.get(in1)[i].ports.add(newMux.ports.get(0));
            else
                GND.ports.add(newMux.ports.get(0));
        }
        for(int i=0;i<bitSize;i++){
            if(i<wireSpots.get(in2).length)
                wireSpots.get(in2)[i].ports.add(newMux.ports.get(1));
            else
                GND.ports.add(newMux.ports.get(1));
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
        Multiplier newMultiplier = new Multiplier("MULT_"+numMultipliers);
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
        Adder newAdder = new Adder("ADD_"+numAdders);
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
        Subtractor newSubtractor = new Subtractor("SUB_"+numSubtractors);
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
            AND and1 = new AND("AND_"+numANDs);numANDs++;
            AND and2 = new AND("AND_"+numANDs);numANDs++;
            XOR xor1 = new XOR("XOR_"+numXORs);numXORs++;
            Inverter inv1 = new Inverter("INV_"+numInverters);numInverters++;
            Inverter inv2 = new Inverter("INV_"+numInverters);numInverters++;
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
}