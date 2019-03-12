package org.gusdb.wdk.model.record.attribute;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordInstance;

/**
 * The AttributeValue holds the actual value of an {@link AttributeField} in a
 * {@link RecordInstance}. It wraps around the actual value, and provides access
 * to the properties of the referenced {@link AttributeField}.
 * 
 * @author jerric
 */
public abstract class AttributeValue implements NamedObject {

  protected static final Logger logger = Logger.getLogger(AttributeValue.class.getName());

  protected AttributeField _field;

  public abstract String getValue() throws WdkModelException, WdkUserException;

  public AttributeValue(AttributeField field) {
    _field = field;
  }

  public AttributeField getAttributeField() {
    return _field;
  }

  public String getDisplayName() {
    return _field.getDisplayName();
  }

  @Override
  public String getName() {
    return _field.getName();
  }

  public String getBriefDisplay() throws WdkModelException, WdkUserException {
    String display = getDisplay();
    int truncateTo = _field.getTruncateTo();
    switch (truncateTo) {
    case -1:
      return display;
    case 0:
      truncateTo = Utilities.TRUNCATE_DEFAULT;
      // drop through
    default:
      return (display.length() <= truncateTo ? display : display.substring(0,
          truncateTo) + "...");
    }
  }

  public String getDisplay() throws WdkModelException, WdkUserException {
    Object value = getValue();
    return (value != null) ? value.toString() : "";
  }

  @Override
  public String toString() {
    try {
      Object value = getValue();
      return (value == null ? "" : value.toString());
    }
    catch (WdkModelException | WdkUserException ex) {
      throw new RuntimeException(ex);
    }
  }
}
