package org.gusdb.wdk.model.record.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


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
public class TextAttributeField extends AttributeField {

  private List<WdkModelText> _texts;
  /**
   * The text are used in the download, and should not include any html tags.
   */
  private String _text;

  private List<WdkModelText> _displays;
  /**
   * the display are used on the website, and html tags are allowed. display is
   * optional, and if not set, text will be used instead.
   */
  private String _display;

  public TextAttributeField() {
    _texts = new ArrayList<WdkModelText>();
    _displays = new ArrayList<WdkModelText>();
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



    // exclude texts
    boolean hasText = false;
    for (WdkModelText text : _texts) {
      if (text.include(projectId)) {
        if (hasText) {
          throw new WdkModelException("The textAttribute " + rcName + getName()
              + " has more than one <text> for " + "project " + projectId);
        } else {
          _text = text.getText();
          hasText = true;
        }
      }
    }
    // check if all texts are excluded
    if (_text == null)
      throw new WdkModelException("The text attribute " + rcName + getName()
          + " does not have a <text> tag for project " + projectId);
    _texts = null;

    // exclude display, display is optional
    boolean hasDisplay = false;
    for (WdkModelText display : _displays) {
      if (display.include(projectId)) {
        if (hasDisplay) {
          throw new WdkModelException("The textAttribute " + rcName + getName()
              + " has more than one <display> for " + "project " + projectId);
        } else {
          _display = display.getText();
          hasDisplay = true;
        }
      }
    }
    _displays = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.AttributeField#getDependents()
   */
  @Override
  public Collection<AttributeField> getDependents() throws WdkModelException {
    String content = _text;
    if (_display != null)
      content += "\n" + _display;
    return parseFields(content).values();
  }
}
