package org.gusdb.wdk.controller;

public class CConstants {
    private CConstants() {
	; // no-op
    }

    protected static final String WDK_MODELCONFIGXML_PARAM = "wdkModelConfigXml_param";
    protected static final String WDK_MODELXML_PARAM = "wdkModelXml_param";
    protected static final String WDK_MODELSCHEMA_PARAM = "wdkModelSchema_param";
    protected static final String WDK_MODELPROPS_PARAM = "wdkModelProps_param";
    protected static final String WDK_LOGFILE_PARAM = "wdkLogFile_param";
    protected static final String WDK_MODELPARSER_PARAM = "wdkModelParser_param";

    public static final String WDK_RESULTFACTORY_KEY = "wdkResultFactory";
    public static final String WDK_MODEL_KEY = "wdkModel";
    public static final String WDK_QUESTION_KEY = "wdkQuestion";
    public static final String WDK_ANSWER_KEY = "wdkAnswer";
    public static final String WDK_RECORD_KEY = "wdkRecord";
    public static final String NEXT_QUESTION_OPERAND = "nextQuestionOperand";
    
    public static final String SHOW_QUESTION_MAPKEY = "show_question";
    public static final String SHOW_SUMMARY_MAPKEY = "show_summary";
    public static final String SHOW_RECORD_MAPKEY = "show_record";
    public static final String PQ_SHOW_SUMMARY_MAPKEY = "pq_show_summary";
    public static final String PQ_GROW_BOOLEAN_MAPKEY = "pq_grow_boolean";


    public static final String QUESTIONSETFORM_KEY = "questionSetForm";
    public static final String QUESTIONFORM_KEY = "questionForm";
    public static final String BOOLEAN_QUESTION_FORM_KEY = "booleanQuestionForm";
    

    public static final String GROW_BOOLEAN_MAPKEY = "grow_boolean";
    public static final String BOOLEAN_SEED_QUESTION_MAPKEY = "booleanSeedQuestionName";
    public static final String CURRENT_BOOLEAN_ROOT_MAPKEY = "currentBooleanRoot";
    public static final String BOOLEAN_OPERATIONS_PARAM_NAME = "booleanOps";
    


    protected static final String DEFAULT_WDKMODELCONFIGXML = "/WEB-INF/wdk-config/wdkModelConfig.xml";
    protected static final String DEFAULT_WDKMODELXML = "/WEB-INF/wdk-config/wdkModel.xml";
    protected static final String DEFAULT_WDKMODELSCHEMA = "/WEB-INF/wdk-config/wdkModel.rng";
    protected static final String DEFAULT_WDKMODELPROPS = "/WEB-INF/wdk-config/wdkModel.props";
    protected static final String DEFAULT_WDKMODELPARSER = "org.gusdb.wdk.model.implementation.ModelXmlParser";

    
}
