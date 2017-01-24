package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;

/*Module: FM_mirror_flow
* mirror incoming flow to a specific switch port
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/

public class FM_mirror_flow extends AFP_Module{
	
	//descriptions about module
	private final static int NUM_INPUT =  1;
	private final static int NUM_OUTPUT = 0;
	
	//module specific variables
	private long dpid;
	private int  portid;
	
	public FM_mirror_flow(FP_LibFloodlight lib, String id, String type, String event, String[] pars, String[] inputs) {
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);	

        this.dpid =   Long.getLong(pars[0]);
        this.portid = Integer.getInteger(pars[1]);
	}
	
    @Override
    public ModuleParameters run(ModuleParameters input, FPContext cntx) {
    	
    	ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
    	
        boolean doAction = false;
    	
        doAction = input.getParameter(1).getBoolean();
         	
        if (doAction){
        	library.MIRROR(cntx, this.dpid, this.portid);
        }
    	
    	return outputPars;
    }
}
