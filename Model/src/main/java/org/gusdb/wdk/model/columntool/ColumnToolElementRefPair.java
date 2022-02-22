package org.gusdb.wdk.model.columntool;

public class ColumnToolElementRefPair {

  private ColumnToolElementRef _reporterRef;
  private ColumnToolElementRef _filterRef;

  public void setReporter(ColumnToolElementRef impl) {
    _reporterRef = impl;
  }

  public ColumnToolElementRef getReporter() {
    return _reporterRef;
  }

  public void setFilter(ColumnToolElementRef impl) {
    _filterRef = impl;
  }

  public ColumnToolElementRef getFilter() {
    return _filterRef;
  }
}
