package org.gusdb.wdk.model.record;

import java.io.PrintWriter;

import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.wdk.model.RngAnnotations.RngOptional;
import org.gusdb.wdk.model.RngAnnotations.RngRequired;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

/**
 * A field defines a property of a {@link RecordClass}.
 *
 * @author Jerric
 * @since Jan 17, 2006
 */
public abstract class Field extends WdkModelBase implements ScopedField, NamedObject {

  protected String _name;
  protected String _displayName;
  protected String _longDisplayName; // a longer version, for UIs that have the room
  protected String _help;
  protected String _type;
  protected int _truncateTo;
  protected boolean _internal;
  protected boolean _inReportMaker;

  public Field() {
    // initialize the optional properties
    _internal = false;
    _inReportMaker = true;
    _truncateTo = Utilities.TRUNCATE_DEFAULT;
  }

  @Override
  public Field clone() {
    return (Field) super.clone();
  }

  /**
   * @return Returns the displayName.
   */
  public String getDisplayName() {
    return (_displayName == null) ? _name : _displayName;
  }

  /**
   * @param displayName
   *          The displayName to set.
   */
  @RngOptional
  public void setDisplayName(String displayName) {
    _displayName = displayName;
  }

  /**
   * For use in UIs that have space for a long display name.  Defaults to display name.
   * @return Returns the longDisplayName.
   */
  public String getLongDisplayName() {
    return (_longDisplayName == null) ? getDisplayName() : _longDisplayName;
  }

  /**
   * @param longDisplayName
   *          The displayName to set.
   */
  @RngOptional
  public void setLongDisplayName(String longDisplayName) {
    _longDisplayName = longDisplayName;
  }

  /**
   * @return Returns the help.
   */
  public String getHelp() {
    return _help;
  }

  /**
   * @param help
   *          The help to set.
   */
  @RngOptional
  public void setHelp(String help) {
    _help = help;
  }

  /**
   * if true, a field is available on download; default true.
   *
   * @return Returns the inReportMaker.
   */
  @Override
  public boolean isInReportMaker() {
    return _inReportMaker;
  }

  /**
   * @param inReportMaker
   *          The inReportMaker to set.
   */
  @RngOptional
  public void setInReportMaker(boolean inReportMaker) {
    _inReportMaker = inReportMaker;
  }

  /**
   * if true, a field is unavailable in the summary configuration. default false.
   *
   * @return Returns the internal.
   */
  @Override
  public boolean isInternal() {
    return _internal;
  }

  /**
   * @param internal
   *          The internal to set.
   */
  @RngOptional
  public void setInternal(boolean internal) {
    _internal = internal;
  }

  /**
   * @return Returns the name.
   */
  @Override
  public String getName() {
    return _name;
  }

  /**
   * @param name
   *   The name to set.
   */
  @RngRequired
  public void setName(String name) {
    _name = name;
  }

  /**
   * @return Returns the truncateTo.
   */
  public int getTruncateTo() {
    return _truncateTo;
  }

  /**
   * @param truncateTo
   *   The truncateTo to set.
   */
  @RngOptional
  public void setTruncateTo(int truncateTo) {
    _truncateTo = truncateTo;
  }

  /**
   * @return Returns the type.
   */
  public String getType() {
    return _type;
  }

  /**
   * @param type
   *          The type to set.
   */
  @RngOptional
  public void setType(String type) {
    _type = type;
  }

  @Override
  public String toString() {
    return getDisplayName();
  }

  /**
   * Poorly named method that prints "dependency information" of fields.  Really seems to just
   * print general information about the field that is different than the string representation.
   * See ModelXmlParser's main method for the top-level call that trickles down to this method.
   *
   * @param writer where to write data
   * @param indent how much to indent
   * @throws WdkModelException if unable to get nested dependency information
   */
  public void printDependency(PrintWriter writer, String indent) throws WdkModelException {
    writer.println(indent + "<" + getClass().getSimpleName() + " name=\"" + getName() + "\">");
    printDependencyContent(writer, indent + WdkModel.INDENT);
    writer.println(indent + "</" + getClass().getSimpleName() + ">");
  }

  /**
   * Prints the dependency tree of this attribute field.
   *
   * @param writer where to write
   * @param indent current indentation
   * @throws WdkModelException if unable to resolve dependencies
   */
  protected void printDependencyContent(PrintWriter writer, String indent) throws WdkModelException {
    // only derived attribute fields have dependencies
    writer.write(indent + "<dependOn count=\"0\"/>");
  }
}
