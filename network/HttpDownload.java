
package common.network;


import generic_utils.NiceFileUtils;


import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


import common.utils.MyResult;
import common.utils.MyTimeUtils;
import posix.generic.errno.errno;
import android.util.Log;


public class HttpDownload <EXTRA_TYPE> {

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
		/* none block */
		public void onDownloadStarted (
			HttpDownload.DownloadContent <T> downloadContent);


		/* block */
		public boolean overwrite (
			HttpDownload.DownloadContent <T> downloadContent);


		/* none block */
		public void onDownloadFailed (
			HttpDownload.DownloadContent <T> downloadContent,
			MyResult <Integer> retval);


		/* none block */
		public void onDownloadFinished (
			HttpDownload.DownloadContent <T> downloadContent);


		/* none block */
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

			if (this.running) {
				ret = this.stopDownload();
			}

			return ret;
		} catch (Exception e) {
			return new MyResult <Integer>(errno.EXTRA_EEUNRESOLVED * 1,
				"ERROR: " + e.getMessage(), null);
		}
	}


	private boolean started, stop;

	private static int nnn = 0;


	/* none block */
	public MyResult <Integer> startDownload (boolean started, boolean stop) {
		try {
			this.started = started;
			this.stop = stop;

			new Thread() {
				@Override
				public void run () {
					try {
						HttpDownload.this.downloadArgs.downloadContent.outStarted = System
							.currentTimeMillis();

						Log.i(TAG, "startDownload");

						if (HttpDownload.this.running
							&& (!HttpDownload.this.started)) {
							MyResult <Integer> ret = new MyResult <Integer>(
								errno.EBUSY * -1,
								"start failed: already running", null);

							if ((null != HttpDownload.this.downloadArgs)
								&& (null != HttpDownload.this.downloadArgs.downloadListener)) {
								HttpDownload.this.downloadArgs.downloadListener
									.onDownloadFailed(
										HttpDownload.this.downloadArgs.downloadContent,
										ret);
								return;
							}
						}

						if (HttpDownload.this.running
							&& HttpDownload.this.started
							&& (!HttpDownload.this.stop)) {
							return;/* ok */
						} else if (HttpDownload.this.running
							&& HttpDownload.this.started
							&& HttpDownload.this.stop) {
							MyResult <Integer> ret = HttpDownload.this
								.stopDownload();

							if (0 != ret.code) {
								if ((null != HttpDownload.this.downloadArgs)
									&& (null != HttpDownload.this.downloadArgs.downloadListener)) {
									HttpDownload.this.downloadArgs.downloadListener
										.onDownloadFailed(
											HttpDownload.this.downloadArgs.downloadContent,
											ret);
								}
								return;
							}
						}

						if (!HttpDownload.this.running) {
							HttpDownload.this.terminate = false;
							++nnn;
							Log.v(TAG + "start main thread: ", "n: " + nnn);

							/* FIXME: the BAD start */
							/* HttpDownload.this.start(); */
							HttpDownload.this.run();/* now just use run */
						}
						/* ok */
					} catch (Exception e) {
						Log.e(TAG + ":startDownload:thread",
							"ERROR: " + e.getMessage());
						MyResult <Integer> ret = new MyResult <Integer>(
							errno.EXTRA_EEUNRESOLVED * -1, e.getMessage(),
							null);
						if ((null != HttpDownload.this.downloadArgs)
							&& (null != HttpDownload.this.downloadArgs.downloadListener)) {
							HttpDownload.this.downloadArgs.downloadListener
								.onDownloadFailed(
									HttpDownload.this.downloadArgs.downloadContent,
									ret);
						}
					}
				}
			}.start();

			return new MyResult <Integer>(0, null, null);
		} catch (Exception e) {
			return new MyResult <Integer>(errno.EXTRA_EEUNRESOLVED * 1,
				"ERROR: " + e.getMessage(), null);
		}
	}


	/* block */
	public MyResult <Integer> stopDownload () {
		try {
			Log.i(TAG, "stopDownload");

			if (this.running) {
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

			if (this.running) {
				return new MyResult <Integer>(errno.EPERM * -1,
					"stop failed: still running", null);
			} else {
				return new MyResult <Integer>(0, null, null);
			}
		} catch (Exception e) {
			return new MyResult <Integer>(errno.EXTRA_EEUNRESOLVED * 1,
				"ERROR: " + e.getMessage(), null);
		}
	}


	MyResult <String> rmOldRet;


	private void run () {
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
						new Thread() {
							@Override
							public void run () {
								try {
									HttpDownload.this.downloadArgs.downloadListener
										.onDownloadStarted(HttpDownload.this.downloadArgs.downloadContent);
								} catch (Exception e) {
									Log.e(TAG
										+ ":run:onDownloadStarted:thread",
										"ERROR: " + e.getMessage());

								}
							}
						}.start();
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

							new Thread() {
								@Override
								public void run () {
									try {
										MyResult <Integer> retval = new MyResult <Integer>(
											errno.EEXIST * -1, "File exists",
											errno.EPERM * -1);
										HttpDownload.this.downloadArgs.downloadListener
											.onDownloadFailed(
												HttpDownload.this.downloadArgs.downloadContent,
												retval);
									} catch (Exception e) {
										Log.e(TAG
											+ ":run:onDownloadFailed:thread",
											"ERROR: " + e.getMessage());

									}
								}
							}.start();
							break;
						} else {
							rmOldRet = NiceFileUtils.rm(
								this.downloadArgs.downloadContent.saveTo,
								false, false);

							if (0 != rmOldRet.code) {
								new Thread() {
									@Override
									public void run () {
										try {
											MyResult <Integer> retval = new MyResult <Integer>(
												rmOldRet.code, rmOldRet.msg,
												errno.EPERM * -1);
											HttpDownload.this.downloadArgs.downloadListener
												.onDownloadFailed(
													HttpDownload.this.downloadArgs.downloadContent,
													retval);
											Log.v(TAG + ":run", "rm fail: "
												+ rmOldRet.code + " "
												+ rmOldRet.msg);
										} catch (Exception e) {
											Log.e(
												TAG
													+ ":run:onDownloadFailed:thread",
												"ERROR: " + e.getMessage());

										}
									}
								}.start();
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
									new Thread() {
										@Override
										public void run () {
											try {
												MyResult <Integer> retval = new MyResult <Integer>(
													errno.ETIME * -1,
													"download timeout",
													errno.EPERM * -1);
												HttpDownload.this.downloadArgs.downloadListener
													.onDownloadFailed(
														HttpDownload.this.downloadArgs.downloadContent,
														retval);
											} catch (Exception e) {
												Log.e(
													TAG
														+ ":run:onDownloadFailed:thread",
													"ERROR: "
														+ e.getMessage());

											}
										}
									}.start();
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
						new Thread() {
							@Override
							public void run () {
								try {
									HttpDownload.this.downloadArgs.downloadListener
										.onDownloadFinished(HttpDownload.this.downloadArgs.downloadContent);
								} catch (Exception e) {
									Log.e(TAG
										+ ":run:onDownloadFinished:thread",
										"ERROR: " + e.getMessage());
								}
							}
						}.start();
					}
					break;
				}

				/* current one time */
				break;
			}

			/* exit */
			Log.w(TAG + ":run", "exit");
			if (null != this.downloadArgs.downloadListener) {
				new Thread() {
					@Override
					public void run () {
						try {
							HttpDownload.this.downloadArgs.downloadListener
								.onDownloadStopped(HttpDownload.this.downloadArgs.downloadContent);
						} catch (Exception e) {
							Log.e(TAG + ":run:onDownloadStopped:thread",
								"ERROR: " + e.getMessage());
						}
					}
				}.start();
			}
			this.running = false;
		} catch (Exception e) {
			Log.e(TAG + ":run", "ERROR: " + e.getMessage());
			if (null != this.downloadArgs.downloadListener) {
				new Thread() {
					@Override
					public void run () {
						try {
							MyResult <Integer> retval = new MyResult <Integer>(
								errno.EXTRA_EEUNRESOLVED * -1, "",
								errno.EPERM * -1);

							HttpDownload.this.downloadArgs.downloadListener
								.onDownloadFailed(
									HttpDownload.this.downloadArgs.downloadContent,
									retval);
						} catch (Exception e) {
							Log.e(TAG + ":run:onDownloadFailed:thread",
								"ERROR: " + e.getMessage());

						}
					}
				}.start();
			}
			this.running = false;
		}
	}


	private static final int STOP_RETRY = 500;

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
