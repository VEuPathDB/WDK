package org.gusdb.wdk.controller.action;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.struts.action.ActionForm;

/**
 * form bean for showing a wdk question from a question set
 */

public class QuestionSetForm extends ActionForm {

    /**
     * 
     */
    private static final long serialVersionUID = 9205598895135808021L;

    protected String qFullName = null;
    private Map<String, String> myProps = new LinkedHashMap<String, String>();
    private Map<String, Object> myPropObjects = new LinkedHashMap<String, Object>();
    private Map<String, String[]> myLabels = new LinkedHashMap<String, String[]>();
    private Map<String, String[]> myValues = new LinkedHashMap<String, String[]>();

    public QuestionSetForm() {
        super();
    }

    public void setQuestionFullName(String qFN) {
        this.qFullName = qFN;
    }

    public String getQuestionFullName() {
        return this.qFullName;
    }

    public void reset() {
        myProps.clear();
        myLabels.clear();
        myValues.clear();
    }

    public void setMyProp(String key, String value) {
        myProps.put(key, value.trim().intern());
    }

    public void setMyMultiProp(String key, String[] values) {
        StringBuffer buffer = new StringBuffer();
        for (String value : values) {
            if (buffer.length() > 0) buffer.append(",");
            buffer.append(value.trim());
        }
        myProps.put(key, buffer.toString().intern());
    }

    public String getMyProp(String key) {
        return myProps.get(key);
    }

    public String[] getMyMultiProp(String key) {
        String value = myProps.get(key);
        if (value == null) return new String[0];
        return value.split(",");
    }

    /* returns a list of labels for a select box */
    public String[] getLabels(String key) {
        return myLabels.get(key);
    }

    /* returns a list of values for a select box */
    public String[] getValues(String key) {
        return myValues.get(key);
    }

    void setMyProps(Map<String, String> props) {
        myProps = new LinkedHashMap<String, String>(props);
    }

    public Map<String, String> getMyProps() {
        return new LinkedHashMap<String, String>(myProps);
    }

    void setMyLabels(String labelName, String[] labels) {
        myLabels.put(labelName, labels);
    }

    Map<String, String[]> getMyLabels() {
        return new LinkedHashMap<String, String[]>(myLabels);
    }

    void setMyValues(String valueName, String[] values) {
        myValues.put(valueName, values);
    }

    Map<String, String[]> getMyValues() {
        return new LinkedHashMap<String, String[]>(myValues);
    }

    public Object getMyPropObject(String propName) {
        return myPropObjects.get(propName);
    }

    public void setMyPropObject(String propName, Object propValue) {
        myPropObjects.put(propName, propValue);
    }
}
