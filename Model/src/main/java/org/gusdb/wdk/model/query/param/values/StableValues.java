package org.gusdb.wdk.model.query.param.values;

import java.util.Map.Entry;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.Param;

public interface StableValues {

  public Query getQuery();

  public default Set<Param> getParams() {
    return getQuery().getParamMap().entrySet().stream()
        .filter(entry -> containsKey(entry.getKey()))
        .map(entry -> entry.getValue())
        .collect(Collectors.toSet());
  }

  public String prettyPrint();

  // read-only map operations
  public int size();
  public boolean isEmpty();
  public boolean containsKey(String key);
  public Set<String> keySet();
  public Collection<String> values();
  public Set<Entry<String,String>> entrySet();
  public String get(String key);

}
