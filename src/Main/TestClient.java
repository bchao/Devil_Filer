package Main;

import java.io.IOException;
import java.io.RandomAccessFile;
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
//		System.out.println("Write test");
//		WriteTest(dfid, "INITIAL");
		
		String r1 = ReadTest(new DFileID(10));
		System.out.println("read: "+r1);
	}

	public void run() {
		// TODO Auto-generated method stub
		extTest();
		Main.globalDFS.sync();
	}
	
	private static void writeToFile(RandomAccessFile file) throws IOException {
		byte[] b = new byte[4];
		file.seek(0);		
		
		// Writing to inodes
		
		int seekLen = Constants.BLOCK_SIZE + 0*Constants.INODE_SIZE;
				
		file.seek(seekLen);
		writeToBytes(b, -1, -1, true);
		file.write(b, 0, b.length);
		writeToBytes(b, 0, 2, false);
		file.write(b, 0, b.length);
		writeToBytes(b, 2, 2, false);
		file.write(b, 0, b.length);
		writeToBytes(b, 0, 10, false);
		file.write(b, 0, b.length);
		
		seekLen = Constants.BLOCK_SIZE + 2*Constants.INODE_SIZE;
		file.seek(seekLen);
		writeToBytes(b, -1, -1, true);
		file.write(b, 0, b.length);
		writeToBytes(b, 1, 1, false);
		file.write(b, 0, b.length);
		writeToBytes(b, 0, 11, false);
		file.write(b, 0, b.length);
		writeToBytes(b, 1, 10, false);
		file.write(b, 0, b.length);		
		
		// Writing to blocks
		seekLen = (1 + 514)*Constants.BLOCK_SIZE + Constants.INODE_SIZE*Constants.MAX_INODE_BLOCKS;
		file.seek(seekLen);
		writeToBytes(b, 0, 20, false);
		file.write(b, 0, b.length);
		
		seekLen = (1 + 10)*Constants.BLOCK_SIZE + Constants.INODE_SIZE*Constants.MAX_INODE_BLOCKS;
		file.seek(seekLen);
		writeToBytes(b, 0, 10, false);
		file.write(b, 0, b.length);
	}
	
	private static void writeToBytes(byte[] b, int x, int y, boolean neg) {
		if (neg) {
			for (int i = 0; i < 4; i++) {
				b[i] = (byte) -1;
			}
		} else {
			b[0] = (byte) 0;
			b[1] = (byte) 0;
			b[2] = (byte) x;
			b[3] = (byte) y;
		}
	}
	
	public static void main (String[] args) throws Exception {
        System.out.println("Initializing DFS");

        RandomAccessFile rafile;
		String nameOfFile = "InitTest.dat";

		rafile = new RandomAccessFile(nameOfFile, "rws");
		rafile.setLength(Constants.BLOCK_SIZE * Constants.NUM_OF_BLOCKS);
		
		writeToFile(rafile);
		rafile.close();
        
//		Main.globalVirtualDisk = new LocalVirtualDisk(Constants.vdiskName, false);
		Main.globalVirtualDisk = new LocalVirtualDisk(nameOfFile, false);
		Main.globalDBufferCache = new LocalDBufferCache(Constants.NUM_OF_CACHE_BLOCKS, Main.globalVirtualDisk);
		Main.globalDFS = new LocalDFS(Constants.vdiskName, false);
		Main.globalDFS.init();
        Main.globalDFS.createDFile();
        System.out.println("Initialized");
        // DFileID file = dfiler.createDFile();
        DFileID file = new DFileID(4);

        ArrayList<Thread> clients = new ArrayList<Thread>();
//        for (int i = 0; i < 10; i++) {
//            TestClient tc = new TestClient(file, i);
            TestClient tc = new TestClient(file, 0);
            Thread f = new Thread(tc);
            clients.add(f);
            f.start();
//        }

        // Sync files to disk
//        for (Thread tc : clients) {
//            tc.join();
//        }
//        System.out.println("SHUTTING DOWN");
    }
}
