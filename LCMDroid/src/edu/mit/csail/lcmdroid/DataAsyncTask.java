package edu.mit.csail.lcmdroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

public class DataAsyncTask extends AsyncTask<DataService, Long, Long> {

	private static final String SRV="Data Async Task";
	private LCMApplication app;
	private static final float tStep = 0.01f;
	
	// this is right before we alloc a whole bunch of new data
	//private static final int MAX_DATA_SIZE = 32767;
    //private static final int LOWER_DATA_SIZE = 16383;
	
	private static final int MAX_DATA_SIZE = 16383;
    private static final int LOWER_DATA_SIZE = 8193;
    
    //private static final int MAX_DATA_SIZE = 1000;
    //private static final int LOWER_DATA_SIZE = 10;

	@Override
	protected Long doInBackground(DataService... dss) {
		
		DataService ds = dss[0];
		
//		Log.d(SRV, "Received start id " + startId + ": " + intent);
		AssetManager assetManager = ds.getAssets();
        Log.d(SRV,"got asset mannager");
        
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        
        app = (LCMApplication) ds.getApplication();
        Log.d(SRV,"got the application");
        // TODO set input sources

   	 	int n = app.channelData.numChannels();
   	 	Log.d(SRV,"Num channels is "+n);
        for(int i=0; i<n; i++){
    		Log.d("data_background","info"+i);
    		ChannelInfo ci = app.channelData.channels.get(i);
        	String channelDataFile = ci.name.toLowerCase() + ".txt";
        	BufferedReader input=null;
        	
            try {
				InputStream dataStream = assetManager.open(channelDataFile);
                input =  new BufferedReader(new InputStreamReader(dataStream));				
			} catch (IOException e) {
				Log.e(SRV, "Failed to initialize input "+channelDataFile);
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
			ci.inputSource = input; // null if there is an exception
        }
		Log.d(SRV,"initialized inputSources");
        
		float t = 0;
		
		
		// init arrays to keep some state so we can generate nice smooth
		// but random moving data
		ArrayList<ArrayList<Float>> lastData = new ArrayList<ArrayList<Float>>();
		
		for (int i=0;i<n;i++)
		{
		    ChannelInfo ci = app.channelData.channels.get(i);
		    lastData.add(new ArrayList<Float>());
		    
		    for (int j=0;j<ci.numComponents();j++)
		    {
		        lastData.get(i).add(0f);
		    }
		}
		
		float componentValue;
		
		Random randGen = new Random();

		
        while(true) {
	        	       	
        	// for every channel in app.channelData
        	for(int i=0; i<n; i++){
        		ChannelInfo ci = app.channelData.channels.get(i);
        		
        		
        		// don't read from a file or the network for now
        		// just make up the data
        		
        		for (int j=0; j<ci.numComponents(); j++)
        		{
        		    ComponentInfo cmpi = ci.getComponent(j);
        		    
        		    // generate random data
        		    componentValue = lastData.get(i).get(j)+ (randGen.nextFloat()  - 0.5f) * tStep * 10f;
        		    
        		    // push the data to the graph
        		    cmpi.timedData.push(t, componentValue);
        		    
        		    // save the generated data so we can make smooth lines
        		    lastData.get(i).set(j, componentValue);
        		    
        		    // if the graph size is large, start erasing things to ensure
        		    // we don't run out of memory
        		    
        		    if (cmpi.timedData.size > MAX_DATA_SIZE)
        		    {
        		        cmpi.timedData.eraseDataFromStart(cmpi.timedData.size - LOWER_DATA_SIZE);
        		    }
        		    
        		}
        	}
        	
        	// update t
        	t += tStep;
        		
        		
//        		if(ci.inputSource!=null){
//        			// look at the input source and load new data
//        			String line = null;
//	        		try {
//						line = ci.inputSource.readLine();
//					} catch (IOException e) {
//						// No more data
//						ci.inputSource = null;
//						line = null;
//						continue;						
//					}
//					if(line!=null){
//						// parse the line and update the corresponding component infos
//						String[] line_items = line.split(",");
//						
//						// every line contains components and values starting with a timestamp						
//						float t = Float.valueOf(line_items[0]);
//						
//						for(int j=1; j<line_items.length; j+=2){
//							String componentName = line_items[j];
//							float componentValue = Float.valueOf(line_items[j+1]);
//							ComponentInfo cmpi = ci.getInfo(componentName);
//							if(cmpi!=null){
//								cmpi.timedData.push(t, componentValue);
//								Log.d(SRV,"channel:"+ci.name+"  component:"+componentName+"   value:"+componentValue);
//							} else {
//								Log.e(SRV,"cmpi is null");
//							}
//						}
//
//					}else{
//						// do nothing - no new data ...
//						return null;
//					}
//        		}
//        	}
        	
        	// maybe add some delay
        	try {
        		// make it a bit longer than perceptual fusion so that the user can read it.
				Thread.sleep((long) (tStep * 1000));
				publishProgress((long) 1);
				
			} catch (InterruptedException e) {
				Log.e(SRV,"ahhhh, couldn't fall asleep");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Log.v(SRV,"finishing while iteration");
        }
	}
	
	protected void onProgressUpdate (Long... myAdapters){
//		myAdapters[0].notifyDataSetChanged();
		//Log.v(SRV,"Publishing progress reports");
//		try{
//			app.mAdapter.notifyDataSetChanged();	
//		}catch (Exception e){
//			// do nothing
//			Log.d(SRV,"Cannot notify the adapter for changes");
//		}
		
	}
	
	
}