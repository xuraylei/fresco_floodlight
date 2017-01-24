package edu.frescoplus.module;

import java.util.HashMap;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;


/* Module: FM_count_ip
* count the number of a specific IP input
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/


public class FM_count_ip extends AFP_Module {

	//descriptions about module
	private final static int NUM_INPUT =  1;
	private final static int NUM_OUTPUT = 1;
	
	private HashMap<Integer, Integer> ipCounter;

	public FM_count_ip(FP_LibFloodlight lib, String id, String type,
			String event, String[] pars, String[] inputs) {
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);
		
		ipCounter = new HashMap<>();
	}

	@Override
	public ModuleParameters run(ModuleParameters input, FPContext cntx) {
		ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
		
		int ip;
		
		ip = input.getParameter(1).getInteger();

		int count;
		
		if (ipCounter.get(ip) == null){
			count = 1;
		}
		else{
			count = ipCounter.get(ip) + 1;
		}
		
		ipCounter.put(ip, count);
		
		outputPars.setParameter(1, count);
		
		return outputPars;
	}


}
