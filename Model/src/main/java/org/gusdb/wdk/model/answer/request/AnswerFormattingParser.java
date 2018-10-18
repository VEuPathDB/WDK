package org.gusdb.wdk.model.answer.request;

import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.report.reporter.DefaultJsonReporter;
import org.json.JSONException;
import org.json.JSONObject;

public interface AnswerFormattingParser extends FunctionWithException<JSONObject, AnswerFormatting> {

  public static final String FORMATTING_KEY = "formatting";
  public static final String FORMAT_KEY = "format";
  public static final String FORMAT_CONFIG_KEY = "formatConfig";

  @Override
  public AnswerFormatting apply(JSONObject input) throws JSONException;

  public AnswerFormatting createFromTopLevelObject(String requestBody) throws JSONException;

  public static final AnswerFormattingParser SPECIFIED_REPORTER_PARSER = new AnswerFormattingParser() {
    @Override
    public AnswerFormatting apply(JSONObject input) throws JSONException {
      JSONObject formatting = input.getJSONObject(FORMATTING_KEY);
      return new AnswerFormatting(
          formatting.getString(FORMAT_KEY),
          JsonUtil.getJsonObjectOrDefault(formatting, FORMAT_CONFIG_KEY, null));
    }
    @Override
    public AnswerFormatting createFromTopLevelObject(String requestBody) throws JSONException {
      // wrap input in an object wrapper, making the requestBody the value of the "formatting" property,
      // then pass to regular parser
      return apply(new JSONObject().put(FORMATTING_KEY, new JSONObject(requestBody)));
    }
  };

  public static final AnswerFormattingParser DEFAULT_REPORTER_PARSER = new AnswerFormattingParser() {
    @Override
    public AnswerFormatting apply(JSONObject input) throws JSONException {
      return new AnswerFormatting(
          DefaultJsonReporter.WDK_SERVICE_JSON_REPORTER_RESERVED_NAME,
          JsonUtil.getJsonObjectOrDefault(input, FORMAT_CONFIG_KEY, null));
    }
    @Override
    public AnswerFormatting createFromTopLevelObject(String requestBody) throws JSONException {
      return new AnswerFormatting(
          DefaultJsonReporter.WDK_SERVICE_JSON_REPORTER_RESERVED_NAME,
          (requestBody == null || requestBody.trim().isEmpty()) ? null : new JSONObject(requestBody));
    }
  };
}
