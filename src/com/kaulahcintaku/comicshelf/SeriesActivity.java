package com.kaulahcintaku.comicshelf;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

public class SeriesActivity extends Activity {
	
	private Item selectedItem;
	private CoverFlow coverFlow;
	private String previousOrientation;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        previousOrientation = getOrientationString();
        
        String seriesName = null;
        boolean readRecent = false;
        Bundle data = this.getIntent().getExtras();
		if(data != null){
			seriesName = data.getString("seriesName");
			readRecent = data.getBoolean("readRecent", false);
		}
		
		setSelectedItem(null);
        PerfectViewerDatabase db = new PerfectViewerDatabase();
        db.open();
        
        List<Item> items = null;
        if(readRecent){
	        try{
	        	items = db.getLastReads(new PerfectViewerPreferences().getLastReads());
	        }
	        catch(Exception e){
	        }
        }
        if(items == null){
	        items = seriesName == null 
	        	? db.getSeries()
	        	: db.getComics(seriesName);
        }
        
    	db.close();
    	initialize(items);
    }
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.recent:
				Bundle data = new Bundle();
				data.putBoolean("readRecent", true);
				runThisActivityAgain(data);
				break;
			case R.id.continue_read:
				runPerfectViewer(null);
				break;
		}
		return true;
	}
	
	@Override
	protected void onDestroy() {
		if(coverFlow != null)
			saveZoom(coverFlow.getZoom());
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
        coverFlow.setZoom(loadZoom());
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		String newOrientation = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ?
				"landscape" : "portrait";
		if(coverFlow != null){
			if(!newOrientation.equals(previousOrientation)){
				saveZoom(coverFlow.getZoom(), previousOrientation);
		        coverFlow.setZoom(loadZoom(newOrientation));
			}
		}
        previousOrientation = newOrientation;
	}
    
    private void initialize(final List<Item> items){
        setContentView(R.layout.main);
        
        final ItemAdapter coverImageAdapter =  new ItemAdapter(this, items, true);
        
        coverFlow = (CoverFlow) findViewById(R.id.coverflow);
        coverFlow.setZoom(loadZoom());
        coverFlow.setAdapter(coverImageAdapter);
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
        
    }
    
    private boolean openSelectedItem(){
		if(selectedItem == null)
			return false;
		if(selectedItem.isSeries()){
			Bundle data = new Bundle();
			data.putString("seriesName", selectedItem.getTitle());
			runThisActivityAgain(data);
		}
		else{
			File file = new File(selectedItem.getDetail());
			Uri uri = Uri.fromFile(file);
			runPerfectViewer(uri);
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
    
    private void runThisActivityAgain(Bundle data){
    	saveZoom(coverFlow.getZoom());
		Intent intent = new Intent(SeriesActivity.this, SeriesActivity.class);
		intent.putExtras(data);
		startActivityForResult(intent, 0);
    }
    
    private void runPerfectViewer(Uri data){
    	saveZoom(coverFlow.getZoom());
		try{
			Intent intent = new Intent();
			intent.setClassName("com.rookiestudio.perfectviewer", "com.rookiestudio.perfectviewer.TViewerMain");
			if(data != null){
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setData(data);
			}
			else{
				intent.setAction(android.content.Intent.ACTION_MAIN);
			}
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
    
    private void saveZoom(float zoom, String orientation){
    	getSharedPreferences("default", MODE_PRIVATE).edit().putFloat(orientation+"_zoom", zoom).commit();
    }
    
    private void saveZoom(float zoom){
    	saveZoom(zoom, getOrientationString());
    }
    
    private float loadZoom(){
    	return loadZoom(getOrientationString());
    }
    
    private float loadZoom(String orientation){
    	return getSharedPreferences("default", MODE_PRIVATE).getFloat(orientation+"_zoom", -200.0f);
    }
    
    private String getOrientationString(){
    	Display display = getWindowManager().getDefaultDisplay();
    	return display.getWidth() > display.getHeight() ? "landscape" : "portrait";
    }
    
}