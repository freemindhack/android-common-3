package nocom.common.utils;


import java.io.File;


import android.content.Context;


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
}
