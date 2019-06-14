package org.gusdb.wdk.model.toolbundle;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

import java.util.Optional;

public interface ColumnToolDelegate<T extends ColumnTool> {
  <C extends ColumnToolConfig> Optional<T> prepareTool(
    AttributeField field,
    AnswerValue answer,
    C config
  );

  Optional<T> getTool(AttributeField field);

  boolean hasToolFor(AttributeFieldDataType type);

  interface ColumnToolDelegateBuilder<T extends ColumnTool> {
    boolean hasToolFor(AttributeFieldDataType type);

    ColumnToolDelegateBuilder<T> addTool(ColumnToolBuilder<T> tool);

    ColumnToolDelegate<T> build(WdkModel wdk) throws WdkModelException;
  }
}
