package edu.frescoplus.module.core;

import java.util.*;

//The runtime context for app/modules
public class FP_RuntimeContext {

	//parameter context for execution of app 
	HashMap<Long, ParameterConext> parContext;
	
	public FP_RuntimeContext(){
		parContext = new HashMap<>();
	}
	
	//-----------------------------------------------------------------
	//APIs for parameter contexts
	
	public long checkEID(long exeID){ // return the feasible EID
		while(parContext.containsKey(exeID)){
			exeID += 1;
		}
		return exeID;
	}

	//store output parameter context
	public void setOutputParameters(long exeID, String moduleID, ModuleParameters output){
		ParameterConext currentParCntx = parContext.get(exeID);
		
		if (currentParCntx == null){
			currentParCntx = new ParameterConext();
		}
		
		currentParCntx.updateModuleOutputContext(moduleID, output);
		parContext.put(exeID, currentParCntx);
		
	}
	
	//retrieve input parameters based on module configuration
	public ModuleParameters getInputParameters(Long exeID, AFP_Module module){
		ParameterConext currentParCntx = parContext.get(exeID);
		
		if (currentParCntx == null){
			return null;
		}
		
		String moduleID;
		int port;
		ModuleParameters inputPars = new ModuleParameters(module.getInputNumber());
		
		for ( int i = 1; i < module.getInputNumber() + 1; i++){
			moduleID = module.getOutputModuleFromInput(i);
			port = module.getOutputPortFromInput(i);
			Parameter par = currentParCntx.getModuleOutputContext(moduleID).getParameter(port);
			inputPars.setParameter(i, par);
		}	
		return inputPars;
	}

	public void removeParmeterContext(long exeID) {
		parContext.remove(exeID);
		
	}
}

//output context information for application  
class ParameterConext{
	HashMap<String, ModuleParameters> outputConext;
	
	public ParameterConext(){
		outputConext = new HashMap<>();
	}
	
	public void updateModuleOutputContext(String moduleID, ModuleParameters output){

		this.outputConext.put(moduleID, output);	
	}
	
	public ModuleParameters getModuleOutputContext(String moduleID){
		ModuleParameters moc = this.outputConext.get(moduleID);
		
		if (moc == null){
			return null;
		}
		
		return moc;	
	}
}

