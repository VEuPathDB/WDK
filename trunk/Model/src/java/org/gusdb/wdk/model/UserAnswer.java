package org.gusdb.wdk.model;

import java.util.Hashtable;
import java.util.Map;

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

            for (Object key : params.keySet()) {
                nameBuf.append(" " + key + ":" + params.get(key));
            }
            nameBuf.append(" (" + answerID + ")");
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
     * The Type of an answer is used in defined as the name of recordClassSet of
     * the record in the answer
     * 
     * @return returns the type of this UserAnswer
     */
    public String getType() {
        String fullName = answer.getQuestion().getRecordClass().getFullName();
        int pos = fullName.indexOf(".");
        return (pos < 0) ? fullName : fullName.substring(0, pos);
    }

    UserAnswer clone(int answerID) {
        UserAnswer ans = new UserAnswer(userID, answerID, answer);
        if (name != null) ans.setName(name);
        return ans;
    }

    BooleanQuestionNode getLeafQuestion() {
        BooleanQuestionNode leaf = new BooleanQuestionNode(
                answer.getQuestion(), null);
        leaf.setValues(new Hashtable(answer.getParams()));
        return leaf;
    }
}
