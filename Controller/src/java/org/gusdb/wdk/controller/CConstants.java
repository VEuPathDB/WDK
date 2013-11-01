package org.gusdb.wdk.controller;

public class CConstants {
    
    private CConstants() { }
    
    //key for objects in cache, used in Action/ActionForm classes and maybe jsp pages
    public static final String WDK_RESULTFACTORY_KEY = "wdkResultFactory";
    public static final String WDK_MODEL_KEY = "wdkModel";
    public static final String WDK_WIZARD_KEY = "wdkWizard";
    public static final String GUS_HOME_KEY = "GUS_HOME";
    public static final String WDK_ALWAYSGOTOSUMMARY_KEY = "wdkAlwaysGoToSummary";
    public static final String WDK_DEFAULT_VIEW_DIR = "/wdk";
    public static final String WDK_CUSTOM_VIEW_DIR = "/wdkCustomization";
    public static final String WDK_PAGES_DIR = "jsp";
    public static final String WDK_RESULTS_DIR = "results";
    public static final String WDK_QUESTIONS_DIR = "questions";
    public static final String WDK_RECORDS_DIR = "records";
    public static final String WDK_REPORTERS_DIR = "reporters";
    public static final String WDK_STRATEGY_DIR = "strategies";

    public static final String WDK_DOWNLOAD_CONFIG_PAGE = "ReporterConfig.jsp";
    public static final String WDK_LOGIN_PAGE = "Login.jsp";
    public static final String WDK_MODEL_ERROR_PAGE = "Error.jsp";
    public static final String WDK_PASSWORD_PAGE = "Password.jsp";
    public static final String WDK_PROFILE_PAGE = "Profile.jsp";
    public static final String WDK_QUESTION_PAGE = "Question.jsp";
    public static final String WDK_RECORD_PAGE = "Record.jsp";
    public static final String WDK_REGISTER_PAGE = "Register.jsp";
    public static final String WDK_RESET_PASSWORD_PAGE = "Resetpwd.jsp";
    public static final String WDK_RESULTS_PAGE = "results.jsp";
    public static final String WDK_STEP_HISTORY_PAGE = "stepHistory.jsp";
    public static final String WDK_STRATEGY_PAGE = "Workspace.jsp";
    public static final String WDK_STRATEGY_HISTORY_PAGE = "strategyHistory.jsp";
    public static final String WDK_SUMMARY_ERROR_PAGE = "SummaryError.jsp";
    public static final String WDK_USER_ERROR_PAGE = "Error.user.jsp";
    public static final String WDK_XMLDATACONTENT_PAGE = "XmlDataContent.jsp";
    public static final String WDK_XMLDATALIST_PAGE = "XmlDataList.jsp";
    public static final String WDK_CUSTOM_QUESTIONSETS_FLAT_PAGE = "customQuestionSetsFlat.jsp";
    public static final String WDK_CUSTOM_QUESTIONSETS_PAGE = "customQuestionSets.jsp";
    public static final String WDK_CUSTOM_DATASET_LIST_PAGE = "customDatasetList.jsp";
    public static final String WDK_CUSTOM_DATASET_PAGE = "customDataset.jsp";
    public static final String WDK_CUSTOM_CREATE_DATASET_PAGE = "customCreateDataset.jsp";
    public static final String WDK_SHOW_SUMMARY_ACTION = "showSummary.do";

    public static final String WDK_QUESTION_KEY = "wdkQuestion";
    public static final String WDK_QUESTION_PARAMS_KEY = "wdkQuestionParams";
    public static final String WDK_ANSWER_KEY = "wdkAnswer";
    public static final String WDK_STRATEGY_KEY = "wdkStrategy";
    public static final String WDK_STRATEGY_COLLECTION_KEY = "wdkActiveStrategies";
    public static final String WDK_STRATEGY_ID_KEY = "strategy";
    public static final String WDK_STEP_KEY = "wdkStep";
    public static final String WDK_STEP_ID_KEY = "step";
    public static final String WDK_STEP_IX_KEY = "step";
    public static final String WDK_REPORT_FORMATS_KEY = "wdkReportFormats";
    public static final String WDK_REPORT_FORMAT_KEY = "wdkReportFormat";
    public static final String WDK_RECORD_KEY = "wdkRecord";
    public static final String NEXT_QUESTION_OPERAND = "nextQuestionOperand";
    public static final String QUESTIONFORM_KEY = "questionForm";
    public static final String BOOLEAN_QUESTION_FORM_KEY = "booleanQuestionForm";
    public static final String BOOLEAN_SEED_QUESTION_KEY = "booleanSedQuestionName";
    public static final String CURRENT_BOOLEAN_ROOT_KEY = "currentBooleanRoot";
    public static final String BOOLEAN_OPERATIONS_PARAM_NAME = "booleanOps";
    public static final String DOWNLOAD_RESULT_KEY = "downloadResult";
    public static final String WDK_XMLQUESTIONSETS_KEY = "wdkXmlQuestionSets";
    public static final String WDK_XMLANSWER_KEY = "wdkXmlAnswer";
    public static final String WDK_SUMMARY_ATTRS_KEY = "wdkSummaryAttributesByQuestion"; 
    public static final String WDK_LOGIN_URL_KEY = "wdkLoginUrl";
    public static final String WDK_PAGE_SIZE_KEY = "pageSize";
    public static final String WDK_ALT_PAGE_SIZE_KEY = "altPageSize";
    public static final String WDK_ALL_RECORD_IDS_KEY = "allRecordIds";
    public static final String WDK_RESULT_SIZE_ONLY_KEY = "resultSizeOnly";
    public static final String WDK_RESULT_SET_ONLY_KEY = "resultsOnly";
    public static final String WDK_FILTER_KEY = "wdkFilter";
    public static final String WDK_STRATEGY_CHECKSUM_KEY = "strategy_checksum";
    public static final String WDK_STATE_KEY = "state";
    public static final String WDK_OPEN_KEY = "open";
    public static final String WDK_NEW_STRATEGY_KEY = "newStrategy";
    public static final String WDK_ASSIGNED_WEIGHT_KEY = "weight";
    public static final String WDK_RESPONSE_TYPE_KEY = "responseType";

    // params used in the url
    public static final String WDK_STEP_ID_PARAM = "step";
    public static final String WDK_SKIPTO_DOWNLOAD_PARAM = "skip_to_download";
    public static final String WDK_HISTORY_TYPE_PARAM = "type";
    public static final String WDK_NO_STRATEGY_PARAM = "no_strategy";

    // constants for remote logins
    public static final String WDK_REMOTE_URL_KEY = "remoteUrl";
    public static final String WDK_REMOTE_ACTION_KEY = "remoteAction";
    public static final String WDK_REMOTE_SIGNATURE_KEY = "signature";
    public static final String WDK_REMOTE_LOGIN_KEY = "remoteKey";

    // the constants for user authentication/authorization
    public static final String WDK_USER_KEY = "wdkUser";
    public static final String WDK_EMAIL_KEY = "email";
    public static final String WDK_PASSWORD_KEY = "password";
    public static final String WDK_OPENID_KEY = "openid";
    public static final String WDK_OPENID_AUTH_SERVICE_KEY = "openIdAuthService";
    public static final String WDK_REMEMBER_USER_KEY = "openIdRememberUser";
    public static final String WDK_ERROR_TEXT_KEY = "errorText";
    public static final String WDK_LOGIN_ERROR_KEY = "loginError";
    public static final String WDK_REGISTER_ERROR_KEY = "registerError";
    public static final String WDK_PROFILE_ERROR_KEY = "profileError";
    public static final String WDK_CHANGE_PASSWORD_ERROR_KEY = "changePasswordError";
    public static final String WDK_RESET_PASSWORD_ERROR_KEY = "resetPasswordError";
    public static final String WDK_REFERRER_URL_KEY = "referrerUrl";
    public static final String WDK_REDIRECT_URL_KEY = "redirectUrl";
    public static final String WDK_PREFERENCE_GLOBAL_KEY = "preference_global_";
    public static final String WDK_PREFERENCE_PROJECT_KEY = "preference_project_";
    public static final String WDK_SUMMARY_COMMAND_KEY = "command";
    public static final String WDK_SUMMARY_ATTRIBUTE_KEY = "attribute";
    public static final String WDK_SUMMARY_SORTING_ORDER_KEY = "sortOrder";
    public static final String WDK_SUMMARY_ARRANGE_ORDER_KEY = "left";
    public static final String WDK_SORTING_KEY = "sort";
    public static final String WDK_SUMMARY_KEY = "summary";

    //key for finding action forward, from struts-config.xml, used in Action classes
    public static final String SHOW_QUESTION_MAPKEY = "show_question";
    public static final String SKIPTO_SUMMARY_MAPKEY = "skip_to_summary";
    public static final String SHOW_QUESTIONSETS_MAPKEY = "show_questionsets";
    public static final String SHOW_QUESTIONSETSFLAT_MAPKEY = "show_questionsetsflat";
    public static final String PROCESS_QUESTIONSETSFLAT_MAPKEY = "process_questionsetsflat";
    public static final String SHOW_SUMMARY_MAPKEY = "show_summary";
    public static final String SHOW_ERROR_MAPKEY = "show_error";
    public static final String SKIPTO_RECORD_MAPKEY = "skip_to_record";
    public static final String SKIPTO_DOWNLOAD_MAPKEY = "skip_to_download";
    public static final String SHOW_RECORD_MAPKEY = "show_record";
    public static final String PQ_SHOW_SUMMARY_MAPKEY = "pq_show_summary";
    public static final String PQ_SHOW_BASKET_MAPKEY = "pq_show_basket";
    public static final String PQ_START_BOOLEAN_MAPKEY = "pq_start_boolean";
    public static final String PBQ_GET_BOOLEAN_ANSWER_MAPKEY = "pbq_get_boolean_answer";
    public static final String PBQ_GROW_BOOLEAN_MAPKEY = "pbq_grow_boolean";
    public static final String GROW_BOOLEAN_MAPKEY = "grow_boolean";
    public static final String GET_BOOLEAN_ANSWER_MAPKEY = "get_boolean_answer";
    public static final String CONFIG_DOWNLOAD_MAPKEY = "config_download";
    public static final String GET_DOWNLOAD_RESULT_MAPKEY = "get_download_result";
    public static final String GET_DOWNLOAD_CONFIG_MAPKEY = "get_download_config";
    public static final String SHOW_STEP_HISTORY_MAPKEY = "show_step_history";
    public static final String SHOW_STRAT_HISTORY_MAPKEY = "show_strat_history";
    public static final String DOWNLOAD_HISTORY_ANSWER_MAPKEY = "download_history_answer";
    public static final String DELETE_HISTORY_MAPKEY = "delete_history";
    public static final String RENAME_HISTORY_MAPKEY = "rename_history";
    public static final String PROCESS_BOOLEAN_EXPRESSION_MAPKEY = "process_boolean_expression";
    public static final String SHOW_XMLDATA_LIST_MAPKEY = "show_xmldata_list";
    public static final String SHOW_XMLDATA_CONTENT_MAPKEY = "show_xmldata_content";
    public static final String SHOW_ERRORPAGE_USER_MAPKEY = "show_error_page_user";
    public static final String SHOW_ERRORPAGE_MODEL_MAPKEY = "show_error_page_model";
    public static final String SHOW_REGISTER_MAPKEY = "show_register";
    public static final String SHOW_PROFILE_MAPKEY = "show_profile";
    public static final String SHOW_PASSWORD_MAPKEY = "show_password";
    public static final String SHOW_RESET_PASSWORD_MAPKEY = "show_reset_password";
    public static final String PROCESS_LOGOUT_MAPKEY = "process_logout";
    public static final String SHOW_DATASET_LIST_MAPKEY = "show_dataset_list";
    public static final String SHOW_DATASET_MAPKEY = "show_dataset";
    public static final String UPDATE_DATASET_MAPKEY = "update_dataset";
    public static final String DELETE_DATASET_MAPKEY = "delete_dataset";
    public static final String SHOW_CREATE_DATASET_MAPKEY = "show_create_dataset";
    public static final String CREATE_DATASET_MAPKEY = "create_dataset";
    public static final String RESULTSONLY_MAPKEY = "results_only";
    public static final String SHOW_STRATEGY_MAPKEY = "show_strategy";
    public static final String DELETE_STRATEGY_MAPKEY = "delete_strategy";
    public static final String SHOW_APPLICATION_MAPKEY = "show_application";

    //button click detectors, used in action, action forms, and jsp pages
    public static final String PQ_SUBMIT_KEY = "questionSubmit"; //match question.jsp
    public static final String PQ_SUBMIT_GET_ANSWER = "Get Answer"; //match question.jsp
    public static final String PQ_SUBMIT_EXPAND_QUERY = "Expand Question"; //match question.jsp
    public static final String PBQ_SUBMIT_KEY = "process_boolean_question"; //match booleanQuestion.jsp
    public static final String PBQ_SUBMIT_GET_BOOLEAN_ANSWER = "Retrieve Answer"; //match booleanQuestion.jsp
    public static final String PBQ_SUBMIT_GROW_BOOLEAN = "Expand"; //match WEB-INF/includes/booleanQuestionNode.jsp
    public static final String PD_CHOOSE_KEY = "chooseFields"; //match summary.jsp 
    public static final String DOWNLOAD_INCLUDE_HEADER = "includeHeader"; //match downloadConfig.jsp
    public static final String YES = "yes"; //match downloadConfig.jsp
    public static final String NO = "no"; 
    public static final String ALL = "all"; //match downloadConfig.jsp
    public static final String NAME = "name"; //match xmlDataList.jsp
    public static final String ERROR_TYPE_PARAM = "type"; //match struts-config.xml
    public static final String ERROR_TYPE_MODEL = "model"; 
    public static final String ERROR_TYPE_USER = "user";
    public static final String VALIDATE_PARAM = "validate";
    public static final String GOTO_SUMMARY_PARAM = "goto_summary";
    public static final String QUESTION_FULLNAME_PARAM = "questionFullName";
    public static final String ALWAYS_GOTO_SUMMARY_PARAM = "always_goto_summary";
    public static final String FROM_QUESTIONSET_PARAM = "fromQuestionSet";
    //used in action, action forms, and jsp pages
    public static final String NEXT_QUESTION_OPERAND_SUFFIX = "_nextQuestionOperand"; //match WEB-INF/includes/booleanQuestionNode.jsp
    public static final String NEXT_BOOLEAN_OPERATION_SUFFIX = "_nextBooleanOperation"; //match WEB-INF/includes/booleanQuestionNode.jsp

    //keys for WDK cookies
    public static final String WDK_TAB_STATE_COOKIE = "wdk_tab_state";

    //name of webapp init params, from web.xml, used in ApplicationInitListener.java
    protected static final String WDK_MODELCONFIGXML_PARAM = "wdkModelConfigXml_param";
    protected static final String WDK_MODELXML_PARAM = "wdkModelXml_param";
    protected static final String WDK_MODELSCHEMA_PARAM = "wdkModelSchema_param";
    protected static final String WDK_MODELPROPS_PARAM = "wdkModelProps_param";
    protected static final String WDK_LOGFILE_PARAM = "wdkLogFile_param";
    protected static final String WDK_MODELPARSER_PARAM = "wdkModelParser_param";
    protected static final String WDK_ALWAYSGOTOSUMMARY_PARAM = "wdkAlwaysGoToSummary_param";
    protected static final String WDK_XMLSCHEMA_PARAM = "wdkXmlSchema_param";   // the schema for xml data source
    protected static final String WDK_XMLDATA_DIR_PARAM = "wdkXmlDataDir_param";
    protected static final String WDK_LOGIN_URL_PARAM = "wdkLoginUrl_param";
    
    //default value of webapp init params, from web.xml, used in ApplicationInitListener.java
    protected static final String DEFAULT_WDKMODELCONFIGXML = "/WEB-INF/wdk-model/config/toyModel-config.xml";
    protected static final String DEFAULT_WDKMODELXML = "/WEB-INF/wdk-model/config/toyModel.xml";
    protected static final String DEFAULT_WDKMODELSCHEMA = "/WEB-INF/wdk-model/lib/rng/wdkModel.rng";
    protected static final String DEFAULT_WDKMODELPROPS = "/WEB-INF/wdk-model/config/toyModel.props";
    protected static final String DEFAULT_WDKMODELPARSER = "org.gusdb.wdk.model.implementation.ModelXmlParser";
    protected static final String DEFAULT_XMLSCHEMA = "/WEB-INF/wdk-model/lib/rng/xmlAnswer.rng";
    protected static final String DEFAULT_XMLDATA_DIR = "/WEB-INF/wdk-model/lib/xml";
    
    public static final int MIN_PAGE_SIZE = 5;
    public static final String WDK_EXCEPTION = "exception";
}
