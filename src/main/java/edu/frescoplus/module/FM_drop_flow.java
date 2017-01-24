package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;

/* Module: FM_drop_flow
* drop action on current pending flow
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/

public class FM_drop_flow extends AFP_Module{

	//descriptions about module input/output
	private final static int NUM_INPUT = 1;
	private final static int NUM_OUTPUT = 0;
	
	public FM_drop_flow(FP_LibFloodlight lib, String id, String type, String event, String[] pars, String[] inputs) {
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);	
	}
    

    public ModuleParameters run(ModuleParameters input, FPContext flCntx) {
       ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT);
       
       boolean doDrop;
       
       doDrop = input.getParameter(1).getBoolean();
  	   
       
       if(doDrop)
       {
    	   library.DROP(flCntx);
       }
        else
       {
           library.FORWARD(flCntx);
       }
       return outputPars;
    }




}
