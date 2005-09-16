
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.UserAnswer;

public class UserAnswerBean {

    UserAnswer userAnswer;
    int nameTruncateTo;

    public UserAnswerBean(UserAnswer answer) {
        this.userAnswer = answer;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.UserAnswer#getAnswer()
     */
    public AnswerBean getAnswer() {
        return new AnswerBean(this.userAnswer.getAnswer());
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

    public void setNameTruncateTo(int truncateTo) {
	nameTruncateTo = truncateTo;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.UserAnswer#getName(int)
     */
    public String getTruncatedName() {
        return this.userAnswer.getName(nameTruncateTo);
    }

    public boolean getIsNameTruncatable() {
	return getName().length() > nameTruncateTo ? true : false;
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
