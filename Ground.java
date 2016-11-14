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
public class Ground extends Part{
    
    public Ground(String name){
        super(name);
        ports.add(new Port("port",this));
    }
    
}
