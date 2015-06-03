package nocom.common.utils;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


import com.zaz.wifilock.R;


public abstract class ProcessBarActivity extends Activity
	implements ProcessBarInterface {
	private long baseMs = 0;
	private ProcessBarThread processBarThread = null;
	TextView textViewProgressBarMsg = null;


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

		Runnable processRoutineRunnable =
			this.processRoutine();
		if (null != processRoutineRunnable) {
			Thread processRuntine =
				new Thread(processRoutineRunnable);
			processRuntine.start();
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
					stopProcessBarRunnable();

					finish();
				}
			});
	}


	@Override
	public void onDestroy () {
		this.stopProcessBarRunnable();

		super.onDestroy();
	}


	private void stopProcessBarRunnable () {
		try {
			this.processBarThread.signalTerminate();

			int nto = 50;

			while (this.processBarThread.isRunning()
				&& (--nto > 0)) {
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					;
				}
			}
		} catch (Exception e) {
			;
		}
	}


	private class ProcessBarThread extends Thread {
		private boolean terminate = true;
		public boolean running = false;


		public ProcessBarThread () {
			this.terminate = false;
			this.running = false;
		}


		public void signalTerminate () {
			this.terminate = true;
		}


		public boolean isRunning () {
			return this.running;
		}


		@Override
		public void run () {
			try {
				Log.println(Log.VERBOSE, "run", "begin");

				if (!this.running) {
					this.running = true;
				}

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

				if (ProcessRoutineState.FAIL == getProcessRoutineState()) {
					onProcessRoutineFail();
				} else if (ProcessRoutineState.SUCC == getProcessRoutineState()) {
					onProcessRoutineSucc();
				} else {
					if (!this.terminate) {
						onTimeout();
					}
				}

				Log.println(Log.VERBOSE,
					"ProcessBarThread/run", "exit");
				this.running = false;
			} catch (Exception e) {
				this.running = false;
			}
		}
	}
}
