package edu.frescoplus.core;



import org.slf4j.Logger;

import java.util.HashMap;

public abstract class AFP_Generic
{
	public final Logger log;
	public HashMap<String,String> database;

	public AFP_Generic(Logger log)
	{
		this.log = log;
		database = new HashMap<>();
	}


	public <T extends String> void logModuleError(T error)
	{
		log.error(error);
	}

	
//	public abstract FP_ModuleContext getModuleContext();

	/*New Model Section*/
	
	public abstract boolean isARP(FPContext cntx);
	public abstract boolean isIPv4(FPContext cntx);

	public abstract boolean isICMP(FPContext cntx);
	public abstract boolean isTCP(FPContext cntx);
	public abstract boolean isUDP(FPContext cntx);

	public abstract long getSrcMac(FPContext cntx);
	public abstract long getDstMac(FPContext cntx);
	
	public abstract int getSrcIP(FPContext cntx);
	public abstract int getDstIP(FPContext cntx);

	public abstract int getDstPort(FPContext cntx);
	public abstract int getSrcPort(FPContext cntx);
	
	//ARP packet utilities
	public abstract long getARPSenderMAC(FPContext cntx);
	public abstract int getARPSenderIP(FPContext cntx);
	public abstract long getARPTargetMAC(FPContext cntx);
	public abstract int getARPTargetIP(FPContext cntx);
	
	public abstract TCPSession getCurrentTCPsession(FPContext cntx);
	

	// Statistics
	// Query the open flow table for # of packets and # of bytes for a given host.
	// TODO: we create a flow table statistics module in SDN controller 
	public abstract int getPacketCount(int srcIP, int dstIP);
	public abstract int getByteCount(int srcIP, int dstIP);

	// Security Actions

	public abstract void BLOCK(FPContext cntx, int blockip);
	
	// Install a drop flow rule for this packet.
	public abstract void DROP(FPContext cntx);

	// Forward packet as normal.
	public abstract void FORWARD(FPContext cntx);

	// Overwrite the intended L3 destination of the packet.
	public abstract void REDIRECT(FPContext flCntx, int srcIP, int dstIP, int rpIP);

	// Reflect packet back at the origin.
	public abstract void MIRROR(FPContext flCntx, long swid, int portid);

	// Tag the packet and only allow it to contact certain hosts.
	public abstract void QUARANTINE(FPContext cntx, int qip, boolean isnotify, long mac, int ip, int port);

	
	// External Events
	public int getTcpFailCount(int src_address)
	{
		// Query external event monitor.
		return 0;
	}
	public boolean isTcpFail(int dst_address)
	{
		// Query external event monitor.
		return false;
	}


	public abstract int parseIPv4(String s);

	public abstract long parseMAC(String mac);

	public abstract boolean parseBoolean(String string);

	public abstract int parseInteger(String string);

	public abstract long parseLong(String string);


}

