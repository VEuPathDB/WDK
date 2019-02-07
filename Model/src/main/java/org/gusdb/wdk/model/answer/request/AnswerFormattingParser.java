package org.gusdb.wdk.model.answer.request;

import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.report.reporter.DefaultJsonReporter;
import org.json.JSONException;
import org.json.JSONObject;

public interface AnswerFormattingParser extends FunctionWithException<JSONObject, AnswerFormatting> {

  String FORMATTING_KEY = "formatting";
  String FORMAT_KEY = "format";
  String FORMAT_CONFIG_KEY = "formatConfig";

  @Override
  AnswerFormatting apply(JSONObject input) throws JSONException;

  AnswerFormatting createFromTopLevelObject(JSONObject requestBody) throws JSONException;

  AnswerFormattingParser SPECIFIED_REPORTER_PARSER = new AnswerFormattingParser() {
    @Override
    public AnswerFormatting apply(JSONObject input) throws JSONException {
      JSONObject formatting = input.getJSONObject(FORMATTING_KEY);
      return new AnswerFormatting(
          formatting.getString(FORMAT_KEY),
          JsonUtil.getJsonObjectOrDefault(formatting, FORMAT_CONFIG_KEY, null));
    }
    @Override
    public AnswerFormatting createFromTopLevelObject(JSONObject requestBody) throws JSONException {
      // wrap input in an object wrapper, making the requestBody the value of the "formatting" property,
      // then pass to regular parser
      return apply(new JSONObject().put(FORMATTING_KEY, requestBody));
    }
  };

  AnswerFormattingParser DEFAULT_REPORTER_PARSER = new AnswerFormattingParser() {
    @Override
    public AnswerFormatting apply(JSONObject input) throws JSONException {
      return new AnswerFormatting(
          DefaultJsonReporter.RESERVED_NAME,
          JsonUtil.getJsonObjectOrDefault(input, FORMAT_CONFIG_KEY, null));
    }
    @Override
    public AnswerFormatting createFromTopLevelObject(JSONObject requestBody) throws JSONException {
      return new AnswerFormatting(
          DefaultJsonReporter.RESERVED_NAME, requestBody);
    }
  };
}
