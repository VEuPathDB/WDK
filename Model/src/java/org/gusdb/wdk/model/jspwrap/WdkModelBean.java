package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.QuestionSet;

import java.util.Map;

/**
 * A wrapper on the wdk model that provides simplified access for 
 * consumption by a view
 */ 
public class WdkModelBean {

    WdkModel model;
    

    public WdkModelBean(WdkModel model) {
	this.model = model;
    }

    public String getIntroduction() {
	return model.getIntroduction();
    }

    public Map getQuestionSets() {
	return model.getQuestionSets();
    }
}
