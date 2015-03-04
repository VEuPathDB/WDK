package org.gusdb.wdk.jmx.mbeans.dbms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

  /**
   * Abstract class for collecting information about databases used by
   * the WDK. Subclasses define the database-specific SQL for querying
   * metadata. The DataSource for querying is provided by the instantiated
   * WDK model.
   */
 public interface DbInfo {

    public void populateDatabaseMetaDataMap(HashMap<String, String> dbAttrs);

    public void populateServernameDataMap(HashMap<String, String> dbAttrs);

    public void populateConnectionPoolDataMap(HashMap<String, String> dbAttrs);

    public void populateDblinkList(ArrayList<Map<String, String>> dblinkList);

}
