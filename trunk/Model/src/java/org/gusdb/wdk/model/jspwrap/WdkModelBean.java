package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.WdkModelException;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;

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
     * used by the controller
     */
    public RecordClassBean findRecordClass (String recClassRef) 
	throws WdkUserException, WdkModelException
    {
	return new RecordClassBean(model.getRecordClass(recClassRef));
    }
    
    /**
     * @return Map of questionSetName --> {@link QuestionSetBean}
     */
    public Map getQuestionSetsMap() {
	Map qSets = model.getQuestionSets();
	Iterator it = qSets.keySet().iterator();

	Map qSetBeans = new LinkedHashMap();
	while (it.hasNext()) {
	    Object qSetKey = it.next();
	    QuestionSetBean qSetBean = new QuestionSetBean((QuestionSet) qSets.get(qSetKey));
	    qSetBeans.put(qSetKey, qSetBean);
	}
	return qSetBeans;
    }

    public QuestionSetBean[] getQuestionSets() {
	Map qSets = model.getQuestionSets();
	Iterator it = qSets.keySet().iterator();

	QuestionSetBean[] qSetBeans = new QuestionSetBean[qSets.size()];
	int i = 0;
	while (it.hasNext()) {
	    Object qSetKey = it.next();
	    QuestionSetBean qSetBean = new QuestionSetBean((QuestionSet) qSets.get(qSetKey));
	    qSetBeans[i++] = qSetBean;
	}
	return qSetBeans;
    }

}
