package org.gusdb.wdk.model.user.dataset.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.gusdb.wdk.model.WdkModelException;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUserDatasetShare implements org.gusdb.wdk.model.user.dataset.UserDatasetShare {
  
  private static final String USER_ID = "userId";
  private static final String TIME_SHARED = "timeShared";
  
  private JSONObject jsonObject;
  private Integer userId;
  private Date timeShared;
  
  public JsonUserDatasetShare (JSONObject jsonObject) throws WdkModelException {
    this.jsonObject = jsonObject;
    try {
      userId = jsonObject.getInt(USER_ID);
      timeShared = new SimpleDateFormat().parse(jsonObject.getString(TIME_SHARED));
    } catch (JSONException | ParseException e) {
      throw new WdkModelException(e);
    }
  }
  
  public JsonUserDatasetShare(Integer userId) {
    this.userId = userId;
    this.timeShared = new Date();
    this.jsonObject = new JSONObject();
    jsonObject.put(USER_ID, userId);
    
    // TODO: fix this time formatting
    jsonObject.put(TIME_SHARED, timeShared.toString());
  }
  
  @Override
  public Integer getUserId() {
    return userId;
  }

  @Override
  public Date getTimeShared() {
    return timeShared;
  }
  
  public JSONObject getJsonObject() {
    return jsonObject;
  }

}
