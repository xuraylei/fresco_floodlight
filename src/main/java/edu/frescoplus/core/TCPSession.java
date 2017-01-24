package edu.frescoplus.core;

import java.util.Objects;

//TODO: add flag into TCP session
public class TCPSession{
	int srcIP;
	int dstIP;
	int srcPort;
	int dstPort;
	
	
	public TCPSession(int sip, int sport, int dip, int dport){
		this.srcIP = sip;
		this.srcPort = sport;
		this.dstIP = dip;
		this.dstPort = dport;
		
	}
	
	public String toString(){
		return srcIP + ":" + srcPort + " <-> " + dstIP + ":" + dstPort;
	}
	
	
	public int getSrcIP(){
		return this.srcIP;
	}
	
	public int getSrcPort(){
		return this.srcPort;
	}
	
	public int getDstIP(){
		return this.dstIP;
	}
	
	public int getDstPort(){
		return this.dstPort;
	}
	
	//Overriding equals() and hashcode() for the key of hashmap
	@Override
	public int hashCode() {
		return Objects.hash(srcIP, srcPort, dstIP, dstPort);
	}
	
	@Override
	public boolean equals(Object o){
		if (o == null){
			return false;
		}
		
		if (!TCPSession.class.isAssignableFrom(o.getClass())) {
	        return false;
	    }
		
		final TCPSession s = (TCPSession) o;
		
		if ((this.srcIP == s.srcIP && this.srcPort == s.srcPort) 
				&& (this.dstIP == s.dstIP && this.dstPort == s.dstPort))
		{
			return true;
		}
		
		if((this.srcIP == s.dstIP && this.srcPort == s.dstPort) 
				&& (this.dstIP == s.srcIP && this.dstPort == s.srcPort))
		{
			return true;
		}
		
		return false;
	}
}