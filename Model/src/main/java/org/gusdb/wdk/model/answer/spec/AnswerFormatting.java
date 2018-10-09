package org.gusdb.wdk.model.answer.spec;

import org.json.JSONObject;

public class AnswerFormatting {
  private String format;
  private JSONObject formatConfig;
  
  public AnswerFormatting(String format, JSONObject formatConfig) {
    this.format = format;
    this.formatConfig = formatConfig;
  }

  public String getFormat() {
    return format;
  }

  public JSONObject getFormatConfig() {
    return formatConfig;
  }  
}
