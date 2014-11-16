package dblockcache;

import java.io.IOException;

import virtualdisk.VirtualDisk;
import common.Constants;

public class LocalDBuffer extends DBuffer {

	private byte[] myBuffer;
	private int myBlockID;
	private int mySize;
	
	private VirtualDisk myDisk;
	private boolean isBusy;
	private boolean isValid;
	private boolean isClean;
	
	public LocalDBuffer(int blockID, int size, VirtualDisk disk) {
		myBuffer = new byte[size];
		mySize = size;
		myBlockID = blockID;
		myDisk = disk;
		isValid = false;
		isBusy = false;
		isClean = true;
	}
	@Override
	public void startFetch() {
		isValid = false;
		isBusy = true;
		try {
			myDisk.startRequest(this, Constants.DiskOperationType.READ);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startPush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean checkValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean waitValid() {
		return isValid;
	}

	@Override
	public boolean checkClean() {
		return isClean;
	}

	@Override
	public boolean waitClean() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBusy() {
		return isBusy;
	}

	@Override
	public int read(byte[] buffer, int startOffset, int count) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int write(byte[] buffer, int startOffset, int count) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void ioComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getBlockID() {
		return myBlockID;
	}

	@Override
	public byte[] getBuffer() {
		return myBuffer;
	}

}
