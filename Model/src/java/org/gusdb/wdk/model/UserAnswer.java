package org.gusdb.wdk.model;

import java.util.Map;

/**
 * a non-persistent mapping of a User to an Answer. All it adds is the ability
 * for the User to rename the Answer. (future persistent subclass will add as
 * state the UserAnswerId so it can be persisted)
 */
public class UserAnswer {

    private String userID;
    private String name;
    private int answerID;
    private Answer answer;
    private boolean combinedAnswer = false;

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
            StringBuffer nameBuf = new StringBuffer();
            // nameBuf.append("[" + answerID + "]");
            nameBuf.append(answer.getQuestion().getDisplayName() + ": ");

            Map<String, Param> params = answer.getQuestion().getParamMap();
            Map<String, Object> paramValues = answer.getParams();

            boolean first = true;
            for (String paramName : paramValues.keySet()) {
                Param param = params.get(paramName);
                Object value = paramValues.get(paramName);
                if (first) first = false;
                else nameBuf.append(", ");
                nameBuf.append("<b>" + param.getPrompt() + "</b>=" + value);
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

    public boolean isCombinedAnswer() {
        return this.combinedAnswer;
    }

    void setCombinedAnswer(boolean combinedAnswer) {
        this.combinedAnswer = combinedAnswer;
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

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();

        // print basic information of this UserAnswer
        try {
            sb.append("UserID=" + userID);
            sb.append("\tAnswerID=" + answerID);
            sb.append("\tType=" + getType());
            sb.append("\tSize=" + answer.getResultSize());
            sb.append("\tAnswerName=" + getName());
            sb.append(newline);
            // do not load record info
            sb.append(answer.toString());
        } catch (WdkModelException ex) {
            ex.printStackTrace();
            // System.err.println(ex);
        }
        sb.append(newline);
        return sb.toString();
    }
}
