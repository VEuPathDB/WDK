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
    if (this.displayText == null) {
      String displayText = ((LinkAttributeField)field).getDisplayText();
      String label = "attribute" + " [" + field.getName() + "] of ["
            + field.getRecordClass().getFullName() + "]";
      this.displayText = container.replaceMacrosWithAttributeValues(displayText, label);
    }
    return this.displayText;
  }

  public String getUrl() throws WdkModelException, WdkUserException {
    if (this.url == null) {
      String url = ((LinkAttributeField)field).getUrl();
      String label = "attribute" + " [" + field.getName() + "] of ["
          + field.getRecordClass().getFullName() + "]";
      this.url = container.replaceMacrosWithAttributeValues(url, label);
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
  public Object getValue() throws WdkModelException, WdkUserException {
    return getDisplayText() + "(" + getUrl() + ")";
  }
}
