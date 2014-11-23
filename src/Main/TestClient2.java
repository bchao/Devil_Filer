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
	//public TestClient2() {}

	//	private void WriteTest(DFileID f, String t) {
	//		byte[] data = t.getBytes();
	//		Main.globalDFS.write(f, data, 0, data.length);
	//	}
	//
	//	private String ReadTest(DFileID f) {
	//		byte[] read = new byte[100];
	//		Main.globalDFS.read(f, read, 0, 50);
	//		return new String(read).trim();
	//	}
	//
	//	private void extTest() {
	//		System.out.println("Write test");
	//		WriteTest(dfid, "INITIAL");
	//
	//		String read = ReadTest(dfid);
	//		System.out.println("read: "+read);
	//
	//	}

	public static void main (String[] args) throws Exception {
		System.out.println("Initializing DFS");
		Main.globalVirtualDisk = new LocalVirtualDisk(Constants.vdiskName, false);
		Main.globalDBufferCache = new LocalDBufferCache(Constants.NUM_OF_CACHE_BLOCKS, Main.globalVirtualDisk);
		Main.globalDFS = new LocalDFS(Constants.vdiskName, false);
		Main.globalDFS.init();
		//DFileID file = Main.globalDFS.createDFile();
		System.out.println("Initialized");
		// DFileID file = dfiler.createDFile();
		//DFileID file = new DFileID(4);

		//TestClient2 tc = new TestClient2();
		//ArrayList<User> clients = test1();
		
		ArrayList<User> clients = new ArrayList<User>();
		User u0 = new User(null, null, 0, 0, DiskOperationType.CREATE);
		clients.add(u0);
		u0.start();
		String t = "Hello World";
		byte[] data = t.getBytes();
		byte[] read = new byte[data.length];
		User u1 = new User(((LocalDFS) Main.globalDFS).getDFileID(0), data, 0, data.length, DiskOperationType.WRITE);
		clients.add(u1);
		u1.start();
		//User u2 = new User(((LocalDFS) Main.globalDFS).getDFileID(0), data, 0, data.length, DiskOperationType.WRITE);
		User u2 = new User(((LocalDFS) Main.globalDFS).getDFileID(0), read, 0, data.length, DiskOperationType.READ); 
		clients.add(u2);
		u2.start();

//		for (User u : clients) {
//			u.start();
//		}
		// Sync files to disk
		for (User u : clients) {
			u.join();
		}
		printOutFSState();
		System.out.println(new String(read).trim());
		System.out.println("SHUTTING DOWN");
	}

//	private static ArrayList<User> test1() throws InterruptedException {
//		
//	}
	
	private static void printOutFSState() {
		for(Inode n : ((LocalDFS) Main.globalDFS).myInodes) {
			n.printOut();
		}
		
		System.out.println("free blocks: " + ((LocalDFS) Main.globalDFS).myFreeBlocks.size());
		System.out.println("free ids: " + ((LocalDFS) Main.globalDFS).myFreeDFID.size());
		System.out.println("used ids: " + ((LocalDFS) Main.globalDFS).myUsedDFID.size());
	}
}