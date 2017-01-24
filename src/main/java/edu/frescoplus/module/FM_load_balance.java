package edu.frescoplus.module;

import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.ModuleParameters;

import java.util.ArrayList;
import java.util.List;

/* Module: FM_load_balance
* round-robin based load-balance service
* Author: Lei Xu
* Email: xray2012@email.tamu.edu 
*/

public class FM_load_balance extends AFP_Module {

	// descriptions about module
	private final static int NUM_INPUT =  1;
	private final static int NUM_OUTPUT = 0;

	// local variables
	private int serviceIP;
	private List<Integer> replicaIPList = new ArrayList<>();

	// for round-robin
	int round = 0;
	
	public FM_load_balance(FP_LibFloodlight lib, String id, String type, String event, String[] pars,
			String[] inputs) {
		super(lib, id, type, event, pars, inputs, NUM_INPUT, NUM_OUTPUT);

		serviceIP = library.parseIPv4(pars[0]);
		
		if (pars[1] != null) {
			//System.out.println(pars[1]);
			String[] token = pars[1].split(",");
			//System.out.println(token[0]);

			for (String t : token) {
				t = t.replaceAll("\\s",""); //remove space
				System.out.println(t);
				replicaIPList.add(library.parseIPv4(t));
			}
		}
	}

	@Override
	 public ModuleParameters run(ModuleParameters input, FPContext cntx) {
		
		ModuleParameters outputPars = new ModuleParameters(NUM_OUTPUT); 
		
		boolean doLoadBalance = false;
		
		int srcIP;
		int dstIP;
		
		if (library.isARP(cntx)){
			srcIP = library.getARPSenderIP(cntx);
			dstIP = library.getARPTargetIP(cntx);
		}
		else{
			srcIP = library.getSrcIP(cntx);
			dstIP = library.getDstIP(cntx);
		}		
		
		doLoadBalance = input.getParameter(1).getBoolean();
		
		// balance traffic to the target service
		if (serviceIP == dstIP && doLoadBalance) {
			
			int replica = replicaIPList.get(round);

			library.REDIRECT(cntx, srcIP, dstIP, replica);

			if (round < replicaIPList.size() - 1) {
				round++;
			} else {
				round = 0;
			}
		} else {
			library.FORWARD(cntx);
		}

		return outputPars;

	}

}
