package org.gusdb.wdk.jmx.mbeans.dbms;

//import java.util.HashMap;
import org.apache.log4j.Logger;

public class PostgreSQLDBInfo extends AbstractDBInfo {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(PostgreSQLDBInfo.class);

  public PostgreSQLDBInfo() {
    super();
    databaseAttributes.put("servicename", "booya");
  }

  @Override
  protected String getMetaDataSql() {
    StringBuffer sql = new StringBuffer(); 
    // column names will be lower-cased keys in metaDataMap
    sql.append(" select                                            ");
    sql.append(" current_database() as db_name,                    ");
    sql.append(" version() as version,                             ");
    sql.append(" to_char(current_timestamp, 'Dy DD-Mon-YYYY HH24:MI:SS') as system_date, ");
    sql.append(" session_user as login,                            ");
    sql.append(" inet_client_addr() as client_host                 ");
    return sql.toString();
  }

  @Override
  protected String getServerNameSql() {
    StringBuffer sql = new StringBuffer();
    sql.append(" select                         ");
    sql.append(" inet_server_addr() as server_ip   ");
    return sql.toString();
  }

  @Override
  protected String getDblinkSql() {
    // not implemented
    return null;
  }



}