package virtualdisk;

import java.util.*;

public class Inode {

	private boolean inUse;
	private int fileSize;
	private ArrayList<Integer> myBlockList;
	
	public Inode() {
		inUse = false;
		fileSize = 0;
		myBlockList = new ArrayList<Integer>();
	}
	
	public void addBlock(int block) {
		myBlockList.add(block);
	}
	
	public ArrayList<Integer> getBlockList() {
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
	
}
