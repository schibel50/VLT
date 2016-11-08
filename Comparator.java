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
public class Comparator extends Part{
    
    public Comparator(String name){
        super(name);
        ports.add(new Port("a",this));
        ports.add(new Port("b",this));
        ports.add(new Port("Greater",this));
        ports.add(new Port("Equal",this));
        ports.add(new Port("Less",this));
    }
}
//">>","<<","~","&","|","~&","~|","~^","^~","%"