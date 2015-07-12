package nocom.common.utils;


import java.io.File;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;


public class ApkUtils implements ApkUtilsInterface {
	private Context savedContext = null;


	public ApkUtils (Context c) {
		this.savedContext = c;
	}


	@Override
	public void startInstall (String absoluteApkPath) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(absoluteApkPath)),
			"application/vnd.android.package-archive");
		this.savedContext.startActivity(intent);
	}
}
