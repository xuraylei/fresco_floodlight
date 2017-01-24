package edu.frescoplus.securityaction;

public class BlockAction extends SecurityAction {

	//match field
	private int blockedIP;
	
	public BlockAction(SATYPE sat, int bip) {
		super(sat);
	
		this.blockedIP = bip;
	}
	
	public int getBlockedIP(){
		return this.blockedIP;
	}

}
