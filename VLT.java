/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vlt;

import java.util.ArrayList;
import java.util.Scanner;


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
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter name of .v file:");
        filename=scan.nextLine();
        
        code = new ArrayList<>();
        
        Loader loader = new Loader(code);
        loader.loadFile(filename);
        Compiler2 compiler = new Compiler2(code);
        compiler.moduleFinder();
        compiler.compile();
        loader.saveFile("output.txt",compiler.edif);
    }
    
}
