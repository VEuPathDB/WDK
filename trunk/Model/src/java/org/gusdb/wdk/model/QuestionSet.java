package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Iterator;

/**
 * QuestionSet.java
 *
 * Created: Fri June 4 15:05:30 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */


public class QuestionSet implements ModelSetI {

    LinkedHashMap questionSet;
    String name;
    String displayName;
    String description;

    Boolean internal = new Boolean(false);

    public QuestionSet() {
	questionSet = new LinkedHashMap();
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public void setDisplayName(String displayName) {
	this.displayName = displayName;
    }

    public String getDisplayName() {
	return (displayName != null)? displayName : name;
    }

     public void setDescription(String description) {
	this.description = description;
    }

    public String getDescription() {
	return description;
    }

    public Boolean getInternal(){
	return this.internal;
    }
    
    public void setInternal(Boolean internal){
	this.internal = internal;
    }

    public Question getQuestion(String name) throws WdkUserException {

	Question s = (Question)questionSet.get(name);
	if (s == null) throw new WdkUserException("Question Set " + getName() + " does not include question " + name);
	return s;
    }

    public Object getElement(String name) {
	return questionSet.get(name);
    }

    public Question[] getQuestions() {
	Question[] questions = new Question[questionSet.size()];
	Iterator questionIterator = questionSet.values().iterator();
	int i = 0;
	while (questionIterator.hasNext()) {
	    questions[i++] = (Question)questionIterator.next();
	}
	return questions;
    }

    public void addQuestion(Question question) throws WdkModelException {

	if (questionSet.get(question.getName()) != null) 
	    throw new WdkModelException("Question named " 
					+ question.getName() 
					+ " already exists in question set "
					+ getName());
	
	questionSet.put(question.getName(), question);
    }

    public void resolveReferences(WdkModel model) throws WdkModelException{
	Iterator questionIterator = questionSet.values().iterator();
	while (questionIterator.hasNext()){
	    Question question = (Question)questionIterator.next();
	    question.resolveReferences(model);
	}
    }

    public void setResources(WdkModel model) throws WdkModelException {
	Iterator questionIterator = questionSet.values().iterator();
	while (questionIterator.hasNext()){
	    Question question = (Question)questionIterator.next();
	    question.setQuestionSet(this);
	    RecordClass rc = question.getRecordClass();
	    rc.addQuestion(question);
	    question.setResources(model);
	}
	
    }

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = 
	   new StringBuffer("QuestionSet: name='" + getName() + "'" + newline +
			    "  displayName='" + getDisplayName() + "'" + newline +
			    "  description='" + getDescription() + "'" + newline +
			    "  isInternal='" + getInternal() + "'" + newline);
       buf.append( newline );

       Iterator questionIterator = questionSet.values().iterator();
       while (questionIterator.hasNext()) {
	   buf.append( newline );
	   buf.append( ":::::::::::::::::::::::::::::::::::::::::::::" );
	   buf.append( newline );
	   buf.append(questionIterator.next()).append( newline );
       }

       return buf.toString();
	
    }

}
