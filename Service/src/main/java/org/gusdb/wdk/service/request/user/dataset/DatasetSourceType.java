package org.gusdb.wdk.service.request.user.dataset;

import java.util.Arrays;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.json.JsonType.ValueType;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;

/**
 * Contains the possible ways a user can submit a dataset (for use as a dataset param),
 * along with how to parse the config JSON for the submission.
 */
public enum DatasetSourceType {

  ID_LIST("idList", "ids", ValueType.ARRAY),
  BASKET("basket", "basketName", ValueType.STRING),
  FILE("file", "temporaryFileId", ValueType.STRING),
  STRATEGY("strategy", JsonKeys.STRATEGY_ID, ValueType.NUMBER),
  URL("url", "url", ValueType.STRING);

  private final String _typeIndicator;
  private final String _configJsonKey;
  private final ValueType _configValueType;

  DatasetSourceType(String typeIndicator, String configJsonKey, ValueType configValueType) {
    _typeIndicator = typeIndicator;
    _configJsonKey = configJsonKey;
    _configValueType = configValueType;
  }

  public String getTypeIndicator() {
    return _typeIndicator;
  }

  public String getConfigJsonKey() {
    return _configJsonKey;
  }

  public ValueType getConfigType() {
    return _configValueType;
  }

  public static DatasetSourceType getFromTypeIndicator(String typeIndicator) throws RequestMisformatException {
    return Arrays.stream(values())
      .filter(val -> val._typeIndicator.equals(typeIndicator))
      .findFirst()
      .orElseThrow(() -> new RequestMisformatException(
          "Invalid source type.  Only [" + FormatUtil.join(values(), ", ") + "] allowed."));
  }
}
