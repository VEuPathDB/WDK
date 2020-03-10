package org.gusdb.wdk.service.formatter;

import org.gusdb.fgputil.accountdb.UserPropertyName;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.config.ModelConfig;
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
      .put(JsonKeys.AUTHENTICATION_METHOD, config.getAuthenticationMethodEnum().name())
      .put(JsonKeys.OAUTH_URL, config.getOauthUrl())
      .put(JsonKeys.OAUTH_CLIENT_URL, serviceEndpoint)
      .put(JsonKeys.OAUTH_CLIENT_ID, config.getOauthClientId());

    // create profile property config sub-array
    JSONArray userProfileProps = new JSONArray();
    for (UserPropertyName prop : wdkModel.getModelConfig().getAccountDB().getUserPropertyNames()) {
      userProfileProps.put(new JSONObject()
          .put(JsonKeys.NAME, prop.getName())
          .put(JsonKeys.DISPLAY_NAME, prop.getDisplayName())
          .put(JsonKeys.IS_REQUIRED, prop.isRequired())
          .put(JsonKeys.IS_PUBLIC, prop.isPublic()));
    }

    return new JSONObject()
      .put(JsonKeys.DESCRIPTION, wdkModel.getIntroduction() == null ?
          WELCOME_MESSAGE : wdkModel.getIntroduction())
      .put(JsonKeys.DISPLAY_NAME, wdkModel.getDisplayName())
      .put(JsonKeys.PROJECT_ID, wdkModel.getProjectId())
      .put(JsonKeys.BUILD_NUMBER, wdkModel.getBuildNumber())
      .put(JsonKeys.RELEASE_DATE, wdkModel.getReleaseDate())
      .put(JsonKeys.STARTUP_TIME, wdkModel.getStartupTime())
      .put(JsonKeys.CHANGE_PASSWORD_URL, config.getChangePasswordUrl())
      .put(JsonKeys.USER_DATASET_STORE_STATUS, wdkModel.getUserDatasetStore().toJson())
      .put(JsonKeys.CATEGORIES_ONTOLOGY_NAME, wdkModel.getCategoriesOntologyName())
      .put(JsonKeys.AUTHENTICATION, authConfig)
      .put(JsonKeys.USER_PROFILE_PROPERTIES, userProfileProps);
  }
}
