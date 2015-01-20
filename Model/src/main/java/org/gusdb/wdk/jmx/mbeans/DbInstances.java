package org.gusdb.wdk.jmx.mbeans;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.db.pool.DatabaseInstance;

public class DbInstances implements DbInstancesMBean {

  @Override
  public String getDbInstanceNames() {
    return FormatUtil.join(DatabaseInstance.getAllInstances().keySet().toArray(), ",");
  }

}
