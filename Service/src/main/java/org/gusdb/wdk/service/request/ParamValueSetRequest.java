package org.gusdb.wdk.service.request;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import jakarta.ws.rs.BadRequestException;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.query.param.ParameterContainer;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

public class ParamValueSetRequest {

  public static ParamValueSetRequest parse(String body, ParameterContainer container)
      throws RequestMisformatException, DataValidationException {
    try {
      JSONObject jsonBody = new JSONObject(body);
      return new ParamValueSetRequest(
          parseContextParamValues(jsonBody, container),
          parseChangedParam(jsonBody, container));
    }
    catch (JSONException e) {
      throw new BadRequestException(e);
    }
  }

  private static Optional<Entry<String, String>> parseChangedParam(
      JSONObject jsonBody, ParameterContainer container)
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
    checkParam(changedParam.getKey(), container);
    return Optional.of(changedParam);
  }

  private static Map<String, String> parseContextParamValues(
      JSONObject bodyJson, ParameterContainer container)
          throws RequestMisformatException, DataValidationException {
    try {
      JSONObject contextJson = bodyJson.getJSONObject("contextParamValues");
      Map<String,String> contextParamValues = JsonUtil.parseProperties(contextJson);
      // param name validation (values will be validated later)
      for (String paramName : contextParamValues.keySet()) {
        checkParam(paramName, container);
      }
      return contextParamValues;
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage(), e);
    }
  }

  private static void checkParam(String paramName, ParameterContainer container) throws DataValidationException {
    if (!container.getParamMap().containsKey(paramName)) {
      throw new DataValidationException("Parameter '" + paramName +
          "' is not in container '" + container.getFullName() + "'.");
    }
  }

  private final Map<String, String> _contextParamValues;
  private final Optional<Entry<String, String>> _changedParam;

  private ParamValueSetRequest(
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
