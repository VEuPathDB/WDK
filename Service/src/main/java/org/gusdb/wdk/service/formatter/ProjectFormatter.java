package org.gusdb.wdk.service.formatter;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.service.formatter.Keys;
import org.json.JSONObject;

public class ProjectFormatter {

  private static final String WELCOME_MESSAGE = "Welcome to the WDK 3.0 Web Service";
  
  public static JSONObject getWdkProjectInfo(WdkModel wdkModel) {
    ModelConfig config = wdkModel.getModelConfig();

    // create authentication config sub-object
    JSONObject authConfig = new JSONObject()
      .put(Keys.AUTHENTICATION_METHOD, config.getAuthenticationMethodEnum().name())
      .put(Keys.OAUTH_URL, config.getOauthUrl())
      .put(Keys.OAUTH_CLIENT_ID, config.getOauthClientId());
    
    return new JSONObject()
      .put(Keys.DESCRIPTION, wdkModel.getIntroduction() == null ?
        WELCOME_MESSAGE : wdkModel.getIntroduction())
      .put(Keys.DISPLAY_NAME, wdkModel.getDisplayName())
      .put(Keys.PROJECT_ID, wdkModel.getProjectId())
      .put(Keys.BUILD_NUMBER, wdkModel.getBuildNumber())
      .put(Keys.RELEASE_DATE, wdkModel.getReleaseDate())
      .put(Keys.WEBAPP_URL, config.getWebAppUrl())
      .put(Keys.WEBSERVICE_URL, config.getWebServiceUrl())
      .put(Keys.ASSETS_URL, config.getAssetsUrl())
      .put(Keys.AUTHENTICATION, authConfig);
  }
}
