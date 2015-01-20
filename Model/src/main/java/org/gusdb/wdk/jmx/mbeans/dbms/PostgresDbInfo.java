package org.gusdb.wdk.jmx.mbeans.dbms;

//import java.util.HashMap;
import org.gusdb.fgputil.db.pool.DatabaseInstance;

public class PostgresDbInfo extends AbstractDbInfo {

  // NOTE: column names will be lower-cased keys in metaDataMap
  private static final String METADATA_SQL =
    "select" +
    " current_database() as db_name," +
    " version() as version," +
    " to_char(current_timestamp, 'Dy DD-Mon-YYYY HH24:MI:SS') as system_date," +
    " session_user as login," +
    " inet_client_addr() as client_host";

  private static final String SERVERNAME_SQL =
    "select inet_server_addr() as server_ip";

  public PostgresDbInfo(DatabaseInstance db) {
    super(db);
  }

  @Override
  protected String getMetaDataSql() {
    return METADATA_SQL;
  }

  @Override
  protected String getServerNameSql() {
    return SERVERNAME_SQL;
  }

  @Override
  protected String getDblinkSql() {
    // not implemented
    return null;
  }

  @Override
  protected String getDbLinkValidationSql(String dblink) {
    // not implemented
    return null;
  }

}