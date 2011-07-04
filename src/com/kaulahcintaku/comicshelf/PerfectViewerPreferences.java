package com.kaulahcintaku.comicshelf;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PerfectViewerPreferences {
	
	private static final String  HISTORY_FILE = "perfect_viewer_history";
	
	public File getSharedPrefsFile(String name) {
        //return new File("mnt/sdcard/Android/data/shared_prefs/", name + ".xml");
        //return new File("/data/data/com.example.coverflowv2/shared_prefs/", name + ".xml");
        return new File("/data/data/com.rookiestudio.perfectviewer/shared_prefs/", name + ".xml");
    }
	
	public SharedPreferences getSharedPreferences(String name) throws Exception{
		SharedPreferences sp = null;
		File file = getSharedPrefsFile(name);
		if(!file.exists())
			throw new RuntimeException("file: "+file.getAbsolutePath()+" doesn't exists");
		if(!file.canRead() && !makeFileReadable(file))
			throw new RuntimeException("file is not readable");
		FileInputStream str = null;
		str = new FileInputStream(file);
        Map map = XmlUtilsReadMapXml(str);
        str.close();
        sp = newPreferences(file, Context.MODE_PRIVATE, map);
		return sp;
	}
	
	private boolean makeFileReadable(File file) throws Exception{
		return executeSuCommand("chmod 664 "+file.getAbsolutePath());
	}
	
	public static boolean executeSuCommand(String command) throws Exception{
		Process process = Runtime.getRuntime().exec("su -c sh");
		DataOutputStream os = new DataOutputStream(process.getOutputStream());
		os.writeBytes(command); os.flush();
		os.writeBytes("\n"); os.flush();
		os.writeBytes("exit\n"); os.flush();
		if(process.waitFor() != 0)
			throw new RuntimeException(command+" returns "+process.exitValue());
		return true;
	}
	
	private Map XmlUtilsReadMapXml(FileInputStream fileInputStream) throws Exception{
		Class clazz = Class.forName("com.android.internal.util.XmlUtils");
		Method method = clazz.getMethod("readMapXml", InputStream.class);
		return (Map) method.invoke(null, fileInputStream);
	}
	
	private SharedPreferences newPreferences(File file, int mode, final Map map) throws Exception{
		return new SharedPreferencesImpl(file, mode, map);
	}
	
	public List<String> getLastReads() throws Exception{
		List<String> results = new ArrayList<String>();
		SharedPreferences sp = getSharedPreferences(HISTORY_FILE);
		for(int i = 0; i < 10; i++){
			String value = sp.getString("HistoryFolder"+i, null);
			if(value != null)
				results.add(value);
		}
		return results;
	}
	
	private static final class SharedPreferencesImpl implements SharedPreferences {

        private Map mMap;

        SharedPreferencesImpl(
            File file, int mode, Map initialContents) {
            mMap = initialContents != null ? initialContents : new HashMap();
        }

        public boolean hasFileChanged() {
            synchronized (this) {
            	return false;
            }
        }
        
        public void replace(Map newContents) {
            if (newContents != null) {
                synchronized (this) {
                    mMap = newContents;
                }
            }
        }
        
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        	throw new UnsupportedOperationException();
        }

        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        	throw new UnsupportedOperationException();
        }

        public Map<String, ?> getAll() {
            synchronized(this) {
                //noinspection unchecked
                return new HashMap(mMap);
            }
        }

        public String getString(String key, String defValue) {
            synchronized (this) {
                String v = (String)mMap.get(key);
                return v != null ? v : defValue;
            }
        }

        public int getInt(String key, int defValue) {
            synchronized (this) {
                Integer v = (Integer)mMap.get(key);
                return v != null ? v : defValue;
            }
        }
        public long getLong(String key, long defValue) {
            synchronized (this) {
                Long v = (Long) mMap.get(key);
                return v != null ? v : defValue;
            }
        }
        public float getFloat(String key, float defValue) {
            synchronized (this) {
                Float v = (Float)mMap.get(key);
                return v != null ? v : defValue;
            }
        }
        public boolean getBoolean(String key, boolean defValue) {
            synchronized (this) {
                Boolean v = (Boolean)mMap.get(key);
                return v != null ? v : defValue;
            }
        }

        public boolean contains(String key) {
            synchronized (this) {
                return mMap.containsKey(key);
            }
        }
        
        @Override
        public Editor edit() {
        	throw new UnsupportedOperationException();
        }
	}

}
