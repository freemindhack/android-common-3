
package common.utils;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


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


	public static MyResult <String> rmGallery (File file2delete,
		boolean recursion, boolean force, Context context) {
		try {
			Log.v(TAG + ":rmGallery", "f: " + file2delete);

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
						NiceFileUtils.refreshGallery(context, file);
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
			NiceFileUtils.refreshGallery(context, file2delete);

			return new MyResult <String>(0, null,
				file2delete.getAbsolutePath());
		} catch (Exception e) {
			return new MyResult <String>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


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

			boolean ret;
			if (__parents) {
				Log.v(TAG + ":mkdir", "mkdirs: " + dir.getAbsolutePath());
				ret = dir.mkdirs();
			} else {
				Log.v(TAG + ":mkdir", "mkdir: " + dir.getAbsolutePath());
				ret = dir.mkdir();
			}

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

			boolean ret;
			if (__parents) {
				ret = dir.mkdirs();
			} else {
				ret = dir.mkdir();
			}

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

			if (file.createNewFile()) {
				return new MyResult <File>(0, null, file);
			} else {
				return new MyResult <File>(errno.EPERM * -1,
					"Operation not permitted: createNewFile fail", null);
			}
		} catch (Exception e) {
			return new MyResult <File>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <String> refreshGallery (Context context, File file) {
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


	public static MyResult <String> refreshGallery (Context context,
		String filePath) {
		try {
			Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			Uri contentUri = Uri.fromFile(new File(filePath));
			mediaScanIntent.setData(contentUri);
			context.sendBroadcast(mediaScanIntent);

			return new MyResult <String>(0, null, filePath);
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

			File f = new File(path);

			if (f.isFile() || (!f.exists() && !path.endsWith("/"))) {
				String ret = f.getParent();
				return new MyResult <String>(0, null, ret);
			} else {
				return new MyResult <String>(0, null, path);
			}
		} catch (Exception e) {
			return new MyResult <String>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <String> parent (String path) {
		try {
			if (null == path || path.length() <= 0) {
				return new MyResult <String>(errno.EINVAL * -1,
					"Invalid argument", null);
			}

			File f = new File(path);

			if (path.equals("/")) {
				return new MyResult <String>(0, null, path);
			} else if (f.isFile() || path.contains("/")) {
				return new MyResult <String>(0, null, f.getParent());
			} else {
				return new MyResult <String>(errno.EINVAL * -1,
					"Invalid argument " + path, null);
			}
		} catch (Exception e) {
			return new MyResult <String>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <Integer> read (String filePath, byte[] buf,
		int startIndex, int maxCount) {
		try {
			Log.v(TAG + ":read", "file: " + filePath);

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
			int ret = 0;
			String msg = null;
			try {
				File file = new File(filePath);

				in = new FileInputStream(file);
			} catch (Exception e) {
				msg = e.getMessage();
				Log.e(TAG + ":read", "E: " + msg);
				ret = errno.EXTRA_EEOPENFILEI * -1;
			}

			if (0 == ret) {
				int fileSize = in.available();
				if (fileSize < maxCount) {
					maxCount = fileSize;
				}
				try {
					ret = in.read(buf, startIndex, maxCount);
					Log.v(TAG + ":read", "ok");
				} catch (IOException e) {
					msg = e.getMessage();
					Log.w(TAG + ":read", "ERROR: " + msg);
					ret = errno.EXTRA_EEREAD * -1;
				}
			}
			if (null != in) {
				try {
					in.close();
				} catch (Exception e) {
					Log.w(TAG + ":read", "ERROR: " + e.getMessage());
				}
			}

			if (ret < 0) {
				return new MyResult <Integer>(ret, msg, null);
			} else {
				return new MyResult <Integer>(0, msg, ret);
			}
		} catch (Exception e) {
			Log.w(TAG + ":read", "ERROR: " + e.getMessage());
			return new MyResult <Integer>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <String> mv (String from, String to,
		boolean overwrite) {
		try {
			Log.v(TAG + ":mv", "from: " + from + " to: " + to);

			boolean done = false;
			try {
				File fromf = new File(from);
				if (fromf.renameTo(new File(to))) {
					done = true;
				} else {
					done = false;
				}
			} catch (Exception e) {
				done = false;
				Log.e(TAG + ":mv", "E: " + e.getMessage());
			}

			if (done) {
				Log.v(TAG + ":mv", "fast mv done");
				return new MyResult <String>(0, null, to);
			}

			MyResult <String> ret = NiceFileUtils.cp(from, to, true,
				overwrite);
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


	public static MyResult <String> cp (String from, String to,
		boolean recursion, boolean overwrite) {
		try {

			MyResult <FILE> fwfrom = NiceFileUtils.isWhat(from);

			if (0 != fwfrom.code) {
				return new MyResult <String>(fwfrom.code, fwfrom.msg, null);
			}

			MyResult <Boolean> e = NiceFileUtils.isExists(from);
			if (0 != e.code) {
				return new MyResult <String>(e.code, e.msg, null);
			}

			if (!e.cc) {
				return new MyResult <String>(errno.ENOENT * -1,
					"No such file or directory " + from, null);
			}

			MyResult <FILE> fwto = NiceFileUtils.isWhat(to);

			if (0 != fwto.code) {
				return new MyResult <String>(fwto.code, fwto.msg, null);
			}

			e = NiceFileUtils.isExists(to);
			if (0 != e.code) {
				return new MyResult <String>(e.code, e.msg, null);
			}
			boolean isFromDir;
			if (FILE.directory == fwfrom.cc.file()) {
				isFromDir = true;

				if (!recursion) {
					return new MyResult <String>(errno.EISDIR * -1,
						"Is a directory " + from, null);
				} else if ((FILE.directory != (FILE.directory & fwto.cc
					.file()))) {
					return new MyResult <String>(errno.ENOTDIR * -1,
						"Not a directory " + to, null);
				}
			} else {
				isFromDir = false;

				if (!e.cc && FILE.directory == fwto.cc.file()) {
					return new MyResult <String>(errno.ENOTDIR * -1,
						"Not a directory " + to, null);
				}
			}

			if (from.equals(to)) {
				return new MyResult <String>(0, "do nothing", null);
			}

			if (!e.cc) {
				MyResult <String> parent = NiceFileUtils.parent(to);
				if (0 != parent.code) {
					return new MyResult <String>(parent.code, parent.msg,
						null);
				}

				e = NiceFileUtils.isExists(parent.cc);
				if (0 != e.code) {
					return new MyResult <String>(e.code, e.msg, null);
				}

				if (!e.cc) {
					return new MyResult <String>(errno.ENOENT * -1,
						"No such file or directory " + parent, null);
				}
				// } else if (!overwrite) {
				// return new MyResult <String>(errno.EPERM * -1,
				// "Operation not permitted: overwrite", null);
			}

			return NiceFileUtils.__cp(from, isFromDir, to, overwrite);
		} catch (Exception e) {
			Log.w(TAG + ":cp", "ERROR: " + e.getMessage());
			return new MyResult <String>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	private static MyResult <String> __cp (String fromPath,
		boolean isFromDir, String toPath, boolean overwrite) {
		try {
			if (!isFromDir) {
				InputStream in = null;
				OutputStream out = null;

				BufferedInputStream bin = null;
				BufferedOutputStream bout = null;

				try {
					File from = new File(fromPath);
					File to = new File(toPath);
					if (to.exists() && !overwrite) {
						return new MyResult <String>(errno.EPERM * -1,
							"Operation not permitted: overwrite", null);
					}
					in = new FileInputStream(from);
					out = new FileOutputStream(to);
					bin = new BufferedInputStream(in);
					bout = new BufferedOutputStream(out);

					byte[] b = new byte[4096];
					int len = bin.read(b, 0, 4096);
					while (len != -1) {
						bout.write(b, 0, len);
						len = bin.read(b);
					}
				} catch (FileNotFoundException e) {
					return new MyResult <String>(errno.ENOENT * -1,
						e.getMessage(), null);
				} catch (IOException e) {
					return new MyResult <String>(errno.ENOENT * -1,
						e.getMessage(), null);
				} finally {
					try {
						if (bin != null) {
							bin.close();
						}
						if (bout != null) {
							bout.close();
						}
					} catch (IOException e) {
						return new MyResult <String>(errno.ENOENT * -1,
							e.getMessage(), null);
					}
				}

				return new MyResult <String>(0, null, toPath);
			} else {
				File from = new File(fromPath);
				File to = new File(toPath);
				if (to.exists() && !overwrite) {
					return new MyResult <String>(errno.EPERM * -1,
						"Operation not permitted: overwrite", null);
				}

				String childsFrom[] = from.list();

				/* empty folder */
				if (childsFrom == null || childsFrom.length <= 0) {
					if (to.mkdirs()) {
						return new MyResult <String>(0, null, toPath);
					} else {
						return new MyResult <String>(errno.EPERM * -1,
							"mkdirs " + toPath, null);
					}
				}

				/*
				 * not empty
				 * no matter what parent DIR first, not check permission here
				 * may TODO
				 */
				if (from.isDirectory() && !to.mkdirs()) {
					return new MyResult <String>(errno.EPERM * -1, "mkdirs "
						+ toPath, null);
				}

				int i = 0;
				while (i++ < childsFrom.length) {
					String child_name = childsFrom[i - 1];

					/* sub files or directories */
					String child_path_s = from.getPath() + File.separator
						+ child_name;
					String child_path_d = to.getPath() + File.separator
						+ child_name;

					File path_s = new File(child_path_s);
					/* File path_d = new File(child_path_d); */

					if (!path_s.exists()) { /* extra check */
						return new MyResult <String>(errno.ENOENT * -1,
							"No such file or directory " + child_path_d, null);
					}

					/* current child is folder, just copy it */
					if (path_s.isDirectory()) {
						MyResult <String> ret = NiceFileUtils.__cp(
							child_path_s, true, child_path_d, overwrite);

						if (0 != ret.code) {
							return ret;
						} else {
							continue;
						}
					}

					/*
					 * current child is file, just copy it to child's parent,
					 * just as
					 * path_d
					 */
					if (path_s.isFile()) {
						MyResult <String> ret = NiceFileUtils.__cp(
							child_path_s, false, child_path_d, overwrite);

						if (0 != ret.code) {
							return ret;
						} else {
							continue;
						}
					}
				}

				return new MyResult <String>(0, null, toPath);
			}
		} catch (Exception e) {
			return new MyResult <String>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static MyResult <FILE> isWhat (String w) {
		try {
			if (null == w || w.length() <= 0) {
				return new MyResult <FILE>(errno.EINVAL * -1,
					"Invalid argument", null);
			}

			File f = new File(w);
			/*
			 * if a regular exists but provided as .../
			 * => also a regular: TODO: test
			 */
			if (f.isDirectory()) {
				return new MyResult <FILE>(0, null, new FILE(FILE.directory));
			} else if (f.exists()) {
				return new MyResult <FILE>(0, null, new FILE(FILE.regular));
			} else {
				return new MyResult <FILE>(0, null, new FILE(FILE.dirOrReg));
			}
		} catch (Exception e) {
			Log.w(TAG + ":mv", "ERROR: " + e.getMessage());
			return new MyResult <FILE>(errno.EXTRA_EEUNRESOLVED * -1,
				e.getMessage(), null);
		}
	}


	public static class FILE {
		public FILE (int w) {
			this.w = w;
		}


		public int file () {
			return this.w;
		}


		public static int directory = 0x1;

		public static int regular = 0x1 << 1;

		public static int dirOrReg = FILE.directory | FILE.regular;

		private int w;
	};


	private static final String TAG = NiceFileUtils.class.getSimpleName();

}
