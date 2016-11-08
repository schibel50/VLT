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
public class Module_Part extends Part{
    
    public Module_Part(String name, String[] portNames){
        super(name);
        for(int i = 0; i < portNames.length; i++)
            ports.add(new Port(portNames[i], this));
    }
}