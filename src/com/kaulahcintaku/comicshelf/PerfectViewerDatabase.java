package com.kaulahcintaku.comicshelf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class PerfectViewerDatabase {
	private static final String DB_PATH = "/mnt/sdcard/Android/data/com.rookiestudio.perfectviewer/bookdata.db";
	private static final String TABLE_NAME = "bookfolder";
	private static final String[] TABLE_COLUMNS = new String[]{
		"book_cate", "book_path", "book_cover"
	};
	
	private SQLiteDatabase database;
	
	public PerfectViewerDatabase() {
	}
	
	public void open(){
		if(database != null)
			throw new IllegalStateException("database is open");
		database = SQLiteDatabase.openDatabase(DB_PATH, null, 0);
	}
	
	public void close(){
		if(database == null)
			throw new IllegalStateException("database is closed");
		database.close();
		database = null;
	}
	
	private static final String SERIES_QUERY = 
	"select book_cate, book_path, book_cover "+
	"from bookfolder "+
	"where book_path "+
	"in ( " +
	"	select bp "+
	"	from " +
	"		( " +
	"		select book_index, book_cate, min(book_path) as bp " +
	"		from bookfolder " +
	"		group by book_cate) " +
	" 		) " +
    "group by book_cate";
	
	public List<Item> getSeries(){
		List<Item> results = new ArrayList<Item>();
		Cursor cursor = database.rawQuery(SERIES_QUERY, null);
		while(cursor.moveToNext()){
			Item series = new Item(cursor.getBlob(2), cursor.getString(0), cursor.getString(1), true);
			String sql = "SELECT COUNT(*) FROM " + TABLE_NAME+" WHERE book_cate=?";
		    SQLiteStatement statement = database.compileStatement(sql);
		    statement.bindString(1, series.getTitle());
		    long count = statement.simpleQueryForLong();
		    statement.close();
		    series.setDetail(count+" books");
			results.add(series);
		}
		cursor.close();
		return results;
	}
	
	public List<Item> getComics(String seriesName){
		List<Item> results = new ArrayList<Item>();
		Cursor cursor = database.query(TABLE_NAME, TABLE_COLUMNS, "book_cate=?", new String[]{seriesName}, null, null, "book_path");
		while(cursor.moveToNext()){
			Item comic = new Item(cursor.getBlob(2), cursor.getString(0), cursor.getString(1), false);
			comic.setDetail(cursor.getString(1));
			results.add(comic);
		}
		cursor.close();
		return results;
	}
	
	public List<Item> getLastReads(List<String> lastReadPaths){
		StringBuilder sql = new StringBuilder();
		sql.append("select book_cate, book_path, book_cover from bookfolder where book_path in (");
		if(lastReadPaths.size() > 0){
			for(String string : lastReadPaths) {
				sql.append("'");
				sql.append(string);
				sql.append("',");
		    }
			sql.setLength(sql.length() - 1);
		}
		sql.append(")");
		Cursor cursor = database.rawQuery(sql.toString(), null);
		Map<String, Item> tempTable = new HashMap<String, Item>();
		while(cursor.moveToNext()){
			Item comic = new Item(cursor.getBlob(2), cursor.getString(0), cursor.getString(1), false);
			comic.setDetail(cursor.getString(1));
			tempTable.put(comic.getDetail(), comic);
		}
		cursor.close();
		List<Item> results = new ArrayList<Item>();
		for(String string : lastReadPaths) {
			if(tempTable.containsKey(string))
				results.add(tempTable.get(string));
	    }
		return results;
	}

}
