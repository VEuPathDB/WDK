package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.Question;


import java.util.Map;

/**
 * A wrapper on a {@link RecordClass} that provides simplified access for 
 * consumption by a view
 */ 
public class RecordClassBean {

    RecordClass recordClass;

    public RecordClassBean(RecordClass recordClass) {
	this.recordClass = recordClass;
    }

    public String getFullName() {
	return recordClass.getFullName();
    }

    public String getType() {
	return recordClass.getType();
    }

    /**
     * @return Map of fieldName --> {@link org.gusdb.wdk.model.FieldI}
     */
    public Map getAttributeFields() {
	return recordClass.getAttributeFields();
    }

    /**
     * @return Map of fieldName --> {@link org.gusdb.wdk.model.FieldI}
     */
    public Map getTableFields() {
	return recordClass.getTableFields();
    }

    /**
     * used by the controller
     */
    public RecordBean makeRecord () {
	return new RecordBean(recordClass.makeRecordInstance());
    }

    public QuestionBean[] getQuestions(){

	Question questions[] = recordClass.getQuestions();
	QuestionBean[] questionBeans = new QuestionBean[questions.length];
	for (int i = 0; i < questions.length; i++){
	    questionBeans[i] = new QuestionBean(questions[i]);
	}
	return questionBeans;
    }

}
