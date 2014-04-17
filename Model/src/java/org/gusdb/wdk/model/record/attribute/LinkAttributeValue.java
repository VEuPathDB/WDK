package org.gusdb.wdk.model.record.attribute;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * <p>
 * A value of a {@link LinkedAttributeField} that represents a hyperlink in the
 * web page. The values of the embedded {@link AttributeField}s will be
 * substituted into the {@link LinkAttributeField#displayText} and
 * {@link LinkAttributeField#url}.
 * </p>
 * 
 * <p>
 * The front end is responsible for rendering the url correctly using the values
 * from {@link LinkAttributeField#getDisplayText} and
 * {@link LinkAttributeField#getUrl}. the overridden
 * {@link LinkAttribute#getValue} will just return a text representation of the
 * url, which is only useful when downloading the url data in download report.
 * </p>
 */
public class LinkAttributeValue extends AttributeValue {

  private AttributeValueContainer container;
  private LinkAttributeField field;
  private String displayText;
  private String url;

  public LinkAttributeValue(LinkAttributeField field,
      AttributeValueContainer container) {
    super(field);
    this.field = field;
    this.container = container;
  }

  public String getDisplayText() throws WdkModelException {
    if (displayText == null) {
      String text = field.getDisplayText();
      Map<String, AttributeField> subFields = field.parseFields(text);
      Map<String, Object> values = new LinkedHashMap<String, Object>();
      for (String subField : subFields.keySet()) {
        AttributeValue value = container.getAttributeValue(subField);
        values.put(subField, value.getValue());
      }
      this.displayText = Utilities.replaceMacros(text, values);
    }
    return this.displayText;
  }

  public String getUrl() throws WdkModelException {
    if (this.url == null) {
      String url = field.getUrl();
      Map<String, AttributeField> subFields = field.parseFields(url);
      Map<String, Object> values = new LinkedHashMap<String, Object>();
      for (String subField : subFields.keySet()) {
        AttributeValue value = container.getAttributeValue(subField);
        values.put(subField, value.getValue());
      }
      this.url = Utilities.replaceMacros(url, values);
    }
    return this.url;
  }

  /**
   * Get the text representation of the url.
   * 
   * @throws WdkUserException
   * 
   * @see org.gusdb.wdk.model.record.attribute.AttributeValue#getValue()
   */
  @Override
  public Object getValue() throws WdkModelException {
    return getDisplayText() + "(" + getUrl() + ")";
  }
}
