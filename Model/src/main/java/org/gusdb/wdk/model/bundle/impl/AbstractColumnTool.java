package org.gusdb.wdk.model.bundle.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.bundle.ColumnTool;
import org.gusdb.wdk.model.bundle.ColumnToolConfig;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.json.JSONObject;

import java.util.Map;

public abstract class AbstractColumnTool implements ColumnTool {
  private JsonNode config;

  private AttributeField column;

  private Map<String, String> props;

  private AnswerValue answer;

  private String key;

  public abstract void parseConfig(JsonNode config);

  @Override
  public ColumnTool setKey(final String key) {
    this.key = key;
    return this;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public ColumnTool setAnswerValue(AnswerValue val) {
    this.answer = val;
    return this;
  }

  @Override
  public ColumnTool setAttributeField(final AttributeField field) {
    this.column = field;
    return this;
  }

  @Override
  public ColumnTool setModelProperties(Map<String, String> props) {
    this.props = Map.copyOf(props);
    return this;
  }

  @Override
  public ColumnTool setConfiguration(ColumnToolConfig config) {
    this.config = config.getConfig();
    parseConfig(this.config);
    return this;
  }

  @Override
  public String toString() {
    return new JSONObject()
      .put("class", getClass().getName())
      .put("props", props)
      .put("column", column == null
        ? JSONObject.NULL
        : new JSONObject().put("name", column.getName()))
      .put("config", config == null
        ? JSONObject.NULL
        : new JSONObject(config.toString()))
      .put("answer", answer == null
        ? JSONObject.NULL
        : answer.toString())
      .toString();
  }

  protected AnswerValue getAnswer() {
    return answer;
  }

  protected Map<String, String> getProps() {
    return this.props;
  }

  protected JsonNode getConfig() {
    return this.config;
  }

  protected AttributeField getColumn() {
    return column;
  }

  protected <T extends ColumnTool> T copyInto(final T tool) {
    if (config != null)
      tool.setConfiguration(() -> config.deepCopy());
    if (column != null)
      tool.setAttributeField(column);
    if (props != null)
      tool.setModelProperties(props);
    if (answer != null)
      tool.setAnswerValue(answer);
    return tool;
  }
}
