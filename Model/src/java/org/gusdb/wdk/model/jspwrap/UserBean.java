package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.User;
import org.gusdb.wdk.model.UserAnswer;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

import java.util.Map;
import java.util.HashMap;

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
    public void addAnswer(AnswerBean answer) {
        user.addAnswer(answer.answer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#combineAnswers(int, int, java.lang.String)
     */
    public UserAnswerBean combineAnswers(int firstAnswerID, int secondAnswerID,
            String operation, int start, int end)
            throws WdkUserException, WdkModelException {
        return new UserAnswerBean(this.user.combineAnswers(firstAnswerID,
                secondAnswerID, operation, start, end));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#combineAnswers(java.lang.String)
     */
    public UserAnswerBean combineAnswers(String expression, int start, int end)
            throws WdkUserException, WdkModelException {
        return new UserAnswerBean(this.user.combineAnswers(expression, start,
                end));
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
    public UserAnswerBean getAnswerByID(int answerID) throws WdkUserException {
        return new UserAnswerBean(this.user.getAnswerByID(answerID));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#getAnswerByName(java.lang.String)
     */
    public UserAnswerBean getAnswerByName(String name) throws WdkUserException {
        return new UserAnswerBean(this.user.getAnswerByName(name));
    }

    public UserAnswerBean getAnswerByAnswer(AnswerBean answer)
            throws WdkUserException {
        return new UserAnswerBean(user.getAnswerByAnswer(answer.answer));
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

    public int getAnswerCount() {
	return user.getAnswers().length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#getRecordAnswerMap()
     */
    public Map getRecordAnswerMap() {
        Map recUsrAnsMap = user.getRecordAnswerMap();
	Map recUsrAnsBeanMap = new HashMap<String, UserAnswerBean[]>();
	for (Object r : recUsrAnsMap.keySet()) {
	    String rec = (String)r;
	    UserAnswer[] usrAns = (UserAnswer[])recUsrAnsMap.get(rec);
	    UserAnswerBean[] answerBeans = new UserAnswerBean[usrAns.length];
	    for (int i = 0; i < usrAns.length; i++) {
		answerBeans[i] = new UserAnswerBean(usrAns[i]);
	    }
	    recUsrAnsBeanMap.put(rec, answerBeans);
	}
        return recUsrAnsBeanMap;
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
