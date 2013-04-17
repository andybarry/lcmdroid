package edu.mit.csail.lcmdroid;

import java.io.*;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;

import lcm.lcm.*;
import lcmtypes.lcmt_hotrod_optotrak;

public class MyLCMSubscriber implements LCMSubscriber
{
    LCM lcm;

    public MyLCMSubscriber(Context mContext, ChannelData channelData)
        throws IOException
    {
    	Log.d("LCM", "starting broadcast address code...");
    	
    	WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    	
        this.lcm = new LCM(wifi);
        this.lcm.subscribe("wingeron_x", this);
        Log.d("LCM", "subscriber started!!");
    }

    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
    {
    	Log.d("LCM", "Received message on channel " + channel);
    	
    	ci.addComponent(new ComponentInfo(channelComps.get(channelNames[i]).get(j)));
        try {
            if (channel.equals("wingeron_x")) {
                lcmt_hotrod_optotrak msg = new lcmt_hotrod_optotrak(ins);

                Log.d("LCM", "  timestamp    = " + msg.timestamp);
                Log.d("LCM", "  position     = [ " + msg.positions[0] +
                                   ", " + msg.positions[1] + ", " + msg.positions[2] + " ]");
            }

        } catch (IOException ex) {
            System.out.println("Exception: " + ex);
        }
    }

}
