package com.hevaisoi.android.control;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.MotionEvent;
import android.widget.Button;

import com.hevaisoi.android.R;


public class ButtonAwesome extends Button{

	private final static String NAME = "FONTAWESOME";
	private static LruCache<String, Typeface> sTypefaceCache = new LruCache<String, Typeface>(12);

	
	public ButtonAwesome(Context context) {
		super(context);
		init();
			
	}
	public ButtonAwesome(Context context,AttributeSet attrs) {
		super(context,attrs);
		init();	
	}
	public ButtonAwesome(Context context, AttributeSet attrs, int defStyle){
		super(context,attrs,defStyle);
		init();	
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (event.getAction()==MotionEvent.ACTION_DOWN){
			this.setBackgroundColor(getResources().getColor(R.color.colorAccent));

		}else if(event.getAction()==MotionEvent.ACTION_UP){

		}
		return  false;
	}

	public void init(){
		Typeface typeface = sTypefaceCache.get(NAME);

		if (typeface == null) {

			typeface = Typeface.createFromAsset(getContext().getAssets(), "fontawesome-webfont.ttf");
			sTypefaceCache.put(NAME, typeface);

		}

		setTypeface(typeface);
	}
}


