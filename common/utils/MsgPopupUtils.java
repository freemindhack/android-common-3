
package nocom.common.utils;


import java.util.ArrayList;
import java.util.List;


import com.za.smartlock.R;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class MsgPopupUtils implements MsgPopupUtilsInterface {
	private Context savedContex = null;


	public MsgPopupUtils (Context c) {
		this.savedContex = c;
	}


	@Override
	public void showOkMsg (String title, String msg,
		MsgPopupUtils.MessageLevel messageLevel) {
		AlertDialog.Builder builder = new Builder(this.savedContex);
		final AlertDialog alertDialog = builder.create();
		alertDialog.show();
		alertDialog.getWindow().setContentView(R.layout.dialog_ok);
		ImageView img = ((ImageView) alertDialog.getWindow().findViewById(
			R.id.imgViewDialogOkTitle));
		if (MsgPopupUtils.MessageLevel.FATAL == messageLevel) {
			img.setBackgroundResource(R.drawable.ic_fatal);
		} else if (MsgPopupUtils.MessageLevel.ERROR == messageLevel) {
			img.setBackgroundResource(R.drawable.ic_error);
		} else if (MsgPopupUtils.MessageLevel.WARNING == messageLevel) {
			img.setBackgroundResource(R.drawable.ic_warning);
		} else if (MsgPopupUtils.MessageLevel.ATTENTION == messageLevel) {
			img.setBackgroundResource(R.drawable.ic_attention);
		} else {
			img.setBackgroundResource(R.drawable.ic_info);
		}
		((TextView) alertDialog.getWindow().findViewById(
			R.id.textViewDialogOkTitle)).setText(title);
		((TextView) alertDialog.getWindow().findViewById(
			R.id.textViewDialogOkMsg)).setText(msg);
		alertDialog.getWindow().findViewById(R.id.btnDialogOkOk)
			.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick (View arg0) {
					alertDialog.dismiss();
				}
			});
	}


	@Override
	public void showYesOrNoMsg (String title, String msg, String yesTxt,
		String noTxt, android.content.DialogInterface.OnClickListener yes,
		android.content.DialogInterface.OnClickListener no) {
		try {
			AlertDialog.Builder builder = new Builder(this.savedContex);
			builder.setTitle(title);
			builder.setPositiveButton(yesTxt, yes);
			builder.setNegativeButton(noTxt, no);
			builder.setIcon(android.R.drawable.ic_dialog_info);
			builder.setMessage(msg);
			builder.show();
		} catch (Exception e) {
			;
		}
	}


	@Override
	public void showOkMsg (String title, String msg,
		Button.OnClickListener onOkClicked,
		MsgPopupUtils.MessageLevel messageLevel) {
		AlertDialog.Builder builder = new Builder(this.savedContex);
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
		alertDialog.getWindow().setContentView(R.layout.dialog_ok);
		dismissDialogs.add(alertDialog);
		ImageView img = ((ImageView) alertDialog.getWindow().findViewById(
			R.id.imgViewDialogOkTitle));
		if (MsgPopupUtils.MessageLevel.FATAL == messageLevel) {
			img.setBackgroundResource(R.drawable.ic_fatal);
		} else if (MsgPopupUtils.MessageLevel.ERROR == messageLevel) {
			img.setBackgroundResource(R.drawable.ic_error);
		} else if (MsgPopupUtils.MessageLevel.WARNING == messageLevel) {
			img.setBackgroundResource(R.drawable.ic_warning);
		} else if (MsgPopupUtils.MessageLevel.ATTENTION == messageLevel) {
			img.setBackgroundResource(R.drawable.ic_attention);
		} else {
			img.setBackgroundResource(R.drawable.ic_info);
		}
		((TextView) alertDialog.getWindow().findViewById(
			R.id.textViewDialogOkTitle)).setText(title);
		((TextView) alertDialog.getWindow().findViewById(
			R.id.textViewDialogOkMsg)).setText(msg);
		Button btnDialogOkOk = (Button) alertDialog.getWindow().findViewById(
			R.id.btnDialogOkOk);
		btnDialogOkOk.setOnClickListener(onOkClicked);
	}


	private static List <AlertDialog> dismissDialogs = new ArrayList <AlertDialog>();

	private static Handler dismissDialogHandler = new Handler() {
		@Override
		public void handleMessage (Message msg) {
			try {
				if (null == msg) {
					return;
				}
				if (dismissDialogs.size() <= 0) {
					return;
				}
				int n = dismissDialogs.size();
				for (int i = 0; i < n; ++i) {
					dismissDialogs.remove(0).dismiss();
				}
				dismissDialogs.clear();
			} catch (Exception e) {
				dismissDialogs.clear();
			}
		}
	};


	public static void dismissDialogs () {
		dismissDialogHandler.sendEmptyMessage(0x0);
	}
}
