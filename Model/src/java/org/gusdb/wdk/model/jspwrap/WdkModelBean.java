package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.WdkModel;

import java.util.Map;

/**
 * A wrapper on a {@link WdkModel} that provides simplified access for 
 * consumption by a view
 */ 
public class WdkModelBean {

    WdkModel model;
    

    public WdkModelBean(WdkModel model) {
	this.model = model;
    }

    public String getName() {
	return model.getName();
    }
    
    public String getIntroduction() {
	return model.getIntroduction();
    }
    
    /**
     * @return Map of questionSetName --> {@link QuestionSetBean}
     */
    public Map getQuestionSets() {
	return model.getQuestionSets();
    }
}
