package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;

import org.projectfloodlight.openflow.types.MacAddress;

/* Module: FM_match_mac
* match a specific IP address
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/
public class FM_match_mac extends AFP_Module{
	
	// descriptions about module
	private final static int NUM_INPUT = 1;
	private final static int NUM_OUTPUT = 0;
	
    public long matchMAC;
	
	public FM_match_mac(FP_LibFloodlight lib, String id, String type, String event, String[] pars, String[] inputs) {
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);	
		
	    String macString = pars[0];
	    
	    this.matchMAC = library.parseMAC(macString);
	}

    @Override
    public ModuleParameters run(ModuleParameters input, FPContext flCntx) {
    	ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
  
    	long mac;
    	boolean result;
 
       	mac = input.getParameter(1).getInteger();
    	  	
        result = (matchMAC == mac);
              
        outputPars.setParameter(1, result);
        
        return outputPars;
        		
    }

}
