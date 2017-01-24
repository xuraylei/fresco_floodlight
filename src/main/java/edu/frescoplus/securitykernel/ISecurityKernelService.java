package edu.frescoplus.securitykernel;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.types.DatapathId;

import net.floodlightcontroller.core.module.IFloodlightService;

public interface ISecurityKernelService extends IFloodlightService {
	
	public  boolean checkFlowMod(DatapathId dpid, OFFlowMod ofm);
}
