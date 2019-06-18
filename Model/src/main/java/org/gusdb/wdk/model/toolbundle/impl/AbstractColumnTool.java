package org.gusdb.wdk.model.toolbundle.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.toolbundle.ColumnTool;
import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.json.JSONObject;

import java.util.Map;

public abstract class AbstractColumnTool implements ColumnTool {

  private JsonNode _config;

  private AttributeField _column;

  private Map<String, String> _props;

  private AnswerValue _answer;

  private String _key;

  public abstract void parseConfig(JsonNode config);

  @Override
  public ColumnTool setKey(final String key) {
    _key = key;
    return this;
  }

  @Override
  public String getKey() {
    return _key;
  }

  @Override
  public ColumnTool setAnswerValue(PreparedAnswerValue val) {
    _answer = val.get();
    return this;
  }

  @Override
  public ColumnTool setAttributeField(final AttributeField field) {
    _column = field;
    return this;
  }

  @Override
  public ColumnTool setModelProperties(Map<String, String> props) {
    _props = Map.copyOf(props);
    return this;
  }

  @Override
  public ColumnTool setConfiguration(ColumnToolConfig config) {
    _config = config.getConfig();
    parseConfig(_config);
    return this;
  }

  @Override
  public String toString() {
    return new JSONObject()
      .put("class", getClass().getName())
      .put("props", _props)
      .put("column", _column == null
        ? JSONObject.NULL
        : new JSONObject().put("name", _column.getName()))
      .put("config", _config == null
        ? JSONObject.NULL
        : new JSONObject(_config.toString()))
      .put("answer", _answer == null
        ? JSONObject.NULL
        : _answer.toString())
      .toString();
  }

  protected AnswerValue getAnswer() {
    return _answer;
  }

  protected Map<String, String> getProps() {
    return _props;
  }

  protected JsonNode getConfig() {
    return _config;
  }

  protected AttributeField getColumn() {
    return _column;
  }

  protected <T extends ColumnTool> T copyInto(final T tool) {
    if (_config != null)
      tool.setConfiguration(() -> _config.deepCopy());
    if (_column != null)
      tool.setAttributeField(_column);
    if (_props != null)
      tool.setModelProperties(_props);
    if (_answer != null)
      tool.setAnswerValue(() -> _answer);
    return tool;
  }
}
