package org.gusdb.wdk.service.formatter;

import org.gusdb.fgputil.accountdb.UserPropertyName;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.service.formatter.Keys;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Formats WDK project information.  JSON will have the following form:
 * {
 *   description: String,
 *   displayName; String,
 *   projectId: String,
 *   buildNumber: Number,
 *   releaseDate: String,
 *   startupTime: Number,
 *   webAppUrl: String (relative URL),
 *   webServiceUrl: String (relative URL),
 *   assetsUrl: String (relative URL),
 *   categoriesOntologyName: String,
 *   authentication: {
 *     method: String (see AuthenticationMethod enum),
 *     oauthUrl: String (absolute URL),
 *     oauthClientId: String
 *   },
 *   profileProperties: [
 *     { <propName>: String }
 *   ]
 * }
 */
public class ProjectFormatter {

  public static final String WELCOME_MESSAGE = "Welcome to the WDK 3.0 Web Service";

  public static JSONObject getWdkProjectInfo(WdkModel wdkModel, String serviceEndpoint) {

    ModelConfig config = wdkModel.getModelConfig();

    // create authentication config sub-object
    JSONObject authConfig = new JSONObject()
      .put(Keys.AUTHENTICATION_METHOD, config.getAuthenticationMethodEnum().name())
      .put(Keys.OAUTH_URL, config.getOauthUrl())
      .put(Keys.OAUTH_CLIENT_URL, serviceEndpoint)
      .put(Keys.OAUTH_CLIENT_ID, config.getOauthClientId());

    // create profile property config sub-array
    JSONArray userProfileProps = new JSONArray();
    for (UserPropertyName prop : wdkModel.getModelConfig().getAccountDB().getUserPropertyNames()) {
      userProfileProps.put(new JSONObject()
          .put(Keys.NAME, prop.getName())
          .put(Keys.DISPLAY_NAME, prop.getDisplayName())
          .put(Keys.IS_REQUIRED, prop.isRequired())
          .put(Keys.IS_PUBLIC, prop.isPublic()));
    }

    return new JSONObject()
      .put(Keys.DESCRIPTION, wdkModel.getIntroduction() == null ?
          WELCOME_MESSAGE : wdkModel.getIntroduction())
      .put(Keys.DISPLAY_NAME, wdkModel.getDisplayName())
      .put(Keys.PROJECT_ID, wdkModel.getProjectId())
      .put(Keys.BUILD_NUMBER, wdkModel.getBuildNumber())
      .put(Keys.RELEASE_DATE, wdkModel.getReleaseDate())
      .put(Keys.STARTUP_TIME, wdkModel.getStartupTime())
      .put(Keys.CHANGE_PASSWORD_URL, config.getChangePasswordUrl())
      .put(Keys.USER_DATASETS_ENABLED, config.getUserDatasetStoreConfig() != null)
      .put(Keys.USER_DATASET_STORE_STATUS, wdkModel.getUserDatasetStoreStatus())
      .put(Keys.CATEGORIES_ONTOLOGY_NAME, wdkModel.getCategoriesOntologyName())
      .put(Keys.AUTHENTICATION, authConfig)
      .put(Keys.USER_PROFILE_PROPERTIES, userProfileProps);
  }
}
