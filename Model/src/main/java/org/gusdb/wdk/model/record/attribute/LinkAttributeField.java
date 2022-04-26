package org.gusdb.wdk.model.record.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.record.RecordClass;

/**
 * <p>
 * This is an {@link AttributeField} that will be rendered as hyper links on the
 * website. You can define your url in the {@code &lt;url>} tag of this field, and
 * the the content in @{code &lt;displayText>} will be rendered as display inside
 * an anchor link.
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
public class LinkAttributeField extends DerivedAttributeField {

  private static final String DEFAULT_TYPE = "link";

  // fields set by XML parsing
  private List<WdkModelText> _urls = new ArrayList<WdkModelText>();
  private List<WdkModelText> _displayTexts = new ArrayList<WdkModelText>();

  // resolved fields
  private String _url;
  private String _displayText;

  private boolean _newWindow = false;

  public LinkAttributeField() {
    // by default, don't show linked attributes in the download
    _inReportMaker = false;
    // set type to default for this subclass
    _type = DEFAULT_TYPE;
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

  public void addDisplayText(WdkModelText displayText) {
    _displayTexts.add(displayText);
  }

  public String getDisplayText() {
    return _displayText;
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);
    _url = excludeModelText(_urls, projectId, "url", true);
    _displayText = excludeModelText(_displayTexts, projectId, "displayText", true);
  }

  @Override
  protected Collection<AttributeField> getDependencies() throws WdkModelException {
    Map<String, AttributeField> dependents = new LinkedHashMap<>();
    if (_displayText != null) dependents.putAll(parseFields(_displayText));
    if (_url != null) dependents.putAll(parseFields(_url));
    return dependents.values();
  }
}
