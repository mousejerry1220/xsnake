package org.xsnake.spring;

public class TestBean {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String init(String arg){
		System.out.println(arg+"---------------------------------aaa");
		return "arg_fffff";
	}
	
}
