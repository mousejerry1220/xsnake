package org.xsnake.rpc.test;

import java.io.Serializable;

public class TestParam implements Serializable{

	private static final long serialVersionUID = 1L;

	String name;

	String aaa;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAaa() {
		return aaa;
	}

	public void setAaa(String aaa) {
		this.aaa = aaa;
	}
}
