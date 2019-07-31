package org.gusdb.wdk.model.toolbundle.impl;

import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;
import org.gusdb.wdk.model.toolbundle.ColumnToolInstance;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

public class AbstractColumnToolInstance implements ColumnToolInstance {

  private AnswerValue _answer;

  private AttributeField _column;

  private JsonNode _config;

  protected AbstractColumnToolInstance(AnswerValue answerValue, AttributeField column, ColumnToolConfig config) {
    _answer = answerValue;
    _column = column;
    _config = config.getConfig();
  }

  protected AnswerValue getAnswerValue() {
    return _answer;
  }

  protected AttributeField getColumn() {
    return _column;
  }

  protected JsonNode getConfig() {
    return _config;
  }

  @Override
  public String toString() {
    return new JSONObject()
    .put("column", _column.getName())
    .put("config", _config.toString())
    .put("answer", _answer.toString())
    .toString();
  }
}
