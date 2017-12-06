package org.gusdb.wdk.model.query.param;


import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.query.Query;


public class ParamStableValues extends HashMap<String,String> {
  private static final long serialVersionUID = 1L;
  private final Query _query;

  public ParamStableValues(Query query, Map<String,String> values) {
    _query = query;
    putAll(values);
  }
  
  public ParamStableValues(ParamStableValues paramStableValues) {
    _query = paramStableValues._query;
    putAll(paramStableValues);
  }
  
  public Query getQuery() {
    return _query;
  }

}
