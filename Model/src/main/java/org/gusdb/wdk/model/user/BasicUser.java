package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.WdkModel;
import org.json.JSONObject;

/**
 * Represents a VEupathDB user
 *
 * @author rdoherty
 */
public class BasicUser extends org.gusdb.oauth2.client.veupathdb.BasicUser implements User {

  private final WdkModel _wdkModel;

  public BasicUser(WdkModel wdkModel, long userId, boolean isGuest, String signature, String stableId) {
    super(userId, isGuest, signature, stableId);
    _wdkModel = wdkModel;
  }

  public BasicUser(WdkModel wdkModel, JSONObject json) {
    super(json);
    _wdkModel = wdkModel;
  }

  @Override
  public WdkModel getWdkModel() {
    return _wdkModel;
  }


}
