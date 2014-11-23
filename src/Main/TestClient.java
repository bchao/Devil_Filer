package Main;

import java.util.ArrayList;

import virtualdisk.LocalVirtualDisk;
import common.Constants;
import common.DFileID;
import dblockcache.LocalDBufferCache;
import dfs.LocalDFS;

public class TestClient implements Runnable {
	DFileID dfid;
	
	public TestClient(DFileID id, int i) {
		dfid = id;
	}
	
	private void WriteTest(DFileID f, String t) {
		byte[] data = t.getBytes();
		Main.globalDFS.write(f, data, 0, data.length);
	}
	
	private String ReadTest(DFileID f) {
		byte[] read = new byte[100];
		Main.globalDFS.read(f, read, 0, 50);
		return new String(read).trim();
	}
	
	private void extTest() {
		System.out.println("Write test");
		WriteTest(dfid, "INITIAL");
		
		String read = ReadTest(dfid);
		System.out.println("read: "+read);
		
	}

	public void run() {
		// TODO Auto-generated method stub
		extTest();
		Main.globalDFS.sync();
	}
	
	public static void main (String[] args) throws Exception {
        System.out.println("Initializing DFS");
		Main.globalVirtualDisk = new LocalVirtualDisk(Constants.vdiskName, false);
		Main.globalDBufferCache = new LocalDBufferCache(Constants.NUM_OF_CACHE_BLOCKS, Main.globalVirtualDisk);
		Main.globalDFS = new LocalDFS(Constants.vdiskName, false);
		Main.globalDFS.init();
        DFileID file = Main.globalDFS.createDFile();
        System.out.println("Initialized");
        // DFileID file = dfiler.createDFile();
        //DFileID file = new DFileID(4);

        ArrayList<Thread> clients = new ArrayList<Thread>();
        for (int i = 0; i < 1; i++) {
            TestClient tc = new TestClient(file, i);
            Thread f = new Thread(tc);
            clients.add(f);
            f.start();
        }
        // Sync files to disk
        for (Thread tc : clients) {
            tc.join();
        }
        System.out.println("SHUTTING DOWN");
    }
}