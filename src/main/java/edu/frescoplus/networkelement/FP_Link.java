package edu.frescoplus.networkelement;

import java.util.Objects;

import net.floodlightcontroller.routing.Link;

public class FP_Link {
	long switch1;
	long switch2;
	int port1;
	int port2;
	
	//a flag indicating if the link is switch-to-host link
	boolean isEdgeLink;
	
	int throughtout;
	
	public FP_Link(Link link){
		this.switch1 = link.getSrc().getLong();
		this.port1 = link.getSrcPort().getPortNumber();
		this.switch2 = link.getDst().getLong();
		this.port2 = link.getDstPort().getPortNumber();
		
		this.isEdgeLink = false;
	}
	
	public int getThroughtput(){
		return this.throughtout;
	}

	
	//Overriding equals() and hashcode() for the key of hashmap
	@Override
	public int hashCode() {
		return Objects.hash(switch1, switch2, port1, port2);
		
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (!FP_Link.class.isAssignableFrom(o.getClass())) {
			return false;
		}

		final FP_Link id = (FP_Link) o;
		
		if (id.isEdgeLink != this.isEdgeLink){
			return false;
		}
		
		if ((id.switch1 == this.switch1) && 
			(id.port1 == this.port1) 	 && 
			(id.switch2 == this.switch2) &&
			(id.port2 == this.port2)){
			return true;
		}
		
		if ((id.switch1 == this.switch2) && 
			(id.port1 == this.port2) 	 && 
			(id.switch2 == this.switch1) &&
			(id.port2 == this.port1)){
			return true;
		}
		return false;
	}
}
