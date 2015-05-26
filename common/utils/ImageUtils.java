/** ===========================================================================
 *
 *       Filename:  ImageUtils.java
 *
 *    Description:
 *
 * - Mode: 644
 * - NOTE. NOT use "dos"
 * - Updated: 2015-5-13 11:23:45
 *
 *        Version:  1.0.0
 *        Created:  2015-05-12
 *       Revision:  1.0.0
 *       Compiler:  ADT
 *
 *         Author:  Yui Wong, email: yuiwong@126.com
 *   Organization:  ""
 *
 * ===========================================================================
 */
package nocom.common.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImageUtils {
	@SuppressWarnings("deprecation")
	public static Drawable scaleDrawable(final Drawable d, int toPxW, int toPxH) {
		int width = d.getIntrinsicWidth();
		int height = d.getIntrinsicHeight();
		Bitmap oldbmp = ImageUtils.drawableToBitmap(d); // drawable ת���� bitmap
		Matrix matrix = new Matrix(); // ��������ͼƬ�õ� Matrix ����
		float scaleWidth = ((float) toPxW / width); // �������ű���
		float scaleHeight = ((float) toPxH / height);
		matrix.postScale(scaleWidth, scaleHeight); // �������ű���
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true); // �����µ� bitmap ���������Ƕ�ԭ bitmap �����ź��ͼ
		return new BitmapDrawable(newbmp); // �� bitmap ת���� drawable ������
	}

	public static Bitmap drawableToBitmap(final Drawable d) {
		int width = d.getIntrinsicWidth(); // ȡ drawable �ĳ���
		int height = d.getIntrinsicHeight();
		Bitmap.Config config = d.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565; // ȡ drawable ����ɫ��ʽ
		/**
		 * ������Ӧbitmap
		 */
		Bitmap bitmap = Bitmap.createBitmap(width, height, config);
		Canvas canvas = new Canvas(bitmap); // ������Ӧ bitmap �Ļ���
		d.setBounds(0, 0, width, height);
		d.draw(canvas);// �� drawable ���ݻ���������
		return bitmap;
	}

}
