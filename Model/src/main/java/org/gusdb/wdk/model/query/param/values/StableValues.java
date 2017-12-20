package org.gusdb.wdk.model.query.param.values;

import java.util.Set;
import java.util.stream.Collectors;

import org.gusdb.fgputil.collection.ReadOnlyMap;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.Param;

public interface StableValues extends ReadOnlyMap<String,String> {

  public Query getQuery();

  public default Set<Param> getParams() {
    return getQuery().getParamMap().entrySet().stream()
        .filter(entry -> containsKey(entry.getKey()))
        .map(entry -> entry.getValue())
        .collect(Collectors.toSet());
  }

  public String prettyPrint();

}
