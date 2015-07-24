
package nocom.message;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import nocom.common.utils.MyResult;
import nocom.common.utils.NiceFileUtils;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import com.za.smartlock.manufacturer.R;


public class NewMessageActivity extends Activity {

	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_message);

		this.context = this.getApplicationContext();

		this.init();
	}


	private void init () {
		this.noScrollgridview = (GridView) findViewById(R.id.noScrollGridViewANM);
		this.noScrollgridview
			.setSelector(new ColorDrawable(Color.TRANSPARENT));
		this.adapter = new GridAdapter(this);
		this.adapter.update();
		this.noScrollgridview.setAdapter(adapter);
		this.noScrollgridview
			.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick (AdapterView <?> arg0, View arg1,
					int arg2, long arg3) {
					if (arg2 == Bimp.bmp.size()) {
						new PopupWindows(NewMessageActivity.this,
							NewMessageActivity.this.noScrollgridview);
					} else {
						Intent intent = new Intent(NewMessageActivity.this,
							PhotoActivity.class);
						intent.putExtra("ID", arg2);
						startActivity(intent);
					}
				}
			});

		this.textViewANMDone = (TextView) findViewById(R.id.textViewANMDone);
		this.textViewANMDone.setOnClickListener(new OnClickListener() {

			public void onClick (View v) {
				List <String> list = new ArrayList <String>();
				for (int i = 0; i < Bimp.drr.size(); i++) {
					String s = Bimp.drr.get(i).substring(
						Bimp.drr.get(i).lastIndexOf("/") + 1,
						Bimp.drr.get(i).lastIndexOf("."));

					/* list.add(FileUtils.SDPATH + Str + ".JPEG"); */
					list.add(new File(
						Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						albumNameCompressed)
						+ s + ".jpg");
				}

				// 高清的压缩图片全部就在 list 路径里面了
				// 高清的压缩过的 BMP 对象 都在 Bimp.bmp里面
				/* TODO: send to server */

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


	@SuppressLint ("HandlerLeak")
	public class GridAdapter extends BaseAdapter {
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
			return (Bimp.bmp.size() + 1);
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

			if (position == Bimp.bmp.size()) {
				holder.image.setImageBitmap(BitmapFactory.decodeResource(
					getResources(), R.drawable.icon_addpic_unfocused));
				if (position == 9) {
					holder.image.setVisibility(View.GONE);
				}
			} else {
				holder.image.setImageBitmap(Bimp.bmp.get(position));
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
						if (Bimp.max == Bimp.drr.size()) {
							Message message = new Message();
							message.what = 1;
							handler.sendMessage(message);
							break;
						} else {
							try {
								String path = Bimp.drr.get(Bimp.max);
								System.out.println(path);
								Bitmap bm = Bimp.revitionImageSize(path);
								Bimp.bmp.add(bm);

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

								Bimp.max += 1;
								Message message = new Message();
								message.what = 1;
								handler.sendMessage(message);
							} catch (IOException e) {

								e.printStackTrace();
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


	protected void onRestart () {
		adapter.update();
		super.onRestart();
	}


	public class PopupWindows extends PopupWindow {

		public PopupWindows (Context mContext, View parent) {

			View view = View.inflate(mContext, R.layout.item_popupwindows,
				null);
			view.startAnimation(AnimationUtils.loadAnimation(mContext,
				R.anim.fade_ins));
			LinearLayout ll_popup = (LinearLayout) view
				.findViewById(R.id.ll_popup);
			ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext,
				R.anim.push_bottom_in_2));

			setWidth(LayoutParams.FILL_PARENT);
			setHeight(LayoutParams.FILL_PARENT);
			setBackgroundDrawable(new BitmapDrawable());
			setFocusable(true);
			setOutsideTouchable(true);
			setContentView(view);
			showAtLocation(parent, Gravity.BOTTOM, 0, 0);
			update();

			Button bt1 = (Button) view
				.findViewById(R.id.item_popupwindows_camera);
			Button bt2 = (Button) view
				.findViewById(R.id.item_popupwindows_Photo);
			Button bt3 = (Button) view
				.findViewById(R.id.item_popupwindows_cancel);
			bt1.setOnClickListener(new OnClickListener() {
				public void onClick (View v) {
					takePhoto();
					dismiss();
				}
			});
			bt2.setOnClickListener(new OnClickListener() {
				public void onClick (View v) {
					Intent intent = new Intent(NewMessageActivity.this,
						PictureAddActivity.class);
					startActivity(intent);
					dismiss();
				}
			});
			bt3.setOnClickListener(new OnClickListener() {
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
			if (Bimp.drr.size() < 9 && resultCode == -1) {
				Bimp.drr.add(path);

				NiceFileUtils.addToGallery(context, lastFile);
			}
			break;
		}
	}


	public static String albumName = "Attachment";

	public static String albumNameCompressed = "Attachment.compressed";

	private GridView noScrollgridview;

	private GridAdapter adapter;

	private TextView textViewANMDone;

	private MyResult <String> lastError = new MyResult <String>(0, "", "");

	private File lastFile;

	private Context context;

}
