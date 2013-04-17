package edu.mit.csail.lcmdroid;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class ChannelCheckboxesAdapter extends BaseExpandableListAdapter
{

    private Context context;
    private ChannelData channelData;
    private LayoutInflater inflater;
    private ArrayList<ArrayList<Boolean>> isCheckedList = new ArrayList<ArrayList<Boolean>>();
    private ArrayList<TraceIndex> previouslyGraphing = new ArrayList<TraceIndex>();

    public ChannelCheckboxesAdapter(Context context, ChannelData dataIn, ArrayList<TraceIndex> checkedTraces)
    { 
        this.context = context;
        this.channelData = dataIn;
        inflater = LayoutInflater.from( context );
        
        boolean thisChecked;
        
        previouslyGraphing = checkedTraces;
        
        // initialize data structure for holding which items are checked
        for (int i=0;i<channelData.numChannels();i++)
        {
            isCheckedList.add(new ArrayList<Boolean>());
            for (int j=0;j<channelData.getChannel(i).numComponents();j++)
            {
                thisChecked = false;
                for (int k=0;k<checkedTraces.size();k++)
                {
                    if (checkedTraces.get(k).parent == i &&
                            checkedTraces.get(k).child == j)
                    {
                        thisChecked = true;
                        break;
                    }
                    
                }
                isCheckedList.get(i).add(thisChecked);
            }
        }
    }

    public Object getChild(int groupPosition, int childPosition)
    {
        return channelData.getChannel(groupPosition).getComponent(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition)
    {
        return (long)( groupPosition*1024+childPosition+1 );  // Max 1024 children per group
    }

    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v = null;
        if( convertView != null )
        {
            v = convertView;
        } else {
            v = inflater.inflate(R.layout.add_trace_child_row, parent, false);
        }
        ComponentInfo cInfo = (ComponentInfo) getChild( groupPosition, childPosition );
        
        // set the name of the channel variable in the view
        
        TextView varNameView = (TextView)v.findViewById( R.id.varname );
        if( varNameView != null )
        {
            varNameView.setText( cInfo.getName() );
        }
        
        // check the checkbox if it has been previously clicked on
        CheckBox cb = (CheckBox)v.findViewById( R.id.varcheck );
        cb.setChecked( isCheckedList.get(groupPosition).get(childPosition) );
        
        // NOTE: this is tricky.  There are /two/ click listeners for each checkbox -- one for the
        // checkbox itself and one for the row --> they should both get the group and child positions
        // and then call another function.
        cb.setOnClickListener( new OnClickListener() {
            public void onClick(View v)
            {
                // the checkbox was clicked (not the row)
                
                onRowChecked(groupPosition, childPosition, ((CheckBox)v).isChecked());
            }
        });
        
        return v;
    }
    
    public void onRowChecked(int groupPosition, int childPosition, boolean isChecked)
    {
        Log.d("LCMDroid", "check check!!, group:" + groupPosition + " child: " + childPosition);
        
        isCheckedList.get(groupPosition).set(childPosition, isChecked);
    }

    public int getChildrenCount(int groupPosition)
    {
        return channelData.getChannel( groupPosition ).numComponents();
    }

    public Object getGroup(int groupPosition)
    {
        return channelData.getChannel( groupPosition );
    }

    public int getGroupCount()
    {
        return channelData.numChannels();
    }

    public long getGroupId(int groupPosition)
    {
        return (long)( groupPosition*1024 );  // To be consistent with getChildId
    } 

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        View v = null;
        if( convertView != null )
        {
            v = convertView;
        } else {
            v = inflater.inflate(R.layout.add_trace_group_row, parent, false);
        }
        
        ChannelInfo chanInfo = (ChannelInfo) getGroup( groupPosition );
        TextView channelName = (TextView)v.findViewById( R.id.childname );
        if( chanInfo != null )
            channelName.setText( chanInfo.getName() );
        return v;
    }

    public boolean hasStableIds()
    {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return true;
    } 

    public void onGroupCollapsed (int groupPosition) {} 
    public void onGroupExpanded(int groupPosition) {}

    public ArrayList<TraceIndex> getCheckedTraces()
    {
        ArrayList<TraceIndex> checkedTraces = new ArrayList<TraceIndex>();
        
        // we want to keep the coloring consistent, which means keeping the ordering
        // of the array the same
        // (ie if we already are plotting something, keep that at the top of the array)
        
        // check to see if we have removed things from the array
        for (int i=0;i<previouslyGraphing.size();i++)
        {
            TraceIndex trace = previouslyGraphing.get(i);
            if (isCheckedList.get(trace.parent).get(trace.child) == true)
            {
                // previously was checked, and still is checked
                checkedTraces.add(trace);
            }
        }
        
        ArrayList<TraceIndex> preChecked = new ArrayList<TraceIndex>(checkedTraces);
        
        for (int i=0;i<channelData.numChannels();i++)
        {
            //isCheckedList.add(new ArrayList<Boolean>());
            for (int j=0;j<channelData.getChannel(i).numComponents();j++)
            {
                // check for prechecked values (this could be more efficient with a HashMap,
                // but it doesn't happen that often
                boolean flag = false;
                for (int k=0;k<preChecked.size();k++)
                {
                    if (preChecked.get(k).parent == i && preChecked.get(k).child == j)
                    {
                        flag = true;
                        break;
                    }
                }
                
                if (isCheckedList.get(i).get(j) == true && flag == false)
                {
                    checkedTraces.add(new TraceIndex(i, j));
                }
            }
        }
        return checkedTraces;
    }
    

}
