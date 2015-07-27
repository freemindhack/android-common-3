
package common.pull2refresh.common;


import android.util.Log;


public class Utils {
	static final String LOG_TAG = "Pull2Refresh";


	public static void warnDeprecation (String depreacted, String replacement) {
		Log.w(LOG_TAG, "You're using the deprecated " + depreacted
			+ " attr, please switch over to " + replacement);
	}
}
