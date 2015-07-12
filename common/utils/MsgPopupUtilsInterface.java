package nocom.common.utils;


import android.widget.Button;


public interface MsgPopupUtilsInterface {
	public enum MessageLevel {
		INFO, ATTENTION, WARNING, ERROR, FATAL,
	};


	public void showOkMsg (String title, String msg, MessageLevel messageLevel);


	public void showOkMsg (String title, String msg,
		Button.OnClickListener handler,
		MsgPopupUtils.MessageLevel messageLevel);


	public void showYesOrNoMsg (String title, String msg, String yesTxt,
		String noTxt, android.content.DialogInterface.OnClickListener yes,
		android.content.DialogInterface.OnClickListener no);
}
