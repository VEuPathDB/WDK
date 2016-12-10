package org.gusdb.wdk.model.dbms;

import java.util.Date;

import org.gusdb.fgputil.cache.ItemFetcher;
import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.ResultFactory.InstanceInfo;
import org.jfree.util.Log;

public class InstanceInfoFetcher implements ItemFetcher<String, InstanceInfo> {

  private static final long EXPIRATION_SECS = 8;

  private static final String KEY_DELIMITER = ":::";

  public static String getKey(String checksum, int queryId) {
    return checksum + KEY_DELIMITER + queryId;
  }

  private final ResultFactory _resultFactory;

  public InstanceInfoFetcher(ResultFactory cacheFactory) {
    _resultFactory = cacheFactory;
  }

  @Override
  public InstanceInfo fetchItem(String id) throws UnfetchableItemException {
    try {
      Log.info("Fetching instance info item with ID: " + id);
      String[] parts = id. split(KEY_DELIMITER);
      return _resultFactory.getInstanceInfo(parts[0], Integer.parseInt(parts[1]));
    }
    catch(WdkModelException e) {
      throw new UnfetchableItemException(e);
    }
  }

  @Override
  public InstanceInfo updateItem(String id, InstanceInfo previousVersion) throws UnfetchableItemException {
    return fetchItem(id);
  }

  @Override
  public boolean itemNeedsUpdating(InstanceInfo item) {
    return item.instanceId == ResultFactory.UNKNOWN_INSTANCE_ID || (new Date().getTime() - item.creationDate) >= (EXPIRATION_SECS * 1000);
  }
}
