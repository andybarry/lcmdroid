package edu.mit.csail.lcmdroid;

import java.util.ArrayList;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;

public class AddTrace extends ExpandableListActivity
{
    private static final String LOG_TAG = "LCMDroid";
    private ChannelCheckboxesAdapter expListAdapter;
    
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.add_trace);
        
        // get which traces should be checked and expanded by default based on what is currently
        // being graphed
        ArrayList<TraceIndex> checkedTraces = (ArrayList<TraceIndex>) getIntent().getSerializableExtra("edu.mit.csail.lcmdroid.tracesBeingGraphed");
        
        
        
        // get the channel names
        LCMApplication app = (LCMApplication) this.getApplication();

        expListAdapter = new ChannelCheckboxesAdapter( this, app.channelData, checkedTraces );
        setListAdapter( expListAdapter );
        
        // expand the channels that have checked graphs
        for (int i=0;i<checkedTraces.size();i++)
        {
            getExpandableListView().expandGroup(checkedTraces.get(i).parent);
        }
        
        // set up event handlers for the "Add Trace(s)" and "Cancel" buttons
        Button okBut = (Button) findViewById(R.id.butOk);
        Button cancelBut = (Button) findViewById(R.id.butCancel);
        
        okBut.setOnClickListener(new OnClickListener() {
            public void onClick(View v)
            {
                // ok button was clicked -- add the new trace back to the graph
                Intent myIntent = new Intent(v.getContext(), AddTrace.class);
                
                
                myIntent.putExtra("edu.mit.csail.lcmdroid.checkedTraces", expListAdapter.getCheckedTraces());
                setResult(RESULT_OK, myIntent);
                finish();
                Log.d(LOG_TAG, "ok clicked");
            }
        });
        
        cancelBut.setOnClickListener(new OnClickListener() {
            public void onClick(View v)
            {
                // cancel button clicked
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        
    }
    
    
    public void onContentChanged  () {
        super.onContentChanged();
        Log.d( LOG_TAG, "onContentChanged" );
    }

    // NOTE: this is tricky.  There are /two/ click listeners for each checkbox -- one for the
    // checkbox itself and one for the row --> they should both get the group and child positions
    // and then call another function.
    public boolean onChildClick(
            ExpandableListView parent, 
            View v, 
            int groupPosition,
            int childPosition,
            long id) {
        
        // here we need to toggle the checkbox, since it won't automatically go
        // like it will when you click the actual widget
        
        CheckBox cb = (CheckBox)v.findViewById( R.id.varcheck );
        if( cb != null )
            cb.toggle();
        
        expListAdapter.onRowChecked(groupPosition, childPosition, cb.isChecked());
        return false;
    }
    
}