package org.gusdb.wdk.beans;

import org.gusdb.wdk.model.filter.Filter;

public class FilterValue {

  private final Filter _filter;
  private final Object _value;

  public FilterValue(Filter filter, Object value) {
    _filter = filter;
    _value = value;
  }

  public String getKey() {
    return _filter.getKey();
  }

  public Object getValue() {
    return _value;
  }

}
