
package common.message.richtext;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import org.apache.http.Consts;


import p7zip.P7zipProcess;
import p7zip.P7zipProcess.P7zipListener;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.za.smartlock.Configurations;
import com.za.smartlock.customer.R;
import common.datastructure.MyArrayList;
import common.message.richtext.MyImage.ImgData;
import common.network.HttpUpload;
import common.utils.MyResult;
import common.utils.NiceFileUtils;
import common.utils.UIUtils;


public class NewMessageActivity extends Activity {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");

		super.onCreate(savedInstanceState);
		UIUtils.transparentStatus(getWindow());
		if (UIUtils.isPortrait(getApplicationContext())) {
			/* FIXED !! */
			UIUtils.transparentNavigation(getWindow());
		}
		setContentView(R.layout.activity_new_message);

		UIUtils.hideInputMethod(getWindow());

		this.context = this.getApplicationContext();
		Configurations.setCurrentContext(this.context);

		NewMessageActivity.myImage = MyImage.getInstance(true, true);

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

		if ((null != this.adapter) && (!this.isInDeleting)
			&& (!this.isAfterTakePhoto)) {
			this.updateView();
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
			&& UIUtils.checkDeviceHasNavigationBar(this)
			&& UIUtils.isPortrait(getApplicationContext())) {
			tvANMDummyNavigation.setVisibility(View.VISIBLE);
		} else {
			tvANMDummyNavigation.setVisibility(View.GONE);
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


	@Override
	protected void onActivityResult (int requestCode, int resultCode,
		Intent data) {
		Log.v(TAG, "onActivityResult: reqcode: " + requestCode + " rescode: "
			+ resultCode);

		switch (requestCode) {
		case MessageConfig.REQCODE_TAKE_PHOTO: {
			if (NewMessageActivity.myImage.originalImgPathes.size() < 9
				&& resultCode == -1) {
				Log.v(TAG + ":onActivityResult", "REQCODE_TAKE_PHOTO: "
					+ NewMessageActivity.takePhotoPath);

				NewMessageActivity.myImage.originalImgPathes
					.add(NewMessageActivity.takePhotoPath);

				NiceFileUtils.refreshGallery(NewMessageActivity.this.context,
					new File(NewMessageActivity.takePhotoPath));

				NewMessageActivity.this.updateView("");
			}
		}
			break;

		case MessageConfig.REQCODE_THUMBNAIL_DELETED: {
			if (NewMessageActivity.myImage.imgData.size() > 0
				&& MessageConfig.RESULTCODE_THUMBNAIL_DELETED == resultCode) {
				Log.v(TAG + ":onActivityResult", "REQCODE_THUMBNAIL_DELETED");

				Bundle bundle = data.getExtras();
				if (null != bundle) {
					ArrayList <String> dd = bundle
						.getStringArrayList(MessageConfig.GETKEY_THUMBNAIL_DELETED);
					if (null != dd && dd.size() > 0) {
						Log.v(TAG
							+ ":onActivityResult:REQCODE_THUMBNAIL_DELETED",
							"sz: " + dd.size());
						NewMessageActivity.this.updateView(dd);
					}
				}
			}
		}
			break;
		}
	}


	private void init () {
		Log.v(TAG, "init");

		TextView tvNMADummyStatusbar = (TextView) findViewById(R.id.tvNMADummyStatusbar);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			tvNMADummyStatusbar.setVisibility(View.VISIBLE);
		} else {
			tvNMADummyStatusbar.setVisibility(View.GONE);
		}

		tvANMDummyNavigation = (TextView) findViewById(R.id.tvANMDummyNavigation);

		RelativeLayout relativeLayoutANMCancel = (RelativeLayout) findViewById(R.id.relativeLayoutANMCancel);
		relativeLayoutANMCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick (View arg0) {
				finish();
			}

		});

		this.editTextANMText = (EditText) findViewById(R.id.editTextANMText);
		MyResult <Integer> maxHeight = UIUtils
			.getScreenHeightPx(NewMessageActivity.this);
		if (null != maxHeight) {
			if (0 == maxHeight.code) {
				int h = maxHeight.cc.intValue()
					- getResources().getDimensionPixelSize(
						R.dimen.message_margin_bottom);
				this.editTextANMText.setMaxHeight(h);
				this.editTextANMText.setMinHeight(h);
			} else {
				Log.e(TAG + ":init:getScreenHeight", "ERROR: "
					+ maxHeight.msg);
			}
		}

		this.scrollGridViewPreview = (GridView) findViewById(R.id.scrollGridViewANM);
		this.scrollGridViewPreview.setSelector(new ColorDrawable(
			Color.TRANSPARENT));
		this.adapter = new GridAdapter(this);

		this.scrollGridViewPreview.setAdapter(this.adapter);
		this.updateView();

		this.scrollGridViewPreview
			.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick (AdapterView <?> arg0, View arg1,
					int pos, long arg3) {
					Log.v(TAG + ":scrollGridViewPreview", "clicked: " + pos);

					if (pos == NewMessageActivity.myImage.originalImgPathes
						.size()) {
						new PopupWindows(NewMessageActivity.this,
							NewMessageActivity.this.scrollGridViewPreview);
					} else {
						Intent intent = new Intent(NewMessageActivity.this,
							ShowPictureActivity.class);
						intent.putExtra("ID", pos);
						startActivityForResult(intent,
							MessageConfig.REQCODE_THUMBNAIL_DELETED);
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
				z.setP7zipListener(p7zipListener);
				z.startCompress(P7zipProcess.TargetType.TARGET_TAR,
					NiceFileUtils.getAppDirStr(getApplicationContext()).cc
						+ "/upload",
					NiceFileUtils.getAlbumStorageDir(albumNameCompressed).cc,
					"123", true);

				/* will send to server in callback */
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

			if (null != NewMessageActivity.myImage.originalImgPathes) {
				NewMessageActivity.myImage.originalImgPathes.clear();
			}

			if (null != NewMessageActivity.myImage.imgData) {
				NewMessageActivity.myImage.imgData.clear();
			}

			this.updateView();
		} catch (Exception e) {
			Log.e(TAG + ":deinit", "ERROR: " + e.getMessage());
		}
	}


	@SuppressLint ("HandlerLeak")
	private Handler __handler = new Handler() {
		public void handleMessage (Message msg) {
			switch (msg.what) {
			case NewMessageActivity.MSG_WHAT_UPDATE: {
				adapter.notifyDataSetChanged();
			}
				break;

			case NewMessageActivity.MSG_WHAT_ERROR: {
				Toast.makeText(context,
					"Oops .." + lastError.code + " " + lastError.msg,
					Toast.LENGTH_SHORT).show();
			}
				break;
			}
			super.handleMessage(msg);
		}
	};


	private class GridAdapter extends BaseAdapter {
		private LayoutInflater inflater;/* 视图容器  */


		public GridAdapter (Context context) {
			inflater = LayoutInflater.from(context);
		}


		public int getCount () {
			return (NewMessageActivity.myImage.imgData.size() + 1);
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
				convertView = inflater.inflate(R.layout.item_new_msg_img,
					parent, false);
				holder = new ViewHolder();
				holder.image = (ImageView) convertView
					.findViewById(R.id.imgViewINMI);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (which == NewMessageActivity.myImage.imgData.size()) {
				holder.image.setImageBitmap(BitmapFactory.decodeResource(
					getResources(), R.drawable.icon_addpic_unfocused));
				if (which == 9) {
					holder.image.setVisibility(View.GONE);
				}
			} else {
				holder.image
					.setImageBitmap(NewMessageActivity.myImage.imgData.get(
						which).getBmp());
			}

			return convertView;
		}


		public class ViewHolder {
			public ImageView image;
		}
	}


	private class PopupWindows extends PopupWindow {
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
						AlbumsActivity.class);
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


	@SuppressLint ({ "SimpleDateFormat", "DefaultLocale" })
	private void takePhoto () {
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
			NewMessageActivity.takePhotoPath = file.getAbsolutePath();
			Uri imageUri = Uri.fromFile(file);
			openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			startActivityForResult(openCameraIntent,
				MessageConfig.REQCODE_TAKE_PHOTO);
		} catch (Exception e) {
			Toast.makeText(context, "Opps.. " + e.getMessage(),
				Toast.LENGTH_SHORT).show();
		}
	}


	private enum UpdateMode {
		Add, Delete,
	}


	private void updateView () {
		new UpdateThread().start();
	}


	private void updateView (String ss) {
		this.isAfterTakePhoto = true;
		new UpdateThread().start();
	}


	private void updateView (ArrayList <String> delete) {
		this.isInDeleting = true;
		new UpdateThread(delete).start();
	}


	private class UpdateThread extends Thread {
		public UpdateThread () {
			this.um = UpdateMode.Add;
		}


		public UpdateThread (ArrayList <String> delete) {
			this.delete = delete;
			this.um = UpdateMode.Delete;
		}


		@Override
		public void run () {
			try {
				if (UpdateMode.Add == this.um) {
					if (NewMessageActivity.myImage.originalImgPathes.size() > 0) {

						int orisz = NewMessageActivity.myImage.originalImgPathes
							.size();
						int add = orisz
							- NewMessageActivity.myImage.imgData.size();

						Log.v(TAG + ":updatePreview:run", "add: " + add);

						for (int i = 0; i < add; ++i) {
							String oriPath = NewMessageActivity.myImage.originalImgPathes
								.get(orisz - i - 1);
							Log.v(TAG + ":updatePreview:run", "oriPath: "
								+ oriPath);

							Bitmap revedbmp = MyImage
								.revisionImageSize(oriPath);

							String pureName = oriPath.substring(
								oriPath.lastIndexOf("/") + 1,
								oriPath.lastIndexOf("."))
								+ ".jpg";

							Log.v(TAG + ":updatePreview:run", "pureName: "
								+ pureName);

							MyResult <File> ret = NiceFileUtils
								.saveCompressedBitmap(
									revedbmp,
									NiceFileUtils
										.getAlbumStorageDir(albumNameCompressed).cc,
									pureName, 90, true, true);

							if (null == ret || 0 != ret.code) {
								lastError.code = ret.code;
								lastError.msg = ret.msg;
								Message message = new Message();
								message.what = NewMessageActivity.MSG_WHAT_ERROR;
								NewMessageActivity.this.__handler
									.sendMessage(message);
							} else {
								/* the preview */
								NewMessageActivity.myImage.imgData
									.add(new ImgData(revedbmp, oriPath,
										ret.cc.getAbsolutePath()));
								NiceFileUtils.refreshGallery(context, ret.cc);
							}
						}

						if (add > 0) {
							Message message = new Message();
							message.what = NewMessageActivity.MSG_WHAT_UPDATE;
							NewMessageActivity.this.__handler
								.sendMessage(message);
						}
					}

					if (NewMessageActivity.this.isAfterTakePhoto) {
						NewMessageActivity.this.isAfterTakePhoto = false;
					}
				} else if (UpdateMode.Delete == this.um) {
					if (null != this.delete && this.delete.size() > 0) {
						Log.v(TAG + ":updatePreview:run", "Delete");

						int n = this.delete.size();
						for (int i = 0; i < n; ++i) {
							String r = this.delete.get(i);
							MyArrayList <ImgData> rr = NewMessageActivity.myImage.imgData
								.removeByCmp(
									r,
									new MyArrayList.CompareMethod <MyImage.ImgData>() {

										@Override
										public int cmp (Object l, ImgData r) {
											if (((String) l).equals(r
												.getCompressedPath())) {
												return 0;
											}
											return -1;
										}
									});

							int sz = rr.size();

							Log.v(TAG + ":updatePreview:run", "Delete: sz: "
								+ sz);
							for (int zz = 0; zz < sz; ++zz) {
								NiceFileUtils.rmGallery(new File(rr.get(zz)
									.getCompressedPath()), false, false,
									context);
								NewMessageActivity.myImage.originalImgPathes
									.remove(rr.get(zz).getOriginalPath());
							}
						}

						if (n > 0) {
							Message message = new Message();
							message.what = NewMessageActivity.MSG_WHAT_UPDATE;
							NewMessageActivity.this.__handler
								.sendMessage(message);
						}

						if (NewMessageActivity.this.isInDeleting) {
							NewMessageActivity.this.isInDeleting = false;
						}
					}
				}

				super.run();
			} catch (Exception e) {
				Log.e(TAG + ":UpdateThread:run", "ERROR: " + e.getMessage());
			}

		}


		private UpdateMode um;

		private ArrayList <String> delete;
	}


	@SuppressLint ("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage (Message msg) {
			try {

				/* final remove local */
				// NiceFileUtils.rm(
				// new File(
				// Environment
				// .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				// albumNameCompressed), true, false);

				deinit();
				finish();
			} catch (Exception e) {
				;
			}
		}
	};

	private P7zipListener p7zipListener = new P7zipListener() {

		@Override
		public void onProcessStarted (MyResult <String> command) {
			// TODO Auto-generated method stub

		}


		@Override
		public void onProcessFailed (MyResult <String> retval) {
			try {
				Log.e(TAG + ":p7zipListener:onProcessFailed",
					"E: "
						+ ((null != retval) ? retval.code + " " + retval.msg
							: ""));

				handler.sendEmptyMessage(0x0);
			} catch (Exception e) {
				;
			}
		}


		@Override
		public void onProcessFinished (MyResult <String> command) {
			try {
				Log.v(TAG + ":p7zipListener", "onProcessFinished");

				HttpUpload hu = new HttpUpload();
				hu.setKEY_UPLOAD("upload");

				String filePath = NiceFileUtils
					.getAppDirStr(getApplicationContext()).cc
					+ "/upload"
					+ ".tar";
				String uploadUrl = "http://" + Configurations.getServerIp()
					+ "/upload/rcv.php";

				hu.startUploadFile(filePath, uploadUrl,
					org.apache.http.entity.ContentType.MULTIPART_FORM_DATA,
					Consts.UTF_8);

				// handler.sendEmptyMessage(0x0);
			} catch (Exception e) {
				;
			}
		}

	};

	public static final String albumName = "Attachment";

	public static final String albumNameCompressed = "Attachment.compressed";

	private static final String TAG = NewMessageActivity.class
		.getSimpleName();

	private static final int MSG_WHAT_UPDATE = 0x0;

	private static final int MSG_WHAT_ERROR = 0x1;

	private static String takePhotoPath = "";

	private GridView scrollGridViewPreview;

	private GridAdapter adapter;

	private EditText editTextANMText;

	private TextView textViewANMDone;

	private MyResult <String> lastError = new MyResult <String>(0, "", "");

	private Context context;

	private boolean isInDeleting = false;

	private boolean isAfterTakePhoto = false;

	private TextView tvANMDummyNavigation;

	private static MyImage myImage;

}
