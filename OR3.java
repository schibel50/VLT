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
public class OR3 extends Part{

    public OR3(String name){
        super(name);
        ports.add(new Port("A",this));
        ports.add(new Port("B",this));
        ports.add(new Port("C",this));
        ports.add(new Port("Y",this));
    }  
}
