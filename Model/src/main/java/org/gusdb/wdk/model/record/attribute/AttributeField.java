package org.gusdb.wdk.model.record.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.gusdb.wdk.model.RngAnnotations.RngOptional;
import org.gusdb.wdk.model.RngAnnotations.RngUndefined;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.report.AttributeReporterRef;
import org.gusdb.wdk.model.report.ReporterRef;

/**
 * <p>
 * The attribute field defines a single value property to a {@link RecordClass}.
 * When a {@link RecordIntance} is created for a {@link RecordClass}, an
 * {@link AttributeValue} of a given AttributeField will be created to hold the
 * actual value of the field.
 * </p>
 * 
 * <p>
 * Some types of the attribute fields can embed other attribute fields in its
 * content, and the values of those attribute fields will be substituted into
 * the content to produce the the value for the {@link AttributeValue} of the
 * current field.
 * </p>
 * 
 * @author Jerric
 * @created Jan 19, 2006
 */
public abstract class AttributeField extends Field implements Cloneable {

  private boolean _sortable = true;
  private String _align;
  private boolean _nowrap = false;
  private boolean _removable = true;
  private String _categoryName;
  protected AttributeFieldContainer _container;

  private List<AttributeReporterRef> _reporterList = new ArrayList<AttributeReporterRef>();
  private Map<String, AttributeReporterRef> _reporterMap;

  public abstract Map<String, ColumnAttributeField> getColumnAttributeFields() throws WdkModelException;

  public abstract Optional<AttributeFieldDataType> getDataType();

  @Override
  public AttributeField clone() {
    return (AttributeField) super.clone();
  }

  @RngUndefined
  public void setContainer(AttributeFieldContainer container) {
    _container = container;
  }

  AttributeFieldContainer getContainer() {
    return _container;
  }

  /**
   * by default, an attribute can be removed from the result page.
   * 
   * @return
   */
  public boolean isRemovable() {
    return _removable;
  }

  /**
   * @param removable
   *          the removable to set
   */
  @RngOptional
  public void setRemovable(boolean removable) {
    _removable = removable;
  }

  /**
   * @return the sortable
   */
  public boolean isSortable() {
    return _sortable;
  }

  /**
   * @param sortable
   *          the sortable to set
   */
  @RngOptional
  public void setSortable(boolean sortable) {
    _sortable = sortable;
  }

  /**
   * @return the align
   */
  public String getAlign() {
    return _align;
  }

  /**
   * @param align
   *          the align to set
   */
  @RngOptional
  public void setAlign(String align) {
    _align = align;
  }

  /**
   * @return the nowrap
   */
  public boolean isNowrap() {
    return _nowrap;
  }

  /**
   * @param nowrap
   *          the nowrap to set
   */
  @RngOptional
  public void setNowrap(boolean nowrap) {
    _nowrap = nowrap;
  }

  /**
   * @return attribute category name
   */
  public String getAttributeCategory() {
    return _categoryName;
  }

  /**
   * @param categoryName
   *          attribute category name
   */
  @RngOptional
  public void setAttributeCategory(String categoryName) {
    _categoryName = categoryName;
  }

  public boolean isDerived() {
    return false;
  }

  public void addReporterReference(AttributeReporterRef reference) {
    reference.setAttributeField(this);
    if (_reporterList != null)
      _reporterList.add(reference);
    else
      _reporterMap.put(reference.getName(), reference);
  }

  public Map<String, AttributeReporterRef> getReporters() {
    return new LinkedHashMap<String, AttributeReporterRef>(_reporterMap);
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude reporter references
    _reporterMap = new LinkedHashMap<String, AttributeReporterRef>();
    for (AttributeReporterRef reporter : _reporterList) {
      if (reporter.include(projectId)) {
        String name = reporter.getName();
        if (_reporterMap.containsKey(name))
          throw new WdkModelException("The reporter '" + name
              + "' is duplicated in attribute " + _name);
        reporter.excludeResources(projectId);
        _reporterMap.put(name, reporter);
      }
    }
    _reporterList = null;

  }

  @Override
  public Map<String, String[]> getPropertyLists() {
    // KLUGE!!!  Override getPropertyLists().  If wdkModel is null, then this
    //   field is contained in a TableField, which does not call resolveReferences
    //   on its attributes.  Return an empty map in this case.
    if (_wdkModel == null) {
      return Collections.emptyMap();
    }
    return super.getPropertyLists();
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    // resolve plugin references
    for (ReporterRef reporter : _reporterMap.values()) {
      reporter.resolveReferences(wdkModel);
    }
  }
}
