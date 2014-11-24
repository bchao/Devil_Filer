package Main;

import java.util.ArrayList;

import virtualdisk.Inode;
import virtualdisk.LocalVirtualDisk;
import common.Constants;
import common.DFileID;
import common.Constants.DiskOperationType;
import dblockcache.LocalDBufferCache;
import dfs.LocalDFS;

public class TestClient2 {
	private static String readStr;
	
	public static void main (String[] args) throws Exception {
		System.out.println("Initializing DFS");
		Main.globalVirtualDisk = new LocalVirtualDisk(Constants.vdiskName, false);
		Main.globalDBufferCache = new LocalDBufferCache(Constants.NUM_OF_CACHE_BLOCKS, Main.globalVirtualDisk);
		Main.globalDFS = new LocalDFS(Constants.vdiskName, false);
		Main.globalDFS.init();
		//DFileID file = Main.globalDFS.createDFile();
		System.out.println("Initialized");
		boolean readTest = false;
		if (readTest) {
			printOutFSState();
		}

		//Create Files 0 and 1
		ArrayList<User> clients = new ArrayList<User>();
		
		if (!readTest) {
			User u0 = new User(null, null, 0, 0, DiskOperationType.CREATE);
			clients.add(u0);
			u0.start();
			User u1 = new User(null, null, 0, 0, DiskOperationType.CREATE);
			clients.add(u1);
			u1.start();
		}

		String t = "Hello World";
		byte[] data = t.getBytes();
		byte[] read = new byte[50];
		t += t;
		byte[] data2 = t.getBytes();
		byte[] read2 = new byte[50];
		byte[] read3 = new byte[50];
		
		//Write twice to file 0 then read once
		
		if (!readTest) {
			User u9 = new User(((LocalDFS) Main.globalDFS).getDFileID(0), data, 0, data.length, DiskOperationType.WRITE);
			clients.add(u9);
			u9.start();
			User u3 = new User(((LocalDFS) Main.globalDFS).getDFileID(0), data2, 0, data2.length, DiskOperationType.WRITE);
			clients.add(u3);
			u3.start();
		}

		User u4 = new User(((LocalDFS) Main.globalDFS).getDFileID(0), read, 0, 50, DiskOperationType.READ); 
		clients.add(u4);
		u4.start();
		
		//Write once to file 1 then read twice
		if (!readTest) {
			User u5 = new User(((LocalDFS) Main.globalDFS).getDFileID(1), data, 0, data.length, DiskOperationType.WRITE);
			clients.add(u5);
			u5.start();
		}

		User u6 = new User(((LocalDFS) Main.globalDFS).getDFileID(1), read2, 0, 50, DiskOperationType.READ);
		clients.add(u6);
		u6.start();
		User u7 = new User(((LocalDFS) Main.globalDFS).getDFileID(1), read3, 0, 50, DiskOperationType.READ);
		clients.add(u7);
		u7.start();
		
		//Delete file 0
//		User u8 = new User(((LocalDFS) Main.globalDFS).getDFileID(0), null, 0, 0, DiskOperationType.DESTROY);
//		clients.add(u8);
//		u8.start();
//		User u11 = new User(((LocalDFS) Main.globalDFS).getDFileID(1), null, 0, 0, DiskOperationType.DESTROY);
//		clients.add(u11);
//		u11.start();

		// Sync files to disk
		for (User u : clients) {
			u.join();
		}
		printOutFSState();
		System.out.println(new String(read).trim());
		System.out.println(new String(read2).trim());
		System.out.println(new String(read3).trim());
		System.out.println("SHUTTING DOWN");
		Main.globalDFS.shutdown();
	}

	
	private static void printOutFSState() {
		for(Inode n : ((LocalDFS) Main.globalDFS).myInodes) {
			n.printOut();
		}
		
		System.out.println("free blocks: " + ((LocalDFS) Main.globalDFS).myFreeBlocks.size());
		System.out.println("free ids: " + ((LocalDFS) Main.globalDFS).myFreeDFID.size());
		System.out.println("used ids: " + ((LocalDFS) Main.globalDFS).myUsedDFID.size());
	}
}