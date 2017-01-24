package edu.frescoplus.securityaction;

public class QuarantineAction extends SecurityAction {
	
	//match field
	private int quarantinedIP;
	
	//notifier server info
	boolean hasNotifier;
	long mac;
	int  ip;
	int  port;
	
	//by default, we block both DNS and HTTP packet for quarantined hosts
	public QuarantineAction(SATYPE sat, int qip, boolean notifier, long mac, int ip, int port) {
		super(sat);
		
		this.quarantinedIP = qip;
		
		this.hasNotifier = notifier;
		
		if (this.hasNotifier){
			this.mac = mac;
			this.ip = ip;
			this.port = port;
		}
		else{
			this.mac = NULLVALUE;
			this.ip = NULLVALUE;
			this.port = NULLVALUE;
		}
	}
	
	public int getQuarantinedIP(){
		return this.quarantinedIP;
	}
	
	//retrieve notifier info
	public boolean hasNotifeir(){
		return this.hasNotifier;
	}
	
	public long getNotifierMACAddr(){
		return this.mac;
	}
	
	public int getNotifierNWAddr(){
		return this.ip;
	}
	
	public int getNotifierPortNum(){
		return this.port;
	}
	
}
