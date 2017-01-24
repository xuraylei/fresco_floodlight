package edu.frescoplus.module.core;

public class Parameter{
	final static Integer INT_TYPE_MISMATCH = null;
	final static Boolean BOOL_TYPE_MISMATCH = null;
	
	String type;
	String value;
	
	public Parameter(String t, String v){
		this.type = t;
		this.value = v;
	}
	
	public String getType(){
		return this.type;
	}
	
	public String getValue(){
		return this.value;
	}
	
	public int getInteger(){
		if (type == "int"){
			return Integer.parseInt(value);
		}
		else{
			return  INT_TYPE_MISMATCH;
		}
	}
	
	public boolean getBoolean(){
		if (type == "boolean"){
			return Boolean.parseBoolean(value);
		}
		else{
			return BOOL_TYPE_MISMATCH;
		}
	}
	
	
}

