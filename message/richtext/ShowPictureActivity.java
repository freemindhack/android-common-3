
package common.message.richtext;


import java.util.ArrayList;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.za.smartlock.customer.R;
import common.datastructure.MyArrayList;
import common.message.richtext.MyImage.ImgData;
import common.utils.UIUtils;


public class ShowPictureActivity extends Activity {
	private ViewPager pager;

	private MyPageAdapter adapter;

	RelativeLayout photo_relativeLayout;


	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UIUtils.transparentStatus(getWindow());
		if (UIUtils.isPortrait(getApplicationContext())) {
			UIUtils.transparentNavigation(getWindow());
		}
		setContentView(R.layout.activity_show_picture);

		photo_relativeLayout = (RelativeLayout) findViewById(R.id.photo_relativeLayout);
		photo_relativeLayout.setBackgroundColor(0x70000000);

		MyImage mi = MyImage.getInstance(true, true);
		int sz = mi.imgData.size();
		if (sz > 0) {
			this.showDatas.addAll(mi.imgData);
		}

		Button btnASPCancel = (Button) findViewById(R.id.btnASPCancel);
		btnASPCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick (View v) {
				finish();
			}
		});

		Button btnASPReadyDelete = (Button) findViewById(R.id.btnASPReadyDelete);
		btnASPReadyDelete.setOnClickListener(new View.OnClickListener() {
			public void onClick (View v) {
				if (ShowPictureActivity.this.viewsList.size() <= 0) {
					finish();
				} else {
					int d = ShowPictureActivity.this.currentPage;

					ImgData r = ShowPictureActivity.this.showDatas
						.removeByIndex(d);

					if (null == r) {
						Log.w(TAG + ":btnAPReadyDelete:onClick",
							"no found to delete");
						return;
					}

					String rs = r.getCompressedPath();

					Log.v(
						TAG + ":btnAPReadyDelete",
						"ready to rm compressed: " + d + " path: " + rs
							+ "lv sz: "
							+ ShowPictureActivity.this.viewsList.size());

					ShowPictureActivity.this.deletedViews
						.add(ShowPictureActivity.this.viewsList.remove(d));
					adapter.notifyDataSetChanged();

					ShowPictureActivity.this.compressed2DelPaths.add(rs);

					if (ShowPictureActivity.this.viewsList.size() <= 0) {
						ShowPictureActivity.this.finishResult();
					}
				}
			}
		});

		Button btnASPDone = (Button) findViewById(R.id.btnASPDone);
		btnASPDone.setOnClickListener(new View.OnClickListener() {
			public void onClick (View v) {
				int n = ShowPictureActivity.this.compressed2DelPaths.size();

				if (n > 0) {
					ShowPictureActivity.this.finishResult();
				} else {
					ShowPictureActivity.this.finish();
				}
			}
		});

		this.tvASPDummyNavigation = (TextView) findViewById(R.id.tvASPDummyNavigation);

		pager = (ViewPager) findViewById(R.id.viewpager);
		pager.setOnPageChangeListener(pageChangeListener);

		int n = this.showDatas.size();
		Log.v(TAG + ":onCreate", "show total: " + n);
		this.viewsList = new ArrayList <View>();
		for (int i = 0; i < n; i++) {
			initListViews(this.showDatas.get(i).getBmp());
		}

		adapter = new MyPageAdapter(viewsList);
		pager.setAdapter(adapter);
		Intent intent = getIntent();
		int id = intent.getIntExtra("ID", 0);
		pager.setCurrentItem(id);
	}


	@Override
	protected void onResume () {
		Log.v(TAG, "onResume");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
			&& UIUtils.checkDeviceHasNavigationBar(this)
			&& UIUtils.isPortrait(getApplicationContext())) {
			this.tvASPDummyNavigation.setVisibility(View.VISIBLE);
		} else {
			this.tvASPDummyNavigation.setVisibility(View.GONE);
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			UIUtils.dotNavigation(getWindow());
		}

		super.onResume();
	}


	private void finishResult () {
		Log.v(TAG + ":finishResult", "feedback rm compressed: sz: "
			+ ShowPictureActivity.this.compressed2DelPaths.size());

		Bundle bundle = new Bundle();
		ArrayList <String> feedbackDeleted = ShowPictureActivity.this.compressed2DelPaths;
		bundle.putStringArrayList(MessageConfig.GETKEY_THUMBNAIL_DELETED,
			feedbackDeleted);
		// ShowPictureActivity.this.compressed2DelPaths.clear();

		Intent intent = new Intent();
		intent.putExtras(bundle);
		setResult(MessageConfig.RESULTCODE_THUMBNAIL_DELETED, intent);

		finish();
	}


	@SuppressWarnings ("deprecation")
	private void initListViews (Bitmap bm) {
		ImageView img = new ImageView(this);
		img.setBackgroundColor(0xff000000);
		img.setImageBitmap(bm);
		img.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
			LayoutParams.FILL_PARENT));
		this.viewsList.add(img);
	}


	private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

		public void onPageSelected (int currentPage) {
			ShowPictureActivity.this.currentPage = currentPage;
		}


		public void onPageScrolled (int arg0, float arg1, int arg2) {

		}


		public void onPageScrollStateChanged (int arg0) {

		}
	};


	class MyPageAdapter extends PagerAdapter {
		private ArrayList <View> viewsList;


		public MyPageAdapter (ArrayList <View> viewsList) {
			this.viewsList = viewsList;
		}


		public void setListViews (ArrayList <View> viewsList) {// 自己写的一个方法用来添加数据
			this.viewsList = viewsList;
		}


		public int getCount () {
			return this.viewsList == null ? 0 : this.viewsList.size();
		}


		public int getItemPosition (Object object) {
			return POSITION_NONE;
		}


		public void destroyItem (View v, int index, Object arg2) {
			int n = ShowPictureActivity.this.deletedViews.size();

			for (int i = 0; i < n; ++i) {
				try {
					((ViewPager) v)
						.removeView(ShowPictureActivity.this.deletedViews
							.get(i));
				} catch (Exception e) {
					Log.e(TAG + ":MyPageAdapterdestroyItem",
						"ERROR: " + e.getMessage());
				}
			}

			ShowPictureActivity.this.deletedViews.clear();
		}


		public void finishUpdate (View arg0) {
		}


		public Object instantiateItem (View v, int i) {
			try {
				int c = this.getCount();
				if (c > 0) {
					((ViewPager) v).addView(this.viewsList.get(i % c), 0);
				}
			} catch (Exception e) {
				;
			}

			return this.viewsList.get(i);
		}


		public boolean isViewFromObject (View arg0, Object arg1) {
			return arg0 == arg1;
		}

	}


	private static final String TAG = ShowPictureActivity.class
		.getSimpleName();

	private MyArrayList <MyImage.ImgData> showDatas = new MyArrayList <MyImage.ImgData>();

	private ArrayList <String> compressed2DelPaths = new ArrayList <String>();

	private int currentPage = 0;

	private ArrayList <View> viewsList = new ArrayList <View>();

	private ArrayList <View> deletedViews = new ArrayList <View>();

	private TextView tvASPDummyNavigation;
}
