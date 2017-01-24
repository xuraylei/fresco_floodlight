package edu.frescoplus.module.core;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import edu.frescoplus.core.AFP_Generic;
import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FP_FloodlightRTE;
import edu.frescoplus.core.FP_LibFloodlight;
import edu.frescoplus.core.TCPSession;
import edu.frescoplus.event.FP_Event;


public abstract class AFP_Module {
	//log
	protected static final Logger log = LoggerFactory.getLogger(AFP_Module.class);
	
	protected String id;
	protected String type;
	protected String[] parameters;
	protected String   event;
	
	//input/output numbers
	protected int inputNum = -1;
	protected int outputNum = -1;
	
	protected AFP_Generic library;
	
	//connections with other modules
	HashMap<Integer, ModuleInputDescription> inputMap;
	
	public AFP_Module(FP_LibFloodlight lib, String id, String type, String event, String[] pars, String[] inputs,
			int in, int out)
	{
		this.id = id;
		this.type = type;
		this.parameters = pars;
		this.inputNum = in;
		this.outputNum = out;
		
		this.library = lib;
		
		//parse input description
		inputMap = new HashMap<>();
		for (String input : inputs){
			String [] items = input.split(":");
			
			if(items != null){
				//parse content in a input description string (inputID: OutputID)
				int inputID = Integer.parseInt(items[0]);
				String outputModule = items[1];
				int outputID = Integer.parseInt(items[2]);
				
				ModuleInputDescription md = new ModuleInputDescription(outputModule, outputID);
				inputMap.put(inputID, md);
			}
		}
		
		//check input number 
		if (inputNum != inputMap.size()){
			log.error("Fail to instantiate module "  + id + " due to input number mismatch.");
		}
		
		//parse event description
		this.event = event;
	}

	//use ID to check the equality
	@Override
	public boolean equals(Object o){
		if (o == null){
			return false;
		}
		
		if (!AFP_Module.class.isAssignableFrom(o.getClass())) {
	        return false;
	    }
		
		final AFP_Module m = (AFP_Module) o;
		
		return m.getID().equals(m.getID());
		
	}

	//check the the number of input variables
	public boolean inputSizeCheck(ArrayList<Parameter> input, int size){
		return (input.size() == size);
	}
	
	
	
	public String getID(){
		return this.id;
	}
	
	public String getType(){
		return this.type;
	}
	
	public String[] getParameters(){
		return this.parameters;
	}
	
	public String getEvent(){
		return this.event;	
	}
	
	public int getInputNumber(){
		return this.inputNum;
	}
	
	public int getOutputNumber(){
		return this.outputNum;
	}
	
	
	//////////////////////////////////////////////////////////////////
	//retrieve input context information from configuration
	public String getOutputModuleFromInput(int id){
		
		ModuleInputDescription mid = this.inputMap.get(id);
		
		return mid.getModuleID();
	}
	
	public int getOutputPortFromInput(int id){
		return this.inputMap.get(id).getOutputPort();
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	//Abstract method for module execution
	public abstract ModuleParameters run(ModuleParameters input, FPContext cntx);
	
}

class ModuleInputDescription{
	String moduleID;
	int    outputPort;
	
	public ModuleInputDescription(String id, int output){
		this.moduleID = id;
		this.outputPort = output;
	}
	
	public String getModuleID(){
		return this.moduleID;
	}
	
	public int getOutputPort(){
		return this.outputPort;
	}
}
