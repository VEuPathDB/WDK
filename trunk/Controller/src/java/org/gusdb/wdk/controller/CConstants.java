package org.gusdb.wdk.controller;

public class CConstants {
    private CConstants() {
	; // no-op
    }
    //key for objects in cache, used in Action/ActionForm classes and maybe jsp pages
    public static final String WDK_RESULTFACTORY_KEY = "wdkResultFactory";
    public static final String WDK_MODEL_KEY = "wdkModel";
    public static final String WDK_CUSTOMVIEWDIR_KEY = "wdkCustomeViewDir";
    public static final String WDK_CUSTOM_QUESTIONSETS_FLAT_PAGE = "customQuestionSetsFlat.jsp";
    public static final String WDK_CUSTOM_QUESTIONSETS_PAGE = "customQuestionSets.jsp";
    public static final String WDK_CUSTOM_QUESTION_PAGE = "customQuestion.jsp";
    public static final String WDK_CUSTOM_SUMMARY_PAGE = "customSummary.jsp";
    public static final String WDK_CUSTOM_RECORD_PAGE = "customRecord.jsp";
    public static final String WDK_QUESTION_KEY = "wdkQuestion";
    public static final String WDK_QUESTION_PARAMS_KEY = "wdkQuestionParams";
    public static final String WDK_ANSWER_KEY = "wdkAnswer";
    public static final String WDK_RECORD_KEY = "wdkRecord";
    public static final String WDK_USER_KEY = "wdkUser";
    public static final String NEXT_QUESTION_OPERAND = "nextQuestionOperand";
    //public static final String QUESTIONSETFORM_KEY = "questionSetForm";
    public static final String QUESTIONFORM_KEY = "questionForm";
    public static final String BOOLEAN_QUESTION_FORM_KEY = "booleanQuestionForm";
    public static final String BOOLEAN_SEED_QUESTION_KEY = "booleanSeedQuestionName";
    public static final String CURRENT_BOOLEAN_ROOT_KEY = "currentBooleanRoot";
    public static final String BOOLEAN_OPERATIONS_PARAM_NAME = "booleanOps";
    public static final String DOWNLOAD_RESULT_KEY = "downloadResult";
    
    //key for finding action forward, from struts-config.xml, used in Action classes
    public static final String SHOW_QUESTION_MAPKEY = "show_question";
    public static final String SHOW_QUESTIONSETS_MAPKEY = "show_questionsets";
    public static final String SHOW_QUESTIONSETSFLAT_MAPKEY = "show_questionsetsflat";
    public static final String PROCESS_QUESTIONSETSFLAT_MAPKEY = "process_questionsetsflat";
    public static final String SHOW_SUMMARY_MAPKEY = "show_summary";
    public static final String SHOW_RECORD_MAPKEY = "show_record";
    public static final String PQ_SHOW_SUMMARY_MAPKEY = "pq_show_summary";
    public static final String PQ_START_BOOLEAN_MAPKEY = "pq_start_boolean";
    public static final String PBQ_GET_BOOLEAN_ANSWER_MAPKEY = "pbq_get_boolean_answer";
    public static final String PBQ_GROW_BOOLEAN_MAPKEY = "pbq_grow_boolean";
    public static final String GROW_BOOLEAN_MAPKEY = "grow_boolean";
    public static final String GET_BOOLEAN_ANSWER_MAPKEY = "get_boolean_answer";
    public static final String CONFIG_DOWNLOAD_MAPKEY = "config_download";
    public static final String GET_DOWNLOAD_RESULT_MAPKEY = "get_download_result";
    public static final String DOWNLOAD_HISTORY_ANSWER_MAPKEY = "download_history_answer";
    public static final String DELETE_HISTORY_ANSWER_MAPKEY = "delete_history_answer";
    public static final String PROCESS_BOOLEAN_EXPRESSION_MAPKEY = "process_boolean_expression";

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
    public static final String ALL = "all"; //match downloadConfig.jsp
    public static final String USER_ANSWER_ID = "user_answer_id"; //match queryHistory.jsp

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

    //default value of webapp init params, from web.xml, used in ApplicationInitListener.java
    protected static final String DEFAULT_WDKMODELCONFIGXML = "/WEB-INF/wdk-config/wdkModelConfig.xml";
    protected static final String DEFAULT_WDKMODELXML = "/WEB-INF/wdk-config/wdkModel.xml";
    protected static final String DEFAULT_WDKMODELSCHEMA = "/WEB-INF/wdk-config/wdkModel.rng";
    protected static final String DEFAULT_WDKMODELPROPS = "/WEB-INF/wdk-config/wdkModel.props";
    protected static final String DEFAULT_WDKMODELPARSER = "org.gusdb.wdk.model.implementation.ModelXmlParser";
    protected static final String DEFAULT_WDKCUSTOMVIEWDIR = "/customPages/";
}
