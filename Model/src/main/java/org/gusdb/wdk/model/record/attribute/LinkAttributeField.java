package org.gusdb.wdk.model.record.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.record.RecordClass;

/**
 * <p>
 * This is an {@link AttributeField} that will be rendered as hyper links on the
 * website. You can define your url in the {@code <url>} tag of this field, and
 * the the content in @{code <displayText>} will be rendered as display inside
 * of {@code <a></a>}.
 * </p>
 * 
 * <p>
 * The link attribute is usually embedded with {@link ColumnAttributeField}s to
 * provide record specific links, and the {@link LinkAttributeField#_displayText}
 * and {@link LinkAttributeField#_url} can embed {@link ColumnAttributeField}s
 * from the same {@link RecordClass}.
 * </p>
 * 
 * @author jerric
 * 
 */
public class LinkAttributeField extends AttributeField {

  private static final String DEFAULT_TYPE = "link";
  
  private List<WdkModelText> _urls;
  private String _url;

  private List<WdkModelText> _displayTexts;
  private String _displayText;

  private boolean _newWindow = false;

  public LinkAttributeField() {
    _displayTexts = new ArrayList<WdkModelText>();
    _urls = new ArrayList<WdkModelText>();
    // by default, don't show linked attributes in the download
    _inReportMaker = false;
    
    if (_type == null) {
      _type = DEFAULT_TYPE;
    }
  }

  public void setNewWindow(boolean newWindow) {
    _newWindow = newWindow;
  }

  /**
   * if true, clicking on the link will open a new window; default false.
   * 
   * @return
   */
  public boolean isNewWindow() {
    return _newWindow;
  }

  public void addUrl(WdkModelText url) {
    _urls.add(url);
  }

  String getUrl() {
    return _url;
  }

  /**
   * @return the displayText
   */
  public String getDisplayText() {
    return _displayText;
  }
  
  /**
   * @param displayText
   *          the displayText to set
   */
  public void addDisplayText(WdkModelText displayText) {
    _displayTexts.add(displayText);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
   */
  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    String rcName = (_recordClass == null) ? ""
        : (_recordClass.getFullName() + ".");

    // exclude urls
    boolean hasUrl = false;
    for (WdkModelText url : _urls) {
      if (url.include(projectId)) {
        if (hasUrl) {
          throw new WdkModelException("The linkAttribute " + rcName + getName()
              + " has more than one <url> for " + "project " + projectId);
        } else {
          _url = url.getText();
          hasUrl = true;
        }
      }
    }
    // check if all urls are excluded
    if (!hasUrl)
      throw new WdkModelException("The linkAttribute " + rcName + _name
          + " does not have a <url> tag for project " + projectId);
    _urls = null;

    // exclude display texts
    boolean hasText = false;
    for (WdkModelText text : _displayTexts) {
      if (text.include(projectId)) {
        if (hasText) {
          throw new WdkModelException("The linkAttribute " + rcName + getName()
              + " has more than one <displayText> " + "for project "
              + projectId);
        } else {
          _displayText = text.getText();
          hasText = true;
        }
      }
    }
    // check if all texts are excluded
    if (!hasText)
      throw new WdkModelException("The linkAttribute " + rcName + _name
          + " does not have a <displayText> tag for project " + projectId);
    _displayTexts = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.AttributeField#getDependents()
   */
  @Override
  protected Collection<AttributeField> getDependents() throws WdkModelException {
    Map<String, AttributeField> dependents = parseFields(_displayText);
    dependents.putAll(parseFields(_url));
    return dependents.values();
  }
}
