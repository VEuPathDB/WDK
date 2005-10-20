/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.xml.XmlQuestion;
import org.gusdb.wdk.model.xml.XmlQuestionSet;

/**
 * @author Jerric
 * @created Oct 19, 2005
 */
public class XmlQuestionSetBean {

    private XmlQuestionSet questionSet;

    public XmlQuestionSetBean(XmlQuestionSet questionSet) {
        this.questionSet = questionSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlQuestionSet#getDescription()
     */
    public String getDescription() {
        return this.questionSet.getDescription();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlQuestionSet#getDisplayName()
     */
    public String getDisplayName() {
        return this.questionSet.getDisplayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlQuestionSet#getName()
     */
    public String getName() {
        return this.questionSet.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlQuestionSet#getQuestions()
     */
    public XmlQuestionBean[] getQuestions() {
        XmlQuestion[] questions = questionSet.getQuestions();
        XmlQuestionBean[] questionBeans = new XmlQuestionBean[questions.length];
        for (int i = 0; i < questions.length; i++) {
            questionBeans[i] = new XmlQuestionBean(questions[i]);
        }
        return questionBeans;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlQuestionSet#isInternal()
     */
    public boolean isInternal() {
        return this.questionSet.isInternal();
    }
}