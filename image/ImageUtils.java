
package common.image;


import generic_utils.MyResult;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


import posix.generic.errno.errno;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;


import common.utils.StreamUtils;


public class ImageUtils {
	/**
	 * 得到本地或者网络上的bitmap url - 网络或者本地图片的绝对路径 比如
	 * 
	 * A.网络路径 url="http://..../girl2.png"
	 * 
	 * B.本地路径 url="file://mnt/sdcard/photo/image.png"
	 * 
	 * C.支持的图片格式 png jpg bmp gif等等
	 */
	public static MyResult <Bitmap> url2bmp (String url) {
		try {
			Bitmap bitmap = null;
			InputStream in = null;
			BufferedOutputStream out = null;

			in = new BufferedInputStream(new URL(url).openStream(),
				ImageUtils.IO_BUFFER_SIZE);
			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream,
				ImageUtils.IO_BUFFER_SIZE);
			MyResult <Long> ret = StreamUtils.copy(in, out);

			out.flush();

			if (0 != ret.code) {
				in.close();
				out.close();
				return new MyResult <Bitmap>(ret.code, ret.msg, null);
			}

			byte[] data = dataStream.toByteArray();
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

			data = null;
			in.close();
			out.close();

			return new MyResult <Bitmap>(0, null, bitmap);
		} catch (IOException ioe) {
			return new MyResult <Bitmap>(errno.EIO * -1, "I/O error: "
				+ ioe.getMessage(), null);
		} catch (Exception e) {
			return new MyResult <Bitmap>(errno.EXTRA_EEUNRESOLVED * -1,
				"Get unresolved exception: " + e.getMessage(), null);
		}
	}


	public static Bitmap revisionImageSize (String path) throws IOException {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
			new File(path)));

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, options);
		in.close();

		int i = 0;
		Bitmap bitmap = null;
		while (true) {
			if ((options.outWidth >> i <= 1000)
				&& (options.outHeight >> i <= 1000)) {
				in = new BufferedInputStream(new FileInputStream(new File(
					path)));
				options.inSampleSize = (int) Math.pow(2.0D, i);
				options.inJustDecodeBounds = false;
				bitmap = BitmapFactory.decodeStream(in, null, options);
				break;
			}
			i += 1;
		}

		return bitmap;
	}


	@SuppressWarnings ("deprecation")
	public static Drawable scaleDrawable (final Drawable d, int toPxW,
		int toPxH) {
		int width = d.getIntrinsicWidth();
		int height = d.getIntrinsicHeight();
		Bitmap oldbmp = ImageUtils.drawableToBitmap(d); // drawable 转换成 bitmap
		Matrix matrix = new Matrix(); // 创建操作图片用的 Matrix 对象
		float scaleWidth = ((float) toPxW / width); // 计算缩放比例
		float scaleHeight = ((float) toPxH / height);
		matrix.postScale(scaleWidth, scaleHeight); // 设置缩放比例
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
			matrix, true); // 建立新的 bitmap ，其内容是对原 bitmap 的缩放后的图
		return new BitmapDrawable(newbmp); // 把 bitmap 转换成 drawable 并返回
	}


	public static Bitmap drawableToBitmap (final Drawable d) {
		int width = d.getIntrinsicWidth(); // 取 drawable 的长宽
		int height = d.getIntrinsicHeight();
		Bitmap.Config config = d.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
			: Bitmap.Config.RGB_565; // 取 drawable 的颜色格式
		/**
		 * 建立对应bitmap
		 */
		Bitmap bitmap = Bitmap.createBitmap(width, height, config);
		Canvas canvas = new Canvas(bitmap); // 建立对应 bitmap 的画布
		d.setBounds(0, 0, width, height);
		d.draw(canvas);// 把 drawable 内容画到画布中
		return bitmap;
	}


	public static Bitmap getBitmap (String localPath) {
		if ((null == localPath) || (localPath.length() <= 0)) {
			return null;
		}
		Bitmap bitmap = null;
		try {
			File file = new File(localPath);
			if (file.exists()) {
				bitmap = BitmapFactory.decodeFile(localPath);
			}
		} catch (Exception e) {
			Log.e(TAG + ":getBitmap", "ERROR: " + e.getMessage());
			return null;
		}
		return bitmap;
	}


	public static Drawable bmp2drawable (Context c, Bitmap bmp) {
		try {
			return new BitmapDrawable(c.getResources(), bmp);
		} catch (Exception e) {
			Log.e(TAG + ":bmp2drawable", "ERROR: " + e.getMessage());
			return null;
		}
	}


	private static final String TAG = ImageUtils.class.getSimpleName();

	private static final int IO_BUFFER_SIZE = 4 * 1024;
}
