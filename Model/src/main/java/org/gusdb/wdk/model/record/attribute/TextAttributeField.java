package org.gusdb.wdk.model.record.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

/**
 * An attribute field that is rendered as html text.
 *
 * The text attribute is usually embedded with column attributes to provide
 * record specific text section.
 *
 * @author jerric
 */
public class TextAttributeField extends DerivedAttributeField {

  // fields set by XML parsing
  private final List<WdkModelText> _texts;
  private final List<WdkModelText> _displays;
  private final List<WdkModelText> _tooltips;

  // resolved fields
  private String _text;
  private String _display;
  private String _tooltip;

  public TextAttributeField() {
    _texts = new ArrayList<>();
    _displays = new ArrayList<>();
    _tooltips = new ArrayList<>();
    _dataType = AttributeFieldDataType.STRING;
  }

  public void addText(WdkModelText text) {
    _texts.add(text);
  }

  public String getText() {
    return _text;
  }

  public void addDisplay(WdkModelText display) {
    _displays.add(display);
  }

  public String getDisplay() {
    return (_display != null) ? _display : _text;
  }

  public void addTooltip(WdkModelText tooltip) {
    _tooltips.add(tooltip);
  }

  public String getTooltip() {
    return _tooltip == null ? "" : _tooltip;
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);
    _text = excludeModelText(_texts, projectId, "text", true);
    _display = excludeModelText(_displays, projectId, "display", false);
    _tooltip = excludeModelText(_tooltips, projectId, "tooltip", false);
  }

  @Override
  public Collection<AttributeField> getDependencies() throws WdkModelException {
    // combine text and display values to look for attribute macros
    Map<String, AttributeField> dependents = new LinkedHashMap<>();
    if (_display!= null) dependents.putAll(parseFields(_display));
    if (_text != null) dependents.putAll(parseFields(_text));
    if (_tooltip != null) dependents.putAll(parseFields(_tooltip));
    return dependents.values();
  }
}
