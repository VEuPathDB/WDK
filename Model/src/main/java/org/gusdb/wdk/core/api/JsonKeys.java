package org.gusdb.wdk.core.api;

/**
 * A collection of common keys; this is an attempt to standardize the names
 * of JSON object keys received from and returned to the client.
 */
public class JsonKeys {

  // response status keys
  public static final String SUCCESS = "success";
  public static final String ERRORS = "errors";
  public static final String MESSAGE = "message";
  public static final String TABLE_ERRORS = "tableErrors";
  public static final String NUMBER_PROCESSED = "numberProcessed";

  // container keys
  public static final String META = "meta";
  public static final String RECORDS = "records";
  public static final String RECORD_TYPES = "recordTypes";
  public static final String SEARCHES = "searches";
  public static final String REPORTS = "reports";

  // identifying keys
  public static final String ID = "id";
  public static final String NAME = "name";
  public static final String PRIMARY_KEY = "primaryKey";
  public static final String URL_SEGMENT = "urlSegment";
  public static final String RECORD_ID_ATTRIBUTE_NAME = "recordIdAttributeName";
  public static final String PRIMARY_KEY_REFS = "primaryKeyColumnRefs";
  public static final String SOURCE_SIGNATURE = "sourceSignature";

  // descriptive keys
  public static final String TYPE = "type";
  public static final String LENGTH = "length";
  public static final String VALUE = "value";
  public static final String PROPERTIES = "properties";
  public static final String TOTAL_COUNT = "totalCount";
  public static final String RESPONSE_COUNT = "responseCount";
  public static final String RECORD_CLASS_NAME = "recordClassName";
  public static final String SEARCH_NAME = "searchName";
  public static final String PARAMETER_NAME = "parameterName";

  // UI-related keys
  public static final String DISPLAY_NAME = "displayName";
  public static final String DISPLAY_NAME_PLURAL = "displayNamePlural";
  public static final String SHORT_DISPLAY_NAME = "shortDisplayName";
  public static final String SHORT_DISPLAY_NAME_PLURAL = "shortDisplayNamePlural";
  public static final String URL = "url";
  public static final String ICON_NAME = "iconName";
  public static final String DISPLAY_TEXT = "displayText";
  public static final String HELP = "help";
  public static final String DESCRIPTION = "description";
  public static final String SUMMARY = "summary";
  public static final String ALIGN = "align";
  public static final String TRUNCATE_TO = "truncateTo";
  public static final String IS_READ_ONLY = "isReadOnly";
  public static final String IS_SORTABLE = "isSortable";
  public static final String IS_REMOVABLE = "isRemovable";
  public static final String IS_REQUIRED = "isRequired";
  public static final String IS_PUBLIC = "isPublic";
  public static final String USE_BASKET = "useBasket";
  public static final String SCOPES = "scopes";
  public static final String CLIENT_SORT_SPEC = "clientSortSpec";


  // scoping-related keys
  public static final String CATEGORIES = "categories";
  public static final String CATEGORY = "category";
  public static final String GROUPS = "groups";
  public static final String GROUP = "group";
  // TODO: see if isVisible and isDisplayable should be merged into one
  public static final String IS_VISIBLE = "isVisible";
  public static final String IS_DISPLAYABLE = "isDisplayable";
  public static final String IS_IN_REPORT = "isInReport";
  public static final String NEW_BUILD = "newBuild";
  public static final String REVISE_BUILD = "reviseBuild";

  // patch command keys
  public static final String ADD = "add";
  public static final String REMOVE = "remove";
  public static final String DELETE = "delete";
  public static final String UNDELETE = "undelete";

  // keys for what and how data is being referenced
  public static final String TREE = "tree";
  public static final String CHILDREN = "children";
  public static final String PARAMETERS = "parameters";
  public static final String ATTRIBUTES = "attributes";
  public static final String DYNAMIC_ATTRIBUTES = "dynamicAttributes";
  public static final String DEFAULT_ATTRIBUTES = "defaultAttributes";
  public static final String TABLES = "tables";
  public static final String FORMATS = "formats";
  public static final String SORTING = "sorting";
  public static final String DIRECTION = "direction";

  // question specific keys
  public static final String OUTPUT_RECORD_CLASS_NAME = "outputRecordClassName";
  public static final String ALLOWED_PRIMARY_INPUT_RECORD_CLASS_NAMES = "allowedPrimaryInputRecordClassNames";
  public static final String ALLOWED_SECONDARY_INPUT_RECORD_CLASS_NAMES = "allowedSecondaryInputRecordClassNames";

  // record class specific keys
  public static final String HAS_ALL_RECORDS_QUERY = "hasAllRecordsQuery";

  // question plugin keys
  public static final String DEFAULT_SUMMARY_VIEW = "defaultSummaryView";
  public static final String SUMMARY_VIEW_PLUGINS = "summaryViewPlugins";
  public static final String STEP_ANALYSIS_PLUGINS = "stepAnalysisPlugins";

  // param-related keys
  public static final String STABLE_VALUE = "stableValue";
  public static final String VOCABULARY = "vocabulary";
  public static final String DEPENDENT_PARAMS = "dependentParams";

  // param types
  public static final String STRING_PARAM_TYPE = "string";
  public static final String DATE_PARAM_TYPE = "date";
  public static final String DATE_RANGE_PARAM_TYPE = "date-range";
  public static final String NUMBER_PARAM_TYPE = "number";
  public static final String NUMBER_RANGE_PARAM_TYPE = "number-range";
  public static final String VOCAB_PARAM_TYPE = "vocabulary";
  public static final String STEP_PARAM_TYPE = "input-step";
  public static final String DATASET_PARAM_TYPE = "input-dataset";
  public static final String FILTER_PARAM_TYPE = "filter";
  public static final String TIMESTAMP_PARAM_TYPE = "timestamp";

  // specific param-related keys
  public static final String COUNT_ONLY_LEAVES = "countOnlyLeaves";
  public static final String MAX_SELECTED_COUNT = "maxSelectedCount";
  public static final String MIN_SELECTED_COUNT = "minSelectedCount";
  public static final String IS_MULTIPICK = "multiPick";
  public static final String DISPLAY_TYPE = "displayType";
  public static final String DEPTH_EXPANDED = "depthExpanded";
  public static final String PARSERS = "parsers";
  public static final String PARSER = "parser";

  // dataset-related keys
  public static final String DEFAULT_ID_LIST = "defaultIdList";
  public static final String SOURCE_TYPE = "sourceType";
  public static final String SOURCE_CONTENT = "sourceContent";

  // step- and strategy-specific keys
  public static final String CUSTOM_NAME = "customName";
  public static final String BASE_CUSTOM_NAME = "baseCustomName";
  public static final String IS_COLLAPSIBLE = "isCollapsible";
  public static final String COLLAPSED_NAME = "collapsedName";
  public static final String OWNER_ID = "ownerId";
  public static final String STRATEGY_ID = "strategyId";
  public static final String ESTIMATED_SIZE = "estimatedSize";
  public static final String HAS_COMPLETE_STEP_ANALYSES = "hasCompleteStepAnalyses";
  public static final String SEARCH_CONFIG = "searchConfig";
  public static final String REPORT_NAME = "reportName";
  public static final String REPORT_CONFIG = "reportConfig";
  public static final String FORMATTING = "formatting";
  public static final String FORMAT = "format";
  public static final String FORMAT_CONFIG = "formatConfig";
  public static final String LEGACY_FILTER_NAME = "legacyFilterName";
  public static final String FILTERS = "filters";
  public static final String VIEW_FILTERS = "viewFilters";
  public static final String WDK_WEIGHT = "wdkWeight";
  public static final String IS_ANSWER_SPEC_COMPLETE = "isAnswerSpecComplete";
  public static final String CREATED_TIME = "createdTime";
  public static final String LAST_RUN_TIME = "lastRunTime";

  // site-level keys
  public static final String PROJECT_ID = "projectId";
  public static final String BUILD_NUMBER = "buildNumber";
  public static final String RELEASE_DATE = "releaseDate";
  public static final String STARTUP_TIME = "startupTime";
  public static final String WEBAPP_URL = "webAppUrl";
  public static final String WEBSERVICE_URL = "webServiceUrl";
  public static final String WDKSERVICE_URL = "wdkServiceUrl";
  public static final String ASSETS_URL = "assetsUrl";
  public static final String CHANGE_PASSWORD_URL = "changePasswordUrl";
  public static final String CATEGORIES_ONTOLOGY_NAME = "categoriesOntologyName";
  public static final String USER_PROFILE_PROPERTIES = "userProfileProperties";
  public static final String USER_DATASETS_ENABLED = "userDatasetsEnabled";
  public static final String USER_DATASET_STORE_STATUS = "userDatasetStoreStatus";

  // authentication keys
  public static final String AUTHENTICATION = "authentication";
  public static final String AUTHENTICATION_METHOD = "method";
  public static final String OAUTH_URL = "oauthUrl";
  public static final String OAUTH_CLIENT_URL = "oauthClientUrl";
  public static final String OAUTH_CLIENT_ID = "oauthClientId";
  public static final String OAUTH_STATE_TOKEN = "oauthStateToken";
  public static final String REDIRECT_URL = "redirectUrl";

  // user-specific keys
  public static final String USER = "user";
  public static final String EMAIL = "email";
  public static final String IS_GUEST = "isGuest";
  public static final String PREFERENCES = "preferences";
  public static final String GLOBAL = "global";
  public static final String PROJECT = "project";

  // date and date range keys
  public static final String MIN_DATE = "minDate";
  public static final String MAX_DATE = "maxDate";
  public static final String MIN_VALUE = "min";
  public static final String MAX_VALUE = "max";
  public static final String STEP = "step";

  // favorites-specific keys
  public static final String FAVORITE_ID = "favoriteId";
  public static final String DISPLAY = "display";
  public static final String NOTE = "note";
  public static final String FAV_NUMBER_PROCESSED = "numberProcessed";

  // additional strategy-specific keys
  public static final String AUTHOR = "author";
  public static final String LATEST_STEP_ID = "latestStepId";
  public static final String ORGANIZATION = "organization";
  public static final String RECORD_CLASS_NAME_PLURAL = "recordClassNamePlural";
  public static final String SIGNATURE = "signature";
  public static final String SAVED_NAME = "savedName";
  public static final String LAST_MODIFIED = "lastModified";
  public static final String IS_SAVED = "isSaved";
  public static final String IS_DELETED = "isDeleted";
  public static final String ROOT_STEP = "root";
  public static final String PRIMARY_INPUT_STEP = "primaryInput";
  public static final String SECONDARY_INPUT_STEP = "secondaryInput";

  // validation-related keys
  public static final String VALIDATION = "validation";
  public static final String LEVEL = "level";
  public static final String IS_VALID = "isValid";
  public static final String GENERAL = "general";
  public static final String BY_KEY = "byKey";
}
