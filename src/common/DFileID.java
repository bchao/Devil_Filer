package common;

/* typedef DFileID to int */
public class DFileID {

	private int _dFID;
	private boolean inUse;
	private int mySize;

	public DFileID(int dFID) {
		_dFID = dFID;
		inUse = false;
		mySize = 0;
	}

	public int getDFileID() {
		return _dFID;
	}
	    
	public boolean equals(Object other){
		DFileID otherID =  (DFileID) other;
		if(otherID.getDFileID() == _dFID){
			return true;
		}
		return false;
	}
	    
	public String toString(){
		return _dFID+"";
	}
	
	public synchronized boolean isInUse() {
		return inUse;
	}
	
	public synchronized void setInUse(boolean b) {
		inUse = b;
		
	}
}
