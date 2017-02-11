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
    static String outputEDIF;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GUI gui = new GUI();
        boolean cont=true;
        do{
            //spinlock the main thread
            while(gui.flag);
            filename = gui.getFileName();
            outputEDIF = gui.getOutput()+"\\output_EDIF.txt";

    //        Scanner scan = new Scanner(System.in);
    //        System.out.println("Enter name of .v file:");
    //        filename=scan.nextLine();

            code = new ArrayList<>();

            Loader loader = new Loader(code);
            loader.loadFile(filename);
            Compiler2 compiler = new Compiler2(code);
    //        Compiler compiler = new Compiler(code);
            try{
                compiler.moduleFinder();
                compiler.compile();
                loader.saveFile(outputEDIF,compiler.edif);
                if(gui.flag2){
                    translator.Translator translator = new translator.Translator(outputEDIF,"output_TPR.tpr",true,true);
                    translator.run();
                }
                cont=false;
            }catch(Exception e){
                gui.giveError();
            }
        }while(cont);
        gui.complete();
    }
    
}
