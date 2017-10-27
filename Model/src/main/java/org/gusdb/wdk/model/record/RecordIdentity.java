package org.gusdb.wdk.model.record;

public class RecordIdentity {

  private final RecordClass _recordClass;
  private final PrimaryKeyValue _pkValue;

  public RecordIdentity(RecordClass recordClass, PrimaryKeyValue pkValue) {
    _recordClass = recordClass;
    _pkValue = pkValue;
  }

  public RecordClass getRecordClass() {
    return _recordClass;
  }

  public PrimaryKeyValue getPrimaryKey() {
    return _pkValue;
  }
}
