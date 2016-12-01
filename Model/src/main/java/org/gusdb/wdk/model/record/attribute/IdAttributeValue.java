package org.gusdb.wdk.model.record.attribute;

import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.StaticRecordInstance;

public class IdAttributeValue extends TextAttributeValue {

  private final PrimaryKeyValue _primaryKey;

  public IdAttributeValue(IdAttributeField field, StaticRecordInstance recordInstance) {
    super(field, recordInstance);
    _primaryKey = recordInstance.getPrimaryKey();
  }

  public PrimaryKeyValue getPrimaryKey() {
    return _primaryKey;
  }
}
