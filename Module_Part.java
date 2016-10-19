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
    
    public String moduleName;
    public Module_Part(String name, String modName, String[] portNames){
        super(name);
        this.moduleName = modName;
        for(int i = 0; i < portNames.length; i++)
            ports.add(new Port(portNames[i], this));
    }
}
