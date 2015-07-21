
package nocom.common.utils;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;


public class UIUtils {
	/*
	 * between:
	 * super.onCreate(savedInstanceState);
	 * and
	 * setContentView(...);
	 */
	public static void fullScreen (Window w) {
		w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}


	@TargetApi (19)
	@SuppressLint ("InlinedApi")
	public static boolean transparentStatus (Window w) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			return true;
		} else {
			return false;
		}
	}


	@TargetApi (19)
	@SuppressLint ("InlinedApi")
	public static boolean transparentNavigation (Window w) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			return true;
		} else {
			return false;
		}
	}


	/* any */
	@SuppressLint ("NewApi")
	public static void hideNavigation (Window w) {
		w.getDecorView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}


	@SuppressLint ("NewApi")
	public static void dotNavigation (Window w) {
		w.getDecorView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_LOW_PROFILE);
	}


	@SuppressLint ("NewApi")
	public static boolean checkDeviceHasNavigationBar (Context c) {

		/* 通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有 navigation bar */
		boolean hasMenuKey = ViewConfiguration.get(c).hasPermanentMenuKey();

		boolean hasBackKey = KeyCharacterMap
			.deviceHasKey(KeyEvent.KEYCODE_BACK);

		if (!hasMenuKey && !hasBackKey) {
			return true;
		} else {
			return false;
		}
	}


	public static void hideInputMethod (Window w) {
		w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
}
