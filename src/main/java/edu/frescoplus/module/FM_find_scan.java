package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.core.TCPSession;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;

import java.util.HashMap;
import java.util.Objects;

import net.floodlightcontroller.packet.IPv4;

/* Module: FM_find_scan
* detect scanning attack in the network
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/

public class FM_find_scan extends AFP_Module{
	
    // descriptions about module
	private final static int NUM_INPUT =  0;
	private final static int NUM_OUTPUT = 3;
	
	 private int threshold;
	 
	 //count the TCP connection failure for source IP address
	 private HashMap<Integer, Integer> ipToTCPFailCountMap;
	
	public FM_find_scan(FP_LibFloodlight lib, String id, String type, String event, String[] pars, String[] inputs) {
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);	
		
		this.threshold = library.parseInteger(pars[0]);
		ipToTCPFailCountMap = new HashMap<>();
	}
	  
    @Override
    public ModuleParameters run(ModuleParameters input, FPContext cntx) 
    {
    	ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
    	
    	int srcIP = 0;
    	int dstIP = 0;
    	//check if the number of TCP failures exceeding the threshold
    	int count;
    	
    	boolean result=false;
    	
    	TCPSession session = library.getCurrentTCPsession(cntx);
    	
    	srcIP = session.getSrcIP();
    	dstIP = session.getDstIP();
    	
    	int key = Objects.hash(srcIP, dstIP);
	 
    	if (ipToTCPFailCountMap.containsKey(key)){
    		count = ipToTCPFailCountMap.get(key) + 1;
    	}
    	else{
    		count = 1;
    	}
    	
    	ipToTCPFailCountMap.put(key, count);
    	
    	if (count >= threshold){
    		result = true;
    	}

        outputPars.setParameter(1, result);
    	outputPars.setParameter(2, srcIP);
    	outputPars.setParameter(3, dstIP);

        return outputPars;
    }
}