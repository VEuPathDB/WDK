package org.gusdb.wdk.service.request.filter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.bundle.ColumnFilter;
import org.gusdb.wdk.model.bundle.filter.StandardColumnFilterConfigSetBuilder;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.attribute.AttributeField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.gusdb.fgputil.json.JsonUtil.Jackson;

class FilterConfigParser {
  private static final String
    ERR_INVALID_COLUMN = "column \"%s\" is not a member of the search \"%s\"",
    ERR_CANNOT_FILTER  = "filter configuration provided for non-filterable " +
      "column \"%s\"",
    ERR_INVALID_FILTER = "column \"%s\" does not have have configured filter " +
      "\"%s\"";

  private final Question question;
  private final Collection<String> errors;
  private final ObjectNode config;
  private final Map<String, AttributeField> fields;
  private final StandardColumnFilterConfigSetBuilder builder;

  private String columnName;
  private ObjectNode columnConf;
  private String filterName;
  private ArrayNode filterConfSet;

  FilterConfigParser(
    final Question question,
    final ObjectNode config
  ) {
    this.question = question;
    this.config = config;
    this.errors = new ArrayList<>();
    this.builder = new StandardColumnFilterConfigSetBuilder();

    // TODO: Is NON_INTERNAL correct here?
    this.fields = question.getAttributeFieldMap(FieldScope.NON_INTERNAL);
  }

  StandardColumnFilterConfigSetBuilder parse() throws WdkUserException {
    config.fieldNames().forEachRemaining(key -> {
      columnConf = (ObjectNode) config.get(columnName = key);
      getColumn().ifPresent(this::handleColumn);
    });

    if (hasErrors())
      throw new WdkUserException(formatErrors());

    return builder;
  }

  private void handleColumn(final AttributeField field) {
    columnConf.fieldNames().forEachRemaining(key -> {
      filterConfSet = (ArrayNode) columnConf.get(filterName = key);
      getFilters(field).ifPresent(this::handleFilters);
    });
  }

  private void handleFilters(final ColumnFilter filter) {
    filterConfSet.forEach(node -> handleFilter(filter, (ObjectNode) node));
  }

  private void handleFilter(final ColumnFilter filter, final ObjectNode conf) {
    try {
      builder.append(columnName, filterName, filter.validateConfig(conf));
    } catch (WdkUserException e) {
      errors.add(e.getMessage());
    }
  }

  private Optional<AttributeField> getColumn() {
    if (!fields.containsKey(columnName)) {
      return error(ERR_INVALID_COLUMN, columnName, question.getFullName());
    }
    return Optional.of(fields.get(columnName));
  }

  private Optional<ColumnFilter> getFilters(final AttributeField field) {
    if (!field.isFilterable())
      return error(ERR_CANNOT_FILTER, columnName);

    final var fil = field.getFilter(filterName);

    if (fil.isEmpty())
      return error(ERR_INVALID_FILTER, columnName, filterName);

    return fil;
  }

  private String formatErrors() {
    return Jackson.createObjectNode()
      .set("errors", errors.stream()
        .reduce(Jackson.createArrayNode(), ArrayNode::add, ArrayNode::addAll))
      .toString();
  }

  private boolean hasErrors() {
    return !errors.isEmpty();
  }

  private <T> Optional<T> error(final String mes, final Object... in) {
    errors.add(String.format(mes, in));
    return Optional.empty();
  }
}
