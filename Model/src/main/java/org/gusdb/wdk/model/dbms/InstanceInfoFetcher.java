package org.gusdb.wdk.model.dbms;

import java.util.Date;

import org.gusdb.fgputil.cache.ValueFactory;
import org.gusdb.fgputil.cache.ValueProductionException;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.ResultFactory.InstanceInfo;
import org.jfree.util.Log;

public class InstanceInfoFetcher implements ValueFactory<String, InstanceInfo> {

  private static final long EXPIRATION_SECS = 8;

  public static String getKey(String checksum) {
    return checksum;
  }

  private final ResultFactory _resultFactory;

  public InstanceInfoFetcher(ResultFactory cacheFactory) {
    _resultFactory = cacheFactory;
  }

  @Override
  public InstanceInfo getNewValue(String id) throws ValueProductionException {
    try {
      Log.info("Fetching instance info item with ID: " + id);
      return _resultFactory.getInstanceInfo(id);
    }
    catch(WdkModelException e) {
      throw new ValueProductionException(e);
    }
  }

  @Override
  public InstanceInfo getUpdatedValue(String id, InstanceInfo previousVersion) throws ValueProductionException {
    return getNewValue(id);
  }

  @Override
  public boolean valueNeedsUpdating(InstanceInfo item) {
    return item.instanceId == ResultFactory.UNKNOWN_INSTANCE_ID || (new Date().getTime() - item.creationDate) >= (EXPIRATION_SECS * 1000);
  }

}
