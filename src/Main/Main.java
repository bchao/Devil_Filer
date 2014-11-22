package Main;

import java.io.FileNotFoundException;
import java.io.IOException;

import EventBarrier.EventBarrier;
import common.Constants;
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
		
		globalDFS = new LocalDFS(Constants.vdiskName, false);
		globalDFS.init();
		globalTestEventBarrier = new EventBarrier();
		
		testCreateFile();
		
		globalTestEventBarrier.arrive(); // wait until everything has tested
		
		printOutFSState();
	}
	
	private static void testCreateFile() {
		User u0 = new User(null, null, 0, 0, Constants.DiskOperationType.CREATE);
		User u1 = new User(null, null, 0, 0, Constants.DiskOperationType.CREATE);
		u0.start();
		u1.start();
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
