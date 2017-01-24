package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;

/* Module: FM_flow_sourceMAC
* select source MAC address of incoming flow
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/

public class FM_flow_sourceMAC extends AFP_Module {

	//descriptions about module
	private final static int NUM_INPUT = 0;
	private final static int NUM_OUTPUT = 1;
	
	public FM_flow_sourceMAC(FP_LibFloodlight lib, String id, String type, String event, String[] pars, String[] inputs) {
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);
	}

    @Override
    public ModuleParameters run(ModuleParameters input, FPContext cntx) {
    	ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
    	
    	long result;
    	
        result = library.getSrcMac(cntx);

    	outputPars.setParameter(1, result);
           
       return outputPars;
    }
  

}
