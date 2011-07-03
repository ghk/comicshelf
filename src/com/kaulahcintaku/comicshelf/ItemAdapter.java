package com.kaulahcintaku.comicshelf;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class ItemAdapter extends BaseAdapter {
	
	private int galleryItemBackground;
	private Context context;

	private ImageView[] imageViews;
	private List<Bitmap> bitmaps;

	public ItemAdapter(Context c, List<Item> items, boolean useReflection) {
		context = c;
		imageViews = new ImageView[items.size()];
		this.bitmaps = new ArrayList<Bitmap>();
		for(Item item: items){
			Bitmap originalImage = BitmapFactory.decodeByteArray(item.getImage(), 0, item.getImage().length);
			bitmaps.add(originalImage);
		}
		if(useReflection){
			bitmaps = createReflectedImages(bitmaps);
		}
	}

	private List<Bitmap> createReflectedImages(List<Bitmap> originalImages) {
		// The gap we want between the reflection and the original image
		final int reflectionGap = 4;

		List<Bitmap> results = new ArrayList<Bitmap>();
		int index = 0;
		for (Bitmap originalImage : originalImages) {
			int width = originalImage.getWidth();
			int height = originalImage.getHeight();

			// This will not scale but will flip on the Y axis
			Matrix matrix = new Matrix();
			matrix.preScale(1, -1);

			// Create a Bitmap with the flip matrix applied to it.
			// We only want the bottom half of the image
			Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
					height / 2, width, height / 2, matrix, false);

			// Create a new bitmap with same width but taller to fit reflection
			Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
					(height + height / 2), Config.ARGB_8888);

			// Create a new Canvas with the bitmap that's big enough for
			// the image plus gap plus reflection
			Canvas canvas = new Canvas(bitmapWithReflection);
			// Draw in the original image
			canvas.drawBitmap(originalImage, 0, 0, null);
			// Draw in the gap
			Paint deafaultPaint = new Paint();
			canvas.drawRect(0, height, width, height + reflectionGap,
					deafaultPaint);
			// Draw in the reflection
			canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

			// Create a shader that is a linear gradient that covers the
			// reflection
			Paint paint = new Paint();
			LinearGradient shader = new LinearGradient(0,
					originalImage.getHeight(), 0,
					bitmapWithReflection.getHeight() + reflectionGap,
					0x70ffffff, 0x00ffffff, TileMode.CLAMP);
			// Set the paint to use this shader (linear gradient)
			paint.setShader(shader);
			// Set the Transfer mode to be porter duff and destination in
			paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
			// Draw a rectangle using the paint with our linear gradient
			canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
					+ reflectionGap, paint);
			results.add(bitmapWithReflection);
		}
		return results;
	}

	public int getCount() {
		return imageViews.length;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		// Use this code if you want to load from resources
		ImageView i = new ImageView(context);
		i.setImageBitmap(bitmaps.get(position));
		i.setLayoutParams(new CoverFlow.LayoutParams(130, 130));
		i.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

		// Make sure we set anti-aliasing otherwise we get jaggies
		BitmapDrawable drawable = (BitmapDrawable) i.getDrawable();
		drawable.setAntiAlias(true);
		return i;

		// return mImages[position];
	}

	/**
	 * Returns the size (0.0f to 1.0f) of the views depending on the 'offset' to
	 * the center.
	 */
	public float getScale(boolean focused, int offset) {
		/* Formula: 1 / (2 ^ offset) */
		return Math.max(0, 1.0f / (float) Math.pow(2, Math.abs(offset)));
	}

}