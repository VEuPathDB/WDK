package org.gusdb.wdk.model.query.param.values;

import java.util.Map;

import org.gusdb.wdk.model.query.Query;

/**
 * Writeable extension of BasicStableValues
 * 
 * @author rdoherty
 */
public class WriteableStableValues extends AbstractStableValues implements Map<String,String> {

  public WriteableStableValues(Query query) {
    super(query);
  }

  public WriteableStableValues(StableValues stableValues) {
    super(stableValues);
  }

  public WriteableStableValues(Query query, Map<String, String> stableValues) {
    super(query, stableValues);
  }

  public WriteableStableValues(Query query, StableValues stableValues) {
    super(query, stableValues);
  }

  @Override
  public boolean containsKey(Object key) {
    return _values.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return _values.containsValue(value);
  }

  @Override
  public String get(Object key) {
    return _values.get(key);
  }

  @Override
  public String put(String key, String value) {
    return _values.put(key, value);
  }

  @Override
  public String remove(Object key) {
    return _values.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends String> map) {
    _values.putAll(map);
  }

  @Override
  public void clear() {
    _values.clear();
  }

}
