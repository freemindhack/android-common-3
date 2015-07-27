
package common.message.richtext;


import java.util.ArrayList;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.Toast;


import com.za.smartlock.manufacturer.R;
import common.datastructure.ThreeValuesSet;
import common.utils.MyResult;
import common.utils.NiceFileUtils;


public class ShowPictureActivity extends Activity {

	private ArrayList <View> listViews = null;

	private ViewPager pager;

	private MyPageAdapter adapter;

	RelativeLayout photo_relativeLayout;


	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo);

		photo_relativeLayout = (RelativeLayout) findViewById(R.id.photo_relativeLayout);
		photo_relativeLayout.setBackgroundColor(0x70000000);

		for (int i = 0; i < MyBMP.compressedBmps.size(); i++) {
			this.showDatas.add(MyBMP.compressedBmps.get(i),
				MyBMP.compressedImgPathes.get(i), null);
		}

		Button photo_bt_exit = (Button) findViewById(R.id.photo_bt_exit);
		photo_bt_exit.setOnClickListener(new View.OnClickListener() {
			public void onClick (View v) {
				finish();
			}
		});

		Button photo_bt_del = (Button) findViewById(R.id.photo_bt_del);
		photo_bt_del.setOnClickListener(new View.OnClickListener() {
			public void onClick (View v) {
				if (listViews.size() <= 0) {
					String torm = NiceFileUtils
						.getAlbumStorageDir(NewMessageActivity.albumNameCompressed).cc;
					MyResult <String> ret = NiceFileUtils.rm(torm, true,
						false);

					if (null == ret || 0 != ret.code) {
						Toast.makeText(getApplicationContext(),
							"Opps.. " + (null != ret ? ret.msg : ""),
							Toast.LENGTH_SHORT).show();
					}

					finish();
				} else {
					int d = ShowPictureActivity.this.currentPage;

					ThreeValuesSet <Bitmap, String, String> r = ShowPictureActivity.this.showDatas
						.remove(d);

					String rs = r.v2(0);
					ShowPictureActivity.this.compressed2DelPaths.add(rs);

					Log.v(TAG + ":btn-del", "rm compressed: " + rs);

					pager.removeAllViews();
					listViews.remove(d);
					adapter.setListViews(listViews);
					adapter.notifyDataSetChanged();
				}
			}
		});

		Button photo_bt_enter = (Button) findViewById(R.id.photo_bt_enter);
		photo_bt_enter.setOnClickListener(new View.OnClickListener() {
			public void onClick (View v) {
				int n = ShowPictureActivity.this.compressed2DelPaths.size();

				for (int i = 0; i < n; i++) {
					String c2rm = ShowPictureActivity.this.compressed2DelPaths
						.get(i);

					Log.v(TAG + ":btn-done", "do-c2rm: " + c2rm);
					MyResult <String> ret = NiceFileUtils.rm(c2rm, false,
						false);
					NiceFileUtils.refreshGallery(getApplicationContext(),
						c2rm);

					Log.v(TAG + ":btn-done", "do-c2rm: " + "code: "
						+ ret.code + " msg: " + ret.msg);
				}

				finish();
			}
		});

		pager = (ViewPager) findViewById(R.id.viewpager);
		pager.setOnPageChangeListener(pageChangeListener);

		ArrayList <Bitmap> bmps = this.showDatas.v1s();
		int n = bmps.size();
		for (int i = 0; i < n; i++) {
			initListViews(bmps.get(i));
		}

		adapter = new MyPageAdapter(listViews);// 构造adapter
		pager.setAdapter(adapter);// 设置适配器
		Intent intent = getIntent();
		int id = intent.getIntExtra("ID", 0);
		pager.setCurrentItem(id);
	}


	@SuppressWarnings ("deprecation")
	private void initListViews (Bitmap bm) {
		if (listViews == null)
			listViews = new ArrayList <View>();
		ImageView img = new ImageView(this);// 构造textView对象
		img.setBackgroundColor(0xff000000);
		img.setImageBitmap(bm);
		img.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
			LayoutParams.FILL_PARENT));
		listViews.add(img);// 添加view
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

		private ArrayList <View> listViews;// content

		private int size;// 页数


		public MyPageAdapter (ArrayList <View> listViews) {// 构造函数
															// 初始化viewpager的时候给的一个页面
			this.listViews = listViews;
			size = listViews == null ? 0 : listViews.size();
		}


		public void setListViews (ArrayList <View> listViews) {// 自己写的一个方法用来添加数据
			this.listViews = listViews;
			size = listViews == null ? 0 : listViews.size();
		}


		public int getCount () {// 返回数量
			return size;
		}


		public int getItemPosition (Object object) {
			return POSITION_NONE;
		}


		public void destroyItem (View arg0, int arg1, Object arg2) {// 销毁view对象
			((ViewPager) arg0).removeView(listViews.get(arg1 % size));
		}


		public void finishUpdate (View arg0) {
		}


		public Object instantiateItem (View arg0, int arg1) {// 返回view对象
			try {
				((ViewPager) arg0).addView(listViews.get(arg1 % size), 0);

			} catch (Exception e) {
			}
			return listViews.get(arg1 % size);
		}


		public boolean isViewFromObject (View arg0, Object arg1) {
			return arg0 == arg1;
		}

	}


	private static final String TAG = ShowPictureActivity.class
		.getSimpleName();

	private ThreeValuesSet <Bitmap, String, String> showDatas = new ThreeValuesSet <Bitmap, String, String>();

	private ArrayList <String> compressed2DelPaths = new ArrayList <String>();

	private int currentPage = 0;
}
