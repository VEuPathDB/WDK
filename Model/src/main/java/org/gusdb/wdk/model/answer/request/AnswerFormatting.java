package org.gusdb.wdk.model.answer.request;

import org.json.JSONObject;

public class AnswerFormatting {

  private String _format;
  private JSONObject _formatConfig;

  public AnswerFormatting(String format, JSONObject formatConfig) {
    _format = format;
    _formatConfig = formatConfig;
  }

  public String getFormat() {
    return _format;
  }

  public void setFormat(String format) {
    _format = format;
  }

  public JSONObject getFormatConfig() {
    return _formatConfig;
  }

  public void setFormatConfig(JSONObject formatConfig) {
    _formatConfig = formatConfig;
  }
}
