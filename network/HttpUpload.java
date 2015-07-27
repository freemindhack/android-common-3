
package common.network;


import http.CharsetMap;
import http.ContentTypeMap;
import http.async.AsyncHttpClient;
import http.async.response.AsyncHttpResponseHandler;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


import org.apache.http.Header;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHeader;


import posix.generic.errno.errno;


import android.util.Log;


import com.google.gson.JsonElement;
import common.utils.MyResult;


public class HttpUpload {

	/*
	 * Post file to postUrl
	 */
	public MyResult <String> AsycPost (String filePath, String postUrl,
		ContentTypeMap.ContentTypeValue contentType,
		CharsetMap.CharsetMapValue charset, ArrayList <Header> headers) {
		this.file = new File(filePath);
		try {
			this.instream = new FileInputStream(HttpUpload.this.file);
		} catch (FileNotFoundException e) {
			return new MyResult <String>(errno.ENOENT * -1, e.getMessage(),
				null);
		}

		this.contentType = contentType;
		this.charset = charset;

		FileEntity postData = new FileEntity(this.file,
			ContentTypeMap.value2string(this.contentType) + ";charset="
				+ CharsetMap.value2string(this.charset)) {

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
					ContentTypeMap.value2string(HttpUpload.this.contentType));
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

		AsyncHttpClient client = new AsyncHttpClient(postUrl);
		client.post(postData, headers, new AsyncHttpResponseHandler() {
			@Override
			public void generateContent () {
				// TODO Auto-generated method stub
			}


			@Override
			public JsonElement getContent () {
				// TODO Auto-generated method stub
				return null;
			}


			@Override
			public void onSuccess () {
				/* JsonElement result = getContent(); */
			}
		});

		return null;
	}


	private static final String TAG = HttpUpload.class.getSimpleName();

	private ContentTypeMap.ContentTypeValue contentType;

	private CharsetMap.CharsetMapValue charset;

	private File file;

	private FileInputStream instream;
}
