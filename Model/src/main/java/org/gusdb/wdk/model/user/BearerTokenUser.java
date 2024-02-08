package org.gusdb.wdk.model.user;

import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.session.WdkOAuthClientWrapper;

public class BearerTokenUser extends org.gusdb.oauth2.client.veupathdb.BearerTokenUser implements User {

  private final WdkModel _wdkModel;

  public BearerTokenUser(WdkModel wdkModel, WdkOAuthClientWrapper client, ValidatedToken token) {
    super(client.getOAuthClient(), client.getOAuthConfig().getOauthUrl(), token);
    _wdkModel = wdkModel;
  }

  @Override
  public WdkModel getWdkModel() {
    return _wdkModel;
  }
}
