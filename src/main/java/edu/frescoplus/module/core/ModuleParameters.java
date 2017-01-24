package edu.frescoplus.module.core;

import java.util.HashMap;

//This parameter is to store the input/output parameters for modules 
public class ModuleParameters{
	
	private int numParameters;
	private HashMap<Integer, Parameter> parameters;
	
	public ModuleParameters(int num){
		this.numParameters = num;
		parameters = new HashMap<>();
	}
	
	//////////////////////////////////////////////////////////////////////
	//Functions to configure module parameters
	public boolean setParameter(int id, Parameter p){
		if (id <= numParameters){
			parameters.put(id,  p);
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean setParameter(int id, long value){
		if (id <= numParameters){
			parameters.put(id, new Parameter("long", String.valueOf(value)));
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean setParameter(int id, int value){
		if (id <= numParameters){
			parameters.put(id, new Parameter("int", String.valueOf(value)));
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean setParameter(int id, boolean value){
		if (id <= numParameters){
			parameters.put(id, new Parameter("boolean", String.valueOf(value)));
			return true;
		}
		else{
			return false;
		}
	}
	
	//////////////////////////////////////////////////////////////////////////
	//function to retrieve module parameters
	public Parameter getParameter(int id){
		return parameters.get(id);
	}
	
	@Override
	public String toString(){
		String output = "";
		
		for (int i=1; i <= numParameters; i++){
			output += "The " + i + " Paramter is " + parameters.get(i).getValue() 
					+ "(" + parameters.get(i).getType() + ")";
		}
		
		return output;
	}
	
}