package dblockcache;

import java.io.IOException;

import virtualdisk.VirtualDisk;
import common.Constants;

public class LocalDBuffer extends DBuffer {

	private byte[] myBuffer;
	private int myBlockID;

	private VirtualDisk myDisk;
	private boolean isBusy;
	private boolean isValid;
	private boolean isClean;

	public LocalDBuffer(int blockID, int size, VirtualDisk disk) {
		myBuffer = new byte[size];
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
		if (isClean) {
			return;
		}
		isBusy = true;
		try {
			myDisk.startRequest(this, Constants.DiskOperationType.WRITE);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		synchronized (this){
			isClean = true;
			notifyAll();
		}
	}

	@Override
	public boolean checkValid() {
		return isValid;
	}

	@Override
	public synchronized boolean waitValid() {
		while (!isValid) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public boolean checkClean() {
		return isClean;
	}

	@Override
	public synchronized boolean waitClean() {
		while (!isClean) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public boolean isBusy() {
		return isBusy;
	}

	@Override
	public synchronized int read(byte[] buffer, int startOffset, int count) {
		isBusy = true;

		if (count > buffer.length) {
			return -1;
		}
		
		if (startOffset + count > buffer.length || startOffset + count < 0 || !isValid) {
			return -1;
		}
		
		if (count > Constants.BLOCK_SIZE) count = Constants.BLOCK_SIZE;

		for(int i = startOffset; i < startOffset + count; i++) {
			buffer[i - startOffset] = myBuffer[i];
		}

		isBusy = false;
		notifyAll();
		return count;
	}

	@Override
	public synchronized int write(byte[] buffer, int startOffset, int count) {
		isBusy = true;

		if(count > buffer.length) {
			//count = buffer.length;
			return -1;
		}

		if(startOffset + count > buffer.length || startOffset + count < 0 || !isValid) {
			return -1;
		}
		
		if(count > Constants.BLOCK_SIZE) count = Constants.BLOCK_SIZE;

		isClean = false;
		
		for(int i = startOffset; i < startOffset + count; i++) {
			//System.out.println(buffer[i]);
			myBuffer[i - startOffset] = buffer[i];
		}
		
		isBusy = false;
		notifyAll();
		return count;
	}

	@Override
	public synchronized void ioComplete() {
		isValid = true;
		isBusy = false;
		notifyAll();
	}

	@Override
	public int getBlockID() {
		return myBlockID;
	}

	@Override
	public byte[] getBuffer() {
		return myBuffer;
	}

	public void setBusy(boolean busy) {
		isBusy = busy;
	}
}
