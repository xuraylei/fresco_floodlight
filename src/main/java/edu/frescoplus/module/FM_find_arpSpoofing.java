package edu.frescoplus.module;

import java.util.HashMap;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;

/* Module: FM_find_arpSpoofing
* detect scanning attack in the network
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/	

public class FM_find_arpSpoofing extends AFP_Module{
	
	// descriptions about module
	private final static int NUM_INPUT =  0;
	private final static int NUM_OUTPUT = 1;
	
	
	private HashMap<Integer, Long> mapIPv4ToMac;
	
	public FM_find_arpSpoofing(FP_LibFloodlight lib, String id, String type, String event, String[] pars, String[] inputs) {
		super(lib, id, type , event, pars, inputs, NUM_INPUT, NUM_OUTPUT);	
		
		mapIPv4ToMac = new HashMap<>();
	}
	
    @Override
    public ModuleParameters run(ModuleParameters input, FPContext cntx) {
    	ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
    	
    	int senderIP;
    	long senderMAC;
    	
    	boolean result=false;
    	
        if (library.isARP(cntx)){
        	senderIP = library.getARPSenderIP(cntx);
        	senderMAC = library.getARPSenderMAC(cntx);
        	
        	
        	if (mapIPv4ToMac.containsKey(senderIP)){
        		
        		if (mapIPv4ToMac.get(senderIP) != senderMAC){
        			result = true;
        		}
        	}
        	else{
        		mapIPv4ToMac.put(senderIP, senderMAC);
        	}
        }
        
        outputPars.setParameter(1, result);
        
        return outputPars;
    }
}

