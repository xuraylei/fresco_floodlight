package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;

/* Module: FM_match_ip
* match a specific port number
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/

public class FM_match_port extends AFP_Module {

	// descriptions about module
	private final static int NUM_INPUT = 1;
	private final static int NUM_OUTPUT = 1;
	

	private int matchPort;
	
	

	public FM_match_port(FP_LibFloodlight lib, String id, String type, String event, String[] pars,
			String[] inputs) {
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);

		this.matchPort = Integer.parseInt(pars[0]);

	}

	@Override
	 public ModuleParameters run(ModuleParameters input, FPContext flCntx) {
		ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
		
		int port;;
	    boolean result;

	    port = input.getParameter(1).getInteger();


		result = (matchPort == port);

		outputPars.setParameter(1, result);

		return outputPars;
	}
}
