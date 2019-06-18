package org.gusdb.wdk.model.toolbundle.impl;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.ColumnTool;
import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;
import org.gusdb.wdk.model.toolbundle.ColumnToolDelegate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

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
  ) throws WdkModelException {
    final var tool = tools.get(field.getDataType());
    if (null == tool)
      return Optional.empty();

    return Optional.of(buildTool(tool, field, val, config));
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
  private <T extends ColumnTool> T buildTool(
    final T tool,
    final AttributeField field,
    final AnswerValue val,
    final ColumnToolConfig config
  ) throws WdkModelException {
    return (T) tool.copy()
      .setAnswerValue(tool.prepareAnswerValue(val))
      .setAttributeField(field)
      .setConfiguration(config);
  }
}
