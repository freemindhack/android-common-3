package nocom.common.view;


import java.util.Date;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.zaz.wifilock.R;


public class RefreshableListView extends ListView implements
	OnScrollListener {
	private final static String TAG = "RefreshableListView";
	private final static int RELEASE_TO_REFRESH = 0;
	private final static int PULL_TO_REFRESH = 1;
	private final static int REFRESHING = 2;
	private final static int DONE = 3;
	private final static int LOADING = 4;

	private final static int RATIO = 3;

	private LayoutInflater inflater;

	private LinearLayout headView;

	private TextView tipsTextview;
	private TextView lastUpdatedTextView;
	private ImageView arrowImageView;
	private ProgressBar progressBar;

	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;

	private boolean isRecored;

	private int headContentHeight;

	private int startY;
	private int firstItemIndex;
	private int state;
	private boolean isBack;
	private OnRefreshListener refreshListener;

	private boolean isRefreshable;
	private boolean isPush;

	private int visibleLastIndex;
	private int visibleItemCount;


	public RefreshableListView (Context context) {
		super(context);
		init(context);
	}


	public RefreshableListView (Context context,
		AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}


	private void init (Context context) {
		inflater = LayoutInflater.from(context);
		headView =
			(LinearLayout) inflater.inflate(
				R.layout.pulllist_head, null);
		arrowImageView =
			(ImageView) headView
				.findViewById(R.id.head_arrowImageView);

		// arrowImageView.setMinimumWidth(70);
		// arrowImageView.setMinimumHeight(50);

		progressBar =
			(ProgressBar) headView
				.findViewById(R.id.head_progressBar);
		tipsTextview =
			(TextView) headView
				.findViewById(R.id.head_tipsTextView);
		lastUpdatedTextView =
			(TextView) headView
				.findViewById(R.id.head_lastUpdatedTextView);

		measureView(headView);
		headContentHeight = headView.getMeasuredHeight();
		// headContentWidth = headView.getMeasuredWidth();

		headView
			.setPadding(0, -1 * headContentHeight, 0, 0);
		headView.invalidate();

		addHeaderView(headView, null, false);
		setOnScrollListener(this);

		animation =
			new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);

		reverseAnimation =
			new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation
			.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);

		state = DONE;
		isRefreshable = false;
		isPush = true;
	}


	public void onScroll (AbsListView arg0,
		int firstVisiableItem, int arg2, int arg3) {
		firstItemIndex = firstVisiableItem;
		visibleLastIndex = firstVisiableItem + arg2 - 1;
		visibleItemCount = arg2;
		if (firstItemIndex == 1 && !isPush) {
			setSelection(0);
		}
	}


	public void setSelectionfoot () {
		this.setSelection(visibleLastIndex
			- visibleItemCount + 1);
	}


	public void onScrollStateChanged (AbsListView arg0,
		int arg1) {

	}


	@Override
	public boolean onTouchEvent (MotionEvent event) {
		try {

			if (isRefreshable) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (firstItemIndex == 0 && !isRecored) {
						isRecored = true;
						isPush = true;
						startY = (int) event.getY();
						Log.v(TAG + "/onTouchEvent",
							"ACTION_DOWN");
					}
					break;

				case MotionEvent.ACTION_UP:
					if (state != REFRESHING
						&& state != LOADING) {
						if (state == DONE) {
							;
						}

						if (state == PULL_TO_REFRESH) {
							state = DONE;
							changeHeaderViewByState();

							Log.v(TAG + "/onTouchEvent",
								"PULL_TO_REFRESH");
						}

						if (state == RELEASE_TO_REFRESH) {
							state = REFRESHING;
							changeHeaderViewByState();
							onRefresh();

							Log.v(TAG + "/onTouchEvent",
								"RELEASE_TO_REFRESH");
						}
					}

					isRecored = false;
					isBack = false;

					break;

				case MotionEvent.ACTION_MOVE:
					int tempY = (int) event.getY();

					if (!isRecored && firstItemIndex == 0) {
						Log.v(TAG,
							"!isRecored && firstItemIndex == 0)");
						isRecored = true;
						startY = tempY;
					}

					if (state != REFRESHING && isRecored
						&& state != LOADING) {
						if (state == RELEASE_TO_REFRESH) {

							setSelection(0);

							if (((tempY - startY) / RATIO < headContentHeight)
								&& (tempY - startY) > 0) {
								state = PULL_TO_REFRESH;
								changeHeaderViewByState();

								Log.v(
									TAG + "/onTouchEvent",
									"鐢辨澗寮�鍒锋柊鐘舵�佽浆鍙樺埌涓嬫媺鍒锋柊鐘舵��");
							}
							// 涓�涓嬪瓙鎺ㄥ埌椤朵簡
							else if (tempY - startY <= 0) {
								state = DONE;
								changeHeaderViewByState();

								Log.v(
									TAG + "/onTouchEvent",
									"鐢辨澗寮�鍒锋柊鐘舵�佽浆鍙樺埌done鐘舵��");
							}
							// 寰�涓嬫媺浜嗭紝鎴栬�呰繕娌℃湁涓婃帹鍒板睆骞曢《閮ㄦ帺鐩杊ead鐨勫湴姝�
							else {
								// 涓嶇敤杩涜鐗瑰埆鐨勬搷浣滐紝鍙敤鏇存柊paddingTop鐨勫�煎氨琛屼簡
							}
						}

						if (state == PULL_TO_REFRESH) {

							setSelection(0);

							// 涓嬫媺鍒板彲浠ヨ繘鍏ELEASE_TO_REFRESH鐨勭姸鎬�
							if ((tempY - startY) / RATIO >= headContentHeight) {
								state = RELEASE_TO_REFRESH;
								isBack = true;

								changeHeaderViewByState();

								Log.v(TAG,
									"鐢眃one鎴栬�呬笅鎷夊埛鏂扮姸鎬佽浆鍙樺埌鏉惧紑鍒锋柊");
							}
							// 涓婃帹鍒伴《浜�
							else if (tempY - startY <= 0) {
								state = DONE;
								changeHeaderViewByState();
								isPush = false;
								Log.v(TAG,
									"鐢盌One鎴栬�呬笅鎷夊埛鏂扮姸鎬佽浆鍙樺埌done鐘舵��");
							}
						}

						// done鐘舵�佷笅
						if (state == DONE) {
							if (tempY - startY > 0) {
								state = PULL_TO_REFRESH;
								changeHeaderViewByState();
							}
						}

						if (state == PULL_TO_REFRESH) {
							headView.setPadding(0, -1
								* headContentHeight
								+ (tempY - startY) / RATIO,
								0, 0);

						}

						if (state == RELEASE_TO_REFRESH) {
							headView.setPadding(0,
								(tempY - startY) / RATIO
									- headContentHeight, 0,
								0);
						}

					}

					break;
				}
			}
		} catch (Exception e) {
			Log.e(TAG + "onTouchEvent",
				"ERROR: " + e.getMessage());
		}

		return super.onTouchEvent(event);
	}


	private void changeHeaderViewByState () {
		try {
			Log.v(TAG, "changeHeaderViewByState");

			switch (state) {
			case RELEASE_TO_REFRESH:
				arrowImageView.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				tipsTextview.setVisibility(View.VISIBLE);
				lastUpdatedTextView
					.setVisibility(View.VISIBLE);

				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(animation);

				tipsTextview
					.setText(getResources().getString(
						R.string.release_to_refresh));

				Log.v(TAG, "RELEASE_TO_REFRESH");
				break;

			case PULL_TO_REFRESH:
				progressBar.setVisibility(View.GONE);
				tipsTextview.setVisibility(View.VISIBLE);
				lastUpdatedTextView
					.setVisibility(View.VISIBLE);
				arrowImageView.clearAnimation();
				arrowImageView.setVisibility(View.VISIBLE);

				if (isBack) {
					isBack = false;
					arrowImageView.clearAnimation();
					arrowImageView
						.startAnimation(reverseAnimation);

					tipsTextview
						.setText(getResources().getString(
							R.string.pull_to_refresh));
				} else {
					tipsTextview
						.setText(getResources().getString(
							R.string.pull_to_refresh));
				}

				Log.v(TAG, "PULL_TO_REFRESH");
				break;

			case REFRESHING:

				headView.setPadding(0, 0, 0, 0);

				progressBar.setVisibility(View.VISIBLE);
				arrowImageView.clearAnimation();
				arrowImageView.setVisibility(View.GONE);
				tipsTextview.setText(getResources()
					.getString(R.string.refreshing));
				lastUpdatedTextView
					.setVisibility(View.VISIBLE);

				Log.v(TAG, "REFRESHING ...");
				break;

			case DONE:
				headView.setPadding(0, -1
					* headContentHeight, 0, 0);

				progressBar.setVisibility(View.GONE);
				arrowImageView.clearAnimation();
				arrowImageView
					.setImageResource(R.drawable.arrow);
				tipsTextview.setText(getResources()
					.getString(R.string.pull_to_refresh));
				lastUpdatedTextView
					.setVisibility(View.VISIBLE);

				Log.v(TAG + "/changeHeaderViewByState",
					"DONE");
				break;
			}
		} catch (Exception e) {
			Log.println(
				Log.ERROR,
				"RefreshableListView/changeHeaderViewByState",
				"ERROR: " + e.getMessage());
		}
	}


	public void setOnRefreshListener (
		OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
		isRefreshable = true;
	}


	public interface OnRefreshListener {
		public void onRefresh ();
	}


	@SuppressWarnings ("deprecation")
	public void onRefreshComplete () {
		try {
			Log.v(TAG, "onRefreshComplete");

			state = DONE;

			lastUpdatedTextView.setText(getResources()
				.getString(R.string.updating)
				+ new Date().toLocaleString());

			this.changeHeaderViewByState();

			invalidateViews();

			setSelection(0);
		} catch (Exception e) {
			Log.println(Log.ERROR, TAG
				+ "/onRefreshComplete",
				"ERROR: " + e.getMessage());
		}
	}


	private void onRefresh () {
		Log.println(Log.VERBOSE, "RefreshableListView",
			"onRefresh");

		if (this.refreshListener != null) {
			this.refreshListener.onRefresh();
		}
	}


	public void clickToRefresh () {
		state = REFRESHING;
		changeHeaderViewByState();
	}


	@SuppressWarnings ("deprecation")
	private void measureView (View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p =
				new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec =
			ViewGroup
				.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec =
				MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec =
				MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}


	@SuppressWarnings ("deprecation")
	public void setAdapter (BaseAdapter adapter) {
		this.lastUpdatedTextView.setText(getResources()
			.getString(R.string.updating)
			+ new Date().toLocaleString());

		super.setAdapter(adapter);
	}
}