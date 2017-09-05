package org.gusdb.wdk.model.jspwrap;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.xml.XmlAnswerValue;
import org.gusdb.wdk.model.xml.XmlQuestion;

/**
 * @author Jerric
 * @created Oct 19, 2005
 */
public class XmlQuestionBean {

    private static final Logger LOG = Logger.getLogger(XmlQuestionBean.class);

    private XmlQuestion _question;

    public XmlQuestionBean(XmlQuestion question) {
        _question = question;
    }

    public String getDescription() {
        return _question.getDescription();
    }

    public String getDisplayName() {
        return _question.getDisplayName();
    }

    public String getFullName() {
        return _question.getFullName();
    }

    public String getHelp() {
        return _question.getHelp();
    }

    public String getName() {
        return _question.getName();
    }

    public String getXmlDataURL() {
        return _question.getXmlDataURL();
    }

    public XmlRecordClassBean getRecordClass() {
        return new XmlRecordClassBean(_question.getRecordClass());
    }

    public XmlAnswerBean makeAnswer(int startIndex,
            int endIndex) throws WdkModelException {
        XmlAnswerValue answer = _question.makeAnswer(startIndex, endIndex);
        return new XmlAnswerBean(answer);
    }

    public XmlAnswerBean getFullAnswer() throws WdkModelException {
        try {
            XmlAnswerBean a = makeAnswer(1, 3);
            int c = a.getResultSize();
            return makeAnswer(1, c);
        } catch (WdkModelException ex) {
            LOG.error("Error on getting answer from xmlQuestion '"
                    + getFullName() + "': " + ex);
            ex.printStackTrace();
            throw ex;
        }
    }
}
