package edu.frescoplus.core;

import java.util.ArrayList;
import java.util.HashMap;

import edu.frescoplus.event.FP_Event;
import edu.frescoplus.module.core.AFP_Module;
import edu.frescoplus.module.core.FP_RuntimeContext;
import edu.frescoplus.module.core.ModuleParameters;
import edu.frescoplus.module.core.Parameter;
import net.floodlightcontroller.core.FloodlightContext;


// Defines in which order we will execute FRESCO modules
// Holds references to all modules.

// Using only a Single Entry point, i.e. a single module is marked as the entry 
// point for the program. 

public class FPM_Graph {
	public final String name;
	
	AFP_Generic library;
	
	ArrayList<AFP_Module> modules;
	
	FP_RuntimeContext  fpmContext;
	
	//map from external event to its source module
	HashMap<FP_Event, ArrayList<AFP_Module>> eventToModulesMap;
	
	public FPM_Graph(String name, AFP_Generic lib) {
//		assert (modules.size() > 0);
		this.name = name;
		this.modules = new ArrayList<>();
		this.library = lib;
		
		this.fpmContext = new FP_RuntimeContext();
		this.eventToModulesMap = new HashMap<>();
	}

	public void addModule(AFP_Module module) {
		//Debug info
		System.out.println("[FRESCO] Add Module " + module.getType() + " to App " + name);
		
		modules.add(module);
	}
	
	//sequential execution of modules in a FRESCO app
	//TODO: Graph-based execution of module in an app,
	public void exec(FPContext cntx, FP_Event e) {
		//parameter lists between modules
		//ArrayList<Parameter> parCntx;
		ModuleParameters parCntx;
		
		//compute execution ID
		long exeID = fpmContext.checkEID(cntx.hashCode());
	
		for (AFP_Module module : modules) {
			//retrieve cached parameter lists 
			parCntx = fpmContext.getInputParameters(exeID, module);
			
			parCntx = module.run(parCntx, cntx);
			
			fpmContext.setOutputParameters(exeID, module.getID(), parCntx);
			
		}
		
		//clear execution context for this execution
		fpmContext.removeParmeterContext(exeID);
	}
	
}
