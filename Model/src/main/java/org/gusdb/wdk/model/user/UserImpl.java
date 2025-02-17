package org.gusdb.wdk.model.user;

import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.oauth2.client.OAuthConfig;
import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.wdk.model.WdkModel;

public class UserImpl extends org.gusdb.oauth2.client.veupathdb.UserImpl implements User {

  private final WdkModel _wdkModel;

  public UserImpl(WdkModel wdkModel, OAuthClient client, OAuthConfig config, ValidatedToken token) {
    super(client, config.getOauthUrl(), token);
    _wdkModel = wdkModel;
  }

  @Override
  public WdkModel getWdkModel() {
    return _wdkModel;
  }

}
