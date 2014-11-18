package virtualdisk;

import java.util.*;

public class Inode {

	private boolean inUse;
	private int fileSize;
	private HashMap<Integer, Integer> myBlockMap;
	
	public Inode() {
		inUse = false;
		fileSize = 0;
		myBlockMap = new HashMap<Integer, Integer>();
	}
	
}
