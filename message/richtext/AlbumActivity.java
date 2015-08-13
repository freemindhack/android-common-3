
package common.message.richtext;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.za.smartlock.customer.R;
import common.message.richtext.AlbumAdapter.TextCallback;
import common.utils.UIUtils;


public class AlbumActivity extends Activity {

	@SuppressWarnings ("unchecked")
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");

		super.onCreate(savedInstanceState);
		UIUtils.transparentStatus(getWindow());
		if (UIUtils.isPortrait(getApplicationContext())) {
			UIUtils.transparentNavigation(getWindow());
		}
		setContentView(R.layout.activity_album);

		this.helper = new AlbumHelper();
		this.helper.prepare(getApplicationContext());

		Intent i = this.getIntent();
		if (null != i) {
			Serializable s = i
				.getSerializableExtra(AlbumActivity.EXTRA_IMAGE_LIST);
			if (null != s) {
				this.imagesList = (List <ImageItem>) s;
			}
		}

		initView();

		this.relativeLayoutAALCancel = (RelativeLayout) findViewById(R.id.relativeLayoutAALCancel);
		this.relativeLayoutAALCancel
			.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick (View arg0) {
					finish();
				}

			});

		this.textViewAALDone = (TextView) findViewById(R.id.textViewAALDone);
		this.textViewAALDone.setOnClickListener(new OnClickListener() {

			public void onClick (View v) {
				ArrayList <String> newList = new ArrayList <String>();
				Collection <String> c = adapter.map.values();
				Iterator <String> it = c.iterator();
				for (; it.hasNext();) {
					newList.add(it.next());
				}

				MyImage mi = MyImage.getInstance(true, true);
				for (int i = 0; i < newList.size(); i++) {
					if (mi.originalImgPathes.size() < 9) {
						String n = newList.get(i);
						Log.v(TAG, "done: add: " + n);
						mi.originalImgPathes.add(n);
					} else {
						break;
					}
				}
				finish();
			}

		});

		this.tvAALDummyStatusbar = (TextView) findViewById(R.id.tvAALDummyStatusbar);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			this.tvAALDummyStatusbar.setVisibility(View.VISIBLE);
		} else {
			this.tvAALDummyStatusbar.setVisibility(View.GONE);
		}

		this.tvAALDummyNavigation = (TextView) findViewById(R.id.tvAALDummyNavigation);
	}


	@Override
	public void onResume () {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
			&& UIUtils.checkDeviceHasNavigationBar(this)
			&& UIUtils.isPortrait(getApplicationContext())) {
			this.tvAALDummyNavigation.setVisibility(View.VISIBLE);
		} else {
			this.tvAALDummyNavigation.setVisibility(View.GONE);
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			UIUtils.dotNavigation(getWindow());
		}

		super.onResume();
	}


	private void initView () {
		try {
			Log.v(TAG, "initView");

			if (null == this.imagesList || this.imagesList.size() <= 0) {
				Log.w(TAG + ":initView", "no images");
				Toast.makeText(getApplicationContext(), "No images",
					Toast.LENGTH_SHORT).show();
				return;
			}

			this.gridView = (GridView) findViewById(R.id.gridview);
			this.gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
			this.adapter = new AlbumAdapter(AlbumActivity.this,
				this.imagesList, this.handler);
			this.gridView.setAdapter(this.adapter);

			this.adapter.setTextCallback(new TextCallback() {
				public void onListen (int count) {
					Log.v(TAG + ":initView:adapter:TextCallback", "onListen");

					AlbumActivity.this.textViewAALDone
						.setText(getString(R.string.wording_finish) + " "
							+ count);
				}
			});

			this.gridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick (AdapterView <?> parent, View view,
					int position, long id) {
					Log.v(TAG + ":initView:gridView:OnItemClickListener",
						"onItemClick");

					ImageItem ii = AlbumActivity.this.imagesList
						.get(position);
					if (null != ii) {
						AlbumActivity.this.adapter.notifyDataSetChanged();
					}
				}

			});
		} catch (Exception e) {
			Log.e(TAG + ":initView", "ERROR: " + e.getMessage());
		}

	}


	@SuppressLint ("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage (Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(AlbumActivity.this, "最多选择9张图片",
					Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		}
	};

	public static final String EXTRA_IMAGE_LIST = "imagelist";

	private static final String TAG = AlbumActivity.class.getSimpleName();

	private List <ImageItem> imagesList = null;

	private GridView gridView;

	private AlbumAdapter adapter;

	private AlbumHelper helper;

	private TextView tvAALDummyStatusbar;

	private RelativeLayout relativeLayoutAALCancel;

	private TextView textViewAALDone;

	private TextView tvAALDummyNavigation;
}
