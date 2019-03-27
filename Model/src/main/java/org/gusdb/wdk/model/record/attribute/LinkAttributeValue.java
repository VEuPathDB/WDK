package org.gusdb.wdk.model.record.attribute;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * A value of a {@link LinkAttributeField} that represents a hyperlink in the
 * web page. The values of the embedded {@link AttributeField}s will be
 * substituted into the {@link LinkAttributeField#_displayText} and {@link
 * LinkAttributeField#_url}.
 * <p>
 * The front end is responsible for rendering the url correctly using the values
 * from {@link LinkAttributeField#getDisplayText} and {@link
 * LinkAttributeField#getUrl}. the overridden {@link LinkAttributeValue#getValue}
 * will just return a text representation of the url, which is only useful when
 * downloading the url data in download report.
 */
public class LinkAttributeValue extends DerivedAttributeValue {

  private String _displayText;
  private String _url;

  public LinkAttributeValue(LinkAttributeField field, AttributeValueContainer container) {
    super(field, container);
  }

  public String getDisplayText() throws WdkModelException, WdkUserException {
    if (_displayText == null) {
      _displayText = populateMacros(((LinkAttributeField)_field).getDisplayText());
    }
    return _displayText;
  }

  public String getUrl() throws WdkModelException, WdkUserException {
    if (_url == null) {
      _url = populateMacros(((LinkAttributeField) _field).getUrl());
    }
    return _url;
  }

  /**
   * Get the text representation of the url (e.g. for reports).
   */
  @Override
  public String getValue() throws WdkModelException, WdkUserException {
    return getDisplayText() + "(" + getUrl() + ")";
  }
}
