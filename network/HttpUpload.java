
package common.network;


import http.CharsetMap;
import http.ContentTypeMap;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;


import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.Lookup;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;


import posix.generic.errno.errno;
import android.util.Log;


import common.utils.MyResult;


public class HttpUpload {

	/*
	 * Post file to postUrl
	 */
	public MyResult <String> asyncUpload (String filePath, String postUrl,
		ContentTypeMap.ContentTypeValue contentType,
		CharsetMap.CharsetMapValue charset, Header headers[]) {
		try {
			Log.v(TAG, "asyncUpload");

			this.file = new File(filePath);

			try {
				this.instream = new FileInputStream(HttpUpload.this.file);
			} catch (FileNotFoundException e) {
				return new MyResult <String>(errno.ENOENT * -1,
					e.getMessage(), null);
			}

			this.contentType = contentType;
			// this.charset = charset;
			this.headers = headers;

			// new FileEntity(this.file,
			// ContentTypeMap.value2string(this.contentType) + ";charset="
			// + CharsetMap.value2string(this.charset)) {
			this.uploadFile = new FileEntity(this.file,
				ContentTypeMap.value2string(this.contentType)) {
				@Override
				public void consumeContent () throws IOException {
					// TODO Auto-generated method stub
				}


				@Override
				public InputStream getContent () throws IOException,
					IllegalStateException {
					return HttpUpload.this.instream;
				}


				@Override
				public Header getContentEncoding () {
					return new BasicHeader("charset", "utf-8");
				}


				@Override
				public long getContentLength () {
					try {
						return HttpUpload.this.instream.available();
					} catch (IOException e) {
						Log.e(TAG + "", "E: " + e.getMessage());

						return 0;
					}
				}


				@Override
				public Header getContentType () {
					return new BasicHeader("content-type",
						ContentTypeMap
							.value2string(HttpUpload.this.contentType));
				}


				@Override
				public boolean isChunked () {
					return false;
				}


				@Override
				public boolean isRepeatable () {
					return false;
				}


				@Override
				public boolean isStreaming () {
					return false;
				}


				@Override
				public void writeTo (OutputStream os) throws IOException {
					// TODO Auto-generated method stub
				}

			};

			try {
				new UploadThread().start();
			} catch (Exception e) {
				;
			}

			return new MyResult <String>(0, null, filePath);
		} catch (Exception e) {
			Log.e(TAG + ":asycPost", "E: " + e.getMessage());

			return new MyResult <String>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	private class UploadThread extends Thread {
		@Override
		public void run () {
			try {
				Lookup <AuthSchemeProvider> authSchemeRegistry = new Lookup <AuthSchemeProvider>() {

					@Override
					public AuthSchemeProvider lookup (String v) {
						// TODO Auto-generated method stub
						return null;
					}

				};

				CloseableHttpAsyncClient httpclient = HttpAsyncClients
					.custom()
					.setDefaultAuthSchemeRegistry(authSchemeRegistry).build();

				/* Start the client */
				httpclient.start();

				try {
					HttpAsyncRequestProducer producer = null;

					final HttpPost request = new HttpPost();
					request.setHeaders(headers);
					request.setEntity(uploadFile);

					producer = HttpAsyncMethods.create(request);

					AsyncCharConsumer <HttpResponse> consumer = new AsyncCharConsumer <HttpResponse>() {

						HttpResponse response;


						@Override
						protected void onResponseReceived (
							final HttpResponse response) {
							this.response = response;
						}


						@Override
						protected void onCharReceived (final CharBuffer buf,
							final IOControl ioctrl) throws IOException {
							// Do something useful
						}


						@Override
						protected void releaseResources () {
						}


						@Override
						protected HttpResponse buildResult (
							final HttpContext context) {
							return this.response;
						}

					};

					/*
					 * Execute request
					 * and wait until response is received
					 */

					Future <HttpResponse> future = httpclient.execute(
						producer, consumer, uploadCallback);

					HttpResponse response = future.get();

					latch.await();

					Log.v(
						TAG,
						request.getRequestLine() + " -> "
							+ response.getStatusLine());

				} catch (Exception e) {
					;
				} finally {
					if (null != httpclient) {
						httpclient.close();
					}
					super.run();
				}
			} catch (Exception e) {
				;
			}
		}
	}


	private FutureCallback <HttpResponse> uploadCallback = new FutureCallback <HttpResponse>() {

		@Override
		public void cancelled () {
			latch.countDown();
		}


		@Override
		public void completed (HttpResponse arg0) {
			latch.countDown();
		}


		@Override
		public void failed (Exception arg0) {
			latch.countDown();
		}

	};

	private static final String TAG = HttpUpload.class.getSimpleName();

	private final CountDownLatch latch = new CountDownLatch(1);

	private FileEntity uploadFile;

	private Header headers[];

	private ContentTypeMap.ContentTypeValue contentType;

	// private CharsetMap.CharsetMapValue charset;

	private File file;

	private FileInputStream instream;

}
