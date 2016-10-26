package org.gusdb.wdk.model.record.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;

/**
 * <p>
 * A primaryKeyField defines the {@link Column}s that can be used to uniquely
 * identify a {@link RecordInstance}.
 * </p>
 * 
 * <p>
 * A primary key field is a combination of one or more {@link Column}s; and the
 * corresponding {@link ColumnAttributeField}s need to be defined in any one of
 * the {@code <attributeQuery>} tags.
 * </p>
 * 
 * <p>
 * Due to the limitation of the basket/dataset tables, we can only support a
 * limited number of columns in a primary key. the number is defined
 * {@link Utilities#MAX_PK_COLUMN_COUNT}.
 * </p>
 * 
 * @author jerric
 * 
 */
public class PrimaryKeyAttributeField extends AttributeField {

  private List<WdkModelText> _columnRefList = new ArrayList<WdkModelText>();
  private Set<String> _columnRefSet = new LinkedHashSet<String>();

  private List<WdkModelText> _textList = new ArrayList<WdkModelText>();
  private List<WdkModelText> _displays = new ArrayList<WdkModelText>();
  private String _text;
  private String _display;

  /**
   * if an alias query ref is defined, the ids will be passed though this alias
   * query to get the new ids whenever a recordInstance is created.
   */
  private String _aliasQueryRef = null;
  private String _aliasPluginClassName = null;

  public PrimaryKeyAttributeField() {
    // this step should be deprecated
    // add project id into the column list
    // columnRefSet.add(Utilities.COLUMN_PROJECT_ID);
  }

  public void addColumnRef(WdkModelText columnRef) {
    _columnRefList.add(columnRef);
  }

  public String[] getColumnRefs() {
    String[] array = new String[_columnRefSet.size()];
    _columnRefSet.toArray(array);
    return array;
  }

  public boolean hasColumn(String columnName) {
    return _columnRefSet.contains(columnName);
  }

  public void addText(WdkModelText text) {
    _textList.add(text);
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
   * @see
   * org.gusdb.wdk.model.Field#setRecordClass(org.gusdb.wdk.model.RecordClass)
   */
  @Override
  public void setRecordClass(RecordClass recordClass) {
    super.setRecordClass(recordClass);
    _recordClass = recordClass;
  }

  /**
   * @return the aliasQueryRef
   */
  public String getAliasQueryRef() {
    return _aliasQueryRef;
  }

  /**
   * @param aliasQueryRef
   *          the aliasQueryRef to set
   */
  public void setAliasQueryRef(String aliasQueryRef) {
    _aliasQueryRef = aliasQueryRef;
  }

  /**
   * @return the aliasPluginClassName
   */
  public String getAliasPluginClassName() {
    return _aliasPluginClassName;
  }

  /**
   * @param aliasPluginClassName
   */
  public void setAliasPluginClassName(String aliasPluginClassName) {
    _aliasPluginClassName = aliasPluginClassName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
   */
  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);



    // exclude columnRefs
    for (WdkModelText columnRef : _columnRefList) {
      if (columnRef.include(projectId)) {
        columnRef.excludeResources(projectId);
        String columnName = columnRef.getText();

        if (_columnRefSet.contains(columnName)) {
          throw new WdkModelException("The columnRef " + columnRef
              + " is duplicated in primaryKetAttribute in " + "recordClass "
              + _recordClass.getFullName());
        } else
          _columnRefSet.add(columnName);
      }
    }
    _columnRefList = null;
    if (_columnRefSet.size() == 0)
      throw new WdkModelException("No primary key column defined in "
          + "recordClass " + _recordClass.getFullName());
    if (_columnRefSet.size() > Utilities.MAX_PK_COLUMN_COUNT)
      throw new WdkModelException("You can specify up to "
          + Utilities.MAX_PK_COLUMN_COUNT + " primary key "
          + "columns in recordClass " + _recordClass.getFullName());

    // exclude format
    for (WdkModelText text : _textList) {
      if (text.include(projectId)) {
        text.excludeResources(projectId);
        _text = text.getText();
        break;
      }
    }
    _textList = null;
    if (_text == null)
      throw new WdkModelException("No primary key format string defined"
          + " in recordClass " + _recordClass.getFullName());


 // exclude display, display is optional
    for (WdkModelText display : _displays) {
      if (display.include(projectId)) {
        display.excludeResources(projectId);
        _display = display.getText();
        break;
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
    return parseFields(_text).values();
  }

  /**
   * primary key cannot be removed (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.record.attribute.AttributeField#isRemovable()
   */
  @Override
  public boolean isRemovable() {
    return false;
  }
}
