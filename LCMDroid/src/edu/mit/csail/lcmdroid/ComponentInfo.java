package edu.mit.csail.lcmdroid;

public class ComponentInfo {

	public String name;
	public Data timedData;
	// potentially more information about the channel component
	// in the future we should add arbitrarily deep sub-components
	
	public ComponentInfo(String name){
		this.name = name;
		this.timedData = new Data();
	}
	
	public String getName() { return name; }
	
}
