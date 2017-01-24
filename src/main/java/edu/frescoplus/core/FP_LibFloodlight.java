package edu.frescoplus.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.frescoplus.event.FP_Event;
import edu.frescoplus.networkelement.FP_Link;
import edu.frescoplus.securityaction.BlockAction;
import edu.frescoplus.securityaction.MirrorAction;
import edu.frescoplus.securityaction.QuarantineAction;
import edu.frescoplus.securityaction.RedirectAction;
import edu.frescoplus.securityaction.SATYPE;
import edu.frescoplus.securityaction.SecurityAction;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IListener.Command;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.routing.Link;

enum TCPSTATE{
	SYN, SYNACK, ESTABLISHED, OTHERS
}

public class FP_LibFloodlight extends AFP_Generic{
	
	public FP_LibFloodlight(Logger log) {
		super(log);
	}

	protected static final Logger log = LoggerFactory.getLogger(FP_FloodlightRTE.class);
	
	//Fresco Module List
	List <String> moduleList = new ArrayList<>();
	
	// Execution context for FRESCO modules. 
	//FP_ModuleContext fpmContext = new FP_ModuleContext();
	
	
	//TCP context.
	HashMap<TCPSession, TCPSTATE> tcpState = new HashMap<>();
	HashMap<FPContext, TCPSession> curTCPSession = new HashMap<>();
	
	//Link context
	Set<FP_Link> switchLinks = new HashSet<>();
	Set<FP_Link> edgeLinks = new HashSet<>();
	
	Command defaultCMD  = Command.CONTINUE;
	
	//the DB to store result command for each flow, current is indexed by Floodlight Context
	HashMap <FPContext, Command> commandDic = 
								new HashMap<FPContext, Command>();

	HashMap <FPContext, ArrayList<SecurityAction>> SADic = 
								new HashMap<FPContext, ArrayList<SecurityAction>>();
	
	 
	
	//public FP_ModuleContext getModuleContext(){
	//	return this.fpmContext;
	//}
	
	
	//======================================================================
	//policies for current flows
	public Command getForwardingDecesion(FPContext cntx){
		//retrieve cached command for the flow if applicable
		if (commandDic.containsKey(cntx)){
			return commandDic.remove(cntx);
		}
		else{ 
			//default command for new packets
			return defaultCMD;
		}
	}
	
	//policies for future flows 
	public ArrayList<SecurityAction> getSecurityActions(FPContext cntx){
		ArrayList<SecurityAction> sas = SADic.get(cntx);
		return sas;
		
	}
	
	public void addSecurityAction(FPContext cntx, SecurityAction sa){
		
		ArrayList<SecurityAction> sas = SADic.get(cntx);
		
		if (sas == null){
			sas = new ArrayList<SecurityAction>();
		}
		sas.add(sa);
		this.SADic.put(cntx, sas);
	}
	
	
	//================================================================================
	//store TCP session state info into DataBase
	//input: Floodlight packet context
	public FP_Event processTCP(FPContext cntx){ 
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		if(eth.getEtherType() == EthType.IPv4)
		{	
			IPv4 ipv4 = (IPv4) eth.getPayload();
			
			if (ipv4.getProtocol() == IpProtocol.TCP){
				TCP tcp = (TCP) ipv4.getPayload();
				short flag = tcp.getFlags();
				
				//We assume here we have visibility of all TCP handshake messages
				TCPSession session;
				
				if (flag == 2){
					session = new TCPSession(ipv4.getSourceAddress().getInt(),
						tcp.getSourcePort().getPort(), ipv4.getDestinationAddress().getInt(),
						tcp.getDestinationPort().getPort());
				}
				else{
					session = new TCPSession(ipv4.getDestinationAddress().getInt(),
							tcp.getDestinationPort().getPort(), ipv4.getSourceAddress().getInt(),
							tcp.getSourcePort().getPort());
				}
					
				//store current TCP session into runtime context
				curTCPSession.put(cntx, session);
				
				TCPSTATE state = tcpState.get(session);
				
				if (flag == 2){//SYN
					if (state == null){ 
						state = TCPSTATE.SYN;
						session = new TCPSession(ipv4.getSourceAddress().getInt(),
								tcp.getSourcePort().getPort(), ipv4.getDestinationAddress().getInt(),
								tcp.getDestinationPort().getPort());
						tcpState.put(session, state);
						return FP_Event.TCP;
					 }
				}
				else if (flag == 16){//ACK
					if (state == TCPSTATE.SYNACK){
						 state = TCPSTATE.ESTABLISHED;
						 tcpState.put(session, state);
						 return FP_Event.TCP_CONNECTION_SUCCESS;
					 }
				}
				else if (flag == 18){ //SYNACK
					 if (state == TCPSTATE.SYN){
						 state = TCPSTATE.SYNACK;
						 tcpState.put(session, state);
					 }
				 }
				else if (flag == 20){//RSTACK
					state = TCPSTATE.OTHERS;
				 	tcpState.put(session, state);
				 	return FP_Event.TCP_CONNECTION_FAIL;
				}
				 // we consider other flags are TCP connection disruptions
				 // we will raise TCP connection disruption events
				 //TODO: More options to distinguish different TCP flags
				 else { 
					 if (state != TCPSTATE.ESTABLISHED){
						state = TCPSTATE.OTHERS;
					 	tcpState.put(session, state);
					 	return FP_Event.TCP_CONNECTION_FAIL;
					 }
					 else{//state is TCPSTATE.ESTABLISHED
						 if (flag == 1){ // TCP FIN
							 state = TCPSTATE.OTHERS;
							 tcpState.put(session, state);
							 return FP_Event.TCP_CONNECTION_FAIL;
						 }
					 }
				 }
				 return FP_Event.TCP;
			}
		}
		return FP_Event.PACKET;
	}
	
	public void processSwitchLinkUpdate(Set<Link> links){
		for (Link l : links){
			FP_Link link = new FP_Link(l);
		
			switchLinks.add(link);
		}
	}
	
	public void processHostLinkUpdate(Set<Link> links){
		
	}
	
	//-----------------------------------------------------------------
	//methods to retrieve information of incoming flow
	@Override
	public boolean isARP(FPContext cntx) {
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		
		return (eth.getEtherType() == EthType.ARP);

	}
	
	@Override
	public boolean isIPv4(FPContext cntx) {
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		if(eth.getEtherType() == EthType.IPv4)
		{		
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean isICMP(FPContext cntx) {
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		if(eth.getEtherType() == EthType.IPv4)
		{	
			IPv4 ipv4 = (IPv4) eth.getPayload();
			return (ipv4.getProtocol() == IpProtocol.ICMP);
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean isTCP(FPContext cntx) {
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		if(eth.getEtherType() == EthType.IPv4)
		{	
			IPv4 ipv4 = (IPv4) eth.getPayload();
			return (ipv4.getProtocol() == IpProtocol.TCP);
		}
		else
		{
			return false;
		}
	}
	
	
	@Override
	public boolean isUDP(FPContext cntx) {
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		if(eth.getEtherType() == EthType.IPv4)
		{	
			IPv4 ipv4 = (IPv4) eth.getPayload();
			return (ipv4.getProtocol() == IpProtocol.UDP);
		}
		else
		{
			return false;
		}
	}
	
	
	@Override
	public long getSrcMac(FPContext cntx) {
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		
		MacAddress srcMac = eth.getSourceMACAddress();
		
		return srcMac.getLong();
		
		
	}
	
	@Override
	public long getDstMac(FPContext cntx) {
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		
		MacAddress dstMac = eth.getSourceMACAddress();
		
		return dstMac.getLong();
		
		
	}
		
	@Override
	public int getSrcIP(FPContext cntx) {
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		IPv4Address srcIP;
		
		if(eth.getEtherType() == EthType.IPv4)
		{		
			IPv4 ipv4 = (IPv4) eth.getPayload();
			srcIP = ipv4.getSourceAddress();
			
			return srcIP.getInt();
		}
		else if (eth.getEtherType() == EthType.ARP){
			ARP arp = (ARP) eth.getPayload();
			srcIP = arp.getSenderProtocolAddress();
			
			return srcIP.getInt();
		}
			
		//for other packets without source IP information	
		return 0;
		
	}

	@Override
	public int getDstIP(FPContext cntx) {
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		IPv4Address dstIP;
		
		if(eth.getEtherType() == EthType.IPv4)
		{		
			IPv4 ipv4 = (IPv4) eth.getPayload();
			dstIP = ipv4.getDestinationAddress();
			return dstIP.getInt();
		}
		else if (eth.getEtherType() == EthType.ARP){
			ARP arp = (ARP) eth.getPayload();

			dstIP = arp.getTargetProtocolAddress();

			return dstIP.getInt();
		}
		
		//for other packets without destination IP information
		return 0;
		
	}
	
	@Override
	public int getDstPort(FPContext cntx) {
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		if(eth.getEtherType() == EthType.IPv4)
		{		
			IPv4 ipv4 = (IPv4) eth.getPayload();
			if( isTCP(cntx) )
			{
				TCP tcp = (TCP) ipv4.getPayload();
				return tcp.getDestinationPort().getPort();	
			}
			else if ( isUDP(cntx) )
			{
				UDP udp = (UDP) ipv4.getPayload();
				return udp.getDestinationPort().getPort();
			}
			else
			{
				return 0;
			}
		}
		else
		{
			return 0;
		}
	}

	@Override
	public int getSrcPort(FPContext cntx) {
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		if(eth.getEtherType() == EthType.IPv4)
		{		
			IPv4 ipv4 = (IPv4) eth.getPayload();
			if( isTCP(cntx) )
			{
				TCP tcp = (TCP) ipv4.getPayload();
				return tcp.getSourcePort().getPort();	
			}
			else if ( isUDP(cntx) )
			{
				UDP udp = (UDP) ipv4.getPayload();
				return udp.getSourcePort().getPort();
			}
			else
			{
				return 0;
			}
		}
		else
		{
			return 0;
		}
	}


	
	@Override
	public int getPacketCount(int srcIP, int dstIP) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getByteCount(int srcIP, int dstIP) {
		// TODO Auto-generated method stub
		return 0;
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	//ARP packet utilities
	@Override
	public long getARPSenderMAC(FPContext cntx){
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		MacAddress senderMAC;
		
		if (eth.getEtherType() == EthType.ARP){
			ARP arp = (ARP) eth.getPayload();
			
			senderMAC = arp.getSenderHardwareAddress();
			
			return senderMAC.getLong();
		}
		
		//for other non-arp packets
		return 0;
	}
	
	@Override
	public int getARPSenderIP(FPContext cntx){
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		
		IPv4Address senderIP;
		
		if (eth.getEtherType() == EthType.ARP){
			ARP arp = (ARP) eth.getPayload();
			
			senderIP = arp.getSenderProtocolAddress();
			
			return senderIP.getInt();
		}
		
		//for other non-arp packets
		return 0;
	}
	
	@Override
	public long getARPTargetMAC(FPContext cntx){
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		MacAddress senderMAC;
		
		if (eth.getEtherType() == EthType.ARP){
			ARP arp = (ARP) eth.getPayload();
			
			senderMAC = arp.getTargetHardwareAddress();
			
			return senderMAC.getLong();
		}
		
		//for other non-arp packets
		return 0;
	}
	
	@Override
	public int getARPTargetIP(FPContext cntx){
		FloodlightContext flCntx = cntx.getFlowContext();
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(flCntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		
		IPv4Address senderIP;
		
		if (eth.getEtherType() == EthType.ARP){
			ARP arp = (ARP) eth.getPayload();
			
			senderIP = arp.getTargetProtocolAddress();
			
			return senderIP.getInt();
		}
		
		//for other non-arp packets
		return 0;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public TCPSession getCurrentTCPsession(FPContext cntx){
		return curTCPSession.get(cntx);
	}
	
	//Link info retrieval functions
	public Set<FP_Link> getAllSwitchLinks(){
	
		return this.switchLinks;
	}
	
	public Set<FP_Link> getAllEdgeLinks(){
		
		return this.edgeLinks;
	}
	
	public int getLinkThroughtput(FP_Link link){
		if (!switchLinks.contains(link) && !edgeLinks.contains(link)){
			return 0;
		}
		
		return link.getThroughtput();
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	//security directives
	
	/*Drop a specific flow but not install blocking rules*/
	@Override
	public void DROP(FPContext cntx) {
		this.commandDic.put(cntx, Command.STOP);
	}

	@Override
	public void FORWARD(FPContext cntx) {
		this.commandDic.put(cntx, Command.CONTINUE);
	}
	
	/*Block a specific host*/
	@Override
	public void BLOCK(FPContext cntx, int bip){
		
		BlockAction ba = new BlockAction(SATYPE.BLOCK, bip);
		addSecurityAction(cntx, ba);
	}

	/*packet-level redirect directive*/
	@Override
	public void REDIRECT(FPContext cntx, int srcIP, int dstIP, int rpIP) {
		/*install flow rules to allow <srcIP> to <rpIP>
		install flow rules to allow <rpIP> to <srcIP>
		decline flow rules from <dstIP> to <srcIP>
		*/
		
		RedirectAction ra = new RedirectAction(SATYPE.REDIRECT, srcIP, dstIP, rpIP);
		addSecurityAction(cntx, ra);
	}

	/*packet-level mirror directive*/
	@Override
	public void MIRROR(FPContext cntx, long swid, int portid) {
		/*mirror incoming packets to a specific switch port for further 
		 * security analysis
		 * */
		MirrorAction ma = new MirrorAction(SATYPE.MIRROR, swid, portid);
		
		addSecurityAction(cntx, ma);
	}

	/*quarantine a specific host*/
	@Override
	public void QUARANTINE(FPContext cntx, int qip, boolean isNotify, long mac, int ip, int port) {
		QuarantineAction qa = new QuarantineAction(SATYPE.QUARANTINE, qip, isNotify, mac, ip, port);
	
		addSecurityAction(cntx, qa);
	
	}
	
	/*parse IPv4 Address from Dot-decimal notation to Integer*/
	@Override
	public int parseIPv4(String s){
		return IPv4.toIPv4Address(s);
	}
	
	/*parse MAC Address from string to long*/
	@Override
	public long parseMAC(String mac){
		MacAddress macAddr = MacAddress.of(mac);
		
		if (macAddr != null){
			return macAddr.getLong();
		}
		else{
			return -1;
		}
	}

	@Override
	public boolean parseBoolean(String string) {
		return Boolean.parseBoolean(string);
	}

	@Override
	public int parseInteger(String string) {
		return Integer.parseInt(string);
	}

	@Override
	public long parseLong(String string) {
		return Long.parseLong(string);
	}

}
