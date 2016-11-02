/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vlt;

/**
 *
 * @author Parker
 */
public class Splitter extends Part{
    
    public Splitter(String name, int size, int number){
        super(name+number);
        ports.add(new Port(name+number+"-in",this));
        for(int i = 0; i < size; i++){
            ports.add(new Port(name+number+"-"+i,this));
        }
    }
}
