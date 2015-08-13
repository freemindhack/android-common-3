
package common.network;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;


import posix.generic.errno.errno;
import android.util.Log;


import common.utils.MyResult;


public class HttpUpload {
	public interface HttpUploadListener {
		public void onFinished (MyResult <Long> retval);
	}


	public void setKEY_UPLOAD (String KEY_UPLOAD) {
		this.KEY_UPLOAD = KEY_UPLOAD;
	}


	public String getKEY_UPLOAD () {
		return this.KEY_UPLOAD;
	}


	/*
	 * start to upload file to uploadUrl
	 */
	public MyResult <String> startUploadFile (String filePath,
		String uploadUrl, org.apache.http.entity.ContentType contentType,
		Charset charset) {
		try {
			Log.v(TAG, "startUploadFile");

			/* FIXME: check all arguments first */

			this.file = new File(filePath);
			if (!file.exists()) {
				return new MyResult <String>(errno.ENOENT * -1,
					"No such file or directory", null);
			}

			this.uploadUrl = uploadUrl;

			try {
				new UploadThread(contentType, charset).start();
			} catch (Exception e) {
				;
			}

			return new MyResult <String>(0, null, filePath);
		} catch (Exception e) {
			Log.e(TAG + ":startUploadFile", "E: " + e.getMessage());

			return new MyResult <String>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public void setHttpUploadListener (HttpUploadListener httpUploadListener) {
		this.httpUploadListener = httpUploadListener;
	}


	public HttpUploadListener getHttpUploadListener () {
		return this.httpUploadListener;
	}


	private class UploadThread extends Thread {
		public UploadThread (org.apache.http.entity.ContentType contentType,
			Charset charset) {
			this.contentType = contentType;
			this.charset = charset;
		}


		@Override
		public void run () {
			try {
				Log.v(TAG, "run");

				final String BOUNDARY = "******";
				final String HYPHENS = "--";
				final String LINEEND = "\n";

				try {
					MyResult <Long> retval = new MyResult <Long>(0, null, 0L);

					URL url = new URL(HttpUpload.this.uploadUrl);
					HttpURLConnection httpURLConnection = null;
					try {
						httpURLConnection = (HttpURLConnection) url
							.openConnection();
					} catch (IOException e) {
						retval.code = errno.EIO * -1;
						retval.msg = e.getMessage();
						Log.e(TAG, "E: openConnection: " + e.getMessage());
					}

					OutputStream os = null;
					if (0 == retval.code) {
						/*
						* 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
						* 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP请求正文的流
						*/
						httpURLConnection.setChunkedStreamingMode(128 * 1024);/* 128K */
						/* 允许输入输出流 */
						httpURLConnection.setDoInput(true);
						httpURLConnection.setDoOutput(true);
						httpURLConnection.setUseCaches(false);

						/* 使用POST方法 */
						httpURLConnection.setRequestMethod("POST");
						httpURLConnection.setRequestProperty("Connection",
							"Keep-Alive");
						/* "UTF-8" */
						httpURLConnection.setRequestProperty("Charset",
							this.charset.name());
						/* multipart/form-data */
						httpURLConnection.setRequestProperty("Content-Type",
							this.contentType.getMimeType() + ";boundary="
								+ BOUNDARY);

						try {
							os = httpURLConnection.getOutputStream();
						} catch (IOException e) {
							retval.code = errno.EIO * -1;
							retval.msg = e.getMessage();
							Log.e(TAG,
								"E: getOutputStream: " + e.getMessage());
						}
					}

					DataOutputStream no = null;
					if (0 == retval.code) {
						no = new DataOutputStream(os);
					}

					if (0 == retval.code) {
						String path = HttpUpload.this.file.getAbsolutePath();
						String filename = path.substring(path
							.lastIndexOf("/") + 1);

						try {
							no.writeBytes(HYPHENS + BOUNDARY + LINEEND);

							no.writeBytes("Content-Disposition: form-data; name=\""
								+ KEY_UPLOAD
								+ "\"; filename=\""
								+ filename
								+ "\"" + LINEEND);
							no.writeBytes(LINEEND);
						} catch (IOException e) {
							retval.code = errno.EIO * -1;
							retval.msg = e.getMessage();
							Log.e(TAG, "E: writeBytes: " + e.getMessage());
						}
					}

					FileInputStream fi = null;
					if (0 == retval.code) {
						try {
							fi = new FileInputStream(
								HttpUpload.this.file.getAbsolutePath());
						} catch (FileNotFoundException e) {
							retval.code = errno.ENOENT * -1;
							retval.msg = e.getMessage();
							fi = null;
							Log.e(TAG,
								"E: FileInputStream: " + e.getMessage());
						}
					}

					long total = 0;
					if (0 == retval.code) {
						byte[] buffer = new byte[8192];/* 8 KB */
						/* 读取文件 */
						int count = 0;

						try {
							while ((count = fi.read(buffer, 0, 8192)) != -1) {
								no.write(buffer, 0, count);
								total += count;
							}
						} catch (Exception e) {
							retval.code = errno.EIO * -1;
							retval.msg = e.getMessage();
							Log.e(TAG, "E: read and write: " + e.getMessage());
						}

						try {
							fi.close();
						} catch (Exception e) {
							Log.e(TAG, "E: close: ignore: " + e.getMessage());
						}

					}

					Log.i(TAG, "transfer content bytes: " + total);
					retval.cc = total;

					if (0 == retval.code) {
						try {
							no.writeBytes(LINEEND);
							no.writeBytes(HYPHENS + BOUNDARY + HYPHENS
								+ LINEEND);
							no.flush();
						} catch (Exception e) {
							Log.e(
								TAG,
								"E: read and write: ignore: "
									+ e.getMessage());
						}

						/* get reply */
						InputStream ni = httpURLConnection.getInputStream();

						InputStreamReader isr = new InputStreamReader(ni,
							"utf-8");

						BufferedReader br = new BufferedReader(isr);
						do {
							String result;
							try {
								result = br.readLine();
								if (null == result || result.length() <= 0) {
									break;
								}
							} catch (Exception e) {
								break;
							}

							Log.i(TAG, "response: " + result);
						} while (true);

						ni.close();
					}

					httpURLConnection.disconnect();
					no.close();
					os.close();

					super.run();
				} catch (Exception e) {
					;
				} finally {
					super.run();
				}
			} catch (Exception e) {
				;
			}
		}


		private org.apache.http.entity.ContentType contentType;

		private Charset charset;
	}


	private static final String TAG = HttpUpload.class.getSimpleName();

	private String KEY_UPLOAD = "upload";

	private String uploadUrl;

	private HttpUploadListener httpUploadListener;

	private File file;
}
