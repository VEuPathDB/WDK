package org.gusdb.wdk.model.record.attribute;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.RecordInstance;

/**
 * The AttributeValue holds the actual value of an {@link AttributeField} in a
 * {@link RecordInstance}. It wraps around the actual value, and provides access
 * to the properties of the referenced {@link AttributeField}.
 * 
 * @author jerric
 * 
 */
public abstract class AttributeValue {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(AttributeValue.class.getName());

  protected AttributeField field;
  protected Object value;

  public abstract Object getValue() throws WdkModelException;

  public AttributeValue(AttributeField field) {
    this.field = field;
  }

  public AttributeField getAttributeField() {
    return this.field;
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.record.Field#getDisplayName()
   */
  public String getDisplayName() {
    return field.getDisplayName();
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.record.Field#getName()
   */
  public String getName() {
    return field.getName();
  }

  public String getBriefDisplay() throws WdkModelException {
    String display = getDisplay();
    int truncateTo = field.getTruncateTo();
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

  public String getDisplay() throws WdkModelException {
    Object value = getValue();
    return (value != null) ? value.toString() : "";
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    try {
      Object value = getValue();
      return (value == null) ? "" : value.toString();
    } catch (WdkModelException ex) {
      throw new RuntimeException(ex);
    }
  }
}
