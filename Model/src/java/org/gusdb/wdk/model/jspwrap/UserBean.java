package org.gusdb.wdk.model.jspwrap;

import java.util.Map;

import org.gusdb.wdk.model.User;
import org.gusdb.wdk.model.UserAnswer;

public class UserBean {

    User user;

    public UserBean(User user) {
        this.user = user;
    }

    public UserAnswerBean addAnswer(AnswerBean answerBean) {
        UserAnswer userAnswer = user.addAnswer(answerBean.answer);
        return new UserAnswerBean(userAnswer);
    }

    public boolean deleteAnswer(int answerId) {
        return user.deleteAnswer(answerId);
    }

    public UserAnswerBean[] getAnswers() {
        Map<Integer, UserAnswer> answers = user.getAnswers();

        UserAnswerBean[] answerBeans = new UserAnswerBean[answers.size()];
        int i = 0;
        for (int answerID : answers.keySet()) {
            UserAnswer userAnswer = answers.get(answerID);
            answerBeans[i] = new UserAnswerBean(userAnswer);
            i++;
        }
        return answerBeans;
    }

    public String getUserID() {
        return user.getUserID();
    }
}
