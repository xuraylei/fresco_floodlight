package edu.frescoplus.securitykernel;

import java.util.ArrayList;
import java.util.List;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionSetDlDst;
import org.projectfloodlight.openflow.protocol.action.OFActionSetDlSrc;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwDst;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwSrc;
import org.projectfloodlight.openflow.protocol.action.OFActionSetTpDst;
import org.projectfloodlight.openflow.protocol.action.OFActionSetTpSrc;
import org.projectfloodlight.openflow.protocol.action.OFActionSetVlanVid;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;

enum FlowRuleAction{
	FORWARD, DROP
}

class Alias{
	public final int WILDCARDVALUE = -1;
	
	private Match match;
	private OFPort outPort;		//output Port Number
	
	
	public Alias(Match m){
		this.match = m;
		this.outPort = OFPort.of(0);
	}
	
	public Alias(Match m, OFPort p){
		this.match = m;
		this.outPort = p;
	}
	
	public void setPort(OFPort p){
		this.outPort = p;
	}
	
	public void setMatch(Match m){
		this.match = m;
	}
	
	public Match getMatch(){
		return this.match;
	}
}

public class FlowAlias {
	
	private OFFlowMod ofm;
	private ArrayList<Alias> aliasList ;//alias Reduction 
	private FlowRuleAction fra;
	
	private Alias lastConflictAlias;
	
	//construct FlowAlias from FlowMod
	public FlowAlias(OFFlowMod flowmod){
		this.ofm = flowmod;
		this.aliasList = new ArrayList<>();
		this.fra = FlowRuleAction.DROP;
		this.lastConflictAlias = null;
		
		Match match = flowmod.getMatch();
		Match.Builder mb = match.createBuilder();
		List<OFAction> actions = flowmod.getActions();
		
		//initialize alias with match filed
		Alias alias = new Alias(match);
		
		//add alias to list
		this.aliasList.add(alias);
		
		//update alias based on actions
		for (OFAction act : actions){		
			switch (act.getType()){
				case SET_DL_SRC:
					OFActionSetDlSrc setDLSRC = (OFActionSetDlSrc)act; 
					mb.setExact(MatchField.ETH_SRC, setDLSRC.getDlAddr());
					break;
				case SET_DL_DST:
					OFActionSetDlDst setDLDST = (OFActionSetDlDst)act;
					mb.setExact(MatchField.ETH_DST, setDLDST.getDlAddr());
					break;
				case SET_NW_SRC:
					OFActionSetNwSrc setNWSRC = (OFActionSetNwSrc)act;
					mb.setExact(MatchField.IPV4_SRC,setNWSRC.getNwAddr());
					break;
				case SET_NW_DST:
					OFActionSetNwDst setNWDST = (OFActionSetNwDst)act;
					mb.setExact(MatchField.IPV4_DST,setNWDST.getNwAddr());
					break;
				case SET_TP_SRC:
					OFActionSetTpSrc setTPSRC = (OFActionSetTpSrc)act;
					mb.setExact(MatchField.TCP_SRC, setTPSRC.getTpPort());
					break;
				case SET_TP_DST:
					OFActionSetTpDst setTPDST = (OFActionSetTpDst)act;
					mb.setExact(MatchField.TCP_SRC, setTPDST.getTpPort());
					break;
					
				case ENQUEUE:
				case OUTPUT:
					OFActionOutput output = (OFActionOutput)act;
					alias.setPort(output.getPort());
					this.fra = FlowRuleAction.FORWARD;
				break;
			
			}
		}
		//save modified action
		alias.setMatch(mb.build());
		
		//add updated alias to the list
		this.aliasList.add(alias);
	}
		
	public void setConflict(Alias c){
		this.lastConflictAlias = c;
	}
	
	public ArrayList<Alias>  getAliasList(){
		return this.aliasList;
	}
	
	public FlowRuleAction getFlowRuleAction(){
		return this.fra;
	}
	
	public int getPriority(){
		return this.ofm.getPriority();
	}
	
	public long getKey(){
		return this.ofm.getCookie().getValue();
	}
}
