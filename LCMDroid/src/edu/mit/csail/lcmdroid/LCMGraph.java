package edu.mit.csail.lcmdroid;

import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import org.javia.arity.Function;
import org.javia.arity.Symbols;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class LCMGraph extends Activity {
	
	static Symbols symbols = new Symbols();
	private GraphView graphView;
	
	private static final int ADD_TRACE_CODE = 1;
	
	LCMApplication app;
    ChannelData cd;	
	
	static final int timerUpdateRate = 50;
	private static final String SRV = "LCM Graph Activity";
	
	Timer updateTimer = new Timer("updateGUI");   // setup timer  
	GUItimer MyGUItimer = new GUItimer();
	
	ArrayList<Data> graphData = new ArrayList<Data>();
	
	ArrayList<String> legendStrings = new ArrayList<String>();
	
	ArrayList<TraceIndex> tracesBeingGraphed = new ArrayList<TraceIndex>();
	
	float xCounter = 0;
	float lastY = 0;
	
	Queue<Float> unshownGraphDataX = new LinkedBlockingQueue<Float>();
	Queue<Float> unshownGraphDataY = new LinkedBlockingQueue<Float>();
	
	Random randGen = new Random();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);
        
        app = (LCMApplication) this.getApplication();
        cd = app.channelData;
        
        graphView = (GraphView) findViewById(R.id.graph);
        graphView.setVisibility(View.VISIBLE);
        graphView.setTracking(true);
        
        //TextView label = (TextView) findViewById(R.id.graphlabel);
        
        // check for data passed in to tell us what channel we are using
        Bundle extras = getIntent().getExtras();
//        
//        int pos = extras.getInt("edu.mit.csail.lcmdroid.Position");
//        graphData = savedInstanceState.
        
        //TODO instead the graph data will be taken from the VizInfo...
        if (extras != null) {
            String channelName = extras.getString("edu.mit.csail.lcmdroid.ChannelName");
            //label.setText(channelName);
            //graphView.yLabel = channelName;
            
            String channelDataFile = extras.getString("edu.mit.csail.lcmdroid.ChannelDataFile");
            Log.d("LCMDroid", "channelDataFile:" + channelDataFile);
            int gr_pos = extras.getInt("edu.mit.csail.lcmdroid.GroupPosition");
            int ch_pos = extras.getInt("edu.mit.csail.lcmdroid.ChildPosition");
            
            
            tracesBeingGraphed.add(new TraceIndex(gr_pos, ch_pos));
            
            Log.d(SRV,"Child Postion:"+ch_pos);
            Log.d(SRV,"Group Position"+gr_pos);
            
            for (int i=0;i<tracesBeingGraphed.size();i++)
            {
                ChannelInfo chanInfo = app.channelData.channels.get(tracesBeingGraphed.get(i).parent);
                ComponentInfo varInfo = chanInfo.components.get(tracesBeingGraphed.get(i).child);
                
                graphData.add(varInfo.timedData);
                
                legendStrings.add(chanInfo.name + "." + varInfo.name);
            }
        }
        
    
	}
    
    
	public void RefreshGraph()
	{
	    runOnUiThread(new Runnable() {
	        public void run() {
	            graphView.setScatterDataArray(graphData, legendStrings);
	        }
	    });

	}
    
    // setup timer for updating GUI
	final class GUItimer extends TimerTask {
			public void run() {
			    RefreshGraph();					
			}
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		MyGUItimer.cancel();
		MyGUItimer = new GUItimer();
		updateTimer.scheduleAtFixedRate(MyGUItimer, 0, timerUpdateRate);
		
	}
    
    
	private void showGraph(Function f) {
        if (f == null) {
            /*if (historyView.getVisibility() != View.VISIBLE) {
                graphView.setVisibility(View.GONE);
                graph3dView.setVisibility(View.GONE);
                graph3dView.onPause();
                historyView.setVisibility(View.VISIBLE);
                result.setVisibility(View.VISIBLE);
            }
            */
        } else {
            // graphedFunction = f;
            if (f.arity() == 1) {
                graphView.setFunction(f);
                /*
                if (graphView.getVisibility() != View.VISIBLE) {
                    if (isAlphaVisible) {
                        isAlphaVisible = false;
                        updateAlpha();
                    }
                    result.setVisibility(View.GONE);
                    historyView.setVisibility(View.GONE);
                    graph3dView.setVisibility(View.GONE);
                    graph3dView.onPause();
                    graphView.setVisibility(View.VISIBLE);
                }
                */
            } else {
            	/*
                graph3dView.setFunction(f);
                if (graph3dView.getVisibility() != View.VISIBLE) {
                    if (isAlphaVisible) {
                        isAlphaVisible = false;
                        updateAlpha();
                    }
                    result.setVisibility(View.GONE);
                    historyView.setVisibility(View.GONE);
                    graphView.setVisibility(View.GONE);
                    graph3dView.setVisibility(View.VISIBLE);
                    graph3dView.onResume();
                }
                */
            }
        }
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.graph_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent startIntent;
        switch (item.getItemId()) {
        case R.id.addTrace:
            startIntent = new Intent(this, edu.mit.csail.lcmdroid.AddTrace.class);
            startIntent.putExtra("edu.mit.csail.lcmdroid.tracesBeingGraphed", tracesBeingGraphed);
            startActivityForResult(startIntent, ADD_TRACE_CODE);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (ADD_TRACE_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    ArrayList<TraceIndex> checkedTraces = (ArrayList<TraceIndex>) data.getSerializableExtra("edu.mit.csail.lcmdroid.checkedTraces");
                    
                    tracesBeingGraphed = checkedTraces;
                    graphData.clear();
                    legendStrings.clear();
                    
                    for (int i=0;i<checkedTraces.size();i++)
                    {
                        ChannelInfo chanInfo = app.channelData.channels.get(checkedTraces.get(i).parent);
                        ComponentInfo varInfo = chanInfo.components.get(checkedTraces.get(i).child);
                        
                        graphData.add(varInfo.timedData);
                        
                        legendStrings.add(chanInfo.name + "." + varInfo.name);
                    }
                    
                }
                
                break;
            } 
        }
    }

    
    @Override
    public void onResume() {
        super.onResume();
        Log.d("LCMDroid", "graph (lcm) is in onResume");
    }
    
}