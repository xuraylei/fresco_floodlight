package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;

/* Module: FM_flow_tcpip
* retrieve source/destination port numbers and IP addresses of the incoming flow
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/

public class FM_flow_tcpip extends AFP_Module {

	//descriptions about module
	private final static int NUM_INPUT = 0;
	private final static int NUM_OUTPUT = 4;
	
	public FM_flow_tcpip(FP_LibFloodlight lib, String id, String type, String event, String[] pars, String[] inputs) {
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);
	}

    @Override
    public ModuleParameters run(ModuleParameters input, FPContext cntx) {
    	ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
    	
    	int srcIP;
    	int dstIP;
    	int srcPort;
    	int dstPort;
    	
    	srcIP = library.getSrcIP(cntx);
    	dstIP = library.getDstIP(cntx);
    	srcPort = library.getSrcPort(cntx);
    	dstPort = library.getDstPort(cntx);

    	outputPars.setParameter(1, srcIP);
    	outputPars.setParameter(2, dstIP);
    	outputPars.setParameter(3, srcPort);
    	outputPars.setParameter(4, dstPort);
            
        return outputPars;
    }
  

}
