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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public synchronized int write(byte[] buffer, int startOffset, int count) {
		// TODO Auto-generated method stub

		return 0;
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
}
