package org.gusdb.wdk.model.report;

import org.gusdb.wdk.model.RngAnnotations.RngUndefined;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.attribute.AttributeField;

public class AttributeReporterRef extends ReporterRef {

  private AttributeField _attributeField;

  public AttributeField getAttributeField() {
    return _attributeField;
  }

  @Override
  public String getName() {
    return _attributeField.getName() + "-" + super.getName();
  }

  @Override
  public String getReferenceName() {
    return super.getName();
  }

  @Override
  public void resolveReferences(WdkModel wodkModel) throws WdkModelException {

    // try to find implementation class
    String msgStart = "Implementation class for reporter '" + getName() + "' [" + getImplementation() + "] ";
    try {
      Class<?> implClass = Class.forName(getImplementation());
      if (!AbstractAttributeReporter.class.isAssignableFrom(implClass)) {
        throw new WdkModelException(msgStart + "must be a subclass of " + AbstractAttributeReporter.class.getName());
      }
    }
    catch (ClassNotFoundException e) {
      throw new WdkModelException(msgStart + "cannot be found.", e);
    }
  }

  @RngUndefined
  public void setAttributeField(AttributeField attributeField) {
    _attributeField = attributeField;
  }
}
