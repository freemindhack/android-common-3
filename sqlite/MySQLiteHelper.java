
package common.sqlite;


import java.util.ArrayList;


import common.datastructure.MyHash;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public abstract class MySQLiteHelper extends SQLiteOpenHelper implements
	MySQLiteSQLs {
	public MySQLiteHelper (Context context, String dbName, int dbVersion) {
		super(context, dbName, null, dbVersion);
	}


	public int execWritableSQL (String sql) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			try {
				db.execSQL(sql);
			} catch (Exception e) {
				db.close();
				return -1;
			}

			db.close();

			return 0;
		} catch (Exception e) {
			return -2;
		}
	}


	public int execReadableSQL (String sql) {
		try {
			SQLiteDatabase db = this.getReadableDatabase();

			try {
				db.execSQL(sql);
			} catch (Exception e) {
				db.close();
				return -1;
			}

			db.close();

			return 0;
		} catch (Exception e) {
			return -2;
		}
	}


	/* Creating Tables */
	@Override
	public void onCreate (SQLiteDatabase db) {
		try {
			String[] ccSqls = this.getCreateTableSQLs();
			if (null == ccSqls) {
				return;
			}

			int n = ccSqls.length;
			for (int i = 0; i < n; ++i) {
				try {
					db.execSQL(ccSqls[i]);
				} catch (Exception e) {
					;
				}
			}
		} catch (Exception e) {
			Log.e(TAG + ":onCreate", "ERROR: " + e.getMessage());
		}
	}


	/* Upgrading database */
	@Override
	public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			String[] ddSqls = this.getDropTableSQLs(oldVersion, newVersion);
			if (null == ddSqls) {
				return;
			}

			int n = ddSqls.length;
			for (int i = 0; i < n; ++i) {
				try {
					db.execSQL(ddSqls[i]);
				} catch (Exception e) {
					;
				}
			}

			this.onCreate(db);
		} catch (Exception e) {
			Log.e(TAG + ":onCreate", "ERROR: " + e.getMessage());
		}
	}


	/* All Create, Read, Update, Delete Operations */
	/* insert one row */
	public int insert (String table, MyHash <String, String> toInsert) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			int n = toInsert.count();

			for (int i = 0; i < n; ++i) {
				values.put(toInsert.key(i), toInsert.value(i));
			}

			/* Inserting row */
			db.insert(table, null, values);

			db.close();

			return 0;/* OK */
		} catch (Exception e) {
			Log.e(TAG + ":insert", "ERROR: " + e.getMessage());

			return -1;
		}
	}


	/* select row(s) */
	public ArrayList <MyHash <String, String>> select (String table,
		String[] columns, String condition, String[] conditionArgs,
		String groupBy, String having, String orderBy) {
		try {
			SQLiteDatabase db = this.getReadableDatabase();

			Cursor cursor = db.query(table, //
				columns, //
				condition, conditionArgs, groupBy, having, orderBy);

			if (null == cursor) {
				db.close();

				Log.e(TAG + ":select", "ERROR: query");
				return null;
			}

			ArrayList <MyHash <String, String>> retval = null;
			boolean ret = cursor.moveToFirst();
			if (ret) {
				int n = columns.length;
				retval = new ArrayList <MyHash <String, String>>();
				retval.clear();

				do {
					MyHash <String, String> row = new MyHash <String, String>();
					row.clear();

					for (int i = 0; i < n; ++i) {
						row.insert(columns[i], cursor.getString(i), false);
					}

					retval.add(row);

				} while (cursor.moveToNext());

				db.close();

				return retval;
			} else {
				Log.e(TAG + ":select", "ERROR: moveToFirst");
			}

			db.close();

			return retval;
		} catch (Exception e) {
			Log.e(TAG + ":select", "ERROR: " + e.getMessage());

			return null;
		}
	}


	/* Updating */
	public int update (String table, MyHash <String, String> toUpdate,
		String whereClause, String[] whereArgs) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			int n = toUpdate.count();

			for (int i = 0; i < n; ++i) {
				values.put(toUpdate.key(i), toUpdate.value(i));
			}

			/* updating row */
			int ret = db.update(table, values, whereClause, whereArgs);

			db.close();

			return ret;
		} catch (Exception e) {
			Log.e(TAG + ":update", "ERROR: " + e.getMessage());

			return -1;
		}
	}


	// // Deleting single contact
	// public void Delete_Contact (int id) {
	// SQLiteDatabase db = this.getWritableDatabase();
	// db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
	// new String[] { String.valueOf(id) });
	// db.close();
	// }
	//
	//
	// // Getting contacts Count
	// public int Get_Total_Contacts () {
	// String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
	// SQLiteDatabase db = this.getReadableDatabase();
	// Cursor cursor = db.rawQuery(countQuery, null);
	// cursor.close();
	//
	// // return count
	// return cursor.getCount();
	// }

	/* private static final */
	private static final String TAG = "MySQLiteHelper";

	/* Database Version */
	/* private static final int DATABASE_VERSION = 1; */
}
