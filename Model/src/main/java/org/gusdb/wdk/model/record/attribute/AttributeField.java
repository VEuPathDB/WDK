package org.gusdb.wdk.model.record.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gusdb.wdk.model.RngAnnotations.RngOptional;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.attribute.plugin.AttributePluginReference;
import org.gusdb.wdk.model.report.AbstractAttributeReporter;
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

  private List<AttributePluginReference> _pluginList = new ArrayList<AttributePluginReference>();
  private Map<String, AttributePluginReference> _pluginMap;
  
  private List<ReporterRef> _reporterList = new ArrayList<ReporterRef>();
  private Map<String, ReporterRef> _reporterMap;

  public abstract Map<String, ColumnAttributeField> getColumnAttributeFields() throws WdkModelException;

  @Override
  public AttributeField clone() {
    return (AttributeField) super.clone();
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

  public void addAttributePluginReference(AttributePluginReference reference) {
    reference.setAttributeField(this);
    if (_pluginList != null)
      _pluginList.add(reference);
    else
      _pluginMap.put(reference.getName(), reference);
  }

  public Map<String, AttributePluginReference> getAttributePlugins() {
    return new LinkedHashMap<String, AttributePluginReference>(_pluginMap);
  }

  public void addReporterReference(ReporterRef reference) {
    WdkModelText prop = new WdkModelText();
    prop.setName(AbstractAttributeReporter.ATTRIBUTE_FIELD_PROP);
    prop.setText(getName());
    reference.addProperty(prop);
    if (_reporterList != null)
      _reporterList.add(reference);
    else
      _reporterMap.put(reference.getName(), reference);
  }

  public Map<String, ReporterRef> getReporters() {
    return new LinkedHashMap<String, ReporterRef>(_reporterMap);
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude attribute plugin references
    _pluginMap = new LinkedHashMap<String, AttributePluginReference>();
    for (AttributePluginReference plugin : _pluginList) {
      if (plugin.include(projectId)) {
        String name = plugin.getName();
        if (_pluginMap.containsKey(name))
          throw new WdkModelException("The plugin '" + name
              + "' is duplicated in attribute " + _name);
        plugin.excludeResources(projectId);
        _pluginMap.put(name, plugin);
      }
    }
    _pluginList = null;
    
    // exclude reporter references
    _reporterMap = new LinkedHashMap<String, ReporterRef>();
    for (ReporterRef reporter : _reporterList) {
      if (reporter.include(projectId)) {
        String name = reporter.getName();
        if (_reporterMap.containsKey(name))
          throw new WdkModelException("The reporter '" + name
              + "' is duplicated in attribute " + _name);
        reporter.excludeResources(projectId);
        _reporterMap.put(getName() + "/" + name, reporter);  // prepend this attribute's name to the reporter for uniqueness
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
    for (AttributePluginReference plugin : _pluginMap.values()) {
      plugin.resolveReferences(wdkModel);
    }
    
    // resolve plugin references
    for (ReporterRef reporter : _reporterMap.values()) {
      reporter.resolveReferences(wdkModel);
    }
  }
  
}
