package nocom.common.utils;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;


public class MsgPopupUtils implements
	MsgPopupUtilsInterface {

	private Context savedContex = null;


	public MsgPopupUtils (Context c) {
		this.savedContex = c;
	}


	@Override
	public void showOkMsg (String title, String msg) {
		AlertDialog.Builder builder =
			new Builder(this.savedContex);
		builder.setTitle(title);
		builder
			.setPositiveButton(
				"好",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick (
						DialogInterface dialog, int which) {
						;
					}
				});

		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setMessage(msg);
		builder.show();
	}


	@Override
	public
		void
		showYesOrNoMsg (
			String title,
			String msg,
			String yesTxt,
			String noTxt,
			android.content.DialogInterface.OnClickListener yes,
			android.content.DialogInterface.OnClickListener no) {
		try {
			AlertDialog.Builder builder =
				new Builder(this.savedContex);
			builder.setTitle(title);
			builder.setPositiveButton(yesTxt, yes);

			builder.setNegativeButton(noTxt, no);
			builder
				.setIcon(android.R.drawable.ic_dialog_info);
			builder.setMessage(msg);
			builder.show();
		} catch (Exception e) {
			;
		}
	}


	@Override
	public
		void
		showOkMsg (
			String title,
			String msg,
			android.content.DialogInterface.OnClickListener handler) {
		AlertDialog.Builder builder =
			new Builder(this.savedContex);

		builder.setTitle(title);
		builder.setPositiveButton("好", handler);

		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setMessage(msg);
		builder.show();
	}
}
