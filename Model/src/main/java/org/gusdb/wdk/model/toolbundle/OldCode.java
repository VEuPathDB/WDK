package org.gusdb.wdk.model.toolbundle;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.gusdb.fgputil.functional.Result;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;

import com.fasterxml.jackson.databind.JsonNode;

public class OldCode {

  /**
   * Delegates to the {@code getFilter} method of this field's filterable
   * dependency (if present)
   *
   * @see #getFilterDependencyField()
   * @see AttributeField#getFilter(String)
   */
  @Override
  public Optional<ColumnFilter> getFilter(final String name) {
    return getFilterDependencyField().flatMap(af -> af.getFilter(name));
  }

  /**
   * Delegates to the {@code prepareFilter} method of this field's filterable
   * dependency (if present)
   *
   * @see #getFilterDependencyField()
   * @see AttributeField#prepareFilter(String, AnswerValue, ColumnToolConfig)
   */
  @Override
  public Optional<ColumnFilterInstance> makeFilterInstance(
    final String name,
    final AnswerValue val,
    final ColumnToolConfig config
  ) throws WdkModelException {
    var field = getFilterDependencyField();
    if (field.isEmpty())
      return Optional.empty();

    return field.get().makeFilterInstance(name, val, config);
  }
  
  @Override
  public Collection<String> getColumnFilterNames() {
    return getToolBundle().getTools()
      .values()
      .stream()
      .filter(s -> s.hasFilterFor(this))
      .map(ColumnToolSet::getName)
      .collect(Collectors.toList());
  }

  @Override
  public Optional<org.gusdb.wdk.model.toolbundle.ColumnFilter> getFilter(
    final String name
  ) {
    return getToolBundle().getTool(name).flatMap(t -> t.getFilterFor(this));
  }

  @Override
  public Optional<ColumnFilterInstance> makeFilterInstance(
    final String name,
    final AnswerValue val,
    final ColumnToolConfig config
  ) throws WdkModelException {
    var set = getToolBundle().getTool(name);
    if (set.isEmpty())
      return Optional.empty();

    return set.get().makeFilterInstance(this, val, config);
  }

  @Override
  public boolean isFilterable() {
    return getToolBundle().getTools()
      .values()
      .stream()
      .anyMatch(s -> s.hasFilterFor(this));
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
  
  
  ////////// IN DerivedAttributeField

  private boolean checkedFilterDependency;
  private QueryColumnAttributeField filterDependency;

  /**
   * @return whether or not this field has a filterable dependency field.
   *
   * @see #getFilterDependencyField()
   */
  @Override
  public boolean isFilterable() {
    return getFilterDependencyField().isPresent();
  }

  /**
   * Delegates to the {@code getColumnFilterNames} method of this field's
   * filterable dependency (if present)
   *
   * @see #getFilterDependencyField()
   * @see AttributeField#getColumnFilterNames()
   */
  @Override
  public Collection<String> getColumnFilterNames() {
    return getFilterDependencyField()
      .map(QueryColumnAttributeField::getColumnFilterNames)
      .orElse(Collections.emptyList());
  }

  /**
   * Returns an option of this {@code DerivedAttributeField}'s filterable
   * dependency field.
   * <p>
   * The filter dependency field is the actual backing field that will be used
   * by column filters if the client chooses to apply a column filter to this
   * {@code DerivedAttributeField}.
   * <p>
   * This {@code DerivedAttributeField} and its filterable dependency must meet
   * the following criteria:
   * <p>
   * <ul>
   * <li>This field has exactly 1 dependency
   * <li>This field's 1 dependency is an instance of
   * {@link QueryColumnAttributeField}
   * <li>this field's 1 dependency is itself filterable
   * </ul>
   *
   * @return an option containing either the filterable field if such a field
   *   exists and satisfies the above criteria, or none if the above rules are
   *   not met.
   */
  public synchronized Optional<QueryColumnAttributeField> getFilterDependencyField() {
    if (checkedFilterDependency)
      return Optional.ofNullable(filterDependency);

    checkedFilterDependency = true;

    var deps = Result.of(this::getDependencies)
      .value()
      .orElse(Collections.emptyList());

    if (deps.size() != 1)
      return Optional.empty();

    var dep = deps.iterator().next();

    if (!(dep instanceof QueryColumnAttributeField) || !dep.isFilterable())
      return Optional.empty();

    filterDependency = (QueryColumnAttributeField) dep;

    return Optional.of(filterDependency);
  }
}
