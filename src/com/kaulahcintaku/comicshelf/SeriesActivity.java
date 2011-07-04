package com.kaulahcintaku.comicshelf;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

public class SeriesActivity extends Activity {
	
	private Item selectedItem;
	private CoverFlow coverFlow;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
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
    
    private void initialize(final List<Item> items){
        setContentView(R.layout.main);
        
        final ItemAdapter coverImageAdapter =  new ItemAdapter(this, items, true);
        
        coverFlow = (CoverFlow) findViewById(R.id.coverflow);
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
		Intent intent = new Intent(SeriesActivity.this, SeriesActivity.class);
		intent.putExtras(data);
		startActivityForResult(intent, 0);
    }
    
    private void runPerfectViewer(Uri data){
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
}