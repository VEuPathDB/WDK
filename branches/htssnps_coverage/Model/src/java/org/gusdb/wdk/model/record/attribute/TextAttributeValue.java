/**
 * 
 */
package org.gusdb.wdk.model.record.attribute;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

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
  private TextAttributeField field;
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
    this.field = field;
    this.container = container;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.AttributeValue#getValue()
   */
  @Override
  public Object getValue() throws WdkModelException {
    if (this.text == null) {
      String text = field.getText();
      Map<String, AttributeField> subFields = field.parseFields(text);
      Map<String, Object> values = new LinkedHashMap<String, Object>();
      for (String subField : subFields.keySet()) {
        AttributeValue value = container.getAttributeValue(subField);
        Object object = value.getValue();
        values.put(subField, (object == null) ? "" : object.toString());
      }
      this.text = Utilities.replaceMacros(text, values);
    }
    return this.text;
  }

  @Override
  public String getDisplay() throws WdkModelException,
      NoSuchAlgorithmException, SQLException, JSONException, WdkUserException {
    if (this.display == null) {
      String content = field.getDisplay();
      Map<String, AttributeField> subFields = field.parseFields(content);
      Map<String, Object> values = new LinkedHashMap<String, Object>();
      for (String subField : subFields.keySet()) {
        AttributeValue value = container.getAttributeValue(subField);
        Object object = value.getValue();
        values.put(subField, (object == null) ? "" : object.toString());
      }
      this.display = Utilities.replaceMacros(content, values);
    }
    return this.display;
  }

}
