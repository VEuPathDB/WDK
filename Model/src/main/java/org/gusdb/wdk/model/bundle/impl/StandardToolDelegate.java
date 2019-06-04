package org.gusdb.wdk.model.bundle.impl;

import org.gusdb.wdk.model.bundle.ColumnToolConfig;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.bundle.ColumnTool;
import org.gusdb.wdk.model.bundle.ColumnToolDelegate;
import org.gusdb.wdk.model.record.attribute.AttributeField;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

class StandardToolDelegate<T extends ColumnTool>
implements ColumnToolDelegate<T> {

  private final Map<AttributeFieldDataType, T> tools;

  StandardToolDelegate(final Map<AttributeFieldDataType, T> tools) {
    this.tools = Collections.unmodifiableMap(tools);
  }

  @Override
  public Optional<T> prepareTool(
    final AttributeField field,
    final AnswerValue val,
    final ColumnToolConfig config
  ) {
    return Optional.ofNullable(tools.get(field.getDataType()))
      .map(buildTool(field, val, config));
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<T> getTool(AttributeField field) {
    return Optional.ofNullable(tools.get(field.getDataType()))
      .map(t -> (T) t.setAttributeField(field));
  }

  @Override
  public boolean hasToolFor(final AttributeFieldDataType type) {
    return tools.containsKey(type);
  }

  @SuppressWarnings("unchecked")
  private Function<T, T> buildTool(
    final AttributeField field,
    final AnswerValue val,
    final ColumnToolConfig config
  ) {
    return v -> (T) v.copy()
      .setAnswerValue(val)
      .setAttributeField(field)
      .setConfiguration(config);
  }
}
