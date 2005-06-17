package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.BooleanQuery;

import java.util.Map;
import java.util.Iterator;
import java.util.Vector;

/**
 * A wrapper on a {@link Answer} that provides simplified access for 
 * consumption by a view
 */ 
public class AnswerBean {

    Answer answer;
    Map downloadConfigMap = null;

    public AnswerBean(Answer answer) {
	this.answer = answer;
    }

    /**
     * @return A Map of param displayName --> param value.
     */
    public Map getParams() {
	return answer.getDisplayParams();
    }

    /**
     * @return opertation for boolean answer
     */
    public String getBooleanOperation() {
	System.err.println("the param map is: " + answer.getParams());
	if (!getIsBoolean()) {
	    throw new RuntimeException("getBooleanOperation can not be called on simple AnswerBean");
	}
	return (String)answer.getParams().get(BooleanQuery.OPERATION_PARAM_NAME);
    }

    /**
     * @return first child answer for boolean answer, null if it is an answer for a simple question.
     */
    public AnswerBean getFirstChildAnswer() {
	if (!getIsBoolean()) {
	    throw new RuntimeException("getFirstChildAnswer can not be called on simple AnswerBean");
	}
	Object childAnswer = answer.getParams().get(BooleanQuery.FIRST_ANSWER_PARAM_NAME);
	return new AnswerBean((Answer)childAnswer);
    }

    /**
     * @return second child answer for boolean answer, null if it is an answer for a simple question.
     */
    public AnswerBean getSecondChildAnswer() {
	if (!getIsBoolean()) {
	    throw new RuntimeException("getSecondChildAnswer can not be called on simple AnswerBean");
	}
	Object childAnswer = answer.getParams().get(BooleanQuery.SECOND_ANSWER_PARAM_NAME);
	return new AnswerBean((Answer)childAnswer);
    }

    public int getPageSize() {
	return answer.getPageSize();
    }

    public int getResultSize() {
 	try {
	    return answer.getResultSize();
	} catch (WdkModelException e) {
	    throw new RuntimeException(e);
	}
    }

    public boolean getIsBoolean(){
	return answer.getIsBoolean();
    }

    public RecordClassBean getRecordClass() {
	return new RecordClassBean(answer.getQuestion().getRecordClass());
    }

    public QuestionBean getQuestion() {
	return new QuestionBean(answer.getQuestion());
    }

    /**
     * @return A list of {@link RecordBean}s.
     */
    public Iterator getRecords() {
	return new RecordBeanList();
    }

    /**
     * for controller: reset counter for download purpose
     */
    public void resetAnswerRowCursor() {
	answer.resetRecordInstanceCounter();
    }

    public void setDownloadConfigMap (Map downloadConfigMap) {
	this.downloadConfigMap = downloadConfigMap;
    }

    public String[] getSummaryAttributeNames() {
	Map attribs = answer.getQuestion().getRecordClass().getAttributeFields();
	Iterator ai = attribs.keySet().iterator();
	Vector v = new Vector();
	while (ai.hasNext()) {
	    String attribName = (String)ai.next();
	    if (answer.isSummaryAttribute(attribName)) {
		v.add(attribName);
	    }
	}
	int size = v.size();
	String[] sumAttribNames = new String[size];
	v.copyInto(sumAttribNames);
	return sumAttribNames;
    }

    public String[] getDownloadAttributeNames() {
	String[] sumAttrNames = getSummaryAttributeNames();

	Vector v = new Vector();
	for (int i=0; i<sumAttrNames.length; i++) {
	    String attrName = sumAttrNames[i];
	    if (downloadConfigMap == null) {
		v.add(attrName);
	    } else {
		Object configStatus = downloadConfigMap.get(attrName);
		System.err.println("DEBUG AnswerBean: configStatus for " + attrName + " is " + configStatus);
		if (configStatus != null) { v.add(attrName); }
	    }
	}
	int size = v.size();
	String[] downloadAttribNames = new String[size];
	v.copyInto(downloadAttribNames);
	return downloadAttribNames;
    }

    ////////////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////////////

    class RecordBeanList implements Iterator {

	public int getSize() {
	    return answer.getPageSize();
	}
    
	public boolean hasNext() {
	    try {
		return answer.hasMoreRecordInstances();
	    } catch (WdkModelException exp) {
                throw new RuntimeException(exp);
            }
	}
	
	public Object next() {
	    try {
		return new RecordBean(answer.getNextRecordInstance());
	    }
	    catch (WdkModelException exp) {
		throw new RuntimeException(exp);
	    }
	}
    
	public void remove() {
	    throw new UnsupportedOperationException("remove isn't allowed on this iterator");
	} 
	
    }
    

}
