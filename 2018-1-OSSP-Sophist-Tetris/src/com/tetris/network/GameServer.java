package com.tetris.network;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.print.attribute.standard.PrinterLocation;

//TODO:--------------------------[ �ڵ鷯 ]--------------------------
class GameHandler extends Thread{
	private static boolean isStartGame;
	private static int maxRank;
	private int rank;
	
	private Socket socket;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private String ip;
	private String name;
	private int index;
	private int totalAdd=0;
	
	private ArrayList<GameHandler> list;
	private ArrayList<Integer> indexList;
	
	public GameHandler(Socket socket,ArrayList<GameHandler> list, int index, ArrayList<Integer> indexList){
		this.index = index;
		this.indexList = indexList;
		this.socket = socket;
		this.list = list;
		try{
			ois = new ObjectInputStream(socket.getInputStream()); //인풋스트림, 아웃풋스림을 열어줌
			oos = new ObjectOutputStream(socket.getOutputStream()); 
		}catch(IOException e){
			e.printStackTrace();
		}
		
		try{
			DataShip data = (DataShip)ois.readObject(); //data에 인풋스트림으로 들어온 객체 선언
			ip = data.getIp();// 인풋스트림을 통해 들어온 ip를 ip에 설정, 클라이언트 ip설정
			name = data.getName(); // 클라이언트 이름 설정
			
			data = (DataShip)ois.readObject();
			printSystemOpenMessage();
			printMessage(ip+":"+name+"���� �����Ͽ����ϴ�.");
		}catch(IOException e){ e.printStackTrace();
		}catch(ClassNotFoundException e){ e.printStackTrace();}
		
		
	}//GameHandler


//TODO:--------------------------[ 요청대기 ]-------------------------
	public void run(){ //연결대기중
		DataShip data = null;
		while(true){
			try{
				data = (DataShip)ois.readObject(); // 인풋스트림으로 데이터를 읽어들임
			}catch(IOException e){ e.printStackTrace(); break;
			}catch(ClassNotFoundException e){e.printStackTrace();}

			if(data==null)continue;
			
			if(data.getCommand()==DataShip.CLOSE_NETWORK){ //나가기 누를경우
				printSystemMessage("<"+index+"P> EXIT");
				printMessage(ip+":"+name+"���� �����Ͽ����ϴ�"); 
				closeNetwork(); // 쌍방향 서로 네트워크 끊힌것을 전송하여 break문을 통해 통신종료위함 
				break; // 나가기가 눌렸을 경우 while문 나감, 소켓통신 종료 
				
			}else if(data.getCommand()==DataShip.SERVER_EXIT){
				exitServer(); //서버가 나갈경우에도 클라이언트에서 출력되도록 
				
			}else if(data.getCommand()==DataShip.PRINT_SYSTEM_OPEN_MESSAGE){
				printSystemOpenMessage(); // 먼저 들어온 사람의 정보를 좌측화면에 출력
				
			}else if(data.getCommand()==DataShip.PRINT_SYSTEM_ADDMEMBER_MESSAGE){
				printSystemAddMemberMessage(); //나중에 들어온 사람의 정보를 좌측화면에 출력 
				
			}else if(data.getCommand()==DataShip.ADD_BLOCK){
				addBlock(data.getNumOfBlock()); //블록 공격한 것 메시지 추가 
				
			}else if(data.getCommand()==DataShip.GAME_START){
				gameStart(data.getSpeed()); //gamestart, 속도 synchronization 
				
			}else if(data.getCommand()==DataShip.SET_INDEX){
				setIndex(); //인덱스 세팅 
				
			}else if(data.getCommand()==DataShip.GAME_OVER){ 
				// GAME_OVER가 넘어올경우 maxRank-- => 초기값 = 1, 0 = 승리 
				rank = maxRank--; // 
				gameover(rank);
				
			}else if(data.getCommand()==DataShip.PRINT_MESSAGE){
				printMessage(data.getMsg());
				
			}else if(data.getCommand()==DataShip.PRINT_SYSTEM_MESSAGE){
				printSystemMessage(data.getMsg());
			}
			
		}//while(true), 클라이언트, 서버 데이터 주고 받음.
		
		try {
			list.remove(this);
			ois.close();
			oos.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}//run
	
	public void printMessage(String msg) { //채팅창 메세지 
		DataShip data = new DataShip(DataShip.PRINT_MESSAGE);
		data.setMsg(name+"("+index+"P)>" + msg);
		broadcast(data);
	}


	//응답하기, closeNetwork 
	public void closeNetwork() {
		DataShip data = new DataShip(DataShip.CLOSE_NETWORK);
		indexList.add(index);
		
		int tmp;
		if(indexList.size()>1){
			for(int i=0;i<indexList.size()-1;i++){
				if(indexList.get(i) > indexList.get(i+1)){
					tmp = indexList.get(i+1);
					indexList.remove(i+1);
					indexList.add(i,new Integer(tmp));	
				}
			}
		}
		send(data);
		//클라이언트와 통신, 1,2를 index를 통해서 쌍방향 전송, 커맨드 같이 실행되도록, 
		//Dataship을 통해 쌍방향 데이터 setter & getter 통해 통신  
	}
	
	//server의 exit를 스트림을 통해 전달 
	public void exitServer(){
		DataShip data = new DataShip(DataShip.SERVER_EXIT);
		broadcast(data);
	}
	
	//응답  : 게임시작 
	// maxRank 초기화
	public void gameStart(int speed){
		isStartGame = true;
		totalAdd = 0; // 공격라인수 
		maxRank = list.size();  
		DataShip data = new DataShip(DataShip.GAME_START);
		data.setPlay(true);
		data.setSpeed(speed);
		data.setMsg("<Game Start>");
		broadcast(data);
		for(int i=0 ; i<list.size() ;i++){
			GameHandler handler = list.get(i);
			handler.setRank(0);
		}
	}
	public void printSystemOpenMessage(){
		DataShip data = new DataShip(DataShip.PRINT_SYSTEM_MESSAGE);
		StringBuffer sb = new StringBuffer();
		for(int i=0 ;i<list.size();i++){
			sb.append("<"+list.get(i).index+"P> "+list.get(i).ip + ":" + list.get(i).name);
			if(i<list.size()-1)sb.append("\n");
		}
		data.setMsg(sb.toString());
		send(data);  // 데이터를 아웃풋스트림으로 내보냄
	}
	public void printSystemAddMemberMessage(){
		DataShip data = new DataShip(DataShip.PRINT_SYSTEM_MESSAGE);
		data.setMsg("<"+index+"P> "+ip + ":" + name);
		broadcast(data);
	}
	public void printSystemWinMessage(int index){
		DataShip data = new DataShip(DataShip.PRINT_SYSTEM_MESSAGE);
		data.setMsg(index+"P> WIN");
		broadcast(data);
	}
	public void printSystemMessage(String msg){
		DataShip data = new DataShip(DataShip.PRINT_SYSTEM_MESSAGE);
		data.setMsg(msg); //인풋스트림으로 데이터를 받아옴 
		broadcast(data); 
	}
	//응답하기 블록추가, 공격할 경우 totalAdd 증가 
	public void addBlock(int numOfBlock){
		DataShip data = new DataShip(DataShip.ADD_BLOCK);
		data.setNumOfBlock(numOfBlock);
		data.setMsg(index+"P -> ADD:"+numOfBlock);
		data.setIndex(index);
		totalAdd+=numOfBlock;
		broadcast(data);
	}
	//응답하기 : 인덱스 추가
	public void setIndex(){
		DataShip data = new DataShip(DataShip.SET_INDEX);
		data.setIndex(index);
		send(data);
	}
	//응답하기 : 게임오버 
	public void gameover(int rank){
		DataShip data = new DataShip(DataShip.GAME_OVER);
		data.setMsg(index+"P -> OVER:"+rank); 
		data.setIndex(index);
		data.setPlay(false);
		data.setRank(rank);
		data.setTotalAdd(totalAdd);
		broadcast(data);
		
		if(rank == 2){
			isStartGame = false;
			for(int i=0 ; i<list.size() ;i++){
				GameHandler handler = list.get(i);
				if(handler.getRank() == 0){
					handler.win();
				}		
			}
		}
	}
	
	public void win(){
		DataShip data = new DataShip(DataShip.GAME_WIN);
		data.setMsg(index+"P -> WIN");
		data.setTotalAdd(totalAdd);
		broadcast(data);
	}
	
	
	
//TODO:--------------------------[ ��� ���� ]--------------------------[�Ϸ�]
	//1��
	private void send(DataShip dataShip){
		try{
			oos.writeObject(dataShip); //데이터 입력하면 객체가 아웃풋스트림으로 나감.
			oos.flush();
		}catch(IOException e){e.printStackTrace();}
	}
	
	//n��
	private void broadcast(DataShip dataShip){
		for(int i=0 ; i<list.size() ; i++){
			GameHandler handler = list.get(i);
			if(handler!=null){
				try{
					handler.getOOS().writeObject(dataShip); 
					handler.getOOS().flush();
				}catch(IOException e){e.printStackTrace();}
			}
		}

	}// broadcast
	
	public ObjectOutputStream getOOS(){return oos;} 
	public int getRank() {return rank;}
	public void setRank(int rank){this.rank = rank;}
	public boolean isPlay(){return isStartGame;}
}//GameHandler



//TODO:--------------------------[ ���� ]--------------------------[�Ϸ�]
public class GameServer implements Runnable{
	private ServerSocket ss;
	private ArrayList<GameHandler> list = new ArrayList<GameHandler>();
	private ArrayList<Integer> indexList = new ArrayList<Integer>();
	private int index=1; //index = 들어온 사람들수를 나타냄, 1은 서버 
	
	public GameServer(int port){
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}//GameServer()	
	
	public void startServer(){
		System.out.println("������ �۵��ϰ� �ֽ��ϴ�.");
		index=1;
		new Thread(this).start();
	} 
	

	@Override
	public void run() {
		try{
			while(true){ // 서버소켓은 계속 열려있음 
				synchronized (GameServer.class) { //서버 동기화, 쓰레드 락
					
				Socket socket = ss.accept(); // 클라이언트 소켓 연결을 기다림 
				int index;
				if(indexList.size()>0) {
					index = indexList.get(0);
					indexList.remove(0);
				}else index = this.index++;
				GameHandler handler = new GameHandler(socket,list,index,indexList);
				list.add(handler);
				handler.start(); // 핸들러 쓰레드 시작 

				}
			}//while(true)

		}catch(IOException e){
			e.printStackTrace();
		}
	}
}//GameServer