
package common.utils;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import posix.generic.errno.errno;


public class StreamUtils {

	public static MyResult <Long> copy (InputStream in, OutputStream out) {
		try {
			if (null == in || null == out) {
				return new MyResult <Long>(errno.EINVAL * -1,
					"Invalid argument", null);
			}

			byte[] b = new byte[StreamUtils.IO_BUFFER_SIZE];
			int read;
			long total = 0;
			while ((read = in.read(b)) != -1) {
				out.write(b, 0, read);
				total += read;
			}

			return new MyResult <Long>(0, null, Long.valueOf(total));
		} catch (IOException ioe) {
			return new MyResult <Long>(errno.EIO * -1, "I/O error: "
				+ ioe.getMessage(), null);
		} catch (Exception e) {
			return new MyResult <Long>(errno.EXTRA_EEUNRESOLVED * -1,
				"Get unresolved exception: " + e.getMessage(), null);
		}
	}


	private static final int IO_BUFFER_SIZE = 4 * 1024;
}
