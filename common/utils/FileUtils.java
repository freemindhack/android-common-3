
package nocom.common.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


import posix.generic.errno.errno;
import android.content.Context;
import android.util.Log;


public class FileUtils implements FileUtilsInterface {
	private Context savedContext = null;


	public FileUtils (Context c) {
		this.savedContext = c;
	}


	@Override
	public boolean isExists (String filename, boolean prependContextPrefix) {
		try {
			if (null == filename) {
				return false;
			}
			String path = "";
			if (prependContextPrefix) {
				path = this.savedContext.getFilesDir() + "/" + filename;
			} else {
				path = filename;
			}
			File file = new File(path);
			return file.exists();
		} catch (Exception e) {
			return false;
		}
	}


	@Override
	public int copyFile (String from, String to,
		boolean prependContextPrefixF, boolean prependContextPrefixT) {
		try {
			int bytesum = 0;
			int byteread = 0;
			/* FIXME: check arguments and to (file) */
			String fromPath = "";
			if (prependContextPrefixF) {
				fromPath = this.savedContext.getFilesDir() + "/" + from;
			} else {
				fromPath = from;
			}
			File fromFile = new File(fromPath);
			if (fromFile.exists()) {
				if (fromFile.isDirectory()) {
					return errno.EISDIR * -1;
				}
				InputStream in = new FileInputStream(fromPath);
				int wSize = in.available();
				String toPath = "";
				if (prependContextPrefixF) {
					toPath = this.savedContext.getFilesDir() + "/" + to;
				} else {
					toPath = to;
				}
				FileOutputStream out = new FileOutputStream(toPath);
				byte[] buffer = new byte[1024];
				int i;
				for (i = 0; i + 1024 < wSize; i += 1024) {
					byteread = in.read(buffer, 0, 1024);
					if (byteread != -1) {
						bytesum += byteread;
						out.write(buffer, 0, byteread);
					}
				}
				if (i < wSize) {
					i = wSize - i;
					byteread = in.read(buffer, 0, i);
					if (byteread != -1) {
						bytesum += byteread;
						out.write(buffer, 0, byteread);
					}
				}
				in.close();
				out.close();
				return bytesum;
			} else {
				Log.w(TAG + ":copyFile", fromPath + " not exists");
				return 0;
			}
		} catch (Exception e) {
			Log.e(TAG + ":copyFile", "ERROR: " + e.getMessage());
			return -1;
		}
	}


	@Override
	public int appendFile (String origin, String append,
		boolean prependContextPrefixA) {
		try {
			int bytesum = 0;
			int byteread = 0;
			/* FIXME: check arguments and append (file) */
			String appendPath = "";
			if (prependContextPrefixA) {
				appendPath = this.savedContext.getFilesDir() + "/" + append;
			} else {
				appendPath = append;
			}
			File appendFile = new File(appendPath);
			if (appendFile.exists()) {
				if (appendFile.isDirectory()) {
					return errno.EISDIR * -1;
				}
				InputStream in = new FileInputStream(appendFile);
				int wSize = in.available();
				FileOutputStream out = this.savedContext.openFileOutput(
					origin, Context.MODE_APPEND);
				byte[] buffer = new byte[1024];
				int i;
				for (i = 0; i + 1024 < wSize; i += 1024) {
					byteread = in.read(buffer, 0, 1024);
					if (byteread != -1) {
						bytesum += byteread;
						out.write(buffer, 0, byteread);
					}
				}
				if (i < wSize) {
					i = wSize - i;
					byteread = in.read(buffer, 0, i);
					if (byteread != -1) {
						bytesum += byteread;
						out.write(buffer, 0, byteread);
					}
				}
				in.close();
				out.close();
				return bytesum;
			} else {
				return 0;
			}
		} catch (Exception e) {
			Log.e(TAG + ":appendFile", "ERROR: " + e.getMessage());
			return -1;
		}
	}


	@Override
	public int rmFile (String filename, boolean prependContextPrefix) {
		try {
			if (null == filename) {
				return errno.EINVAL * -1;
			}
			String path = "";
			if (prependContextPrefix) {
				path = this.savedContext.getFilesDir() + "/" + filename;
			} else {
				path = filename;
			}
			File file = new File(path);
			if (file.delete()) {
				/* true: success */
				return 0;
			} else {
				return errno.EAGAIN * -1;
			}
		} catch (Exception e) {
			return -1;
		}
	}


	@Override
	public int chmod (String filename, boolean prependContextPrefix,
		String mode) {
		try {
			if (null == filename) {
				return errno.EINVAL * -1;
			}
			String path = "";
			if (prependContextPrefix) {
				path = this.savedContext.getFilesDir() + "/" + filename;
			} else {
				path = filename;
			}
			try {
				Runtime runtime = Runtime.getRuntime();
				String command = "chmod " + mode + " " + path;
				Log.v(TAG + ":chmod", "command: " + command);
				Process proc = runtime.exec(command);
				int ret = proc.waitFor();
				Log.w(TAG + ":chmod", "retval: " + ret);
				proc.destroy();
				return 0;
			} catch (IOException e) {
				Log.e(TAG + ":chmod", "ERROR: " + e.getMessage());
				return -1;
			}
		} catch (Exception e) {
			return -1;
		}
	}


	@Override
	public String getContextPrefix () {
		try {
			return this.savedContext.getFilesDir().getAbsolutePath();
		} catch (Exception e) {
			return "";
		}
	}


	@Override
	public String getAbsolutePath (String filename) {
		try {
			boolean exists;
			String retPath;
			try {
				exists = this.isExists(filename, false);
			} catch (Exception e) {
				exists = false;
			}
			if (!exists) {
				try {
					exists = this.isExists(filename, true);
				} catch (Exception e) {
					exists = false;
				}
			} else {
				return filename;
			}
			if (!exists && !filename.startsWith("/")) {
				retPath = this.savedContext.getFilesDir() + "/" + filename;
			} else {
				retPath = filename;
			}
			return retPath;
		} catch (Exception e) {
			return null;
		}
	}


	@Override
	public int write (String file, byte[] buf, int startIndex, int count) {
		try {
			if ((null == buf) || (buf.length <= 0) || (startIndex < 0)
				|| (count <= 0) || (startIndex > (buf.length - 1))
				|| (startIndex + count > buf.length)) {
				return errno.EINVAL * -1;
			}
			if (count > buf.length) {
				count = buf.length;
			}
			FileOutputStream out = null;
			int ret;
			try {
				out = this.savedContext.openFileOutput(file,
					Context.MODE_PRIVATE);
				if (null == out) {
					return errno.EXTRA_ENOPENFILEO * -1;
				} else {
					ret = 0;
				}
			} catch (Exception e) {
				ret = errno.EXTRA_EEOPENFILEO * -1;
			}
			if (0 == ret) {
				try {
					out.write(buf, startIndex, count);
					Log.println(Log.VERBOSE, "FileUtils/write/write",
						"write ok");
					ret = count;
				} catch (IOException e) {
					Log.println(Log.WARN, "FileUtils/write/write",
						"ERROR: write: " + e.getMessage());
					ret = errno.EXTRA_EEWRITE * -1;
				}
			}
			try {
				out.close();
			} catch (IOException e) {
				Log.println(Log.WARN, "FileOutputStream/write/close",
					"ERROR: " + e.getMessage());
			}
			return ret;
		} catch (Exception e) {
			Log.println(Log.WARN, "FileUtils/write",
				"ERROR: " + e.getMessage());
			return errno.EXTRA_EEUNRESOLVED * -1;
		}
	}


	@Override
	public int append (String file, byte[] buf, int startIndex, int count) {
		try {
			if ((null == buf) || (buf.length <= 0) || (startIndex < 0)
				|| (count <= 0) || (startIndex > (buf.length - 1))
				|| (startIndex + count > buf.length)) {
				return errno.EINVAL * -1;
			}
			if (count > buf.length) {
				count = buf.length;
			}
			FileOutputStream out = null;
			int ret;
			try {
				out = this.savedContext.openFileOutput(file,
					Context.MODE_APPEND);
				if (null == out) {
					return errno.EXTRA_ENOPENFILEO * -1;
				} else {
					ret = 0;
				}
			} catch (Exception e) {
				ret = errno.EXTRA_EEOPENFILEO * -1;
			}
			if (0 == ret) {
				try {
					out.write(buf, startIndex, count);
					Log.println(Log.VERBOSE, "FileUtils/write/write",
						"write ok");
					ret = count;
				} catch (IOException e) {
					Log.println(Log.WARN, "FileUtils/write/write",
						"ERROR: write: " + e.getMessage());
					ret = errno.EXTRA_EEWRITE * -1;
				}
			}
			try {
				out.close();
			} catch (IOException e) {
				Log.println(Log.WARN, "FileOutputStream/write/close",
					"ERROR: " + e.getMessage());
			}
			return ret;
		} catch (Exception e) {
			Log.println(Log.WARN, "FileUtils/write",
				"ERROR: " + e.getMessage());
			return errno.EXTRA_EEUNRESOLVED * -1;
		}
	}


	@Override
	public int read (String file, byte[] buf, int startIndex, int maxCount) {
		try {
			if ((null == buf) || (buf.length <= 0) || (startIndex < 0)
				|| (maxCount <= 0) || (startIndex > (buf.length - 1))
				|| (startIndex + maxCount > buf.length)) {
				return errno.EINVAL * -1;
			}
			if (maxCount > buf.length) {
				maxCount = buf.length;
			}
			FileInputStream in = null;
			int ret;
			try {
				in = this.savedContext.openFileInput(file);
				if (null == in) {
					ret = errno.EXTRA_ENOPENFILEI * -1;
				} else {
					ret = 0;
				}
			} catch (Exception e) {
				ret = errno.EXTRA_EEOPENFILEI * -1;
			}
			if (0 == ret) {
				int fileSize = in.available();
				if (fileSize < maxCount) {
					maxCount = fileSize;
				}
				try {
					ret = in.read(buf, startIndex, maxCount);
					Log.println(Log.VERBOSE, "FileUtils/read/read", "read ok");
				} catch (IOException e) {
					Log.println(Log.WARN, "FileUtils/read/read",
						"ERROR: read: " + e.getMessage());
					ret = errno.EXTRA_EEREAD * -1;
				}
			}
			try {
				in.close();
			} catch (IOException e) {
				Log.println(Log.WARN, "FileOutputStream/read/close",
					"ERROR: " + e.getMessage());
			}
			return ret;
		} catch (Exception e) {
			Log.println(Log.WARN, "FileUtils/read",
				"ERROR: " + e.getMessage());
			return errno.EXTRA_EEUNRESOLVED * -1;
		}
	}


	/*** XXX private static final ***/
	private static final String TAG = "FileUtils";
}
