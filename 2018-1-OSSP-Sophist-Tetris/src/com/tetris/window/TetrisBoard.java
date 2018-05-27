package com.tetris.window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.tetris.classes.Block;
import com.tetris.classes.TetrisBlock;
import com.tetris.controller.TetrisController;
import com.tetris.network.GameClient;
import com.tetris.shape.CenterUp;
import com.tetris.shape.LeftTwoUp;
import com.tetris.shape.LeftUp;
import com.tetris.shape.Line;
import com.tetris.shape.Nemo;
import com.tetris.shape.RightTwoUp;
import com.tetris.shape.RightUp;

public class TetrisBoard extends JPanel implements Runnable, KeyListener, MouseListener, ActionListener{
	private static final long serialVersionUID = 1L;
	
	private Tetris tetris;
	private GameClient client;

	public static boolean ITEM_CLEAR_SOUND = false;
	public static boolean ITEM_BLIND_SOUND = false;
	public static boolean EXP_SOUND = false;

	public static final int PLAY_ITEM_CLEAR_SOUND = 1;
	public static final int PLAY_ITEM_BLIND_SOUND = 2;
	public static final int PLAY_EXP_SOUND = 3;
	public static final int PLAY_BGM = 4;
	public static final int PLAY_BLOCK_SPIN_SOUND = 5;
	public static final int PLAY_BLOCK_SET_SOUND = 6;
	public static final int PLAY_GAME_OVER_SOUND = 7;
	
	public static final int BLOCK_SIZE = 20; //20
	public static final int BOARD_X = 120;
	public static final int BOARD_Y = 50;
	private int minX=1, minY=0, maxX=10, maxY=21, down=50, up=0;
			// maxY = 게임화면 세로길이, maxX = 게임화면 가로 길이 

	private final int MESSAGE_X = 2;
	private final int MESSAGE_WIDTH = BLOCK_SIZE * (7 + minX);
	private final int MESSAGE_HEIGHT = BLOCK_SIZE * (6 + minY);
	private final int PANEL_WIDTH = (maxX*BLOCK_SIZE + MESSAGE_WIDTH + BOARD_X);
	private final int PANEL_HEIGHT = (maxY*BLOCK_SIZE + MESSAGE_HEIGHT  + BOARD_Y);
	
	private long start_t = 0, end_t = 0;
	private long start_time_record = 0;
	private long end_time_record = 0;
	private long play_time = 60;
	

	
	private SystemMessageArea systemMsg = new SystemMessageArea(BLOCK_SIZE*1,BOARD_Y + BLOCK_SIZE + BLOCK_SIZE*9, BLOCK_SIZE*5, BLOCK_SIZE*9);
	private MessageArea messageArea = new MessageArea(this,2, PANEL_HEIGHT - (MESSAGE_HEIGHT-MESSAGE_X), PANEL_WIDTH-BLOCK_SIZE*7-2, MESSAGE_HEIGHT-2);
	private JButton btnStart = new JButton("시작하기");
	private JButton btnExit = new JButton("나가기");
	private JCheckBox checkGhost = new JCheckBox("고스트모드",true);
	private JCheckBox checkGrid  = new JCheckBox("격자 표시",true);
	public JCheckBox checktimemod = new JCheckBox("타임어택", true);
	private Integer[] lv = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
	
	public JLabel time = new JLabel("60:00");
	public JComboBox<Integer> comboSpeed = new JComboBox<Integer>(lv);
	
	private String ip;
	private int port;
	private String nickName;
	private Thread th;
	private ArrayList<Block> blockList;
	//
	//
	// 아이템 추가 
	private ArrayList<Block> tempBlockList;
	// 아이템 추가 
	//
	//
	private ArrayList<TetrisBlock> nextBlocks;
	private TetrisBlock shap;
	private TetrisBlock ghost;
	private TetrisBlock hold;
	private Block[][] map;
	private TetrisController controller;
	private TetrisController controllerGhost;
	
	//
	//
	// 아이템 테스트 임시 변수
	private int Blind_cnt=0;
	private int maxHeight; //블록 아이템 추가를 위한 높이수를 가져옴 
	// 아이템 테스트 임시 변수 
	//
	//
	
	
	private boolean isPlay = false;
	private boolean isHold = false;
	private boolean usingGhost = true;
	private boolean usingGrid = true;
	private int removeLineCount = 0;
	private int removeLineCombo = 0;
	
	public int mode_number = 1; // 1 : 아이템 x 모드, 0 : 아이템 모드 
	
	public TetrisBoard(Tetris tetris, GameClient client) {
		this.tetris = tetris; // 테트리스 받아오고
		this.client = client; // 클라이언트 받아옴
		this.setPreferredSize(new Dimension(PANEL_WIDTH,PANEL_HEIGHT));//기본크기
		this.addKeyListener(this);
		this.addMouseListener(this);
		this.setLayout(null);
		this.setFocusable(true);
		
		btnStart.setBounds(PANEL_WIDTH - BLOCK_SIZE*7, PANEL_HEIGHT - messageArea.getHeight(), BLOCK_SIZE*7, messageArea.getHeight()/2);
		btnStart.setFocusable(false);
		btnStart.setEnabled(false);
		btnStart.addActionListener(this);
		btnExit.setBounds(PANEL_WIDTH - BLOCK_SIZE*7, PANEL_HEIGHT - messageArea.getHeight()/2, BLOCK_SIZE*7, messageArea.getHeight()/2);
		btnExit.setFocusable(false);	
		btnExit.addActionListener(this);
		checkGhost.setBounds(PANEL_WIDTH - BLOCK_SIZE*7+35,5,95,20);
		checkGhost.setBackground(new Color(0,87,102));
		checkGhost.setForeground(Color.WHITE);
		checkGhost.setFont(new Font("굴림", Font.BOLD,13));
		checkGhost.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				usingGhost = checkGhost.isSelected();
				TetrisBoard.this.setRequestFocusEnabled(true);
				TetrisBoard.this.repaint();
			}
		});
		checkGrid.setBounds(PANEL_WIDTH - BLOCK_SIZE*7+35,25,95,20);
		checkGrid.setBackground(new Color(0,87,102));
		checkGrid.setForeground(Color.WHITE);
		checkGrid.setFont(new Font("굴림", Font.BOLD,13));
		checkGrid.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				usingGrid = checkGrid.isSelected();
				TetrisBoard.this.setRequestFocusEnabled(true);
				TetrisBoard.this.repaint();
			}
		});
		checktimemod.setBounds(PANEL_WIDTH - BLOCK_SIZE*10,25,100,20);
		checktimemod.setBackground(new Color(0,87,102));
		checktimemod.setForeground(Color.WHITE);
		checktimemod.setFont(new Font("굴림", Font.BOLD,13));
		checktimemod.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				if(checktimemod.isSelected()) {
					time.setVisible(true);
					mode_number = 1; // item
					TetrisBoard.this.setRequestFocusEnabled(true);
					TetrisBoard.this.repaint();
				}
				else if(!checktimemod.isSelected()){
					mode_number = 0; // no item
					time.setVisible(false);
					TetrisBoard.this.setRequestFocusEnabled(true);
					TetrisBoard.this.repaint();
				}
			}
		});
		time.setBounds(BLOCK_SIZE*1,BOARD_Y + BLOCK_SIZE + BLOCK_SIZE*6, BLOCK_SIZE*5, BLOCK_SIZE*3);
		time.setForeground(Color.black);	
		time.setFont(new Font("Agency FB", Font.BOLD,30));//글씨크기가 커지면 보이지 않을 수 있으니 주의
		
		comboSpeed.setBounds(PANEL_WIDTH - BLOCK_SIZE*8, 5, 45, 20);
		this.add(time);
		this.add(comboSpeed);
		this.add(systemMsg);
		this.add(messageArea);
		this.add(btnStart);		
		this.add(btnExit);
		this.add(checkGhost);
		this.add(checkGrid);
		this.add(checktimemod);
		
	}
	
	/*********************************************************************************************************************
	 *화면구성단 끝
	 *************************************************************************************************************************/
	
	public void startNetworking(String ip, int port, String nickName){
		this.ip = ip;
		this.port = port;
		this.nickName = nickName;
		this.repaint();
	}
	
	/**TODO : 게임시작
	 * 게임을 시작한다.
	 */
	
	public void gameStart(int speed, int mode){
		comboSpeed.setSelectedItem(speed);
		mode_number = mode;
		//작업쓰레드가 돌고있다면 (핸들러=클라이언트가 있다면)
		// isPlas = false, thread.join()을 통해 실행중인 쓰레드를 멈춘다
		if(th!=null){
			try {isPlay = false;th.join();} 
			catch (InterruptedException e) {e.printStackTrace();}
		}
		/******************************************************************/
		/******************************************************************/
		//맵셋팅
		map = new Block[maxY][maxX]; //게임화면
		blockList = new ArrayList<Block>();
		nextBlocks = new ArrayList<TetrisBlock>();
		
		
		//도형셋팅
		shap = getRandomTetrisBlock(); // 도형을 받아옴
		ghost = getBlockClone(shap,true);  //고스트 뷰, 혹은 도형나오는 위치 설정 
		hold = null;
		isHold = false;
		
		controller = new TetrisController(shap,maxX-1,maxY-1,map); 
		controllerGhost = new TetrisController(ghost,maxX-1,maxY-1,map);
		this.showGhost();
		for(int i=0 ; i<5 ; i++){
			nextBlocks.add(getRandomTetrisBlock());
		}
		/******************************************************************/
		/******************************************************************/
		//스레드 셋팅
		isPlay = true;
		th = new Thread(this);
		th.start();
		
		//
		//
		this.maxHeight = 20;
		// 테스트 변수 초기화 
		//
		//
		
	}
	
	
	//TODO : paint
	@Override
	protected void paintComponent(Graphics g) {
		//시간추가기능생기면 end_t - start_t + add_time
		if(mode_number == 1) {
		 	if(end_t == 0 && start_t == 0 ) {
		 			time.setText("60:00");
		 	}
		 	else if((end_t - start_t)/1000 < play_time && 59-(end_t-start_t)/1000 >= 10) {
		 		time.setText(Long.toString(59-(end_t - start_t)/1000) + ":" + Long.toString(9-(end_t - start_t)%1000/100) + Long.toString(9-(end_t - start_t)%100/10));
		 	}
		 	else if((end_t - start_t)/1000 < play_time && 59-(end_t-start_t)/1000 < 10){
		 		time.setText("0" + Long.toString(59-(end_t - start_t)/1000) + ":" + Long.toString(9-(end_t - start_t)%1000/100) + Long.toString(9-(end_t - start_t)%100/10));
		 				
		 	}
		 	else {
		 		time.setText("00:00");
		 			this.gameEndCallBack();
		 	}
		}
		g.clearRect(0, 0, this.getWidth(), this.getHeight()+1);
		

		g.setColor(new Color(0,87,102));
		g.fillRect(0, 0, (maxX+minX+13)*BLOCK_SIZE+1, BOARD_Y);
		
		g.setColor(new Color(92,109,129));
		g.fillRect(0, BOARD_Y, (maxX+minX+13)*BLOCK_SIZE+1, maxY*BLOCK_SIZE+1);
		g.setColor(Color.WHITE);
		
		//IP 출력
		g.drawString("ip : "+ip+"     port : "+port, 20, 20);
		
		//NickName 출력
		g.drawString("닉네임 : "+nickName, 20, 40);
		
		//속도
		Font font= g.getFont();
		g.setFont(new Font("굴림", Font.BOLD,13));
		g.drawString("속도", PANEL_WIDTH - BLOCK_SIZE*10, 20);
		g.setFont(font);
		
		g.setColor(Color.BLACK);
		g.fillRect(BOARD_X + BLOCK_SIZE*minX, BOARD_Y, maxX*BLOCK_SIZE+1, maxY*BLOCK_SIZE+1);
		g.fillRect(BLOCK_SIZE*minX ,BOARD_Y + BLOCK_SIZE, BLOCK_SIZE*5,BLOCK_SIZE*5);
		g.fillRect(BOARD_X + BLOCK_SIZE*minX + (maxX+1)*BLOCK_SIZE+1,BOARD_Y + BLOCK_SIZE, BLOCK_SIZE*5,BLOCK_SIZE*5);
		g.fillRect(BOARD_X + BLOCK_SIZE*minX + (maxX+1)*BLOCK_SIZE+1,BOARD_Y + BLOCK_SIZE + BLOCK_SIZE*7, BLOCK_SIZE*5,BLOCK_SIZE*12);
		
		//HOLD  NEXT 출력
		g.setFont(new Font(font.getFontName(),font.getStyle(),20));
		g.drawString("H O L D", BLOCK_SIZE + 12, BOARD_Y + BLOCK_SIZE + BLOCK_SIZE*5 + 20);
		g.drawString("N E X T", BOARD_X + BLOCK_SIZE + (maxX+1)*BLOCK_SIZE+1 + 12, BOARD_Y + BLOCK_SIZE + BLOCK_SIZE*5 + 20);
		g.setFont(font);
		
		//그리드 표시
		if(usingGrid){
			g.setColor(Color.darkGray);
			for(int i=1;i<maxY;i++) g.drawLine(BOARD_X + BLOCK_SIZE*minX, BOARD_Y+BLOCK_SIZE*(i+minY), BOARD_X + (maxX+minX)*BLOCK_SIZE, BOARD_Y+BLOCK_SIZE*(i+minY));
			for(int i=1;i<maxX;i++) g.drawLine(BOARD_X + BLOCK_SIZE*(i+minX), BOARD_Y+BLOCK_SIZE*minY, BOARD_X + BLOCK_SIZE*(i+minX), BOARD_Y+BLOCK_SIZE*(minY+maxY));
			for(int i=1;i<5;i++) g.drawLine(BLOCK_SIZE*minX ,BOARD_Y + BLOCK_SIZE*(i+1), BLOCK_SIZE*(minX+5)-1,BOARD_Y + BLOCK_SIZE*(i+1));
			for(int i=1;i<5;i++) g.drawLine(BLOCK_SIZE*(minY+i+1) ,BOARD_Y + BLOCK_SIZE, BLOCK_SIZE*(minY+i+1),BOARD_Y + BLOCK_SIZE*(minY+6)-1);
			for(int i=1;i<5;i++) g.drawLine(BOARD_X + BLOCK_SIZE*minX + (maxX+1)*BLOCK_SIZE+1, BOARD_Y + BLOCK_SIZE*(i+1), BOARD_X + BLOCK_SIZE*minX + (maxX+1)*BLOCK_SIZE+BLOCK_SIZE*5,BOARD_Y + BLOCK_SIZE*(i+1));
			for(int i=1;i<5;i++) g.drawLine(BOARD_X + BLOCK_SIZE*minX + (maxX+1+i)*BLOCK_SIZE+1, BOARD_Y + BLOCK_SIZE, BOARD_X + BLOCK_SIZE*minX + BLOCK_SIZE+BLOCK_SIZE*(10+i)+1,BOARD_Y + BLOCK_SIZE*6-1);	
		}
		
		//홀드 도형 표시
		int x=0,y=0,newY=0;
		
		if(hold!=null){
			x=0; y=0; newY=3;
			x = hold.getPosX();
			y = hold.getPosY();
			hold.setPosX(-4+minX);
			hold.setPosY(newY+minY);
			hold.drawBlock(g);
			hold.setPosX(x);
			hold.setPosY(y);
		}
		
		//넥스트 도형 표시 
		if(nextBlocks!=null){
			x=0; y=0; newY=3;
			for(int i = 0 ; i<nextBlocks.size() ; i++){
				TetrisBlock block = nextBlocks.get(i);
				x = block.getPosX();
				y = block.getPosY();
				block.setPosX(13+minX);
				block.setPosY(newY+minY);
				if(newY==3) newY=6;
				block.drawBlock(g);
				block.setPosX(x);
				block.setPosY(y);
				newY+=3;
			}
		}
		
		if(blockList!=null){
			x=0; y=0;
			for(int i = 0 ; i<blockList.size() ; i++){
				Block block = blockList.get(i);
				x = block.getPosGridX();
				y = block.getPosGridY();
				block.setPosGridX(x+minX);
				block.setPosGridY(y+minY);
				block.drawColorBlock(g);
				block.setPosGridX(x);
				block.setPosGridY(y);
			}
		}

		if(ghost!=null){

			if(usingGhost){
				x=0; y=0;
				x = ghost.getPosX();
				y = ghost.getPosY();
				ghost.setPosX(x+minX);
				ghost.setPosY(y+minY);
				ghost.drawBlock(g);
				ghost.setPosX(x);
				ghost.setPosY(y);
			}
		}
		
		if(shap!=null){
			x=0; y=0;
			x = shap.getPosX();
			y = shap.getPosY();
			shap.setPosX(x+minX);
			shap.setPosY(y+minY);
			shap.drawBlock(g);
			shap.setPosX(x);
			shap.setPosY(y);
		}
	}
	
	/*
	 * 게임 메소드 구현위치 
	 * 
	 */
	@Override
	public void run() {
		if(mode_number == 1) {
			time.setVisible(true);
			start_t = System.currentTimeMillis();
		 		if(start_time_record == 0) {
		 			start_time_record = start_t;
		 		}
		}
		else {
			time.setVisible(false);
		}
		int countMove = (21-(int)comboSpeed.getSelectedItem())*5; // 
		//블록을 내려보냄
		//countMove가 작아질수록 moveDown 실행 
		
		int countDown = 0;		
		//블록이 움직일 수 없는 위치에 도착할 경우 countDown = down
		//down은 fixingTetrisBlock이 실행되는 데까지 while문을 계속 돌게함으로써
		//약간의 딜레이를 제공 
		
		int countUp = up; 
		// 0으로 시
		/******************************************************************/
		/*************************** 게임 ing 상태 **************************/
		while(isPlay){
			if(mode_number == 1) {
				end_t = System.currentTimeMillis();
			}
			/*
				이벤트마다 사운드 추가하기
			 */
			if(tetris.isServer()) {
				comboSpeed.setEnabled(false);
				checktimemod.setEnabled(false);
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
						
			//countdown!=0인 경우는 moveDown == false인경우
			//즉 더이상 블록이 움직일수 없는 경우, 카운트 다운을 통해 this.fixingTetrisBlock을 실행 
			if(countDown!=0){
				countDown--;
				if(countDown==0){					
					if(controller!=null && !controller.moveDown()) this.fixingTetrisBlock();
				}
				this.repaint();
				continue;
			} //만약 계속 내려갈 수 있다면 countMove-- , 아래로 이동 
			
			countMove--;
			
			if (countMove == 0) {
				countMove = (21-(int)comboSpeed.getSelectedItem())*5;
				if (controller != null && !controller.moveDown()) countDown = down;
				//down : 50
				else this.showGhost();
			}  
			
			
			if (countUp != 0) {
				countUp--;
				if (countUp == 0) {
					countUp = up;
					addBlockLine(1);
				}
			}
			
			this.repaint();
		}//while()
		if(tetris.isServer()) {
			comboSpeed.setEnabled(true);
			checktimemod.setEnabled(true);
		}
	}//run()
		/******************************************************************/

	
	/**
	 * 맵(보이기, 논리)을 상하로 이동한다.
	 * @param lineNumber	
	 * @param num -1 or 1
	 */
	public void dropBoard(int lineNumber, int num){
		
		// 맵을 떨어트린다.
		this.dropMap(lineNumber,num);
		
		//좌표바꿔주기(1만큼증가)
		this.changeTetrisBlockLine(lineNumber,num);
		
		//다시 체크하기
		this.checkMap();
		
		//고스트 다시 뿌리기
		this.showGhost();
	}
	
	
	/**
	 * lineNumber의 위쪽 라인들을 모두 num칸씩 내린다.
	 * @param lineNumber
	 * @param num 칸수 -1,1
	 */
	private void dropMap(int lineNumber, int num) {
		if(num==1){
			//한줄씩 내리기
			for(int i= lineNumber ; i>0 ;i--){
				for(int j=0 ; j<map[i].length ;j++){
					map[i][j] = map[i-1][j];
				}
			}
			
			//맨 윗줄은 null로 만들기
			for(int j=0 ; j<map[0].length ;j++){
				map[0][j] = null;
			}
		}
		else if(num==-1){
			//한줄씩 올리기
			for(int i= 1 ; i<=lineNumber ;i++){
				for(int j=0 ; j<map[i].length ;j++){
					map[i-1][j] = map[i][j];
				}
			}
			
			//removeLine은 null로 만들기
			for(int j=0 ; j<map[0].length ;j++){
				map[lineNumber][j] = null;
			}
		}
	}
	
	
	/**
	 * lineNumber의 위쪽 라인들을 모두 num만큼 이동시킨다.
	 * @param lineNumber 
	 * @param num	이동할 라인
	 */	
	private void changeTetrisBlockLine(int lineNumber, int num){
		int y=0, posY=0;
		for(int i=0 ; i<blockList.size() ; i++){
			y = blockList.get(i).getY();
			posY = blockList.get(i).getPosGridY();
			if(y<=lineNumber)blockList.get(i).setPosGridY(posY + num);
		}
	}

	
	/**
	 * 테트리스 블럭을 고정시킨다. 
	 */
	private void fixingTetrisBlock() {
		synchronized (this) {
			if(stop){
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		ITEM_CLEAR_SOUND = false;
		ITEM_BLIND_SOUND = false;
		EXP_SOUND = false;
		boolean isCombo = false;
		removeLineCount = 0;
		
		// drawList 추가
		for (Block block : shap.getBlock()) { //실질적으로 내리면 그리는 부분
			blockList.add(block);
			//test
			
			// TetrisBlock 인 shap를 통해 도형을 가져온 뒤에 멥의 블록에 가져온 테트리스블록을 올림.
		}
		
		// check
		isCombo = checkMap();

		if(isCombo) removeLineCombo++;
		else removeLineCombo = 0;
		
		//콜백메소드
		this.getFixBlockCallBack(blockList,removeLineCombo,removeLineCount);
		
		if(ITEM_CLEAR_SOUND && !ITEM_BLIND_SOUND) {
			playSound(PLAY_ITEM_CLEAR_SOUND);
		}
		else if(!ITEM_CLEAR_SOUND && ITEM_BLIND_SOUND) {
			
		}else if(!ITEM_CLEAR_SOUND && !ITEM_BLIND_SOUND && EXP_SOUND) {
			playSound(PLAY_EXP_SOUND);
		}
		
		playSound(PLAY_BLOCK_SET_SOUND);
		//다음 테트리스 블럭을 가져온다.
		this.nextTetrisBlock();
		
		//홀드가능상태로 만들어준다.
		isHold = false;
	}//fixingTetrisBlock()
	
	
	/**
	 * 
	 * @return true-지우기성공, false-지우기실패
	 */
	private boolean checkMap(){
		boolean isCombo = false;
		int count = 0;
		Block mainBlock;
		
		for(int i=0 ; i<blockList.size() ;i++){
			mainBlock = blockList.get(i);
			
			/*
			 * 
			 * 아이템 테스트 
			 * 맵에서 가장 높게 있는 Y를 저장하여 dropBoard를 이용하기 위한 변수 
			 */
			if(maxHeight > mainBlock.getY())
				maxHeight = mainBlock.getY();			
			/*
			 *
			 * 아이템 테스트 
			 * 
			 */
			
			// map에 추가
			if(mainBlock.getY()<0 || mainBlock.getY() >=maxY) continue;
			
			if(mainBlock.getY()<maxY && mainBlock.getX()<maxX) 
				map[mainBlock.getY()][mainBlock.getX()] = mainBlock;

			// 줄이 꽉 찼을 경우. 게임을 종료한다.
			if (mainBlock.getY() == 1 && mainBlock.getX() > 2 && mainBlock.getX() < 9) {
			//	end_time_record = System.currentTimeMillis(); 나중에 점수기록할때써먹을 코드
  				this.gameEndCallBack();
 			//	System.out.println((end_time_record - start_time_record)/1000);
				break;
			}
			
			//1줄개수 체크
			count = 0;
			for (int j = 0; j < maxX; j++) {
				if(map[mainBlock.getY()][j] != null) count++;
				
			}
			
			//block의 해당 line을 지운다.

			if (count == maxX) {
				removeLineCount++;
				this.removeBlockLine(mainBlock.getY());
				isCombo = true;
			}
		}
	
		return isCombo;
	}
	
	/**
	 * 테트리스 블럭 리스트에서 테트리스 블럭을 받아온다.
	 */
	public void nextTetrisBlock(){
		shap = nextBlocks.get(0);
		this.initController();
		nextBlocks.remove(0);
		nextBlocks.add(getRandomTetrisBlock());
	}
	
	private void initController(){
		controller.setBlock(shap);
		ghost = getBlockClone(shap,true);
		controllerGhost.setBlock(ghost);
	}
	
	
	/**
	 * lineNumber 라인을 삭제하고, drawlist에서 제거하고, map을 아래로 내린다.
	 * @param lineNumber 삭제라인
	 */
	//--------------
	public void clearMap() {
		if(Blind_cnt == 1) {
			System.out.println("Clear All");
			for (int k = 0; k < blockList.size(); k++) {
				blockList.remove(k);
			}
			for (int x = 0 ; x < maxX ; x++ ) {
				for(int y = 0 ; y < maxY ; y++) {
					map[y][x] = null;
				}
			}
			dropBoard(20, 21-maxHeight);
			ITEM_CLEAR_SOUND = true;
		}
		Blind_cnt = 0;
	}
	
	public void blindMap() {
		
	}
	
	//--------------
	private void removeBlockLine(int lineNumber) {
		// 1줄을 지워줌
		for (int j = 0; j < maxX ; j++) {
			for (int s = 0; s < blockList.size(); s++) {
				Block b = blockList.get(s);
				if (b == map[lineNumber][j]) {
					if(map[lineNumber][j].color.equals(new Color(255,255,50))) {
						System.out.println("아이템터짐");//@@@ 이 부분에 아이템 메소드를 집어넣으면 됩니다.
						Blind_cnt =1;
						clearMap();
					}
					else {
						blockList.remove(s);
						EXP_SOUND = true;
					}
				}
			}
			map[lineNumber][j] = null;
		}// for(j)

		this.dropBoard(lineNumber,1);
	}
	
	
	/**TODO : 게임종료콜벡
	 * 게임이 종료되면 실행되는 메소드
	 */
	public void gameEndCallBack(){
		client.gameover();
		this.isPlay = false;
		playSound(PLAY_GAME_OVER_SOUND);
	}
	
	
	/**
	 * 고스트블럭을 보여준다.
	 */
	private void showGhost(){
		ghost = getBlockClone(shap,true);
		controllerGhost.setBlock(ghost);
		controllerGhost.moveQuickDown(shap.getPosY(), true);
	}	
	
	
	/**
	 * 랜덤으로 테트리스 블럭을 생성하고 반환한다.
	 * @return 테트리스 블럭
	 */
	public TetrisBlock getRandomTetrisBlock(){
		if(mode_number == 1) {
			TetrisBlock.MODE_NUM = 1;
		}
		else if(mode_number == 0) {
			TetrisBlock.MODE_NUM = 0;
		}
		switch((int)(Math.random()*7)){
		case TetrisBlock.TYPE_CENTERUP : return new CenterUp(4, 1);
		case TetrisBlock.TYPE_LEFTTWOUP : return new LeftTwoUp(4, 1);
		case TetrisBlock.TYPE_LEFTUP : return new LeftUp(4, 1);
		case TetrisBlock.TYPE_RIGHTTWOUP : return new RightTwoUp(4, 1);
		case TetrisBlock.TYPE_RIGHTUP : return new RightUp(4, 1);
		case TetrisBlock.TYPE_LINE : return new Line(4, 1);
		case TetrisBlock.TYPE_NEMO : return new Nemo(4, 1);
		}
		return null;
	}
	
	
	/**
	 * tetrisBlock과 같은 모양으로 고스트의 블럭모양을 반환한다.
	 * @param tetrisBlock 고스트의 블럭모양을 결정할 블럭
	 * @return 고스트의 블럭모양을 반환
	 */
	//(4,1)시작 위치 
	public TetrisBlock getBlockClone(TetrisBlock tetrisBlock, boolean isGhost){
		TetrisBlock blocks = null;
		switch(tetrisBlock.getType()){
		case TetrisBlock.TYPE_CENTERUP : blocks =  new CenterUp(4, 1); break;
		case TetrisBlock.TYPE_LEFTTWOUP : blocks =  new LeftTwoUp(4, 1); break;
		case TetrisBlock.TYPE_LEFTUP : blocks =  new LeftUp(4, 1); break;
		case TetrisBlock.TYPE_RIGHTTWOUP : blocks =  new RightTwoUp(4, 1); break;
		case TetrisBlock.TYPE_RIGHTUP : blocks =  new RightUp(4, 1); break;
		case TetrisBlock.TYPE_LINE : blocks =  new Line(4, 1); break;
		case TetrisBlock.TYPE_NEMO : blocks =  new Nemo(4, 1); break;
		}
		//고스트 뷰 
		if(blocks!=null && isGhost){
			blocks.setGhostView(isGhost);
			blocks.setPosX(tetrisBlock.getPosX());
			blocks.setPosY(tetrisBlock.getPosY());
			blocks.rotation(tetrisBlock.getRotationIndex());
		}
		return blocks;
	}	
	
	
	/**TODO : 콜백메소드
	 * 테트리스 블럭이 고정될 때 자동 호출 된다.
	 * @param removeCombo	현재 콤보 수
	 * @param removeMaxLine	한번에 지운 줄수 
	 */
	public void getFixBlockCallBack(ArrayList<Block> blockList, int removeCombo, int removeMaxLine){
		if(removeCombo<3){
			if(removeMaxLine==3)client.addBlock(1);
			else if(removeMaxLine==4)client.addBlock(3);
		}else if(removeCombo<10){
			if(removeMaxLine==3)client.addBlock(2);
			else if(removeMaxLine==4)client.addBlock(4);
			else client.addBlock(1);
		}else{
			if(removeMaxLine==3)client.addBlock(3);
			else if(removeMaxLine==4)client.addBlock(5);
			else client.addBlock(2);
		}
	}
	
	/**
	 * 블럭을 홀드시킨다.
	 */
	public void playBlockHold(){
		if(isHold) 
			return;
		
		if(hold==null){
			hold = getBlockClone(shap,false);
			this.nextTetrisBlock();
		}else{
			TetrisBlock tmp = getBlockClone(shap,false);
			shap = getBlockClone(hold,false);
			hold = getBlockClone(tmp,false);
			this.initController();
		}
		
		isHold = true;
	}
	
	
	/**
	 * 가장 밑에 줄에 블럭을 생성한다.
	 * @param numOfLine
	 */
	boolean stop = false;
	public void addBlockLine(int numOfLine){
		stop = true;
		// 내리기가 있을 때까지 대기한다.
		// 내리기를 모두 실행한 후 다시 시작한다.
		Block block;
		int rand = (int) (Math.random() * maxX);
		for (int i = 0; i < numOfLine; i++) {
			this.dropBoard(maxY - 1, -1);
			for (int col = 0; col < maxX; col++) {
				if (col != rand) {
					block = new Block(0, 0, Color.GRAY, Color.GRAY);
					block.setPosGridXY(col, maxY - 1);
					blockList.add(block);
					map[maxY - 1][col] = block;
				}
			}
			//만약 내려오는 블럭과 겹치면 블럭을 위로 올린다.
			boolean up = false;
			for(int j=0 ; j<shap.getBlock().length ; j++){
				Block sBlock = shap.getBlock(j);
				if(map[sBlock.getY()][sBlock.getX()]!=null){
					up = true;
					break;
				}
			}
			if(up){
				controller.moveDown(-1);
			}
		}
		
		
		
		
		this.showGhost();
		this.repaint();
		synchronized (this) {
			stop = false;
			this.notify();
		}
	}
	
	
	
	//키이벤트 컨트롤
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			messageArea.requestFocus();
		}
		if(!isPlay) return;
		if(e.getKeyCode() == KeyEvent.VK_LEFT){
			controller.moveLeft();
			controllerGhost.moveLeft();
		}else if(e.getKeyCode() == KeyEvent.VK_RIGHT){
			controller.moveRight();
			controllerGhost.moveRight();
		}else if(e.getKeyCode() == KeyEvent.VK_DOWN){
			controller.moveDown();
		}else if(e.getKeyCode() == KeyEvent.VK_UP){
			controller.nextRotationLeft();
			controllerGhost.nextRotationLeft();
			playSound(PLAY_BLOCK_SPIN_SOUND);
		}else if(e.getKeyCode() == KeyEvent.VK_SPACE){
			controller.moveQuickDown(shap.getPosY(), true);
			this.fixingTetrisBlock();
		}else if(e.getKeyCode() == KeyEvent.VK_SHIFT){ 
			playBlockHold();
		}
		this.showGhost();
		this.repaint();
	}

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		this.requestFocus();
	}
	public void mouseReleased(MouseEvent e) {}
	
	
	
	

	public void actionPerformed(ActionEvent e) { 
		if(e.getSource() == btnStart){ //if btnStart clicked, start tetris 
			if(client!=null){
				client.gameStart((int)comboSpeed.getSelectedItem(), mode_number);
			}else{			
				this.gameStart((int)comboSpeed.getSelectedItem(), mode_number);
			}
		}else if(e.getSource() == btnExit){
			//client -> 처음에 isServer=true 
			//따라서 server_exit가 실행됨
			//server_exit가 소켓통신되게 되면 
			// client의 closeNetwork(false)로 들어가게되고 CLOSE_NETWORK가 전송
			// client의 recloseNetwork실행 
			// 소켓, 스트림 닫음, 연결 종료 
			if(client!=null ){
				if(tetris.isNetwork()){//연결이 되있는 상태라면 
					client.closeNetwork(tetris.isServer());
				}
			}else{
				System.exit(0);
			}
			
		}
	}
	
	
/* 
 * ********************************* 블록 이벤트 처리시 사용 가능할 듯 ******************************
 * ********************************* 1회만 재생 ***********************************************
public void playSound(File file, boolean loop) {
	try {
		AudioInputStream ais = AudioSystem.getAudioInputStream(file);
		AudioFormat af = ais.getFormat();		
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
		SourceDataLine sdl = (SourceDataLine)AudioSystem.getLine(info);
		sdl.open();
		sdl.start();
		byte[] buffer = new byte[128000];
		int i;
		while(loop) {
			i=ais.read(buffer, 0, buffer.length);
			if(i == -1)
				break;
			sdl.write(buffer, 0, i);
		}
	}catch(Exception e) {
		e.printStackTrace();
	}
	
}
 * 
 */
	
	public void playSound(int play){
		//사운드재생용메소드
		//메인 클래스에 추가로 메소드를 하나 더 만들었습니다.
		//사운드파일을받아들여해당사운드를재생시킨다.
		
		String path = Tetris.class.getResource("").getPath();
		String file = path;
		boolean Loop = false;
		
		switch(play){
		case PLAY_ITEM_CLEAR_SOUND :
			file = new String(path + "Item_Clear.wav");
			Loop = false;
			break;
		case PLAY_ITEM_BLIND_SOUND :
			break;
		case PLAY_EXP_SOUND :
			file = new String(path + "Block_Exp.wav");
			Loop = false;
			break;
		case PLAY_BGM :
			file = new String(path + "BGM.wav");
			Loop = true;
			break;
		case PLAY_BLOCK_SPIN_SOUND :
			file = new String(path + "Block_Spin.wav");
			Loop = false;
			break;
		case PLAY_BLOCK_SET_SOUND :
			file = new String(path + "Block_Set.wav");
			Loop = false;
			break;
		case PLAY_GAME_OVER_SOUND :
			file = new String(path + "Game_Over.wav");
			Loop = false;
			break;
		}
		
		Clip clip;
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));
			clip = AudioSystem.getClip();
			clip.open(ais);
			clip.start();
			if ( Loop) clip.loop(-1);
			//Loop 값이true면 사운드재생을무한반복시킵니다.
			//false면 한번만재생시킵니다.
			} catch (Exception e) {
				e.printStackTrace();
			}
	}	
/* 사운드 추가 */


	public boolean isPlay(){return isPlay;}
	public void setPlay(boolean isPlay){this.isPlay = isPlay;}
	public JButton getBtnStart() {return btnStart;}
	public JButton getBtnExit() {return btnExit;}
	public void setClient(GameClient client) {this.client = client;}
	public void printSystemMessage(String msg){systemMsg.printMessage(msg);}
	public void printMessage(String msg){messageArea.printMessage(msg);}
	public GameClient getClient(){return client;}
	public void changeSpeed(Integer speed) {comboSpeed.setSelectedItem(speed);}
	public void clearMessage() {
		messageArea.clearMessage();
		systemMsg.clearMessage();
	}
	
	
	
}
