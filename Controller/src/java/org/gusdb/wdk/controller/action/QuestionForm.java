package org.gusdb.wdk.controller.action;

import org.apache.struts.action.ActionForm;
import java.util.Map;
import java.util.HashMap;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.QuestionBean;

/**
 *  form bean for showing a wdk question from a question set
 */

public class QuestionForm extends ActionForm {

    private Map myProps = new HashMap();
    private Map myLabels = new HashMap();
    private Map myValues = new HashMap();

    private QuestionBean question = null;

    public void reset() {
	; //no-op
    }

    public void setMyProp(String key, Object val)
    {
	myProps.put(key, val);
    }

    public Object getMyProp(String key)  throws WdkModelException
    {
	Object res = myProps.get(key);
	return res;
    }

    /* returns a list of labels for a select box */
    public String[] getLabels(String key) throws WdkModelException
    {
	return (String[])myLabels.get(key);
    }

    /* returns a list of values for a select box */
    public String[] getValues(String key) throws WdkModelException
    {
	return (String[])myValues.get(key);
    }

    void setMyProps(Map props) { myProps = props; }
    public Map getMyProps() { return myProps; }

    void setMyLabels (Map lbls) { myLabels = lbls; }
    Map getMyLabels () { return myLabels; }

    void setMyValues (Map vals) { myValues = vals; }
    Map getMyValues () { return myValues; }

    public void setQuestion(QuestionBean s) { question = s; }
    public QuestionBean getQuestion() { return question; }
}
