package nocom.common.utils;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;


public class VersionUtils implements VersionUtilsInterface {
	Context savedContext = null;


	public VersionUtils (Context c) {
		this.savedContext = c;
	}


	@Override
	public String getVersion () {
		try {
			if (null == this.savedContext) {
				return "";
			}

			PackageManager pm =
				this.savedContext.getPackageManager();

			/* 参数一：当前应用程序的包名 参数二：可选的附加消息，这里我们用不到 ，可以定义为0 */
			PackageInfo info =
				pm.getPackageInfo(
					this.savedContext.getPackageName(), 0);

			/* 返回当前应用程序的版本号 */
			return info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
