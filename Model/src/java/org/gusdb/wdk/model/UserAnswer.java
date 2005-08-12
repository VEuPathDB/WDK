package org.gusdb.wdk.model;

import java.util.Map;
import java.util.Iterator;

/**
 * a non-persistent mapping of a User to an Answer.  All it adds is
 * the ability for the User to rename the Answer.
 * (future persistent subclass will add as state the UserAnswerId so
 * it can be persisted)
 */
public class UserAnswer { 

    String name;
    Answer answer;
    

    public UserAnswer (Answer answer) {
	this.answer = answer;
    }

    /* by default this is "complete" (unique) description of the Answer, 
     * ie, the question display name and the list of parameter values as 
     * found in the QueryInstance table.  the view truncates it as needed 
     * to make it fit into its display.  the user also can rename it to 
     * some name of his/her creation
     */
    public String getName() {	
	if (name == null) {
	    StringBuffer nameBuf = 
		new StringBuffer(answer.getQuestion().getDisplayName());
	    
	    Map params = answer.getParams();
	    Iterator paramKeys = params.keySet().iterator();

	    while (paramKeys.hasNext()) {
		Object key = paramKeys.next();
		nameBuf.append(" " + key + ":" + params.get(key));
	    }
	    name = nameBuf.toString();
	}
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public Answer getAnswer() {
	return answer;
    }
}
