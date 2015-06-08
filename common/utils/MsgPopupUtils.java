package nocom.common.utils;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.zaz.wifilock.R;


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

		final AlertDialog alertDialog = builder.create();

		alertDialog.show();

		alertDialog.getWindow().setContentView(
			R.layout.dialog_ok);

		((TextView) alertDialog.getWindow().findViewById(
			R.id.textViewDialogOkTitle)).setText(title);

		((TextView) alertDialog.getWindow().findViewById(
			R.id.textViewDialogOkMsg)).setText(msg);

		alertDialog
			.getWindow()
			.findViewById(R.id.btnDialogOkOk)
			.setOnClickListener(
				new Button.OnClickListener() {
					@Override
					public void onClick (View arg0) {
						alertDialog.dismiss();
					}

				});
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
