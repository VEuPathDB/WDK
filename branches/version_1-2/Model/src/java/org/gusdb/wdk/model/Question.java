package org.gusdb.wdk.model;

import java.util.Map;
import java.util.Iterator;
import java.util.Vector;
import java.util.Hashtable;
import java.util.HashMap;

/**
 * Question.java
 *
 * A class representing a binding between a RecordClass and a Query.
 *
 * Created: Fri June 4 11:19:30 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

public class Question {

    private String recordClassTwoPartName;

    private String queryTwoPartName;

    private String name;

    private String displayName;

    private String description;

    private String help;

    private QuestionSet questionSet;

    private Query query;
    
    protected RecordClass recordClass;

    private String summaryAttributesRef;

    private Map summaryAttributeMap;

    ///////////////////////////////////////////////////////////////////////
    // setters called at initialization
    ///////////////////////////////////////////////////////////////////////
    
    public Question(){
	summaryAttributeMap = new HashMap();
    }


    public void setName(String name){
	this.name = name;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public void setHelp(String help) {
	this.help = help;
    }

    public void setRecordClassRef(String recordClassTwoPartName){

	this.recordClassTwoPartName = recordClassTwoPartName;
    }

    public void setQueryRef(String queryTwoPartName){

	this.queryTwoPartName = queryTwoPartName;
    }

    public void setDisplayName(String displayName) {
	this.displayName = displayName;
    }

    public void setSummaryAttributesRef(String summaryAttributesRef){
	this.summaryAttributesRef = summaryAttributesRef;
    }


    ///////////////////////////////////////////////////////////////////////


    public Answer makeAnswer(Map paramValues, int i, int j) throws WdkUserException, WdkModelException{
	
	QueryInstance qi = query.makeInstance();
	qi.setValues(paramValues);
	Answer answer = 
	    new Answer(this, qi, i, j);
	
	return answer;
    }

    public Param[] getParams() {
	return query.getParams();
    }

    public String getDescription() {
	return description;
    }

    public String getHelp() {
	return help;
    }

    public String getDisplayName() {
	return displayName;
    }
	
    public RecordClass getRecordClass(){
	return this.recordClass;
    }

    Query getQuery(){
	return this.query;
    }

    public void setRecordClass(RecordClass rc){
	this.recordClass = rc;
    }
    
    public void setQuery(Query q){
	this.query = q;
    }

    public String getName(){
	return name;
    }

    public String getFullName() {
	return questionSet.getName() + "." + name;
    }

    public String toString() {
	String newline = System.getProperty( "line.separator" );
	StringBuffer buf =
	    new StringBuffer("Question: name='" + name + "'" + newline  +
			     "  recordClass='" + recordClassTwoPartName + "'" + newline +
			     "  query='" + queryTwoPartName + "'" + newline +
			    "  displayName='" + getDisplayName() + "'" + newline +
			    "  description='" + getDescription() + "'" + newline +
			    "  help='" + getHelp() + "'" + newline 
			     );	    
	return buf.toString();
    }
    

    ///////////////////////////////////////////////////////////////////////
    // package methods
    ///////////////////////////////////////////////////////////////////////


    void resolveReferences(WdkModel model)throws WdkModelException{
	
	this.query = (Query)model.resolveReference(queryTwoPartName, name, "question", "queryRef");
	this.recordClass = (RecordClass)model.resolveReference(recordClassTwoPartName, name, "question", "recordClassRef");
	Map attribFields = getRecordClass().getAttributeFields();

	String[] summaryAttributeList;

	if (summaryAttributesRef != null){
	    String primaryKey = "primaryKey";
	    if (!(summaryAttributesRef.equals(primaryKey)
		  || summaryAttributesRef.startsWith(primaryKey + ",")
		  || summaryAttributesRef.endsWith("," + primaryKey)
		  || summaryAttributesRef.indexOf("," + primaryKey + ",") > 0)) {
		summaryAttributesRef = primaryKey + "," + summaryAttributesRef;
	    }
	    summaryAttributeList = summaryAttributesRef.split(",");

	    for (int i = 0; i< summaryAttributeList.length; i++){
		String nextAtt = summaryAttributeList[i];
		if (attribFields.get(nextAtt) == null){
		    throw new WdkModelException("Question " + getName() + " defined attribute " +
						nextAtt + " as a summary attribute, but that is not a " + 
						"valid attribute for this Question");
		}
	    }
	} else {
	    Iterator ai = attribFields.keySet().iterator();
	    Vector v = new Vector();
	    while (ai.hasNext()) {
		v.add((String)ai.next());
	    }
	    summaryAttributeList = new String[v.size()];
	    v.copyInto(summaryAttributeList);
	}
	for (int i = 0; i < summaryAttributeList.length; i++){
	    String nextSummaryAtt = summaryAttributeList[i];
	    summaryAttributeMap.put(nextSummaryAtt, new Integer(1));
	}
    }

    boolean isSummaryAttribute(String attName){
	Object isSummaryAtt = summaryAttributeMap.get(attName);
	if (isSummaryAtt == null){
	    return false;
	}
	else {
	    return true;
	}
    }

    Map getSummaryAttributes(){
	return summaryAttributeMap;
    }

    
    void setSummaryAttributes(Map summaryAtts){
	this.summaryAttributeMap = summaryAtts;
    }


    ///////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////
        
    

    protected void setQuestionSet(QuestionSet questionSet) {
	this.questionSet = questionSet;
    }

}
