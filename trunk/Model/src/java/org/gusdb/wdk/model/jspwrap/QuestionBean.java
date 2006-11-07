package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.FlatVocabParam;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.BooleanQuestionNode;
import org.gusdb.wdk.model.AttributeField;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * A wrapper on a {@link Question} that provides simplified access for 
 * consumption by a view
 */ 
public class QuestionBean implements Serializable {

    /**
     * Added by Jerric - to make QuestionBean serializable
     */
    private static final long serialVersionUID = 6353373897551871273L;
    
    Question question;

    public QuestionBean(Question question) {
	this.question = question;
    }

    public RecordClassBean getRecordClass(){
	return new RecordClassBean(question.getRecordClass());
    }

    public ParamBean[] getParams() {
	Param[] params = question.getParams();
	ParamBean[] paramBeans = new ParamBean[params.length];
	for (int i=0; i<params.length; i++) {
	    if (params[i] instanceof FlatVocabParam) {
		paramBeans[i] = 
		    new FlatVocabParamBean((FlatVocabParam)params[i]);
	    } else {
		paramBeans[i] = new ParamBean(params[i]);
	    }
	}
	return paramBeans;
    }

    public Map getParamsMap() {
	ParamBean[] paramBeans = getParams();
	Map<String, ParamBean> pMap = new LinkedHashMap<String, ParamBean>();
	for (int i=0; i<paramBeans.length; i++) {
	    ParamBean p = paramBeans[i];
	    pMap.put(p.getName(), p);
	}
	return pMap;
    }

    public Map getSummaryAttributesMap() {
	Map<String, AttributeField> attribs = question.getSummaryAttributes();
	Iterator<String> ai = attribs.keySet().iterator();

	Map<String, AttributeFieldBean> saMap = new LinkedHashMap<String, AttributeFieldBean>();
	while (ai.hasNext()) {
	    String attribName = ai.next();
	    saMap.put(attribName, new AttributeFieldBean(attribs.get(attribName)));
	}
	return saMap;
    }

    public Map getReportMakerAttributesMap() {
	Map<String, AttributeField> attribs = question.getReportMakerAttributeFields();
	Iterator<String> ai = attribs.keySet().iterator();

	Map<String, AttributeFieldBean> rmaMap = new LinkedHashMap<String, AttributeFieldBean>();
	while (ai.hasNext()) {
	    String attribName = ai.next();
	    rmaMap.put(attribName, new AttributeFieldBean(attribs.get(attribName)));
	}
	return rmaMap;
    }

    public Map getAdditionalSummaryAttributesMap() {
	Map all = getReportMakerAttributesMap();
	Map dft = getSummaryAttributesMap();
	Map opt = new LinkedHashMap<String, AttributeFieldBean>();
	Iterator<String> ai = all.keySet().iterator();
	while (ai.hasNext()) {
	    String attribName = ai.next();
	    if (dft.get(attribName) == null) {
		opt.put(attribName, all.get(attribName));
	    }
	}
	return opt;
    }

    public String getName() {
	return question.getName();
    }

    public String getFullName() {
	return question.getFullName();
    }

    public String getDisplayName() {
	return question.getDisplayName();
    }

    public String getHelp() {
	return question.getHelp();
    }
    
    public BooleanQuestionLeafBean makeBooleanQuestionLeaf(){

	BooleanQuestionNode bqn = new BooleanQuestionNode(this.question, null);
	BooleanQuestionLeafBean leaf = new BooleanQuestionLeafBean(bqn, null);
	return leaf;

    }

    /**
     * Called by the controller
     * @param paramValues Map of paramName-->value
     * @param start Index of the first record to include in the answer
     * @param end Index of the last record to include in the answer
     */
    public AnswerBean makeAnswer(Map paramValues, int start, int end) throws WdkModelException, WdkUserException {
	return new AnswerBean(question.makeAnswer(paramValues, start,end));
    }
    
    public String getDescription(){
	return question.getDescription();
    }
    
    public String getSummary(){
	return question.getSummary();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Question#getCategory()
     */
    public String getCategory() {
        return question.getCategory();
    }
}
