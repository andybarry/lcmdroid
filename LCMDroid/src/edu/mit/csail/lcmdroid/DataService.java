package edu.mit.csail.lcmdroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DataService extends Service{

//	private LCMApplication app;
	private static final String SRV="Data Listener";
	
	
	@Override
	public void onCreate(){
		// TODO, just do the stuff that DataAsyncTask does right now
		Log.d(SRV, "Created the data service");
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		DataAsyncTask dat = new DataAsyncTask();
		dat.execute(this);
		
// TODO this blocks the main Application thread - solve it
//		Log.d(SRV, "Received start id " + startId + ": " + intent);
//		AssetManager assetManager = this.getAssets();
//        Log.d(SRV,"got asset mannager");
//        
//        // We want this service to continue running until it is explicitly
//        // stopped, so return sticky.
//        
//        app = (LCMApplication) this.getApplication();
//        Log.d(SRV,"got the application");
//        // TODO set input sources
//
//   	 	int n = app.channelData.numChannels();
//   	 	Log.d(SRV,"Num channels is "+n);
//        for(int i=0; i<n; i++){
//    		Log.d("data_background","info"+i);
//    		ChannelInfo ci = app.channelData.channels.get(i);
//        	String channelDataFile = ci.name.toLowerCase() + ".txt";
//        	BufferedReader input=null;
//        	
//            try {
//				InputStream dataStream = assetManager.open(channelDataFile);
//                input =  new BufferedReader(new InputStreamReader(dataStream));				
//			} catch (IOException e) {
//				Log.e(SRV, "Failed to initialize input "+channelDataFile);
//				// TODO Auto-generated catch block
//				// e.printStackTrace();
//			}
//			ci.inputSource = input; // null if there is an exception
//        }
//		Log.d(SRV,"initialized inputSources");
//               
//        while(true){      	
//	        	       	
//        	// for every channel in app.channelData
//        	for(int i=0; i<n; i++){
//        		ChannelInfo ci = app.channelData.channels.get(i);
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
//							}
//						}
//
//					}else{
//						// do nothing - no new data ...
//					}
//        		}        		
//        	}
//        	
//        	// maybe add some delay
//        	try {
//        		// make it a bit longer than perceptual fusion so that the user can read it.
//				Thread.sleep(100); // 0.1 seconds 
//			} catch (InterruptedException e) {
//				Log.e(SRV,"ahhhh, couldn't fall asleep");
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			Log.d(SRV,"finishing while iteration");
//        }
		return 0;
    }
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
