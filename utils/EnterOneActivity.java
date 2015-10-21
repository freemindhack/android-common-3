
package common.utils;


import generic_utils.UIUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;


import com.za.smartlock.customer.R;


public class EnterOneActivity extends Activity {

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		UIUtils.fullScreen(getWindow());

		setContentView(R.layout.activity_enter_one);

		Intent ii = this.getIntent();
		Bundle bundle;
		if (null != ii) {
			bundle = ii.getExtras();
		} else {
			bundle = null;
		}

		String title = null;
		String hint = null;
		int it = -1;
		int nh = -1;
		this.maxLen = -1;
		String digits = null;
		if (null != bundle) {
			title = bundle.getString(EnterOneActivity.KEY_TITLE_EO);
			hint = bundle.getString(EnterOneActivity.KEY_HINT_EO);
			it = bundle.getInt(EnterOneActivity.KEY_INPUTTYPE_EO);
			digits = bundle.getString(EnterOneActivity.KEY_DIGITS_EO);
			this.maxLen = bundle.getInt(EnterOneActivity.KEY_MAXLEN_EO);

			nh = bundle.getInt(EnterOneActivity.KEY_NOHIDE_EO);
		}

		if (null != title) {
			TextView tv = (TextView) findViewById(R.id.textViewAEOTitle);
			tv.setText(title);
		}

		et = (EditText) findViewById(R.id.editTextAEOInput);
		et.setCursorVisible(false);

		if (null != hint) {
			et.setHint(hint);
		}

		if (-1 != it) {
			et.setInputType(it);
		}

		if (this.maxLen > 0) {
			et.addTextChangedListener(watcher);
		}

		if (null != digits) {
			et.setKeyListener(DigitsKeyListener.getInstance(digits));
		}

		TextView tvAEOCancel = (TextView) findViewById(R.id.tvAEOCancel);

		tvAEOCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick (View v) {
				Intent intent = new Intent();
				setResult(EnterOneActivity.RESULT_CODE_EO_CANCEL, intent);

				EnterOneActivity.this.finish();
			}

		});

		TextView tvAEODone = (TextView) findViewById(R.id.tvAEODone);
		if (this.maxLen > 0 && nh <= 0) {
			tvAEODone.setVisibility(View.GONE);
		} else {
			tvAEODone.setVisibility(View.VISIBLE);

			tvAEODone.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick (View v) {
					String s = et.getEditableText().toString();
					/* Log.v(TAG, "r: " + s); */

					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putString(EnterOneActivity.KEY_RESULT_EO, s);
					intent.putExtras(bundle);
					setResult(EnterOneActivity.RESULT_CODE_EO, intent);

					EnterOneActivity.this.finish();
				}

			});
		}

	}


	@Override
	public void onResume () {
		UIUtils.dotNavigation(getWindow());

		super.onResume();
	}


	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void afterTextChanged (Editable e) {
			String i = e.toString();

			if (null != i) {
				int l = i.length();

				if (l >= maxLen) {
					if (l > maxLen) {
						et.setText(i.substring(0, maxLen));
						et.setSelection(maxLen);
					}

					if (maxLen > 0) {
						String s = et.getEditableText().toString();

						Intent intent = new Intent();
						Bundle bundle = new Bundle();
						bundle.putString(EnterOneActivity.KEY_RESULT_EO, s);
						intent.putExtras(bundle);
						setResult(EnterOneActivity.RESULT_CODE_EO, intent);

						EnterOneActivity.this.finish();
					}
				}
			}
		}


		@Override
		public void beforeTextChanged (CharSequence arg0, int arg1, int arg2,
			int arg3) {
			// TODO Auto-generated method stub

		}


		@Override
		public void onTextChanged (CharSequence arg0, int arg1, int arg2,
			int arg3) {
			// TODO Auto-generated method stub

		}

	};


	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event) {
		Log.v(TAG + ":onKeyDown", "keyCode: " + keyCode);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent();
			setResult(EnterOneActivity.RESULT_CODE_EO_CANCEL, intent);
		}

		return super.onKeyDown(keyCode, event);
	}


	public static final String KEY_TITLE_EO = "KEY_TITLE_EO";

	public static final String KEY_HINT_EO = "KEY_HINT_EO";

	public static final String KEY_INPUTTYPE_EO = "KEY_INPUTTYPE_EO";

	public static final String KEY_DIGITS_EO = "KEY_DIGITS_EO";

	public static final String KEY_MAXLEN_EO = "KEY_MAXLEN_EO";

	public static final String KEY_NOHIDE_EO = "KEY_NOHIDE_EO";

	public static final String KEY_RESULT_EO = "KEY_RESULT_EO";

	public static final int RESULT_CODE_EO = 0x222;

	public static final int RESULT_CODE_EO_CANCEL = 0x223;

	private static final String TAG = EnterOneActivity.class.getSimpleName();

	private EditText et;

	private int maxLen;
}
