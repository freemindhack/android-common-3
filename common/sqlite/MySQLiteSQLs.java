
package nocom.common.sqlite;


public interface MySQLiteSQLs {
	public String[] getCreateTableSQLs ();


	public String[] getDropTableSQLs (int oldVersion, int newVersion);
}
