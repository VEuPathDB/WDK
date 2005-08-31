package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.User;
import org.gusdb.wdk.model.UserAnswer;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

public class UserBean {

    User user;

    public UserBean(User user) {
        this.user = user;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#addAnswer(org.gusdb.wdk.model.Answer)
     */
    public UserAnswer addAnswer(Answer answer) {
        return this.user.addAnswer(answer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#combineAnswers(int, int, java.lang.String)
     */
    public UserAnswer combineAnswers(int firstAnswerID, int secondAnswerID,
            String operation) throws WdkUserException, WdkModelException {
        return this.user.combineAnswers(firstAnswerID, secondAnswerID,
                operation);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#combineAnswers(java.lang.String)
     */
    public UserAnswer combineAnswers(String expression)
            throws WdkUserException, WdkModelException {
        return this.user.combineAnswers(expression);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#deleteAnswer(int)
     */
    public void deleteAnswer(int answerId) throws WdkUserException {
        this.user.deleteAnswer(answerId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#clearAnswers()
     */
    public void clearAnswers() {
        this.user.clearAnswers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#getAnswerByID(int)
     */
    public UserAnswer getAnswerByID(int answerID) throws WdkUserException {
        return this.user.getAnswerByID(answerID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#getAnswerByName(java.lang.String)
     */
    public UserAnswer getAnswerByName(String name) throws WdkUserException {
        return this.user.getAnswerByName(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#getAnswers()
     */
    public UserAnswerBean[] getAnswers() {
        UserAnswer[] answers = user.getAnswers();
        UserAnswerBean[] answerBeans = new UserAnswerBean[answers.length];
        for (int i = 0; i < answers.length; i++) {
            answerBeans[i] = new UserAnswerBean(answers[i]);
        }
        return answerBeans;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#getUserID()
     */
    public String getUserID() {
        return this.user.getUserID();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#renameAnswer(int, java.lang.String)
     */
    public void renameAnswer(int answerID, String name) throws WdkUserException {
        this.user.renameAnswer(answerID, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.user.toString();
    }

}
