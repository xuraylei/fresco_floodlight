package edu.frescoplus.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.U16;
import org.projectfloodlight.openflow.types.U64;
import org.python.constantine.platform.darwin.IPProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.frescoplus.event.FP_Event;
import edu.frescoplus.event.FP_EventManager;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.securityaction.BlockAction;
import edu.frescoplus.securityaction.MirrorAction;
import edu.frescoplus.securityaction.QuarantineAction;
import edu.frescoplus.securityaction.RedirectAction;
import edu.frescoplus.securityaction.SecurityAction;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Route;
import net.floodlightcontroller.topology.NodePortTuple;
import net.floodlightcontroller.util.FlowModUtils;
import net.floodlightcontroller.util.OFMessageDamper;

public class FP_FloodlightRTE implements  
				IFloodlightModule, IOFMessageListener, ILinkDiscoveryListener{
	//log
	protected static final Logger log = LoggerFactory.getLogger(FP_FloodlightRTE.class);
	
	protected static int OFMESSAGE_DAMPER_CAPACITY = 10000; 
	protected static int OFMESSAGE_DAMPER_TIMEOUT = 250;
	
    protected IDeviceService deviceService;
    protected IRoutingService routingService;
    protected IOFSwitchService switchService;
    protected ILinkDiscoveryService linkService;
	
	private boolean VERBOSE = true;
	
	//the priority for security flow rules, including 5 levels
	protected static int SEC_PRIORITY_0 = 1000;
	protected static int SEC_PRIORITY_1 = 1001;
	protected static int SEC_PRIORITY_2 = 1002;
	protected static int SEC_PRIORITY_3 = 1003;
	protected static int SEC_PRIORITY_4 = 1004;
	
	//TODO: change to relative path
	String FRESCO_SCRIPTS_LOCATION = "fresco_apps/enable";
	String FRESCO_MODULE_PACKAGE = "edu.frescoplus.module.";
	
	protected IFloodlightProviderService floodlightProvider;
	
	// The FRESCO library
	FP_LibFloodlight library;
	
	//Fresco Applications
    ArrayList<FPM_Graph> fpApps = new ArrayList<FPM_Graph>();;

	
	// Instance for FRESCO event manager
	FP_EventManager fpeManager = new FP_EventManager();
	
	
	protected OFMessageDamper messageDamper;
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return FP_FloodlightRTE.class.getName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg,
			FloodlightContext flCntx) {
		
		FPContext cntx = new FPContext(flCntx);
		
		//deliver new flows to corresponding apps/modules.
		fpeManager.raiseEvent(cntx, FP_Event.PACKET);
		
		ArrayList<SecurityAction> sas_packet = library.getSecurityActions(cntx);
		if (sas_packet != null){
			for (SecurityAction sa : sas_packet){
				enforceSecurityAcitions(sw, cntx, sa);
			}
		}
		
		//early stop when drop flow is enforced
		if(library.getForwardingDecesion(cntx) == Command.STOP){
			return Command.STOP;
		}
		

		if (library.isTCP(cntx)){
			FP_Event event = library.processTCP(cntx);
			
			// we ignore installing flow rules for TCP packets
			if (event == FP_Event.TCP || event == FP_Event.TCP_CONNECTION_FAIL ||
					event == FP_Event.TCP_CONNECTION_SUCCESS){
				fpeManager.raiseEvent(cntx, event);
				ArrayList<SecurityAction> sas_tcp = library.getSecurityActions(cntx);
				if (sas_tcp != null){
					for (SecurityAction sa : sas_tcp){
						enforceSecurityAcitions(sw, cntx, sa);
					}
				}
				installTCPProcessingRules(flCntx);
				
				return Command.STOP;
			}
		}

		return library.getForwardingDecesion(cntx);
	}
	
	//we use PacketOut to transfer TCP packets instead of FlowMod
	//we use transportation technique to forward TCP packet direct to destination location
	private void installTCPProcessingRules(FloodlightContext cntx){
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		IPv4 ipv4 = (IPv4) eth.getPayload();
		
		int dstIP = ipv4.getDestinationAddress().getInt();
		
		IDevice dstDevice = getDeviceFromIP(dstIP);
		
		if (dstDevice == null){
			log.error("[FRESCO] cannot send out TCP packets due to failure to locate destination");
			return;
		}
		
        if (dstDevice.getAttachmentPoints().length < 1){
        	log.error("[FRESCO] can not install TCP processing"
        			+ " flow rules due to missing host location info");
         	return;
        }
        
        SwitchPort dstLocation = getLocationFromDevice(dstDevice);
        IOFSwitch sw = switchService.getSwitch(dstLocation.getSwitchDPID());
        
        if (sw == null){
        	log.error("[FRESCO] can not install TCP processing"
        			+ " due to to destionation switch is offline");
        }
		
		OFPacketOut.Builder pob = sw.getOFFactory().buildPacketOut();

        // set actions
        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(sw.getOFFactory().actions().buildOutput().
        		setPort(dstLocation.getPort()).setMaxLen(Integer.MAX_VALUE).build());

        pob.setActions(actions);
        
        byte[] packetData = eth.serialize();
        pob.setData(packetData);
 
        sw.write(pob.build());
	}
	
/////////////////////////////////////////////////////////////////////
//
	private IDevice getDeviceFromIP(int ip){
		IDevice device = null;
		
		 for (IDevice d : deviceService.getAllDevices()) {
	            for (int j = 0; j < d.getIPv4Addresses().length; j++) {
	                    if (device == null && 
	                    		(ip == d.getIPv4Addresses()[j].getInt())){
	                    	device = d;
	                    }
	            }
		 }
		return device;
	}
	
	private SwitchPort getLocationFromDevice(IDevice d){
		
		
		if (d != null && d.getAttachmentPoints() != null && d.getAttachmentPoints().length > 0)
			return d.getAttachmentPoints()[0];
		return null;
	}
	
	private OFFlowMod createFRESCOFlowMod(IOFSwitch sw, Match match, List<OFAction> actions, int priority){
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowAdd();;
		
		fmb.setIdleTimeout(FlowModUtils.INFINITE_TIMEOUT);
		fmb.setHardTimeout(FlowModUtils.INFINITE_TIMEOUT);
		fmb.setBufferId(OFBufferId.NO_BUFFER);
		fmb.setOutPort(OFPort.ANY);
		fmb.setCookie(U64.of(0));  
		
		fmb.setPriority(U16.t(priority));
		fmb.setMatch(match);
		fmb.setActions(actions);
		
		return fmb.build();
		
	}
///////////////////////////////////////////////////////////////////////
	//install flow rules in the ingress switch to block host traffic
	private void enforceBlockAction(BlockAction ba){
		//block flow rule lists to instaled in data plane
		ArrayList<OFFlowMod> blockFMs = new ArrayList<>();
		
		OFFlowMod blockFM;
		
		IDevice blockedDevice = getDeviceFromIP(ba.getBlockedIP());
		
		if (blockedDevice == null){
			log.error("[FRESCO] Block host " + IPv4Address.of(ba.getBlockedIP()) + " fail because cannot locate the host location");
			return;
		}
		
		SwitchPort blockedLocation = getLocationFromDevice(blockedDevice);		
		IOFSwitch inSW = switchService.getSwitch(blockedLocation.getSwitchDPID());
		
		Match.Builder mb = inSW.getOFFactory().buildMatch();
		mb.setExact(MatchField.IPV4_SRC, IPv4Address.of(ba.getBlockedIP()));
		
        List<OFAction> blockActions = new ArrayList<OFAction>();
        
        blockFM = createFRESCOFlowMod(inSW, mb.build(), blockActions, SEC_PRIORITY_0);
        
        
        blockFMs.add(blockFM);
 	   	
	    //enforce block flow rules
        for (OFFlowMod fm : blockFMs){
          try {
				messageDamper.write(inSW, fm);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } 
	}
	
	
	//Install both ingress and egress flow rules for redirection directive
	//We hold some assumptions for redirection directive:
	//1. the new destination (redirected target) is host-reachable 
	//2. there is a route between the source and the new destination
	private void enforceRedirectAction(RedirectAction ra, boolean isARP, boolean isIgnorePort){
		//redirect flow rule list to install in data plane
		//ArrayList<OFFlowMod> redirectFMs = new ArrayList<>();
		
        IDevice srcDevice = getDeviceFromIP(ra.getSourceIP());
        IDevice dstDevice = getDeviceFromIP(ra.getDestinationIP());
        IDevice redirectDevice = getDeviceFromIP(ra.getRedirectedNWAddr());
        
        //we assume the redirected destination is retrieved in device store
        if (srcDevice == null || redirectDevice == null) {
        	log.error("[FRESCO] can not install redirect flow rules due to failure to find out host instance");
         	return;
        }
        
        SwitchPort srcLocation = getLocationFromDevice(srcDevice);
        SwitchPort redirectLocation = getLocationFromDevice(redirectDevice);
              
        Route routeIn = routingService.getRoute(srcLocation.getSwitchDPID(), srcLocation.getPort(),
        		redirectLocation.getSwitchDPID(), redirectLocation.getPort(), U64.of(0));
        Route routeOut = routingService.getRoute(redirectLocation.getSwitchDPID(), redirectLocation.getPort(),
        		srcLocation.getSwitchDPID(), srcLocation.getPort(), U64.of(0));
        
        IOFSwitch inSW = switchService.getSwitch(srcLocation.getSwitchDPID());
       
        //get the path from source to redirected location
        if (srcDevice.getAttachmentPoints().length < 1 || 
        		redirectDevice.getAttachmentPoints().length < 1){
        	log.error("[FRESCO] can not install redirect flow rules due to missing host location info");
         	return;
        }
		
        // proxy arp
        if (isARP)
        {
	        IPacket arpReply = new Ethernet()
			.setSourceMACAddress(Ethernet.toByteArray(redirectDevice.getMACAddress().getLong()))
	    	.setDestinationMACAddress(Ethernet.toByteArray(srcDevice.getMACAddress().getLong()))
	    	.setEtherType(EthType.ARP)
	    	.setPayload(new ARP()
				.setHardwareType(ARP.HW_TYPE_ETHERNET)
				.setProtocolType(ARP.PROTO_TYPE_IP)
				.setOpCode(ARP.OP_REPLY)
				.setHardwareAddressLength((byte)6)
				.setProtocolAddressLength((byte)4)
				.setSenderHardwareAddress(redirectDevice.getMACAddress())
				.setSenderProtocolAddress(IPv4Address.of(ra.getDestinationIP()))
				.setTargetHardwareAddress(srcDevice.getMACAddress())
				.setTargetProtocolAddress(IPv4Address.of(ra.getSourceIP()))
				.setPayload(new Data(new byte[] {0x01})));
	        
	        //use packet-out to send out arp reply to the request
			OFPacketOut.Builder pob = inSW.getOFFactory().buildPacketOut();
			byte[] data = arpReply.serialize();
	        pob.setData(data);
			
	        // Set actions
	        List<OFAction> actions = new ArrayList<OFAction>();
			actions.add(inSW.getOFFactory().actions().output(srcLocation.getPort(),Integer.MAX_VALUE));
			
			//inSW.write(pob.build());
			
	    	try {
				messageDamper.write(inSW, pob.build());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
			
			return;
        }
		if (isIgnorePort){
	        //no path between suspicious host and directed destination
	        if (routeIn == null || routeOut == null){
	        	log.error("[FRESCO] can not install redirect flow rules due to missing route info");
	        	return;
	        }
	        
	        //we install redirect rules in ingress switch
	        List<NodePortTuple> pathIn = routeIn.getPath();
	        List<NodePortTuple> pathOut = routeOut.getPath();

	        
	        for (int i = 0; i < pathIn.size(); i = i+2){
	        	DatapathId dpid = pathIn.get(i).getNodeId();
	        	//OFPort curPort = pathIn.get(i).getPortId();
	        	OFPort nextPort = pathIn.get(i+1).getPortId();
	        	
	        	IOFSwitch sw = switchService.getSwitch(dpid);
	        	
	        	if (sw == null){
	        		log.error("[FRESCO] can not install redirect flow rules due to failure of locating switch " + dpid);
	        	}
	        	
	        	//install flow rule to redirect traffic from source node to new destination node  
		        Match.Builder matchIn = sw.getOFFactory().buildMatch();
		        ArrayList<OFAction> actionsIn = new ArrayList<OFAction>();
		        
		        if (sw.getId() == inSW.getId()){
		        	matchIn.setExact(MatchField.ETH_TYPE, EthType.IPv4)
		        	.setExact(MatchField.IP_PROTO, IpProtocol.TCP)
		        	.setExact(MatchField.IPV4_SRC, IPv4Address.of(ra.getSourceIP()))
			            .setExact(MatchField.IPV4_DST, IPv4Address.of(ra.getDestinationIP()));
			          //  .setExact(MatchField.IN_PORT, curPort);
		        	
		        	actionsIn.add(inSW.getOFFactory().actions().setDlDst(redirectDevice.getMACAddress()));
		        	actionsIn.add(inSW.getOFFactory().actions().setNwDst(IPv4Address.of(ra.getRedirectedNWAddr())));
		        	actionsIn.add(inSW.getOFFactory().actions().output(nextPort, Integer.MAX_VALUE));
		        }
		        else{
		        	matchIn.setExact(MatchField.ETH_TYPE, EthType.IPv4)
		        	.setExact(MatchField.IP_PROTO, IpProtocol.TCP)
		        	.setExact(MatchField.IPV4_SRC, IPv4Address.of(ra.getSourceIP()))
		            .setExact(MatchField.IPV4_DST, IPv4Address.of(ra.getRedirectedNWAddr()));
		           // .setExact(MatchField.IN_PORT, curPort);
	        	
		        	actionsIn.add(inSW.getOFFactory().actions().output(nextPort, Integer.MAX_VALUE));
		        }
		      
		     	OFFlowMod inFlowMod = createFRESCOFlowMod(inSW, matchIn.build(), actionsIn, SEC_PRIORITY_0);
		     	
		     	try {
	    				messageDamper.write(sw, inFlowMod);
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    		}
	        }
	        //install reverse route
	        for (int i = 0; i < pathOut.size(); i = i+2){
	        	DatapathId dpid = pathIn.get(i).getNodeId();
	          //	OFPort curPort = pathIn.get(i+1).getPortId();
	        	OFPort nextPort = pathIn.get(i).getPortId();
	      
	        	
	        	IOFSwitch sw = switchService.getSwitch(dpid);
	        	
	        	if (sw == null){
	        		log.error("[FRESCO] can not install redirect flow rules due to failure of locating switch " + dpid);
	        	}
	        	
	        	//install flow rule to redirect traffic from source node to new destination node  
		        Match.Builder matchOut = sw.getOFFactory().buildMatch();
		        ArrayList<OFAction> actionsOut = new ArrayList<OFAction>();
		        
		        if (sw.getId() == inSW.getId()){
		        		matchOut.setExact(MatchField.ETH_TYPE, EthType.IPv4)
		        		.setExact(MatchField.IP_PROTO, IpProtocol.TCP)
		        	.setExact(MatchField.IPV4_SRC, IPv4Address.of(ra.getRedirectedNWAddr()))
			            .setExact(MatchField.IPV4_DST, IPv4Address.of(ra.getSourceIP()));
			          //  .setExact(MatchField.IN_PORT, curPort);
		        	
		        	actionsOut.add(inSW.getOFFactory().actions().setDlSrc(dstDevice.getMACAddress()));
		        	actionsOut.add(inSW.getOFFactory().actions().setNwSrc(IPv4Address.of(ra.getDestinationIP())));
		        	actionsOut.add(inSW.getOFFactory().actions().output(nextPort, Integer.MAX_VALUE));
		        }
		        else{
		        	matchOut.setExact(MatchField.ETH_TYPE, EthType.IPv4)
		        	.setExact(MatchField.IP_PROTO, IpProtocol.TCP)
		        	.setExact(MatchField.IPV4_SRC, IPv4Address.of(ra.getRedirectedNWAddr()))
		            .setExact(MatchField.IPV4_DST, IPv4Address.of(ra.getSourceIP()));
		          //  .setExact(MatchField.IN_PORT, curPort);
	        	
		        	actionsOut.add(inSW.getOFFactory().actions().output(nextPort, Integer.MAX_VALUE));
		        }
		      
		     	OFFlowMod OutFlowMod = createFRESCOFlowMod(inSW, matchOut.build(), actionsOut, SEC_PRIORITY_0);
		     	
		     	try {
	    				messageDamper.write(sw, OutFlowMod);
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    		}
	        }
	        
	        /*
	        //install flow rule to redirect traffic from source node to new destination node  
	        Match.Builder match1 = inSW.getOFFactory().buildMatch();
	        match1.setExact(MatchField.IPV4_SRC, IPv4Address.of(ra.getSourceIP()))
            .setExact(MatchField.IPV4_DST, IPv4Address.of(ra.getDestinationIP()))
            .setExact(MatchField.IN_PORT, srcLocation.getPort());
	        
            ArrayList<OFAction> actions1 = new ArrayList<OFAction>();
            actions1.add(inSW.getOFFactory().actions().setDlDst(redirectDevice.getMACAddress()));
     	   	actions1.add(inSW.getOFFactory().actions().setNwDst(IPv4Address.of(ra.getRedirectedNWAddr())));
     	   	actions1.add(inSW.getOFFactory().actions().output(pathIn.get(0).getPortId(), Integer.MAX_VALUE));
            //path.get(1) to retrieve the next hop NodePort
           
     	  
            //install flow rules to redirect the traffic from destination node back to source node
	        Match.Builder match2 = inSW.getOFFactory().buildMatch();
	        match2.setExact(MatchField.IPV4_SRC, IPv4Address.of(ra.getRedirectedNWAddr()))
            .setExact(MatchField.IPV4_DST, IPv4Address.of(ra.getSourceIP()))
            .setExact(MatchField.IN_PORT, pathIn.get(0).getPortId());
	        
            ArrayList<OFAction> actions2 = new ArrayList<OFAction>();
            actions2.add(inSW.getOFFactory().actions().setDlSrc(dstDevice.getMACAddress()));
     	   	actions2.add(inSW.getOFFactory().actions().setNwSrc(IPv4Address.of(ra.getDestinationIP())));
     	   	actions2.add(inSW.getOFFactory().actions().output(srcLocation.getPort(), Integer.MAX_VALUE));
         
     	   	OFFlowMod inFlowMod = createFRESCOFlowMod(inSW, match1.build(), actions1, SEC_PRIORITY_0);
     	   	OFFlowMod outFlowMod = createFRESCOFlowMod(inSW, match2.build(), actions2, SEC_PRIORITY_0);
     	   	
     	   	redirectFMs.add(inFlowMod);
     	    redirectFMs.add(outFlowMod);
     	   	
     	      //enforce redirect flow rules
            for (OFFlowMod fm : redirectFMs){
                try {
    				messageDamper.write(inSW, fm);
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            } 
            */
		}
		else{
			//TODO: redirect directive with consideration of port info 
		}
	}
	
	//We use Packet-Out messages to mirror packet to a specific switch port
	private void enforceMirrorAction(FPContext cntx, MirrorAction ma){
		//check if the mirrored switch and port are still active
		IOFSwitch sw = switchService.getSwitch(DatapathId.of(ma.getDPID()));
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx.getFlowContext(),
					   IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		
		if (sw == null){
			log.error("[FRESCO] Cannot mirrow packet since the destination switch is offline");
			return;
		}
		
		if (sw.getPort(OFPort.of(ma.getPortID())) == null){
			log.error("[FRESCO] Cannot mirrow packet since the destination port is closed");
			return;
		}	
		
		//use packet-out to send out packet to a specific switch port
		OFPacketOut.Builder pob = sw.getOFFactory().buildPacketOut();

		ArrayList<OFAction> actions = new ArrayList<OFAction>();
 	   	actions.add(sw.getOFFactory().actions().output(OFPort.of(ma.getPortID()),Integer.MAX_VALUE));
	
 	   byte[] packetData = eth.serialize();
       pob.setData(packetData);
 	   	
		sw.write(pob.build());
	}
	
	
	//Install flow rules quarantine directive
	//function: drop traffic from quarantined hosts off the network, redirect DNS traffic to notifier
	//		
	//We hold some assumptions for redirection directive:
	//1. there is a route between the quarantined hosts to notifier 
	private void enforceQuarantineAction(QuarantineAction qa){
		//quarantine flow rule lists to installed in data plane
		ArrayList<OFFlowMod> quarantineFMs = new ArrayList<>();
		
		boolean hasNotifier = qa.hasNotifeir();
		
		IDevice notifier = null;
		SwitchPort notifierLocation = null;
		
		IDevice quarantinedDevice = getDeviceFromIP(qa.getQuarantinedIP());
		if (quarantinedDevice == null){
			log.error("[FRESCO] Cannot quarantine packet due to fail to find the quarantined device " 
						+ IPv4Address.of(qa.getQuarantinedIP()));
			return;
		}
		
		if (getLocationFromDevice(quarantinedDevice) == null){
			log.error("[FRESCO] Cannot quarantine packet due to fail to find the location of quarantined device " 
						+ IPv4Address.of(qa.getQuarantinedIP()));
			return;
		}
		
		SwitchPort quarantinedLocation = getLocationFromDevice(quarantinedDevice);		
		
		IOFSwitch inSW = switchService.getSwitch(quarantinedLocation.getSwitchDPID());
		
		//if the attached switch is offline
		if (inSW == null){
			log.error("[FRESCO] Cannot quarantine packet since the ingress switch is offline");
			return;
		}
		
		//quanrainted flow rules (FlowMod)
        OFFlowMod blockFM;
        //for http/dns traffic we need handle both inbound and outbound redirection traffic
        OFFlowMod dnsTCPFMin;
        OFFlowMod dnsTCPFMout;
        OFFlowMod dnsUDPFMin;
        OFFlowMod dnsUDPFMout;
        OFFlowMod httpFMin;
        OFFlowMod httpFMout;
        
        //quarantin match fields
        Match.Builder matchIn = inSW.getOFFactory().buildMatch();
        Match.Builder matchOut = inSW.getOFFactory().buildMatch();
        Match.Builder dnsTCPMatchIn = inSW.getOFFactory().buildMatch();
        Match.Builder dnsTCPMatchOut = inSW.getOFFactory().buildMatch();
        Match.Builder dnsUDPMatchIn = inSW.getOFFactory().buildMatch();
        Match.Builder dnsUDPMatchOut = inSW.getOFFactory().buildMatch();
    	Match.Builder httpMatchIn = inSW.getOFFactory().buildMatch();
        Match.Builder httpMatchOut = inSW.getOFFactory().buildMatch();
    	
        //quanratine actions
    	List<OFAction> blockActions = new ArrayList<OFAction>();
    	List<OFAction> inRedirectActions = new ArrayList<OFAction>();
    	List<OFAction> outRedirectActions = new ArrayList<OFAction>();
		
		//match flows from the quarantined host
        matchIn.setExact(MatchField.ETH_TYPE, EthType.IPv4)
        .setExact(MatchField.IPV4_SRC, IPv4Address.of(qa.getQuarantinedIP()));        
        matchOut.setExact(MatchField.ETH_TYPE, EthType.IPv4)
        .setExact(MatchField.IPV4_DST, IPv4Address.of(qa.getQuarantinedIP()));
      
		blockActions = new ArrayList<OFAction>(); //drop action
		
		//add block action
		blockFM = createFRESCOFlowMod(inSW, matchIn.build(), blockActions, SEC_PRIORITY_0);
		quarantineFMs.add(blockFM);
		
        if (hasNotifier){
        	//retrieve notifier device
			notifier = getDeviceFromIP(qa.getNotifierNWAddr());	
        }
        
        //if we can locate notifier, we redirect DNS/HTTP traffic the notifier 
        if (notifier != null){
        	notifierLocation = getLocationFromDevice(notifier);
        	
        	dnsTCPMatchIn = matchIn;
        	dnsUDPMatchIn = matchIn;
        	httpMatchIn = matchIn;
        	
        	dnsTCPMatchOut = matchOut;
        	dnsUDPMatchOut = matchOut;
        	httpMatchOut = matchOut;
        	
        	dnsTCPMatchIn.setExact(MatchField.TCP_DST, TransportPort.of(53));
        	dnsUDPMatchIn.setExact(MatchField.UDP_DST, TransportPort.of(53));
        	httpMatchIn.setExact(MatchField.TCP_DST, TransportPort.of(80));
        	
        	dnsTCPMatchOut.setExact(MatchField.TCP_SRC, TransportPort.of(53));
        	dnsUDPMatchOut.setExact(MatchField.UDP_SRC, TransportPort.of(53));
        	httpMatchOut.setExact(MatchField.TCP_SRC, TransportPort.of(80));
        	
        	Route routeIn = routingService.getRoute(quarantinedLocation.getSwitchDPID(), 
 	        		notifierLocation.getSwitchDPID(), U64.of(0));
 	      
 	        //no path between suspicious host and directed destination
 	        if (routeIn == null){
 	        	log.error("[FRESCO] can not install redirect flow rules due to missing route info");
 	        	return;
 	        }
 	        
 	       //we install redirect rules in ingress switch
 	       List<NodePortTuple> pathIn = routeIn.getPath();
 	       //List<NodePortTuple> pathOut = routeOut.getPath();
 	       
 	       inRedirectActions.add(inSW.getOFFactory().actions().setNwDst(IPv4Address.of(qa.getNotifierNWAddr())));
 	       inRedirectActions.add(inSW.getOFFactory().actions().output(pathIn.get(0).getPortId(), Integer.MAX_VALUE));
           //path.get(0) to retrieve the next hop NodePort
  	   	   outRedirectActions.add(inSW.getOFFactory().actions().setNwSrc(IPv4Address.of(qa.getQuarantinedIP())));
  	   	   outRedirectActions.add(inSW.getOFFactory().actions().output(quarantinedLocation.getPort(), Integer.MAX_VALUE));
  	   	   
  	   	   //install flow rules to redirect DNS/HTTP traffic to notifier
  	   	   //we use level 1 priority to override default drop action
  	   	   dnsTCPFMin = createFRESCOFlowMod(inSW, dnsTCPMatchIn.build(), inRedirectActions, SEC_PRIORITY_1);
  	   	   dnsTCPFMout = createFRESCOFlowMod(inSW, dnsTCPMatchOut.build(), outRedirectActions, SEC_PRIORITY_1);
  	   	   dnsUDPFMin = createFRESCOFlowMod(inSW, dnsUDPMatchIn.build(), inRedirectActions, SEC_PRIORITY_1);
	   	   dnsUDPFMout = createFRESCOFlowMod(inSW, dnsUDPMatchOut.build(), outRedirectActions, SEC_PRIORITY_1);
	   	   httpFMin = createFRESCOFlowMod(inSW, httpMatchIn.build(), inRedirectActions, SEC_PRIORITY_1);
	   	   httpFMout = createFRESCOFlowMod(inSW, httpMatchOut.build(), outRedirectActions, SEC_PRIORITY_1);
	   	   
	   	   quarantineFMs.add(dnsTCPFMin);
	   	   quarantineFMs.add(dnsTCPFMin);
	   	   quarantineFMs.add(dnsTCPFMin);
	   	   quarantineFMs.add(dnsTCPFMin);
	   	   quarantineFMs.add(dnsTCPFMin);
	   	   quarantineFMs.add(dnsTCPFMin);	
  	   	   
		}
        else{
        	log.error("[FRESCO] Cannot locate notifier host, block DNS/HTTP traffic");
        }
        
        //enforce quarantine flow rules
        for (OFFlowMod fm : quarantineFMs){
            try {
				messageDamper.write(inSW, fm);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		
	}
	

	void enforceSecurityAcitions(IOFSwitch sw, FPContext cntx, SecurityAction sa){
		switch(sa.getType()){
		case REDIRECT:
			RedirectAction ra = (RedirectAction)sa;
			enforceRedirectAction(ra, library.isARP(cntx), ra.isIgnorePort());
			break;
		case MIRROR:
			MirrorAction ma = (MirrorAction)sa;
			enforceMirrorAction(cntx, ma);
			break;
		case QUARANTINE:
			QuarantineAction qa = (QuarantineAction)sa;
			//proxy arp
			if (library.isARP(cntx)){
			}
			enforceQuarantineAction(qa);
			break;
		case BLOCK:
			BlockAction ba = (BlockAction)sa;
			enforceBlockAction(ba);
			break;
		}
			
	}
		
	//Parse FRESCO scripts and load corresponding modules at runtime
	public void parseScripts()
	{
		File script_folder = new File(FRESCO_SCRIPTS_LOCATION);
		File[] files = script_folder.listFiles();
		
		if ( files == null || files.length == 0){
			log.error("[FRESCO] No FRESCO script found!");
			return;
		}
		
		for (int i = 0;  i < files.length; i ++){
			
			if (files[i].isFile() && files[i].getName().endsWith(".fre")){
				parseScript(files[i]);
			}
		}
	}
	
	private void parseScript(File script_file){
		ClassLoader classLoader = FP_FloodlightRTE.class.getClassLoader();
		
		try {
			byte[] script_data = Files.readAllBytes(script_file.toPath());
			
			ObjectMapper om  = new ObjectMapper();
			
			FP_script script = om.readValue(script_data, FP_script.class);
			
			log.info("Loading Fresco Scripts: " + script.toString());
			
			String appName = script.getName();
			Module[] modules = script.getModules();
			
			FPM_Graph app = new FPM_Graph(appName, library);
			
			for (Module m : modules){
				String moduleName = FRESCO_MODULE_PACKAGE + m.getType();
				Class<?> moduleClass = classLoader.loadClass(moduleName);
				log.info("Loading " + moduleClass.getName() +
					" Module in the Floodlight controller.");
				
				//TODO: dynamic create module instances and add them to app graph.	
				String id = m.getID();
				String type = m.getType();
				String[] pars = m.getParameters();
				String[] inputs = m.getInputs();
				String   event = m.getEvent();	
				
				//debug
			//	for (String s : pars)
			//		log.info("The parameters for " + type + "(" + id + ")" + " is " + s);
				
				Constructor<?> c = moduleClass.getDeclaredConstructor(
						new Class[] {FP_LibFloodlight.class, String.class, String.class, 
											String.class, String[].class ,String[].class});
				c.setAccessible(true);
				app.addModule((AFP_Module) c.newInstance(new Object[]{library, id, type, event, pars,inputs}));
			
				//register external event for the app
				fpeManager.register(event, app);		
			} 
				
		} 
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		 Collection<Class<? extends IFloodlightService>> l =
                 new ArrayList<Class<? extends IFloodlightService>>();
         l.add(IFloodlightProviderService.class);
         return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException 
	{
		
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		
        deviceService = context.getServiceImpl(IDeviceService.class);
        routingService = context.getServiceImpl(IRoutingService.class);
        switchService = context.getServiceImpl(IOFSwitchService.class);
		linkService = context.getServiceImpl(ILinkDiscoveryService.class);
		
        messageDamper =  new OFMessageDamper(OFMESSAGE_DAMPER_CAPACITY,
				EnumSet.of(OFType.FLOW_MOD),
				OFMESSAGE_DAMPER_TIMEOUT);
        
		library = new FP_LibFloodlight( LoggerFactory.getLogger( getClass() ));
		
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// TODO : packet listeners.
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		
		parseScripts();
	}

	
	@Override
	public void linkDiscoveryUpdate(LDUpdate update) {
		library.processSwitchLinkUpdate(linkService.getLinks().keySet());
	}
	
	

	@Override
	public void linkDiscoveryUpdate(List<LDUpdate> updateList) {
		library.processSwitchLinkUpdate(linkService.getLinks().keySet());
	}
	
}
