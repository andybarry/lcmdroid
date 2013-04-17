// Copyright (C) 2009-2010 Mihai Preda
// Copyright (C) 2011 Dimitar Simeonov, Andy Barry (changes for LCM Droid)

package edu.mit.csail.lcmdroid;

import java.util.ArrayList;

import org.javia.arity.Function;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.ZoomButtonsController;

public class GraphView extends View implements 
                                        Grapher,
                                        ZoomButtonsController.OnZoomListener,
                                        TouchHandler.TouchHandlerInterface
{
    private int width, height;
    private Matrix matrix = new Matrix();
    private Paint paint = new Paint(), textPaint = new Paint(), fillPaint = new Paint(),
        axisNamePaint = new Paint(), legendTextPaint = new Paint();
    private ArrayList<Function> funcs = new ArrayList<Function>();
    private Data next = new Data(), endGraph = new Data();
    private Data graphs[] = {new Data(), new Data(), new Data(), new Data(), new Data()};
    private ArrayList<Data> scatterData = new ArrayList<Data>();
    private ArrayList<String> legendStrings = new ArrayList<String>();
    private boolean isScatterPlot = false;
    private boolean isTrackingData = false;
    private static final int GRAPHS_SIZE = 5;
    private float gwidth = 8;
    private float currentX, currentY;
    private float lastMinX;
    private Scroller scroller;
    private float boundMinY, boundMaxY;
    protected ZoomButtonsController zoomController = new ZoomButtonsController(this); 
    private ZoomTracker zoomTracker = new ZoomTracker();
    private TouchHandler touchHandler;
    private float lastTouchX, lastTouchY;
    
    private static final int
        COL_AXIS = 0xff00a000,
        COL_GRID = 0xff004000,
        COL_TEXT = 0xff00ff00;

    private static final int COL_GRAPH[] = {0xffffffff, 0xff00ffff, 0xffffff00, 0xffff00ff, 0xff80ff80};
    private static final int NUM_GRAPH_COLORS = 5;
    
    private static final int
        COL_ZOOM = 0x40ffffff,
        COL_ZOOM_TEXT1 = 0xd0ffffff,
        COL_ZOOM_TEXT2 = 0x30ffffff;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        // do want to be able to get touch events in windowed mode,
        // so go ahead and create a touch handler
        // -- Andy
        touchHandler = new TouchHandler(this);
        init(context);
    }

    public GraphView(Context context) {
        super(context);
        touchHandler = new TouchHandler(this);
        init(context);
    }

    private void init(Context context) {
        // add a button to the zoom controller for "reattaching"
        // to the live view of new data
        
        Button reattachButton = (Button) LayoutInflater.from(context).inflate(R.layout.track_button, null);

        RelativeLayout.LayoutParams trackParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        trackParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        trackParams.addRule(RelativeLayout.CENTER_VERTICAL);
        trackParams.setMargins(0, 30, 10, 0);
        
        
        RelativeLayout.LayoutParams zoomParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        zoomParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        zoomParams.addRule(RelativeLayout.CENTER_VERTICAL);
        
        reattachButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v)
            {
                isTrackingData = !isTrackingData;
            }
        });
        
        View zoomButs = zoomController.getZoomControls();
        zoomController.getContainer().removeAllViews();
        
        RelativeLayout layout = new RelativeLayout(context);
        layout.addView(zoomButs, zoomParams);
        layout.addView(reattachButton, trackParams);
        
        zoomController.getContainer().addView(layout);
        //zoomController.getContainer().addView(reattachButton, linparams);
        
        zoomController.setOnZoomListener(this);
        scroller = new Scroller(context);
        paint.setAntiAlias(false);
        textPaint.setAntiAlias(true);
        axisNamePaint.setAntiAlias(true);
        legendTextPaint.setAntiAlias(true);
        
    }

    public String captureScreenshot() {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        onDraw(canvas);
        return Util.saveBitmap(bitmap, Grapher.SCREENSHOT_DIR, "calculator");
    }

    private void clearAllGraph() {
        for (int i = 0; i < GRAPHS_SIZE; ++i) {
            graphs[i].clear();
        }
    }

    public void setFunctions(ArrayList<Function> fs) {
        funcs.clear();
        for (Function f : fs) {
            int arity = f.arity();
            if (arity == 0 || arity == 1) {
                funcs.add(f);
            }
        }
        clearAllGraph();
        invalidate();
    }
    
    public void setFunction(Function f) {
        funcs.clear();
        if (f != null) {
            funcs.add(f);
        }
        clearAllGraph();
        invalidate();
    }
    
    public void setScatterDataArray(ArrayList<Data> arrayIn, ArrayList<String> stringsIn)
    {
        scatterData = arrayIn;
        legendStrings = stringsIn;
        
        for (int i=0;i<scatterData.size();i++)
        {
            setScatterData(scatterData.get(i), i);
        }
        setVisibility(View.VISIBLE);
        
        
    }
    
    // Added by Andy to allow for graphing of arbitrary data
    private void setScatterData(Data d, int index) {
        funcs.clear();
        
        if (index >= scatterData.size()) {
            scatterData.add(index, d);
        } else {
            scatterData.set(index, d);
        }
        
        isScatterPlot = true;
        if (isTrackingData == true) {
            final float scale = gwidth / width;
            currentX = d.topX() - width/2 * scale * 0.85f;
            
            //check Y scale
            
            if (scatterData.size() < 2) // don't move the y-axis if there is more than one plot!
            {
                float ywidth = gwidth * height / width;
                float minY = currentY - ywidth/2;
                float maxY = minY + ywidth;
                
                if (d.topY() > maxY || d.topY() < minY) {
    
                    // out of range
                    // jump Y to center data
                    currentY = d.topY();
                }
            }
        }
        clearAllGraph();
        invalidate();
    }
    
    public boolean getTracking() {
        return isTrackingData;
    }
    
    public void setTracking(boolean newTrack) {
        isTrackingData = newTrack;
    }
    
    public void onVisibilityChanged(boolean visible) {
    }

    public void onZoom(boolean zoomIn) {
        if (zoomIn) {
            if (canZoomIn()) {
                gwidth /= 2;
                invalidateGraphs();
            }
        } else {
            if (canZoomOut()) {
                gwidth *= 2;
                invalidateGraphs();
            }
        }
        zoomController.setZoomInEnabled(canZoomIn());
        zoomController.setZoomOutEnabled(canZoomOut());
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onDetachedFromWindow() {
        zoomController.setVisible(false);
        super.onDetachedFromWindow();
    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        width = w;
        height = h;
        clearAllGraph();
        // points = new float[w+w];
    }

    protected void onDraw(Canvas canvas) {
        if (funcs.size() == 0 && isScatterPlot == false) {
            return;
        }
        if (scroller.computeScrollOffset()) {
            final float scale = gwidth / width;
            currentX = scroller.getCurrX() * scale;
            currentY = scroller.getCurrY() * scale;
            if (!scroller.isFinished()) {
                invalidate();
            }
        }

        drawGraph(canvas);
    }

    private float eval(Function f, float x) {
        float v = (float) f.eval(x);
        // Calculator.log("eval " + x + "; " + v); 
        if (v < -10000f) {
            return -10000f;
        }
        if (v > 10000f) {
            return 10000f;
        }
        return v;
    }

    // distance from (x,y) to the line (x1,y1) to (x2,y2), squared, multiplied by 4
    /*
    private float distance(float x1, float y1, float x2, float y2, float x, float y) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float mx = x - x1;
        float my = y - y1;
        float up = dx*my - dy*mx;
        return up*up*4/(dx*dx + dy*dy);
    }
    */
    
    // distance as above when x==(x1+x2)/2. 
    private float distance2(float x1, float y1, float x2, float y2, float y) {
        final float dx = x2 - x1;
        final float dy = y2 - y1;
        final float up = dx * (y1 + y2 - y - y);
        return up*up/(dx*dx + dy*dy);
    }

    private void computeGraph(Function f, float minX, float maxX, float minY, float maxY, Data graph) {
        if (f.arity() == 0) {
            float v = (float) f.eval();
            if (v < -10000f) { v = -10000f; }
            if (v > 10000f) { v = 10000f; }
            graph.clear();
            graph.push(minX, v);
            graph.push(maxX, v);
            return;
        }

        final float scale = width / gwidth;
        final float maxStep = 15.8976f / scale;
        final float minStep = .05f / scale;
        float ythresh = 1/scale;
        ythresh = ythresh * ythresh;
        // next.clear();
        // endGraph.clear();
        if (!graph.empty()) {
            // Calculator.log("last " + lastMinX + " min " + minX);
            if (minX >= lastMinX) {
                graph.eraseBefore(minX);
            } else {
                graph.eraseAfter(maxX);
                maxX = Math.min(maxX, graph.firstX());
                graph.swap(endGraph);
            }
        }
        if (graph.empty()) {
            graph.push(minX, eval(f, minX));
        }
        float leftX, leftY;
        float rightX = graph.topX(), rightY = graph.topY();
        int nEval = 1;
        while (true) {
            leftX = rightX;
            leftY = rightY;
            if (leftX > maxX) {
                break;
            }
            if (next.empty()) {
                float x = leftX + maxStep;
                next.push(x, eval(f, x));
                ++nEval;
            }
            rightX = next.topX();
            rightY = next.topY();
            next.pop();

            if (leftY != leftY && rightY != rightY) { // NaN
                continue;
            }

            float dx = rightX - leftX;
            float middleX = (leftX + rightX) / 2;
            float middleY = eval(f, middleX);
            ++nEval;
            boolean middleIsOutside = (middleY < leftY && middleY < rightY) || (leftY < middleY && rightY < middleY);
            if (dx < minStep) {
            	// Calculator.log("minStep");
                if (middleIsOutside) {
                    graph.push(rightX, Float.NaN);
                }
                graph.push(rightX, rightY);
                continue;
            }
            if (middleIsOutside && ((leftY < minY && rightY > maxY) || (leftY > maxY && rightY < minY))) {
                graph.push(rightX, Float.NaN);
                graph.push(rightX, rightY);
                // Calculator.log("+-inf");
                continue;
            }

            if (!middleIsOutside) {
                /*
                float diff = leftY + rightY - middleY - middleY;
                float dy = rightY - leftY;
                float dx2 = dx*dx;
                float distance = dx2*diff*diff/(dx2+dy*dy);
                */
                // Calculator.log("" + dx + ' ' + leftY + ' ' + middleY + ' ' + rightY + ' ' + distance + ' ' + ythresh);
                if (distance2(leftX, leftY, rightX, rightY, middleY) < ythresh) {
                    graph.push(rightX, rightY);
                    continue;
                }
            }
            next.push(rightX, rightY);
            next.push(middleX, middleY);
            rightX = leftX;
            rightY = leftY;
        }
        if (!endGraph.empty()) {
            graph.append(endGraph);
        }
        long t2 = System.currentTimeMillis();
        // Calculator.log("graph points " + graph.size + " evals " + nEval + " time " + (t2-t1));
        
        next.clear();
        endGraph.clear();
    }
    
    private static Path path = new Path();
    private Path graphToPath(Data graph) {
        boolean first = true;
        int size = graph.size;
        float[] xs = graph.xs;
        float[] ys = graph.ys;
        path.rewind();
        for (int i = 0; i < size; ++i) {
            float y = ys[i];
            float x = xs[i];
            // Calculator.log("path " + x + ' ' + y);
            if (y == y) { // !NaN
                if (first) {
                    path.moveTo(x, y);
                    first = false;
                } else {
                    path.lineTo(x, y);
                }
            } else {
                first = true;
            }
        }
        return path;
    }

    private static final float NTICKS = 15;
    private static float stepFactor(float w) {
        float f = 1;
        while (w / f > NTICKS) {
            f *= 10;
        }
        while (w / f < NTICKS / 10) {
            f /= 10;
        }
        float r = w / f;
        if (r < NTICKS / 5) {
            return f / 5;
        } else if (r < NTICKS / 2) {
            return f / 2;
        } else {
            return f;
        }
    }

    private static StringBuilder b = new StringBuilder();
    private static char[] buf = new char[20];
    private static StringBuilder format(float fv) {
        int pos = 0;
        boolean addDot = false;
        int v = Math.round(fv * 100);
        boolean isNeg = v < 0;
        v = isNeg ? -v : v;
        for (int i = 0; i < 2; ++i) {
            int digit = v % 10;
            v /= 10;
            if (digit != 0 || addDot) {
                buf[pos++] = (char) ('0' + digit);
                addDot = true;
            }
        }
        if (addDot) {
            buf[pos++] = '.';
        }
        if (v == 0) {
            buf[pos++] = '0';
        }
        while (v != 0) {
            buf[pos++] = (char) ('0' + (v % 10));
            v /= 10;
        }
        if (isNeg) {
            buf[pos++] = '-';
        }
        b.setLength(0);
        b.append(buf, 0, pos);
        b.reverse();
        return b;
    }

    private void drawGraph(Canvas canvas) {
        long t1 = System.currentTimeMillis();
        float minX = currentX - gwidth/2;
        float maxX = minX + gwidth;
        float ywidth = gwidth * height / width;
        float minY = currentY - ywidth/2;
        float maxY = minY + ywidth;
        if (minY < boundMinY || maxY > boundMaxY) {
            float halfw = ywidth/2;
            boundMinY = minY - halfw;
            boundMaxY = maxY + halfw;
            clearAllGraph();
        }

        canvas.drawColor(0xff000000);
                
        paint.setStrokeWidth(0);
        paint.setAntiAlias(false);
        paint.setStyle(Paint.Style.STROKE);

        final float h2 = height/2f;
        final float scale = width / gwidth;
        
        float x0 = -minX * scale;
        boolean drawYAxis = true;
        if (x0 < 35) {
            x0 = 35; // used to be 25 -- changed to allow for negative 4 digit numbers
            // drawYAxis = false;
        } else if (x0 > width - 3) {
            x0 = width - 3;
            // drawYAxis = false;
        }
        float y0 = maxY * scale;
        if (y0 < 3) {
            y0 = 3;
        } else if (y0 > height - 15) {
            y0 = height - 15;
        }

        final float tickSize = 3;
        final float y2 = y0 + tickSize;
        paint.setColor(COL_GRID);
        float step = stepFactor(gwidth);
        // Calculator.log("width " + gwidth + " step " + step);
        float v = ((int) (minX/step)) * step;
        textPaint.setColor(COL_TEXT);
        axisNamePaint.setColor(COL_TEXT);
        textPaint.setTextSize(12);
        textPaint.setTextAlign(Paint.Align.CENTER);
        axisNamePaint.setTextSize(28); 
        float stepScale = step * scale;
        
        boolean skip = false, skipFlag = false;
        
        for (float x = (v - minX) * scale; x <= width; x += stepScale, v += step) {
        //for (float x = (v - minX) * scale; x <= width - 105; x += stepScale, v += step) {
            canvas.drawLine(x, 0, x, height, paint);
            if (!(-.001f < v && v < .001f)) {
                StringBuilder b = format(v);
                
                // make sure the labels won't be overlaping
                if (x == (v - minX) * scale && textPaint.measureText(b.toString()) + 4 >= stepScale) // plus 4 because sometimes the first one has a trailing "1" which is small
                {
                    skip = true;
                    if (Math.round(v/step) % 2 == 1)
                    {
                        skipFlag = false;
                    } else {
                        skipFlag = true;
                    }
                }
        
                if (skip == true)
                {
                    if (skipFlag == true)
                    {
                        canvas.drawText(b, 0, b.length(), x, y2+10, textPaint);
                    }
                } else {
                    canvas.drawText(b, 0, b.length(), x, y2+10, textPaint);
                }
                
                
                if (skip == true)
                {
                    skipFlag = !skipFlag;
                }
                //canvas.drawText(b, 0, b.length(), x, y2+30, textPaint);
            }
        }
        //canvas.drawText("seconds", width - 100, y2+30, axisNamePaint);
        
        
        final float x1 = x0 - tickSize;
        v = ((int) (minY/step)) * step;
        textPaint.setTextAlign(Paint.Align.RIGHT);
        for (float y = height - (v - minY) * scale; y >= 0; y -= stepScale, v += step) {
            canvas.drawLine(0, y, width, y, paint);
            if (!(-.001f < v && v < .001f)) {
                StringBuilder b = format(v);
                canvas.drawText(b, 0, b.length(), x1, y+4, textPaint);
            }
        }
        //canvas.drawText(yLabel, x1, 30, axisNamePaint);

        paint.setColor(COL_AXIS);
        if (drawYAxis) {
            canvas.drawLine(x0, 0, x0, height, paint);
        }
        canvas.drawLine(0, y0, width, y0, paint);
        
        matrix.reset();
        matrix.preTranslate(-currentX, -currentY);
        matrix.postScale(scale, -scale);
        matrix.postTranslate(width/2, height/2);

        paint.setStrokeWidth(0);
        paint.setAntiAlias(false);



        if (isScatterPlot == true) {
            
            for (int i=0; i<scatterData.size(); i++) {
                Path path = graphToPath(scatterData.get(i));
                path.transform(matrix);
                paint.setColor(COL_GRAPH[i % NUM_GRAPH_COLORS]); // mod by 5 so if we have > 5 plots we repeat colors
                canvas.drawPath(path, paint);
            }
        } else {
            int n = Math.min(funcs.size(), GRAPHS_SIZE);
            for (int i = 0; i < n; ++i) {
                computeGraph(funcs.get(i), minX, maxX, boundMinY, boundMaxY, graphs[i]);
                Path path = graphToPath(graphs[i]);
                path.transform(matrix);
                paint.setColor(COL_GRAPH[i]);
                canvas.drawPath(path, paint);
            }
        }
        lastMinX = minX;
        
        drawLegend(canvas);
    }
    
    private void drawLegend(Canvas canvas) {
        legendTextPaint.setColor(COL_TEXT);
        
        float maxTextWidth = 0;
        
        int topMargin = 35;
        int lineHeight = 20;
        int lineBuffer = 10;
        int lineLength = 30;
        int textHeightDiv2 = 5;
        
        legendTextPaint.setTextSize(15);
        
        for (int i=0;i<legendStrings.size();i++)
        {
            // draw the names of each string
            
            String b = legendStrings.get(i);
            
            float len = legendTextPaint.measureText(b);
            if (len > maxTextWidth)
            {
                maxTextWidth = len;
            }
            
            canvas.drawText(b, 0, b.length(), width-len, topMargin+lineHeight*i, legendTextPaint);
        }
        
        float startX = width-maxTextWidth - lineBuffer - lineLength;
        
        for (int i=0;i<legendStrings.size();i++)
        {
            // draw a line before each piece of text
            paint.setColor(COL_GRAPH[i % NUM_GRAPH_COLORS]);
            
            float thisY = topMargin+lineHeight*i - textHeightDiv2;
            
            Path mypath = new Path();
            mypath.moveTo(startX, thisY);
            mypath.lineTo(startX + lineLength, thisY);
            
            canvas.drawPath(mypath, paint);
        }
    }

    private boolean canZoomIn() {
        return gwidth > 1f;
    }

    private boolean canZoomOut() {
        return gwidth < 50;
    }

    private void invalidateGraphs() {
        clearAllGraph();
        boundMinY = boundMaxY = 0;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchHandler != null ? touchHandler.onTouchEvent(event) : super.onTouchEvent(event);
    }

    public void onTouchDown(float x, float y) {
        zoomController.setVisible(true);
        if (!scroller.isFinished()) {
            scroller.abortAnimation();
        }
        lastTouchX = x;
        lastTouchY = y;
    }

    public void onTouchMove(float x, float y) {
        float deltaX = x - lastTouchX;
        float deltaY = y - lastTouchY;
        if (deltaX < -1 || deltaX > 1 || deltaY < -1 || deltaY > 1) {
            scroll(-deltaX, deltaY);
            lastTouchX = x;
            lastTouchY = y;
            invalidate();
        }
        isTrackingData = false;
    }

    public void onTouchUp(float x, float y) {
        final float scale = width / gwidth;
        float sx = -touchHandler.velocityTracker.getXVelocity();
        float sy = touchHandler.velocityTracker.getYVelocity();
        final float asx = Math.abs(sx);
        final float asy = Math.abs(sy);
        if (asx < asy / 3) {
            sx = 0;
        } else if (asy < asx / 3) {
            sy = 0;
        }
        scroller.fling(Math.round(currentX * scale),
                       Math.round(currentY * scale),
                       Math.round(sx), Math.round(sy), -10000 + (int)(currentX  * scale),
                       10000 + (int)(currentX * scale), -10000 + (int)(currentY  * scale),
                       10000 + (int)(currentY  * scale));
        invalidate();
    }

    public void onTouchZoomDown(float x1, float y1, float x2, float y2) {
        // compute where the center of the zoom is
        
        final float ywidth = gwidth * height / width;
        
        float ytop = currentY + ywidth/2;
        float middleX = (x1 + x2) / 2;
        float middleY = (y1 + y2) / 2;
        
        float percentX = middleX/width;
        float percentY = middleY/height;
        middleX = (percentX - 0.5f) * gwidth + currentX;
        middleY = ytop - percentY * ywidth;

        isTrackingData = false;

        zoomTracker.start(gwidth, x1, y1, x2, y2, middleX, middleY);
    }

    public void onTouchZoomMove(float x1, float y1, float x2, float y2) {
        if (!zoomTracker.update(x1, y1, x2, y2)) {
            return;
        }
        float targetGwidth = zoomTracker.value;

        if (targetGwidth > .25f && targetGwidth < 200) {
            gwidth = targetGwidth;
            
        }
        
        // zoom in on the spot that is the center of the touchpoint, not just
        // the center of the screen
        final float ywidth = gwidth * height / width;
        
        float middleX = (x1 + x2) / 2;
        float middleY = (y1 + y2) / 2;
        
        float percentX = middleX/width;
        float percentY = middleY/height;

        Log.d("LCMDroid","midX: " + percentX + "midY: " + percentY);
        
        currentX = zoomTracker.getZoomCenterX() - gwidth*percentX + gwidth/2;
        
        currentY = zoomTracker.getZoomCenterY() + ywidth * percentY - ywidth/2;
        
        invalidateGraphs();
        // Calculator.log("zoom redraw");
    }

    private void scroll(float deltaX, float deltaY) {
        final float scale = gwidth / width;
        float dx = deltaX * scale;
        float dy = deltaY * scale;
        final float adx = Math.abs(dx);
        final float ady = Math.abs(dy);
        if (adx < ady / 3) {
            dx = 0;
        } else if (ady < adx / 3) {
            dy = 0;
        }
        currentX += dx;
        currentY += dy;
    }
}