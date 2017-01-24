package edu.frescoplus.securityaction;

import edu.frescoplus.securityaction.SATYPE;

public class RedirectAction extends SecurityAction{
	//mactch field
	private int srcIP;
	private int srcPort;
	private int dstIP;
	private int dstPort;
	
	private boolean ignorePort;
	
	//redirect destination
	private long dl_rd;
	private int  nw_rd;
	private int  port_rd;
	
	public RedirectAction(SATYPE sat, int sip, int dip, long dl, int nw){
		super (sat);
		
		this.srcIP = sip;
		this.dstIP = dip;
		this.dl_rd = dl;
		this.nw_rd = nw;
		
		this.ignorePort = true;
		
		this.srcPort = NULLVALUE;
		this.dstPort = NULLVALUE;
		this.port_rd = NULLVALUE;
	}
	
	public RedirectAction(SATYPE sat, int sip, int sport, 
			int dip, int dport, long dl, int nw, int port){
		super (sat);
		
		this.srcIP = sip;
		this.dstIP = dip;
		this.dl_rd = dl;
		this.nw_rd = nw;
		
		this.ignorePort = false;
		
		this.srcPort = sport;
		this.dstPort = dport;
		this.port_rd = port;
	}
	
	public RedirectAction(SATYPE sat, int sip, 
			int dip, int nw){
		super (sat);
		
		this.srcIP = sip;
		this.dstIP = dip;
		this.nw_rd = nw;
		
		this.ignorePort = true;
		
		this.srcPort = NULLVALUE;
		this.dstPort = NULLVALUE;
		this.port_rd = NULLVALUE;
	}
	
	public int getSourceIP(){
		return this.srcIP;
	}
	
	public int getDestinationIP(){
		return this.dstIP;
	}
	
	public int getSourcePort(){
		return this.srcPort;
	}
	
	public int getDestinationPort(){
		return this.dstPort;
	}

	
	public long getRedirectedDLAddr(){
		return this.dl_rd;
	}
	
	public int getRedirectedNWAddr(){
		return this.nw_rd;
	}
	
	public int getRedirectedPort(){
		return this.port_rd;
	}
	
	public boolean isIgnorePort(){
		return this.ignorePort;
	}
}