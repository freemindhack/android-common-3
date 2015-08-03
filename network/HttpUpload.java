
package common.network;


import http.ContentTypeMap;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;


import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;


import posix.generic.errno.errno;
import android.util.Log;


import common.utils.MyResult;


public class HttpUpload {

	/*
	 * Post file to postUrl
	 */
	public MyResult <String> asyncUpload (String filePath, String uploadUrl,
		ContentTypeMap.ContentTypeValue contentType, Charset charset,
		Header headers[]) {
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
			this.charset = charset;
			this.headers = headers;
			this.uploadUrl = uploadUrl;

			new FileEntity(this.file,
				ContentTypeMap.value2string(this.contentType) + ";charset="
					+ this.charset.toString()) {
				// this.uploadFile = new FileEntity(this.file,
				// ContentTypeMap.value2string(this.contentType)) {
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
					return true;
				}


				@Override
				public void writeTo (OutputStream os) throws IOException {
					Log.v(TAG, "writeTo");
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
				/* builder to create HTTP client */

				HttpAsyncClientBuilder builder = HttpAsyncClients.custom();

				/* verify for host name */
				/* System.setProperty("http.keepAlive", "false"); */
				final HostnameVerifier hostnameVerifier = new HostnameVerifier() {

					@Override
					public boolean verify (String hn, SSLSession ssl) {
						return true;
					}

				};

				builder.setSSLHostnameVerifier(hostnameVerifier);

				/* IO session */
				SchemeIOSessionStrategy strategy = new SchemeIOSessionStrategy() {

					@Override
					public boolean isLayeringRequired () {
						// TODO Auto-generated method stub
						return false;
					}


					@Override
					public IOSession upgrade (HttpHost arg0, IOSession arg1)
						throws IOException {
						// TODO Auto-generated method stub
						return null;
					}
				};
				builder.setSSLStrategy(strategy);

				/* connection reuse */
				ConnectionReuseStrategy reuseStrategy = new ConnectionReuseStrategy() {

					@Override
					public boolean keepAlive (HttpResponse hr, HttpContext hc) {
						return false;
					}

				};
				builder.setConnectionReuseStrategy(reuseStrategy);

				/* connection manager */
				NHttpClientConnectionManager connManager = new NHttpClientConnectionManager() {

					@Override
					public void closeExpiredConnections () {
						// TODO Auto-generated method stub

					}


					@Override
					public void closeIdleConnections (long arg0, TimeUnit arg1) {
						// TODO Auto-generated method stub

					}


					@Override
					public void execute (IOEventDispatch arg0)
						throws IOException {
					}


					@Override
					public boolean isRouteComplete (NHttpClientConnection arg0) {
						// TODO Auto-generated method stub
						return false;
					}


					@Override
					public void releaseConnection (
						NHttpClientConnection arg0, Object arg1, long arg2,
						TimeUnit arg3) {
						// TODO Auto-generated method stub

					}


					@Override
					public Future <NHttpClientConnection> requestConnection (
						HttpRoute arg0, Object arg1, long arg2, long arg3,
						TimeUnit arg4,
						FutureCallback <NHttpClientConnection> arg5) {
						// TODO Auto-generated method stub
						return null;
					}


					@Override
					public void routeComplete (NHttpClientConnection arg0,
						HttpRoute arg1, HttpContext arg2) {
						// TODO Auto-generated method stub

					}


					@Override
					public void shutdown () throws IOException {
						// TODO Auto-generated method stub

					}


					@Override
					public void startRoute (NHttpClientConnection arg0,
						HttpRoute arg1, HttpContext arg2) throws IOException {
						// TODO Auto-generated method stub

					}


					@Override
					public void upgrade (NHttpClientConnection arg0,
						HttpRoute arg1, HttpContext arg2) throws IOException {
						// TODO Auto-generated method stub

					}

				};
				builder.setConnectionManager(connManager);

				/* connection keep alive */
				ConnectionKeepAliveStrategy keepAliveStrategy = new ConnectionKeepAliveStrategy() {

					@Override
					public long getKeepAliveDuration (HttpResponse arg0,
						HttpContext arg1) {
						return 60 * 1000;
					}

				};
				builder.setKeepAliveStrategy(keepAliveStrategy);

				builder.setHttpProcessor(HttpUpload.this.httpprocessor);

				/* create HTTP client */
				CloseableHttpAsyncClient httpclient = builder.build();

				/* Start the client */
				httpclient.start();

				try {

					final HttpHost target = new HttpHost("183.237.232.202",
						80);
					final HttpPost request = new HttpPost();
					request.setHeaders(headers);
					request.setEntity(uploadFile);
					// request.setURI(new URI(uploadUrl));

					HttpAsyncRequestProducer producer = HttpAsyncMethods
						.create(target, request);

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
							// ioctrl.requestOutput();
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

	private HttpProcessor httpprocessor = new HttpProcessor() {

		@Override
		public void process (HttpRequest hr, HttpContext arg1)
			throws HttpException, IOException {
			Log.v(TAG + ":httpprocessor", "process");
		}


		@Override
		public void process (HttpResponse arg0, HttpContext arg1)
			throws HttpException, IOException {
			// TODO Auto-generated method stub
		}

	};

	private static final String TAG = HttpUpload.class.getSimpleName();

	private final CountDownLatch latch = new CountDownLatch(1);

	private FileEntity uploadFile;

	private Header headers[];

	private String uploadUrl;

	private ContentTypeMap.ContentTypeValue contentType;

	private Charset charset;

	private File file;

	private FileInputStream instream;

}
