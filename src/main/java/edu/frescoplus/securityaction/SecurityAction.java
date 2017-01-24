package edu.frescoplus.securityaction;

public class SecurityAction {
	protected final int NULLVALUE = -1;
	
	SATYPE saType;
	
	public SecurityAction(SATYPE sat){
		this.saType = sat;
	}
	
	public SATYPE getType(){
		return this.saType;
	}
}

