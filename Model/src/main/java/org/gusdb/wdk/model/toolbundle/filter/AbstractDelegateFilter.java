package org.gusdb.wdk.model.toolbundle.filter;

import com.fasterxml.jackson.databind.JsonNode;
import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import org.gusdb.fgputil.functional.Result;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.toolbundle.ColumnFilter;
import org.gusdb.wdk.model.toolbundle.ColumnFilterInstance;
import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;
import org.gusdb.wdk.model.toolbundle.impl.AbstractColumnTool;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gusdb.fgputil.FormatUtil.NL;

public abstract class AbstractDelegateFilter extends AbstractColumnTool<ColumnFilterInstance> implements ColumnFilter {

  private ColumnFilter[] _delegatees;

  protected AbstractDelegateFilter(ColumnFilter... delegatees) {
    _delegatees = delegatees;
  }

  /**
   * Creates an configured instance of this tool
   * 
   * @param answerValue answer value from which instance should be made
   * @param field column to which this tool has been assigned
   * @param config configuration of the instance
   * @return configured instance of this tool
   * @throws WdkModelException if unable to create an instance with this config
   */
  @Override
  public ColumnFilterInstance makeInstance(AnswerValue answerValue, AttributeField field, ColumnToolConfig config) throws WdkModelException {
    final var errs = new StringBuilder();
    final var okay = new ArrayList<ColumnFilter>();

    for (final var res : requireFilters(field.getDataType(), config.getConfig()))
      if (res.isError())
        errs.append(res.getError().getMessage()).append(NL);
      else
        okay.add(res.getValue());

    if (okay.size() == 1)
      return okay.get(0).makeInstance(answerValue, field, config);

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
  public SchemaBuilder getInputSpec(AttributeFieldDataType type) {
    var out = Schema.draft4().oneOf();
    for (var d : _delegatees)
      if (d.isCompatibleWith(type))
        out.add(d.getInputSpec(type));
    return out;
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type, JsonNode js) {
    return filterDelegatees(type, js).anyMatch(x -> true);
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
    return new JSONObject(super.toString())
      .put("delegatees", new JSONArray(_delegatees))
      .toString();
  }

  private Stream<ColumnFilter> filterDelegatees(AttributeFieldDataType type, JsonNode config) {
    return Arrays.stream(_delegatees)
      .filter(c -> c.isCompatibleWith(type))
      .filter(c -> c.isCompatibleWith(type, config));
  }

  private List<Result<WdkModelException, ColumnFilter>> tryDelegatees(
      AttributeFieldDataType type, JsonNode config
  ) {
    return filterDelegatees(type, config)
      .map(d -> tryConfig(d, type, config))
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
    final AttributeFieldDataType type,
    final JsonNode config
  ) {
    try {
      filter.validateConfig(type, config);
      return Result.value(filter);
    } catch (WdkUserException e) {
      return Result.error(new WdkModelException(e));
    }
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
    final AttributeFieldDataType type,
    final JsonNode config
  ) throws WdkModelException {
    final var tmp = tryDelegatees(type, config);
    if (tmp.isEmpty())
      throw new WdkModelException("No column filter found compatible with given config");
    return tmp;
  }
}
