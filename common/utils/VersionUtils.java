package nocom.common.utils;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;


public class VersionUtils implements VersionUtilsInterface {
	Context savedContext = null;


	public VersionUtils (Context c) {
		this.savedContext = c;
	}


	@Override
	public String getVersionName () {
		try {
			if (null == this.savedContext) {
				return "";
			}

			PackageManager pm =
				this.savedContext.getPackageManager();

			PackageInfo info =
				pm.getPackageInfo(
					this.savedContext.getPackageName(), 0);

			return info.versionName;
		} catch (Exception e) {
			Log.e(TAG + ":VersionUtils",
				"ERROR: " + e.getMessage());
			return "";
		}
	}


	@Override
	public String getVersionString () {
		try {
			int versionCode = this.getVersionCode();

			if (-1 == versionCode) {
				return "";
			}

			return this.fourcc2string(versionCode);
		} catch (Exception e) {
			Log.e(TAG + ":getVersionString",
				"ERROR: " + e.getMessage());
			return "";
		}
	}


	@Override
	public int getVersionCode () {
		try {
			if (null == this.savedContext) {
				return -1;
			}

			PackageManager pm =
				this.savedContext.getPackageManager();

			PackageInfo info =
				pm.getPackageInfo(
					this.savedContext.getPackageName(), 0);

			return info.versionCode;
		} catch (Exception e) {
			Log.e(TAG + ":getVersionCode",
				"ERROR: " + e.getMessage());
			return -1;
		}
	}


	@Override
	public String fourcc2string (int fourccValue) {
		try {
			byte code;

			String ret = new String("");

			for (int i = 0; i < 3; ++i) {
				code =
					(byte) (0xff & (fourccValue >> (24 - (8 * i))));

				ret += StringUtils.toString(code);
				ret += ".";
			}

			code = (byte) (0xff & fourccValue);

			ret += StringUtils.toString(code);

			return ret;
		} catch (Exception e) {
			Log.e(TAG + ":fourcc2string",
				"ERROR: " + e.getMessage());
			return "";
		}
	}


	private static String TAG = "VersionUtils";
}
