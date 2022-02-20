package org.gusdb.wdk.model.record.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.gusdb.wdk.model.RngAnnotations.RngOptional;
import org.gusdb.wdk.model.RngAnnotations.RngUndefined;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.columntool.ColumnTool;
import org.gusdb.wdk.model.columntool.ColumnToolBundle;
import org.gusdb.wdk.model.columntool.ColumnToolElementPair;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.report.AttributeReporterRef;
import org.gusdb.wdk.model.report.ReporterRef;

/**
 * The attribute field defines a single value property to a {@link RecordClass}.
 * When a {@code RecordInstance} is created for a {@link RecordClass}, an
 * {@link AttributeValue} of a given AttributeField will be created to hold the
 * actual value of the field.
 * <p>
 * Some types of the attribute fields can embed other attribute fields in its
 * content, and the values of those attribute fields will be substituted into
 * the content to produce the the value for the {@link AttributeValue} of the
 * current field.
 *
 * @author Jerric
 * @since  Jan 19, 2006
 */
public abstract class AttributeField extends Field implements Cloneable {

  private boolean _sortable = true;
  private String _align;
  private boolean _nowrap;
  private boolean _removable = true;
  private String _categoryName;

  protected AttributeFieldDataType _dataType = AttributeFieldDataType.OTHER;
  protected AttributeFieldContainer _container;

  private List<AttributeReporterRef> _reporterList = new ArrayList<>();
  private Map<String, AttributeReporterRef> _reporterMap;

  private String _toolBundleRef;
  protected Map<String,ColumnToolElementPair> _columnToolElementPairs = new HashMap<>();

  public abstract Map<String, ColumnAttributeField> getColumnAttributeFields() throws WdkModelException;

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

  public AttributeFieldDataType getDataType() {
    return _dataType;
  }

  public boolean isDerived() {
    return false;
  }

  @RngOptional
  public void setToolBundleRef(String toolBundleRef) {
    _toolBundleRef = toolBundleRef;
  }

  public void addReporterReference(AttributeReporterRef reference) {
    reference.setAttributeField(this);
    if (_reporterList != null)
      _reporterList.add(reference);
    else
      _reporterMap.put(reference.getName(), reference);
  }

  public Map<String, AttributeReporterRef> getReporters() {
    return new LinkedHashMap<>(_reporterMap);
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude reporter references
    _reporterMap = new LinkedHashMap<>();
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

  /**
   * Returns a list of available column tool names for this field.  By
   * default, regardles of assigned column tools, this is the empty list.
   * It is overridden only by QueryColumnAttributeFields, whose statistics
   * can be reported and who can be filtered.
   *
   * @return list of available column tools by name
   */
  public Map<String, ColumnToolElementPair> getColumnToolElementPairs() {
    return Collections.emptyMap();
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    // resolve tool bundle ref for this attribute
    if (_toolBundleRef == null) {
      // use parent default (may still be null
      _toolBundleRef = resolveDefaultToolBundleRef();
    }

    // if tool bundle ref specified for this attribute field, resolve all tools in the
    //   bundle that have element pairs compatible with this attribute's data type
    if (_toolBundleRef != null) {
      ColumnToolBundle toolBundle = wdkModel.getColumnToolBundleMap().getToolBundle(_toolBundleRef);
      for (ColumnTool tool : toolBundle.getTools()) {
        ColumnToolElementPair elements = tool.getElementPair(_dataType);
        if (elements != null) {
          _columnToolElementPairs.put(tool.getName(), elements);
        }
      }
    }

    // resolve plugin references
    for (ReporterRef reporter : _reporterMap.values()) {
      reporter.resolveReferences(wdkModel);
    }
  }

  private String resolveDefaultToolBundleRef() {
    if (_container instanceof TableField)
      return ((TableField) _container).getRecordClass().getDefaultColumnToolBundleRef();

    if (_container instanceof RecordClass)
      return ((RecordClass) _container).getDefaultColumnToolBundleRef();

    if (_container instanceof Question)
      return ((Question) _container).getRecordClass().getDefaultColumnToolBundleRef();

    return null;
  }

  // all tools must have a reporter per the RNG
  public Set<String> getColumnReporterNames() {
    return _columnToolElementPairs.keySet();
  }

  // but only some tools have a filter; find which ones
  public List<String> getColumnFilterNames() {
    return _columnToolElementPairs.entrySet().stream()
      .filter(entry -> entry.getValue().getFilter() != null)
      .map(entry -> entry.getKey())
      .collect(Collectors.toList());
  }
}
