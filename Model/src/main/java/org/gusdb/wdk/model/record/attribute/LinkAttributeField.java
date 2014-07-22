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
 * provide record specific links, and the {@link LinkAttributeField#displayText}
 * and {@link LinkAttributeField#url} can embed {@link ColumnAttributeField}s
 * from the same {@link RecordClass}.
 * </p>
 * 
 * @author jerric
 * 
 */
public class LinkAttributeField extends AttributeField {

  private List<WdkModelText> urls;
  private String url;

  private List<WdkModelText> displayTexts;
  private String displayText;

  private boolean newWindow = false;

  public LinkAttributeField() {
    displayTexts = new ArrayList<WdkModelText>();
    urls = new ArrayList<WdkModelText>();
    // by default, don't show linked attributes in the download
    this.inReportMaker = false;
  }

  public void setNewWindow(boolean newWindow) {
    this.newWindow = newWindow;
  }

  /**
   * if true, clicking on the link will open a new window; default false.
   * 
   * @return
   */
  public boolean isNewWindow() {
    return newWindow;
  }

  public void addUrl(WdkModelText url) {
    this.urls.add(url);
  }

  String getUrl() {
    return url;
  }

  /**
   * @return the displayText
   */
  public String getDisplayText() {
    return displayText;
  }

  /**
   * @param displayText
   *          the displayText to set
   */
  public void addDisplayText(WdkModelText displayText) {
    this.displayTexts.add(displayText);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
   */
  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    String rcName = (recordClass == null) ? ""
        : (recordClass.getFullName() + ".");

    // exclude urls
    boolean hasUrl = false;
    for (WdkModelText url : urls) {
      if (url.include(projectId)) {
        if (hasUrl) {
          throw new WdkModelException("The linkAttribute " + rcName + getName()
              + " has more than one <url> for " + "project " + projectId);
        } else {
          this.url = url.getText();
          hasUrl = true;
        }
      }
    }
    // check if all urls are excluded
    if (!hasUrl)
      throw new WdkModelException("The linkAttribute " + rcName + name
          + " does not have a <url> tag for project " + projectId);
    urls = null;

    // exclude display texts
    boolean hasText = false;
    for (WdkModelText text : displayTexts) {
      if (text.include(projectId)) {
        if (hasText) {
          throw new WdkModelException("The linkAttribute " + rcName + getName()
              + " has more than one <displayText> " + "for project "
              + projectId);
        } else {
          this.displayText = text.getText();
          hasText = true;
        }
      }
    }
    // check if all texts are excluded
    if (!hasText)
      throw new WdkModelException("The linkAttribute " + rcName + name
          + " does not have a <displayText> tag for project " + projectId);
    displayTexts = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.AttributeField#getDependents()
   */
  @Override
  protected Collection<AttributeField> getDependents() throws WdkModelException {
    Map<String, AttributeField> dependents = parseFields(displayText);
    dependents.putAll(parseFields(url));
    return dependents.values();
  }
}
