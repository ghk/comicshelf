/*
 * Copyright (C) 2010 Neil Davies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code is base on the Android Gallery widget and was Created 
 * by Neil Davies neild001 'at' gmail dot com to be a Coverflow widget
 * 
 * @author Neil Davies
 */
package com.kaulahcintaku.comicshelf;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;
import android.widget.ImageView;

public class CoverFlow extends Gallery {

	/**
	 * Graphics Camera used for transforming the matrix of ImageViews
	 */
	private Camera mCamera = new Camera();

	/**
	 * The maximum angle the Child ImageView will be rotated by
	 */
	private int mMaxRotationAngle = 30;
	
	/**
	 * The base z translation
	 */
	private float mZoom = -350.0f;

	/**
	 * The maximum zoom (z translation) on the centre Child
	 * so the center child translations equals to mZoom + mMaxZoom
	 */
	private int mMaxZoom = -100;
	
	private int mSpacing = 0;
	
	private static float MAX_ALLOWED_ZOOM = -200.0f;
	private static float MIN_ALLOWED_ZOOM = -400.0f;
	
	private ScaleGestureDetector gestureDetector;
	

	/**
	 * The Centre of the Coverflow
	 */
	private int mCoveflowCenter;

	public CoverFlow(Context context) {
		super(context);
		this.setStaticTransformationsEnabled(true);
		setZoom(getZoom());
		gestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
	}

	public CoverFlow(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setStaticTransformationsEnabled(true);
		setZoom(getZoom());
		gestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
	}

	public CoverFlow(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setStaticTransformationsEnabled(true);
		setZoom(getZoom());
		gestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
	}

	/**
	 * Get the max rotational angle of the image
	 * 
	 * @return the mMaxRotationAngle
	 */
	public int getMaxRotationAngle() {
		return mMaxRotationAngle;
	}

	/**
	 * Set the max rotational angle of each image
	 * 
	 * @param maxRotationAngle
	 *            the mMaxRotationAngle to set
	 */
	public void setMaxRotationAngle(int maxRotationAngle) {
		mMaxRotationAngle = maxRotationAngle;
	}

	/**
	 * Get the Max zoom of the centre image
	 * 
	 * @return the mMaxZoom
	 */
	public int getMaxZoom() {
		return mMaxZoom;
	}

	/**
	 * Set the max zoom of the centre image
	 * 
	 * @param maxZoom
	 *            the mMaxZoom to set
	 */
	public void setMaxZoom(int maxZoom) {
		mMaxZoom = maxZoom;
	}
	
	
	public void setZoom(float zoom){
		if(zoom > MAX_ALLOWED_ZOOM)
			zoom = MAX_ALLOWED_ZOOM;
		if(zoom < MIN_ALLOWED_ZOOM)
			zoom = MIN_ALLOWED_ZOOM;
		mZoom = zoom;
		int newSpacing = (int)(((zoom - MAX_ALLOWED_ZOOM) / MAX_ALLOWED_ZOOM) * 90) + 20;
		setSpacing(newSpacing);
	}
	
	public float getZoom(){
		return mZoom;
	}
	
	@Override
	public void setSpacing(int spacing) {
		mSpacing = spacing;
		super.setSpacing(spacing);
	}
	
	public int getSpacing(){
		return mSpacing;
	}

	/**
	 * Get the Centre of the Coverflow
	 * 
	 * @return The centre of this Coverflow.
	 */
	private int getCenterOfCoverflow() {
		return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2
				+ getPaddingLeft();
	}

	/**
	 * Get the Centre of the View
	 * 
	 * @return The centre of the given view.
	 */
	private static int getCenterOfView(View view) {
		return view.getLeft() + view.getWidth() / 2;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see #setStaticTransformationsEnabled(boolean)
	 */
	protected boolean getChildStaticTransformation(View child, Transformation t) {

		final int childCenter = getCenterOfView(child);
		final int childWidth = child.getWidth();
		int rotationAngle = 0;

		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);

		if (childCenter == mCoveflowCenter) {
			transformImageBitmap((ImageView) child, t, 0);
		} else {
			rotationAngle = (int) (((float) (mCoveflowCenter - childCenter) / childWidth) * mMaxRotationAngle);
			if (Math.abs(rotationAngle) > mMaxRotationAngle) {
				rotationAngle = (rotationAngle < 0) ? -mMaxRotationAngle
						: mMaxRotationAngle;
			}
			transformImageBitmap((ImageView) child, t, rotationAngle);
		}

		return true;
	}

	/**
	 * This is called during layout when the size of this view has changed. If
	 * you were just added to the view hierarchy, you're called with the old
	 * values of 0.
	 * 
	 * @param w
	 *            Current width of this view.
	 * @param h
	 *            Current height of this view.
	 * @param oldw
	 *            Old width of this view.
	 * @param oldh
	 *            Old height of this view.
	 */
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mCoveflowCenter = getCenterOfCoverflow();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	/**
	 * Transform the Image Bitmap by the Angle passed
	 * 
	 * @param imageView
	 *            ImageView the ImageView whose bitmap we want to rotate
	 * @param t
	 *            transformation
	 * @param rotationAngle
	 *            the Angle by which to rotate the Bitmap
	 */
	private void transformImageBitmap(ImageView child, Transformation t,
			int rotationAngle) {
		mCamera.save();
		final Matrix imageMatrix = t.getMatrix();
		
		final int imageHeight = child.getHeight();
		final int imageWidth = child.getWidth();
		final int rotation = Math.abs(rotationAngle);

		mCamera.translate(0.0f, -10f, mZoom);

		// As the angle of the view gets less, zoom in
		if (rotation < mMaxRotationAngle) {
			float zoomAmount = (float) (mMaxZoom + (rotation * 1.5));
			mCamera.translate(0.0f, 0.0f, zoomAmount);
		}

		mCamera.rotateY(rotationAngle);
		mCamera.getMatrix(imageMatrix);
		imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));
		imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
		mCamera.restore();
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
    	gestureDetector.onTouchEvent(event);
    	return super.onTouchEvent(event);
	}

	
	private class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener{
		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
		}
        			
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			return true;
		}
        			
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float newZoom = getZoom()*detector.getScaleFactor();
			setZoom(newZoom);
			setSelection(getSelectedItemPosition());
			return true;
		}
	}

}
