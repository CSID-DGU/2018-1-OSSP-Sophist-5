package com.tetris.controller;

import com.tetris.classes.Block;
import com.tetris.classes.TetrisBlock;

public class TetrisController {

	private int rotation_index;
	private TetrisBlock block;
	private Block[][] map;
	
	private int maxX, maxY;
	
	/** 
	15 	 * 테트리스 블럭을 조정하는 컨트롤러이다. 
	16 	 *  
	17 	 * @param block : 움직일 테트리스 블럭 
	18 	 * @param minX : 블럭이 움직일 최소 GridX좌표 
	19 	 * @param minY : 블럭이 움직일 최소 GridY좌표 
	20 	 * @param maxX : 블럭이 움직일 최대 GridX좌표 
	21 	 * @param maxY : 블럭이 움직일 최대 GridY좌표 
	22 	 */ 

	public TetrisController(TetrisBlock block, int maxX, int maxY, Block[][] map) {
		this.block = block;
		
		this.maxX = maxX;
		this.maxY = maxY;
		
		this.map = map;
		this.rotation_index = block.getRotationIndex();
		
	}
	
	
	/** 
	36 	 * 움직일 테트리스 블럭을 넘겨준다. 
	37 	 * @param block 움직일 테트리스 블럭 
	38 	 */ 

	public void setBlock(TetrisBlock block){
		this.block = block;
		this.rotation_index = block.getRotationIndex();
	}
	
	
	/** 
	46 	 * 블럭의 좌표를 출력한다. 
	47 	 */ 

	public void showIndex(){
		for(Block blocks : block.getBlock()){
			if(blocks!=null)System.out.print("("+blocks.getX()+","+blocks.getY()+")");
		}
		System.out.println();
	}
	
	
	/**
	 * ���� ��ǥ �����ȿ� �ִ��� Ȯ���Ѵ�.
	 * 
	 * @param maxX : 블록이 움직일 수 있는 x좌표의 갯수
	 * @param maxY : 블록이 움직일 수 있는 y좌표의 갯수
	 * @return
	 */
	public boolean checkIndex(int maxX, int maxY){
		for(Block blocks : block.getBlock()){
			if(blocks==null || blocks.getY()<0) continue;
			
			if(blocks.getX() < 0 || blocks.getY() < 0 
					|| blocks.getX() > maxX || blocks.getY() > maxY )
				return false;
			else{
				if(map[blocks.getY()][blocks.getX()]!=null)return false;
			}
		}
		return true;
	}
	
	/**
	 * �������� �̵�
	 * default 1ĭ
	 */
	public void moveLeft(){moveLeft(1);}
	public void moveLeft(int x){
		//�̵�
		block.moveLeft(x);
				
		//üũ, ������ ����ٸ� ���󺹱�
		if(!checkIndex(maxX,maxY)) {
			block.moveLeft(-x);
		}
	}
	
	/**
	 * ���������� �̵�
	 * default 1ĭ
	 */
	public void moveRight(){moveRight(1);}
	public void moveRight(int x){
		// �̵�
		block.moveRight(x);
		
				
		// üũ, ������ ����ٸ� ���󺹱�
		if (!checkIndex(maxX, maxY)) {
			block.moveRight(-x);
		}
	}
	
	
	/**
	 * �Ʒ��� �̵�
	 * default 1ĭ
	 */
	public boolean moveDown(){return moveDown(1);}
	public boolean moveDown(int y){
		
		boolean moved = true;
		// �̵�
		block.moveDown(y);
		//체크, 범위를 벗어났다면 원상복귀 
		if (!checkIndex(maxX, maxY)) {
			block.moveDown(-y);
			moved = false;
		}
		return moved;
	}
	
	 /* @param startY 현재 블럭의 위치 
	 130 	 * @param moved 재귀함수에 필요한 인자로, 무조건 true로 한다. 
	 131 	 * @return	moveQuickDown를 다시 호출한다. 
	 132 	 */ 

	public boolean moveQuickDown(int startY, boolean moved){
		
		//이동
		block.moveDown(1);
		// 체크, 범위를 벗어났다면 원상복귀		
		if (!checkIndex(maxX, maxY)) {
			block.moveDown(-1);
			if(moved) return false;
		}
		return moveQuickDown(startY+1, true);
	}
	
	
	
	/** 
	148 	 * 테트리스 블럭을 회전시킨다. 
	149 	 * @param rotation_direction : 회전방향 
	150 	 * TetrisBlock.ROTATION_LEFT(시계방향), TetrisBlock.ROTATION_RIGHT(반시계방향) 
	151 	 */ 

	public void nextRotation(int rotation_direction){
		if(rotation_direction == TetrisBlock.ROTATION_LEFT) 
			this.nextRotationLeft();
		else if(rotation_direction == TetrisBlock.ROTATION_RIGHT) 
			this.nextRotationRight();
	}
	
	
	/** 
	161 	 * 테트리스 블럭을 회전시킨다. (시계방향) 
	162 	 * 만약 회전시 범위를 벗어나면, 회전을 하지 않는다. 
	163 	 */ 

	public void nextRotationLeft(){
		//ȸ��
		rotation_index++;
		if(rotation_index == TetrisBlock.ROTATION_270+1) rotation_index = TetrisBlock.ROTATION_0;
		block.rotation(rotation_index);
		
		//üũ, ������ ����ٸ� ���󺹱�
		if(!checkIndex(maxX,maxY)) {
			rotation_index--;
			if(rotation_index == TetrisBlock.ROTATION_0-1) rotation_index = TetrisBlock.ROTATION_270;
			block.rotation(rotation_index);
		}
	}
	
	
	/**
	 * ��Ʈ���� ���� ȸ����Ų��. (�ݽð����)
	 * ���� ȸ���� ������ �����, ȸ���� ���� �ʴ´�.
	 */
	public void nextRotationRight(){
		//ȸ��
		rotation_index--;
		if(rotation_index == TetrisBlock.ROTATION_0-1) rotation_index = TetrisBlock.ROTATION_270;
		block.rotation(rotation_index);
		
		//üũ, ������ ����ٸ� ���󺹱�
		if(!checkIndex(maxX,maxY)) {
			rotation_index++;
			if(rotation_index == TetrisBlock.ROTATION_270+1) rotation_index = TetrisBlock.ROTATION_0;
			block.rotation(rotation_index);
		}
	}
}
