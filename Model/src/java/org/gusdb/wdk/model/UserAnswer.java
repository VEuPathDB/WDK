package org.gusdb.wdk.model;

import java.util.Map;
import java.util.Iterator;

/**
 * a non-persistent mapping of a User to an Answer. All it adds is the ability
 * for the User to rename the Answer. (future persistent subclass will add as
 * state the UserAnswerId so it can be persisted)
 */
public class UserAnswer {

    protected String userID;
    protected String name;
    protected int answerID;
    private Answer answer;

    UserAnswer(String userID, int answerID) {
        this.userID = userID;
        this.answerID = answerID;
        this.answer = null;
    }

    public UserAnswer(String userID, int answerID, Answer answer) {
        this.userID = userID;
        this.answerID = answerID;
        this.answer = answer;
    }

    public String getUserID() {
        return this.userID;
    }

    public int getAnswerID() {
        return this.answerID;
    }

    /*
     * by default this is "complete" (unique) description of the Answer, ie, the
     * question display name and the list of parameter values as found in the
     * QueryInstance table. the view truncates it as needed to make it fit into
     * its display. the user also can rename it to some name of his/her creation
     */
    public String getName() {
        if (name == null) {
            StringBuffer nameBuf = new StringBuffer(
                    answer.getQuestion().getDisplayName());

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

    public String getName(int truncateTo) {
        return getName().substring(0, truncateTo);
    }

    void setName(String name) {
        this.name = name;
    }

    public Answer getAnswer() {
        return answer;
    }

    void setAnswer(Answer answer) {
        this.answer = answer;
    }

    /**
     * The Type of an answer is used in boolean combinations. Only answers of
     * the same type can be combined together
     * 
     * @return returns the type of this UserAnswer
     */
    public String getType() {
        Question question = answer.getQuestion();
        // use the question set name as the type of this user answer
        String fullName = question.getFullName().trim();
        int pos = fullName.indexOf(".");
        return fullName.substring(0, pos);
    }

    public UserAnswer clone(int answerID) {
        UserAnswer ans = new UserAnswer(userID, answerID, answer);
        if (name != null) ans.setName(name);
        return ans;
    }
}
