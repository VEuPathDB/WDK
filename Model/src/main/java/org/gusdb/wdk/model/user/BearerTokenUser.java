package org.gusdb.wdk.model.user;

import org.apache.log4j.Logger;
import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.oauth2.shared.token.IdTokenFields;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.session.WdkOAuthClientWrapper;
import org.json.JSONObject;

public class BearerTokenUser extends User {

  private static final Logger LOG = Logger.getLogger(BearerTokenUser.class);

  private final WdkOAuthClientWrapper _client;
  private final ValidatedToken _token;
  private boolean _userInfoFetched = false;

  public BearerTokenUser(WdkModel wdkModel, WdkOAuthClientWrapper client, ValidatedToken token) {
    // parent constructor sets immutable fields provided on the token
    super(wdkModel,
        Long.valueOf(token.getUserId()),
        token.isGuest(),
        token.getTokenContents().get(IdTokenFields.signature.name(), String.class),
        token.getTokenContents().get(IdTokenFields.preferred_username.name(), String.class));
    _client = client;
    _token = token;
  }

  @Override
  protected void fetchUserInfo() {
    // return if already fetched
    if (_userInfoFetched) return;

    LOG.info("User data fetch requested for user " + getUserId() + "; querying OAuth server.");
    // fetch user info from OAuth server where it is stored (but only on demand, and only once for this object's lifetime)
    JSONObject userInfo = _client.getUserData(_token);

    // set email (standard property but mutable so set on user profile and not token
    setEmail(userInfo.getString(IdTokenFields.email.name()));

    // set other user properties found only on user profile object
    for (WdkUserProperty userProp : User.USER_PROPERTIES.values()) {
      userProp.setValue(this, userInfo.optString(userProp.getName(), null));
    }

    LOG.info("User data successfully fetched for " + getDisplayName() + " / " + getOrganization());
    _userInfoFetched = true;
  }

}

