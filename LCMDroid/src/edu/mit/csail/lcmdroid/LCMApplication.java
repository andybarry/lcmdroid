package edu.mit.csail.lcmdroid;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.ExpandableListAdapter;

public class LCMApplication extends Application {

   public static final String APP_NAME = "LCM Monitor";
   public ChannelData channelData;
   public ChannelMainAdapter mAdapter;
   
   
   
   @Override
   public void onCreate() {
      super.onCreate();
      Log.d(APP_NAME, "APPLICATION onCreate");  
      channelData = new ChannelData();
      String[] channelNames = {
    		  "position","control","waypoints","laser_scan"
      };
      
      HashMap<String,ArrayList<String>> channelComps = new HashMap<String, ArrayList<String>>();
      
      
      // Default data - damn you, Java!
      ArrayList<String> pos =  new ArrayList<String>();
      pos.add("x");pos.add("y");pos.add("z");pos.add("theta"); 
      channelComps.put("position",pos);
      
      ArrayList<String> con =  new ArrayList<String>();
      con.add("linear_velocity");con.add("rotational_velocity"); 
      channelComps.put("control",con);
      
      ArrayList<String> wayp =  new ArrayList<String>();
      wayp.add("x");wayp.add("y"); 
      channelComps.put("waypoints",wayp);
      
      ArrayList<String> lsr =  new ArrayList<String>();
      lsr.add("dist_to_ground");lsr.add("dist_to_wall"); 
      channelComps.put("laser_scan",lsr);
      
      for(int i=0; i<channelNames.length; i++){
    	  ChannelInfo ci = new ChannelInfo(channelNames[i]);
    	  for(int j=0; j<channelComps.get(channelNames[i]).size(); j++){
    		  ci.addComponent(new ComponentInfo(channelComps.get(channelNames[i]).get(j)));  		  
    	  }
    	  channelData.addChannel(ci);    	  
      }
      
      
      startService(new Intent(this, DataService.class));
   }
   
   @Override
   public void onTerminate() {
      Log.d(APP_NAME, "APPLICATION onTerminate");      
      super.onTerminate();      
   }

}
