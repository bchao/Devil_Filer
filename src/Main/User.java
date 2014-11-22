package Main;

import common.Constants;
import common.Constants.DiskOperationType;
import common.DFileID;

public class User extends Thread {

	public DFileID myDFileID;
	public byte[] myByteArray;
	public int myOffset;
	public int myCount;
	public DiskOperationType myOp;
	
	
	public User(DFileID fileID, byte[] bArray, int offset, int count, DiskOperationType t) {
		myDFileID = fileID;
		myByteArray = bArray;
		myOffset = offset;
		myCount = count;
		myOp = t;
	}
	
	public void run() {
		
		switch (myOp) {
		
		case CREATE:
			Main.globalDFS.createDFile();
			Main.globalTestEventBarrier.raise();
			
		break;
		
		case DESTROY:
			Main.globalDFS.destroyDFile(myDFileID);
		break;
		
		case READ:		
			Main.globalDFS.read(myDFileID, myByteArray, myOffset, myCount);
		break;
		
		case WRITE:
			Main.globalDFS.write(myDFileID, myByteArray, myOffset, myCount);
		break;
		
		}
			
			
	}
	
}
