package com.tetris.classes;

import java.awt.Color;
import java.awt.Graphics;

//테트리스 블럭
public abstract class TetrisBlock {
	
	public static int MODE_NUM = 0; //0일때는 item블럭을 생성, 1일때는 생성하지 않음
	/* TetrisBlock Type*/
	public static final int TYPE_CENTERUP = 0 ;
	public static final int TYPE_LEFTTWOUP = 1 ;
	public static final int TYPE_LEFTUP = 2 ;
	public static final int TYPE_LINE = 3 ;
	public static final int TYPE_NEMO = 4 ;
	public static final int TYPE_RIGHTTWOUP = 5 ;
	public static final int TYPE_RIGHTUP = 6 ;
	
	/* Rotation Index */
	public static final int ROTATION_0 = 0;			//원래 모양의   0도 회전
	public static final int ROTATION_90 = 1;		//원래 모양의  90도 회전
	public static final int ROTATION_180 = 2;		//원래 모양의 180도 회전
	public static final int ROTATION_270 = 3;		//원래 모양의 270도 회전
	
	/* Rotation Type */
	public static final int ROTATION_LEFT = 1;		//시계방향회전
	public static final int ROTATION_RIGHT = -1;	//반시계방향회전
	
	/* 그외 필드 */
	protected int type;								//블럭모양;
	protected Block[] colBlock= new Block[4];		//모양을 나타내는 4개블럭
	protected int rotation_index;					//블럭회전 모양
	protected int posX,posY;						//모양의 좌표
	
	public Color color;							//블록색상
	
	
	
	
	public TetrisBlock(int x, int y, Color color, Color ghostColor) {
		this.color = color;
		Color item = new Color(255,255,50);//Yellow
		Color item2 = new Color(255, 0, 255);//Pink, 아이템 변수 추가
		int random = (int)(Math.random()*150);//아이템이 나올 확률을 조정.
		for(int i=0 ; i<colBlock.length ; i++){
			if(MODE_NUM == 0) {//아이템을 사용할 때
				if(i == random) {
				colBlock[i] = new Block(0,0,item,ghostColor);
				}
				else {
					if(random >= 40 && random <= 43) {//random 변수가 50-53사이의 수면 item2의 색을 가짐
						colBlock[i] = new Block(0, 0, item2, ghostColor);
						random += 5; //한 블록에 아이템이 하나만 나오게 하기 위해
					}
					else {
						colBlock[i] = new Block(0,0,color,ghostColor);
					}
				}
			}
			else if(MODE_NUM == 1){//아이템을 사용하지 않을 때
				colBlock[i] = new Block(0,0,color,ghostColor);
			}
		}
		this.rotation(ROTATION_0); //기본 회전모양 : 0도
		this.setPosX(x);
		this.setPosY(y);
	}
	
	
	/**
	 * 테트리스 블럭모양을 회전한다. 
	 * @param rotation_index : 회전모양
	 * ROTATION_0, ROTATION_90, ROTATION_180, ROTATION_270
	 */
	public abstract void rotation(int rotation_index);
	
	
	/**
	 * 테트리스 블럭모양을 왼쪽으로 이동시킨다.
	 * @param addX : 이동양
	 * 0이상의 값을 넣어야 한다.
	 */
	public void moveLeft(int addX) {this.setPosX(this.getPosX()-addX);}
	
	
	/**
	 * 테트리스 블럭모양을 오른쪽으로 이동시킨다.
	 * @param addX : 이동양
	 * 0이상의 값을 넣어야 한다.
	 */
	public void moveRight(int addX) {this.setPosX(this.getPosX()+addX);}
	
	
	/**
	 * 테트리스 블럭모양을 아래로 이동시킨다.
	 * @param addY : 이동양
	 * 0이상의 값을 넣어야 한다.
	 */
	public void moveDown(int addY) {this.setPosY(this.getPosY()+addY);}
	
	
	/**
	 * 테트리스 블럭을 Graphics를 이용하여 그린다.
	 * @param g
	 */
	public void drawBlock(Graphics g){
		for(Block col : colBlock){
			if(col!=null) {col.drawColorBlock(g);

			
			}
		}
	}
	
	

	/* Getter */
	public Block[] getBlock() {return colBlock;}
	public Block getBlock(int index) {return colBlock[index];}
	public int getPosX() {return posX;}
	public int getPosY() {return posY;}
	public int getRotationIndex() {return rotation_index;}
	public int getType() {return type;}
	
	
	/* Setter */
	public void setType(int type) {this.type = type;}
	public void setBlock(Block[] blocks) {this.colBlock = blocks;}
	public void setBlock(int index, Block block) {this.colBlock[index] = block;}
	public void setPosX(int x) {
		this.posX = x;
		for(int i=0; i<colBlock.length ;i++){
			if(colBlock[i]!=null)colBlock[i].setPosGridX(x);
		}
	}
	public void setPosY(int y) {
		this.posY = y;
		for(int i=0; i<colBlock.length ;i++){
			if(colBlock[i]!=null)colBlock[i].setPosGridY(y);
		}
	}
	public void setGhostView(boolean b){
		for(int i=0; i<colBlock.length ;i++){
			if(colBlock[i]!=null)colBlock[i].setGhostView(b);
		}
	}


}
