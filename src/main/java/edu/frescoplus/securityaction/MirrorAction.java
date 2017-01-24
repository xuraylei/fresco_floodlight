package edu.frescoplus.securityaction;

public class MirrorAction extends SecurityAction {
	//the Location of mirror packet
	private long dpid;	//Data Plane ID
	private int portid;	//Port ID
	
	public MirrorAction(SATYPE sat, long did, int pid) {
		super(sat);
		
		this.dpid = did;
		this.portid = pid;
	}
	
	public long getDPID(){
		return this.dpid;
	}
	
	public int getPortID(){
		return this.portid;
	}
}