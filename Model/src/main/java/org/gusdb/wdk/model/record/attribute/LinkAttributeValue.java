package org.gusdb.wdk.model.record.attribute;

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
  private String displayText;
  private String url;

  public LinkAttributeValue(LinkAttributeField field,
      AttributeValueContainer container) {
    super(field);
    this.container = container;
  }

  public String getDisplayText() throws WdkModelException, WdkUserException {
    if (displayText == null) {
      String baseText = ((LinkAttributeField)field).getDisplayText();
      String label = "attribute" + " [" + field.getName() + "] of ["
            + field.getRecordClass().getFullName() + "]";
      displayText = replaceMacrosWithAttributeValues(baseText, container, label);
    }
    return displayText;
  }

  public String getUrl() throws WdkModelException, WdkUserException {
    if (url == null) {
      String baseUrl = ((LinkAttributeField)field).getUrl();
      String label = "attribute" + " [" + field.getName() + "] of ["
          + field.getRecordClass().getFullName() + "]";
      url = replaceMacrosWithAttributeValues(baseUrl, container, label);
    }
    return url;
  }

  /**
   * Get the text representation of the url.
   * 
   * @throws WdkUserException
   * 
   * @see org.gusdb.wdk.model.record.attribute.AttributeValue#getValue()
   */
  @Override
  public String getValue() throws WdkModelException, WdkUserException {
    return getDisplayText() + "(" + getUrl() + ")";
  }
}
