package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;

import net.floodlightcontroller.packet.IPv4;

/*Module: FM_redirect_ip
* redirect traffic from a host with specified IP
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/

public class FM_redirect_ip extends AFP_Module{
	
	// descriptions about module
	private final static int NUM_INPUT = 3;
	private final static int NUM_OUTPUT = 0;
	
	
	int redIP;
	
	public FM_redirect_ip(FP_LibFloodlight lib, String id, String type, String event, String[] pars, String[] inputs) {
		super(lib, id, type , event, pars, inputs, NUM_INPUT, NUM_OUTPUT);	
		
		redIP = IPv4.toIPv4Address(pars[0]);
	}

    @Override
    public ModuleParameters run(ModuleParameters input, FPContext cntx) {
    	ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
    	
    	boolean doReflect;
    	int sourceIP; 
    	int destIP;
    	
    	doReflect = input.getParameter(1).getBoolean();
        sourceIP = input.getParameter(2).getInteger();
        destIP = input.getParameter(3).getInteger();
    	
     
        if(doReflect)
        {	
            log.debug("[FRESCO] redirect " + sourceIP + " to " + destIP  );
            library.REDIRECT(cntx, sourceIP, destIP, redIP);
        }
        else
        {
            library.FORWARD(cntx);
        }
        
        return outputPars;
    }
}
