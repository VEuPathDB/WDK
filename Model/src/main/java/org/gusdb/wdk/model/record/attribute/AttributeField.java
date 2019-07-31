package org.gusdb.wdk.model.record.attribute;

import com.fasterxml.jackson.databind.JsonNode;
import org.gusdb.wdk.model.RngAnnotations.RngOptional;
import org.gusdb.wdk.model.RngAnnotations.RngUndefined;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.toolbundle.*;
import org.gusdb.wdk.model.toolbundle.impl.EmptyToolBundle;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.report.AttributeReporterRef;
import org.gusdb.wdk.model.report.ReporterRef;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

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
  private ColumnToolBundle _toolBundle;

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
    this._toolBundleRef = toolBundleRef;
  }

  public ColumnToolBundle getToolBundle() {
    // This can happen if the attribute field is used without reference
    // resolution
    return isNull(_toolBundle)
      ? (_toolBundle = resolveToolBundle())
      : _toolBundle;
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
   * Prepares and configures the column reporter with the given name if such a
   * column reporter exists.
   *
   * @param name
   *   name of the column reporter to prepare
   * @param val
   *   answer value the column reporter will operate on
   * @param config
   *   raw client configuration for the reporter
   *
   * @return an option containing either a configured column reporter if a one
   * was found matching the given name, or none if no such reporter was found.
   *
   * @throws WdkUserException
   *   if the given json client configuration is invalid.
   */
  public Optional<ColumnReporterInstance> makeReporterInstance(
    final String name,
    final AnswerValue val,
    final JsonNode config
  ) throws WdkModelException, WdkUserException {
    var tmp = getReporter(name);
    if (tmp.isEmpty())
      return Optional.empty();

    var conf = tmp.get().validateConfig(getDataType(), config);
    var set  = getToolBundle().getTool(name);
    if (set.isEmpty())
      return Optional.empty();

    return set.get().makeReporterInstance(this, val, conf);
  }

  /**
   * Convenience shortcut to get an unconfigured instance of the column reporter
   * with the given name.
   *
   * @param name
   *   name of the column reporter to return.
   *
   * @return
   */
  public Optional<ColumnReporter> getReporter(final String name) {
    return getToolBundle().getTool(name)
      .flatMap(t -> t.getReporterFor(this));
  }

  /**
   * Retrieves and returns a list of the names of all the available column
   * reporters that can be used on this field.
   *
   * @return list of reporters available for this field by name
   */
  public Collection<String> getColumnReporterNames() {
    return getToolBundle().getTools()
      .values()
      .stream()
      .filter(s -> s.hasReporterFor(this))
      .map(ColumnToolSet::getName)
      .collect(Collectors.toList());
  }

  /**
   * Convenience shortcut to get an unconfigured instance of the column filter
   * with the given name.
   *
   * @param name
   *   name of the column filter to return.
   *
   * @return an option of either an unconfigured column filter matching the
   * given name, or an empty option if no such filter exists.
   */
  public Optional<ColumnFilter> getFilter(final String name) {
    return Optional.empty();
  }

  /**
   * Prepares and returns an option of a configured, runnable column filter.
   *
   * @param name
   *   name of the column filter to be configured
   * @param val
   *   answer value that the column filter will be applied to
   * @param config
   *   validated client config to use in configuring the column filter
   *
   * @return an option of either a configured column filter matching the given
   * name, or an empty option if no such filter exists.
   * 
   * @throws WdkModelException if unable to create an instance of the specified filter
   */
  public Optional<ColumnFilterInstance> makeFilterInstance(
    @SuppressWarnings("unused") final String name,
    @SuppressWarnings("unused") final AnswerValue val,
    @SuppressWarnings("unused") final ColumnToolConfig config
  ) throws WdkModelException {
    return Optional.empty();
  }

  /**
   * Retrieves a list of the usable column filter names for the current field.
   *
   * @return list of available column filters by name
   */
  public Collection<String> getColumnFilterNames() {
    return Collections.emptyList();
  }

  /**
   * Convenience method for determining whether or not a field is filterable.
   * <p>
   * A field is considered filterable if it is of the correct field type
   * <i>and</i> has at least one column filter that could be applied.
   * <p>
   * Note: the base attribute field type is not filterable, however extending
   * classes override this with their own internal checks on whether or not the
   * above criteria is met.
   *
   * @return whether or not this field is filterable
   */
  public boolean isFilterable() {
    return false;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    _toolBundle = isNull(_toolBundleRef)
      ? resolveDefaultToolBundle()
      : resolveToolBundleRef().orElseThrow(() -> new WdkModelException(
        "Invalid columnToolBundle reference: " + _toolBundleRef));

    // resolve plugin references
    for (ReporterRef reporter : _reporterMap.values()) {
      reporter.resolveReferences(wdkModel);
    }
  }

  private ColumnToolBundle resolveToolBundle() {
    return resolveToolBundleRef().orElseGet(this::resolveDefaultToolBundle);
  }

  private Optional<ColumnToolBundle> resolveToolBundleRef() {
    return isNull(_toolBundleRef)
      ? Optional.empty()
      : _wdkModel.getColumnToolBundle(_toolBundleRef);
  }

  private ColumnToolBundle resolveDefaultToolBundle() {
    if (_container instanceof TableField)
      return ((TableField) _container).getRecordClass().getDefaultToolBundle();

    if (_container instanceof RecordClass)
      return ((RecordClass) _container).getDefaultToolBundle();

    if (_container instanceof Question)
      return ((Question) _container).getRecordClass().getDefaultToolBundle();

    return new EmptyToolBundle();
  }
}
