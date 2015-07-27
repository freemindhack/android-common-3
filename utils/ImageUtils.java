/**
 * ===========================================================================
 * 
 * Filename: ImageUtils.java
 * 
 * Description:
 * 
 * - Mode: 644 - NOTE. NOT use "dos" - Updated: 2015-5-13 11:23:45
 * 
 * Version: 1.0.0 Created: 2015-05-12 Revision: 1.0.0 Compiler: ADT
 * 
 * Author: Yui Wong, email: yuiwong@126.com Organization: ""
 * 
 * ===========================================================================
 */

package common.utils;


import java.io.File;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;


public class ImageUtils {
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


	private static final String TAG = "ImageUtils";
}
