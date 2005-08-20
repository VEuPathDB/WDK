package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.UserAnswer;

public class UserAnswerBean {

    UserAnswer userAnswer;

    public UserAnswerBean(UserAnswer answer) {
        this.userAnswer = answer;
    }

    public int getAnswerID() {
        return userAnswer.getAnswerID();
    }

    public String getName() {
        return userAnswer.getName();
    }

    public String getUserID() {
        return userAnswer.getUserID();
    }

    public void setName(String name) {
        userAnswer.setName(name);
    }

    public AnswerBean getAnswer() {
        return new AnswerBean(userAnswer.getAnswer());
    }
}
