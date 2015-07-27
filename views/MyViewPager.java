
package common.views;


import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class MyViewPager extends ViewPager {
	public MyViewPager (Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	public MyViewPager (Context context) {
		super(context);
	}


	public void setNoScroll (boolean noScroll) {
		this.noScroll = noScroll;
	}


	@Override
	public void scrollTo (int x, int y) {
		super.scrollTo(x, y);
	}


	@Override
	public boolean onTouchEvent (MotionEvent arg0) {
		/* return false; super.onTouchEvent(arg0); */
		if (noScroll) {
			return false;
		} else {
			return super.onTouchEvent(arg0);
		}
	}


	@Override
	public boolean onInterceptTouchEvent (MotionEvent me) {
		if (noScroll) {
			return false;
		} else {
			return super.onInterceptTouchEvent(me);
		}
	}


	@Override
	public void setCurrentItem (int item, boolean smoothScroll) {
		super.setCurrentItem(item, smoothScroll);
	}


	@Override
	public void setCurrentItem (int item) {
		super.setCurrentItem(item);
	}


	private boolean noScroll = true;
}
