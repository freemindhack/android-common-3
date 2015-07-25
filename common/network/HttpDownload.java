
package nocom.common.network;


import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


import nocom.common.utils.MyResult;
import nocom.common.utils.MyTimeUtils;
import nocom.common.utils.NiceFileUtils;
import posix.generic.errno.errno;
import android.util.Log;


public class HttpDownload <EXTRA_TYPE> extends Thread {

	/*
	 * timeout: if -1(or < 0): NOT timeout
	 */
	public HttpDownload (HttpDownload.DownloadArgs <EXTRA_TYPE> downloadArgs,
		long timeout) {
		try {
			Log.v(TAG, "HttpDownload");

			if (timeout > 0) {
				this.timeoutMode = HttpDownload.TimeoutMode.YES;
				this.timeout = timeout;
			} else if (timeout < 0) {
				this.timeoutMode = HttpDownload.TimeoutMode.NO;
				this.timeout = -1;
			} else {
				this.timeoutMode = HttpDownload.TimeoutMode.ONE_RUN;
				this.timeout = 0;
			}

			this.downloadArgs = downloadArgs;
			this.terminate = true;
			this.running = false;
		} catch (Exception e) {
			;
		}
	}


	public interface DownloadListener <T> {
		public void onDownloadStarted (
			HttpDownload.DownloadContent <T> downloadContent);


		public boolean overwrite (
			HttpDownload.DownloadContent <T> downloadContent);


		public void onDownloadFailed (
			HttpDownload.DownloadContent <T> downloadContent,
			MyResult <Integer> retval);


		public void onDownloadFinished (
			HttpDownload.DownloadContent <T> downloadContent);


		public void onDownloadStopped (
			HttpDownload.DownloadContent <T> downloadContent);
	}


	public static class DownloadContent <T> {
		public String httpDldUrl;

		public String saveTo;/* dir and file name */

		public long outFirstRemoteFilesize;

		public long outStarted;

		public long outFinished;

		public long outTotal;

		public T extra;
	}


	public static class DownloadArgs <T> {
		public DownloadArgs (
			HttpDownload.DownloadContent <T> downloadContent,
			HttpDownload.DownloadListener <T> downloadListener) {
			this.downloadContent = downloadContent;
			this.downloadListener = downloadListener;
		}


		public HttpDownload.DownloadContent <T> downloadContent;

		public HttpDownload.DownloadListener <T> downloadListener;
	}


	public MyResult <Integer> finish () {
		try {
			MyResult <Integer> ret = new MyResult <Integer>(0, null, null);

			if (this.isAlive() || this.running) {
				ret = this.stopDownload();
			}

			return ret;
		} catch (Exception e) {
			return new MyResult <Integer>(errno.EXTRA_EEUNRESOLVED * 1,
				e.getMessage(), null);
		}
	}


	public MyResult <Integer> startDownload (boolean started, boolean stop) {
		try {
			this.downloadArgs.downloadContent.outStarted = System
				.currentTimeMillis();

			Log.i(TAG, "startDownload");

			if (this.running && (!started)) {
				return new MyResult <Integer>(errno.EBUSY * -1,
					"start failed: already running", null);

			}

			if (this.running && started && (!stop)) {
				return new MyResult <Integer>(0, null, null);
			} else if (this.running && started && stop) {
				MyResult <Integer> ret = this.stopDownload();

				if (null == ret || 0 != ret.code) {
					return ret;
				}
			}

			if (!this.running) {
				this.terminate = false;
				this.start();
			}

			return new MyResult <Integer>(0, null, null);
		} catch (Exception e) {
			return new MyResult <Integer>(errno.EXTRA_EEUNRESOLVED * 1,
				e.getMessage(), null);
		}
	}


	public MyResult <Integer> stopDownload () {
		try {
			Log.i(TAG, "startDownload");

			if (this.isAlive() || this.running) {
				this.terminate = true;

				for (int i = 0; i < HttpDownload.STOP_RETRY; ++i) {
					if (!this.running) {
						return new MyResult <Integer>(0, null, null);
					} else {
						try {
							Thread.sleep(HttpDownload.RUN_TIME_SLICE);
						} catch (Exception e) {
							;
						}
					}
				}
			}

			if (this.isAlive() || this.running) {
				return new MyResult <Integer>(errno.EPERM * -1,
					"stop failed: still running", null);
			} else {
				return new MyResult <Integer>(0, null, null);
			}
		} catch (Exception e) {
			return new MyResult <Integer>(errno.EXTRA_EEUNRESOLVED * 1,
				e.getMessage(), null);
		}
	}


	/*** XXX @Override ***/
	@Override
	public void run () {
		try {
			this.running = true;

			Log.v(TAG, "run");

			long timeoutWhen = 0;
			if (HttpDownload.TimeoutMode.YES == this.timeoutMode) {
				timeoutWhen = MyTimeUtils.makeTimeout(this.timeout);
			}

			while (!this.terminate) {
				URL urlDownload = new URL(
					this.downloadArgs.downloadContent.httpDldUrl);
				URLConnection con = urlDownload.openConnection();

				int firstRemoteFilesize = con.getContentLength();

				Log.v(TAG + ":run", "firstRemoteFilesize: "
					+ firstRemoteFilesize);
				if (firstRemoteFilesize > 0) {
					downloadArgs.downloadContent.outFirstRemoteFilesize = firstRemoteFilesize;
					if (null != this.downloadArgs.downloadListener) {
						this.downloadArgs.downloadListener
							.onDownloadStarted(this.downloadArgs.downloadContent);
					}

					MyResult <Boolean> exists = NiceFileUtils
						.isExists(this.downloadArgs.downloadContent.saveTo);

					if (exists.cc
						&& (null == this.downloadArgs.downloadListener)) {
						break;
					}

					if (!exists.cc) {
						MyResult <String> dir = NiceFileUtils
							.dir(this.downloadArgs.downloadContent.saveTo);
						if (0 == dir.code) {
							NiceFileUtils.mkdir(dir.cc, true);
						}
					}

					if (exists.cc
						&& (null != this.downloadArgs.downloadListener)) {
						if (!this.downloadArgs.downloadListener
							.overwrite(this.downloadArgs.downloadContent)) {
							MyResult <Integer> retval = new MyResult <Integer>(
								errno.EEXIST * -1, "File exists", errno.EPERM
									* -1);
							this.downloadArgs.downloadListener
								.onDownloadFailed(
									this.downloadArgs.downloadContent, retval);
							break;
						} else {
							MyResult <String> ret = NiceFileUtils.rm(
								this.downloadArgs.downloadContent.saveTo,
								false, false);

							if (0 != ret.code) {
								MyResult <Integer> retval = new MyResult <Integer>(
									ret.code, ret.msg, errno.EPERM * -1);
								this.downloadArgs.downloadListener
									.onDownloadFailed(
										this.downloadArgs.downloadContent,
										retval);
								Log.v(TAG + ":run", "rm fail: " + ret.code
									+ " " + ret.msg);
								break;
							}
						}
					}

					byte[] buffer = new byte[2048];
					long total = 0;
					InputStream is = con.getInputStream();
					int len;
					OutputStream os = new FileOutputStream(
						this.downloadArgs.downloadContent.saveTo);

					if (TimeoutMode.YES != timeoutMode) {
						while ((!this.terminate)
							&& -1 != (len = is.read(buffer, 0, 2048))) {
							os.write(buffer, 0, len);

							total += len;
						}
					} else {
						do {
							if (MyTimeUtils.isTimeout(timeoutWhen)) {
								Log.w(TAG + ":run", "timeout");

								if (null != this.downloadArgs.downloadListener) {
									MyResult <Integer> retval = new MyResult <Integer>(
										errno.ETIME * -1, "download timeout",
										errno.EPERM * -1);
									this.downloadArgs.downloadListener
										.onDownloadFailed(
											this.downloadArgs.downloadContent,
											retval);
								}

								Log.e(TAG + ":run", "timeout");

								break;
							}

							/* Log.v(TAG + ":run", "read"); */
							len = is.read(buffer, 0, 2048);
							if (-1 != len) {
								os.write(buffer, 0, len);
								total += len;
							}
						} while ((!this.terminate) && -1 != len);
					}

					os.close();
					is.close();

					Log.v(TAG + ":run", "download finished: total: " + total);
					this.downloadArgs.downloadContent.outFinished = System
						.currentTimeMillis();
					this.downloadArgs.downloadContent.outTotal = total;
					if (null != this.downloadArgs.downloadListener) {
						this.downloadArgs.downloadListener
							.onDownloadFinished(this.downloadArgs.downloadContent);
					}
					break;
				}

				/* current one time */
				break;
			}

			/* exit */
			Log.w(TAG + ":run", "exit");
			if (null != this.downloadArgs.downloadListener) {
				this.downloadArgs.downloadListener
					.onDownloadStopped(this.downloadArgs.downloadContent);
			}
			this.running = false;
		} catch (Exception e) {
			Log.e(TAG + ":run", "ERROR: " + e.getMessage());
			if (null != this.downloadArgs.downloadListener) {
				MyResult <Integer> retval = new MyResult <Integer>(
					errno.EXTRA_EEUNRESOLVED * -1, e.getMessage(),
					errno.EPERM * -1);

				this.downloadArgs.downloadListener.onDownloadFailed(
					this.downloadArgs.downloadContent, retval);
			}
			this.running = false;
		}
	}


	private static final int STOP_RETRY = 10;

	private static final int RUN_TIME_SLICE = 20;/* 20 ms */

	private boolean terminate = true;

	private boolean running = false;

	private static final String TAG = HttpDownload.class.getSimpleName();


	private enum TimeoutMode {
		ONE_RUN, YES, NO
	};


	private TimeoutMode timeoutMode;

	private long timeout;

	private DownloadArgs <EXTRA_TYPE> downloadArgs;
}
