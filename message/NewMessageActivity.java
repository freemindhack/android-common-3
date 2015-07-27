
package nocom.message;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import p7zip.P7zipProcess;


import nocom.common.utils.MyResult;
import nocom.common.utils.NiceFileUtils;
import nocom.common.utils.UIUtils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import com.za.smartlock.manufacturer.R;


public class NewMessageActivity extends Activity {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");

		super.onCreate(savedInstanceState);
		UIUtils.transparentStatus(getWindow());
		UIUtils.transparentNavigation(getWindow());
		setContentView(R.layout.activity_new_message);

		UIUtils.hideInputMethod(getWindow());

		this.context = this.getApplicationContext();

		this.init();
	}


	@Override
	protected void onDestroy () {
		Log.v(TAG, "onDestroy");

		super.onDestroy();
	}


	@Override
	protected void onResume () {
		Log.v(TAG, "onResume");

		if (null != this.adapter) {
			this.adapter.update();
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			UIUtils.dotNavigation(getWindow());
		}

		super.onResume();
	}


	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event) {
		Log.v(TAG + ":onKeyDown", "keyCode: " + keyCode);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.deinit();
		}

		return super.onKeyDown(keyCode, event);
	}


	private void init () {
		Log.v(TAG, "init");

		TextView tvNMADummyStatusbar = (TextView) findViewById(R.id.tvNMADummyStatusbar);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			tvNMADummyStatusbar.setVisibility(View.VISIBLE);
		} else {
			tvNMADummyStatusbar.setVisibility(View.GONE);
		}

		TextView tvANMDummyNavigation = (TextView) findViewById(R.id.tvANMDummyNavigation);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
			&& UIUtils.checkDeviceHasNavigationBar(this)) {
			tvANMDummyNavigation.setVisibility(View.VISIBLE);
		} else {
			tvANMDummyNavigation.setVisibility(View.GONE);
		}

		this.editTextANMText = (EditText) findViewById(R.id.editTextANMText);
		MyResult <Integer> maxHeight = UIUtils
			.getScreenHeightPx(NewMessageActivity.this);
		if (null != maxHeight) {
			if (0 == maxHeight.code) {
				this.editTextANMText.setMaxHeight(maxHeight.cc.intValue()
					- getResources().getDimensionPixelSize(
						R.dimen.message_margin_bottom));
			} else {
				Log.e(TAG + ":init:getScreenHeight", "ERROR: "
					+ maxHeight.msg);
			}
		}

		this.noScrollgridview = (GridView) findViewById(R.id.noScrollGridViewANM);
		this.noScrollgridview
			.setSelector(new ColorDrawable(Color.TRANSPARENT));
		this.adapter = new GridAdapter(this);
		// MyBMP.bmps.clear();
		// MyBMP.bmpAddres.clear();
		this.adapter.update();
		this.noScrollgridview.setAdapter(adapter);

		this.noScrollgridview
			.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick (AdapterView <?> arg0, View arg1,
					int pos, long arg3) {
					if (pos == MyBMP.bmps.size()) {
						new PopupWindows(NewMessageActivity.this,
							NewMessageActivity.this.noScrollgridview);
					} else {
						Intent intent = new Intent(NewMessageActivity.this,
							PhotoActivity.class);
						intent.putExtra("ID", pos);
						startActivity(intent);
					}
				}
			});

		this.textViewANMDone = (TextView) findViewById(R.id.textViewANMDone);
		this.textViewANMDone.setOnClickListener(new OnClickListener() {

			public void onClick (View v) {
				List <String> list = new ArrayList <String>();
				for (int i = 0; i < MyBMP.bmpAddres.size(); i++) {
					String s = MyBMP.bmpAddres.get(i).substring(
						MyBMP.bmpAddres.get(i).lastIndexOf("/") + 1,
						MyBMP.bmpAddres.get(i).lastIndexOf("."));

					list.add(new File(
						Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						albumNameCompressed)
						+ s + ".jpg");
				}

				// 高清的压缩图片全部就在 list 路径里面了
				// 高清的压缩过的 BMP 对象 都在 Bimp.bmp里面
				/* zip file */
				/* TODO: send to server */
				P7zipProcess z = new P7zipProcess(null);
				z.starCompress(P7zipProcess.TargetType.TARGET_TAR_BZ2,
					NiceFileUtils.getAppDirStr(getApplicationContext()).cc
						+ "upload",
					NiceFileUtils.getAlbumStorageDir(albumNameCompressed).cc,
					"123");

				// 完成上传服务器后 .........
				/* FileUtils.deleteDir(); */
				NiceFileUtils.rm(
					new File(
						Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						albumNameCompressed), true, false);
			}
		});
	}


	private void deinit () {
		try {
			Log.w(TAG, "deinit");

			MyResult <String> ret = NiceFileUtils
				.rm(new File(
					Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					albumNameCompressed), true, false);

			if (null != ret && ret.code != 0) {
				Log.w(TAG + ":deinit", "ERROR: " + ret.code + " " + ret.msg);
			}

			if (null != MyBMP.bmpAddres) {
				MyBMP.bmps.clear();
				MyBMP.bmpAddres.clear();
				MyBMP.max = 0;
			}
			this.adapter.update();
		} catch (Exception e) {
			Log.e(TAG + ":deinit", "ERROR: " + e.getMessage());
		}
	}


	@SuppressLint ("HandlerLeak")
	private class GridAdapter extends BaseAdapter {
		private LayoutInflater inflater; // 视图容器

		private int selectedPosition = -1;// 选中的位置

		private boolean shape;


		public boolean isShape () {
			return shape;
		}


		public void setShape (boolean shape) {
			this.shape = shape;
		}


		public GridAdapter (Context context) {
			inflater = LayoutInflater.from(context);
		}


		public void update () {
			loading();
		}


		public int getCount () {
			return (MyBMP.bmps.size() + 1);
		}


		public Object getItem (int arg0) {

			return null;
		}


		public long getItemId (int arg0) {

			return 0;
		}


		public void setSelectedPosition (int position) {
			selectedPosition = position;
		}


		public int getSelectedPosition () {
			return selectedPosition;
		}


		/**
		 * ListView Item设置
		 */
		public View getView (int position, View convertView, ViewGroup parent) {
			final int coord = position;
			ViewHolder holder = null;
			if (convertView == null) {

				convertView = inflater.inflate(R.layout.item_published_grida,
					parent, false);
				holder = new ViewHolder();
				holder.image = (ImageView) convertView
					.findViewById(R.id.item_grida_image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (position == MyBMP.bmps.size()) {
				holder.image.setImageBitmap(BitmapFactory.decodeResource(
					getResources(), R.drawable.icon_addpic_unfocused));
				if (position == 9) {
					holder.image.setVisibility(View.GONE);
				}
			} else {
				holder.image.setImageBitmap(MyBMP.bmps.get(position));
			}

			return convertView;
		}


		public class ViewHolder {
			public ImageView image;
		}


		Handler handler = new Handler() {
			public void handleMessage (Message msg) {
				switch (msg.what) {
				case 1:
					adapter.notifyDataSetChanged();
					break;

				case 999: {
					Toast.makeText(context,
						"Oops .." + lastError.code + " " + lastError.msg,
						Toast.LENGTH_SHORT).show();
				}
					break;
				}
				super.handleMessage(msg);
			}
		};


		public void loading () {
			new Thread(new Runnable() {
				public void run () {
					while (true) {
						if (MyBMP.max >= MyBMP.bmpAddres.size()) {
							Log.i(TAG + ":loading:run", "max: " + MyBMP.max
								+ " " + "sz: " + MyBMP.bmpAddres.size());
							if (MyBMP.max > MyBMP.bmpAddres.size()) {
								MyBMP.max = MyBMP.bmpAddres.size();
							}

							Message message = new Message();
							message.what = 1;
							handler.sendMessage(message);
							break;
						} else {
							try {
								String path = MyBMP.bmpAddres.get(MyBMP.max);
								System.out.println(path);
								Bitmap bm = MyBMP.revitionImageSize(path);
								MyBMP.bmps.add(bm);

								String newStr = path.substring(
									path.lastIndexOf("/") + 1,
									path.lastIndexOf("."))
									+ ".jpg";

								MyResult <File> ret = NiceFileUtils.saveCompressedBitmap(
									bm,
									NiceFileUtils
										.getAlbumStorageDir(albumNameCompressed).cc,
									newStr, 90, true, true);

								if (null == ret || 0 != ret.code) {
									lastError.code = ret.code;
									lastError.msg = ret.msg;
									Message message = new Message();
									message.what = 999;
									handler.sendMessage(message);
								} else {
									NiceFileUtils.addToGallery(context,
										ret.cc);
								}

								MyBMP.max += 1;
								Message message = new Message();
								message.what = 1;
								handler.sendMessage(message);
							} catch (IOException e) {
								Log.e(TAG + ":loading",
									"ERROR: " + e.getMessage());
							}
						}
					}
				}
			}).start();
		}
	}


	public String getString (String s) {
		String path = null;
		if (s == null)
			return "";
		for (int i = s.length() - 1; i > 0; i++) {
			s.charAt(i);
		}
		return path;
	}


	public class PopupWindows extends PopupWindow {

		@SuppressWarnings ("deprecation")
		public PopupWindows (Context mContext, View parent) {
			Log.v(TAG, "PopupWindows");

			View view = View.inflate(mContext, R.layout.menu_new_picture,
				null);
			view.startAnimation(AnimationUtils.loadAnimation(mContext,
				R.anim.fade_ins));
			LinearLayout llMNPpopup = (LinearLayout) view
				.findViewById(R.id.llMNPpopup);
			llMNPpopup.startAnimation(AnimationUtils.loadAnimation(mContext,
				R.anim.push_bottom_in_2));

			setWidth(LayoutParams.FILL_PARENT);
			setHeight(LayoutParams.FILL_PARENT);
			setBackgroundDrawable(new BitmapDrawable());
			setFocusable(true);
			setOutsideTouchable(true);
			setContentView(view);
			TextView tvIPWDummyNavigation = (TextView) view
				.findViewById(R.id.tvIPWDummyNavigation);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
				&& UIUtils
					.checkDeviceHasNavigationBar(NewMessageActivity.this)) {
				tvIPWDummyNavigation.setVisibility(View.VISIBLE);
			} else {
				tvIPWDummyNavigation.setVisibility(View.GONE);
			}

			showAtLocation(parent, Gravity.BOTTOM, 0, 0);

			update();

			LinearLayout llMNPCamera = (LinearLayout) view
				.findViewById(R.id.llMNPCamera);
			LinearLayout llMNPFromAlbum = (LinearLayout) view
				.findViewById(R.id.llMNPFromAlbum);
			LinearLayout llMNPCancel = (LinearLayout) view
				.findViewById(R.id.llMNPCancel);
			llMNPCamera.setOnClickListener(new OnClickListener() {
				public void onClick (View v) {
					takePhoto();
					dismiss();
				}
			});

			llMNPFromAlbum.setOnClickListener(new OnClickListener() {
				public void onClick (View v) {
					Log.v(TAG + ":PopupWindows:btnSelectPhoto", "onClick");
					Intent intent = new Intent(NewMessageActivity.this,
						PictureAddActivity.class);
					startActivity(intent);
					dismiss();
				}
			});

			llMNPCancel.setOnClickListener(new OnClickListener() {
				public void onClick (View v) {
					dismiss();
				}
			});
		}
	}


	private static final int TAKE_PICTURE = 0x000000;

	private String path = "";


	@SuppressLint ({ "SimpleDateFormat", "DefaultLocale" })
	public void takePhoto () {
		try {
			long nowMs = System.currentTimeMillis();
			Date nowDate = new Date(nowMs);
			SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd.HH.mm.ss");
			String ts = formatter.format(nowDate);

			MyResult <File> dir = NiceFileUtils.makeAlbumStorageDir(
				NewMessageActivity.albumName, true);
			if (null == dir || dir.code != 0) {
				Toast.makeText(context,
					"Opps.. " + dir != null ? dir.msg : "",
					Toast.LENGTH_SHORT).show();
			}

			String name = String
				.format("Photo.%s.%03d.jpg", ts, nowMs % 1000);

			Intent openCameraIntent = new Intent(
				MediaStore.ACTION_IMAGE_CAPTURE);
			File file = new File(dir.cc, name);
			lastFile = file;
			path = file.getPath();
			Uri imageUri = Uri.fromFile(file);
			openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			startActivityForResult(openCameraIntent, TAKE_PICTURE);
		} catch (Exception e) {
			Toast.makeText(context, "Opps.. " + e.getMessage(),
				Toast.LENGTH_SHORT).show();
		}
	}


	protected void onActivityResult (int requestCode, int resultCode,
		Intent data) {
		switch (requestCode) {
		case TAKE_PICTURE:
			if (MyBMP.bmpAddres.size() < 9 && resultCode == -1) {
				MyBMP.bmpAddres.add(path);

				NiceFileUtils.addToGallery(context, lastFile);
			}
			break;
		}
	}


	public static String albumName = "Attachment";

	public static String albumNameCompressed = "Attachment.compressed";

	private static final String TAG = NewMessageActivity.class
		.getSimpleName();

	private GridView noScrollgridview;

	private GridAdapter adapter;

	private EditText editTextANMText;

	private TextView textViewANMDone;

	private MyResult <String> lastError = new MyResult <String>(0, "", "");

	private File lastFile;

	private Context context;

}
