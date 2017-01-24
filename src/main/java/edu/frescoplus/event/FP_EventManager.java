package edu.frescoplus.event;

import java.util.*;

import net.floodlightcontroller.core.FloodlightContext;
import edu.frescoplus.core.FPContext;
import edu.frescoplus.core.FPM_Graph;

//This module only manage the external events, e.g., PACKET, TCP_CONNECTION_FAIL

public class FP_EventManager{

	//Mapping from events to FRESCO handled events
	HashMap<String, FP_Event> events;
	
	//store the map between modules and event
	HashMap<FP_Event, ArrayList<FPM_Graph>> eventHandlers;
	

	public FP_EventManager(){
		events = new HashMap<>();
		eventHandlers = new HashMap<>();
		
		//Map from script events to FRESCO internal events
		events.put("INCOMMING_FLOW", FP_Event.PACKET);
		events.put("TCP_CONNECTION_FAIL", FP_Event.TCP_CONNECTION_FAIL);
		events.put("TCP_CONNECTION_SUCCESS", FP_Event.TCP_CONNECTION_SUCCESS);
	}
	
	public FP_Event parseFPEvent(String e){
		return this.events.get(e);
	}
	
	public void register(String e, FPM_Graph app){
		FP_Event event = events.get(e);
		
		if (event != null){
			register (event, app);
		}
	}
	
	//register module to a specific event
	private void register(FP_Event e, FPM_Graph app){
		ArrayList<FPM_Graph> handlers = eventHandlers.get(e);
		if (handlers == null){
			handlers = new ArrayList<FPM_Graph>();
		}
		handlers.add(app);
		eventHandlers.put(e, handlers);
	}
	
	public void raiseEvent(FPContext cntx, FP_Event e){
		ArrayList<FPM_Graph> pHandlers = eventHandlers.get(e);
		
		
		if (pHandlers == null) {
			return;
		}
		
		for (FPM_Graph app : pHandlers){
			app.exec(cntx, e);
		}
	}

}
