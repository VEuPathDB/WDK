package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.Question;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * A wrapper on a {@link QuestionSet} that provides simplified access for 
 * consumption by a view
 */ 
public class QuestionSetBean {

    QuestionSet questionSet;

    public QuestionSetBean(QuestionSet questionSet) {
	this.questionSet = questionSet;
    }

    public QuestionBean[] getQuestions() {
	Question[] questions = questionSet.getQuestions();
	QuestionBean[] questionBeans = new QuestionBean[questions.length];
	for (int i=0; i<questions.length; i++) {
	    questionBeans[i] = new QuestionBean(questions[i]);
	}
	return questionBeans;
    }

    public Map getQuestionsMap() {
	LinkedHashMap map = new LinkedHashMap();
	QuestionBean[] questions = getQuestions();
	for (int i=0; i<questions.length; i++) {
	    map.put(questions[i].getName(), questions[i]);
	}
	return map;
    }

    public String getName() {
	return questionSet.getName();
    }

    public Boolean getIsInternal() {
	return questionSet.getIsInternal();
    }

    public String getDisplayName() {
	return questionSet.getDisplayName();
    }

    public String getDescription(){
	return questionSet.getDescription();
    }
}
