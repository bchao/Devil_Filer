package dblockcache;

import java.util.HashMap;
import java.util.Map;

import common.Constants;

import virtualdisk.VirtualDisk;

public class LocalDBufferCache extends DBufferCache {

	private VirtualDisk myDisk;
	private int myCacheSize; // do i need this?
	private Map<Integer, DBuffer> DBufferMap;
	
	public LocalDBufferCache(int cacheSize, VirtualDisk disk) {
		super(cacheSize);
		myCacheSize = cacheSize;
		myDisk = disk;
		DBufferMap = new HashMap<Integer, DBuffer>();
		Thread virtualDiskThread = new Thread(myDisk);
		virtualDiskThread.start();
	}

	@Override
	public synchronized DBuffer getBlock(int blockID) {
		if(DBufferMap.containsKey(blockID)) {
			return DBufferMap.get(blockID);
		}
		LocalDBuffer dbuf = new LocalDBuffer(blockID, Constants.BLOCK_SIZE, myDisk);
		dbuf.setBusy(true);
		DBufferMap.put(blockID, dbuf);
		return dbuf;
	}

	@Override
	public synchronized void releaseBlock(DBuffer buf) {
		((LocalDBuffer) buf).setBusy(false);
		//signal??	
	}

	@Override
	public synchronized void sync() {
		for (Integer id : DBufferMap.keySet()) {
			LocalDBuffer dbuf = (LocalDBuffer) DBufferMap.get(id);
			if (!dbuf.checkClean()) {
				dbuf.startPush();
				dbuf.waitClean();
			}
		}
		notifyAll();
	}
}
