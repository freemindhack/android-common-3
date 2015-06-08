package nocom.common.utils;


public interface MsgPopupUtilsInterface {
	public enum MessageLevel {
		INFO, ATTENTION, WARNING, ERROR, FATAL,
	};


	public void showOkMsg (String title, String msg,
		MessageLevel messageLevel);


	public
		void
		showOkMsg (
			String title,
			String msg,
			android.content.DialogInterface.OnClickListener handler);


	public
		void
		showYesOrNoMsg (
			String title,
			String msg,
			String yesTxt,
			String noTxt,
			android.content.DialogInterface.OnClickListener yes,
			android.content.DialogInterface.OnClickListener no);
}
