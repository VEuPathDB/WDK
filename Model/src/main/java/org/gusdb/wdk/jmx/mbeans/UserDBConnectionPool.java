package org.gusdb.wdk.jmx.mbeans;

import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.WdkModel;

public class UserDBConnectionPool extends AbstractConnectionPool implements UserDBConnectionPoolMBean {

  @Override
  protected DatabaseInstance getDb(WdkModel model) {
    return model.getUserDb();
  }
}
