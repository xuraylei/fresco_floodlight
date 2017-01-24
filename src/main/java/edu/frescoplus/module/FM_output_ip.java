package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;

/* Module: FM_output_ip
* output a int value according to predefined IP address
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/

public class FM_output_ip extends AFP_Module {

	//descriptions about module
	private final static int NUM_INPUT = 0;
	private final static int NUM_OUTPUT = 1;
	
	private int outputPar;
	
	public FM_output_ip(FP_LibFloodlight lib, String id, String type,
			String event, String[] pars, String[] inputs) {
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);
		
		outputPar = library.parseIPv4(pars[0]);
	}

	@Override
	public ModuleParameters run(ModuleParameters input, FPContext cntx) {
		ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
		
		outputPars.setParameter(1, outputPar);
		
		return outputPars;
	}


}
