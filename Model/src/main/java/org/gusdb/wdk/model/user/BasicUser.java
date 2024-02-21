package org.gusdb.wdk.model.user;

import org.gusdb.oauth2.client.veupathdb.UserProperty;
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

  public BasicUser(User user) {
    super(user.getUserId(), user.isGuest(), user.getSignature(), user.getStableId());
    _wdkModel = user.getWdkModel();
    setEmail(user.getEmail());
    for (UserProperty prop : USER_PROPERTIES.values()) {
      prop.setValue(this, prop.getValue(user));
    }
  }

  @Override
  public WdkModel getWdkModel() {
    return _wdkModel;
  }


}
