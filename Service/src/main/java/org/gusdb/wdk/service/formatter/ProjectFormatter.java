package org.gusdb.wdk.service.formatter;

import java.util.Optional;

import org.gusdb.oauth2.client.veupathdb.UserProperty;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.user.User;
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
 *     { &lt;propName>: String }
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
    for (UserProperty prop : User.USER_PROPERTIES.values()) {
      userProfileProps.put(new JSONObject()
          .put(JsonKeys.NAME, prop.getName())
          .put(JsonKeys.IS_PUBLIC, prop.isPublic())
          .put(JsonKeys.DISPLAY_NAME, prop.getDisplayName())
          .put(JsonKeys.HELP, prop.getHelpText())
          .put(JsonKeys.SUGGEST_TEXT, prop.getSuggest())
          .put(JsonKeys.INPUT_TYPE, prop.getInputType().name().toLowerCase())
          .put(JsonKeys.IS_REQUIRED, prop.isRequired()));
    }

    return new JSONObject()
      .put(JsonKeys.DESCRIPTION, Optional.ofNullable(wdkModel.getIntroduction()).orElse(WELCOME_MESSAGE))
      .put(JsonKeys.DISPLAY_NAME, wdkModel.getDisplayName())
      .put(JsonKeys.PROJECT_ID, wdkModel.getProjectId())
      .put(JsonKeys.BUILD_NUMBER, wdkModel.getBuildNumber())
      .put(JsonKeys.RELEASE_DATE, wdkModel.getReleaseDate())
      .put(JsonKeys.STARTUP_TIME, wdkModel.getStartupTime())
      .put(JsonKeys.CHANGE_PASSWORD_URL, config.getChangePasswordUrl())
      .put(JsonKeys.USER_DATASET_STORE_STATUS, new JSONObject()
        .put(JsonKeys.IS_AVAILABLE, wdkModel.getUserDatasetStore().isPresent()))
      .put(JsonKeys.CATEGORIES_ONTOLOGY_NAME, wdkModel.getCategoriesOntologyName())
      .put(JsonKeys.AUTHENTICATION, authConfig)
      .put(JsonKeys.USER_PROFILE_PROPERTIES, userProfileProps);
  }
}
