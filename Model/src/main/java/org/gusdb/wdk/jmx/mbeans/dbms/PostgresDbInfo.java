package org.gusdb.wdk.jmx.mbeans.dbms;

//import java.util.HashMap;
import org.gusdb.fgputil.db.pool.DatabaseInstance;

public class PostgresDbInfo extends AbstractDbInfo {

  // NOTE: column names will be lower-cased keys in metaDataMap
  private static final String METADATA_SQL = new StringBuilder()
    .append(" select                                         ")
    .append(" current_database() as db_name,                 ")
    .append(" version() as version,                          ")
    .append(" to_char(current_timestamp,                     ")
    .append("   'Dy DD-Mon-YYYY HH24:MI:SS') as system_date, ")
    .append(" session_user as login,                         ")
    .append(" (select pg_encoding_to_char(encoding)          ")
    .append("  from pg_database                              ")
    .append("  where datname = current_database())           ")
    .append("  as character_encoding,                        ")
    .append(" inet_client_addr() as client_host              ")
    .toString();

  private static final String SERVERNAME_SQL =
    "select inet_server_addr() as server_ip";

  private static final String DBF_SIZE_ON_DISK = "SELECT pg_size_pretty(pg_database_size(current_database())) as dbf_gb_on_disk";

  private static final String DBLINK_SQL =
    " SELECT " +
    "   s.srvname   AS server_name, " +
    "   w.fdwname   AS foreign_data_wrapper, " +
    "   s.srvoptions AS options, " +
    "   COALESCE(( " +
    "       SELECT string_agg(t.nspname || '(' || t.cnt || ')', ', ' ORDER BY t.nspname) " +
    "       FROM ( " +
    "           SELECT ns.nspname, COUNT(*) AS cnt " +
    "           FROM pg_catalog.pg_foreign_table ft " +
    "           JOIN pg_catalog.pg_class     c  ON c.oid  = ft.ftrelid " +
    "           JOIN pg_catalog.pg_namespace ns ON ns.oid = c.relnamespace " +
    "           WHERE ft.ftserver = s.oid " +
    "           GROUP BY ns.nspname " +
    "       ) t " +
    "   ), '') AS schemas " +
    " FROM pg_foreign_server s " +
    "   JOIN pg_foreign_data_wrapper w ON w.oid = s.srvfdw " +
    "   JOIN pg_user_mappings um ON um.srvid = s.oid AND um.usename = CURRENT_USER " +
    " ORDER BY w.fdwname, s.srvname ";


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
    return DBLINK_SQL;
  }

  @Override
  protected String getDbLinkValidationSql(String dblink) {
    return "SELECT 1";
  }

  @Override
  protected String getDbfSizeOnDisk() {
    return DBF_SIZE_ON_DISK;
  }

}