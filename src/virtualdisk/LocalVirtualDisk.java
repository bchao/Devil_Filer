package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;

import common.Constants.DiskOperationType;
import dblockcache.DBuffer;

public class LocalVirtualDisk extends VirtualDisk {

	public LocalVirtualDisk(String volName, boolean format) throws FileNotFoundException, IOException {
		super(volName, format);
	}
	
	public LocalVirtualDisk(boolean format) throws FileNotFoundException,
	IOException {
		super(format);
	}
	
	public LocalVirtualDisk() throws FileNotFoundException,
	IOException {
		super();
	}

	@Override
	public void startRequest(DBuffer buf, DiskOperationType operation)
			throws IllegalArgumentException, IOException {
		// TODO Auto-generated method stub
		
	}

}
