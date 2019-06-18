package org.gusdb.wdk.model.toolbundle.filter;

import com.fasterxml.jackson.databind.JsonNode;
import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import org.gusdb.fgputil.functional.Result;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.toolbundle.ColumnFilter;
import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gusdb.fgputil.FormatUtil.NL;

abstract class DelegateFilter implements ColumnFilter {

  private Map<String, String> _properties;

  private String _key;

  private AnswerValue _answer;

  private AttributeField _column;

  private ColumnToolConfig _config;

  private ColumnFilter[] _delegatees;

  DelegateFilter(ColumnFilter... delegatees) {
    _properties = Collections.emptyMap();
    _delegatees = delegatees;
  }

  @Override
  public DelegateFilter setKey(String key) {
    _key = key;
    return this;
  }

  @Override
  public String getKey() {
    return _key;
  }

  @Override
  public DelegateFilter setAnswerValue(PreparedAnswerValue val) {
    _answer = val.get();

    for (var d : _delegatees)
      d.setAnswerValue(val);

    return this;
  }

  @Override
  public DelegateFilter setAttributeField(AttributeField field) {
    _column = field;

    for (var d : _delegatees)
      d.setAttributeField(field);

    return this;
  }

  @Override
  public DelegateFilter setModelProperties(Map<String, String> props) {
    _properties = Map.copyOf(props);

    for (var d : _delegatees)
      d.setModelProperties(_properties);

    return this;
  }

  @Override
  public DelegateFilter setConfiguration(ColumnToolConfig config) {
    _config = config;
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
    for (var d : _delegatees)
      if (_column == null || d.isCompatibleWith(_column.getDataType()))
        out.add(d.inputSpec());
    return out;
  }

  @Override
  public boolean isCompatibleWith(JsonNode js) {
    return filterDelegatees(js).anyMatch(x -> true);
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    for (var d : _delegatees)
      if (d.isCompatibleWith(type))
        return true;
    return false;
  }

  @Override
  public String toString() {
    return new JSONObject()
      .put("class", getClass().getName())
      .put("properties", _properties)
      .put("column", _column == null
        ? JSONObject.NULL
        : new JSONObject().put("name", _column.getName()))
      .put("config", _config == null
        ? JSONObject.NULL
        : new JSONObject(_config.getConfig().toString()))
      .put("answer", _answer == null
        ? JSONObject.NULL
        : _answer.toString())
      .put("delegatees", new JSONArray(_delegatees))
      .toString();
  }

  protected Optional<ColumnToolConfig> getConfig() {
    return Optional.ofNullable(_config);
  }

  protected final <T extends DelegateFilter> T copyInto(T copy) {
    DelegateFilter val = copy;
    val._config     = _config;
    val._properties = Map.copyOf(_properties);
    val._column     = _column;
    val._answer     = _answer;
    val._delegatees = Arrays.stream(_delegatees)
      .map(ColumnFilter::copy)
      .toArray(ColumnFilter[]::new);
    return copy;
  }

  private ColumnFilter configure(ColumnFilter filter) {
    filter.setConfiguration(_config);
    filter.setAttributeField(_column);
    filter.setAnswerValue(() -> _answer);
    filter.setModelProperties(_properties);
    return filter;
  }

  private Stream<ColumnFilter> filterDelegatees(final JsonNode config) {
    return Arrays.stream(_delegatees)
      .filter(c -> c.isCompatibleWith(_column.getDataType()))
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
