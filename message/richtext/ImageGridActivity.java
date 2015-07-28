
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;


import com.za.smartlock.manufacturer.R;
import common.message.richtext.ImageGridAdapter.TextCallback;


public class ImageGridActivity extends Activity {

	@SuppressWarnings ("unchecked")
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image_grid);

		this.helper = new AlbumHelper();
		this.helper.prepare(getApplicationContext());

		Intent i = this.getIntent();
		if (null != i) {
			Serializable s = i
				.getSerializableExtra(ImageGridActivity.EXTRA_IMAGE_LIST);
			if (null != s) {
				this.imagesList = (List <ImageItem>) s;
			}
		}

		initView();
		this.btnFinish = (Button) findViewById(R.id.bt);
		this.btnFinish.setOnClickListener(new OnClickListener() {

			public void onClick (View v) {
				ArrayList <String> newList = new ArrayList <String>();
				Collection <String> c = adapter.map.values();
				Iterator <String> it = c.iterator();
				for (; it.hasNext();) {
					newList.add(it.next());
				}

				for (int i = 0; i < newList.size(); i++) {
					if (MyImage.originalImgPathes.size() < 9) {
						MyImage.originalImgPathes.add(newList.get(i));
					} else {
						break;
					}
				}
				finish();
			}

		});
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
			this.adapter = new ImageGridAdapter(ImageGridActivity.this,
				this.imagesList, this.handler);
			this.gridView.setAdapter(this.adapter);

			this.adapter.setTextCallback(new TextCallback() {
				public void onListen (int count) {
					Log.v(TAG + ":initView:adapter:TextCallback", "onListen");

					ImageGridActivity.this.btnFinish.setText("完成" + "("
						+ count + ")");
				}
			});

			this.gridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick (AdapterView <?> parent, View view,
					int position, long id) {
					Log.v(TAG + ":initView:gridView:OnItemClickListener",
						"onItemClick");

					ImageItem ii = ImageGridActivity.this.imagesList
						.get(position);
					if (null != ii) {
						ImageGridActivity.this.adapter.notifyDataSetChanged();
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
				Toast.makeText(ImageGridActivity.this, "最多选择9张图片",
					Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		}
	};

	public static final String EXTRA_IMAGE_LIST = "imagelist";

	private static final String TAG = ImageGridActivity.class.getSimpleName();

	private List <ImageItem> imagesList = null;

	private GridView gridView;

	private ImageGridAdapter adapter;

	private AlbumHelper helper;

	private Button btnFinish;
}
