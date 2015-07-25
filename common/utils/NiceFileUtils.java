
package nocom.common.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


import posix.generic.errno.errno;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;


public class NiceFileUtils {

	public static MyResult <File> saveCompressedBitmap (Bitmap bmp,
		String dirPath, String name, int quality, boolean overwrite,
		boolean __parents) {

		try {
			int ret = NiceFileUtils.isDirExists(dirPath);
			if ((errno.ENOENT * -1) == ret) {
				MyResult <File> dir = NiceFileUtils.mkdir(dirPath, __parents);
				if (null == dir || dir.code != 0) {
					return new MyResult <File>(errno.EPERM * -1,
						dir != null ? dir.msg : "", null);
				}
			}

			MyResult <File> file = NiceFileUtils.touch(dirPath, name,
				overwrite);
			if (null == file || 0 != file.code) {
				return new MyResult <File>(file.code, file != null ? file.msg
					: "", null);
			}

			MyResult <File> retval = new MyResult <File>(0, null, file.cc);
			FileOutputStream out = new FileOutputStream(file.cc);
			try {
				/* quality e.x. 90 */
				if (!bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
					retval.code = errno.EXTRA_EEUNRESOLVED * -1;
					retval.msg = "compress fail";
					retval.cc = null;
				}
			} catch (Exception e) {
				retval.code = errno.EXTRA_EEUNRESOLVED * -1;
				retval.msg = e.getMessage();
				retval.cc = null;
			}
			out.flush();
			out.close();

			return retval;
		} catch (FileNotFoundException e) {
			return new MyResult <File>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		} catch (IOException e) {
			return new MyResult <File>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		} catch (Exception e) {
			return new MyResult <File>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <File> makeAlbumStorageDir (String albumName,
		boolean __parents) {
		try {
			/* Get the directory for the user's public pictures directory. */
			return NiceFileUtils
				.mkdir(
					Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					albumName, __parents);

		} catch (Exception e) {
			return new MyResult <File>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <String> getAlbumStorageDir (String albumName) {
		try {
			/* Get the directory for the user's public pictures directory. */
			File dir = new File(
				Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				albumName);

			return new MyResult <String>(0, null, dir.getAbsolutePath());

		} catch (Exception e) {
			return new MyResult <String>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <String> rm (File file2delete, boolean recursion,
		boolean force) {
		try {
			if (!file2delete.exists() && force) {
				return new MyResult <String>(0, null,
					file2delete.getAbsolutePath());
			} else if (!file2delete.exists()) {
				return new MyResult <String>(errno.ENOENT * -1,
					"No such file or directory", null);
			} else if (file2delete.isDirectory() && (!recursion)) {
				return new MyResult <String>(errno.EISDIR * -1,
					"Is a directory", null);
			}

			if (file2delete.isDirectory()) {
				for (File file : file2delete.listFiles()) {
					if (file.isFile()) {
						file.delete();
					} else if (file.isDirectory()) {
						MyResult <String> last = NiceFileUtils.rm(file,
							recursion, force);
						if (null == last || 0 != last.code) {
							if (!force) {
								return last;
							} else {
								return new MyResult <String>(0, null,
									file2delete.getAbsolutePath());
							}
						}
					}
				}
			}

			file2delete.delete();/* the file/folder itself */

			return new MyResult <String>(0, null,
				file2delete.getAbsolutePath());
		} catch (Exception e) {
			return new MyResult <String>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <String> rm (String path, boolean recursion,
		boolean force) {
		try {
			return NiceFileUtils.rm(new File(path), recursion, force);
		} catch (Exception e) {
			return new MyResult <String>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	// protected static File _createSDDir (String dirName) throws IOException
	// {
	// File dir = new File(_SDPATH + dirName);
	// if (Environment.getExternalStorageState().equals(
	// Environment.MEDIA_MOUNTED)) {
	//
	// System.out.println("createSDDir:" + dir.getAbsolutePath());
	// System.out.println("createSDDir:" + dir.mkdir());
	// }
	// return dir;
	// }
	//
	//
	// protected static boolean _isFileExist (String fileName) {
	// File file = new File(_SDPATH + fileName);
	// file.isFile();
	// return file.exists();
	// }
	//
	//
	// protected static void delFile (String fileName) {
	// File file = new File(_SDPATH + fileName);
	// if (file.isFile()) {
	// file.delete();
	// }
	// file.exists();
	// }
	//
	//

	//
	//
	// protected static boolean _fileIsExists (String path) {
	// try {
	// File f = new File(path);
	// if (!f.exists()) {
	// return false;
	// }
	// } catch (Exception e) {
	//
	// return false;
	// }
	// return true;
	// }

	public static int isDirExists (String path) {
		try {
			File f = new File(path);
			if (!f.exists()) {
				return errno.ENOENT * -1;
			} else if (f.isDirectory()) {
				return errno.ENOTDIR * -1;
			} else {
				return 1;
			}
		} catch (Exception e) {
			return errno.EXTRA_EEUNRESOLVED * -1;
		}
	}


	public static MyResult <File> mkdir (String path, boolean __parents) {
		try {
			File dir = new File(path);

			if (__parents && dir.exists() && dir.isDirectory()) {
				return new MyResult <File>(0, null, dir);
			}

			MyResult <String> parent = NiceFileUtils.dir(path);
			if (0 == parent.code && !path.equals(parent.cc) && __parents) {
				MyResult <File> ret = NiceFileUtils.mkdir(parent.cc,
					__parents);
				if (0 != ret.code) {
					return ret;
				}
			}

			boolean ret = dir.mkdir();

			if (ret) {
				return new MyResult <File>(0, null, dir);
			} else {
				return new MyResult <File>(errno.EPERM * -1, "EPERM", null);
			}
		} catch (Exception e) {
			return new MyResult <File>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <File> mkdir (File dirPath, String folder,
		boolean __parents) {
		try {
			File dir = new File(dirPath, folder);

			if (__parents && dir.exists() && dir.isDirectory()) {
				return new MyResult <File>(0, null, dir);
			}

			boolean ret = dir.mkdir();

			if (ret) {
				return new MyResult <File>(0, null, dir);
			} else {
				return new MyResult <File>(errno.EPERM * -1, "EPERM", null);
			}
		} catch (Exception e) {
			return new MyResult <File>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <File> touch (String dirPath, String name,
		boolean truncate) {
		try {
			File file = new File(dirPath, name);

			if (file.exists() && file.isDirectory()) {
				return new MyResult <File>(errno.EISDIR * -1,
					"Is a directory", null);
			} else if (file.exists() && file.isFile() && (!truncate)) {
				return new MyResult <File>(errno.EEXIST * -1, "File exists",
					null);
			} else if (file.exists() && file.isFile() && truncate) {
				file.delete();
			}

			return new MyResult <File>(0, null, file);
		} catch (Exception e) {
			return new MyResult <File>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <String> addToGallery (Context context, File file) {
		try {
			Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			Uri contentUri = Uri.fromFile(file);
			mediaScanIntent.setData(contentUri);
			context.sendBroadcast(mediaScanIntent);

			return new MyResult <String>(0, null, file.getAbsolutePath());
		} catch (Exception e) {
			return new MyResult <String>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <Boolean> isExists (String path) {
		try {
			if (null == path) {
				return new MyResult <Boolean>(0, null, false);
			}

			File file = new File(path);
			if (file.exists()) {
				return new MyResult <Boolean>(0, null, true);
			} else {
				return new MyResult <Boolean>(0, null, false);
			}
		} catch (Exception e) {
			return new MyResult <Boolean>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <String> getAppDirStr (Context c) {
		try {
			String ret = c.getFilesDir().getAbsolutePath();

			return new MyResult <String>(0, null, ret);
		} catch (Exception e) {
			return new MyResult <String>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <String> dir (String path) {
		try {
			if (null == path || path.length() <= 0 || path.endsWith("/")) {
				return new MyResult <String>(0, null, path);
			}

			int _ret = path.lastIndexOf("/");
			if (_ret > 0) {
				String ret = path.substring(0, _ret + 1);
				return new MyResult <String>(0, null, ret);
			} else {
				return new MyResult <String>(0, null, path);
			}
		} catch (Exception e) {
			return new MyResult <String>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <Integer> read (Context c, String file,
		byte[] buf, int startIndex, int maxCount) {
		try {
			if ((null == buf) || (buf.length <= 0) || (startIndex < 0)
				|| (maxCount <= 0) || (startIndex > (buf.length - 1))
				|| (startIndex + maxCount > buf.length)) {
				return new MyResult <Integer>(errno.EINVAL * -1,
					"Invalid argument", null);
			}

			if (maxCount > buf.length) {
				maxCount = buf.length;
			}

			FileInputStream in = null;
			int ret;
			try {
				in = c.openFileInput(file);
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

			return new MyResult <Integer>(errno.EINVAL * -1, "done", ret);
		} catch (Exception e) {
			Log.w(TAG + ":read", "ERROR: " + e.getMessage());
			return new MyResult <Integer>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <String> mv (String from, String to,
		boolean __parents) {
		try {
			MyResult <String> ret = NiceFileUtils.cp(from, to, true,
				__parents);
			if (0 != ret.code) {
				return ret;
			}

			return NiceFileUtils.rm(from, true, false);
		} catch (Exception e) {
			Log.w(TAG + ":mv", "ERROR: " + e.getMessage());
			return new MyResult <String>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	private static MyResult <String> cp (String from, String to,
		boolean recursion, boolean __parents) {
		MyResult <FILE> fw = NiceFileUtils.isWhat(from);

		return null;
	}


	private static MyResult <FILE> isWhat (String from) {
		try {
		} catch (Exception e) {
			Log.w(TAG + ":mv", "ERROR: " + e.getMessage());
			return new MyResult <FILE>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);

		}
	}


	public static enum FILE {
		dir, regular,
	};


	private static final String TAG = NiceFileUtils.class.getSimpleName();

}