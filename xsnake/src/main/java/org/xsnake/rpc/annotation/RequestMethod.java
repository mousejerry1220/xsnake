package org.xsnake.rpc.annotation;

public enum RequestMethod {
	
	get("GET"),post("POST"),put("PUT"),delete("DELETE");
	
	String type;
	
	RequestMethod(String type){
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return type;
	}
	
}
