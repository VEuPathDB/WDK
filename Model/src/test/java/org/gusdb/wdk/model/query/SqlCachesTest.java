package org.gusdb.wdk.model.query;

import static org.gusdb.fgputil.functional.Functions.fSwallow;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.fgputil.db.cache.SqlCountCache;
import org.gusdb.fgputil.db.cache.SqlResultCache;
import org.gusdb.fgputil.db.runner.BasicResultSetHandler;
import org.gusdb.wdk.model.WdkModelException;

public class SqlCachesTest {

  private final SqlCountCache _countCache;
  private final SqlResultCache<List<Map<String,Object>>> _resultCache;

  public SqlCachesTest(DataSource ds) {

    // instantiate a count cache
    _countCache = new SqlCountCache(ds);

    // instantiate a result cache that simply saves the result
    _resultCache = new SqlResultCache<>(ds, fSwallow(rs -> {
      BasicResultSetHandler handler = new BasicResultSetHandler();
      handler.handleResult(rs);
      return handler.getResults();
    }));
  }

  public Long getCachedCountResult(String countSql) throws WdkModelException {
    try {
      return _countCache.getItem(countSql, "cached-count-sql");
    }
    catch (UnfetchableItemException e) {
      return WdkModelException.unwrap(e, Long.class);
    }
  }

  public List<Map<String,Object>> getCachedQueryResult(String sql) throws WdkModelException {
    try {
      return _resultCache.getItem(sql, "cached-query-sql");
    }
    catch (UnfetchableItemException e) {
      return WdkModelException.unwrap(e, List.class);
    }
  }
}
