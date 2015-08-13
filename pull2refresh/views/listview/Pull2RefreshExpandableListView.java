
package common.pull2refresh.views.listview;


import common.pull2refresh.common.OverscrollHelper;
import common.pull2refresh.interfaces.EmptyViewMethodAccessor;
import common.pull2refresh.views.Pull2RefreshAdapterViewBase;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ExpandableListView;


public class Pull2RefreshExpandableListView extends
	Pull2RefreshAdapterViewBase <ExpandableListView> {
	public Pull2RefreshExpandableListView (Context context) {
		super(context);
	}


	public Pull2RefreshExpandableListView (Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	public Pull2RefreshExpandableListView (Context context, Mode mode) {
		super(context, mode);
	}


	public Pull2RefreshExpandableListView (Context context, Mode mode,
		AnimationStyle style) {
		super(context, mode, style);
	}


	@Override
	public final Orientation getPull2RefreshScrollDirection () {
		return Orientation.VERTICAL;
	}


	@Override
	protected ExpandableListView createRefreshableView (Context context,
		AttributeSet attrs) {
		final ExpandableListView lv;
		if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
			lv = new InternalExpandableListViewSDK9(context, attrs);
		} else {
			lv = new InternalExpandableListView(context, attrs);
		}
		// Set it to this so it can be used in ListActivity/ListFragment
		lv.setId(android.R.id.list);
		return lv;
	}


	class InternalExpandableListView extends ExpandableListView implements
		EmptyViewMethodAccessor {
		public InternalExpandableListView (Context context, AttributeSet attrs) {
			super(context, attrs);
		}


		@Override
		public void setEmptyView (View emptyView) {
			Pull2RefreshExpandableListView.this.setEmptyView(emptyView);
		}


		@Override
		public void setEmptyViewInternal (View emptyView) {
			super.setEmptyView(emptyView);
		}
	}


	@TargetApi (9)
	final class InternalExpandableListViewSDK9 extends
		InternalExpandableListView {
		public InternalExpandableListViewSDK9 (Context context,
			AttributeSet attrs) {
			super(context, attrs);
		}


		@Override
		protected boolean overScrollBy (int deltaX, int deltaY, int scrollX,
			int scrollY, int scrollRangeX, int scrollRangeY,
			int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
			final boolean returnValue = super.overScrollBy(deltaX, deltaY,
				scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX,
				maxOverScrollY, isTouchEvent);
			// Does all of the hard work...
			OverscrollHelper.overScrollBy(
				Pull2RefreshExpandableListView.this, deltaX, scrollX, deltaY,
				scrollY, isTouchEvent);
			return returnValue;
		}
	}
}
