
package common.utils;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.za.smartlock.manufacturer.R;


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
		maxLen = -1;
		if (null != bundle) {
			title = bundle.getString(EnterOneActivity.KEY_TITLE_EO);
			hint = bundle.getString(EnterOneActivity.KEY_HINT_EO);
			it = bundle.getInt(EnterOneActivity.KEY_INPUTTYPE_EO);
			maxLen = bundle.getInt(EnterOneActivity.KEY_MAXLEN_EO);
		}

		if (null != title) {
			TextView tv = (TextView) findViewById(R.id.textViewAEOTitle);
			tv.setText(title);
		}

		et = (EditText) findViewById(R.id.editTextAEOInput);

		if (null != hint) {
			et.setHint(hint);
		}

		if (-1 != it) {
			et.setInputType(InputType.TYPE_CLASS_NUMBER);
		}

		if (-1 != maxLen) {
			et.addTextChangedListener(watcher);
		}

		RelativeLayout relativeLayoutAEOCancel = (RelativeLayout) findViewById(R.id.relativeLayoutAEOCancel);
		relativeLayoutAEOCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick (View v) {
				EnterOneActivity.this.finish();
			}

		});

		RelativeLayout relativeLayoutAEODone = (RelativeLayout) findViewById(R.id.relativeLayoutAEODone);
		relativeLayoutAEODone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick (View v) {
				String s = et.getEditableText().toString();
				Log.v(TAG, "r: " + s);

				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString(EnterOneActivity.KEY_RESULT_EO, s);
				intent.putExtras(bundle);
				setResult(EnterOneActivity.RESULT_CODE_EO, intent);

				EnterOneActivity.this.finish();
			}

		});

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

			if (null != i && i.length() > maxLen) {
				et.setText(i.substring(0, maxLen));
				et.setSelection(maxLen);
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

	public static final String KEY_TITLE_EO = "KEY_TITLE_EO";

	public static final String KEY_HINT_EO = "KEY_HINT_EO";

	public static final String KEY_INPUTTYPE_EO = "KEY_INPUTTYPE_EO";

	public static final String KEY_MAXLEN_EO = "KEY_MAXLEN_EO";

	public static final String KEY_RESULT_EO = "KEY_RESULT_EO";

	public static final int RESULT_CODE_EO = 0x222;

	private static final String TAG = EnterOneActivity.class.getSimpleName();

	private EditText et;

	private int maxLen;
}
