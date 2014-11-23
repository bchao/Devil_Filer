package dfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import common.*;
import dblockcache.*;
import virtualdisk.Inode.memBlock;
import virtualdisk.LocalVirtualDisk;
import virtualdisk.Inode;
import virtualdisk.VirtualDisk;
import Main.Main;

public class LocalDFS extends DFS {
	
	public VirtualDisk virtualDisk;
	public RandomAccessFile myRAFile;
	public LinkedList<DFileID> myFreeDFID;
	public HashMap<Integer, DFileID> myUsedDFID;
//	public LinkedList<DFileID> myUsedDFIDList;
	public LinkedList<Integer> myFreeBlocks;
	public Inode[] myInodes;

	LocalDBufferCache myDBufferCache;

	public LocalDFS(String volName, boolean format) {
		super(volName, format);
		
		virtualDisk = Main.globalVirtualDisk;
		myDBufferCache = (LocalDBufferCache) Main.globalDBufferCache;
		myFreeDFID = new LinkedList<DFileID>();
		myFreeBlocks = new LinkedList<Integer>();
		myRAFile = null;
		// temporary
		for(int i = 0; i < Constants.MAX_DFILES; i++) {
			myFreeDFID.add(new DFileID(i));			
		}

		for(int i = 0; i < Constants.NUM_OF_BLOCKS; i++) {
			myFreeBlocks.add((Integer) i);
		}

//		myUsedDFIDList = new LinkedList<DFileID>();
		myUsedDFID = new HashMap<Integer, DFileID>();

		// assuming 1 : 1 relation of inodes to dfiles
		myInodes = new Inode[Constants.MAX_DFILES];

		//		init(); // initialize this bad boy
	}

	public LocalDFS(boolean format) {
		this(Constants.vdiskName,format);
	}

	public LocalDFS() {
		this(Constants.vdiskName,false);
	}	
	@Override
	public void init() {

//				if(myDBufferCache == null) {
//					try {
//						myDBufferCache = new LocalDBufferCache(Constants.NUM_OF_CACHE_BLOCKS, new LocalVirtualDisk(super._volName, super._format));
//					} catch (FileNotFoundException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}

		// will need to change back 

		RandomAccessFile raFile = virtualDisk.returnRAF();
//		RandomAccessFile raFile = myRAFile;

		//HashMap<DFileID, Inode> map = new HashMap<DFileID, Inode>();

		// read through the virtual disk to get the goods

		try {
			initializeInodeState(raFile, Constants.NUMBER_INODE_METADATA);
		} catch (IOException e) {
			e.printStackTrace();
		}

		initializeDFileLists();

	}

	private void initializeDFileLists() {
		for (int i = 0; i < myInodes.length; i++) {
			if (myInodes[i].getInUse()) {
				//myUsedDFID.add(new DFileID(i));
				myUsedDFID.put(0, new DFileID(i));
				for (int blockID : myInodes[i].getUsedBlocks()) {
					myFreeBlocks.remove((Integer) blockID);
				}
			} else {
				myFreeDFID.add(new DFileID(i));
			}
		}

	}

	// NOT TESTED, WILL DO TOMORROW (Wednesday) -- why does inode have to have size?
	/**
	 * This method assumes we just have a tag (-1) if its a written file and a block map
	 * @param raFile the file passed in
	 * @param numMetaData number of other data other than bmap
	 * @throws IOException 
	 */
	private void initializeInodeState(RandomAccessFile raFile, int numMetaData) throws IOException {
		int raFilePointer = Constants.BLOCK_SIZE;
		raFile.seek(raFilePointer);

		// skip first block
		for (int i = 0; i < Constants.MAX_DFILES; i++) {
			int[] myIntBMap = new int[Constants.INODE_SIZE/4 - Constants.NUMBER_INODE_METADATA];
			int fileSize = 0;
			boolean inUse = true;
			for (int j = 0; j < Constants.INODE_SIZE/4; j++) {
				if (j == 0) {
					if (raFile.readInt() != -1) {
						raFilePointer += (Constants.INODE_SIZE); 
						raFile.seek(raFilePointer);
						myInodes[i] = new Inode(); // create the inode that has use = false
						inUse = false;
						break; // not a used inode
					} else {
						raFilePointer += 4;
						continue;
					} 
				} else if (j == 1) {
					fileSize = raFile.readInt();
					raFilePointer += 4;
				} else {
					myIntBMap[j-Constants.NUMBER_INODE_METADATA] = raFile.readInt();
					raFilePointer += 4;
				}
			}
			if (inUse)
				myInodes[i] = createINode(myIntBMap, fileSize);
		}
	}

	private Inode createINode(int[] myBMap, int size) {
		Inode toRet = new Inode();
		toRet.setInUse(true);
		toRet.setSize(size);
		toRet.setBlockMap(myBMap);
		return toRet;
	}

	private LinkedList<DFileID> initUsedlist() {
		LinkedList<DFileID> ret = new LinkedList<DFileID>();

		// called in init

		return ret;
	}

	@Override
	public synchronized DFileID createDFile() {
		if(myFreeDFID.isEmpty()) {
			try {
				throw new Exception("ERROR: NO SPACE");
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}

		DFileID dFID = myFreeDFID.get(0);
		myFreeDFID.remove(dFID);
		
//		myUsedDFIDList.add(dFID);
		myUsedDFID.put(dFID.getDFileID(), dFID);

		myInodes[dFID.getDFileID()].setInUse(true);

		notifyAll();
		
		return dFID;
	}

	@Override
	public synchronized void destroyDFile(DFileID dFID) {
		
		while(!myUsedDFID.containsValue(dFID)) {
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		dFID.setInUse(true);
		
		Inode currInode = myInodes[dFID.getDFileID()];
		currInode.setSize(0);		

		for(memBlock block : currInode.getBlockList()) {
			if(block != null) {
				myFreeBlocks.add(block.getBlockID());
				block = null;
			}
		}

		
		myFreeDFID.add(dFID);
		myUsedDFID.remove(dFID.getDFileID());
		
		dFID.zeroSize();
		dFID.setInUse(false);
		notifyAll();
		
	}

	@Override
	public synchronized int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
		
		while (dFID.isInUse()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		dFID.setInUse(true);
		
		Inode currInode = myInodes[dFID.getDFileID()];
		//int currOffset = startOffset + Constants.MAX_INODE_BLOCKS + 1;
		//int currOffset = startOffset + Constants.MAX_INODE_BLOCKS + Constants.BLOCK_SIZE;

		if(currInode == null)
			return -1;

		for(memBlock block : currInode.getBlockList()) {
			if(block == null)
				continue;
			
			int numBytesWritten;
			LocalDBuffer dbuff = (LocalDBuffer) myDBufferCache.getBlock(block.getBlockID());
			
			numBytesWritten = dbuff.read(buffer, startOffset, count);
			count -= numBytesWritten;
			startOffset += numBytesWritten;
		}

		dFID.setInUse(false);
		notifyAll();
		
		return count;
	}

	/*
	 * writes to the file specified by DFileID from the buffer starting from the
	 * buffer offset startOffset; at most count bytes are transferred
	 */
	@Override
	public synchronized int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
		int totalNumBytesWritten = 0;
		int numBytesWritten = 0;
		while (dFID.isInUse()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		dFID.setInUse(true);
		
		Inode currInode = myInodes[dFID.getDFileID()];
		int index = 0;
		
		for(int DFileBlock : getDFileBlocks(count)) {
			memBlock mb = currInode.getBlockList()[DFileBlock];
			int blockID;
			//TODO
			//check if inode corresponds
			if(mb == null) {
				if(myFreeBlocks.size() == 0) {
					try {
						throw new Exception("ERROR: NO FREE BLOCKS");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				blockID = myFreeBlocks.get(0);
				myFreeBlocks.remove((Integer) blockID);
				currInode.addBlock(index++, blockID);
			} else {
				blockID = mb.getBlockID();
			}
			
			//write to dbuffer
			//int currOffset = startOffset + Constants.MAX_INODE_BLOCKS + Constants.BLOCK_SIZE;
			LocalDBuffer dBuffer = (LocalDBuffer) myDBufferCache.getBlock(blockID);
			numBytesWritten = dBuffer.write(buffer, startOffset, count);			
			count -= numBytesWritten;
			startOffset += numBytesWritten;
			totalNumBytesWritten += numBytesWritten;
		}
		if (totalNumBytesWritten > dFID.getSize()) {
			dFID.incrementSize(numBytesWritten);
			currInode.setSize(dFID.getSize());
		}
		
		dFID.setInUse(false);
		notifyAll();
		
		return 0;
	}

	private ArrayList<Integer> getDFileBlocks(int count) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		int index = 0;
		
		while (count > Constants.BLOCK_SIZE) {
			ret.add(index);
			count -= Constants.BLOCK_SIZE;
			index++;
		}
		ret.add(index);
//		int x = startOffset % Constants.BLOCK_SIZE;
//		ret.add(x);
//
//		while(count > Constants.BLOCK_SIZE) {
//			ret.add(++x);
//			count -= Constants.BLOCK_SIZE;
//		}
//
//		if(startOffset + count > Constants.BLOCK_SIZE) {
//			ret.add(++x);
//		}

		return ret;
	}
	
	public synchronized DFileID getDFileID(int x) {
		
		while (myUsedDFID.get(x) == null) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return myUsedDFID.get(x);
	}

	@Override
	public int sizeDFile(DFileID dFID) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<DFileID> listAllDFiles() {
		return (List<DFileID>) myUsedDFID.values();
	}

	@Override
	public void sync() {
		// TODO Auto-generated method stub
		myDBufferCache.getInodes(myInodes);
		myDBufferCache.sync();
		
	}
	
	public void shutdown() {
		sync();
		myDBufferCache.shutdown();
	}

	public Inode[] getINodes() {
		return myInodes;
	}

	public void setVirtualDisk(VirtualDisk d) {
		virtualDisk = d;
	}

	public void setRAFile(RandomAccessFile d) {
		myRAFile = d;
	}
}
