package virtualdisk;

import java.util.*;

import common.Constants;

public class Inode {

	private boolean inUse;
	private int fileSize;
//	private ArrayList<Integer> myBlockList;
	private memBlock[] myBlockList;
	
	public Inode() {
		inUse = false;
		fileSize = 0;
//		myBlockList = new ArrayList<Integer>();
		myBlockList = new memBlock[Constants.INODE_SIZE];
	}
	
	public void addBlock(int index, int block) {
		myBlockList[index] = new memBlock(block);
		fileSize++;
	}
	
	public void remBlock(int index) {
		myBlockList[index] = null;
		fileSize--;
	}
	
	public memBlock[] getBlockList() {
		return myBlockList;
	}
	
	public int getFileSize() {
		return fileSize;
	}
	
	public boolean getInUse() {
		return inUse;
	}
	
	public void setInUse(boolean bool) {
		inUse = bool;
	}
	
	public class memBlock {
		private int block;
		
		public memBlock(int b) {
			block = b;
		}
		
		public void setBlock(int b) {
			block = b;
		}
		
		public int getBlockID() {
			return block;
		}
	}
	
}
