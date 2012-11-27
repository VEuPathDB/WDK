package org.gusdb.wdk.jmx.mbeans.dbms;

//import java.util.HashMap;
import org.apache.log4j.Logger;

public class OracleDBInfo extends AbstractDBInfo {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(OracleDBInfo.class);

  public OracleDBInfo() {
    super();
    databaseAttributes.put("servicename", "booya");
  }

  protected String getMetaDataSql() {
    StringBuffer sql = new StringBuffer();
    
    // column names will be lower-cased keys in metaDataMap
    sql.append(" select                                                          ");
    sql.append(" global_name,                                                    ");
    sql.append(" ver.banner version,                                             ");
    sql.append(" to_char(sysdate, 'Dy DD-Mon-YYYY HH24:MI:SS') system_date,      ");
    sql.append(" sys_context('USERENV', 'SESSION_USER'       ) login,            ");
    sql.append(" sys_context('userenv', 'SERVICE_NAME'       ) service_name,     ");
    sql.append(" sys_context('userenv', 'DB_NAME'            ) db_name,          ");
    sql.append(" sys_context('USERENV', 'DB_UNIQUE_NAME'     ) db_unique_name,   ");
    sql.append(" sys_context('USERENV', 'INSTANCE_NAME'      ) instance_name,    ");
    sql.append(" sys_context('USERENV', 'DB_DOMAIN'          ) db_domain,        ");
    sql.append(" sys_context('USERENV', 'HOST'               ) client_host,      ");
    sql.append(" sys_context('USERENV', 'OS_USER'            ) os_user,          ");
    sql.append(" sys_context('USERENV', 'CURRENT_USERID'     ) current_userid,   ");
    sql.append(" sys_context('USERENV', 'SESSION_USER'       ) session_user,     ");
    sql.append(" sys_context('USERENV', 'SESSION_USERID'     ) session_userid    ");
    sql.append(" from global_name, v$version ver                                 ");
    sql.append(" where lower(ver.banner) like '%oracle%'                         ");
    
    return sql.toString();
  }


  protected String getServerNameSql() {
    StringBuffer sql = new StringBuffer();
    sql.append(" select                                     ");
    sql.append(" UTL_INADDR.get_host_name as server_name,   ");
    sql.append(" UTL_INADDR.get_host_address as server_ip   ");
    sql.append(" from dual                                  ");
    return sql.toString();
  }

  protected String getDblinkSql() {
    StringBuffer sql = new StringBuffer();

    sql.append(" select             ");
    sql.append(" owner owner,       ");
    sql.append(" db_link db_link,   ");
    sql.append(" username username, ");
    sql.append(" host host,         ");
    sql.append(" created created    ");
    sql.append(" from all_db_links  ");
    sql.append(" order by db_link   ");
    
    return sql.toString();
  }



}
