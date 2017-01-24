package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;


/* Module: FM_match_ip
* match a specific IP address
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/

public class FM_match_ip extends AFP_Module{
	
	// descriptions about module
	private final static int NUM_INPUT = 1;
	private final static int NUM_OUTPUT = 1;
	
    public int matchIP;
	
	public FM_match_ip(FP_LibFloodlight lib, String id, String type, String event, String[] pars, String[] inputs) {
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);	
		
	    String ip_to_match = pars[0];
	    
	    this.matchIP = library.parseIPv4(ip_to_match);
	}
     
    @Override
    public ModuleParameters run(ModuleParameters input, FPContext flCntx) {
    	ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
  
    	int ip;
    	boolean result;
 

       	ip = input.getParameter(1).getInteger();
    	  	
        result = (matchIP == ip);
             
        outputPars.setParameter(1, result);
        
        return outputPars;
        		
    }

}
