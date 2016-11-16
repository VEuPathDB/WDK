package org.gusdb.wdk.model.dbms;

import java.util.Date;

import org.gusdb.fgputil.cache.ItemFetcher;
import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.wdk.model.WdkModelException;

public class QueryInfoFetcher implements ItemFetcher<String, QueryInfo> {

  private static final long EXPIRATION_SECS = 8;

  private static final String KEY_DELIMITER = ":::";

  public static String getKey(String queryName, String checksum) {
    return queryName + KEY_DELIMITER + checksum;
  }

  private final CacheFactory _cacheFactory;

  public QueryInfoFetcher(CacheFactory cacheFactory) {
    _cacheFactory = cacheFactory;
  }

  @Override
  public QueryInfo fetchItem(String id) throws UnfetchableItemException {
    try {
      String[] parts = id.split(KEY_DELIMITER);
      return _cacheFactory.createQueryInfo(parts[0], parts[1]);
    }
    catch(WdkModelException e) {
      throw new UnfetchableItemException(e);
    }
  }

  @Override
  public QueryInfo updateItem(String id, QueryInfo previousVersion) throws UnfetchableItemException {
    return fetchItem(id);
  }

  @Override
  public boolean itemNeedsUpdating(QueryInfo item) {
    return !item.isExist() || (new Date().getTime() - item.creationDate) >= (EXPIRATION_SECS * 1000);
  }
}
