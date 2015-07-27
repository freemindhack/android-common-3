
package common.pull2refresh.views.listview;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Scroller;


import com.za.smartlock.manufacturer.R;
import common.pull2refresh.common.LoadingLayoutProxy;
import common.pull2refresh.common.OverscrollHelper;
import common.pull2refresh.interfaces.EmptyViewMethodAccessor;
import common.pull2refresh.views.LoadingLayout;
import common.pull2refresh.views.Pull2RefreshAdapterViewBase;


public class Pull2RefreshListView extends
	Pull2RefreshAdapterViewBase <ListView> {
	/* 滑动删除方向的枚举值 */
	public static enum ItemSlideDirection {
		RIGHT, LEFT;
	}


	private LoadingLayout mHeaderLoadingView;

	private LoadingLayout mFooterLoadingView;

	private FrameLayout mLvFooterLoadingFrame;

	private boolean mListViewExtrasEnabled;


	public Pull2RefreshListView (Context context) {
		super(context);
	}


	public Pull2RefreshListView (Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	public Pull2RefreshListView (Context context, Mode mode) {
		super(context, mode);
	}


	public Pull2RefreshListView (Context context, Mode mode,
		AnimationStyle style) {
		super(context, mode, style);
	}


	@Override
	public final Orientation getPull2RefreshScrollDirection () {
		return Orientation.VERTICAL;
	}


	@Override
	protected void onRefreshing (final boolean doScroll) {
		/**
		 * If we're not showing the Refreshing view, or the list is empty, the
		 * the header/footer views won't show so we use the normal method.
		 */
		ListAdapter adapter = mRefreshableView.getAdapter();
		if (!mListViewExtrasEnabled || !getShowViewWhileRefreshing()
			|| null == adapter || adapter.isEmpty()) {
			super.onRefreshing(doScroll);
			return;
		}
		super.onRefreshing(false);
		final LoadingLayout origLoadingView, listViewLoadingView, oppositeListViewLoadingView;
		final int selection, scrollToY;
		switch (getCurrentMode()) {
		case MANUAL_REFRESH_ONLY:
		case PULL_FROM_END:
			origLoadingView = getFooterLayout();
			listViewLoadingView = mFooterLoadingView;
			oppositeListViewLoadingView = mHeaderLoadingView;
			selection = mRefreshableView.getCount() - 1;
			scrollToY = getScrollY() - getFooterSize();
			break;
		case PULL_FROM_START:
		default:
			origLoadingView = getHeaderLayout();
			listViewLoadingView = mHeaderLoadingView;
			oppositeListViewLoadingView = mFooterLoadingView;
			selection = 0;
			scrollToY = getScrollY() + getHeaderSize();
			break;
		}
		// Hide our original Loading View
		origLoadingView.reset();
		origLoadingView.hideAllViews();
		// Make sure the opposite end is hidden too
		oppositeListViewLoadingView.setVisibility(View.GONE);
		// Show the ListView Loading View and set it to refresh.
		listViewLoadingView.setVisibility(View.VISIBLE);
		listViewLoadingView.refreshing();
		if (doScroll) {
			// We need to disable the automatic visibility changes for now
			disableLoadingLayoutVisibilityChanges();
			// We scroll slightly so that the ListView's header/footer is at
			// the
			// same Y position as our normal header/footer
			setHeaderScroll(scrollToY);
			// Make sure the ListView is scrolled to show the loading
			// header/footer
			mRefreshableView.setSelection(selection);
			// Smooth scroll as normal
			smoothScrollTo(0);
		}
	}


	@Override
	protected void onReset () {
		/**
		 * If the extras are not enabled, just call up to super and return.
		 */
		if (!mListViewExtrasEnabled) {
			super.onReset();
			return;
		}
		final LoadingLayout originalLoadingLayout, listViewLoadingLayout;
		final int scrollToHeight, selection;
		final boolean scrollLvToEdge;
		switch (getCurrentMode()) {
		case MANUAL_REFRESH_ONLY:
		case PULL_FROM_END:
			originalLoadingLayout = getFooterLayout();
			listViewLoadingLayout = mFooterLoadingView;
			selection = mRefreshableView.getCount() - 1;
			scrollToHeight = getFooterSize();
			scrollLvToEdge = Math.abs(mRefreshableView
				.getLastVisiblePosition() - selection) <= 1;
			break;
		case PULL_FROM_START:
		default:
			originalLoadingLayout = getHeaderLayout();
			listViewLoadingLayout = mHeaderLoadingView;
			scrollToHeight = -getHeaderSize();
			selection = 0;
			scrollLvToEdge = Math.abs(mRefreshableView
				.getFirstVisiblePosition() - selection) <= 1;
			break;
		}
		// If the ListView header loading layout is showing, then we need to
		// flip so that the original one is showing instead
		if (listViewLoadingLayout.getVisibility() == View.VISIBLE) {
			// Set our Original View to Visible
			originalLoadingLayout.showInvisibleViews();
			// Hide the ListView Header/Footer
			listViewLoadingLayout.setVisibility(View.GONE);
			/**
			 * Scroll so the View is at the same Y as the ListView
			 * header/footer, but only scroll if: we've pulled to refresh,
			 * it's positioned correctly
			 */
			if (scrollLvToEdge && getState() != State.MANUAL_REFRESHING) {
				mRefreshableView.setSelection(selection);
				setHeaderScroll(scrollToHeight);
			}
		}
		// Finally, call up to super
		super.onReset();
	}


	@Override
	protected LoadingLayoutProxy createLoadingLayoutProxy (
		final boolean includeStart, final boolean includeEnd) {
		LoadingLayoutProxy proxy = super.createLoadingLayoutProxy(
			includeStart, includeEnd);
		if (mListViewExtrasEnabled) {
			final Mode mode = getMode();
			if (includeStart && mode.showHeaderLoadingLayout()) {
				proxy.addLayout(mHeaderLoadingView);
			}
			if (includeEnd && mode.showFooterLoadingLayout()) {
				proxy.addLayout(mFooterLoadingView);
			}
		}
		return proxy;
	}


	protected ListView createListView (Context context, AttributeSet attrs) {
		final ListView lv;
		if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
			lv = new InternalListViewSDK9(context, attrs);
		} else {
			lv = new InternalListView(context, attrs);
		}
		return lv;
	}


	@Override
	protected ListView createRefreshableView (Context context,
		AttributeSet attrs) {
		ListView lv = createListView(context, attrs);
		// Set it to this so it can be used in ListActivity/ListFragment
		lv.setId(android.R.id.list);
		return lv;
	}


	@Override
	protected void handleStyledAttributes (TypedArray a) {
		super.handleStyledAttributes(a);
		mListViewExtrasEnabled = a.getBoolean(
			R.styleable.Pull2Refresh_listViewExtrasEnabled, true);
		if (mListViewExtrasEnabled) {
			final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_HORIZONTAL);
			// Create Loading Views ready for use later
			FrameLayout frame = new FrameLayout(getContext());
			mHeaderLoadingView = createLoadingLayout(getContext(),
				Mode.PULL_FROM_START, a);
			mHeaderLoadingView.setVisibility(View.GONE);
			frame.addView(mHeaderLoadingView, lp);
			mRefreshableView.addHeaderView(frame, null, false);
			mLvFooterLoadingFrame = new FrameLayout(getContext());
			mFooterLoadingView = createLoadingLayout(getContext(),
				Mode.PULL_FROM_END, a);
			mFooterLoadingView.setVisibility(View.GONE);
			mLvFooterLoadingFrame.addView(mFooterLoadingView, lp);
			/**
			 * If the value for Scrolling While Refreshing hasn't been
			 * explicitly set via XML, enable Scrolling While Refreshing.
			 */
			if (!a
				.hasValue(R.styleable.Pull2Refresh_scrollingWhileRefreshingEnabled)) {
				setScrollingWhileRefreshingEnabled(true);
			}
		}
	}


	@TargetApi (9)
	final class InternalListViewSDK9 extends InternalListView {
		public InternalListViewSDK9 (Context context, AttributeSet attrs) {
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
			OverscrollHelper.overScrollBy(Pull2RefreshListView.this, deltaX,
				scrollX, deltaY, scrollY, isTouchEvent);
			return returnValue;
		}
	}


	protected class InternalListView extends ListView implements
		EmptyViewMethodAccessor {
		private boolean mAddedLvFooter = false;

		private ItemSlideDirection itemSlideDirection;

		private boolean savedLongClickable = true;

		private boolean hasSavedLongClickable = false;


		public InternalListView (Context context, AttributeSet attrs) {
			super(context, attrs);

			this.initializeItemSlide(context);
		}


		@SuppressWarnings ("deprecation")
		private void initializeItemSlide (Context context) {
			this.screenWidth = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getWidth();
			this.scroller = new Scroller(context);
			this.touchSlop = ViewConfiguration.get(getContext())
				.getScaledTouchSlop();
		}


		@Override
		protected void dispatchDraw (Canvas canvas) {
			/**
			 * This is a bit hacky, but Samsung's ListView has got a bug in it
			 * when using Header/Footer Views and the list is empty. This
			 * masks the issue so that it doesn't cause an FC. See Issue #66.
			 */
			try {
				super.dispatchDraw(canvas);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}


		/**
		 * 分发事件，主要做的是判断点击的是那个item, 以及通过postDelayed来设置响应左右滑动事件
		 */
		@Override
		public boolean dispatchTouchEvent (MotionEvent ev) {
			// Log.v(TAG + ":InternalListView", "dispatchTouchEvent");
			/**
			 * This is a bit hacky, but Samsung's ListView has got a bug in it
			 * when using Header/Footer Views and the list is empty. This
			 * masks the issue so that it doesn't cause an FC. See Issue #66.
			 */
			try {
				switch (ev.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					if (!scroller.isFinished()) {
						Log.w(
							TAG
								+ ":InternalListView:dispatchTouchEvent:ACTION_DOWN",
							"NOT FINISH");

						return super.dispatchTouchEvent(ev);
					}

					int downX = (int) ev.getX();
					int downY = (int) ev.getY();

					int slidePosition = this.pointToPosition(downX, downY);

					/* 无效的position, 不做任何处理 */
					if (slidePosition == AdapterView.INVALID_POSITION) {
						Log.w(TAG + ":InternalListView:dispatchTouchEvent",
							"INVALID_POSITION");

						this.releaseSlideAnchor();

						return super.dispatchTouchEvent(ev);
					} else {
						this.setSlideAnchor(ev, slidePosition);
					}
					break;
				}

				case MotionEvent.ACTION_MOVE: {
					if (null != this.velocityTracker) {
						this.velocityTracker.addMovement(ev);
					}

					if (null == this.velocityTracker) {
						break;
					}

					int sv = getScrollVelocity();
					if (-1 == sv) {
						break;
					}

					try {
						if (Math.abs(sv) > SNAP_VELOCITY
							|| (Math.abs(ev.getX() - this.anchorX) > touchSlop && Math
								.abs(ev.getY() - this.anchorY) < touchSlop)) {
							Log.v(TAG + ":InternalListView", "is slide");
							this.isSlide = true;
							if (!this.hasSavedLongClickable) {
								this.hasSavedLongClickable = true;

								this.savedLongClickable = this
									.isLongClickable();
							}
							this.setLongClickable(false);
						} else {
							Log.v(TAG + ":InternalListView", "not slide");
						}
					} catch (Exception e) {
						;
					}
					break;
				}

				case MotionEvent.ACTION_UP: {
					int downX = (int) ev.getX();
					int downY = (int) ev.getY();

					int slidePosition = this.pointToPosition(downX, downY);

					if (slidePosition == AdapterView.INVALID_POSITION) {
						Log.w(
							TAG
								+ ":InternalListView:dispatchTouchEvent:ACTION_UP",
							"INVALID_POSITION");

						this.releaseSlideAnchor();
					}
				}
					break;
				}

				return super.dispatchTouchEvent(ev);
				// return super.dispatchTouchEvent(ev);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
				return false;
			}
		}


		@Override
		public void setAdapter (ListAdapter adapter) {
			// Add the Footer View at the last possible moment
			if (null != mLvFooterLoadingFrame && !mAddedLvFooter) {
				addFooterView(mLvFooterLoadingFrame, null, false);
				mAddedLvFooter = true;
			}
			super.setAdapter(adapter);
		}


		@Override
		public void setEmptyView (View emptyView) {
			Pull2RefreshListView.this.setEmptyView(emptyView);
		}


		@Override
		public void setEmptyViewInternal (View emptyView) {
			super.setEmptyView(emptyView);
		}


		@Override
		public boolean onTouchEvent (MotionEvent ev) {
			Log.v(TAG, "onTouchEvent");

			if (this.isSlide
				&& this.anchorView != AdapterView.INVALID_POSITION) {
				addVelocityTracker(ev);
				final int action = ev.getAction();
				int x = (int) ev.getX();
				switch (action) {
				case MotionEvent.ACTION_MOVE:
					int deltaX = (int) (this.anchorX - x);
					this.anchorX = x;

					/* 手指拖动itemView滚动, deltaX大于0向左滚动，小于0向右滚 */
					this.itemView.scrollBy(deltaX, 0);
					break;
				case MotionEvent.ACTION_UP:
					int velocityX = getScrollVelocity();
					if (velocityX > SNAP_VELOCITY) {
						scrollRight();
					} else if (velocityX < -SNAP_VELOCITY) {
						scrollLeft();
					} else {
						scrollByDistanceX();
					}

					/* 手指离开的时候就不响应左右滚动 */
					this.isSlide = false;
					break;
				}

				return true;/* 拖动的时候ListView不滚动 */
			}

			// 否则直接交给ListView来处理onTouchEvent事件
			return super.onTouchEvent(ev);
		}


		@Override
		public void computeScroll () {
			try {
				/* 调用startScroll的时候scroller.computeScrollOffset()返回true， */
				if (this.scroller.computeScrollOffset()) {
					/* 让ListView item根据当前的滚动偏移量进行滚动 */
					this.itemView.scrollTo(this.scroller.getCurrX(),
						this.scroller.getCurrY());

					postInvalidate();

					/* 滚动动画结束的时候调用回调接口 */
					if (this.scroller.isFinished()) {
						if (onItemSlideListener == null) {
							throw new NullPointerException(
								"onItemSlideListener is null");
						} else {
							onItemSlideListener.onItemSlided(
								this.itemSlideDirection, this.anchorView,
								this.getScrollVelocity());
						}

						this.itemView.scrollTo(0, 0);
						this.releaseSlideAnchor();
						postInvalidate();
					}
				}
			} catch (Exception e) {
				;
			}
		}


		/**
		 * 往右滑动，getScrollX()返回的是左边缘的距离，就是以View左边缘为原点到开始滑动的距离，所以向右边滑动为负值
		 */
		private void scrollRight () {
			this.itemSlideDirection = ItemSlideDirection.RIGHT;
			final int delta = (screenWidth + itemView.getScrollX());
			/* 调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item */
			scroller.startScroll(itemView.getScrollX(), 0, -delta, 0,
				Math.abs(delta));
			postInvalidate();/* 刷新itemView */
		}


		/**
		 * 向左滑动，根据上面我们知道向左滑动为正值
		 */
		private void scrollLeft () {
			this.itemSlideDirection = ItemSlideDirection.LEFT;
			final int delta = (screenWidth - itemView.getScrollX());
			/* 调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item */
			scroller.startScroll(itemView.getScrollX(), 0, delta, 0,
				Math.abs(delta));
			postInvalidate();/* 刷新itemView */
		}


		/**
		 * 根据手指滚动itemView的距离来判断是滚动到开始位置还是向左或者向右滚动
		 */
		private void scrollByDistanceX () {
			boolean needRestore = false;

			/* 如果向左滚动的距离大于屏幕的三分之一，就让其删除 */
			if (itemView.getScrollX() >= 2 * (screenWidth / 3)) {
				scrollLeft();
			} else if (itemView.getScrollX() <= -2 * (screenWidth / 3)) {
				scrollRight();
			} else {
				/* 滚回到原始位置,为了偷下懒这里是直接调用scrollTo滚动 */
				// itemView.scrollTo(0, 0);
				if (itemView.getScrollX() >= scrollWidth) {
					itemView.scrollTo(scrollWidth
						* (itemView.getScrollX() / scrollWidth), 0);

					needRestore = true;
				} else if (itemView.getScrollX() <= -scrollWidth) {
					itemView.scrollTo(
						-scrollWidth
							* (-1 * itemView.getScrollX() / scrollWidth), 0);

					needRestore = true;
				} else {
					itemView.scrollTo(0, 0);
				}

				if (this.hasSavedLongClickable) {
					this.setLongClickable(this.savedLongClickable);

					this.hasSavedLongClickable = false;
				}
			}

			if (needRestore) {

				postDelayed(new Runnable() {

					@Override
					public void run () {
						try {
							itemView.scrollTo(0, 0);
						} catch (Exception e) {
							;
						}
					}

				}, 2000);
			}
		}


		private void addVelocityTracker (MotionEvent event) {
			if (this.velocityTracker == null) {
				this.velocityTracker = VelocityTracker.obtain();
			}

			this.velocityTracker.addMovement(event);
		}


		/**
		 * ListView的item
		 */
		private View itemView;

		/**
		 * 是否响应滑动，默认为不响应
		 */
		private boolean isSlide = false;


		@SuppressLint ("Recycle")
		private void setSlideAnchor (MotionEvent event, int anchorView) {
			Log.v(TAG + ":InternalListView", "setSlideAnchor");

			this.anchorView = anchorView;

			this.itemView = getChildAt(anchorView
				- this.getFirstVisiblePosition());

			this.anchorX = event.getX();
			this.anchorY = event.getY();

			if (this.velocityTracker == null) {
				this.velocityTracker = VelocityTracker.obtain();
			} else {
				this.recycleVelocityTracker();
				this.velocityTracker = VelocityTracker.obtain();
			}

			this.velocityTracker.addMovement(event);
		}


		private void releaseSlideAnchor () {
			this.anchorView = -1;
			this.itemView = null;
			this.anchorX = 0.0f;
			this.anchorY = 0.0f;

			this.recycleVelocityTracker();

			if (this.hasSavedLongClickable) {
				this.setLongClickable(this.savedLongClickable);

				this.hasSavedLongClickable = false;
			}
		}


		private void recycleVelocityTracker () {
			Log.v(TAG, "recycleVelocityTracker");

			if (this.velocityTracker != null) {
				this.velocityTracker.recycle();
				this.velocityTracker = null;
			}
		}


		private float anchorX = 0.0f;

		private float anchorY = 0.0f;


		/**
		 * 获取X方向的滑动速度,大于0向右滑动，反之向左
		 */
		public int getScrollVelocity () {
			try {
				this.velocityTracker.computeCurrentVelocity(1000);
				int velocity = (int) this.velocityTracker.getXVelocity();
				return velocity;
			} catch (Exception e) {
				Log.e(TAG + ":getScrollVelocity", "ERROR: " + e.getMessage());
				return 0;
			}
		}


		private int anchorView = -1;

		/* 屏幕宽度 */
		protected int screenWidth;

		/* 滑动类 */
		protected Scroller scroller;

		private static final int SNAP_VELOCITY = 600;

		/* 速度追踪对象 */
		public VelocityTracker velocityTracker;

		/* 用户滑动的最小距离 */
		private int touchSlop;
	}


	/**
	 * 当ListView item滑出屏幕，回调这个接口 我们需要在回调方法removeItem()中移除该Item,然后刷新ListView
	 */
	public static interface OnItemSlideListener {
		public void onItemSlided (ItemSlideDirection direction, int position,
			int speed);
	}


	public void setOnItemSlideListener (
		OnItemSlideListener onItemSlideListener) {
		this.onItemSlideListener = onItemSlideListener;
	}


	private int scrollWidth = 96;


	public void setEachScrollWidth (int scrollWidth) {
		this.scrollWidth = scrollWidth;
	}


	public int getEachScrollWidth () {
		return this.scrollWidth;
	}


	/**
	 * 移除item后的回调接口
	 */
	private OnItemSlideListener onItemSlideListener;

	private final String TAG = "Pull2RefreshListView";
}
