package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.User;
import org.gusdb.wdk.model.UserAnswer;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

import java.util.LinkedHashMap;
import java.util.Map;

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
    public void addAnswer(AnswerBean answer) throws WdkUserException {
        user.addAnswer(answer.answer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#addAnswerFuzzy(org.gusdb.wdk.model.Answer)
     */
    public void addAnswerFuzzy(AnswerBean answer) throws WdkUserException {
        user.addAnswerFuzzy(answer.answer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#combineAnswers(int, int, java.lang.String)
     */
    public UserAnswerBean combineUserAnswers(int firstAnswerID,
            int secondAnswerID, String operation, int start, int end,
            Map<String, String> operatorMap)
            throws WdkUserException, WdkModelException {
        return new UserAnswerBean(this.user.combineUserAnswers(firstAnswerID,
                secondAnswerID, operation, start, end, operatorMap));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#combineAnswers(java.lang.String)
     */
    public UserAnswerBean combineAnswers(String expression, int start, int end,
            Map<String, String> operatorMap)
            throws WdkUserException, WdkModelException {
        return new UserAnswerBean(this.user.combineUserAnswers(expression,
                start, end, operatorMap));
    }

    public String validateExpression(String expression, int startIndex,
            int endIndex, Map<String, String> operatorMap) throws WdkModelException
    {
	return this.user.validateExpression(expression, startIndex, endIndex,
					    operatorMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#deleteAnswer(int)
     */
    public void deleteUserAnswer(int answerId) throws WdkUserException {
        this.user.deleteUserAnswer(answerId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#clearAnswers()
     */
    public void clearUserAnswers() throws WdkUserException {
        this.user.clearUserAnswers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#getAnswerByID(int)
     */
    public UserAnswerBean getUserAnswerByID(int answerID)
            throws WdkUserException {
        return new UserAnswerBean(this.user.getUserAnswerByID(answerID));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#getAnswerByName(java.lang.String)
     */
    public UserAnswerBean getUserAnswerByName(String name)
            throws WdkUserException {
        return new UserAnswerBean(this.user.getUserAnswerByName(name));
    }

    public int getUserAnswerIdByAnswer(AnswerBean answer)
            throws WdkUserException {
	return getUserAnswerByAnswerFuzzy(answer).getAnswerID();
    }

    public UserAnswerBean getUserAnswerByAnswer(AnswerBean answer)
            throws WdkUserException {
        return new UserAnswerBean(user.getUserAnswerByAnswer(answer.answer));
    }

    public UserAnswerBean getUserAnswerByAnswerFuzzy(AnswerBean answer)
            throws WdkUserException {
        return new UserAnswerBean(user.getUserAnswerByAnswerFuzzy(answer.answer));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#getAnswers()
     */
    public UserAnswerBean[] getUserAnswers() {
        UserAnswer[] answers = user.getUserAnswers();
        UserAnswerBean[] answerBeans = new UserAnswerBean[answers.length];
        for (int i = 0; i < answers.length; i++) {
            answerBeans[i] = new UserAnswerBean(answers[i]);
        }
        return answerBeans;
    }

    public int getAnswerCount() {
        return user.getUserAnswers().length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.User#getRecordAnswerMap()
     */
    public Map<String, UserAnswerBean[]> getRecordAnswerMap() {
        Map recUsrAnsMap = user.getRecordAnswerMap();
        Map<String, UserAnswerBean[]> recUsrAnsBeanMap = new LinkedHashMap<String, UserAnswerBean[]>();
        for (Object r : recUsrAnsMap.keySet()) {
            String rec = (String) r;
            UserAnswer[] usrAns = (UserAnswer[]) recUsrAnsMap.get(rec);
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
    public void renameUserAnswer(int answerID, String name)
            throws WdkUserException {
        this.user.renameUserAnswer(answerID, name);
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
