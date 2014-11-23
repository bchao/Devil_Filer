package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import common.Constants;
import common.Constants.DiskOperationType;
import dblockcache.DBuffer;

public class LocalVirtualDisk extends VirtualDisk implements Runnable {
	private Queue<Request> requestQueue;
	private boolean running;

	public LocalVirtualDisk(String volName, boolean format) throws FileNotFoundException, IOException {
		super(volName, format);
		
		requestQueue = new LinkedList<Request>();
	}
	
	public LocalVirtualDisk(boolean format) throws FileNotFoundException, IOException {
		super(format);
	}
	
	public LocalVirtualDisk() throws FileNotFoundException, IOException {
		super();
	}

	@Override
	public void startRequest(DBuffer buf, DiskOperationType operation) throws IllegalArgumentException, IOException {
		synchronized(requestQueue) {
			Request request = new Request(buf, operation);
			requestQueue.add(request);
			requestQueue.notifyAll();
		}
	}
	
	public void stopDisk() {
		synchronized(requestQueue) {
			running = false;
			requestQueue.notifyAll();
		}
	}

	public void run() {
		running = true;
		
		while(running) {
			synchronized(requestQueue) {
				while(requestQueue.isEmpty()) {
					try {
						requestQueue.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				//process
				Request request;
				synchronized(requestQueue) {
					request = requestQueue.poll();					
				}
				
				if(request == null) {
					return;
				}
				try {
					if(request.operation == DiskOperationType.READ) {
						readBlock(request.dbuffer);
					} else {
						writeBlock(request.dbuffer);
					}
				} catch ( Exception e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					request.dbuffer.ioComplete();
				}
			}
		}
	}

}
