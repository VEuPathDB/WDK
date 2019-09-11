package org.gusdb.wdk.service.formatter;

import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.wdk.core.api.JsonKeys;
import org.json.JSONArray;
import org.json.JSONObject;

public class ValidationFormatter {

  public static JSONObject getValidationBundleJson(ValidationBundle validationBundle) {
    boolean isValid = validationBundle.getStatus().isValid();
    JSONObject json = new JSONObject()
        .put(JsonKeys.LEVEL, validationBundle.getLevel().name())
        .put(JsonKeys.IS_VALID, isValid);
    if (!isValid) {
      json.put(JsonKeys.ERRORS,
        new JSONObject()
          .put(JsonKeys.GENERAL, validationBundle.getUnkeyedErrors())
          .put(JsonKeys.BY_KEY, validationBundle.getKeyedErrors()));
    }
    return json;
  }

  public static JSONObject getValidationBundleJson(String singleGeneralErrorMessage) {
    return new JSONObject()
      .put(JsonKeys.LEVEL, "UNSPECIFIED")
      .put(JsonKeys.IS_VALID, false)
      .put(JsonKeys.ERRORS, new JSONObject()
        .put(JsonKeys.GENERAL, new JSONArray().put(singleGeneralErrorMessage))
        .put(JsonKeys.BY_KEY, new JSONObject()));
  }
}
