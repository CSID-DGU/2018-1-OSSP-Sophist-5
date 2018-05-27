package com.tetris.main;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.tetris.window.Tetris;

import javax.swing.JComboBox;
import javax.swing.ImageIcon;


public class select implements ActionListener
{
	public	String path = Tetris.class.getResource("").getPath();
	public	String file = new String(path + "tetris2.jpg");
	public	JFrame frame = new JFrame("MODE SELECT");
	public  JPanel p = new JPanel();
	public  JButton b = new JButton("SELECT");
	public  Font fot = new Font("ALGERIAN",Font.ITALIC,12);
    
    String[] mode=
    {
       "SERVER",
       "CLIENT"
    };
	
    JComboBox box =new JComboBox(mode);
    
	select(){      
	      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	      String path = Tetris.class.getResource("").getPath();
	      ImageIcon ic = new ImageIcon(path + "tetris.png");//상대경로로 변경
	      JLabel IbImage1 = new JLabel(ic);
	      
	      frame.add(new JLabel("CONNECT:"));
	
	      Container ct = frame.getContentPane();
	      ct.setLayout(new FlowLayout());
	      
	      ct.add(box);
	      
	      frame.add(p);
	      frame.add(b);
	      b.setFont(fot);
	      b.addActionListener(this);
	      frame.add(IbImage1);
	      frame.setSize(500,700);
	      frame.setVisible(true);
	      frame.setLocation((400),(50));
		}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()== b) {
			if(box.getSelectedItem().toString() == "SERVER") {
				System.out.println("server");
				Tetris abc = new Tetris();
				abc.itemServerStart.doClick();
			}
			else if(box.getSelectedItem().toString() == "CLIENT"){
				System.out.println("client");				
				Tetris abc = new Tetris();
				abc.itemClientStart.doClick();

			}
			frame.setVisible(false);  
			frame.dispose();   // 서버선택시 선택프레임 종료
		}
	}
}