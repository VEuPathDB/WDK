package org.gusdb.wdk.controller;

public class CConstants {
    private CConstants() {
	; // no-op
    }
    //key for objects in cache, used in Action/ActionForm classes and maybe jsp pages
    public static final String WDK_RESULTFACTORY_KEY = "wdkResultFactory";
    public static final String WDK_MODEL_KEY = "wdkModel";
    public static final String WDK_CUSTOMVIEWDIR_KEY = "wdkCustomeViewDir";
    public static final String WDK_ALWAYSGOTOSUMMARY_KEY = "wdkAlwaysGoToSummary";
    public static final String WDK_CUSTOM_QUESTIONSETS_FLAT_PAGE = "customQuestionSetsFlat.jsp";
    public static final String WDK_CUSTOM_QUESTIONSETS_PAGE = "customQuestionSets.jsp";
    public static final String WDK_CUSTOM_QUESTION_PAGE = "customQuestion.jsp";
    public static final String WDK_CUSTOM_SUMMARY_PAGE = "customSummary.jsp";
    public static final String WDK_CUSTOM_RECORD_PAGE = "customRecord.jsp";
    public static final String WDK_CUSTOM_HISTORY_PAGE = "customQueryHistory.jsp";
    public static final String WDK_CUSTOM_REGISTER_PAGE = "customRegister.jsp";
    public static final String WDK_CUSTOM_PROFILE_PAGE = "customProfile.jsp";
    public static final String WDK_CUSTOM_PASSWORD_PAGE = "customPassword.jsp";
    public static final String WDK_CUSTOM_RESET_PASSWORD_PAGE = "customResetpwd.jsp";
    public static final String WDK_CUSTOM_DATASET_LIST_PAGE = "customDatasetList.jsp";
    public static final String WDK_CUSTOM_DATASET_PAGE = "customDataset.jsp";
    public static final String WDK_CUSTOM_CREATE_DATASET_PAGE = "customCreateDataset.jsp";
    public static final String WDK_SHOW_SUMMARY_ACTION = "showSummary.do";

    public static final String WDK_QUESTION_KEY = "wdkQuestion";
    public static final String WDK_QUESTION_PARAMS_KEY = "wdkQuestionParams";
    public static final String WDK_ANSWER_KEY = "wdkAnswer";
    public static final String WDK_REPORT_FORMATS_KEY = "wdkReportFormats";
    public static final String WDK_REPORT_FORMAT_KEY = "wdkReportFormat";
    public static final String WDK_RECORD_KEY = "wdkRecord";
    public static final String WDK_HISTORY_KEY = "wdkHistory";
    public static final String WDK_HISTORY_ID_KEY = "wdk_history_id";
    public static final String WDK_HISTORY_CUSTOM_NAME_KEY = "customHistoryName";
    public static final String NEXT_QUESTION_OPERAND = "nextQuestionOperand";
    public static final String QUESTIONFORM_KEY = "questionForm";
    public static final String BOOLEAN_QUESTION_FORM_KEY = "booleanQuestionForm";
    public static final String BOOLEAN_SEED_QUESTION_KEY = "booleanSedQuestionName";
    public static final String CURRENT_BOOLEAN_ROOT_KEY = "currentBooleanRoot";
    public static final String BOOLEAN_OPERATIONS_PARAM_NAME = "booleanOps";
    public static final String DOWNLOAD_RESULT_KEY = "downloadResult";
    public static final String WDK_XMLQUESTIONSETS_KEY = "wdkXmlQuestionSets";
    public static final String WDK_XMLANSWER_KEY = "wdkXmlAnswer";
    public static final String QUESTIONSETFORM_KEY = "questionSetForm"; 
    public static final String WDK_SUMMARY_ATTRS_KEY = "wdkSummaryAttributesByQuestion"; 
    public static final String WDK_LOGIN_URL_KEY = "wdkLoginUrl";
    public static final String WDK_PAGE_SIZE_KEY = "pageSize";
    public static final String WDK_ALT_PAGE_SIZE_KEY = "altPageSize";
    public static final String WDK_ALL_RECORD_IDS_KEY = "allRecordIds";
    
    // constants for remote logins
    public static final String WDK_REMOTE_URL_KEY = "remoteUrl";
    public static final String WDK_REMOTE_ACTION_KEY = "remoteAction";
    public static final String WDK_REMOTE_SIGNATURE_KEY = "signature";
    public static final String WDK_REMOTE_LOGIN_KEY = "remoteKey";

    // the constants for user authentication/authorization
    public static final String WDK_USER_KEY = "wdkUser";
    public static final String WDK_EMAIL_KEY = "email";
    public static final String WDK_PASSWORD_KEY = "password";
    public static final String WDK_LOGIN_ERROR_KEY = "loginError";
    public static final String WDK_REGISTER_ERROR_KEY = "registerError";
    public static final String WDK_PROFILE_ERROR_KEY = "profileError";
    public static final String WDK_CHANGE_PASSWORD_ERROR_KEY = "changePasswordError";
    public static final String WDK_RESET_PASSWORD_ERROR_KEY = "resetPasswordError";
    public static final String WDK_REFERER_URL_KEY = "refererUrl";
    public static final String WDK_ORIGIN_URL_KEY = "originUrl";
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
    public static final String SKIPTO_RECORD_MAPKEY = "skip_to_record";
    public static final String SHOW_RECORD_MAPKEY = "show_record";
    public static final String PQ_SHOW_SUMMARY_MAPKEY = "pq_show_summary";
    public static final String PQ_START_BOOLEAN_MAPKEY = "pq_start_boolean";
    public static final String PBQ_GET_BOOLEAN_ANSWER_MAPKEY = "pbq_get_boolean_answer";
    public static final String PBQ_GROW_BOOLEAN_MAPKEY = "pbq_grow_boolean";
    public static final String GROW_BOOLEAN_MAPKEY = "grow_boolean";
    public static final String GET_BOOLEAN_ANSWER_MAPKEY = "get_boolean_answer";
    public static final String CONFIG_DOWNLOAD_MAPKEY = "config_download";
    public static final String GET_DOWNLOAD_RESULT_MAPKEY = "get_download_result";
    public static final String GET_DOWNLOAD_CONFIG_MAPKEY = "get_download_config";
    public static final String SHOW_QUERY_HISTORY_MAPKEY = "show_query_history";
    public static final String DOWNLOAD_HISTORY_ANSWER_MAPKEY = "download_history_answer";
    public static final String DELETE_HISTORY_MAPKEY = "delete_history";
    public static final String RENAME_HISTORY_MAPKEY = "rename_history";
    public static final String PROCESS_BOOLEAN_EXPRESSION_MAPKEY = "process_boolean_expression";
    public static final String SHOW_XMLDATA_LIST_MAPKEY = "show_xmldata_list";
    public static final String SHOW_XMLDATA_CONTENT_MAPKEY = "show_xmldata_content";
    public static final String SHOW_ERRORPAGE_USER_MAPKEY = "show_error_page_user";
    public static final String SHOW_ERRORPAGE_MODEL_MAPKEY = "show_error_page_model";
    public static final String SHOW_LOGIN_MAPKEY = "show_login_page";
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
    public static final int MAX_PARAM_LABEL_LEN = 69;
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

    //name of webapp init params, from web.xml, used in ApplicationInitListener.java
    protected static final String WDK_MODELCONFIGXML_PARAM = "wdkModelConfigXml_param";
    protected static final String WDK_MODELXML_PARAM = "wdkModelXml_param";
    protected static final String WDK_MODELSCHEMA_PARAM = "wdkModelSchema_param";
    protected static final String WDK_MODELPROPS_PARAM = "wdkModelProps_param";
    protected static final String WDK_LOGFILE_PARAM = "wdkLogFile_param";
    protected static final String WDK_MODELPARSER_PARAM = "wdkModelParser_param";
    protected static final String WDK_CUSTOMVIEWDIR_PARAM = "wdkCustomViewDir_param";
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
    protected static final String DEFAULT_WDKCUSTOMVIEWDIR = "/customPages/";
    protected static final String DEFAULT_XMLSCHEMA = "/WEB-INF/wdk-model/lib/rng/xmlAnswer.rng";
    protected static final String DEFAULT_XMLDATA_DIR = "/WEB-INF/wdk-model/lib/xml";
}
