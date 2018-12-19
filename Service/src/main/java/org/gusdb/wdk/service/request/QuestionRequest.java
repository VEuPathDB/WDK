package org.gusdb.wdk.service.request;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.ws.rs.BadRequestException;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionRequest {

  public static QuestionRequest parse(String body, Question question)
      throws RequestMisformatException, DataValidationException {
    try {
      JSONObject jsonBody = new JSONObject(body);
      return new QuestionRequest(
          parseContextParamValues(jsonBody, question),
          parseChangedParam(jsonBody, question));
    }
    catch (JSONException e) {
      throw new BadRequestException(e);
    }
  }

  private static Optional<Entry<String, String>> parseChangedParam(
      JSONObject jsonBody, Question question)
          throws RequestMisformatException, DataValidationException {
    if (!jsonBody.has("changedParam")) {
      return Optional.empty();
    }
    JSONObject changedParamJson = jsonBody.getJSONObject("changedParam");
    if (!changedParamJson.has("name") || !changedParamJson.has("value")) {
      throw new RequestMisformatException(
          "changedParam property must be a JSON object with name and value properties.");
    }
    Entry<String,String> changedParam = new TwoTuple<String,String>(
        changedParamJson.getString("name"), changedParamJson.getString("value"));
    checkParam(changedParam.getKey(), question);
    return Optional.of(changedParam);
  }

  private static Map<String, String> parseContextParamValues(
      JSONObject bodyJson, Question question)
          throws RequestMisformatException, DataValidationException {
    try {
      JSONObject contextJson = bodyJson.getJSONObject("contextParamValues");
      Map<String,String> contextParamValues = JsonUtil.parseProperties(contextJson);
      // param name validation (values will be validated later)
      for (String paramName : contextParamValues.keySet()) {
        checkParam(paramName, question);
      }
      return contextParamValues;
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage(), e);
    }
  }

  private static void checkParam(String paramName, Question question) throws DataValidationException {
    if (!question.getParamMap().containsKey(paramName)) {
      throw new DataValidationException("Parameter '" + paramName +
          "' is not in question '" + question.getFullName() + "'.");
    }
  }

  private final Map<String, String> _contextParamValues;
  private final Optional<Entry<String, String>> _changedParam;

  private QuestionRequest(
      Map<String,String> contextParamValues,
      Optional<Entry<String,String>> changedParam) {
    _contextParamValues = contextParamValues;
    _changedParam = changedParam;
  }

  public Map<String, String> getContextParamValues() {
    return _contextParamValues;
  }

  public Optional<Entry<String,String>> getChangedParam() {
    return _changedParam;
  }
}
