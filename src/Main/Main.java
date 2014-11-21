package Main;

import java.io.FileNotFoundException;
import java.io.IOException;

import common.Constants;
import dblockcache.*;
import dfs.*;
import virtualdisk.*;

public class Main {
	public static VirtualDisk globalVirtualDisk;
	public static DFS globalDFS;
	public static DBufferCache globalDBufferCache;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		//create disk
		globalVirtualDisk = new LocalVirtualDisk(Constants.vdiskName, false);
		
		globalDFS = new LocalDFS(Constants.vdiskName, false);
		globalDFS.init();
		
		for(Inode n : ((LocalDFS) globalDFS).myInodes) {
			n.printOut();
		}
		System.out.println("free blocsk: " + ((LocalDFS) globalDFS).myFreeBlocks.size());
		System.out.println("free ids: " + ((LocalDFS) globalDFS).myFreeDFID.size());
		System.out.println("used ids: " + ((LocalDFS) globalDFS).myUsedDFID.size());
	}
}
