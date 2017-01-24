package edu.frescoplus.securitykernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowRemoved;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.devicemanager.IDeviceService;

//Security Kernel for FRESCO
public class SecurityKernel implements IFloodlightModule, IOFMessageListener, IOFSwitchListener, ISecurityKernelService{

enum OFPRole{
	ADMIN_ROLE, SECURITY_ROLE, APP_ROLE;
}
		
enum MATCH_COMPARE_RESULT{
	SKIP, EQUAL, NONEQUAL
}
	
	private final String name = "Security Kernel";
	
	//constraints map 
	private HashMap<DatapathId, HashMap<Long, FlowAlias>> cMap = new HashMap<>();
	
	protected static final Logger log = LoggerFactory.getLogger(SecurityKernel.class);
	@Override
	public String getName() {
		return name;
	}

//TODO: support wildcard rules
private <F> MATCH_COMPARE_RESULT matchExact(F a, F b){
	if (a == null || b == null){
		return MATCH_COMPARE_RESULT.SKIP;
	}
	
	return (a.equals(b)) ? MATCH_COMPARE_RESULT.EQUAL : MATCH_COMPARE_RESULT.NONEQUAL;
}

//match common comparators (IP protocol, Ethernet type, VLAN id, Inport)
private MATCH_COMPARE_RESULT matchCommonComparators(Alias a, Alias b){
	Match matchA = a.getMatch();
	Match matchB = b.getMatch();
	
	MATCH_COMPARE_RESULT result = MATCH_COMPARE_RESULT.EQUAL;
	
	//match IP Protocol
	result = matchExact(matchA.get(MatchField.IP_PROTO),
						  matchB.get(MatchField.IP_PROTO));
	
	if (result == MATCH_COMPARE_RESULT.NONEQUAL){
		return result;
	}
	
	//match Ethernet type
	result = matchExact(matchA.get(MatchField.ETH_TYPE),
			  matchB.get(MatchField.ETH_TYPE));

	if (result == MATCH_COMPARE_RESULT.NONEQUAL){
		return result;
	}
	
	//match VLAN ID
	result = matchExact(matchA.get(MatchField.VLAN_VID),
			  matchB.get(MatchField.VLAN_VID));

	if (result == MATCH_COMPARE_RESULT.NONEQUAL){
		return result;
	}
	
	//match Incoming Port
	result = matchExact(matchA.get(MatchField.IN_PORT),
			  matchB.get(MatchField.IN_PORT));

	if (result == MATCH_COMPARE_RESULT.NONEQUAL){
		return result;
	}
	
	return result;
}

//match source comparators (source MAC, source IP, source Port num)
private MATCH_COMPARE_RESULT matchSrcComparators(Alias a, Alias b){
	Match matchA = a.getMatch();
	Match matchB = b.getMatch();
	
	MATCH_COMPARE_RESULT result = MATCH_COMPARE_RESULT.EQUAL;
	
	//match source MAC
	result = matchExact(matchA.get(MatchField.ETH_SRC),
			  matchB.get(MatchField.ETH_SRC));
	if (result == MATCH_COMPARE_RESULT.NONEQUAL){
		return result;
	}
	
	//match source IP
	result = matchExact(matchA.get(MatchField.IPV4_SRC),
			  matchB.get(MatchField.IPV4_SRC));
	if (result == MATCH_COMPARE_RESULT.NONEQUAL){
		return result;
	}
	
	//match source Port Number
	result = matchExact(matchA.get(MatchField.TCP_SRC),
			  matchB.get(MatchField.TCP_SRC));
	
	if (result == MATCH_COMPARE_RESULT.SKIP){
		result = matchExact(matchA.get(MatchField.UDP_SRC),
				  matchB.get(MatchField.UDP_SRC));
	}
	if (result == MATCH_COMPARE_RESULT.NONEQUAL){
		return result;
	}
	
	return result;
}

//match destionation comparators (dest MAC, dest IP, dest Port num)
private MATCH_COMPARE_RESULT matchDstComparators(Alias a, Alias b){
	Match matchA = a.getMatch();
	Match matchB = b.getMatch();
	
	MATCH_COMPARE_RESULT result = MATCH_COMPARE_RESULT.EQUAL;
	
	//match destination MAC
	result = matchExact(matchA.get(MatchField.ETH_DST),
			  matchB.get(MatchField.ETH_DST));
	if (result == MATCH_COMPARE_RESULT.NONEQUAL){
		return result;
	}
	
	//match destination IP
	result = matchExact(matchA.get(MatchField.IPV4_DST),
			  matchB.get(MatchField.IPV4_DST));
	if (result == MATCH_COMPARE_RESULT.NONEQUAL){
		return result;
	}
	
	//match destination Port Number
	result = matchExact(matchA.get(MatchField.TCP_DST),
			  matchB.get(MatchField.TCP_DST));
	
	if (result == MATCH_COMPARE_RESULT.SKIP){
		result = matchExact(matchA.get(MatchField.UDP_DST),
				  matchB.get(MatchField.UDP_DST));
	}
	if (result == MATCH_COMPARE_RESULT.NONEQUAL){
		return result;
	}
	
	return result;
}


///////////////////////////////////////////////////////////////////////////////////////
//Service Functions of Security Kernel

//internal conflict check
private FlowAlias haveConflict(DatapathId dpid, FlowAlias newFlowAlias){
	
	
	HashMap<Long, FlowAlias> flowAliases = cMap.get(dpid); 
	
	if (flowAliases == null){
		flowAliases = new HashMap<>();
	}
	
	for (FlowAlias fa : flowAliases.values()){//iterate over all flow aliases for the switch
		ArrayList<Alias> cRules = fa.getAliasList();
		boolean nextCAlias = false;
		
		//evaluate forward/drop action
		if (fa.getFlowRuleAction() == newFlowAlias.getFlowRuleAction()){
			break;
		}
		
		for (int i=0; i < cRules.size() && !nextCAlias; i++){//iterate over all constraint aliases
			ArrayList<Alias> fRules = newFlowAlias.getAliasList();
			boolean nextFAlias = false;
			Alias cRule = cRules.get(i);
			
			for (int j=0; j < fRules.size() && !nextFAlias; j++){//iterate over all ARR inside new FlowMod
				Alias fRule = fRules.get(j);
				MATCH_COMPARE_RESULT result;
			
				if (i == 0 && j == 0){//compare match and src between raw alias of flow alias
					//evaluate common comparators (IP protocol, Ethernet type, VLAN id, Inport)
					result = matchCommonComparators(cRule, fRule);
					if (result == MATCH_COMPARE_RESULT.NONEQUAL){
						nextCAlias = true;
						break;
					}
					
					//evaluate src comparators (IP protocol, Ethernet type, VLAN id, Inport)
					result = matchSrcComparators(cRule, fRule);
					if (result == MATCH_COMPARE_RESULT.NONEQUAL){
						nextCAlias = true;
						break;
					}						
				}
				
				//evaluate dst comparators (IP protocol, Ethernet type, VLAN id, Inport)
				result = matchDstComparators(cRule, fRule);
				if (result == MATCH_COMPARE_RESULT.NONEQUAL){
					nextFAlias = true;
					break;
				}			
				
				//found conflicts
				fa.setConflict(cRule);
				return fa;
				
			}//iterate over all ARR inside new FlowMod
		}//iterate over all constraint aliases
	}//iterate over all flow aliases for the switch
	
	return null;
	
}

//add or modify flow alias 
private void updateConstraints(DatapathId dpid, FlowAlias fa){
	HashMap<Long, FlowAlias> faList = cMap.get(dpid);
	
	
	if (faList == null){
		faList = new HashMap<Long, FlowAlias>();
	}
	
	//update Flow Alias list, TODO: if there is existing Flow Alias with same key?
	faList.put(fa.getKey(), fa);
	
	cMap.put(dpid, faList);
	
}

//remove flow alias
private void removeConstraints(DatapathId dpid, long key){
	HashMap<Long, FlowAlias> faList = cMap.get(dpid);
	
	if (faList != null){
		faList.remove(key);
	}
	
	cMap.put(dpid, faList);
}

//retrieve flow alias
private FlowAlias getFlowAlias(DatapathId dpid, long key){
	HashMap<Long, FlowAlias> faList = cMap.get(dpid);
	
	if (faList != null){
		return faList.get(key);
	}
	return null;
}

//clear flow alias list for a specific switch
private void clearFlowAliasList(DatapathId dpid){
	cMap.remove(dpid);
}


public  SecurityKernel getSecurityKernek(){
	return this;
}

//check the new FlowMod: 1. if it is conflict with existing constraints 2. update constraints if necessary
//return : true (conflict), false (non-conflict)

@Override
public boolean checkFlowMod(DatapathId dpid, OFFlowMod ofm){
	
	FlowAlias newFlowAlias = new FlowAlias(ofm);
	
//	boolean isAdd = false;
	
	switch(ofm.getCommand()){
	case ADD:
//		isAdd = true;
	case MODIFY:
	case MODIFY_STRICT:
		//detect conflicts, null (for no conflicts)
		FlowAlias conflictFlowAlias = haveConflict(dpid, newFlowAlias);
		
		if (conflictFlowAlias != null){// has conflict
			if (ofm.getPriority() < conflictFlowAlias.getPriority()){ //if new flow has lower priority				
				return true;//early return
			}
			else{//if new flow has higher priority
				//TODO: delete lower or equal priority rules?
				//if the new flow mod has higher priority, we can just allow it installed to the dataplane without delete the conflicted flow rules	
				//deleteFlow();
				removeConstraints(dpid, conflictFlowAlias.getKey());
			}
		}
		
		if (conflictFlowAlias == null){// no conflict
			//update constraint lists
			updateConstraints(dpid, newFlowAlias);
			return false;
		}
	break;

	case DELETE_STRICT:
	case DELETE:
		//find existing matched Flow Alias
		FlowAlias existingFlowAlias = getFlowAlias(dpid, newFlowAlias.getKey());
		
		if (existingFlowAlias != null){
			if (newFlowAlias.getPriority() > existingFlowAlias.getPriority()){ 
				//remove existing Flow Alias
				removeConstraints(dpid, existingFlowAlias.getKey());
			}
		}
		else{//non-existing flow alias for deleting rules.
			
		}
	}
	return false;
	
}

/////////////////////////////////////////////////////////////
//handler functions 

//handler of Flow_Remove
private Command handleFlowRemovedMessage(IOFSwitch sw, OFFlowRemoved ofr){
	HashMap<Long, FlowAlias> faList = cMap.get(sw.getId());
	
	if (faList != null){
		if (faList.remove(ofr.getCookie()) != null){
			log.debug("Clear Flow Alias corresponding to removed Flow Mod.");
			
		}else{//cannot find to-remove flow alias
			log.error("Cannot find Flow Alias corresponding to removed Flow Mod.");
		}
	}
	
	cMap.put(sw.getId(), faList);
	return Command.CONTINUE;
}

//handler of Switch_Join
private void handleSwitchJoinMessage(DatapathId dpid){
	clearFlowAliasList(dpid);
}

//handler of Switch_Leave
private void handleSwitchLeaveMessage(DatapathId dpid){
	clearFlowAliasList(dpid);
}
/////////////////////////////////////////////////////////////////////////////////////
	
	
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
	public void switchAdded(DatapathId switchId) {
		// TODO Auto-generated method stub
	}

	@Override
	public void switchRemoved(DatapathId switchId) {
		this.handleSwitchLeaveMessage(switchId);
	}

	@Override
	public void switchActivated(DatapathId switchId) {
		this.handleSwitchJoinMessage(switchId);

	}

	@Override
	public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchChanged(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg,
			FloodlightContext cntx) {
		
		switch (msg.getType()) {
		case FLOW_REMOVED:
			return this.handleFlowRemovedMessage(sw, (OFFlowRemoved) msg);	
		default:
			return Command.CONTINUE;
		}

	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(ISecurityKernelService.class);
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> m =
				new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
		//we are the implementation of Security Kernel Service
		m.put(ISecurityKernelService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		// TODO Auto-generated method stub
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// TODO Auto-generated method stub
	}
	

}
