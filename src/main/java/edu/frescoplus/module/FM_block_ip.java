package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;

/*Module: FM_block_ip
* block traffic from a host with specified IP
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/

public class FM_block_ip extends AFP_Module {
		
	//descriptions about module
	private final static int NUM_INPUT = 2;
	private final static int NUM_OUTPUT = 0;
	
	public FM_block_ip(FP_LibFloodlight lib, String id, String type, String event, String[] pars, String[] inputs) {
		
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);	
	}

	@Override
	public ModuleParameters run(ModuleParameters input,
			FPContext cntx) {

		ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT);
		
		boolean doBlock;
		int blockIP;
		
		doBlock = input.getParameter(1).getBoolean();
		blockIP = input.getParameter(2).getInteger();
		
		if (doBlock) {
			library.BLOCK(cntx, blockIP);
		}
		
		return outputPars;
	}

}
