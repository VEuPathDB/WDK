package org.gusdb.wdk.jmx.mbeans.factory;

import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.jmx.mbeans.DbEnvironment;

public class DbEnvironmentFactory extends AbstractDbMbeanFactory {

  @Override
  protected Object getMbeanObject(DatabaseInstance db) {
    return new DbEnvironment(db);
  }

}
