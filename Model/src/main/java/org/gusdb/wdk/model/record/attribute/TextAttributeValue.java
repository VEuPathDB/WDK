/**
 * 
 */
package org.gusdb.wdk.model.record.attribute;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * The text attribute value represents the actual text of the text attribute
 * field for a given record instance. It will discover the values of the column
 * attribute that are embedded in the text attribute field, and substitute them
 * into the text and display.
 * 
 * @author Jerric Gao
 * 
 */
public class TextAttributeValue extends AttributeValue {

  private AttributeValueContainer container;

  /**
   * The text will be used in the download report.
   */
  private String text;

  /**
   * The display will be used in the summary and record page display. if a
   * display is not specified in the model, the text will be used as display.
   */
  private String display;

  /**
   * @param attributeValueContainer
   * @param field
   */
  public TextAttributeValue(TextAttributeField field,
      AttributeValueContainer container) {
    super(field);
    this.container = container;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.AttributeValue#getValue()
   */
  @Override
  public String getValue() throws WdkModelException, WdkUserException {
    if (this.text == null) {
      String fieldText = ((TextAttributeField)field).getText();
      String label = "attribute [" + field.getName() + "] of ["
          + field.getRecordClass().getFullName() + "]";
      this.text = replaceMacrosWithAttributeValues(fieldText, container, label);
    }
    return this.text;
  }

  @Override
  public String getDisplay() throws WdkModelException, WdkUserException {
    if (this.display == null) {
      String fieldDisplay = ((TextAttributeField)field).getDisplay();
      String label = "attribute [" + field.getName() + "] of ["
            + field.getRecordClass().getFullName() + "]";
      this.display = replaceMacrosWithAttributeValues(fieldDisplay, container, label);
    }
    return this.display;
  }

}
