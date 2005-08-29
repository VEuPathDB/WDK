package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.UserAnswer;

public class UserAnswerBean {

    UserAnswer userAnswer;

    public UserAnswerBean(UserAnswer answer) {
        this.userAnswer = answer;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.UserAnswer#getAnswer()
     */
    public Answer getAnswer() {
        return this.userAnswer.getAnswer();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.UserAnswer#getAnswerID()
     */
    public int getAnswerID() {
        return this.userAnswer.getAnswerID();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.UserAnswer#getName()
     */
    public String getName() {
        return this.userAnswer.getName();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.UserAnswer#getName(int)
     */
    public String getName(int truncateTo) {
        return this.userAnswer.getName(truncateTo);
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.UserAnswer#getType()
     */
    public String getType() {
        return this.userAnswer.getType();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.UserAnswer#getUserID()
     */
    public String getUserID() {
        return this.userAnswer.getUserID();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.userAnswer.toString();
    }
}
