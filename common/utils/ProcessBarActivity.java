package nocom.common.utils;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


import com.zaz.wifilock.R;


public abstract class ProcessBarActivity extends Activity
	implements ProcessBarInterface {

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (this.isNeedFullScreen()) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		setContentView(R.layout.activity_process_bar);

		Log.e(TAG, "onCreate");

		try {
			this.startProcessRoutine();
		} catch (Exception e) {
			Log.e(TAG + ":onCreate:startProcessRoutine",
				"ERROR: " + e.getMessage());
		}

		this.baseMs = MyTimeUtils.nowMs();
		this.processBarThread = new ProcessBarThread();
		this.processBarThread.start();

		this.textViewProgressBarMsg =
			(TextView) findViewById(R.id.textViewProgressBarMsg);

		String msg = this.getMessage();
		if (null != msg) {
			textViewProgressBarMsg.setText(msg);
		}

		Button btnProgressBarCancel =
			(Button) findViewById(R.id.btnProgressBarCancel);
		btnProgressBarCancel
			.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick (View v) {
					finish();
				}
			});
	}


	@Override
	public void onDestroy () {
		Log.v(TAG, "onDestroy");

		try {
			this.stopProcessRoutine();
		} catch (Exception e) {
			Log.e(TAG + ":onDestroy:stopProcessRoutine",
				"ERROR: " + e.getMessage());
		}

		try {
			this.processBarThread.finish();
		} catch (Exception e) {
			Log.e(TAG + ":onDestroy",
				"ERROR: " + e.getMessage());
		}

		super.onDestroy();
	}


	public void signalFinish () {
		try {
			Log.v(TAG, "signalFinish");

			this.selfHandler
				.sendEmptyMessage(ProcessBarActivity.WHAT_MSG_FINISH);

		} catch (Exception e) {
			;
		}
	}


	public class ProcessBarThread extends Thread {
		private boolean terminate = true;
		private boolean selfTerminate = false;
		public boolean running = false;


		public ProcessBarThread () {
			this.terminate = false;
			this.running = false;
			this.selfTerminate = false;
		}


		public void finish () {
			try {
				Log.v(TAG + ":ProcessBarThread", "finish");

				this.selfTerminate = true;
				this.terminate = true;

				for (long retry = 0; retry < 500; ++retry) {
					if (!this.running) {
						Log.i(TAG
							+ ":ProcessBarThread:finish",
							"OK1");

						ProcessBarActivity.this.finish();

						Log.i(TAG
							+ ":ProcessBarThread:finish",
							"OK2");
						return;
					}

					this.terminate = true;

					for (int i = 0; i < 1000; ++i) {
						this.terminate = true;
					}
				}
			} catch (Exception e) {
				Log.e(TAG + ":ProcessBarThread:finish",
					"ERROR: " + e.getMessage());

				ProcessBarActivity.this.finish();
			}

		} /* finish */


		@Override
		public void run () {
			try {
				if ((!this.running) && (!this.terminate)) {
					this.running = true;
				}

				Log.println(
					Log.VERBOSE,
					"ProcessBarActivity:ProcessBarThread:run",
					"begin");

				String msg = getMessage();
				if (null != msg) {
					textViewProgressBarMsg.setText(msg);
				}

				while ((!this.terminate)
					&& (0 != isTimeout(baseMs))) {
					try {
						Thread.sleep(50);
					} catch (Exception e) {
						;
					}

					if (isProcessRoutineDone()) {
						this.terminate = true;
						break;
					}
				}

				if (!this.selfTerminate) {
					if (ProcessRoutineState.FAIL == getProcessRoutineState()) {
						onProcessRoutineFail();
					} else if (ProcessRoutineState.SUCC == getProcessRoutineState()) {
						onProcessRoutineSucc();
					} else {
						onTimeout();
					}
				}

				Log.println(Log.INFO,
					"ProcessBarThread/run", "exit");
				this.running = false;
			} catch (Exception e) {
				this.running = false;
			}
		}
	}


	@SuppressLint ("HandlerLeak")
	private class SelfHandler extends Handler {
		@Override
		public void handleMessage (Message msg) {
			try {
				if (null == msg) {
					return;
				}

				if (ProcessBarActivity.WHAT_MSG_FINISH == msg.what) {
					finish();
				}
			} catch (Exception e) {
				;
			}
		}

	};


	/*** XXX normal fields ***/
	private long baseMs = 0;
	public ProcessBarThread processBarThread = null;
	private TextView textViewProgressBarMsg = null;
	private SelfHandler selfHandler = new SelfHandler();

	/*** XXX static final fields ***/
	private static final String TAG = "ProcessBarActivity";
	private static final int WHAT_MSG_FINISH = 0xffffff00;
}
