package org.gusdb.wdk.model.record.attribute;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

public abstract class DerivedAttributeValue extends AttributeValue {

  private AttributeValueContainer _container;

  public DerivedAttributeValue(DerivedAttributeField field, AttributeValueContainer container) {
    super(field);
    _container = container;
  }

  protected String populateMacros(String fieldText) throws WdkModelException, WdkUserException {
    String label = "attribute [" + _field.getName() + "] of [" + _field.getContainer().getNameForLogging() + "]";
    return DerivedAttributeField.replaceMacrosWithAttributeValues(fieldText, _container, label);
  }

}
