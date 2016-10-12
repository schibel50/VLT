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
public class VLT {

    static ArrayList<String> code;
    static String filename;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        filename=args[0];
        filename="maxfinder.v";
        
        Loader loader = new Loader(code);
        loader.loadFile(filename);
        Compiler compiler = new Compiler(code);
//        loader.saveFile("output.txt");
    }
    
}
