package edu.mit.csail.lcmdroid;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChannelMainAdapter extends BaseExpandableListAdapter
{

    private Context context;
    private ChannelData channelData;
    private LayoutInflater inflater;
    private String SRV = "ChannelMainAdapter";
    private ArrayList<ArrayList<Boolean>> isStarredList = new ArrayList<ArrayList<Boolean>>();
    
    
    public ChannelMainAdapter(Context context, ChannelData dataIn)
    { 
        this.context = context;
        this.channelData = dataIn;
        inflater = LayoutInflater.from( this.context );
        // initialize isChecke
        for (int i=0;i<channelData.numChannels();i++){
        	isStarredList.add(new ArrayList<Boolean>());
            for (int j=0;j<channelData.getChannel(i).numComponents();j++){
            	isStarredList.get(i).add(false);
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
        if ( ((LCMDroidChannels)context).showOnlyStarred && isStarredList.get(groupPosition).get(childPosition)==false  ){
        	v = inflater.inflate(R.layout.main_hidden_child, parent, false);
        	return v;
        }
        //Log.d(SRV,"Getting child view"+groupPosition);
        // TODO instead check the type of the view to gain performance
//        if( convertView != null )
//        {
//            v = convertView;
//        } else {
            v = inflater.inflate(R.layout.main_child, parent, false);
//        }
        ComponentInfo cInfo = (ComponentInfo) getChild( groupPosition, childPosition );
        
        // set the name of the channel variable in the view
        ImageView starImageView = (ImageView)v.findViewById( R.id.star_image);
        Boolean starStatus = isStarredList.get(groupPosition).get(childPosition);
        starImageView.setImageResource( starStatus ? R.drawable.star_yes2 : R.drawable.star_no2);
        
        TextView varNameView = (TextView)v.findViewById( R.id.component_name );
        TextView varValueView = (TextView)v.findViewById( R.id.component_value );
        
        if (starStatus){
        	varNameView.setTypeface(Typeface.DEFAULT_BOLD);
        	varNameView.setTextColor(0xffedeb87);
        	
        	varValueView.setTypeface(Typeface.DEFAULT_BOLD);
        	varValueView.setTextColor(0xffedeb87);
        }
        
        if( varNameView != null && varValueView != null)
        {
            varNameView.setText( cInfo.getName() );
            try {
            	varValueView.setText( String.format("%.1f", cInfo.timedData.topY() ));	
            } catch (ArrayIndexOutOfBoundsException e){
            	varValueView.setText( "no" );
            }
            
        }
        
        starImageView.setOnClickListener(new OnClickListener() {
            public void onClick(View v)
            {
                // the checkbox was clicked (not the row)
                Log.d(SRV,"Clicked on the star!!! "+groupPosition+"  "+childPosition);
                Boolean oldStarStatus = isStarredList.get(groupPosition).get(childPosition);
                isStarredList.get(groupPosition).set(childPosition, !oldStarStatus);
                //ImageView iv = (ImageView) v;                
                //iv.setImageResource(!oldStarStatus ? R.drawable.star_yes : R.drawable.star_no);
                notifyDataSetChanged();
                //onRowChecked(groupPosition, childPosition, ((CheckBox)v).isChecked());
            }
        });
        return v;
    }

    public int getChildrenCount(int groupPosition)
    {
    	//Log.v(SRV,"children_count = "+channelData.getChannel( groupPosition ).numComponents());
        return channelData.getChannel( groupPosition ).numComponents();
    }

    public Object getGroup(int groupPosition)
    {
    	//Log.d(SRV,"getting group "+groupPosition);
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
            v = inflater.inflate(R.layout.main_group, parent, false);
        }
        
        ChannelInfo chanInfo = (ChannelInfo) getGroup( groupPosition );
        TextView channelName = (TextView)v.findViewById( R.id.channelname );
        
        
        TextView channelHz = (TextView)v.findViewById( R.id.channelhz );
        TextView channelSpeed = (TextView)v.findViewById( R.id.channelspeed );
        
        if( chanInfo != null )
        {
            channelName.setText( chanInfo.getName() );
            channelHz.setText( String.format("% 5.1f Hz", chanInfo.getFrequency()));
            channelSpeed.setText( String.format("% 5.1f KB/s", chanInfo.getSpeed()));
        }
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
        
        for (int i=0;i<channelData.numChannels();i++)
        {
            isStarredList.add(new ArrayList<Boolean>());
            for (int j=0;j<channelData.getChannel(i).numComponents();j++)
            {
                if (isStarredList.get(i).get(j) == true)
                {
                    checkedTraces.add(new TraceIndex(i, j));
                }
            }
        }
        return checkedTraces;
    }

}
