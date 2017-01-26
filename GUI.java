/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vlt;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.JTextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Ryan
 */
public class GUI extends JFrame{
    
    private JButton b1;
    private JButton b2;
    private JButton b3;
    private JTextArea t1;
    private JTextArea t2;
    private JFileChooser outputchooser;
    private JFileChooser inputchooser;
    private JOptionPane warning;
    private String input;
    private String output;
    
    boolean flag;
    
    public GUI(){
        super("VLT");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500,300);
        setResizable(false);
        
        JPanel pane = new JPanel();
        
        b1 = new JButton("Browse");
        b2 = new JButton("RUN");
        b3 = new JButton("Browse");
        t1 = new JTextArea("Input File Path",1,20);
        t2 = new JTextArea("Output File Path",1,20);
        
        b1.addActionListener(new MyActionListener());
        b2.addActionListener(new MyActionListener());
        b3.addActionListener(new MyActionListener());
        t1.setEditable(false);
        t2.setEditable(false);
        pane.add(t1);
        pane.add(t2);
        pane.add(b1);
        pane.add(b2);
        pane.add(b3);
        pane.setLayout(null);
        b1.setBounds(62,300/2,110,30);
        b3.setBounds(328,300/2,110,30);
        b2.setBounds(195,(300/4)*3,110,30);
        t1.setBounds(17,300/4,200,20);
        t2.setBounds(283,300/4,200,20);
//        b1.setBounds(62,250,110,30);
//        b3.setBounds(328,250,110,30);
//        b2.setBounds(195,375,110,30);
//        t1.setBounds(17,125,200,20);
//        t2.setBounds(283,125,200,20);
        t1.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        t2.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.setContentPane(pane);
        
        //only show .v files for the input
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "verilog files (*.v)", "v");
        
        //get rid of file creation and editting options in filechooser
        Boolean ro = UIManager.getBoolean("FileChooser.readOnly");  
        UIManager.put("FileChooser.readOnly", Boolean.TRUE); 
        inputchooser = new JFileChooser();
        UIManager.put("FileChooser.readOnly",ro);
        inputchooser.setFileFilter(filter);
        inputchooser.setApproveButtonText("Select");
        
        outputchooser = new JFileChooser();
        outputchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        outputchooser.setApproveButtonText("Select");
        
        warning = new JOptionPane();
        
        //pack();
        setVisible(true);
        
        flag = true;
    }
    
    /**
     * get name of input (.v) file
     */
    private void selectInput(){
        //this.setContentPane(filechooser);
        int choice = inputchooser.showDialog(this, "Select Input (.v) File");
        if (choice == JFileChooser.APPROVE_OPTION) {
            input = inputchooser.getSelectedFile().getPath();
            t1.setText(input);
        } else if (choice == JFileChooser.CANCEL_OPTION) {
            ;
        }
        //repaint();
    }
    
    /**
     * set name of output (.txt) file and directory
     */
    private void selectOutput(){
        int choice = outputchooser.showDialog(this, "Select Output Directory");
        if (choice == JFileChooser.APPROVE_OPTION) {
            output = outputchooser.getSelectedFile().getPath();
            t2.setText(output);
        } else if (choice == JFileChooser.CANCEL_OPTION) {
            ;
        }
    }
    
    /**
     * display warning messages
     */
    private void giveWarning(){
        JOptionPane.showMessageDialog(this,"ERROR: Missing input or output path");
    }
    
    public String getFileName(){
        return input;
    }
    
    public String getOutput(){
        return output;
    }
    
    class MyActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            if(e.getSource()==b1){
                selectInput();
            }else if(e.getSource()==b2){
                if(input==null||output==null){
                    giveWarning();
                }else{
                    flag = false;
                }
            }else if(e.getSource()==b3){
                selectOutput();
            }
        }
    }
}
