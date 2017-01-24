package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;

/* Module: FM_forward_flow
* forward action on current pending flow
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/

public class FM_forward_flow extends AFP_Module {

	// descriptions about module
	private final static int NUM_INPUT = 1;
	private final static int NUM_OUTPUT = 0;

	public FM_forward_flow(FP_LibFloodlight lib, String id, String type, String event,
			String[] pars, String[] inputs) {
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);
	}

	 public ModuleParameters run(ModuleParameters input, FPContext cntx) {
		ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
		 
		boolean doAction;
		
	    doAction = input.getParameter(1).getBoolean();
		
		if (doAction) {
			library.FORWARD(cntx);
		} else {
			library.DROP(cntx);
		}
		
		return outputPars;
	}
}
