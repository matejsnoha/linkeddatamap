package info.snoha.matej.linkeddatamap.app.gui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.view.View;
import info.snoha.matej.linkeddatamap.Log;

/**
 * SVG ImageView that supports HW acceleration and ColorFilters
 */
public class SVGImageView extends com.caverock.androidsvg.SVGImageView {

	private Integer maxWidth;
	private BitmapDrawable bitmapDrawable;
	private ColorFilter colorFilter;
	private Canvas vectorToBitmapCanvas = new Canvas();
	
	public SVGImageView(Context context) {
		super(context);
		addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

			@Override
			public void onViewAttachedToWindow(View view) {
				requestLayout();
			}

			@Override
			public void onViewDetachedFromWindow(View view) {
				if (bitmapDrawable != null && bitmapDrawable.getBitmap() != null) {

					// release old bitmaps native heap
					Log.debug("SVG layout recycling old bitmap (view detached)");
					bitmapDrawable.getBitmap().recycle();
					bitmapDrawable = null;
				}
			}
		});
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		colorFilter = cf;
		if (bitmapDrawable != null) {
			bitmapDrawable.setColorFilter(cf);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		if (bitmapDrawable != null) {
			// Log.debug("SVG draw");
			bitmapDrawable.draw(canvas);
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		// Log.debug("SVG layout for " + toString());
	
		Picture picture = getPicture();
		if (picture != null) {
			
			int width = right - left;
			int height = bottom - top;
			
			if (width <= 0 || height <= 0) {
				// will be called again during next layout that changes dimensions
				Log.debug("Target dimensions for SVG unknown");
				return;
			}
			
			Bitmap bitmap = null;
			if (bitmapDrawable != null && bitmapDrawable.getBitmap() != null) {

				if (bitmapDrawable.getBitmap().getWidth() == width
						&& bitmapDrawable.getBitmap().getHeight() == height) {

					// reuse old bitmap if resolution matches
					// Log.debug("SVG layout reusing bitmap");
					bitmap = bitmapDrawable.getBitmap();
					bitmap.eraseColor(Color.TRANSPARENT);

				} else {

					// release old bitmaps native heap
					Log.debug("SVG layout recycling old bitmap");
					bitmapDrawable.getBitmap().recycle();
				}
			}

			if (bitmap == null) {
				Log.debug("SVG layout creating new bitmap " + width + "x" + height);
				bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			}

			vectorToBitmapCanvas.setBitmap(bitmap);
			vectorToBitmapCanvas.drawPicture(picture, new Rect(0, 0, width, height));
			vectorToBitmapCanvas.setBitmap(null);

			bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
			bitmapDrawable.setColorFilter(colorFilter);
			
			// scaling			
			float pictureWidth = picture.getWidth();
			float pictureHeight = picture.getHeight();
			float pictureRatio = pictureWidth / pictureHeight;

			float scale;
			if (width / (float) height > pictureRatio) {
				// bitmap is wider than picture, use picture height
				scale = height / pictureHeight;
			} else {
				// bitmap is taller than picture, use picture width
				scale = width / pictureWidth;
			}
			
			int scaledWidth = Math.round(pictureWidth * scale);
			int scaledHeight = Math.round(pictureHeight * scale); 
			
			bitmapDrawable.setBounds(new Rect(
					0 + (width - scaledWidth) / 2, 
					0 + (height - scaledHeight) / 2, 
					width - (width - scaledWidth) / 2, 
					height - (height - scaledHeight) / 2));
		}
	}
	
	@Override
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
		requestLayout();
	}
	
	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

		final Drawable drawable = this.getDrawable();

		if (drawable != null) {
			
			// image
			float imageAspectRatio = (float) drawable.getIntrinsicHeight()
					/ drawable.getIntrinsicWidth();

			// layout hint
			int layoutHeight = View.MeasureSpec.getSize(heightMeasureSpec);
			int layoutWidth = View.MeasureSpec.getSize(widthMeasureSpec);
			if (maxWidth != null && layoutWidth > maxWidth) {
				layoutWidth = maxWidth;
		    }
			
			float layoutAspectRatio = layoutHeight / (float) layoutWidth;

			// resulting dimensions
			int height;
			int width;
			
			// height not available from layout, fit to width
			if (layoutHeight == 0) {
				
				width = layoutWidth;
				height = (int) Math.ceil(width * imageAspectRatio);
				
			} else {

				// showing portrait image on landscape layout
				if (imageAspectRatio > layoutAspectRatio) {
					
					height = layoutHeight;
					width = (int) Math.ceil(height / imageAspectRatio);
				} 
				// showing landscape image on portrait layout
				else {
					
					width = layoutWidth;
					height = (int) Math.ceil(width * imageAspectRatio);
				}
			}

			// scale = width / (float) drawable.getIntrinsicWidth();
			width += getPaddingLeft() + getPaddingRight();
			height += getPaddingTop() + getPaddingBottom();
			this.setMeasuredDimension(width, height);

		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
	
	public Picture getPicture() {
		
		Drawable drawable = getDrawable();
		if (drawable instanceof PictureDrawable) {
			return ((PictureDrawable) drawable).getPicture();
		} else { 
			return null;
		}
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		requestLayout();
	}
}
