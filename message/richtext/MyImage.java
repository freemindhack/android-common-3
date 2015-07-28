
package common.message.richtext;


import java.io.IOException;
import java.util.ArrayList;


import android.graphics.Bitmap;


import common.datastructure.MyArrayList;
import common.image.ImageUtils;


public class MyImage {

	public static class ImgData {
		public ImgData (Bitmap bmp, String originalPath, String compressedPath) {
			this.bmp = bmp;
			this.originalPath = originalPath;
			this.compressedPath = compressedPath;
		}


		public Bitmap getBmp () {
			return this.bmp;
		}


		public String getOriginalPath () {
			return this.originalPath;
		}


		public String getCompressedPath () {
			return this.compressedPath;
		}


		public Bitmap bmp;

		public String originalPath;

		public String compressedPath;

	};


	public static Bitmap revisionImageSize (String path) throws IOException {
		return ImageUtils.revisionImageSize(path);
	}


	public static MyImage getInstance (boolean getOld, boolean recreate) {
		if ((null != MyImage.instance) && getOld) {
			return MyImage.instance;
		} else if ((null != MyImage.instance) && (!getOld) && (!recreate)) {
			return null;
		} else if ((null != MyImage.instance) && (!getOld) && recreate) {
			return MyImage.instance = new MyImage();
		} else if (null == MyImage.instance) {
			return MyImage.instance = new MyImage();
		} else {
			return null;
		}
	}


	public static void clearInstance () {
		if (null != MyImage.instance) {
			MyImage.instance.originalImgPathes.clear();
			MyImage.instance.imgData.clear();
			MyImage.instance = null;
		}
	}


	private static MyImage instance = null;

	public ArrayList <String> originalImgPathes = new ArrayList <String>();

	public MyArrayList <ImgData> imgData = new MyArrayList <ImgData>();
}
