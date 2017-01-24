package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;


/*Module: FM_arith_smaller
* if the input is smaller than predefined integer, output true. 
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/


public class FM_arith_smaller extends AFP_Module {

	//descriptions about module
	private final static int NUM_INPUT = 1;
	private final static int NUM_OUTPUT = 1;
	

	private int threshold;

	public FM_arith_smaller(FP_LibFloodlight lib, String id, String type,
			String event, String[] pars, String[] inputs) {
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);
		
		threshold = library.parseInteger(pars[0]);

	}

	@Override
	public ModuleParameters run(ModuleParameters input, FPContext cntx) {
		ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
		
		int number;
		boolean result;
		
		number = input.getParameter(1).getInteger();
		
		result = (number < this.threshold);
		
		outputPars.setParameter(1, result);
		
		return outputPars;
	}


}
