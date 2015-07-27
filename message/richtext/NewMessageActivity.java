
package common.message.richtext;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


import p7zip.P7zipProcess;
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
import common.utils.MyResult;
import common.utils.NiceFileUtils;
import common.utils.UIUtils;


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
			this.adapter.updatePreview();
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

		this.scrollGridViewPreview = (GridView) findViewById(R.id.noScrollGridViewANM);
		this.scrollGridViewPreview.setSelector(new ColorDrawable(
			Color.TRANSPARENT));
		this.adapter = new GridAdapter(this);

		this.scrollGridViewPreview.setAdapter(this.adapter);
		this.adapter.updatePreview();

		this.scrollGridViewPreview
			.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick (AdapterView <?> arg0, View arg1,
					int pos, long arg3) {
					Log.v(TAG + ":scrollGridViewPreview", "clicked: " + pos);

					if (pos == MyBMP.originalImgPathes.size()) {
						new PopupWindows(NewMessageActivity.this,
							NewMessageActivity.this.scrollGridViewPreview);
					} else {
						Intent intent = new Intent(NewMessageActivity.this,
							ShowPictureActivity.class);
						intent.putExtra("ID", pos);
						startActivity(intent);
					}
				}
			});

		this.textViewANMDone = (TextView) findViewById(R.id.textViewANMDone);
		this.textViewANMDone.setOnClickListener(new OnClickListener() {

			public void onClick (View v) {
				/*
				 * compressed in MyBMP.originalImgPathes
				 * and: Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						albumNameCompressed
				 */

				P7zipProcess z = new P7zipProcess(null);
				z.startCompress(P7zipProcess.TargetType.TARGET_TAR,
					NiceFileUtils.getAppDirStr(getApplicationContext()).cc
						+ "/upload",
					NiceFileUtils.getAlbumStorageDir(albumNameCompressed).cc,
					"123", true);

				/* TODO: send to server */

				/* final remove local */
				// NiceFileUtils.rm(
				// new File(
				// Environment
				// .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				// albumNameCompressed), true, false);
			}
		});
	}


	private void deinit () {
		try {
			Log.w(TAG, "deinit");

			MyResult <String> ret = NiceFileUtils
				.rmGallery(
					new File(
						Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						albumNameCompressed), true, false,
					getApplicationContext());

			if (null != ret && ret.code != 0) {
				Log.w(TAG + ":deinit", "ERROR: " + ret.code + " " + ret.msg);
			}

			if (null != MyBMP.originalImgPathes) {
				MyBMP.originalImgPathes.clear();
			}

			if (null != MyBMP.compressedImgPathes) {
				MyBMP.compressedImgPathes.clear();
			}

			if (null != MyBMP.compressedBmps) {
				MyBMP.compressedBmps.clear();
			}

			this.adapter.updatePreview();
		} catch (Exception e) {
			Log.e(TAG + ":deinit", "ERROR: " + e.getMessage());
		}
	}


	@SuppressLint ("HandlerLeak")
	private class GridAdapter extends BaseAdapter {
		private LayoutInflater inflater;/* 视图容器  */


		public GridAdapter (Context context) {
			inflater = LayoutInflater.from(context);
		}


		public int getCount () {
			return (MyBMP.compressedBmps.size() + 1);
		}


		public Object getItem (int arg) {
			return null;
		}


		@Override
		public long getItemId (int arg) {

			return 0;
		}


		@Override
		public View getView (int which, View convertView, ViewGroup parent) {
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

			if (which == MyBMP.compressedBmps.size()) {
				holder.image.setImageBitmap(BitmapFactory.decodeResource(
					getResources(), R.drawable.icon_addpic_unfocused));
				if (which == 9) {
					holder.image.setVisibility(View.GONE);
				}
			} else {
				holder.image.setImageBitmap(MyBMP.compressedBmps.get(which));
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


		public void updatePreview () {
			new Thread(new Runnable() {
				public void run () {
					if (MyBMP.compressedBmps.size() >= 9) {
						Log.v(TAG + ":loadingPreview:run", "sz: "
							+ MyBMP.compressedBmps.size());

						Message message = new Message();
						message.what = 1;
						handler.sendMessage(message);
					} else if (MyBMP.originalImgPathes.size() > 0) {
						try {
							int add = MyBMP.originalImgPathes.size()
								- MyBMP.compressedImgPathes.size();

							for (int i = 0; i < add; ++i) {
								String oriPath = MyBMP.originalImgPathes
									.get(MyBMP.originalImgPathes.size() - i
										- 1);
								Log.v(TAG + ":updatePreview:run", "oriPath: "
									+ oriPath);

								Bitmap revedbmp = MyBMP
									.revitionImageSize(oriPath);

								String compressedName = oriPath.substring(
									path.lastIndexOf("/") + 1,
									path.lastIndexOf("."))
									+ ".jpg";

								Log.v(TAG + ":updatePreview:run",
									"compressedName: " + compressedName);

								MyResult <File> ret = NiceFileUtils
									.saveCompressedBitmap(
										revedbmp,
										NiceFileUtils
											.getAlbumStorageDir(albumNameCompressed).cc,
										compressedName, 90, true, true);

								if (null == ret || 0 != ret.code) {
									lastError.code = ret.code;
									lastError.msg = ret.msg;
									Message message = new Message();
									message.what = 999;
									handler.sendMessage(message);
								} else {
									/* the preview */
									MyBMP.compressedBmps.add(revedbmp);
									MyBMP.compressedImgPathes.add(ret.cc
										.getAbsolutePath());
									NiceFileUtils.refreshGallery(context,
										ret.cc);

								}
							}

							if (add > 0) {
								Message message = new Message();
								message.what = 1;
								handler.sendMessage(message);
							}
						} catch (IOException e) {
							Log.e(TAG + ":updatePreview:run",
								"ERROR: " + e.getMessage());
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
			if (MyBMP.originalImgPathes.size() < 9 && resultCode == -1) {
				Log.v(TAG, "TAKE_PICTURE: " + path);

				MyBMP.originalImgPathes.add(path);

				NiceFileUtils.refreshGallery(context, new File(path));
			}
			break;
		}
	}


	public static String albumName = "Attachment";

	public static String albumNameCompressed = "Attachment.compressed";

	private static final String TAG = NewMessageActivity.class
		.getSimpleName();

	private GridView scrollGridViewPreview;

	private GridAdapter adapter;

	private EditText editTextANMText;

	private TextView textViewANMDone;

	private MyResult <String> lastError = new MyResult <String>(0, "", "");

	private Context context;

}
