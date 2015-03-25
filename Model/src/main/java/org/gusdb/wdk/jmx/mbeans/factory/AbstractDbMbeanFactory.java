package org.gusdb.wdk.jmx.mbeans.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.jmx.NamedMBeanFactory;

public abstract class AbstractDbMbeanFactory implements NamedMBeanFactory {

  private static final Logger LOG = Logger.getLogger(AbstractDbMbeanFactory.class);
  
  private static final String DB_NAME_MACRO = "{dbName}";

  protected abstract Object getMbeanObject(DatabaseInstance db);
  
  @Override
  public Map<String, Object> getNamedMbeans(String parameterizedMbeanName) {
    boolean firstInstanceOnly = false;
    if (!parameterizedMbeanName.contains(DB_NAME_MACRO)) {
      firstInstanceOnly = true;
      LOG.warn("Passed DB MBean name '" + parameterizedMbeanName + "' does not contain '" + DB_NAME_MACRO +
          "'; substitution will not be performed and only the first DatabaseInstance will have an MBean");
    }
    // will query DatabaseInstance to get a list of active connection pools
    Map<String, Object> namedMbeans = new HashMap<>();
    for (Entry<String, DatabaseInstance> dbEntry : DatabaseInstance.getAllInstances().entrySet()) {
      String finalName = parameterizedMbeanName.replace(DB_NAME_MACRO, dbEntry.getKey());
      Object beanObject = getMbeanObject(dbEntry.getValue());
      namedMbeans.put(finalName, beanObject);
      if (firstInstanceOnly) {
        return namedMbeans;
      }
    }
    return namedMbeans;
  }
}
