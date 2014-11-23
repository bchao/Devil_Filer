package virtualdisk;
/*
 * VirtualDisk.java
 *
 * A virtual asynchronous disk.
 *
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;

import virtualdisk.Inode.memBlock;
import common.Constants;
import common.Constants.DiskOperationType;
import dblockcache.DBuffer;

public abstract class VirtualDisk implements IVirtualDisk, Runnable {

	private String _volName;
	private RandomAccessFile _file;
	private int _maxVolSize;

	/*
	 * VirtualDisk Constructors
	 */
	public VirtualDisk(String volName, boolean format) throws FileNotFoundException,
			IOException {

		_volName = volName;
		_maxVolSize = Constants.BLOCK_SIZE * Constants.NUM_OF_BLOCKS;

		/*
		 * mode: rws => Open for reading and writing, as with "rw", and also
		 * require that every update to the file's content or metadata be
		 * written synchronously to the underlying storage device.
		 */
		_file = new RandomAccessFile(_volName, "rws");

		/*
		 * Set the length of the file to be NUM_OF_BLOCKS with each block of
		 * size BLOCK_SIZE. setLength internally invokes ftruncate(2) syscall to
		 * set the length.
		 */
		_file.setLength(Constants.BLOCK_SIZE * Constants.NUM_OF_BLOCKS);
		if(format) {
			formatStore();
		}
		/* Other methods as required */
	}
	
	public VirtualDisk(boolean format) throws FileNotFoundException,
	IOException {
		this(Constants.vdiskName, format);
	}
	
	public RandomAccessFile returnRAF() {
		return _file;
	}
	
	public VirtualDisk() throws FileNotFoundException,
	IOException {
		this(Constants.vdiskName, false);
	}

	/*
	 * Start an asynchronous request to the underlying device/disk/volume. 
	 * -- buf is an DBuffer object that needs to be read/write from/to the volume.	
	 * -- operation is either READ or WRITE  
	 */
	public abstract void startRequest(DBuffer buf, DiskOperationType operation) throws IllegalArgumentException,
			IOException;
	
	/*
	 * Clear the contents of the disk by writing 0s to it
	 */
	private void formatStore() {
		byte b[] = new byte[Constants.BLOCK_SIZE];
		setBuffer((byte) 0, b, Constants.BLOCK_SIZE);
		for (int i = 0; i < Constants.NUM_OF_BLOCKS; i++) {
			try {
				int seekLen = i * Constants.BLOCK_SIZE;
				_file.seek(seekLen);
				_file.write(b, 0, Constants.BLOCK_SIZE);
			} catch (Exception e) {
				System.out.println("Error in format: WRITE operation failed at the device block " + i);
			}
		}
	}

	/*
	 * helper function: setBuffer
	 */
	private static void setBuffer(byte value, byte b[], int bufSize) {
		for (int i = 0; i < bufSize; i++) {
			b[i] = value;
		}
	}

	/*
	 * Reads the buffer associated with DBuffer to the underlying
	 * device/disk/volume
	 */
	protected int readBlock(DBuffer buf) throws IOException {
		//offsetting for empty block zero and inode region
		int seekLen = getOffset(buf);
		/* Boundary check */
		if (_maxVolSize < seekLen + Constants.BLOCK_SIZE) {
			return -1;
		}
		_file.seek(seekLen);
		return _file.read(buf.getBuffer(), 0, Constants.BLOCK_SIZE);
	}
	
	public void writeInode(int id, Inode inode) throws IOException {
		int seekLen = (1 + id) * Constants.BLOCK_SIZE;
		
		_file.seek(seekLen);
		// boolean inUse
		byte[] inUse = (inode.getInUse() == true) ? intToBytes(-1) : intToBytes(0);
		_file.write(inUse);
		
		// int fileSize
		_file.write(intToBytes(inode.getSize()));
		
		// blocks of memory
		memBlock[] blocks = inode.getBlockList();
		for(int i = 0; i < 4; i++) {
			if(blocks[i] != null) {
				_file.write(intToBytes(blocks[i].getBlockID()));
			}
			else {
				// or should I continue and skip over this?
				break;
			}
		}
	}
	
	private static byte[] intToBytes(int x) throws IOException {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    DataOutputStream out = new DataOutputStream(bos);
	    out.writeInt(x);
	    out.close();
	    byte[] int_bytes = bos.toByteArray();
	    bos.close();
	    return int_bytes;
	}

	/*
	 * Writes the buffer associated with DBuffer to the underlying
	 * device/disk/volume
	 */
	protected void writeBlock(DBuffer buf) throws IOException {
		int seekLen = getOffset(buf);
		_file.seek(seekLen);
		_file.write(buf.getBuffer(), 0, Constants.BLOCK_SIZE);
	}

	private int getOffset(DBuffer buf) {
		return (buf.getBlockID() + 1 + Constants.MAX_INODE_BLOCKS) * Constants.BLOCK_SIZE;
	}
}
