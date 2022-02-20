package org.gusdb.wdk.model.columntool;

public class ColumnToolElementPair {

  private ImplementationRef _reporterRef;
  private ImplementationRef _filterRef;

  public void setReporter(ImplementationRef impl) {
    _reporterRef = impl;
  }

  public ImplementationRef getReporter() {
    return _reporterRef;
  }

  public void setFilter(ImplementationRef impl) {
    _filterRef = impl;
  }

  public ImplementationRef getFilter() {
    return _filterRef;
  }
}
