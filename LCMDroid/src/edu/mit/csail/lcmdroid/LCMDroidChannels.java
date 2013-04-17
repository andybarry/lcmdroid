package edu.mit.csail.lcmdroid;


import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class LCMDroidChannels extends ExpandableListActivity { //Activity { 

    ExpandableListView channels;
    public Boolean showOnlyStarred = false;
    Timer updateTimer = new Timer("updateGUI");   // setup timer  
    GUItimer2 MyGUItimer = new GUItimer2();
    LCMApplication app;
    private String SRV = "LCM Droid Channels";
    
    
	public VizAdapter myAdapter;
	
	static final int timerUpdateRate = 100;
	
	Button butPlot;
	
	String[] channelArray = {"CONTROL", "LASER_SCAN", "POSITION", "WAYPOINTS"};
	int numChannels = 4;
	
	
	int[] numVars = {2, 2, 4, 2};
	
	String[][] varArray = { {"linear_velocity", "rotational_velocity"}, 
	                        {"dist_to_ground", "dist_to_wall"},
	                        {"x", "y", "z", "theta"},
	                        {"x", "y"}
	                      };
	
    public VizInfo[] theData= {
    		new VizInfo("CONTROL_linear_velocity",VizType.CHANNEL, 0.0),
    		new VizInfo("CONTROL_rotational_velocity",VizType.CHANNEL, 0.0),
    		new VizInfo("LASER_SCAN_dist_to_ground",VizType.CHANNEL, 0.0),
    		new VizInfo("LASER_SCAN_dist_to_wall",VizType.CHANNEL, 0.0),
    		new VizInfo("POSITION_x",VizType.CHANNEL, 0.0),
    		new VizInfo("POSITION_y",VizType.CHANNEL, 0.0),
    		new VizInfo("POSITION_z",VizType.CHANNEL, 0.0),
    		new VizInfo("POSITION_theta",VizType.CHANNEL, 0.0),
    		new VizInfo("WAYPOINTS_x",VizType.CHANNEL, 0.0),
    		new VizInfo("WAYPOINTS_y",VizType.CHANNEL, 0.0)
    		
        };
    
    public ChannelMainAdapter mAdapter;
    
    
	public void updateData(){
		Log.w("Main Screen", "update data");

		//dat.execute(this.mAdapter);
	}
	
	private static final String CHANNEL = "CHANNEL";
    private static final String VARIABLE = "VARIABLE";
    
    
    private MyLCMSubscriber sub;
   
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (LCMApplication) this.getApplication();
        ChannelData cd = app.channelData;
        try {
			sub = new MyLCMSubscriber(this, cd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        while(true){
        	if(cd.channels.size()==4){
        		Log.v("LCMChannels","got all the channels");
        		break;
        	}
        }
        

        

        // Set up our adapter
        mAdapter = new ChannelMainAdapter(this, cd);
        app.mAdapter = mAdapter;
        ExpandableListView ev = getExpandableListView();
        ev.setDividerHeight(0);
        setListAdapter(mAdapter);
        for(int i=0; i<mAdapter.getGroupCount(); i++){
        	Log.d(SRV,"group number "+i);
        	ev.expandGroup(i);
        }
        
        Log.v("LCMDroid", "starting async task");
        new ChannelsAsyncTask().execute(mAdapter);
        
    }
    
    public boolean onChildClick(ExpandableListView parent, View v,
            int groupPosition, int childPosition, long id) {
        
        // child was clicked -- show graph
        Intent myIntent = new Intent(v.getContext(), LCMGraph.class);
      myIntent.putExtra("edu.mit.csail.lcmdroid.ChildPosition", childPosition);
      myIntent.putExtra("edu.mit.csail.lcmdroid.GroupPosition", groupPosition);
      startActivityForResult(myIntent, 0);
        
        return true;
    }
    
    public void UpdateLabels() {
        runOnUiThread(new Runnable() {
            public void run() {         
                
                ((BaseExpandableListAdapter) mAdapter).notifyDataSetChanged();
            }
        });
    }
    
    
    // setup timer for updating GUI
    final class GUItimer2 extends TimerTask {
            public void run() {
                UpdateLabels();                 
            }
    }
    
    public void onStart()
    {
        super.onStart();
        
        
//        MyGUItimer.cancel();
//        MyGUItimer = new GUItimer2();
//        updateTimer.scheduleAtFixedRate(MyGUItimer, 0, timerUpdateRate);
    
    }
    
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_screen_menu, menu);
        return true;
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    
	    MenuItem filter = menu.findItem(R.id.toggleStarredAll);
	    MenuItem noFilter = menu.findItem(R.id.toggleStarredOnly);
	    
	    filter.setEnabled(showOnlyStarred);
	    noFilter.setEnabled(!showOnlyStarred);
	    
	    return true;
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.toggleStarredOnly:
        	showOnlyStarred = true;
        	((BaseExpandableListAdapter) mAdapter).notifyDataSetChanged();
            return true;
        case R.id.toggleStarredAll:
        	showOnlyStarred = false;
        	((BaseExpandableListAdapter) mAdapter).notifyDataSetChanged();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    
}


