/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.xml.XmlAnswerValue;
import org.gusdb.wdk.model.xml.XmlQuestion;

/**
 * @author Jerric
 * @created Oct 19, 2005
 */
public class XmlQuestionBean {

    private static final Logger logger = Logger.getLogger(XmlQuestionBean.class);

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
     * @see org.gusdb.wdk.model.xml.XmlQuestion#makeAnswer(java.util.Map, int,
     * int)
     */
    public XmlAnswerBean makeAnswer(Map<String, String> params, int startIndex,
            int endIndex) throws WdkModelException {
        XmlAnswerValue answer = question.makeAnswer(params, startIndex,
                endIndex);
        return new XmlAnswerBean(answer);
    }

    public XmlAnswerBean getFullAnswer() throws WdkModelException {
        try {
            XmlAnswerBean a = makeAnswer(null, 1, 3);
            int c = a.getResultSize();
            return makeAnswer(null, 1, c);
        } catch (WdkModelException ex) {
            logger.error("Error on getting answer from xmlQuestion '"
                    + getFullName() + "': " + ex);
            ex.printStackTrace();
            throw ex;
        }
    }
}
