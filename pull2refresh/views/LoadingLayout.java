
package nocom.pull2refresh.views;


import com.za.smartlock.R;


import nocom.pull2refresh.common.Utils;
import nocom.pull2refresh.common.ViewCompat;
import nocom.pull2refresh.interfaces.LoadingLayoutInterface;
import nocom.pull2refresh.views.Pull2RefreshBase.Mode;
import nocom.pull2refresh.views.Pull2RefreshBase.Orientation;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


@SuppressLint ("ViewConstructor")
public abstract class LoadingLayout extends FrameLayout implements
	LoadingLayoutInterface {
	static final String LOG_TAG = "Pull2Refresh-LoadingLayout";

	static final Interpolator ANIMATION_INTERPOLATOR = new LinearInterpolator();

	private FrameLayout mInnerLayout;

	protected final ImageView mHeaderImage;

	protected final ProgressBar mHeaderProgress;

	private boolean mUseIntrinsicAnimation;

	private final TextView mHeaderText;

	private final TextView mSubHeaderText;

	protected final Mode mMode;

	protected final Orientation mScrollDirection;

	private CharSequence mPullLabel;

	private CharSequence mRefreshingLabel;

	private CharSequence mReleaseLabel;


	public LoadingLayout (Context context, final Mode mode,
		final Orientation scrollDirection, TypedArray attrs) {
		super(context);
		mMode = mode;
		mScrollDirection = scrollDirection;
		switch (scrollDirection) {
		case HORIZONTAL:
			LayoutInflater.from(context).inflate(
				R.layout.pull_to_refresh_header_horizontal, this);
			break;
		case VERTICAL:
		default:
			LayoutInflater.from(context).inflate(
				R.layout.pull_to_refresh_header_vertical, this);
			break;
		}
		mInnerLayout = (FrameLayout) findViewById(R.id.fl_inner);
		mHeaderText = (TextView) mInnerLayout
			.findViewById(R.id.pull_to_refresh_text);
		mHeaderProgress = (ProgressBar) mInnerLayout
			.findViewById(R.id.pull_to_refresh_progress);
		mSubHeaderText = (TextView) mInnerLayout
			.findViewById(R.id.pull_to_refresh_sub_text);
		mHeaderImage = (ImageView) mInnerLayout
			.findViewById(R.id.pull_to_refresh_image);
		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mInnerLayout
			.getLayoutParams();
		switch (mode) {
		case PULL_FROM_END:
			lp.gravity = scrollDirection == Orientation.VERTICAL ? Gravity.TOP
				: Gravity.LEFT;
			// Load in labels
			mPullLabel = context
				.getString(R.string.pull_to_refresh_from_bottom_pull_label);
			mRefreshingLabel = context
				.getString(R.string.pull_to_refresh_from_bottom_refreshing_label);
			mReleaseLabel = context
				.getString(R.string.pull_to_refresh_from_bottom_release_label);
			break;
		case PULL_FROM_START:
		default:
			lp.gravity = scrollDirection == Orientation.VERTICAL ? Gravity.BOTTOM
				: Gravity.RIGHT;
			// Load in labels
			mPullLabel = context
				.getString(R.string.pull_to_refresh_pull_label);
			mRefreshingLabel = context
				.getString(R.string.pull_to_refresh_refreshing_label);
			mReleaseLabel = context
				.getString(R.string.pull_to_refresh_release_label);
			break;
		}
		if (attrs.hasValue(R.styleable.Pull2Refresh_headerBackground)) {
			Drawable background = attrs
				.getDrawable(R.styleable.Pull2Refresh_headerBackground);
			if (null != background) {
				ViewCompat.setBackground(this, background);
			}
		}
		if (attrs.hasValue(R.styleable.Pull2Refresh_headerTextAppearance)) {
			TypedValue styleID = new TypedValue();
			attrs.getValue(R.styleable.Pull2Refresh_headerTextAppearance,
				styleID);
			setTextAppearance(styleID.data);
		}
		if (attrs.hasValue(R.styleable.Pull2Refresh_subHeaderTextAppearance)) {
			TypedValue styleID = new TypedValue();
			attrs.getValue(R.styleable.Pull2Refresh_subHeaderTextAppearance,
				styleID);
			setSubTextAppearance(styleID.data);
		}
		// Text Color attrs need to be set after TextAppearance attrs
		if (attrs.hasValue(R.styleable.Pull2Refresh_headerTextColor)) {
			ColorStateList colors = attrs
				.getColorStateList(R.styleable.Pull2Refresh_headerTextColor);
			if (null != colors) {
				setTextColor(colors);
			}
		}
		if (attrs.hasValue(R.styleable.Pull2Refresh_headerSubTextColor)) {
			ColorStateList colors = attrs
				.getColorStateList(R.styleable.Pull2Refresh_headerSubTextColor);
			if (null != colors) {
				setSubTextColor(colors);
			}
		}
		// Try and get defined drawable from Attrs
		Drawable imageDrawable = null;
		if (attrs.hasValue(R.styleable.Pull2Refresh_drawable)) {
			imageDrawable = attrs
				.getDrawable(R.styleable.Pull2Refresh_drawable);
		}
		// Check Specific Drawable from Attrs, these overrite the generic
		// drawable attr above
		switch (mode) {
		case PULL_FROM_START:
		default:
			if (attrs.hasValue(R.styleable.Pull2Refresh_drawableStart)) {
				imageDrawable = attrs
					.getDrawable(R.styleable.Pull2Refresh_drawableStart);
			} else if (attrs.hasValue(R.styleable.Pull2Refresh_drawableTop)) {
				Utils.warnDeprecation("drawableTop", "drawableStart");
				imageDrawable = attrs
					.getDrawable(R.styleable.Pull2Refresh_drawableTop);
			}
			break;
		case PULL_FROM_END:
			if (attrs.hasValue(R.styleable.Pull2Refresh_drawableEnd)) {
				imageDrawable = attrs
					.getDrawable(R.styleable.Pull2Refresh_drawableEnd);
			} else if (attrs
				.hasValue(R.styleable.Pull2Refresh_drawableBottom)) {
				Utils.warnDeprecation("drawableBottom", "drawableEnd");
				imageDrawable = attrs
					.getDrawable(R.styleable.Pull2Refresh_drawableBottom);
			}
			break;
		}
		// If we don't have a user defined drawable, load the default
		if (null == imageDrawable) {
			imageDrawable = context.getResources().getDrawable(
				getDefaultDrawableResId());
		}
		// Set Drawable, and save width/height
		setLoadingDrawable(imageDrawable);
		reset();
	}


	public final void setHeight (int height) {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) getLayoutParams();
		lp.height = height;
		requestLayout();
	}


	public final void setWidth (int width) {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) getLayoutParams();
		lp.width = width;
		requestLayout();
	}


	public final int getContentSize () {
		switch (mScrollDirection) {
		case HORIZONTAL:
			return mInnerLayout.getWidth();
		case VERTICAL:
		default:
			return mInnerLayout.getHeight();
		}
	}


	public final void hideAllViews () {
		if (View.VISIBLE == mHeaderText.getVisibility()) {
			mHeaderText.setVisibility(View.INVISIBLE);
		}
		if (View.VISIBLE == mHeaderProgress.getVisibility()) {
			mHeaderProgress.setVisibility(View.INVISIBLE);
		}
		if (View.VISIBLE == mHeaderImage.getVisibility()) {
			mHeaderImage.setVisibility(View.INVISIBLE);
		}
		if (View.VISIBLE == mSubHeaderText.getVisibility()) {
			mSubHeaderText.setVisibility(View.INVISIBLE);
		}
	}


	public final void onPull (float scaleOfLayout) {
		if (!mUseIntrinsicAnimation) {
			onPullImpl(scaleOfLayout);
		}
	}


	public final void Pull2Refresh () {
		if (null != mHeaderText) {
			mHeaderText.setText(mPullLabel);
		}
		// Now call the callback
		Pull2RefreshImpl();
	}


	public final void refreshing () {
		if (null != mHeaderText) {
			mHeaderText.setText(mRefreshingLabel);
		}
		if (mUseIntrinsicAnimation) {
			((AnimationDrawable) mHeaderImage.getDrawable()).start();
		} else {
			// Now call the callback
			refreshingImpl();
		}
		if (null != mSubHeaderText) {
			mSubHeaderText.setVisibility(View.GONE);
		}
	}


	public final void releaseToRefresh () {
		if (null != mHeaderText) {
			mHeaderText.setText(mReleaseLabel);
		}
		// Now call the callback
		releaseToRefreshImpl();
	}


	public final void reset () {
		if (null != mHeaderText) {
			mHeaderText.setText(mPullLabel);
		}
		mHeaderImage.setVisibility(View.VISIBLE);
		if (mUseIntrinsicAnimation) {
			((AnimationDrawable) mHeaderImage.getDrawable()).stop();
		} else {
			// Now call the callback
			resetImpl();
		}
		if (null != mSubHeaderText) {
			if (TextUtils.isEmpty(mSubHeaderText.getText())) {
				mSubHeaderText.setVisibility(View.GONE);
			} else {
				mSubHeaderText.setVisibility(View.VISIBLE);
			}
		}
	}


	@Override
	public void setLastUpdatedLabel (CharSequence label) {
		setSubHeaderText(label);
	}


	public final void setLoadingDrawable (Drawable imageDrawable) {
		// Set Drawable
		mHeaderImage.setImageDrawable(imageDrawable);
		mUseIntrinsicAnimation = (imageDrawable instanceof AnimationDrawable);
		// Now call the callback
		onLoadingDrawableSet(imageDrawable);
	}


	public void setPullLabel (CharSequence pullLabel) {
		mPullLabel = pullLabel;
	}


	public void setRefreshingLabel (CharSequence refreshingLabel) {
		mRefreshingLabel = refreshingLabel;
	}


	public void setReleaseLabel (CharSequence releaseLabel) {
		mReleaseLabel = releaseLabel;
	}


	@Override
	public void setTextTypeface (Typeface tf) {
		mHeaderText.setTypeface(tf);
	}


	public final void showInvisibleViews () {
		if (View.INVISIBLE == mHeaderText.getVisibility()) {
			mHeaderText.setVisibility(View.VISIBLE);
		}
		if (View.INVISIBLE == mHeaderProgress.getVisibility()) {
			mHeaderProgress.setVisibility(View.VISIBLE);
		}
		if (View.INVISIBLE == mHeaderImage.getVisibility()) {
			mHeaderImage.setVisibility(View.VISIBLE);
		}
		if (View.INVISIBLE == mSubHeaderText.getVisibility()) {
			mSubHeaderText.setVisibility(View.VISIBLE);
		}
	}


	/**
	 * Callbacks for derivative Layouts
	 */
	protected abstract int getDefaultDrawableResId ();


	protected abstract void onLoadingDrawableSet (Drawable imageDrawable);


	protected abstract void onPullImpl (float scaleOfLayout);


	protected abstract void Pull2RefreshImpl ();


	protected abstract void refreshingImpl ();


	protected abstract void releaseToRefreshImpl ();


	protected abstract void resetImpl ();


	private void setSubHeaderText (CharSequence label) {
		if (null != mSubHeaderText) {
			if (TextUtils.isEmpty(label)) {
				mSubHeaderText.setVisibility(View.GONE);
			} else {
				mSubHeaderText.setText(label);
				// Only set it to Visible if we're GONE, otherwise VISIBLE
				// will
				// be set soon
				if (View.GONE == mSubHeaderText.getVisibility()) {
					mSubHeaderText.setVisibility(View.VISIBLE);
				}
			}
		}
	}


	private void setSubTextAppearance (int value) {
		if (null != mSubHeaderText) {
			mSubHeaderText.setTextAppearance(getContext(), value);
		}
	}


	private void setSubTextColor (ColorStateList color) {
		if (null != mSubHeaderText) {
			mSubHeaderText.setTextColor(color);
		}
	}


	private void setTextAppearance (int value) {
		if (null != mHeaderText) {
			mHeaderText.setTextAppearance(getContext(), value);
		}
		if (null != mSubHeaderText) {
			mSubHeaderText.setTextAppearance(getContext(), value);
		}
	}


	private void setTextColor (ColorStateList color) {
		if (null != mHeaderText) {
			mHeaderText.setTextColor(color);
		}
		if (null != mSubHeaderText) {
			mSubHeaderText.setTextColor(color);
		}
	}
}
