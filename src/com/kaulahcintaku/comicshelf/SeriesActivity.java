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
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

public class SeriesActivity extends Activity {
	private Item selectedItem;
	
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
    
    private void initialize(final List<Item> items){
        setContentView(R.layout.main);
        
        CoverFlow coverFlow = (CoverFlow) findViewById(R.id.coverflow);
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
        
        ItemAdapter coverImageAdapter =  new ItemAdapter(this, items, true);
        coverFlow.setAdapter(coverImageAdapter);
        for(Item item: items)
        	item.clearImage();
        
        coverFlow.setSpacing(0);
        coverFlow.setAnimationDuration(1000);
        
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