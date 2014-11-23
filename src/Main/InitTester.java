package Main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import virtualdisk.Inode;
import virtualdisk.LocalVirtualDisk;
import virtualdisk.VirtualDisk;
import common.Constants;
import dfs.DFS;
import dfs.LocalDFS;

public class InitTester {

	public static void main(String[] args) throws IOException {
		//testOne();
		testTwo();
	}
	
	private static void testTwo() throws FileNotFoundException, IOException {
		RandomAccessFile file;
		String nameOfFile = "InitTest.dat";

		file = new RandomAccessFile(nameOfFile, "rws");
		file.setLength(Constants.BLOCK_SIZE * Constants.NUM_OF_BLOCKS);
		
		writeToFile(file);
		file.close();
		
		Main.globalVirtualDisk = new LocalVirtualDisk(nameOfFile, false);
		LocalDFS myDFS = new LocalDFS(nameOfFile, false);
//		myDFS.setVirtualDisk(myDisk);
//		myDFS.setRAFile(file);
		myDFS.init();
		
		int count = 0;
		for (Inode n : myDFS.myInodes) {
			System.out.print("\n"+count++);
			n.printOut();
		}
		
		System.out.println("free blocsk: " + myDFS.myFreeBlocks.size());
		System.out.println("free ids: " + myDFS.myFreeDFID.size());
		System.out.println("used ids: " + myDFS.myUsedDFID.size());
		
		//file.close();

	}
	

	private static void writeToFile(RandomAccessFile file) throws IOException {
		byte[] b = new byte[4];
		file.seek(0);		
		
		int seekLen = Constants.BLOCK_SIZE + 0*Constants.INODE_SIZE;
		
		//file.write(b, 0, b.length);

		
		file.seek(seekLen);
		writeToBytes(b, -1, -1, true);
		file.write(b, 0, b.length);
		writeToBytes(b, 0, 2, false);
		file.write(b, 0, b.length);
		writeToBytes(b, 2, 2, false);
		file.write(b, 0, b.length);
		writeToBytes(b, 0, 10, false);
		file.write(b, 0, b.length);
		
		
		// write to the correct block
		
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
	
	private static void testOne() throws FileNotFoundException, IOException {
		RandomAccessFile file;
		//byte[] b = new byte[Constants.INODE_SIZE];
		byte[] b = new byte[4];
		writeToBuffer(b);
		
		file = new RandomAccessFile("TestOne.dat", "rws");
		file.setLength(Constants.BLOCK_SIZE * Constants.NUM_OF_BLOCKS);
		
		int seekLen = 1*Constants.BLOCK_SIZE;
		file.seek(seekLen);
		file.write(b, 0, b.length);

		file.seek(seekLen);
		int x = file.readInt();
		int y = file.readInt();
		
		file.close();
		
		System.out.println(x);
		System.out.println(y);
	}

	private static void writeToBuffer(byte[] b) {
		
		b[0] = (byte) -1;
		b[1] = (byte) -1;
		b[2] = (byte) -1;
		b[3] = (byte) -1;

	}

}
