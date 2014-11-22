package Main;

import java.io.FileNotFoundException;
import java.io.IOException;

import EventBarrier.EventBarrier;
import common.Constants;
import common.DFileID;
import dblockcache.*;
import dfs.*;
import virtualdisk.*;

public class Main {
	public static VirtualDisk globalVirtualDisk;
	public static DFS globalDFS;
	public static DBufferCache globalDBufferCache;
	public static EventBarrier globalTestEventBarrier;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		//create disk
		globalVirtualDisk = new LocalVirtualDisk(Constants.vdiskName, false);
		globalDBufferCache = new LocalDBufferCache(Constants.NUM_OF_CACHE_BLOCKS, globalVirtualDisk);
		globalDFS = new LocalDFS(Constants.vdiskName, false);
		globalDFS.init();
		globalTestEventBarrier = new EventBarrier();
		
//		testCreateFile();
//		testDestroyFile();
		testReadWriteFile();
		
//		globalTestEventBarrier.arrive(); // wait until everything has tested
		
		printOutFSState();
	}
	
	private static void testReadWriteFile() {
		
		User u0 = new User(null, null, 0, 0, Constants.DiskOperationType.CREATE);
		byte[] arr = new byte[4];
		arr[0] = (byte) 10;
		arr[1] = (byte) 20;
		arr[2] = (byte) 30;
		arr[3] = (byte) 40;
		User u1 = new User(((LocalDFS) globalDFS).getDFileID(0), arr, 0, 4, Constants.DiskOperationType.WRITE);
		u1.start();
	}
	
	private static void testCreateFile() {
		User u0 = new User(null, null, 0, 0, Constants.DiskOperationType.CREATE);
		User u1 = new User(null, null, 0, 0, Constants.DiskOperationType.CREATE);
		u0.start();
		u1.start();
	}
	
	private static void testDestroyFile() {
		User u0 = new User(null, null, 0, 0, Constants.DiskOperationType.CREATE);
		User u1 = new User(null, null, 0, 0, Constants.DiskOperationType.CREATE);
		u0.start();
		u1.start();
		User u2 = new User(((LocalDFS) globalDFS).getDFileID(0), null, 0, 0, Constants.DiskOperationType.DESTROY);
		u2.start();
	}

	private static void printOutFSState() {
		for(Inode n : ((LocalDFS) globalDFS).myInodes) {
			n.printOut();
		}
		
		System.out.println("free blocks: " + ((LocalDFS) globalDFS).myFreeBlocks.size());
		System.out.println("free ids: " + ((LocalDFS) globalDFS).myFreeDFID.size());
		System.out.println("used ids: " + ((LocalDFS) globalDFS).myUsedDFID.size());
	}
}
