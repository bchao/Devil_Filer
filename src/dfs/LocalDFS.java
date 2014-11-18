package dfs;

import java.util.List;

import common.Constants;
import common.DFileID;
import virtualdisk.LocalVirtualDisk;

public class LocalDFS extends DFS {

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
		for(int i = 1; i < Constants.MAX_INODE_BLOCKS + 1; i++) {
			for(int j = 0; j < 32; j++) {
				
			}
		}
		
		
	}

	@Override
	public DFileID createDFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void destroyDFile(DFileID dFID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sizeDFile(DFileID dFID) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<DFileID> listAllDFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sync() {
		// TODO Auto-generated method stub
		
	}

}
