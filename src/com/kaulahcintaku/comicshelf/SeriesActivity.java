package com.kaulahcintaku.comicshelf;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

public class SeriesActivity extends Activity {
	
	private Item selectedItem;
	private CoverFlow coverFlow;
	
	private ScaleGestureDetector scaleDetector;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        String seriesName = null;
        Bundle data = this.getIntent().getExtras();
		if(data != null){
			seriesName = data.getString("seriesName");
		}
		
		setSelectedItem(null);
        PerfectViewerDatabase db = new PerfectViewerDatabase();
        db.open();
        List<Item> items = seriesName == null 
        	? db.getSeries()
        	: db.getComics(seriesName);
    	db.close();
    	initialize(items);
    }
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(scaleDetector == null)
    		return false;
    	
		return scaleDetector.onTouchEvent(event);
    }
    
    private void initialize(final List<Item> items){
        setContentView(R.layout.main);
        
        ItemAdapter coverImageAdapter =  new ItemAdapter(this, items, true);
        
        coverFlow = (CoverFlow) findViewById(R.id.coverflow);
        coverFlow.setAdapter(coverImageAdapter);
        coverFlow.setSpacing(0);
        coverFlow.setAnimationDuration(1000);
        
        coverFlow.setOnItemSelectedListener(new  AdapterView.OnItemSelectedListener() {
        	@Override
        	public void onItemSelected(AdapterView<?> arg0, View arg1,
        			int position, long arg3) {
        		Item item = (Item)items.get(position);
        		setSelectedItem(item);
        	}
        	public void onNothingSelected(android.widget.AdapterView<?> arg0) {
        		setSelectedItem(null);
        	};
		});
        coverFlow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
        			long arg3) {
        		Item item = (Item)items.get(position);
        		if(item == selectedItem)
	        		openSelectedItem();
        	}
		});
        
        scaleDetector =  new ScaleGestureDetector(this, 
        		new ScaleGestureDetector.OnScaleGestureListener() {
            	
        			float minZoom = -400;
        			float maxZoom = -200;
        		
        			@Override
        			public void onScaleEnd(ScaleGestureDetector detector) {
        			}
        			
        			@Override
        			public boolean onScaleBegin(ScaleGestureDetector detector) {
        				return true;
        			}
        			
        			@Override
        			public boolean onScale(ScaleGestureDetector detector) {
        				float newZoom = coverFlow.getZoom()*detector.getScaleFactor();
        				if(newZoom > maxZoom)
        					newZoom = maxZoom;
        				if(newZoom < minZoom)
        					newZoom = minZoom;
        				coverFlow.setZoom(newZoom);
        				coverFlow.setSelection(coverFlow.getSelectedItemPosition());
        				return true;
        			}
        	});
    }
    
    private boolean openSelectedItem(){
		if(selectedItem == null)
			return false;
		if(selectedItem.isSeries()){
			Intent intent = new Intent(SeriesActivity.this, SeriesActivity.class);
			Bundle data = new Bundle();
			data.putString("seriesName", selectedItem.getTitle());
			intent.putExtras(data);
			startActivityForResult(intent, 0);
		}
		else{
			try{
				File file = new File(selectedItem.getDetail());
				Uri uri = Uri.fromFile(file);
	
				Intent intent = new Intent();
				intent.setClassName("com.rookiestudio.perfectviewer", "com.rookiestudio.perfectviewer.TViewerMain");
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setData(uri);
				
				startActivityForResult(intent, 0);
			} catch(Exception e){
				AlertDialog alertDialog = new AlertDialog.Builder(this)
				.create();
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				alertDialog.setMessage(sw.toString());
				alertDialog.show();
			}
		}
		return true;
    }
    
    private void setSelectedItem(Item item){
    	this.selectedItem = item;
    	String title = "";
    	String detail = "";
    	if(item != null){
    		title = item.getTitle();
    		detail = item.getDetail();
    	}
		((TextView) findViewById(R.id.title)).setText(title);
		((TextView) findViewById(R.id.detail)).setText(detail);
    }
}