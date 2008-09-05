/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.xml.XmlAnswerValue;
import org.gusdb.wdk.model.xml.XmlQuestion;

/**
 * @author Jerric
 * @created Oct 19, 2005
 */
public class XmlQuestionBean {

    private XmlQuestion question;

    /**
     * 
     */
    public XmlQuestionBean(XmlQuestion question) {
        this.question = question;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlQuestion#getDescription()
     */
    public String getDescription() {
        return this.question.getDescription();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlQuestion#getDisplayName()
     */
    public String getDisplayName() {
        return this.question.getDisplayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlQuestion#getFullName()
     */
    public String getFullName() {
        return this.question.getFullName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlQuestion#getHelp()
     */
    public String getHelp() {
        return this.question.getHelp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlQuestion#getName()
     */
    public String getName() {
        return this.question.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlQuestion#getName()
     */
    public String getXmlDataURL() {
        return this.question.getXmlDataURL();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlQuestion#getRecordClass()
     */
    public XmlRecordClassBean getRecordClass() {
        return new XmlRecordClassBean(question.getRecordClass());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlQuestion#makeAnswerValue(java.util.Map, int,
     *      int)
     */
    public XmlAnswerValueBean makeAnswerValue(Map<String, String> params, int startIndex,
            int endIndex) throws WdkModelException {
        XmlAnswerValue answer = question.makeAnswerValue(params, startIndex, endIndex);
        return new XmlAnswerValueBean(answer);
    }

    public XmlAnswerValueBean getFullAnswerValue() throws WdkModelException {
	
	XmlAnswerValueBean a = makeAnswerValue(null, 1, 3);
	int c = a.getResultSize();
	return makeAnswerValue(null, 1, c);
    }
}
