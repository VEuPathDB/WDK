package org.gusdb.wdk.model;

import java.util.HashMap;
import java.util.Map;

/**
 * A wdk model user.  The WdkModel has a list of Users, and maintains
 * unique identifiers for them.  These will likely be the session id for 
 * non-persistent users.
 */
public class User { 

    HashMap userAnswers;
    int answerIndex = 1;

    public void addAnswer(Answer answer) {
        UserAnswer userAnswer = new UserAnswer(answer);
        userAnswers.put(new Integer(answerIndex++), userAnswer);
    }

    public void deleteAnswer(int answerId) {
        userAnswers.remove(new Integer(answerId));
    }

    public Map getAnswers() {
	return userAnswers;
    }
}
