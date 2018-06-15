package com.tetris.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.tetris.classes.Block;
import com.tetris.classes.TetrisBlock;
import com.tetris.window.Tetris;
import com.tetris.window.TetrisBoard;

//---------------------[ 클라이언트 ]---------------------
public class GameClient implements Runnable{
	private Tetris tetris;
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	//서버 IP
	private String ip;
	private int port;
	private String name;
	public int index;
	private boolean isPlay;
	
	//생성자
	public GameClient(Tetris tetris,String ip, int port, String name){
		this.tetris = tetris;
		this.ip = ip;
		this.port = port;
		this.name = name;
		
	}//GameClient()

	public boolean start(){
		return this.execute();	
	}

	//소켓 & IO 처리
	public boolean execute(){
		try{
			socket = new Socket(ip,port);
			ip = InetAddress.getLocalHost().getHostAddress();
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
			System.out.println("클라이언트가 실행 중입니다.");
		}catch(UnknownHostException e){
			e.printStackTrace();
			return false;
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}

		tetris.getBoard().clearMessage();
		
		//이름보내기
		DataShip data = new DataShip();
		data.setIp(ip);
		data.setName(name);
		send(data);
		
		//리스트받아오기
		printSystemMessage(DataShip.PRINT_SYSTEM_OPEN_MESSAGE);
		//리스트에 추가하기
		printSystemMessage(DataShip.PRINT_SYSTEM_ADDMEMBER_MESSAGE);
		//인덱스받아오기
		setIndex();
		//스레드
		Thread t = new Thread(this);
		t.start();
		
		return true;
	}//execute()

	
	//Run : 서버의 명령을 기다림.
	public void run(){
		DataShip data = null;
		while(true){
			try{
				data = (DataShip)ois.readObject(); 
			}catch(IOException e){e.printStackTrace();break;
			}catch(ClassNotFoundException e){e.printStackTrace();}


			//서버로부터 DataShip Object를 받아옴.
			if(data == null) continue;
			if(data.getCommand() == DataShip.CLOSE_NETWORK){
				reCloseNetwork();
				break;
			}else if(data.getCommand() == DataShip.SERVER_EXIT){
				closeNetwork(false);
			}else if(data.getCommand() == DataShip.GAME_START){
				reGameStart(data.isPlay(), data.getMsg(), data.getSpeed(), data.getgame_mode());
			}else if(data.getCommand() == DataShip.ADD_BLOCK){
				if(isPlay)
					reAddBlock(data.getMsg(), data.getNumOfBlock(), data.getIndex());
			}else if(data.getCommand() == DataShip.SET_INDEX){
				reSetIndex(data.getIndex());
			}else if(data.getCommand() == DataShip.GAME_OVER){
				if(index == data.getIndex()) isPlay = data.isPlay();
				reGameover(data.getMsg(), data.getTotalAdd());
			}else if(data.getCommand() == DataShip.PRINT_MESSAGE){
				rePrintMessage(data.getMsg());
			}else if(data.getCommand() == DataShip.PRINT_SYSTEM_MESSAGE){
				rePrintSystemMessage(data.getMsg());
			}else if(data.getCommand() == DataShip.GAME_WIN){
				rePrintSystemMessage(data.getMsg()+"\nTOTAL ADD : "+data.getTotalAdd());
				tetris.getBoard().setPlay(false);
			}else if(data.getCommand() == DataShip.GAME_MODE) {
				tetris.getBoard().mode_number = data.getgame_mode();
			}else if(data.getCommand() == DataShip.ITEM_BLIND) {
				if(isPlay)
					reBlindMap(data.getMsg(), data.getIndex());
			}else if(data.getCommand() == DataShip.ITEM_CLEAR) {
				if(isPlay) {
					reClearMessage(data.getMsg(), data.getIndex());
					if(data.getIndex() == 1) {
						for (int y = 0 ; y < 21 ; y++ ) {
							for(int x = 0 ; x < 10 ; x++) {
								tetris.getBoard().map1[y][x] = null;
							}
						}
					}else if(data.getIndex() == 2) {
						for (int y = 0 ; y < 21 ; y++ ) {
							for(int x = 0 ; x < 10 ; x++) {
								tetris.getBoard().map2[y][x] = null;
							}
						}
					}else if(data.getIndex() == 3) {
						for (int y = 0 ; y < 21 ; y++ ) {
							for(int x = 0 ; x < 10 ; x++) {
								tetris.getBoard().map3[y][x] = null;
							}
						}
					}else if(data.getIndex() == 4) {
						for (int y = 0 ; y < 21 ; y++ ) {
							for(int x = 0 ; x < 10 ; x++) {
								tetris.getBoard().map4[y][x] = null;
							}
						}
					}else if(data.getIndex() == 5) {
						for (int y = 0 ; y < 21 ; y++ ) {
							for(int x = 0 ; x < 10 ; x++) {
								tetris.getBoard().map5[y][x] = null;
							}
						}
					}
				}
			}else if(data.getCommand() == DataShip.BOARD_INFO1) {
				if(this.index != 1) {
					tetris.getBoard().setblocklist1(data.getBlock1(), data.get_map1_info());
				}
			}else if(data.getCommand() == DataShip.BOARD_INFO2) {
				if(this.index != 2) {
					tetris.getBoard().setblocklist2(data.getBlock2(), data.get_map2_info());
				}
			}else if(data.getCommand() == DataShip.BOARD_INFO3) {
				if(this.index != 3) {
					tetris.getBoard().setblocklist3(data.getBlock3(), data.get_map3_info());
				}
			}else if(data.getCommand() == DataShip.BOARD_INFO4) {
				if(this.index != 4) {
					tetris.getBoard().setblocklist4(data.getBlock4(), data.get_map4_info());
				}
			}else if(data.getCommand() == DataShip.BOARD_INFO5) {
				if(this.index != 5) {
					tetris.getBoard().setblocklist5(data.getBlock5(), data.get_map5_info());
				}
			}
			
			
		}//while(true)
		
		
	}//run()


	// 서버에게 요청함
	public void send(DataShip data){
		try{
			oos.writeObject(data); 
			oos.flush();
		}catch(IOException e){
			e.printStackTrace();
		}
	}//sendData()
	
	
	
	public void sendblockinfo(ArrayList<Block> b_l, Block[][] m) {
	//	System.out.println(this.index + "번에서 블락인포보냄");
		if(this.index == 1) {
	//		System.out.println("센드1");
		DataShip data = new DataShip(DataShip.BOARD_INFO1);		
		data.setBlock1(b_l); data.set_map1_info(m);
	//	System.out.println("b_l의 사이즈는" + b_l.size());	
		send(data);
		}
		else if(this.index == 2) {
	//		System.out.println("센드2");
			DataShip data = new DataShip(DataShip.BOARD_INFO2);		
			data.setBlock2(b_l); data.set_map2_info(m);
	//		System.out.println("b_l의 사이즈는" + b_l.size());
			send(data);
		}
		else if(this.index == 3) {
	//		System.out.println("센드3");
			DataShip data = new DataShip(DataShip.BOARD_INFO3);				
			data.setBlock3(b_l);data.set_map3_info(m);
	//		System.out.println("b_l의 사이즈는" + b_l.size());
			send(data);
		}
		else if(this.index == 4) {
	//		System.out.println("센드4");
			DataShip data = new DataShip(DataShip.BOARD_INFO4);				
			data.setBlock4(b_l);data.set_map4_info(m);
	//		System.out.println("b_l의 사이즈는" + b_l.size());
			send(data);
		}
		else if(this.index == 5) {
	//		System.out.println("센드5");
			DataShip data = new DataShip(DataShip.BOARD_INFO5);				
			data.setBlock5(b_l);data.set_map5_info(m);
	//		System.out.println("b_l의 사이즈는" + b_l.size());
			send(data);
		}
	}
	
	//요청하기 : 연결끊기
	public void closeNetwork(boolean isServer){
		DataShip data = new DataShip(DataShip.CLOSE_NETWORK);
		if(isServer) data.setCommand(DataShip.SERVER_EXIT);
		send(data);
	}
	//실행하기 : 연결끊기
	public void reCloseNetwork(){

		tetris.closeNetwork();
		try {
			ois.close();
			oos.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//요청하기 : 게임시작
	public void gameStart(int speed, int mode){
		DataShip data = new DataShip(DataShip.GAME_START);
		data.setSpeed(speed);
		data.setgame_mode(mode);
		send(data);
	}
	//실행하기 : 게임시작
	public void reGameStart(boolean isPlay, String msg, int speed, int mode){
		this.isPlay = isPlay;
		tetris.gameStart(speed, mode);
		rePrintSystemMessage(msg);
	}
	//요청하기 : 메시지
	public void printSystemMessage(int cmd){
		DataShip data = new DataShip(cmd);
		send(data);
	}
	//실행하기 : 메시지
	public void rePrintSystemMessage(String msg){
		tetris.printSystemMessage(msg);
	}
	public void addBlock(int numOfBlock){
		DataShip data = new DataShip(DataShip.ADD_BLOCK);
		data.setNumOfBlock(numOfBlock);
		send(data);
	}
	public void reAddBlock(String msg, int numOfBlock, int index){
		if(index != this.index)tetris.getBoard().addBlockLine(numOfBlock);
		if(index == 1) {
			for(int i = 0; i < numOfBlock; i++) {
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList2, tetris.getBoard().map2);
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList3, tetris.getBoard().map3);
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList4, tetris.getBoard().map4);
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList5, tetris.getBoard().map5);
			}
		}
		else if(index == 2) {
			for(int i = 0; i < numOfBlock; i++) {
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList1, tetris.getBoard().map1);
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList3, tetris.getBoard().map3);
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList4, tetris.getBoard().map4);
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList5, tetris.getBoard().map5);
			}
		}
		else if(index == 3) {
			for(int i = 0; i < numOfBlock; i++) {
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList1, tetris.getBoard().map1);
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList2, tetris.getBoard().map2);
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList4, tetris.getBoard().map4);
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList5, tetris.getBoard().map5);
			}
		}
		else if(index == 4) {
			for(int i = 0; i < numOfBlock; i++) {
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList1, tetris.getBoard().map1);
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList2, tetris.getBoard().map2);
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList3, tetris.getBoard().map3);
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList5, tetris.getBoard().map5);
			}
		}
		else if(index == 5) {
			for(int i = 0; i < numOfBlock; i++) {
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList1, tetris.getBoard().map1);
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList2, tetris.getBoard().map2);
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList3, tetris.getBoard().map3);
			tetris.getBoard().dropBoard2(20, -1, tetris.getBoard().blockList4, tetris.getBoard().map4);
			}
		}
		rePrintSystemMessage(msg);
	}
	
	//
	//요청하기 : 아이템 블라인드 
	public void blindMap() {
		DataShip data = new DataShip(DataShip.ITEM_BLIND);
		send(data);
	}
	
	//실행하기 : 아이템 블라인드
	public void reBlindMap(String msg, int index) {
		if(index != this.index)tetris.getBoard().reBlindMap();
		rePrintSystemMessage(msg);
	}
	//
	//
	
	public void clearMessage() {
		DataShip data = new DataShip(DataShip.ITEM_CLEAR);
		send(data);
	}
	
	public void reClearMessage(String msg, int index) {
		rePrintSystemMessage(msg);
	}
	
	
	
	public void setIndex(){
		DataShip data = new DataShip(DataShip.SET_INDEX);
		send(data);
	}
	public void reSetIndex(int index){
		this.index = index;
	}
	
	//요청하기 : 게임종료
	public void gameover(){
		DataShip data = new DataShip(DataShip.GAME_OVER);
		send(data);
	}
	public void reGameover(String msg, int totalAdd){
		tetris.printSystemMessage(msg);
		tetris.printSystemMessage("TOTAL ADD : "+totalAdd);
	}
	public void printMessage(String msg){
		DataShip data = new DataShip(DataShip.PRINT_MESSAGE);
		data.setMsg(msg);
		send(data);
	}
	public void rePrintMessage(String msg){
		tetris.printMessage(msg);
	}
	public void reChangSpeed(Integer speed) {
		tetris.changeSpeed(speed);
	}
}
