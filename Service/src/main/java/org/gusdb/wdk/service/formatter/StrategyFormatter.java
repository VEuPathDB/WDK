package org.gusdb.wdk.service.formatter;

import java.util.List;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Strategy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StrategyFormatter {
  public static JSONArray getStrategiesJson(List<Strategy> strategies) throws WdkModelException {
	JSONArray strategiesJson = new JSONArray();
    try {
      for(Strategy strategy : strategies) {	
        strategiesJson.put(getStrategyJson(strategy));
      }
    }
    catch(JSONException e) {
      throw new WdkModelException("Unable to convert Strategies to service JSON", e);
    }
    return strategiesJson;
  }

  public static JSONObject getStrategyJson(Strategy strategy) throws WdkModelException, JSONException {
	return new JSONObject() 
        .put(Keys.STRATEGY_ID, strategy.getStrategyId())
        .put(Keys.DESCRIPTION, strategy.getDescription())
        .put(Keys.NAME, strategy.getName())
        .put(Keys.AUTHOR, strategy.getUser().getDisplayName())
        .put(Keys.LATEST_STEP_ID, strategy.getLatestStepId())
        .put(Keys.RECORD_CLASS_NAME_PLURAL, strategy.getLatestStep().getQuestion().getRecordClass().getDisplayNamePlural())
        .put(Keys.SIGNATURE, strategy.getSignature())
        .put(Keys.LAST_MODIFIED, strategy.getLastModifiedTime())
        .put(Keys.IS_PUBLIC, strategy.getIsPublic())
        .put(Keys.IS_SAVED, strategy.getIsSaved())
        .put(Keys.IS_VALID, strategy.isValid())
        .put(Keys.IS_DELETED, strategy.isDeleted())
        .put(Keys.IS_PUBLIC, strategy.getIsPublic())
        .put(Keys.ORGANIZATION, strategy.getUser().getProfileProperties().get("organization"));
  }
}
