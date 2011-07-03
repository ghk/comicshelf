package com.kaulahcintaku.comicshelf;

import java.io.File;

public class Item {
	private byte[] image;
	private String title;
	private String detail;
	private boolean isSeries;
	
	public Item(byte[] thumb, String cate, String path, boolean isSeries) {
		this.image = thumb;
		this.isSeries = isSeries;
		if(isSeries){
			title = cate;
		}
		else{
			title = new File(path).getName();
			int dotIndex = title.lastIndexOf('.');
			if(dotIndex > 0){
				title = title.substring(0, dotIndex);
			}
		}
	}
	
	public String getTitle() {
		return title;
	}
	
	public byte[] getImage() {
		return image;
	}
	
	public void clearImage(){
		image = null;
	}
	
	public void setDetail(String detail) {
		this.detail = detail;
	}
	
	public String getDetail() {
		return detail;
	}
	
	public boolean isSeries() {
		return isSeries;
	}
	
}
