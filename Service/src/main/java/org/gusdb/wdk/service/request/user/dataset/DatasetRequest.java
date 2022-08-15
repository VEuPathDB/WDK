package org.gusdb.wdk.service.request.user.dataset;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONObject;

public class DatasetRequest {

  private final DatasetSourceType _sourceType;
  private final JsonType _configValue;
  private final Optional<String> _displayName;
  private final Map<String,JsonType> _additionalConfig;

  public DatasetRequest(JSONObject input) throws RequestMisformatException {
    _sourceType = DatasetSourceType.getFromTypeIndicator(input.getString(JsonKeys.SOURCE_TYPE));
    JSONObject sourceContent = input.getJSONObject(JsonKeys.SOURCE_CONTENT);
    _configValue = new JsonType(sourceContent.get(_sourceType.getConfigJsonKey()));
    if (!_configValue.getType().equals(_sourceType.getConfigType())) {
      throw new RequestMisformatException("Value of '" +
          _sourceType.getConfigJsonKey() + "' must be a " + _sourceType.getConfigType());
    }
    _additionalConfig = Functions.getMapFromKeys(
        JsonUtil.getKeys(sourceContent).stream()
          .filter(key -> !key.equals(_sourceType.getConfigJsonKey()))
          .collect(Collectors.toSet()),
        key -> new JsonType(sourceContent.get(key)));
    _displayName = Optional.ofNullable(JsonUtil.getStringOrDefault(input, JsonKeys.DISPLAY_NAME, null));
  }

  public DatasetSourceType getSourceType() { return _sourceType; }
  public JsonType getConfigValue() { return _configValue; }
  public Optional<String> getDisplayName() { return _displayName; }
  public Map<String,JsonType> getAdditionalConfig() { return _additionalConfig; }

}