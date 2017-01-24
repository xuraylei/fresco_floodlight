package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.core.TCPSession;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;

import java.util.HashMap;
import java.util.Objects;

import net.floodlightcontroller.packet.IPv4;

/* Module: FM_tcp_failure
* output tcp connection failure information
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/

public class FM_tcp_failure extends AFP_Module{
	
    // descriptions about module
	private final static int NUM_INPUT =  0;
	private final static int NUM_OUTPUT = 4;

	public FM_tcp_failure(FP_LibFloodlight lib, String id, String type, String event, String[] pars, String[] inputs) {
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);	
	}
	  
    @Override
    public ModuleParameters run(ModuleParameters input, FPContext cntx) 
    {
    	ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
    
    	int srcIP = 0;
    	int dstIP = 0;
    	int srcPort = 0;
    	int dstPort = 0;
    	
    	TCPSession session = library.getCurrentTCPsession(cntx);
    	
    	srcIP = session.getSrcIP();
    	dstIP = session.getDstIP();
    	   	
    	outputPars.setParameter(1, srcIP);
    	outputPars.setParameter(2, srcPort);
    	outputPars.setParameter(3, dstIP);
    	outputPars.setParameter(4, dstPort);
    
        return outputPars;
    }
}
