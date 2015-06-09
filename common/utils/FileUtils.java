package nocom.common.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


import posix.generic.errno.errno;
import android.content.Context;
import android.util.Log;


public class FileUtils implements FileUtilsInterface {
	private Context savedContext = null;


	public FileUtils (Context c) {
		this.savedContext = c;
	}


	@Override
	public boolean isExists (String filename,
		boolean prependContextPrefix) {
		try {
			if (null == filename) {
				return false;
			}

			String path = "";
			if (prependContextPrefix) {
				path =
					this.savedContext.getFilesDir() + "/"
						+ filename;

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
				retPath =
					this.savedContext.getFilesDir() + "/"
						+ filename;
			} else {
				retPath = filename;
			}

			return retPath;
		} catch (Exception e) {
			return null;
		}
	}


	@Override
	public int write (String file, byte[] buf,
		int startIndex, int count) {
		try {
			if ((null == buf) || (buf.length <= 0)
				|| (startIndex < 0) || (count <= 0)
				|| (startIndex > (buf.length - 1))
				|| (startIndex + count > buf.length)) {
				return errno.EINVAL * -1;
			}

			if (count > buf.length) {
				count = buf.length;
			}

			FileOutputStream out = null;
			int ret;
			try {
				out =
					this.savedContext.openFileOutput(file,
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
					Log.println(Log.VERBOSE,
						"FileUtils/write/write", "write ok");

					ret = count;
				} catch (IOException e) {
					Log.println(Log.WARN,
						"FileUtils/write/write",
						"ERROR: write: " + e.getMessage());

					ret = errno.EXTRA_EEWRITE * -1;
				}
			}

			try {
				out.close();
			} catch (IOException e) {
				Log.println(Log.WARN,
					"FileOutputStream/write/close",
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
	public int append (String file, byte[] buf,
		int startIndex, int count) {
		try {
			if ((null == buf) || (buf.length <= 0)
				|| (startIndex < 0) || (count <= 0)
				|| (startIndex > (buf.length - 1))
				|| (startIndex + count > buf.length)) {
				return errno.EINVAL * -1;
			}

			if (count > buf.length) {
				count = buf.length;
			}

			FileOutputStream out = null;
			int ret;
			try {
				out =
					this.savedContext.openFileOutput(file,
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
					Log.println(Log.VERBOSE,
						"FileUtils/write/write", "write ok");

					ret = count;
				} catch (IOException e) {
					Log.println(Log.WARN,
						"FileUtils/write/write",
						"ERROR: write: " + e.getMessage());

					ret = errno.EXTRA_EEWRITE * -1;
				}
			}

			try {
				out.close();
			} catch (IOException e) {
				Log.println(Log.WARN,
					"FileOutputStream/write/close",
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
	public int read (String file, byte[] buf,
		int startIndex, int maxCount) {
		try {
			if ((null == buf) || (buf.length <= 0)
				|| (startIndex < 0) || (maxCount <= 0)
				|| (startIndex > (buf.length - 1))
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
				try {
					ret =
						in.read(buf, startIndex, maxCount);
					Log.println(Log.VERBOSE,
						"FileUtils/read/read", "read ok");
				} catch (IOException e) {
					Log.println(Log.WARN,
						"FileUtils/read/read",
						"ERROR: read: " + e.getMessage());

					ret = errno.EXTRA_EEREAD * -1;
				}
			}

			try {
				in.close();
			} catch (IOException e) {
				Log.println(Log.WARN,
					"FileOutputStream/read/close",
					"ERROR: " + e.getMessage());
			}

			return ret;
		} catch (Exception e) {
			Log.println(Log.WARN, "FileUtils/read",
				"ERROR: " + e.getMessage());
			return errno.EXTRA_EEUNRESOLVED * -1;
		}
	}
}
