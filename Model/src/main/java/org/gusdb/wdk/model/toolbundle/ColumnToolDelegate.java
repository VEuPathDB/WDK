package org.gusdb.wdk.model.toolbundle;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

import java.util.Optional;

public interface ColumnToolDelegate<S extends ColumnToolInstance, T extends ColumnTool<S>> {

  <C extends ColumnToolConfig> Optional<S> makeInstance(
    AttributeField field,
    AnswerValue answer,
    C config
  ) throws WdkModelException;

  Optional<T> getTool(AttributeField field);

  boolean hasToolFor(AttributeFieldDataType type);

  interface ColumnToolDelegateBuilder<S extends ColumnToolInstance, T extends ColumnTool<S>> {
    boolean hasToolFor(AttributeFieldDataType type);

    ColumnToolDelegateBuilder<S,T> addTool(ColumnToolBuilder<S,T> tool);

    ColumnToolDelegate<S,T> build(WdkModel wdk) throws WdkModelException;
  }
}
