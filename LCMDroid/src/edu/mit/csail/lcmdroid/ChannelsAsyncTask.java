package edu.mit.csail.lcmdroid;

import android.os.AsyncTask;
import android.util.Log;


public class ChannelsAsyncTask extends AsyncTask<ChannelMainAdapter, ChannelMainAdapter, Long> {

	private String SRV = "ChannelsAsyncTask";
	
	@Override
	protected Long doInBackground(ChannelMainAdapter... adapters) {
		ChannelMainAdapter cma = adapters[0];
		while(true){
			try {
				Thread.sleep((long) (1000));
				publishProgress(cma);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(SRV,"could not notify or sleep");
			}
		}
	
	}

	@Override
	protected void onProgressUpdate (ChannelMainAdapter... myAdapters){
		ChannelMainAdapter cma = myAdapters[0];
		cma.notifyDataSetChanged();
	}
	
}
