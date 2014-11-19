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

public class LocalDFS extends DFS {
	LocalVirtualDisk virtualDisk;
	LinkedList<DFileID> myFreeDFID;
	LinkedList<DFileID> myUsedDFID;
	LinkedList<Integer> myFreeBlocks;
	Inode[] myInodes;
	
	LocalDBufferCache myDBufferCache;
	
	public LocalDFS(String volName, boolean format) {
		super(volName, format);
		
		myFreeDFID = new LinkedList<DFileID>();
		// temporary
		for(int i = 0; i < Constants.MAX_DFILES; i++) {
			myFreeDFID.add(new DFileID(i));			
		}
		
		for(int i = 0; i < Constants.NUM_OF_BLOCKS; i++) {
			myFreeBlocks.add(i);
		}
		
		myUsedDFID = initUsedlist();
		
		// assuming 1 : 1 relation of inodes to dfiles
		myInodes = new Inode[Constants.MAX_DFILES];
		
		init(); // initialize this bad boy
	}

	public LocalDFS(boolean format) {
		this(Constants.vdiskName,format);
	}

	public LocalDFS() {
		this(Constants.vdiskName,false);
	}	
	@Override
	public void init() {
		
		if(myDBufferCache == null) {
			try {
				myDBufferCache = new LocalDBufferCache(Constants.NUM_OF_CACHE_BLOCKS, new LocalVirtualDisk(super._volName, super._format));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		RandomAccessFile raFile = virtualDisk.returnRAF();
		
		try {
			raFile.seek(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//HashMap<DFileID, Inode> map = new HashMap<DFileID, Inode>();

		// read through the virtual disk to get the goods
		try {
			initializeInodeState(raFile, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		initializeDFileLists();

	}
	
	private void initializeDFileLists() {
		for (int i = 0; i < myInodes.length; i++) {
			if (myInodes[i].getInUse()) {
				myUsedDFID.add(new DFileID(i));
				for (int blockID : myInodes[i].getUsedBlocks()) {
					myFreeBlocks.remove(blockID);
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
		int raFilePointer = 1;
		
		for (int i = 0; i < Constants.MAX_DFILES; i += Constants.INODE_SIZE) {
			int[] myBMap = new int[Constants.INODE_SIZE/4 - 1];				
			for (int j = 0; j < Constants.INODE_SIZE/4; j++) {
				if (j == 0 && raFile.readInt() != -1) {
					// moves file pointer ahead to next inode region
					raFilePointer += (Constants.INODE_SIZE + Constants.INODE_SIZE/4 - 1); 
					raFile.seek(raFilePointer);
					myInodes[i] = new Inode(); // create the inode that has use = false
					break; // not a used inode
				}
				
				myBMap[j-1] = raFile.readInt();
				
			}
			
			myInodes[i] = createINode(myBMap);

		}
	}

	private Inode createINode(int[] myBMap) {
		Inode toRet = new Inode();
		toRet.setInUse(true);
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
		myUsedDFID.add(dFID);
		
		myInodes[dFID.getDFileID()].setInUse(true);
		
		return dFID;
	}

	@Override
	public synchronized void destroyDFile(DFileID dFID) {
		Inode currInode = myInodes[dFID.getDFileID()];
		// TODO: have to destroy Inode data too
		currInode.setInUse(false);
		
		for(memBlock block : currInode.getBlockList()) {
			if(block != null) {
				myFreeBlocks.add(block.getBlockID());
				block = null;
			}
		}
		
		myFreeDFID.add(dFID);
		myUsedDFID.remove(dFID);
	}

	@Override
	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
		Inode currInode = myInodes[dFID.getDFileID()];
		int currOffset = startOffset;
		int currCount = count;
		
		if(currInode == null)
			return -1;
		
		for(memBlock block : currInode.getBlockList()) {
			if(block == null)
				continue;
			
			LocalDBuffer dbuff = (LocalDBuffer) myDBufferCache.getBlock(block.getBlockID());
			dbuff.read(buffer, currOffset, currCount);
			currOffset += count;
			currCount -= count;
		}
		
		return count;
	}
	
	/*
	 * writes to the file specified by DFileID from the buffer starting from the
	 * buffer offset startOffset; at most count bytes are transferred
	 */
	@Override
	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
		Inode currInode = myInodes[dFID.getDFileID()];
		
		for(int DFileBlock : getDFileBlocks(startOffset, count)) {
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
			} else {
				blockID = mb.getBlockID();
			}
			
			//write to dbuffer
			LocalDBuffer dBuffer = (LocalDBuffer) myDBufferCache.getBlock(blockID);
			dBuffer.write(buffer, startOffset, count);
		}

		// to do
		return 0;
	}
	
	private ArrayList<Integer> getDFileBlocks(int startOffset, int count) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		int x = startOffset % Constants.BLOCK_SIZE;
		ret.add(x);
		
		while(count > Constants.BLOCK_SIZE) {
			ret.add(++x);
			count -= Constants.BLOCK_SIZE;
		}
		
		if(startOffset + count > Constants.BLOCK_SIZE) {
			ret.add(++x);
		}
		
		return ret;
	}

	@Override
	public int sizeDFile(DFileID dFID) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<DFileID> listAllDFiles() {
		return myUsedDFID;
	}

	@Override
	public void sync() {
		// TODO Auto-generated method stub
		
	}
}
