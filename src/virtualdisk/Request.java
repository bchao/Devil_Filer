package virtualdisk;

import common.Constants.DiskOperationType;
import dblockcache.DBuffer;

public class Request {
	public DBuffer dbuffer;
	public DiskOperationType operation;
	
	public Request(DBuffer db, DiskOperationType dot) {
		dbuffer = db;
		operation = dot;
	}
}
