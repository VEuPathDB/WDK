package org.gusdb.wdk.controller.action;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.gusdb.wdk.model.WdkModelException;
import java.util.Map;
import java.util.LinkedHashMap;


/**
 *  form bean for showing a wdk question from a question set
 */

public class QuestionSetForm extends ActionForm {

    /**
     * 
     */
    private static final long serialVersionUID = 9205598895135808021L;
    private static Logger logger = Logger.getLogger(QuestionSetForm.class);

    public QuestionSetForm () {
	super();
    }

    private String qFullName = null;
    private Map<String, Object> myProps = new LinkedHashMap<String, Object>();
    private Map myLabels = new LinkedHashMap();
    private Map myValues = new LinkedHashMap();
    private Map<String, Object> myPropObjects = new LinkedHashMap<String, Object>();

    public void setQuestionFullName(String qFN) {
	this.qFullName = qFN;
    }

    public String getQuestionFullName() {
	return this.qFullName;
    }

    public void reset() {
	/*
	QuestionBean wdkQuestion = getQuestion();
	ParamBean[] params = wdkQuestion.getParams();
	for (int i=0; i<params.length; i++) {
	    ParamBean p = params[i];
	    setMyProp(p.getName(), null);
	}
	*/
    }
    protected void resetMappedProps() {
	myProps.clear();
	myLabels.clear();
	myValues.clear();
    }

    public void setMyProp(String key, String val)
    {
	//System.err.println("*** QuestionSetForm.setMyProp: " + key + " = " + val + "\n");
	myProps.put(key, val.trim());
    }

    public void setMyPropObject(String key, Object val)
    {
        //System.err.println("*** QuestionSetForm.setMyProp: " + key + " = " + val + "\n");
        myPropObjects.put(key, val);
        logger.info("setMyPropObject: " + key + " = '" + val + "' (" + val.getClass().getName() + ")");
    }

    public void setMyMultiProp(String key, String[] vals)
    {
	//System.err.println("*** QuestionSetForm.setMyMultiProp: " + key + " with " + vals.length + " values\n");
	myProps.put(key, vals);
    }

    public String getMyProp(String key)  throws WdkModelException
    {
        Object value = getMyProps().get(key);
        if (value == null) return null;
        if (value instanceof String[]) {
            String[] array = (String[]) value;
            if (array.length > 0) return array[0];
            else return null;
        } else return (String) value;
    }

    public String[] getMyMultiProp(String key)  throws WdkModelException
    {
        Object value = getMyProps().get(key);
        if (value == null) return null;
        if (value instanceof String[]) return ( String[] )value;
        else return new String[]{ ( String )value };
    }

    public Object getMyPropObject(String key) throws WdkModelException {
	return myPropObjects.get(key);
    }

    /* returns a list of labels for a select box */
    public String[] getLabels(String key) throws WdkModelException
    {
	return (String[])getMyLabels().get(key);
    }

    /* returns a list of values for a select box */
    public String[] getValues(String key) throws WdkModelException
    {
	//System.out.println("DEBUG: QuestionSetForm:getValues for: " + key + ": " + getMyValues().get(key));

	return (String[])getMyValues().get(key);
    }

    void setMyProps(Map<String, Object> props) { myProps = props; }
    public Map<String, Object> getMyProps() { return myProps; }

    void setMyPropObjects(Map<String, Object> props) { myPropObjects = props; }
    public Map<String, Object> getMyPropObjects() { return myPropObjects; }

    void setMyLabels (Map lbls) { myLabels = lbls; }
    Map getMyLabels () { return myLabels; }

    void setMyValues (Map vals) { myValues = vals; }
    Map getMyValues () { return myValues; }
}
