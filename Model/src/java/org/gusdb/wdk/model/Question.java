package org.gusdb.wdk.model;

import java.util.Map;

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
    
    private QueryInstance listIdQueryInstance;

    private RecordClass recordClass;

    ///////////////////////////////////////////////////////////////////////
    // setters called at initialization
    ///////////////////////////////////////////////////////////////////////
    
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

    ///////////////////////////////////////////////////////////////////////


    public SummaryInstance makeSummaryInstance(Map paramValues, int i, int j) throws WdkUserException, WdkModelException{
	
	if (listIdQueryInstance == null){
	    listIdQueryInstance = query.makeInstance();
	}
	//return new SummaryInstance(this, listIdQueryInstance);
	SummaryInstance summaryInstance = 
	    new SummaryInstance(this, query.makeInstance(), paramValues, i, j);
	return summaryInstance;
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
    }

    protected void setQuestionSet(QuestionSet questionSet) {
	this.questionSet = questionSet;
    }

}
