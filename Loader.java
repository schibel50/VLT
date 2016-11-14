/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vlt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Ryan
 */
public class Loader {
    
    ArrayList<String> code;
    
    public Loader(ArrayList<String> code){
        this.code = code;
    }
    
    public void loadFile(String fn){
        try{
            FileReader reader = new FileReader(fn);
            BufferedReader buffer = new BufferedReader(reader);
            
            String line = buffer.readLine();
            
            int i;
            for(i=0;line!=null;i++){
                code.add(line);
                line = buffer.readLine();
                System.out.println(code.get(i)); //FOR TESTING PURPOSES ONLY
            }
            
            buffer.close();
        }catch(Exception e){
            if(e instanceof FileNotFoundException);
            else if(e instanceof IOException);
        }
    }
    
    public void saveFile(String fn, EWriter edif){
        try{
            File file = new File(fn);
            
            if(!file.exists())
                file.createNewFile();
            
            FileWriter writer = new FileWriter(fn);
            BufferedWriter buffer = new BufferedWriter(writer);
            
            for (String top : edif.top) {
                buffer.write(top);
                buffer.newLine();
            }
            
            for (String IO : edif.IOs) {
                buffer.write(IO);
                buffer.newLine();
            }
            
            for (String instance : edif.instances) {
                buffer.write(instance);
                buffer.newLine();
            }
            
            for (String net : edif.nets) {
                buffer.write(net);
                buffer.newLine();
            }
            
            buffer.close();
            
        }catch(IOException e){}
    }
}
