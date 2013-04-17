package edu.mit.csail.lcmdroid;

import java.io.BufferedReader;

public class VizInfo {
	
	public VizType type;
	public String name;
	public double bandwidth;
	public Data graphData;
	public BufferedReader inputSource;
	public VizState state = VizState.STABLE;
	
	public VizInfo(String nm, VizType tp, double bw){
		name = nm;
		type = tp;
		bandwidth = bw;
		graphData = new Data();
	}
	
	public void setDataStream(BufferedReader bf){
		inputSource = bf;
	}
	
}
