package org.gusdb.wdk.model.user.dataset.json;

import org.json.JSONObject;

public class JsonUserDatasetShare implements org.gusdb.wdk.model.user.dataset.UserDatasetShare {

  private static final String USER_ID = "userId";
  private static final String TIME_SHARED = "timeShared";

  private Long userId;
  private Long timeShared; // milliseconds since epoch

  public JsonUserDatasetShare (Long userId, Long timeShared) {
    this.userId = userId;
    this.timeShared = timeShared;
  }

  @Override
  public Long getUserId() {
    return userId;
  }

  @Override
  public Long getTimeShared() {
    return timeShared;
  }

  public JSONObject getJsonObject() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(USER_ID, userId);

    // TODO: fix this time formatting
    jsonObject.put(TIME_SHARED, timeShared.toString());

    return jsonObject;
  }

}
