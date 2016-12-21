package org.gusdb.wdk.model.record;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValueContainer;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.IdAttributeField;
import org.gusdb.wdk.model.record.attribute.IdAttributeValue;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeValue;

public class TableValueRow extends AttributeValueContainer {

  private static final long serialVersionUID = 1L;

  private final TableField _tableField;

  public TableValueRow(TableField tableField) {
    super(tableField.getAttributeFieldMap());
    _tableField = tableField;
  }

  public void initializeFromResultList(ResultList resultList) throws WdkModelException {
    for (AttributeField field : _tableField.getAttributeFields()) {
      if (!(field instanceof ColumnAttributeField)) {
        continue;
      }
      Object value = resultList.get(field.getName());
      QueryColumnAttributeValue attributeValue = new QueryColumnAttributeValue(
          (QueryColumnAttributeField) field, value);
      addAttributeValue(attributeValue);
    }
  }

  @Override
  public QueryColumnAttributeValue getQueryColumnAttributeValue(QueryColumnAttributeField field)
      throws WdkModelException, WdkUserException {
    if (!containsKey(field.getName())) {
      throw new WdkModelException("Requested column attribute [" + field.getName() + " not loaded into container.");
    }
    return (QueryColumnAttributeValue)get(field.getName());
  }

  @Override
  public IdAttributeValue getIdAttributeValue(IdAttributeField field) {
    throw new UnsupportedOperationException("Table rows cannot have ID attributes");
  }
}
