package org.gusdb.wdk.service.request.filter;

import static org.gusdb.fgputil.json.JsonUtil.Jackson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.gusdb.fgputil.json.JsonIterators;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.spec.ColumnFilterConfigSet.ColumnFilterConfigSetBuilder;
import org.gusdb.wdk.model.columntool.ColumnFilter;
import org.gusdb.wdk.model.columntool.ColumnToolFactory;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.node.ArrayNode;

class FilterConfigParser {

  private static final String
    ERR_NOT_AN_OBJECT = "value of property \"%s\" is not a JSON object",
    ERR_INVALID_COLUMN = "column \"%s\" is not a member of the search \"%s\"",
    ERR_INVALID_FILTER = "column \"%s\" does not have have configured filter \"%s\"";

  private final Question _question;
  private final Map<String,AttributeField> _attributeMap;

  FilterConfigParser(Question question) {
    _question = question;
    _attributeMap = question.getAttributeFieldMap();
  }

  ColumnFilterConfigSetBuilder parse(JSONObject columnFiltersConfig) throws WdkUserException {
    var builder = new ColumnFilterConfigSetBuilder();
    var errors = new ArrayList<String>();
    for (Entry<String,JsonType> columnEntry : JsonIterators.objectIterable(columnFiltersConfig)) {
      Optional<AttributeField> field = getColumn(columnEntry.getKey(), errors);
      Optional<JSONObject> columnConfig = getJsonObject(columnEntry, errors);
      if (field.isPresent() && columnConfig.isPresent()) {
        for (Entry<String,JsonType> filterEntry : JsonIterators.objectIterable(columnConfig.get())) {
          Optional<ColumnFilter> filterOpt = getFilter(field.get(), filterEntry.getKey(), errors);
          Optional<JSONObject> filterConfig = getJsonObject(filterEntry, errors)
              .flatMap(jsonObject -> filterOpt.flatMap(filter -> getToolConfig(field.get(), filter, jsonObject, errors)));
          if (filterConfig.isPresent()) {
            builder.setFilterConfig(columnEntry.getKey(), filterEntry.getKey(), filterConfig.get());
          }
        }
      }
    }
    if (!errors.isEmpty())
      throw new WdkUserException(formatErrors(errors));

    return builder;
  }

  private Optional<AttributeField> getColumn(String columnName, List<String> errors) {
    if (!_attributeMap.containsKey(columnName)) {
      return error(errors, ERR_INVALID_COLUMN, columnName, _question.getName());
    }
    return Optional.of(_attributeMap.get(columnName));
  }

  private static Optional<ColumnFilter> getFilter(AttributeField field, String filterName, List<String> errors) {
    Optional<ColumnFilter> filterOpt = ColumnToolFactory.tryColumnFilterInstance(field, filterName);
    return filterOpt.isPresent() ? filterOpt : error(errors, ERR_INVALID_FILTER, field.getName(), filterName);
  }

  private static Optional<JSONObject> getToolConfig(AttributeField column, ColumnFilter filter, JSONObject config, ArrayList<String> errors) {
    try {
      return Optional.of(filter.validateConfig(config));
    }
    catch (WdkUserException e) {
      return error(errors, e.getMessage());
    }
  }

  private static Optional<JSONObject> getJsonObject(Entry<String, JsonType> namedValue, ArrayList<String> errors) {
    if (namedValue.getValue().getType().equals(JsonType.ValueType.OBJECT)) {
      return Optional.of(namedValue.getValue().getJSONObject());
    }
    return error(errors, ERR_NOT_AN_OBJECT, namedValue.getKey());
  }

  private static <T> Optional<T> error(final List<String> errors, final String mes, final Object... in) {
    errors.add(String.format(mes, in));
    return Optional.empty();
  }

  private static String formatErrors(List<String> errors) {
    return Jackson.createObjectNode()
      .set("errors", errors.stream()
        .reduce(Jackson.createArrayNode(), ArrayNode::add, ArrayNode::addAll))
      .toString();
  }
}
