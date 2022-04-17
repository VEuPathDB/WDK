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
public class TextAttributeValue extends DerivedAttributeValue {

  /**
   * The text will be used in the download report.
   */
  private String _text;

  /**
   * The display will be used in the summary and record page display. if a
   * display is not specified in the model, the text will be used as display.
   */
  private String _display;

  /**
   * @param field
   * @param container
   */
  public TextAttributeValue(TextAttributeField field, AttributeValueContainer container) {
    super(field, container);
  }

  @Override
  public String getValue() throws WdkModelException, WdkUserException {
    if (_text == null) {
      _text = populateMacros(((TextAttributeField)_field).getText());
    }
    return _text;
  }

  @Override
  public String getDisplay() throws WdkModelException, WdkUserException {
    if (_display == null) {
      _display = populateMacros(((TextAttributeField)_field).getDisplay());
    }
    return _display;
  }

}
