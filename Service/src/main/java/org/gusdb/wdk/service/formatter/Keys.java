package org.gusdb.wdk.service.formatter;



/**
 * A collection of common keys; this is an attempt to standardize the names
 * of JSON object keys received from and returned to the client.
 */
public class Keys {

  // container keys
  public static final String META = "meta";
  public static final String RECORDS = "records";

  // identifying keys
  public static final String ID = "id";
  public static final String NAME = "name";
  public static final String URL_SEGMENT = "urlSegment";
  public static final String PRIMARY_KEY_REFS = "primaryKeyColumnRefs";

  // descriptive keys
  public static final String TYPE = "type";
  public static final String LENGTH = "length";
  public static final String VALUE = "value";
  public static final String PROPERTIES = "properties";
  public static final String TOTAL_COUNT = "totalCount";
  public static final String RESPONSE_COUNT = "responseCount";
  public static final String RECORD_CLASS = "recordClass";
  public static final String QUESTION_NAME = "questionName";

  // UI-related keys
  public static final String DISPLAY_NAME = "displayName";
  public static final String DISPLAY_NAME_PLURAL = "displayNamePlural";
  public static final String SHORT_DISPLAY_NAME = "shortDisplayName";
  public static final String SHORT_DISPLAY_NAME_PLURAL = "shortDisplayNamePlural";
  public static final String URL = "url";
  public static final String DISPLAY_TEXT = "displayText";
  public static final String HELP = "help";
  public static final String DESCRIPTION = "description";
  public static final String OVERVIEW = "overview";
  public static final String ALIGN = "align";
  public static final String TRUNCATE_TO = "truncateTo";
  public static final String IS_READ_ONLY = "isReadOnly";
  public static final String IS_SORTABLE = "isSortable";
  public static final String IS_REMOVABLE = "isRemovable";

  // scoping-related keys
  public static final String CATEGORIES = "categories";
  public static final String CATEGORY = "category";
  public static final String GROUP = "group";
  // TODO: see if isVisible and isDisplayable should be merged into one
  public static final String IS_VISIBLE = "isVisible";
  public static final String IS_DISPLAYABLE = "isDisplayable";
  public static final String IS_IN_REPORT = "isInReport";
  public static final String NEW_BUILD = "newBuild";
  public static final String REVISE_BUILD = "reviseBuild";

  // keys for what and how data is being referenced
  public static final String PARAMETERS = "parameters";
  public static final String ATTRIBUTES = "attributes";
  public static final String DYNAMIC_ATTRIBUTES = "dynamicAttributes";
  public static final String DEFAULT_ATTRIBUTES = "defaultAttributes";
  public static final String TABLES = "tables";
  public static final String FORMATS = "formats";
  public static final String SORTING = "sorting";
  public static final String DIRECTION = "direction";

  // question plugin keys
  public static final String DEFAULT_SUMMARY_VIEW = "defaultSummaryView";
  public static final String SUMMARY_VIEW_PLUGINS = "summaryViewPlugins";
  public static final String STEP_ANALYSIS_PLUGINS = "stepAnalysisPlugins";

  // param-related keys
  public static final String DEFAULT_VALUE = "defaultValue";
  public static final String VOCABULARY = "vocabulary";

  // specific param-related keys
  public static final String COUNT_ONLY_LEAVES = "countOnlyLeaves";
  public static final String MAX_SELECTED_COUNT = "maxSelectedCount";
  public static final String MIN_SELECTED_COUNT = "minSelectedCount";
  public static final String IS_MULTIPICK = "multiPick";
  public static final String DISPLAY_TYPE = "displayType";
  public static final String PARSERS = "parsers";

  // step- and strategy-specific keys
  public static final String CUSTOM_NAME = "customName";
  public static final String BASE_CUSTOM_NAME = "baseCustomName";
  public static final String COLLAPSED_NAME = "collapsedName";
  public static final String OWNER_ID = "ownerId";
  public static final String STRATEGY_ID = "strategyId";
  public static final String ESTIMATED_SIZE = "estimatedSize";
  public static final String HAS_COMPLETE_STEP_ANALYSES = "hasCompleteStepAnalyses";
  public static final String ANSWER_SPEC = "answerSpec";
  public static final String LEGACY_FILTER_NAME = "legacyFilterName";
  public static final String FILTERS = "filters";
  public static final String VIEW_FILTERS = "viewFilters";
  public static final String WDK_WEIGHT = "wdk_weight";

  // site-level keys
  public static final String PROJECT_ID = "projectId";
  public static final String BUILD_NUMBER = "buildNumber";
  public static final String RELEASE_DATE = "releaseDate";
  public static final String WEBAPP_URL = "webAppUrl";
  public static final String WEBSERVICE_URL = "webServiceUrl";
  public static final String ASSETS_URL = "assetsUrl";

  // authentication keys
  public static final String AUTHENTICATION = "authentication";
  public static final String AUTHENTICATION_METHOD = "method";
  public static final String OAUTH_URL = "oauthUrl";
  public static final String OAUTH_CLIENT_ID = "oauthClientId";

  // user-specific keys
  public static final String FIRST_NAME = "firstName";
  public static final String MIDDLE_NAME = "middleName";
  public static final String LAST_NAME = "lastName";
  public static final String EMAIL = "email";
  public static final String ORGANIZATION = "organization";
  public static final String PREFERENCES = "preferences";

}
