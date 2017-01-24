package edu.frescoplus.securitykernel;

//test case for security kernel

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwSrc;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;

public class testSecurityKernel implements IFloodlightModule, IOFSwitchListener {

	protected IOFSwitchService switchService;

	// test security kernel for conflict rules
	@Override
	public void switchAdded(DatapathId switchId) {
		IOFSwitch sw = switchService.getSwitch(switchId);

		if (sw == null) {
			return;
		}
		
		// install conflicting forwarding rules and security rules for testing security kernel
	

		// forward rules
		Match.Builder matchFwd = sw.getOFFactory().buildMatch();
		matchFwd.setExact(MatchField.IPV4_SRC, IPv4Address.of("10.0.0.3"));
		matchFwd.setExact(MatchField.ETH_TYPE, EthType.IPv4);

		OFFlowAdd.Builder forwardflow = sw.getOFFactory().buildFlowAdd();
		ArrayList<OFAction> forwardfActionList = new ArrayList<OFAction>();

		OFActionOutput.Builder forwardaction = sw.getOFFactory().actions()
				.buildOutput();
		forwardaction.setPort(OFPort.of(2));
		OFActionSetNwSrc.Builder secaction = sw.getOFFactory().actions()
				.buildSetNwSrc();
		secaction.setNwAddr(IPv4Address.of("10.0.0.1"));

		forwardfActionList.add(forwardaction.build());
		forwardfActionList.add(secaction.build());

		forwardflow.setBufferId(OFBufferId.NO_BUFFER);
		forwardflow.setHardTimeout(0);
		forwardflow.setIdleTimeout(0);
		forwardflow.setOutPort(OFPort.CONTROLLER);
		forwardflow.setActions(forwardfActionList);
		forwardflow.setMatch(matchFwd.build());
		forwardflow.setPriority(200);

		// security rules
		OFFlowAdd.Builder securityFlow = sw.getOFFactory().buildFlowAdd();
		ArrayList<OFAction> securityActionList = new ArrayList<OFAction>();
		OFActionOutput.Builder securitydaction = sw.getOFFactory().actions()
				.buildOutput();

		Match.Builder matchSec = sw.getOFFactory().buildMatch();
		matchSec.setExact(MatchField.IPV4_SRC, IPv4Address.of("10.0.0.1"));
		matchSec.setExact(MatchField.ETH_TYPE, EthType.IPv4);

		securityFlow.setBufferId(OFBufferId.NO_BUFFER);
		securityFlow.setHardTimeout(0);
		securityFlow.setIdleTimeout(0);
		securityFlow.setOutPort(OFPort.CONTROLLER);
		securityFlow.setActions(securityActionList);
		securityFlow.setMatch(matchSec.build());
		securityFlow.setPriority(300);

		sw.write(securityFlow.build());
		sw.write(forwardflow.build());

	}

	@Override
	public void switchRemoved(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchActivated(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchPortChanged(DatapathId switchId, OFPortDesc port,
			PortChangeType type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchChanged(DatapathId switchId) {
		// TODO Auto-generated method stub

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
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IStaticFlowEntryPusherService.class);
		l.add(IOFSwitchService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {

		switchService = context.getServiceImpl(IOFSwitchService.class);

	}

	@Override
	public void startUp(FloodlightModuleContext context)
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		switchService.addOFSwitchListener(this);

	}

}
