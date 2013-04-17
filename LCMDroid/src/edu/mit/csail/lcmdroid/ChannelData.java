package edu.mit.csail.lcmdroid;

import java.util.ArrayList;
import java.util.List;

public class ChannelData {

	public List<ChannelInfo> channels;
	
	public ChannelData() {
		channels = new ArrayList<ChannelInfo>();
	}
	
	public void addChannel(ChannelInfo cinfo){
		channels.add(cinfo);
	}
	
	public int numChannels(){
		return channels.size();
	}
	
	public ChannelInfo getChannel(int index)
	{
	    return channels.get(index);
	}
}
