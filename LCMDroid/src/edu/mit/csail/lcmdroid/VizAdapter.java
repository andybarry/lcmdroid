package edu.mit.csail.lcmdroid;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

//import com.example.android.apis.R;
//import com.example.android.apis.view.List14.EfficientAdapter.ViewHolder;

public class VizAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    public Context context;
    public VizInfo[] DATA= {
    };
//    private Bitmap mIcon1;
//    private Bitmap mIcon2;

    public VizAdapter(Context ctx) {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(ctx);
        context = ctx;
//        , VizInfo[] channels
//        DATA=channels;
        // Icons bound to the rows.
//        mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon48x48_1);
//        mIcon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon48x48_2);
    }

    /**
     * The number of items in the list is determined by the number of speeches
     * in our array.
     *
     * @see android.widget.ListAdapter#getCount()
     */
    public int getCount() {
        return DATA.length;
    }

    /**
     * Since the data comes from an array, just returning the index is
     * sufficent to get at the data. If we were using a more complex data
     * structure, we would return whatever object represents one row in the
     * list.
     *
     * @see android.widget.ListAdapter#getItem(int)
     */
    public Object getItem(int position) {
        return DATA[position];
    }

    /**
     * Use the array index as a unique id.
     *
     * @see android.widget.ListAdapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * Make a view to hold each row.
     *
     * @see android.widget.ListAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;
        Context c = parent.getContext();
        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.single_viz_layout, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.text);
            holder.bandwidth = (TextView) convertView.findViewById(R.id.bandwidth);
          
//            holder.icon = (ImageView) convertView.findViewById(R.id.icon);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }
        
        // Bind the data efficiently with the holder.
        holder.text.setText(DATA[position].name);
        String bwString = new Double(DATA[position].bandwidth).toString();
        holder.bandwidth.setText(bwString);
//        holder.icon.setImageBitmap((position & 1) == 1 ? mIcon1 : mIcon2);
        Log.v("style","will be switching");
        
        holder.text.setBackgroundColor(Color.rgb(0, 0, 0));
        holder.bandwidth.setBackgroundColor(Color.rgb(0, 0, 0));
        
        switch(DATA[position].state){
        case INCREASING:
        	Log.v("style","increasing");
//    		<color name="yellow">#</color>
//    		<color name="whitish">#</color>
//    		<color name="greyish">#</color>
        	holder.text.setTextColor(Color.rgb(255,255,255)); // yellow
//        	holder.text.setTypeface(Typeface.SANS_SERIF, 1); 
        	break;
        case STABLE:
        	Log.v("style","stable");
        	holder.text.setTextColor(Color.rgb(210,210,210)); // white-ish
//        	holder.text.setTypeface(Typeface.SANS_SERIF, 0); 
        	break;
        case DECREASING:
        	Log.v("style","decreasing");
        	holder.text.setTextColor(Color.rgb(153,153,153)); // grey
//        	holder.text.setTypeface(Typeface.SANS_SERIF, 0);        	
        	break;
        case OVER:
        	Log.v("style","over");
        	holder.text.setTextColor(Color.rgb(128,128,128)); // grey
//        	holder.text.setTypeface(Typeface.SANS_SERIF, 0);        	
        	break;        	
        }
        
        
        return convertView;
    }

    static class ViewHolder {
        TextView text;
        TextView bandwidth;
//        ImageView icon;
    }
}