package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.FlatVocabParam;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.BooleanQuestionNode;

import java.io.Serializable;
import java.util.Map;

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
}
