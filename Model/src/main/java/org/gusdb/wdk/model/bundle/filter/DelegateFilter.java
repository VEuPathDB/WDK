package org.gusdb.wdk.model.bundle.filter;

import com.fasterxml.jackson.databind.JsonNode;
import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import org.gusdb.fgputil.functional.Result;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.bundle.ColumnFilter;
import org.gusdb.wdk.model.bundle.ColumnToolConfig;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gusdb.fgputil.FormatUtil.NL;

abstract class DelegateFilter implements ColumnFilter {

  private Map<String, String> properties;

  private String key;

  private AnswerValue answer;

  private AttributeField column;

  private ColumnToolConfig config;

  private ColumnFilter[] delegatees;

  DelegateFilter(ColumnFilter... delegatees) {
    this.properties = Collections.emptyMap();
    this.delegatees = delegatees;
  }

  @Override
  public DelegateFilter setKey(String key) {
    this.key = key;
    return this;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public DelegateFilter setAnswerValue(AnswerValue val) {
    this.answer = val;

    for (var d : delegatees)
      d.setAnswerValue(val);

    return this;
  }

  @Override
  public DelegateFilter setAttributeField(AttributeField field) {
    this.column = field;

    for (var d : delegatees)
      d.setAttributeField(field);

    return this;
  }

  @Override
  public DelegateFilter setModelProperties(Map<String, String> props) {
    this.properties = Map.copyOf(props);

    for (var d : delegatees)
      d.setModelProperties(this.properties);

    return this;
  }

  @Override
  public DelegateFilter setConfiguration(ColumnToolConfig config) {
    this.config = config;
    return this;
  }

  @Override
  public SqlBuilder build() throws WdkModelException {
    final var errs = new StringBuilder();
    final var okay = new ArrayList<ColumnFilter>();

    for (final var res : requireFilters(requireConfig()))
      if (res.isError())
        errs.append(res.getError().getMessage()).append(NL);
      else
        okay.add(res.getValue());

    if (okay.size() == 1)
      return configure(okay.get(0)).build();

    if (okay.size() > 1)
      errs.append("Multiple column filters match the given config: ")
        .append(okay.stream()
          .map(Object::getClass)
          .map(Class::getSimpleName)
          .collect(Collectors.joining(", ")))
        .append(NL);

    errs.setLength(errs.length() - 1);
    throw new WdkModelException(errs.toString());
  }

  @Override
  public SchemaBuilder inputSpec() {
    var out = Schema.draft4().oneOf();
    for (var d : delegatees)
      if (column == null || d.isCompatibleWith(column.getDataType()))
        out.add(d.inputSpec());
    return out;
  }

  @Override
  public boolean isCompatibleWith(JsonNode js) {
    return filterDelegatees(js).anyMatch(x -> true);
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    for (var d : delegatees)
      if (d.isCompatibleWith(type))
        return true;
    return false;
  }

  @Override
  public String toString() {
    return new JSONObject()
      .put("class", getClass().getName())
      .put("properties", properties)
      .put("column", column == null
        ? JSONObject.NULL
        : new JSONObject().put("name", column.getName()))
      .put("config", config == null
        ? JSONObject.NULL
        : new JSONObject(config.getConfig().toString()))
      .put("answer", answer == null
        ? JSONObject.NULL
        : answer.toString())
      .put("delegatees", new JSONArray(delegatees))
      .toString();
  }

  protected Optional<ColumnToolConfig> getConfig() {
    return Optional.ofNullable(config);
  }

  protected final <T extends DelegateFilter> T copyInto(T copy) {
    var val = (DelegateFilter) copy;
    val.config     = this.config;
    val.properties = Map.copyOf(this.properties);
    val.column     = this.column;
    val.answer     = this.answer;
    val.delegatees = Arrays.stream(delegatees)
      .map(ColumnFilter::copy)
      .toArray(ColumnFilter[]::new);
    return copy;
  }

  private ColumnFilter configure(ColumnFilter filter) {
    filter.setConfiguration(config);
    filter.setAttributeField(column);
    filter.setAnswerValue(answer);
    filter.setModelProperties(properties);
    return filter;
  }

  private Stream<ColumnFilter> filterDelegatees(final JsonNode config) {
    return Arrays.stream(delegatees)
      .filter(c -> c.isCompatibleWith(column.getDataType()))
      .filter(c -> c.isCompatibleWith(config));
  }

  private List<Result<WdkModelException, ColumnFilter>> tryDelegatees(
    final JsonNode config
  ) {
    return filterDelegatees(config)
      .map(d -> tryConfig(d, config))
      .collect(Collectors.toList());
  }

  /**
   * Attempts to validate the given config against the given filter.
   *
   * @param filter
   *   Column filter to use for validation
   * @param config
   *   User configuration to try to validate against the given {@link
   *   ColumnFilter}
   *
   * @return validation result
   */
  private Result<WdkModelException, ColumnFilter> tryConfig(
    final ColumnFilter filter,
    final JsonNode config
  ) {
    try {
      filter.validateConfig(config);
      return Result.value(filter);
    } catch (WdkUserException e) {
      return Result.error(new WdkModelException(e));
    }
  }

  /**
   * Requires and returns a configuration for this column tool
   *
   * @return raw json configuration for a filter
   *
   * @throws WdkModelException if the config was not yet set
   */
  private JsonNode requireConfig() throws WdkModelException {
    return getConfig().map(ColumnToolConfig::getConfig)
      .orElseThrow(WdkModelException::new);
  }

  /**
   * Requires that there is at least one filter result
   *
   * @param config config to try against the delegatee filters
   *
   * @return a list of results of filter configuration attempts
   *
   * @throws WdkModelException if there were no filter results
   */
  private List<Result<WdkModelException, ColumnFilter>> requireFilters(
    final JsonNode config
  ) throws WdkModelException {
    final var tmp = tryDelegatees(config);
    if (tmp.isEmpty())
      throw new WdkModelException("No column filter found compatible with given config");
    return tmp;
  }
}
