/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Jerric
 * @created Aug 18, 2005
 */
public class SanityUser {

    protected String userID;
    protected Set<SanityUserAnswer> answers;

    public SanityUser() {
        answers = new HashSet<SanityUserAnswer>();
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void addUserAnswer(SanityUserAnswer answer) {
        answers.add(answer);
    }

    public SanityUserAnswer[] getUserAnswers() {
        SanityUserAnswer[] anrs = new SanityUserAnswer[answers.size()];
        answers.toArray(anrs);
        return anrs;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("userID=" + userID + "\n");
        for (SanityUserAnswer answer : answers) {
            sb.append("\t" + answer.toString() + "\n");
        }
        return sb.toString();
    }
}
