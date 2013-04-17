package edu.mit.csail.lcmdroid;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.util.Log;

public class ChannelInfo {

	public String name;
	public BufferedReader inputSource;
	
	private float lastFreq = 40f;
	private float lastSpeed = 10f;
	
	
	// potentially more information about the channel.	
	
	public List<ComponentInfo> components;
	
	public ChannelInfo(String name){
		this.name = name;
		this.components = new ArrayList<ComponentInfo>();
	}
	
	public ChannelInfo(String name,List<ComponentInfo> comps){
		this.name = name;
		this.components = comps;
	}
	
	public void addComponent(ComponentInfo c){
		this.components.add(c);
	}
	
	public int numComponents(){
		return components.size();
	}
	
	public ComponentInfo getComponent(String compName){
		// TODO optimize !!!!!!!!!!!!
		for(int i=0; i<components.size(); i++){
			if(components.get(i).name.equals(compName)){
				return components.get(i);
			}else{
				//Log.v("Missing component", components.get(i).name);
			}
			
		}
		Log.e("Channel "+name,"failed to find component named "+compName);
		
		return null;
	}
	
	public ComponentInfo getComponent(int index) {
	    return components.get(index);
	}
	
	public String getName() { return name; }
	
	public float getFrequency()
	{
	    Random rand = new Random();
	    lastFreq += rand.nextFloat() - 0.5; 
	    if (lastFreq < 10)
	    {
	        lastFreq = 10;
	    }
	    
	    if (lastFreq > 240)
	    {
	        lastFreq = 240;
	    }
	    return lastFreq;
	    
	}
	
	public float getSpeed()
	{
	    Random rand = new Random();
	    lastSpeed += rand.nextFloat() - 0.5; 
        if (lastSpeed < 1)
        {
            lastSpeed = 1;
        }
        
        if (lastSpeed > 290)
        {
            lastSpeed = 290;
        }
        return lastSpeed;
	}
	
}
