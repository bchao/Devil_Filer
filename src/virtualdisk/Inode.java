package virtualdisk;

import java.util.*;

import common.Constants;

public class Inode {

	private boolean inUse;
//	private ArrayList<Integer> myBlockList;
	private memBlock[] myBlockList;
	
	public Inode() {
		inUse = false;
//		myBlockList = new ArrayList<Integer>();
		myBlockList = new memBlock[Constants.INODE_SIZE];
	}
	
	public void addBlock(int index, int block) {
		myBlockList[index] = new memBlock(block);
	}
	
	public void remBlock(int index) {
		myBlockList[index] = null;
	}
	
	public memBlock[] getBlockList() {
		return myBlockList;
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
