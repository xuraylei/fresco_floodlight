package edu.frescoplus.core;

import net.floodlightcontroller.core.FloodlightContext;

//FRESCO App context 

public class FPContext {	
	private FloodlightContext flowContext;
	
	public FPContext(){
		this.flowContext = null;
	}

	public FPContext(FloodlightContext flcntx){
		this.flowContext = flcntx;
	}
	
	public FloodlightContext getFlowContext(){
		return this.flowContext;
	}
	
	public void updateFlowContext(FloodlightContext flcntx){
		this.flowContext = flcntx;
	}
	
}
