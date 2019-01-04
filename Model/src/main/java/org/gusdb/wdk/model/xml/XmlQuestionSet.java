package org.gusdb.wdk.model.xml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.ModelSetI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

/**
 * @author Jerric
 * @created Oct 14, 2005
 */
public class XmlQuestionSet extends WdkModelBase implements ModelSetI<XmlQuestion> {

    private String _name;
    private String _displayName;

    private List<WdkModelText> _descriptions = new ArrayList<WdkModelText>();
    private String _description;

    private boolean _isInternal;

    private List<XmlQuestion> _questionList = new ArrayList<XmlQuestion>();
    private Map<String, XmlQuestion> _questions = new LinkedHashMap<String, XmlQuestion>();

    @Override
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return _description;
    }

    /**
     * @param description
     *                The description to set.
     */
    public void addDescription(WdkModelText description) {
        _descriptions.add(description);
    }

    /**
     * @return Returns the displayName.
     */
    public String getDisplayName() {
        return _displayName;
    }

    /**
     * @param displayName
     *                The displayName to set.
     */
    public void setDisplayName(String displayName) {
        _displayName = displayName;
    }

    /**
     * @return Returns the isInternal.
     */
    public boolean isInternal() {
        return _isInternal;
    }

    /**
     * @param isInternal
     *                The isInternal to set.
     */
    public void setInternal(boolean isInternal) {
        _isInternal = isInternal;
    }

    public XmlQuestion getQuestion(String name) throws WdkModelException {
        XmlQuestion question = _questions.get(name);
        if (question == null)
            throw new WdkModelException("Question " + name
                    + " not found in set " + getName());
        return question;
    }

    public XmlQuestion[] getQuestions() {
        XmlQuestion[] quesArray = new XmlQuestion[_questions.size()];
        _questions.values().toArray(quesArray);
        return quesArray;
    }

    public Map<String,XmlQuestion> getQuestionsMap() {
      return _questions;
    }

    public void addQuestion(XmlQuestion question) {
        _questionList.add(question);
    }

    @Override
    public XmlQuestion getElement(String elementName) {
        return _questions.get(elementName);
    }

    @Override
    public void resolveReferences(WdkModel model) throws WdkModelException {
        // resolve the references of questions
        for (XmlQuestion question : _questions.values()) {
            question.resolveReferences(model);
        }
    }

    @Override
    public void setResources(WdkModel model) throws WdkModelException {
        // set resources for questions
        for (XmlQuestion question : _questions.values()) {
            question.setQuestionSet(this);
            question.setResources(model);
        }
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("QuestionSet: name='");
        buf.append(getName());
        buf.append("'\r\n\tdisplayName='");
        buf.append(getDisplayName());
        buf.append("'\r\n\tdescription='");
        buf.append(getDescription());
        buf.append("'\r\n\r\n");

        for (XmlQuestion question : _questions.values()) {
            buf.append("\r\n:::::::::::::::::::::::::::::::::::::::::::::\r\n");
            buf.append(question);
            buf.append("\r\n");
        }
        return buf.toString();
    }

    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude descriptions
        boolean hasDescription = false;
        for (WdkModelText description : _descriptions) {
            if (description.include(projectId)) {
                if (hasDescription) {
                    throw new WdkModelException("The xmlQuestionSet "
                            + getName() + " has more than one description "
                            + "for project " + projectId);
                } else {
                    _description = description.getText();
                    hasDescription = true;
                }
            }
        }
        _descriptions = null;

        // exclude xml questions
        for (XmlQuestion question : _questionList) {
            if (question.include(projectId)) {
                question.setQuestionSet(this);
                question.excludeResources(projectId);
                String questionName = question.getName();
                if (_questions.containsKey(questionName))
                    throw new WdkModelException("The xmlQuestion "
                            + questionName + " is duplicated in the "
                            + "xmlQuestionSet " + _name);
                _questions.put(questionName, question);
            }
        }
        _questionList = null;
    }
}
