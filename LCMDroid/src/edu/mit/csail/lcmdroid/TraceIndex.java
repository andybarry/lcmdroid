package edu.mit.csail.lcmdroid;

import java.io.Serializable;

public class TraceIndex implements Serializable
{
    private static final long serialVersionUID = -4535858894523094438L;
    
    public int parent;
    public int child;
    
    public TraceIndex(int parentIn, int childIn)
    {
        parent = parentIn;
        child = childIn;
    }
}