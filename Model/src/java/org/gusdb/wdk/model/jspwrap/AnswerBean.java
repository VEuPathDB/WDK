package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.WdkModelException;

import java.util.Map;
import java.util.Iterator;

/**
 * A wrapper on a {@link Answer} that provides simplified access for 
 * consumption by a view
 */ 
public class AnswerBean {

    Answer answer;
    

    public AnswerBean(Answer answer) {
	this.answer = answer;
    }

    /**
     * @return A Map of param displayName --> param value.
     */
    public Map getParams() {
	return answer.getDisplayParams();
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
