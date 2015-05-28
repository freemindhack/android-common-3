package nocom.common.utils;


public interface MsgPopupUtilsInterface {
	public void showOkMsg (String title, String msg);


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
