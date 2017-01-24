package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;

/*Module: FM_op_and
* output a boolean operation to conduct "AND" upon two input boolean
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/

public class FM_logic_and extends AFP_Module {

	//descriptions about module
	private final static int NUM_INPUT = 2;
	private final static int NUM_OUTPUT = 1;
	
	public FM_logic_and(FP_LibFloodlight lib, String id, String type,
			String event, String[] pars, String[] inputs) {
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);
		
	}

	@Override
	public ModuleParameters run(ModuleParameters input, FPContext cntx) {
		ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
		
		boolean input1 = false;
		boolean input2 = false;
		boolean result = false;
		
		input1 = input.getParameter(1).getBoolean();
		input2 = input.getParameter(2).getBoolean();
     	
		result = (input1 && input2);
		
		log.error("input1:" + input1 + ", input2: " + input2 + ", result: " +result);
	
		outputPars.setParameter(1, result);
		
		return outputPars;
	}


}
