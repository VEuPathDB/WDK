package org.gusdb.wdk.controller;

public class CConstants {
    private CConstants() {
	; // no-op
    }
    //key for objects in cache, used in Action/ActionForm classes and maybe jsp pages
    public static final String WDK_RESULTFACTORY_KEY = "wdkResultFactory";
    public static final String WDK_MODEL_KEY = "wdkModel";
    public static final String WDK_QUESTION_KEY = "wdkQuestion";
    public static final String WDK_ANSWER_KEY = "wdkAnswer";
    public static final String WDK_RECORD_KEY = "wdkRecord";
    public static final String NEXT_QUESTION_OPERAND = "nextQuestionOperand";
    public static final String QUESTIONSETFORM_KEY = "questionSetForm";
    public static final String QUESTIONFORM_KEY = "questionForm";
    public static final String BOOLEAN_QUESTION_FORM_KEY = "booleanQuestionForm";
    public static final String BOOLEAN_SEED_QUESTION_KEY = "booleanSeedQuestionName";
    public static final String CURRENT_BOOLEAN_ROOT_KEY = "currentBooleanRoot";
    public static final String BOOLEAN_OPERATIONS_PARAM_NAME = "booleanOps";
    
    //key for finding action forward, from struts-config.xml, used in Action classes
    public static final String SHOW_QUESTION_MAPKEY = "show_question";
    public static final String SHOW_SUMMARY_MAPKEY = "show_summary";
    public static final String SHOW_RECORD_MAPKEY = "show_record";
    public static final String PQ_SHOW_SUMMARY_MAPKEY = "pq_show_summary";
    public static final String PQ_START_BOOLEAN_MAPKEY = "pq_start_boolean";
    public static final String PBQ_GET_BOOLEAN_ANSWER_MAPKEY = "pbq_get_boolean_answer";
    public static final String PBQ_GROW_BOOLEAN_MAPKEY = "pbq_grow_boolean";
    public static final String GROW_BOOLEAN_MAPKEY = "grow_boolean";
    public static final String GET_BOOLEAN_ANSWER_MAPKEY = "get_boolean_answer";

    //name of webapp init params, from web.xml, used in ApplicationInitListener.java
    protected static final String WDK_MODELCONFIGXML_PARAM = "wdkModelConfigXml_param";
    protected static final String WDK_MODELXML_PARAM = "wdkModelXml_param";
    protected static final String WDK_MODELSCHEMA_PARAM = "wdkModelSchema_param";
    protected static final String WDK_MODELPROPS_PARAM = "wdkModelProps_param";
    protected static final String WDK_LOGFILE_PARAM = "wdkLogFile_param";
    protected static final String WDK_MODELPARSER_PARAM = "wdkModelParser_param";

    //default value of webapp init params, from web.xml, used in ApplicationInitListener.java
    protected static final String DEFAULT_WDKMODELCONFIGXML = "/WEB-INF/wdk-config/wdkModelConfig.xml";
    protected static final String DEFAULT_WDKMODELXML = "/WEB-INF/wdk-config/wdkModel.xml";
    protected static final String DEFAULT_WDKMODELSCHEMA = "/WEB-INF/wdk-config/wdkModel.rng";
    protected static final String DEFAULT_WDKMODELPROPS = "/WEB-INF/wdk-config/wdkModel.props";
    protected static final String DEFAULT_WDKMODELPARSER = "org.gusdb.wdk.model.implementation.ModelXmlParser";
}
