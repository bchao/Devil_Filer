package dblockcache;

import java.util.HashMap;
import java.util.Map;

import virtualdisk.VirtualDisk;

public class LocalDBufferCache extends DBufferCache {

	private VirtualDisk myDisk;
	private int myCacheSize;
	private Map<Integer, DBuffer> DBufferMap;
	
	public LocalDBufferCache(int cacheSize, VirtualDisk disk) {
		super(cacheSize);
		myCacheSize = cacheSize; // Do I need to calculate this by multiplying by block size constant?
		myDisk = disk;
		DBufferMap = new HashMap<Integer, DBuffer>();
		Thread virtualDiskThread = new Thread(myDisk);
		virtualDiskThread.start();
	}

	@Override
	public DBuffer getBlock(int blockID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void releaseBlock(DBuffer buf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sync() {
		// TODO Auto-generated method stub
		
	}

}
