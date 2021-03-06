package virtualdisk;

import java.util.*;

import common.Constants;

public class Inode {

	private boolean inUse;
	private int fileSize;
	private memBlock[] myBlockList;
	
	public Inode() {
		inUse = false;
		myBlockList = new memBlock[Constants.INODE_SIZE/4 - Constants.NUMBER_INODE_METADATA];
		fileSize = 0;
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

	public void setBlockMap(int[] myBMap) {
		for (int i = 0; i < myBMap.length; i++) {
			if (myBMap[i] == 0 && fileSize < Constants.BLOCK_SIZE*i) 
				myBlockList[i] = null;
			else 
				myBlockList[i] = new memBlock(myBMap[i]);
		}
	}

	public List<Integer> getUsedBlocks() {
		ArrayList<Integer> toRet = new ArrayList<Integer>();
		for (int i = 0; i < myBlockList.length; i++) {
			if (myBlockList[i] != null) {
				toRet.add(myBlockList[i].block);
			}
		}
		return toRet;
	}
	
	public int getSize() {
		return fileSize;
	}
	
	public void printOut() {
		System.out.println("*** Inode ***");
		System.out.println("Is active: "+inUse);
		System.out.println("File Size: " + fileSize);
		for (int i = 0; i < myBlockList.length; i++) {
			if (myBlockList[i] != null) 
				System.out.println("BlockMap[" + i + "] = " + myBlockList[i].block);
			else
				System.out.println("BlockMap[" + i + "] = null");
		}
		System.out.println();
	}

	public void setSize(int size) {
		fileSize = size;
	}
	
}
