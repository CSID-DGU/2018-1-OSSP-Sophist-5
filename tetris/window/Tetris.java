package com.tetris.window;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.tetris.network.GameClient;
import com.tetris.network.GameServer;

public class Tetris extends JFrame implements ActionListener{
	private static final long serialVersionUID = 1L;
	private GameServer server; //GameServer객체화
	private GameClient client; //GameClient객체화
	private TetrisBoard board = new TetrisBoard(this,client);
	private JMenuItem itemServerStart = new JMenuItem("서버로 접속하기");
	private JMenuItem itemClientStart = new JMenuItem("클라이언트로 접속하기");
	
	private boolean isNetwork;
	private boolean isServer;

	

	public Tetris() {
		JMenuBar mnBar = new JMenuBar();
		JMenu mnGame = new JMenu("게임하기");
		
		mnGame.add(itemServerStart);
		mnGame.add(itemClientStart);
		mnBar.add(mnGame);
		
		this.setJMenuBar(mnBar); //기본 UI를 가져옴 
		
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.getContentPane().add(board); // 컨텐트패널에 board의 화면을 넣는다.
		
		this.setResizable(false); //사이즈 고정 
		this.pack(); //조립된 UI를 창에 맞게 조절 
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((size.width-this.getWidth())/2,(size.height-this.getHeight())/2);
		this.setVisible(true);
		
		itemServerStart.addActionListener(this); //serverstart 액션리스너
		itemClientStart.addActionListener(this); //clientstart 액션리스터
		this.addWindowListener(new WindowAdapter() {//이벤트 처리를 위한 리스너 객체
			//입력을 받고 닫아주기 위한 메소드 정의
			@Override
			public void windowClosing(WindowEvent e) {
				if(client!=null ){
					
					if(isNetwork){
						client.closeNetwork(isServer);
					}
				}else{
					System.exit(0);
				}
				
			}
			
		});
		
	} 
	
	// 화면구성 및 액션리스너 실행

	@Override
	//사용자 버튼 클릭후 actionperformed메소드 실행
	//액션버튼 이후의 작업
	public void actionPerformed(ActionEvent e) {
		
		String ip=null;
		int port=0;
		String nickName=null;
		//server start
		if(e.getSource() == itemServerStart){ 
			
			String sp = JOptionPane.showInputDialog("port번호를 입력해주세요","9500");
			if(sp!=null && !sp.equals(""))port = Integer.parseInt(sp);
			nickName = JOptionPane.showInputDialog("닉네임을 입력해주세요","이름없음");
			
			if(port!=0){
				if(server == null) server = new GameServer(port);
				server.startServer();
				//서버온, 소켓연결 
				try {ip = InetAddress.getLocalHost().getHostAddress();
				} catch (UnknownHostException e1) {e1.printStackTrace();}
				if(ip!=null){
					client = new GameClient(this,ip,port,nickName);
					if(client.start()){
						//클라이언트서버 소켓도 만들어 연결함.
						itemServerStart.setEnabled(false);
						itemClientStart.setEnabled(false);
						//버튼비활성화 ( 서버에서만 게임시작이 가능함 )
						board.setClient(client);
						//board에 현재 서버로 들어온 유저를 컨트롤에 연결 
						board.getBtnStart().setEnabled(true);
						//시작버튼 활성화 
						board.startNetworking(ip, port, nickName);
						//TetrisBoard에 ip, port, nickName을 넘겨줌 
						isNetwork = true; // server.startServer()를 통해 ServerSocket 열려있는 상태 
						isServer = true;
						//서버, 네트워크 온
					}
				}
			}
		}else if(e.getSource() == itemClientStart){
			try {
				ip = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
			
			ip = JOptionPane.showInputDialog("IP를 입력해주세요.",ip);
			String sp = JOptionPane.showInputDialog("port번호를 입력해주세요","9500");
			if(sp!=null && !sp.equals(""))port = Integer.parseInt(sp);
			nickName = JOptionPane.showInputDialog("닉네임을 입력해주세요","이름없음");

		
			if(ip!=null){
				client = new GameClient(this,ip,port,nickName);
				if(client.start()){
					itemServerStart.setEnabled(false);
					itemClientStart.setEnabled(false);
					board.setClient(client);
					//board를 통해 컨트롤러에 현재 클라이언트를 연결
					board.startNetworking(ip, port, nickName);
					isNetwork = true;
					//클라이언트도 소켓으로 연결 
				}
			}
		}
	}

	public void closeNetwork(){
		//소켓을 통해 
		isNetwork = false;
		client = null;
		itemServerStart.setEnabled(true);
		itemClientStart.setEnabled(true);
		board.setPlay(false);
		board.setClient(null);
	}

	public JMenuItem getItemServerStart() {return itemServerStart;}
	public JMenuItem getItemClientStart() {return itemClientStart;}
	public TetrisBoard getBoard(){return board;}
	public void gameStart(int speed){board.gameStart(speed);}
	public boolean isNetwork() {return isNetwork;}
	public void setNetwork(boolean isNetwork) {this.isNetwork = isNetwork;}
	public void printSystemMessage(String msg){board.printSystemMessage(msg);}
	public void printMessage(String msg){board.printMessage(msg);}
	public boolean isServer() {return isServer;}
	public void setServer(boolean isServer) {this.isServer = isServer;}

	public void changeSpeed(Integer speed) {board.changeSpeed(speed);}
}
